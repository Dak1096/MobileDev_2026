package com.example.emailclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class InboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        DrawerLayout drawer = findViewById(R.id.drawer);
        NavigationView nav = findViewById(R.id.navView);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ExtendedFloatingActionButton fab = findViewById(R.id.fabCompose);

        String account = AuthStore.get(this).currentEmail();
        if (account != null) toolbar.setSubtitle(account);

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        toolbar.inflateMenu(R.menu.menu_inbox);
        Menu menu = toolbar.getMenu();
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                applyQuery(query);
                return true;
            }
        });
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                refresh();
                return true;
            }
            return false;
        });

        nav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                AuthStore.get(this).logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            String folder = "INBOX";
            if (id == R.id.nav_starred)          folder = "[Gmail]/Starred";
            else if (id == R.id.nav_sent)        folder = "[Gmail]/Sent Mail";
            else if (id == R.id.nav_drafts)      folder = "[Gmail]/Drafts";
            else if (id == R.id.nav_spam)        folder = "[Gmail]/Spam";
            else if (id == R.id.nav_trash)       folder = "[Gmail]/Trash";

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

    private MailListFragment currentFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        return fragment instanceof MailListFragment ? (MailListFragment) fragment : null;
    }

    private void applyQuery(String query) {
        MailListFragment fragment = currentFragment();
        if (fragment != null) fragment.applyQuery(query);
    }

    private void refresh() {
        MailListFragment fragment = currentFragment();
        if (fragment != null) fragment.refresh();
    }
}
