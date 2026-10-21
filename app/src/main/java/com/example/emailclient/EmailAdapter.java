package com.example.emailclient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.VH> {
    private String folder = "INBOX";

    public void setFolder(String folder) {
        this.folder = folder;
    }

    private final List<Email> list;

    public EmailAdapter(List<Email> list) {
        this.list = list;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvSender, tvSubject, tvPreview, tvDate;

        VH(View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvSender = v.findViewById(R.id.tvSender);
            tvSubject = v.findViewById(R.id.tvSubject);
            tvPreview = v.findViewById(R.id.tvPreview);
            tvDate = v.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_email, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Email e = list.get(position);
        holder.tvSender.setText(e.getSender());
        holder.tvSubject.setText(e.getSubject());
        holder.tvPreview.setText(e.getPreview());
        holder.tvDate.setText(e.getDate());

        String sender = e.getSender();
        holder.tvAvatar.setText(sender.isEmpty() ? "?" : sender.substring(0, 1).toUpperCase());

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (folder.contains("Drafts")) {
                // Bấm vào nháp thì mở lại màn soạn thư với dữ liệu điền sẵn
                intent = new Intent(v.getContext(), ComposeActivity.class);
                intent.putExtra("draftKey", EmailRepository.key(e));
                intent.putExtra("to", e.getSender());
                intent.putExtra("subject", e.getSubject());
                intent.putExtra("body", e.getPreview());
            } else {
                intent = new Intent(v.getContext(), MailDetailActivity.class);
                intent.putExtra("sender", e.getSender());
                intent.putExtra("subject", e.getSubject());
                intent.putExtra("date", e.getDate());
                intent.putExtra("body", e.getPreview());
                intent.putExtra("folder", folder);
            }
            v.getContext().startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}