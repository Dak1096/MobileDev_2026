package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class InboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        DrawerLayout drawer = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.navView);
        TextView signOutButton = findViewById(R.id.btnSignOut);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ExtendedFloatingActionButton composeButton = findViewById(R.id.fabCompose);
        SessionManager sessionManager = new SessionManager(getApplicationContext());

        String accountFromSession = sessionManager.getSignedInEmail();
        String account = accountFromSession == null
                ? getIntent().getStringExtra("email")
                : accountFromSession;

        if (account != null) {
            toolbar.setSubtitle(account);
        }

        toolbar.setNavigationOnClickListener(view ->
                drawer.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            String folder = getFolderForMenuItem(itemId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MailListFragment.newInstance(folder))
                    .commit();

            toolbar.setTitle(item.getTitle());
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        signOutButton.setOnClickListener(view -> signOut(drawer, sessionManager));

        composeButton.setOnClickListener(view ->
                startActivity(new Intent(this, ComposeActivity.class))
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MailListFragment.newInstance("INBOX"))
                    .commit();
        }
    }

    private String getFolderForMenuItem(int itemId) {
        if (itemId == R.id.nav_sent) {
            return "[Gmail]/Sent Mail";
        }

        if (itemId == R.id.nav_drafts) {
            return "[Gmail]/Drafts";
        }

        if (itemId == R.id.nav_spam) {
            return "[Gmail]/Spam";
        }

        if (itemId == R.id.nav_trash) {
            return "[Gmail]/Trash";
        }

        return "INBOX";
    }

    private void signOut(DrawerLayout drawer, SessionManager sessionManager) {
        drawer.closeDrawer(GravityCompat.START);
        sessionManager.clearSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}