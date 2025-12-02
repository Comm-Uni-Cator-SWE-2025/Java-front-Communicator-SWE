package com.swe.launcher;

import java.util.concurrent.atomic.AtomicBoolean;

public class FrontendStateChecker {
    private static final AtomicBoolean UI_READY = new AtomicBoolean(false);

    public static boolean isUIReady() {
        return UI_READY.get();
    }

    public static void markUIReady() {
        UI_READY.set(true);
    }
}
