package com.example.emailclient;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class EmailRepository {

    private EmailRepository() {
    }

    public static void syncFolder(Context ctx, String folder, ApiClient.Callback<List<Email>> cb) {
        final String canon = MailStore.canon(folder);
        if ("DRAFTS".equals(canon) || "STARRED".equals(canon)) {
            cb.onSuccess(MailStore.get(ctx).getMessages(folder));
            return;
        }
        ApiClient.get(pathFor(canon), body -> parseComments(body, canon),
                new ApiClient.Callback<List<Email>>() {
                    @Override
                    public void onSuccess(List<Email> serverMails) {
                        MailStore store = MailStore.get(ctx);
                        store.upsertServer(serverMails, canon);
                        store.markSynced(canon);
                        cb.onSuccess(store.getMessages(folder));
                    }

                    @Override
                    public void onError(Exception e) {
                        cb.onError(e);
                    }
                });
    }

    private static List<Email> parseComments(String body, String canon) throws JSONException {
        JSONArray arr = new JSONArray(body);
        List<Email> out = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            int sid = o.optInt("id");
            Email e = new Email(
                    o.optString("email"),
                    o.optString("name"),
                    o.optString("body"),
                    "#" + sid);
            e.setId(canon + ":" + sid);
            e.setSortKey(sid);
            out.add(e);
        }
        return out;
    }

    static String pathFor(String canon) {
        switch (canon) {
            case "SENT":   return "/comments?postId=2";
            case "SPAM":   return "/comments?postId=4";
            case "TRASH":  return "/comments?postId=5";
            default:       return "/comments?_limit=30";
        }
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
        ApiClient.post("/posts", json, r -> r.contains("\"id\""), new ApiClient.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean ok) {
                if (Boolean.TRUE.equals(ok)) {
                    MailStore.get(ctx).addSent(to, subject, body);
                }
                cb.onSuccess(ok);
            }

            @Override
            public void onError(Exception e) {
                cb.onError(e);
            }
        });
    }
}
