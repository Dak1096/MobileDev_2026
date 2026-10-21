package com.example.emailclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MailListFragment extends Fragment {

    private static final String ARG_FOLDER = "folder";

    private RecyclerView rvMails;
    private String folder = "INBOX";

    public static MailListFragment newInstance(String folder) {
        MailListFragment fragment = new MailListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOLDER, folder);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail_list, container, false);

        rvMails = view.findViewById(R.id.rvMails);
        rvMails.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            folder = getArguments().getString(ARG_FOLDER, "INBOX");
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMails();   // nạp lại mỗi lần quay về màn này
    }

    private void loadMails() {
        EmailRepository.fetchFolder(requireContext(), folder,
                new ApiClient.Callback<List<Email>>() {
                    @Override
                    public void onSuccess(List<Email> emails) {
                        if (!isAdded()) return;
                        EmailAdapter adapter = new EmailAdapter(emails);
                        adapter.setFolder(folder);
                        rvMails.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                R.string.error_load_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}