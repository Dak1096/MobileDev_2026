package com.example.emailclient;

import android.content.Context;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;

public class ApiClient {
    private static final String BASE = "https://jsonplaceholder.typicode.com";

    // Một RequestQueue duy nhất cho cả app
    private static RequestQueue queue;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private static RequestQueue queue(Context ctx) {
        if (queue == null) {
            queue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return queue;
    }

    public static void get(Context ctx, String path, Callback<String> cb) {
        StringRequest req = new StringRequest(
                Request.Method.GET, BASE + path,
                response -> cb.onSuccess(response),
                error -> cb.onError(error));
        req.setRetryPolicy(new DefaultRetryPolicy(8000, 0, 1f));
        queue(ctx).add(req);
    }

    public static void post(Context ctx, String path, String jsonBody, Callback<String> cb) {
        StringRequest req = new StringRequest(
                Request.Method.POST, BASE + path,
                response -> cb.onSuccess(response),
                error -> cb.onError(error)) {
            @Override
            public byte[] getBody() {

                return jsonBody.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {

                return "application/json; charset=UTF-8";
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(8000, 0, 1f));
        queue(ctx).add(req);
    }
}