package com.example.emailclient;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;

public class InboxActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        DrawerLayout drawer = findViewById(R.id.drawer);
        NavigationView nav = findViewById(R.id.navView);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ExtendedFloatingActionButton fab = findViewById(R.id.fabCompose);
        String account = getIntent().getStringExtra("email");
        if (account != null) {
            toolbar.setSubtitle(account);
        }

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        nav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            String folder = "INBOX";
            if (id == R.id.nav_sent)        folder = "[Gmail]/Sent Mail";
            else if (id == R.id.nav_drafts) folder = "[Gmail]/Drafts";
            else if (id == R.id.nav_spam)   folder = "[Gmail]/Spam";
            else if (id == R.id.nav_trash)  folder = "[Gmail]/Trash";

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MailListFragment.newInstance(folder))
                    .commit();
            toolbar.setTitle(item.getTitle());
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        fab.setOnClickListener(v -> startActivity(new Intent(this, ComposeActivity.class)));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MailListFragment.newInstance("INBOX"))
                    .commit();
        }
    }
}