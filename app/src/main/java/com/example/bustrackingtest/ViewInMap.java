package com.example.bustrackingtest;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class ViewInMap extends AppCompatActivity {
    BottomSheetBehavior sheetBehavior;
    EditText lateMinutes;
    RadioGroup statusGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_in_map);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });

        Configuration.getInstance().setUserAgentValue(getPackageName());

        MapView map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //enable zoom in and out
        map.setMultiTouchControls(true);

        IMapController ctrl = map.getController();
        ctrl.setZoom(17.0);
        ctrl.setCenter(new GeoPoint(11.759431, 76.006005));

        String busName = getIntent().getStringExtra("bus_name");
        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        Marker marker = new Marker(map);
        Drawable drawable = getDrawable(R.drawable.bus);

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        View sheet = findViewById(R.id.bottomSheet);

        sheetBehavior = BottomSheetBehavior.from(sheet);
        sheetBehavior.setPeekHeight(200);

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

        bell.setOnClickListener(v -> {

            bell.setSelected(!bell.isSelected());

        });

        findViewById(R.id.submit).setOnClickListener(v -> {

            int id = statusGroup.getCheckedRadioButtonId();

            if(id == -1){
                Toast.makeText(this,"Select an option",Toast.LENGTH_SHORT).show();
                return;
            }

            String mins = lateMinutes.getText().toString();

            Toast.makeText(this,"Feedback Submitted", Toast.LENGTH_SHORT).show();
        });

        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        Drawable smallDrawable = new BitmapDrawable(getResources(), smallBitmap);
        AtomicReference<Boolean> zoomOneTime = new AtomicReference<>(true);

            new Thread(() -> {
                while(true) {
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

                                GeoPoint point = new GeoPoint(lat, lng);
                                marker.setPosition(point);
                                marker.setIcon(smallDrawable);
                                marker.setAnchor(0.5f, 0.9f);
                                if(zoomOneTime.get()) {
                                    map.getController().animateTo(point);
                                    zoomOneTime.set(false);
                                    map.getController().setZoom(15.0);
                                }

                                map.getOverlays().clear();
                                map.getOverlays().add(marker);

                                map.invalidate();

                            });
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
            }).start();
    }
}