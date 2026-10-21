package com.example.emailclient;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EmailRepository {

    // Thư do app tự giữ cho từng thư mục (server giả không lưu gì cả)
    private static final Map<String, List<Email>> local = new HashMap<>();
    // Khóa của các thư từ server đã bị chuyển đi hoặc xóa, để không hiện lại
    private static final Set<String> hidden = new HashSet<>();

    static {
        for (String f : new String[]{"INBOX", "SENT", "DRAFTS", "SPAM", "TRASH"}) {
            local.put(f, new ArrayList<>());
        }
    }

    //Hàm tiện ích

    public static String key(Email e) {
        return e.getSender() + "|" + e.getSubject() + "|" + e.getDate();
    }

    public static String canon(String folder) {
        if (folder.contains("Sent"))   return "SENT";
        if (folder.contains("Drafts")) return "DRAFTS";
        if (folder.contains("Spam"))   return "SPAM";
        if (folder.contains("Trash"))  return "TRASH";
        return "INBOX";
    }

    private static String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    private static void removeByKey(List<Email> list, String k) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (key(list.get(i)).equals(k)) list.remove(i);
        }
    }

    private static void removeEverywhere(String k) {
        for (List<Email> l : local.values()) removeByKey(l, k);
    }

    // Chuyển, xóa, nháp

    public static void moveEmail(Email e, String target) {
        String k = key(e);
        removeEverywhere(k);
        hidden.add(k);
        local.get(target).add(0, e);
    }

    public static void deleteForever(Email e) {
        String k = key(e);
        removeEverywhere(k);
        hidden.add(k);
    }

    public static void saveDraft(String oldKey, String to, String subject, String body) {
        if (oldKey != null) discardDraft(oldKey);
        local.get("DRAFTS").add(0, new Email(to, subject, body, now()));
    }

    public static void discardDraft(String k) {
        removeByKey(local.get("DRAFTS"), k);
        hidden.add(k);
    }

    // Gọi mạng

    public static void login(Context ctx, String email, ApiClient.Callback<Boolean> cb) {
        String path;
        try {
            path = "/users?email=" + URLEncoder.encode(email, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            cb.onError(e);
            return;
        }
        ApiClient.get(ctx, path, new ApiClient.Callback<String>() {
            @Override
            public void onSuccess(String body) {
                try {
                    cb.onSuccess(new JSONArray(body).length() > 0);
                } catch (JSONException e) {
                    cb.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

    public static void fetchFolder(Context ctx, String folder,
                                   ApiClient.Callback<List<Email>> cb) {
        String path = "/comments?_limit=30";
        if (folder.contains("Sent"))        path = "/comments?postId=2";
        else if (folder.contains("Drafts")) path = "/comments?postId=3";
        else if (folder.contains("Spam"))   path = "/comments?postId=4";
        else if (folder.contains("Trash"))  path = "/comments?postId=5";

        ApiClient.get(ctx, path, new ApiClient.Callback<String>() {
            @Override
            public void onSuccess(String body) {
                try {
                    JSONArray arr = new JSONArray(body);
                    List<Email> result = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        Email m = new Email(
                                o.optString("email"),
                                o.optString("name"),
                                o.optString("body").replace("\n", " "),
                                "#" + o.optInt("id"));
                        if (!hidden.contains(key(m))) result.add(m);
                    }
                    result.addAll(0, local.get(canon(folder)));
                    cb.onSuccess(result);
                } catch (JSONException e) {
                    cb.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }

    public static void sendEmail(Context ctx, String to, String subject, String body,
                                 ApiClient.Callback<Boolean> cb) {
        String json;
        try {
            JSONObject o = new JSONObject();
            o.put("to", to);
            o.put("title", subject);
            o.put("body", body);
            o.put("userId", 1);
            json = o.toString();
        } catch (JSONException e) {
            cb.onError(e);
            return;
        }
        ApiClient.post(ctx, "/posts", json, new ApiClient.Callback<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    boolean ok = new JSONObject(response).has("id");
                    if (ok) {
                        local.get("SENT").add(0,
                                new Email(to, subject, body.replace("\n", " "), now()));
                    }
                    cb.onSuccess(ok);
                } catch (JSONException e) {
                    cb.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }
}