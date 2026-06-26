package com.focusmaxxing.util.multimedia;

/**
 * Enumerates every application event that triggers a multimedia reaction
 * (audio, popup, or GIF animation).
 *
 * <p>Each constant documents:
 * <ul>
 *   <li>which audio file to play (relative to the resources root)</li>
 *   <li>which visual reaction should occur</li>
 * </ul>
 *
 * <p>Adding a new event in the future only requires:
 * <ol>
 *   <li>Adding a constant here.</li>
 *   <li>Handling it in {@link MediaEventHandler}.</li>
 * </ol>
 */
public enum MultimediaEvent {

    /**
     * User pressed Start for the first time (or Resume after Pause).
     * Audio: click.wav — immediate tactile feedback.
     * Visual: ProgressTimer.gif becomes visible via state → RUNNING.
     */
    TIMER_START,

    /**
     * User pressed Stop while timer is running.
     * Audio: click.wav (instant) + stop&skip.wav (with popup).
     * Visual: Popup showing stop&skip.gif.
     */
    TIMER_STOP,

    /**
     * User pressed Skip while timer is running.
     * Audio: click.wav (instant) + stop&skip.wav (with popup).
     * Visual: Popup showing stop&skip.gif.
     */
    TIMER_SKIP,

    /**
     * Timer naturally reached 00:00 without manual interruption.
     * Audio: pomodoroDone.mp3 (with popup).
     * Visual: Popup showing PomodoroDone.gif.
     */
    TIMER_COMPLETE,

    /**
     * Timer berhenti otomatis karena tidak ada aktivitas mouse selama batas waktu.
     * Audio: tidak produktif.mp3 — popup khusus, terpisah dari TIMER_STOP.
     */
    TIMER_INACTIVITY_STOP,

    /**
     * User pressed the Logout button.
     * Audio: PopUp.mp3.
     * Visual: Confirmation dialog "Apakah Anda yakin ingin logout?".
     */
    LOGOUT_CONFIRM
}
