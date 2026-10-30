package com.example.emailclient;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SessionManagerTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation()
                .getTargetContext();
        sessionManager = new SessionManager(context);
        sessionManager.clearSession();
    }

    @After
    public void tearDown() {
        sessionManager.clearSession();
    }

    @Test
    public void savedAccountCanBeReadByAnotherSessionManager() {
        sessionManager.saveSignedInEmail("student@example.com");

        SessionManager anotherInstance = new SessionManager(
                InstrumentationRegistry.getInstrumentation().getTargetContext()
        );

        assertEquals("student@example.com", anotherInstance.getSignedInEmail());
    }

    @Test
    public void clearingSessionRemovesTheSignedInAccount() {
        sessionManager.saveSignedInEmail("student@example.com");

        sessionManager.clearSession();

        assertNull(sessionManager.getSignedInEmail());
    }

    @Test
    public void blankAccountIsNotTreatedAsSignedIn() {
        sessionManager.saveSignedInEmail("   ");

        assertNull(sessionManager.getSignedInEmail());
    }
}