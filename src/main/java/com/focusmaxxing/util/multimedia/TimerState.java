package com.focusmaxxing.util.multimedia;

/**
 * Represents all possible states of the Pomodoro timer.
 *
 * <p>State transition diagram:
 * <pre>
 *   IDLE ──[Start]──► RUNNING ──[Pause]──► PAUSED
 *                        │                    │
 *                        │                 [Resume]
 *                        │                    │
 *                        ◄────────────────────┘
 *                        │
 *                    [Stop] ──► STOPPED ──► IDLE (after reset)
 *                        │
 *                    [Skip] ──► STOPPED ──► IDLE (after reset)
 *                        │
 *                  [0:00 reached] ──► FINISHED ──► IDLE (after transition)
 * </pre>
 *
 * <p>The {@code ProgressTimer.gif} should be visible ONLY in {@code RUNNING} state.
 * All other states hide the GIF component.
 */
public enum TimerState {

    /** Timer has not been started, or was reset. No session in progress. */
    IDLE,

    /** Timer is actively counting down. */
    RUNNING,

    /** Timer was paused by the user (Resume is available). */
    PAUSED,

    /** Timer was manually stopped or skipped by the user before reaching 0. */
    STOPPED,

    /** Timer reached 00:00 naturally without interruption. */
    FINISHED;

    /**
     * Returns true if the progress GIF should be visible in this state.
     * Only {@code RUNNING} shows the animation.
     */
    public boolean isGifVisible() {
        return this == RUNNING;
    }

    /**
     * Returns true if the timer is considered "active" (running or paused,
     * i.e., a session has been started and not yet ended).
     */
    public boolean isActive() {
        return this == RUNNING || this == PAUSED;
    }
}
