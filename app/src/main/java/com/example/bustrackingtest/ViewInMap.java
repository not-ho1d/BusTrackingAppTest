package com.example.bustrackingtest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class ViewInMap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_in_map);

        Configuration.getInstance().setUserAgentValue(getPackageName());

        MapView map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //enable zoom in and out
        map.setMultiTouchControls(true);

        IMapController ctrl = map.getController();
        ctrl.setZoom(17.0);
        ctrl.setCenter(new GeoPoint(11.759431, 76.006005));
    }
}