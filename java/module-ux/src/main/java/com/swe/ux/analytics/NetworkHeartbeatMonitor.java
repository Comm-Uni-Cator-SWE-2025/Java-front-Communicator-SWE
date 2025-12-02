package com.swe.ux.analytics;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/**
 * Simple network heartbeat monitor that periodically pings a well known host
 * and exposes a boolean online/offline signal.
 */
public final class NetworkHeartbeatMonitor {
    /** Time window within which probes must succeed to stay online. */
    private static final long HEARTBEAT_WINDOW_MS = 5_000L;
    /** Interval for running reachability probes. */
    private static final long PROBE_INTERVAL_MS = 3_000L;
    /** Host to ping for reachability. */
    private static final String PROBE_HOST = "www.google.com";
    /** Ping timeout in milliseconds. */
    private static final int PROBE_TIMEOUT_MS = 2_000;
    /** Singleton instance. */
    private static final NetworkHeartbeatMonitor INSTANCE = new NetworkHeartbeatMonitor();

    /** Last outbound timestamp. */
    private volatile long lastOutboundTs;
    /** Last inbound timestamp. */
    private volatile long lastInboundTs;
    /** Cached state. */
    private volatile boolean currentAlive;
    /** Registered listeners. */
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    private NetworkHeartbeatMonitor() {
        final Timer timer = new Timer("network-heartbeat", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                performProbe();
                evaluateStatus();
            }
        }, 0L, PROBE_INTERVAL_MS);
    }

    /**
     * Returns singleton instance.
     */
    public static NetworkHeartbeatMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * Marks general network activity (used by outbound HTTP calls, etc.).
     */
    public void recordActivity() {
        final long now = Instant.now().toEpochMilli();
        lastInboundTs = now;
        lastOutboundTs = now;
        evaluateStatus();
    }

    /**
     * Returns whether recent probes/activity indicate that we are online.
     */
    public boolean isAlive() {
        final long now = Instant.now().toEpochMilli();
        return (now - lastInboundTs) <= HEARTBEAT_WINDOW_MS
                && (now - lastOutboundTs) <= HEARTBEAT_WINDOW_MS;
    }

    /**
     * Adds a listener notified whenever the online state changes.
     * @param listener consumer receiving the latest status
     */
    public void addListener(final Consumer<Boolean> listener) {
        if (listener != null) {
            listeners.add(listener);
            listener.accept(isAlive());
        }
    }

    private void evaluateStatus() {
        final boolean alive = isAlive();
        if (alive != currentAlive) {
            currentAlive = alive;
            for (Consumer<Boolean> listener : listeners) {
                try {
                    listener.accept(alive);
                } catch (Exception ignored) {
                    // ignore broken listener
                }
            }
        }
    }

    private void performProbe() {
        try {
            final InetAddress host = InetAddress.getByName(PROBE_HOST);
            if (host.isReachable(PROBE_TIMEOUT_MS)) {
                recordActivity();
            }
        } catch (IOException ignored) {
            // inability to reach the host will eventually mark status offline
        }
    }
}
