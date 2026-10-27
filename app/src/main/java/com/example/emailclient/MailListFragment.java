package com.example.emailclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MailListFragment extends Fragment {

    private static final String ARG_FOLDER = "folder";

    private RecyclerView rvMails;
    private TextView tvEmpty;
    private View progress;
    private EmailAdapter adapter;
    private MailListViewModel viewModel;
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
        if (getArguments() != null) {
            folder = getArguments().getString(ARG_FOLDER, "INBOX");
        }

        rvMails = view.findViewById(R.id.rvMails);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progress = view.findViewById(R.id.progress);

        rvMails.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMails.setHasFixedSize(true);
        rvMails.setItemViewCacheSize(20);

        adapter = new EmailAdapter(folder, email -> {
            if (viewModel != null) viewModel.toggleStar(email.getId());
        });
        rvMails.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MailListViewModel.class);

        viewModel.getMessages().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading ->
                progress.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));
        viewModel.getToastRes().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                Toast.makeText(requireContext(), res, Toast.LENGTH_SHORT).show();
                viewModel.clearToast();
            }
        });

        viewModel.open(folder);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.reload();
    }

    public void applyQuery(String query) {
        if (viewModel != null) viewModel.setQuery(query);
    }

    public void refresh() {
        if (viewModel != null) viewModel.refresh();
    }
}
