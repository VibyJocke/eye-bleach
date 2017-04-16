package com.lahtinen.apps.eyebleach;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Map<String, JsonNode> cache = new HashMap<>();
        final ObjectMapper mapper = new ObjectMapper();
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final SimpleDraweeView imageView = (SimpleDraweeView) findViewById(R.id.image);

        findViewById(R.id.generate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button pressed, executing a GiphyTask");
                new GiphyTask(cache, mapper, httpClient, imageView).execute();
            }
        });

        new GiphyTask(cache, mapper, httpClient, imageView).execute();
    }
}
