package com.example.emailclient;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class EmailAdapter extends ListAdapter<Email, EmailAdapter.VH> {

    public interface Listener {
        void onStarClick(Email email);
    }

    private static final DiffUtil.ItemCallback<Email> DIFF = new DiffUtil.ItemCallback<Email>() {
        @Override
        public boolean areItemsTheSame(@NonNull Email a, @NonNull Email b) {
            return a.getId().equals(b.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Email a, @NonNull Email b) {
            return a.contentEquals(b);
        }
    };

    private final String folder;
    private final Listener listener;

    public EmailAdapter(String folder, Listener listener) {
        super(DIFF);
        this.folder = folder;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_email, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Email e = getItem(position);
        h.tvSender.setText(e.getSender());
        h.tvSubject.setText(e.getSubject());
        h.tvPreview.setText(e.getPreview());
        h.tvDate.setText(e.getDate());

        String sender = e.getSender() == null ? "" : e.getSender();
        h.tvAvatar.setText(sender.isEmpty()
                ? "?" : sender.substring(0, 1).toUpperCase(Locale.getDefault()));

        int style = e.isRead() ? Typeface.NORMAL : Typeface.BOLD;
        h.tvSender.setTypeface(null, style);
        h.tvSubject.setTypeface(null, style);

        h.tvStar.setText(e.isStarred() ? "\u2605" : "\u2606");
        h.tvStar.setOnClickListener(v -> listener.onStarClick(e));

        h.itemView.setOnClickListener(v -> {
            Intent intent;
            if (folder.contains("Drafts")) {
                intent = new Intent(v.getContext(), ComposeActivity.class);
                intent.putExtra("draftId", e.getId());
            } else {
                intent = new Intent(v.getContext(), MailDetailActivity.class);
                intent.putExtra("id", e.getId());
                intent.putExtra("folder", folder);
            }
            v.getContext().startActivity(intent);
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvSender;
        final TextView tvSubject;
        final TextView tvPreview;
        final TextView tvDate;
        final TextView tvStar;

        VH(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvSender = v.findViewById(R.id.tvSender);
            tvSubject = v.findViewById(R.id.tvSubject);
            tvPreview = v.findViewById(R.id.tvPreview);
            tvDate = v.findViewById(R.id.tvDate);
            tvStar = v.findViewById(R.id.tvStar);
        }
    }
}
