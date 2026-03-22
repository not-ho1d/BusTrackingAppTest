package com.example.bustrackingtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {

    Handler handler = new Handler();
    Runnable runnable;
    // Add this inside your LocationService class
    private boolean hasLocationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1️⃣ Immediately show notification
        startForeground(1, createNotification());

        // 2️⃣ Now start your location Runnable
        String busName = intent.getStringExtra("bus_name");

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    boolean isTracking = pref.getBoolean("is_tracking", false);
                    if (!isTracking) {
                        handler.postDelayed(this, 1000);
                        return;
                    }

                    if (!hasLocationPermission()) return;

                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location == null) location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();


                        new Thread(() -> {
                            try {
                                JSONObject data = new JSONObject();
                                data.put("action", "get_driver_location");
                                data.put("latitude", lat);
                                data.put("longitude", lng);
                                data.put("bus_name", busName);

                                String server_url = pref.getString("server_url", "http://192.168.1.5:8000/Api/");

                                HttpURLConnection conn = (HttpURLConnection) new URL(server_url).openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);
                                conn.setRequestProperty("Content-Type", "application/json");
                                OutputStream os = conn.getOutputStream();
                                os.write(data.toString().getBytes("UTF-8"));
                                os.close();
                                conn.getResponseCode();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }

                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

        return START_STICKY;
    }

    private Notification createNotification() {
        String channelId = "location_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Bus Tracking Active")
                .setContentText("Sharing live location...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}