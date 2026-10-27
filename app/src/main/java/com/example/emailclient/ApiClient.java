package com.example.emailclient;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApiClient {

    private static final String BASE = "https://jsonplaceholder.typicode.com";
    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public interface Parser<T> {
        T parse(String body) throws Exception;
    }

    private ApiClient() {
    }

    public static <T> void get(String path, Parser<T> parser, Callback<T> cb) {
        request("GET", path, null, parser, cb);
    }

    public static <T> void post(String path, String jsonBody, Parser<T> parser, Callback<T> cb) {
        request("POST", path, jsonBody, parser, cb);
    }

    private static <T> void request(String method, String path, String body,
                                    Parser<T> parser, Callback<T> cb) {
        IO.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(BASE + path).openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty("Accept", "application/json");
                if (body != null) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }
                }
                int code = conn.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                String text = read(is);
                conn.disconnect();
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException("HTTP " + code);
                }
                T parsed = parser.parse(text);
                MAIN.post(() -> cb.onSuccess(parsed));
            } catch (Exception e) {
                MAIN.post(() -> cb.onError(e));
            }
        });
    }

    private static String read(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
