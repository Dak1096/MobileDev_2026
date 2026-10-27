package com.example.emailclient;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MailStore {

    private static final String PREF = "mail_store";
    private static final String K_MAILS = "mails";
    private static final String K_DELETED = "deleted";
    private static final String K_SYNCED = "synced";

    private static MailStore instance;

    private final SharedPreferences prefs;
    private final LinkedHashMap<String, Email> mails = new LinkedHashMap<>();
    private final Set<String> deleted = new HashSet<>();
    private final Set<String> synced = new HashSet<>();
    private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final Comparator<Email> NEWEST_FIRST = new Comparator<Email>() {
        @Override
        public int compare(Email a, Email b) {
            return Long.compare(b.getSortKey(), a.getSortKey());
        }
    };

    private MailStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized MailStore get(Context ctx) {
        if (instance == null) instance = new MailStore(ctx);
        return instance;
    }

    public static String canon(String folder) {
        if (folder == null) return "INBOX";
        String f = folder.toLowerCase(Locale.ROOT);
        if (f.contains("starred")) return "STARRED";
        if (f.contains("sent"))    return "SENT";
        if (f.contains("drafts"))  return "DRAFTS";
        if (f.contains("spam"))    return "SPAM";
        if (f.contains("trash"))   return "TRASH";
        return "INBOX";
    }

    public synchronized List<Email> getMessages(String folder) {
        String c = canon(folder);
        List<Email> out = new ArrayList<>();
        for (Email e : mails.values()) {
            if ("STARRED".equals(c)) {
                if (e.isStarred() && !"TRASH".equals(e.getFolder()) && !"SPAM".equals(e.getFolder())) {
                    out.add(e);
                }
            } else if (c.equals(e.getFolder())) {
                out.add(e);
            }
        }
        Collections.sort(out, NEWEST_FIRST);
        return out;
    }

    public synchronized List<Email> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Email> out = new ArrayList<>();
        for (Email e : mails.values()) {
            if ("TRASH".equals(e.getFolder()) || "SPAM".equals(e.getFolder())) continue;
            String haystack = (e.getSender() + " " + e.getSubject()).toLowerCase(Locale.ROOT);
            if (haystack.contains(q)) out.add(e);
        }
        Collections.sort(out, NEWEST_FIRST);
        return out;
    }

    public synchronized void upsertServer(List<Email> list, String folder) {
        for (Email e : list) {
            e.setFolder(folder);
            Email current = mails.get(e.getId());
            if (current == null) {
                if (deleted.contains(e.getId())) continue;
                mails.put(e.getId(), e);
            } else {
                current.setSender(e.getSender());
                current.setSubject(e.getSubject());
                current.setBody(e.getBody());
                current.setDate(e.getDate());
            }
        }
        persist();
    }

    public synchronized boolean hasSynced(String folder) {
        return synced.contains(canon(folder));
    }

    public synchronized void markSynced(String folder) {
        synced.add(canon(folder));
        persist();
    }

    public synchronized Email findById(String id) {
        return mails.get(id);
    }

    public synchronized void move(String id, String targetFolder) {
        Email e = mails.get(id);
        if (e == null) return;
        e.setFolder(canon(targetFolder));
        persist();
    }

    public synchronized void deleteForever(String id) {
        if (id == null) return;
        mails.remove(id);
        deleted.add(id);
        persist();
    }

    public synchronized void markRead(String id) {
        Email e = mails.get(id);
        if (e != null && !e.isRead()) {
            e.setRead(true);
            persist();
        }
    }

    public synchronized void toggleStar(String id) {
        Email e = mails.get(id);
        if (e != null) {
            e.setStarred(!e.isStarred());
            persist();
        }
    }

    public synchronized String saveDraft(String existingId, String to, String subject, String body) {
        if (existingId != null) {
            Email draft = mails.get(existingId);
            if (draft != null) {
                draft.setSender(to);
                draft.setSubject(subject);
                draft.setBody(body);
                persist();
                return existingId;
            }
        }
        Email e = new Email(to, subject, body, nowTime());
        e.setId("DRAFTS:l" + System.nanoTime());
        e.setFolder("DRAFTS");
        e.setSortKey(System.currentTimeMillis());
        mails.put(e.getId(), e);
        persist();
        return e.getId();
    }

    public synchronized void discardDraft(String id) {
        if (id != null) {
            mails.remove(id);
            deleted.add(id);
            persist();
        }
    }

    public synchronized void addSent(String to, String subject, String body) {
        Email e = new Email(to, subject, body, nowTime());
        e.setId("SENT:l" + System.nanoTime());
        e.setFolder("SENT");
        e.setSortKey(System.currentTimeMillis());
        mails.put(e.getId(), e);
        persist();
    }

    private String nowTime() {
        return clock.format(new Date());
    }

    private void persist() {
        JSONArray arr = new JSONArray();
        for (Email e : mails.values()) arr.put(toJson(e));
        prefs.edit()
                .putString(K_MAILS, arr.toString())
                .putString(K_DELETED, new JSONArray(deleted).toString())
                .putString(K_SYNCED, new JSONArray(synced).toString())
                .apply();
    }

    private void load() {
        try {
            JSONArray arr = new JSONArray(prefs.getString(K_MAILS, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                Email e = fromJson(arr.getJSONObject(i));
                if (e.getId() != null) mails.put(e.getId(), e);
            }
        } catch (JSONException ignored) {
        }
        try {
            JSONArray del = new JSONArray(prefs.getString(K_DELETED, "[]"));
            for (int i = 0; i < del.length(); i++) deleted.add(del.getString(i));
        } catch (JSONException ignored) {
        }
        try {
            JSONArray sy = new JSONArray(prefs.getString(K_SYNCED, "[]"));
            for (int i = 0; i < sy.length(); i++) synced.add(sy.getString(i));
        } catch (JSONException ignored) {
        }
    }

    private static JSONObject toJson(Email e) {
        JSONObject o = new JSONObject();
        try {
            o.put("id", e.getId());
            o.put("sender", e.getSender());
            o.put("subject", e.getSubject());
            o.put("body", e.getBody());
            o.put("date", e.getDate());
            o.put("folder", e.getFolder());
            o.put("read", e.isRead());
            o.put("starred", e.isStarred());
            o.put("sortKey", e.getSortKey());
        } catch (JSONException ignored) {
        }
        return o;
    }

    private static Email fromJson(JSONObject o) {
        Email e = new Email();
        e.setId(o.optString("id", null));
        e.setSender(o.optString("sender"));
        e.setSubject(o.optString("subject"));
        e.setBody(o.optString("body"));
        e.setDate(o.optString("date"));
        e.setFolder(o.optString("folder", "INBOX"));
        e.setRead(o.optBoolean("read"));
        e.setStarred(o.optBoolean("starred"));
        e.setSortKey(o.optLong("sortKey"));
        return e;
    }
}
