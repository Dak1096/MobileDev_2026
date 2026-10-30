package com.example.emailclient;

import android.content.Context;
import android.content.Intent;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class InboxSignOutTest {

    private static final String TEST_EMAIL = "student@example.com";

    private SessionManager sessionManager;
    private ActivityScenario<InboxActivity> inboxScenario;
    private DrawerIdlingResource drawerIdlingResource;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        sessionManager = new SessionManager(context);
        sessionManager.clearSession();
        sessionManager.saveSignedInEmail(TEST_EMAIL);

        Intent intent = new Intent(context, InboxActivity.class);
        intent.putExtra("email", TEST_EMAIL);
        inboxScenario = ActivityScenario.launch(intent);

        inboxScenario.onActivity(activity -> {
            DrawerLayout drawer = activity.findViewById(R.id.drawer);
            drawerIdlingResource = new DrawerIdlingResource(drawer);
        });
        IdlingRegistry.getInstance().register(drawerIdlingResource);
    }

    @After
    public void tearDown() {
        if (drawerIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(drawerIdlingResource);
            drawerIdlingResource.stopObserving();
        }

        if (inboxScenario != null) {
            inboxScenario.close();
        }

        sessionManager.clearSession();
    }

    @Test
    public void signOutReturnsToLoginAndClearsTheSession() {
        inboxScenario.onActivity(activity -> {
            DrawerLayout drawer = activity.findViewById(R.id.drawer);
            drawer.openDrawer(GravityCompat.START);
        });

        onView(withId(R.id.btnSignOut))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        assertNull(sessionManager.getSignedInEmail());
    }

    private static class DrawerIdlingResource implements IdlingResource {

        private final DrawerLayout drawer;
        private ResourceCallback callback;
        private volatile boolean idle = true;

        private final DrawerLayout.DrawerListener drawerListener =
                new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerStateChanged(int newState) {
                        idle = newState == DrawerLayout.STATE_IDLE;

                        if (idle && callback != null) {
                            callback.onTransitionToIdle();
                        }
                    }
                };

        DrawerIdlingResource(DrawerLayout drawer) {
            this.drawer = drawer;
            drawer.addDrawerListener(drawerListener);
        }

        @Override
        public String getName() {
            return "InboxDrawerIdlingResource";
        }

        @Override
        public boolean isIdleNow() {
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.callback = callback;
        }

        void stopObserving() {
            drawer.removeDrawerListener(drawerListener);
        }
    }
}