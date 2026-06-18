package com.focusmaxxing.util.multimedia;

import com.focusmaxxing.util.component.PopupDialog;
import com.focusmaxxing.util.component.ProgressGifComponent;

import java.util.logging.Logger;

/**
 * Subscribes to every {@link MultimediaEvent} on the {@link MultimediaEventBus}
 * and performs the corresponding audio / popup / animation action.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Translates semantic events ({@code TIMER_STOP}) into concrete reactions
 *       (play click.wav, show popup with stop&amp;skip.gif + stop&amp;skip.wav).</li>
 *   <li>Keeps all multimedia logic in one place — controllers stay clean.</li>
 *   <li>Manages the {@link ProgressGifComponent} state transitions.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Instantiate once in {@code MainDashboardController.initialize()}.</li>
 *   <li>Optionally pass a {@link ProgressGifComponent} reference so the handler
 *       can toggle its visibility.</li>
 *   <li>Optionally register a {@code logoutConfirmedCallback} to receive the
 *       user's response to the logout confirmation dialog.</li>
 * </ol>
 *
 * <h2>Resource Paths</h2>
 * All paths are classpath-relative constants.
 * If an asset is moved, update the constant here — nothing else changes.
 */
public final class MediaEventHandler {

    private static final Logger LOG = Logger.getLogger(MediaEventHandler.class.getName());

    // ─── Asset paths ──────────────────────────────────────────────────────────
    /** GIF shown during an active timer session. */
    private static final String GIF_PROGRESS  =
            "/com/focusmaxxing/images/character/ProgressTimer.gif";

    /** GIF shown when stop/skip popup appears. */
    private static final String GIF_STOP_SKIP =
            "/com/focusmaxxing/images/pop up/stop&skip.gif";

    /** GIF shown when a Pomodoro session completes naturally. */
    private static final String GIF_DONE      =
            "/com/focusmaxxing/images/character/PomodoroDone.gif";

    // ─── Dependencies ─────────────────────────────────────────────────────────
    private final AudioPlayer audioPlayer;
    private final MultimediaEventBus eventBus;

    /** Nullable — only relevant in PomodoroView context. */
    private ProgressGifComponent progressGif;

    /**
     * Callback invoked after the logout confirmation popup closes.
     * The boolean argument is {@code true} if the user confirmed logout.
     */
    private java.util.function.Consumer<Boolean> logoutConfirmedCallback;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates the handler and immediately registers all subscriptions on the
     * shared {@link MultimediaEventBus}.
     */
    public MediaEventHandler() {
        this.audioPlayer = AudioPlayer.getInstance();
        this.eventBus    = MultimediaEventBus.getInstance();

        // Preload all audio assets upfront for minimum latency
        audioPlayer.preloadAll();

        registerSubscriptions();
    }

    // ─── Configuration setters ────────────────────────────────────────────────

    /**
     * Provides the handler with a reference to the {@link ProgressGifComponent}
     * so it can show/hide the animation based on timer state.
     *
     * @param component the progress GIF component from PomodoroView
     */
    public void setProgressGifComponent(ProgressGifComponent component) {
        this.progressGif = component;
    }

    /**
     * Registers a callback to be invoked when the user responds to the logout
     * confirmation dialog.
     *
     * @param callback receives {@code true} if logout was confirmed, {@code false} otherwise
     */
    public void setLogoutConfirmedCallback(java.util.function.Consumer<Boolean> callback) {
        this.logoutConfirmedCallback = callback;
    }

    // ─── Private: event subscriptions ─────────────────────────────────────────

    private void registerSubscriptions() {
        eventBus.subscribe(MultimediaEvent.TIMER_START,    e -> onTimerStart());
        eventBus.subscribe(MultimediaEvent.TIMER_STOP,     e -> onTimerStop());
        eventBus.subscribe(MultimediaEvent.TIMER_SKIP,     e -> onTimerSkip());
        eventBus.subscribe(MultimediaEvent.TIMER_COMPLETE, e -> onTimerComplete());
        eventBus.subscribe(MultimediaEvent.LOGOUT_CONFIRM, e -> onLogoutConfirm());
    }

    // ─── Event handlers ───────────────────────────────────────────────────────

    /**
     * TIMER_START:
     * 1. Play click.wav immediately (tactile feedback, ~0 latency).
     * 2. Transition progress GIF to RUNNING state (makes it visible).
     */
    private void onTimerStart() {
        LOG.fine("Handling TIMER_START");
        audioPlayer.play(AudioPlayer.AudioClip.CLICK);
        updateGifState(TimerState.RUNNING);
    }

    /**
     * TIMER_STOP:
     * 1. Play click.wav immediately.
     * 2. Transition progress GIF to STOPPED state (hides it).
     * 3. Show stop popup with GIF + audio (synchronised via PopupDialog).
     */
    private void onTimerStop() {
        LOG.fine("Handling TIMER_STOP");
        audioPlayer.play(AudioPlayer.AudioClip.CLICK);
        updateGifState(TimerState.STOPPED);
        showStopSkipPopup();
    }

    /**
     * TIMER_SKIP:
     * 1. Play click.wav immediately.
     * 2. Transition progress GIF to STOPPED state (hides it).
     * 3. Show stop/skip popup with GIF + audio.
     */
    private void onTimerSkip() {
        LOG.fine("Handling TIMER_SKIP");
        audioPlayer.play(AudioPlayer.AudioClip.CLICK);
        updateGifState(TimerState.STOPPED);
        showStopSkipPopup();
    }

    /**
     * TIMER_COMPLETE:
     * 1. Transition progress GIF to FINISHED state (hides it).
     * 2. Show completion popup with PomodoroDone.gif + pomodoroDone.mp3.
     *
     * <p>No click.wav here because the timer completed naturally, not via a button press.
     */
    private void onTimerComplete() {
        LOG.fine("Handling TIMER_COMPLETE");
        updateGifState(TimerState.FINISHED);
        showPomodoroDonePopup();
    }

    /**
     * LOGOUT_CONFIRM:
     * 1. Show confirmation dialog with PopUp.mp3.
     * 2. Invoke {@link #logoutConfirmedCallback} with the user's choice.
     */
    private void onLogoutConfirm() {
        LOG.fine("Handling LOGOUT_CONFIRM");
        boolean confirmed = new PopupDialog.Builder()
                .title("Konfirmasi Logout")
                .message("Apakah Anda yakin ingin logout?")
                .audioClip(AudioPlayer.AudioClip.POPUP)
                .confirmText("Ya, Logout")
                .cancelText("Batal")
                .build()
                .showAndWait();

        if (logoutConfirmedCallback != null) {
            logoutConfirmedCallback.accept(confirmed);
        }
    }

    // ─── Private: popup helpers ───────────────────────────────────────────────

    /**
     * Shows the stop/skip popup modal.
     * Audio ({@code stop&skip.wav}) is played synchronously when the popup appears.
     */
    private void showStopSkipPopup() {
        new PopupDialog.Builder()
                .title("Session Dihentikan")
                .message("Timer telah dihentikan.\nLanjutkan kapan saja!")
                .gifResourcePath(GIF_STOP_SKIP)
                .audioClip(AudioPlayer.AudioClip.STOP_SKIP)
                .confirmText("OK")
                .gifSize(180, 150)
                .build()
                .showAndWait();
    }

    /**
     * Shows the Pomodoro completion popup modal.
     * Audio ({@code pomodoroDone.mp3}) is played synchronously when the popup appears.
     */
    private void showPomodoroDonePopup() {
        new PopupDialog.Builder()
                .title("Pomodoro Selesai! 🎉")
                .message("Hebat! Kamu berhasil menyelesaikan satu sesi fokus.")
                .gifResourcePath(GIF_DONE)
                .audioClip(AudioPlayer.AudioClip.POMODORO_DONE)
                .confirmText("Lanjutkan")
                .gifSize(180, 180)
                .build()
                .showAndWait();
    }

    // ─── Private: state helper ────────────────────────────────────────────────

    private void updateGifState(TimerState state) {
        if (progressGif != null) {
            progressGif.setState(state);
        }
    }
}
