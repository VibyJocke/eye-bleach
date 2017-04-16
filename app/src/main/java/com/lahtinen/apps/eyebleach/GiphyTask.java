package com.lahtinen.apps.eyebleach;

import android.os.AsyncTask;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class GiphyTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "GetGiphyUrlTask";
    private static final String API_KEY = "dc6zaTOxFJmzC"; //This is the public test-key
    private static final List<String> ANIMALS = Arrays.asList("dog", "doggy", "doggo", "corgi", "cat", "kitty");
    private static final String NO_IMAGE_FOUND_URL = "http://blog.mageworx.com/wp-content/uploads/2012/06/Page-Not-Found-2.jpg";
    private static final Random RANDOM = new Random();

    private Map<String, JsonNode> cache;
    private ObjectMapper mapper;
    private OkHttpClient httpClient;
    private SimpleDraweeView imageView;

    GiphyTask(Map<String, JsonNode> cache, ObjectMapper mapper, OkHttpClient httpClient, SimpleDraweeView imageView) {
        this.cache = cache;
        this.mapper = mapper;
        this.httpClient = httpClient;
        this.imageView = imageView;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            final String query = ANIMALS.get(RANDOM.nextInt(ANIMALS.size()));
            if (cache.containsKey(query)) {
                Log.i(TAG, "Reading from cache and displaying " + query);
                final JsonNode data = cache.get(query);
                return data.get(RANDOM.nextInt(data.size()))
                        .path("images")
                        .path("original")
                        .get("webp")
                        .asText();
            } else {
                final String queryUrl = String.format("http://api.giphy.com/v1/gifs/search?q=%s&api_key=%s", query, API_KEY);
                final Request request = new Request.Builder().url(queryUrl).build();
                final Response response = httpClient.newCall(request).execute();
                final JsonNode data = mapper.readTree(response.body().string()).path("data");

                filterOnSize(data);

                if (data.size() == 0) {
                    Log.i(TAG, "Query returned no results (after filtering)");
                    return NO_IMAGE_FOUND_URL;
                } else {
                    cache.put(query, data);
                    Log.i(TAG, "Caching and displaying " + query);
                    return data.get(RANDOM.nextInt(data.size()))
                            .path("images")
                            .path("original")
                            .get("webp").asText();
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to call API", e);
            return NO_IMAGE_FOUND_URL;
        }
    }

    private void filterOnSize(JsonNode node) {
        for (int i = 0; i < node.size(); i++) {
            final int sizeOfImage = node.get(i)
                    .path("images")
                    .path("original")
                    .get("webp_size")
                    .asInt();
            if (sizeOfImage > 750000) {
                ((ArrayNode) node).remove(i);
                Log.i(TAG, "Removing node due to size restriction");
            }
        }
    }

    @Override
    protected void onPostExecute(String url) {
        super.onPostExecute(url);
        imageView.setController(
                Fresco.newDraweeControllerBuilder()
                        .setUri(url)
                        .setAutoPlayAnimations(true)
                        .build()
        );
    }
}
