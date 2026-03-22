package com.example.bustrackingtest;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.views.overlay.Polygon;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;

public class ViewInMap extends AppCompatActivity {
    BottomSheetBehavior sheetBehavior;
    EditText lateMinutes;
    RadioGroup statusGroup;

    volatile Boolean running = true;
    private void sendFeedback(int early, int late) {

        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action", "add_new_timetable");
                data.put("bus_name", ((TextView)findViewById(R.id.busName)).getText().toString());
                data.put("time_bracket", ((TextView)findViewById(R.id.arrivalTime)).getText().toString());
                data.put("early", early);
                data.put("late", late);

                String server_url = getSharedPreferences("pref", MODE_PRIVATE)
                        .getString("server_url", "http://192.168.1.5:8000/Api/");

                URL url = new URL(server_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

                InputStream is;

                if (responseCode >= 200 && responseCode < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String responseText = response.toString();
                JSONObject res = new JSONObject(responseText);

                boolean success = res.optBoolean("success", true);

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Submitted!", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = res.optString("reason", "Failed");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_in_map);

        SharedPreferences prefs = getSharedPreferences("notif_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            running = false;
            finish();
        });

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1024L * 1024L * 200L);
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(1024L * 1024L * 150L);
        MapView map = findViewById(R.id.map);

        ITileSource positronHD = new XYTileSource(
                "CartoDB Positron HD",
                0,
                20,
                256,
                "@2x.png",
                new String[]{
                        "https://a.basemaps.cartocdn.com/light_all/",
                        "https://b.basemaps.cartocdn.com/light_all/",
                        "https://c.basemaps.cartocdn.com/light_all/",
                        "https://d.basemaps.cartocdn.com/light_all/"
                }
        );

        map.setTileSource(positronHD);

    // enable zoom
        map.setMultiTouchControls(true);

        IMapController ctrl = map.getController();
        ctrl.setZoom(17.0);
        ctrl.setCenter(new GeoPoint(11.759431, 76.006005));

        Intent intent = getIntent();

        String busName = intent.getStringExtra("bus_name");
        String routePoly = intent.getStringExtra("route_poly");
        String routeName = intent.getStringExtra("route_name");
        String time = intent.getStringExtra("time");
        //String time = "01:01";
        TextView at = findViewById(R.id.arrivalTime);
        TextView bn = findViewById(R.id.busName);
        TextView rn = findViewById(R.id.routeName);

        at.setText(time);
        bn.setText(busName);
        rn.setText(routeName);


        try {
            JSONObject json = new JSONObject(routePoly);

            JSONArray coords = json.getJSONArray("route_poly");

            ArrayList<GeoPoint> points = new ArrayList<>();

            for(int i = 0; i < coords.length(); i++){

                JSONObject point = coords.getJSONObject(i);

                double lat = point.getDouble("lat");
                double lon = point.getDouble("lng");

                points.add(new GeoPoint(lat, lon));
            }

            Polyline routeLine = new Polyline();
            routeLine.setPoints(points);

            routeLine.getOutlinePaint().setColor(Color.parseColor("#FF6F6F"));
            routeLine.getOutlinePaint().setStrokeWidth(8f);

            routeLine.getOutlinePaint().setAntiAlias(true);
            routeLine.getOutlinePaint().setStrokeCap(android.graphics.Paint.Cap.ROUND);
            routeLine.getOutlinePaint().setStrokeJoin(android.graphics.Paint.Join.ROUND);

            map.getOverlayManager().add(routeLine);
            map.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }


        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        Marker marker = new Marker(map);



        Drawable drawable = getDrawable(R.drawable.bus);
        Polygon circle = new Polygon();
        circle.setFillColor(Color.parseColor("#337EC8FF")); // transparent light blue
        circle.setStrokeColor(Color.parseColor("#7EC8FF"));
        circle.setStrokeWidth(2f);

        map.getOverlayManager().add(circle);


        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        View sheet = findViewById(R.id.bottomSheet);

        sheetBehavior = BottomSheetBehavior.from(sheet);
        sheetBehavior.setPeekHeight(330);

        RadioGroup group = findViewById(R.id.statusGroup);
        EditText early = findViewById(R.id.earlyMinutes);
        EditText late = findViewById(R.id.lateMinutes);

        group.setOnCheckedChangeListener((g,id)->{

            early.setVisibility(View.GONE);
            late.setVisibility(View.GONE);

            if(id == R.id.early)
                early.setVisibility(View.VISIBLE);

            if(id == R.id.late)
                late.setVisibility(View.VISIBLE);

        });
        ImageView bell = findViewById(R.id.bell);

        boolean savedState = prefs.getBoolean("bell_" + time, false);
        bell.setSelected(savedState);

        bell.setOnClickListener(v -> {

            String[] parts = time.split(":");
            int timeh = Integer.parseInt(parts[0]);
            int timem = Integer.parseInt(parts[1]);

            int requestCode = timeh * 100 + timem;

            Intent i = new Intent(this, BusNotification.class);
            i.putExtra("bus_time", time);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    i,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (!bell.isSelected()) {
                bell.setSelected(true);
                editor.putBoolean("bell_" + time, bell.isSelected());
                editor.apply();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timeh);
                calendar.set(Calendar.MINUTE, timem);
                calendar.set(Calendar.SECOND, 1);
                calendar.add(Calendar.MINUTE, -2);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    } else {
                        alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                pendingIntent
                        );
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();

                    // fallback (still schedules, but less precise)
                    alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                }
                Toast.makeText(this, "Notification ON for " + time, Toast.LENGTH_SHORT).show();

            } else {
                bell.setSelected(false);
                editor.putBoolean("bell_" + time, bell.isSelected());
                editor.apply();
                alarmManager.cancel(pendingIntent);

                Toast.makeText(this, "Notification OFF for " + time, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.submit).setOnClickListener(v -> {

            RadioGroup grp = findViewById(R.id.statusGroup);
            int selectedId = grp.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Select early/on time/late", Toast.LENGTH_SHORT).show();
                return;
            }

            EditText earlyMinutes = findViewById(R.id.earlyMinutes);
            EditText lateMinutes = findViewById(R.id.lateMinutes);

            int erly = 0;
            int lte = 0;

            if (selectedId == R.id.early) {
                String val = earlyMinutes.getText().toString();
                erly = val.isEmpty() ? 0 : Integer.parseInt(val);
            }
            else if (selectedId == R.id.late) {
                String val = lateMinutes.getText().toString();
                lte = val.isEmpty() ? 0 : Integer.parseInt(val);
            }

            sendFeedback(erly, lte);
        });

        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        Drawable smallDrawable = new BitmapDrawable(getResources(), smallBitmap);
        AtomicReference<Boolean> zoomOneTime = new AtomicReference<>(true);

            new Thread(() -> {
                while(running) {
                    if (!running) break;
                    try {
                        JSONObject data = new JSONObject();
                        data.put("action", "find_bus_location");
                        data.put("bus_name", busName);
                        String server_url = pref.getString(
                                "server_url",
                                "http://192.168.1.5:8000/Api/"
                        );

                        URL url = new URL(server_url);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);

                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");

                        conn.setConnectTimeout(2000);
                        conn.setReadTimeout(2000);

                        OutputStream os = conn.getOutputStream();
                        os.write(data.toString().getBytes("UTF-8"));
                        os.close();

                        int responseCode = conn.getResponseCode();

                        InputStream is;

                        if (responseCode >= 200 && responseCode < 300) {
                            is = conn.getInputStream();     // success response
                        } else {
                            is = conn.getErrorStream();     // error response
                        }

                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder response = new StringBuilder();

                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        String responseText = response.toString();
                        JSONObject res = new JSONObject(responseText);
                        boolean status = res.getBoolean("live_location");
                        if (status) {

                            JSONObject coord = res.getJSONObject("data");

                            double lat = coord.getDouble("lat");
                            double lng = coord.getDouble("lng");

                            runOnUiThread(() -> {
                                if (!running || isFinishing() || isDestroyed()) return;
                                GeoPoint point = new GeoPoint(lat, lng);
                                marker.setPosition(point);
                                //circle.setPoints(Polygon.pointsAsCircle(circleCenter, 80.0));
                                circle.setPoints(Polygon.pointsAsCircle(point, 280.0)); // 80m radius
                                marker.setIcon(smallDrawable);
                                marker.setAnchor(0.5f, 0.5f);
                                if(zoomOneTime.get()) {
                                    map.getController().animateTo(point);
                                    zoomOneTime.set(false);
                                    map.getController().setZoom(15.0);
                                }

                                if(!map.getOverlays().contains(marker)){
                                    map.getOverlays().add(marker);
                                }

                                map.invalidate();

                            });
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
            }).start();
    }



    @Override
    protected void onDestroy(){
        running=false;
        super.onDestroy();
    }
}