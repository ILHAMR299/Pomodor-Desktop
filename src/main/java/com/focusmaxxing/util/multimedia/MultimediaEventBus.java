package com.focusmaxxing.util.multimedia;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Lightweight, synchronous event bus for multimedia events.
 *
 * <h2>Why an Event Bus?</h2>
 * <p>Controllers (PomodoroController, MainDashboardController) should not know
 * anything about audio files, popup windows, or GIF animations. By publishing
 * a semantic event ({@link MultimediaEvent#TIMER_STOP}) instead of directly
 * calling {@code AudioPlayer.play(…)} or opening a popup, we achieve:
 * <ul>
 *   <li><b>Decoupling</b> — controllers stay clean; multimedia logic lives in
 *       {@link MediaEventHandler}.</li>
 *   <li><b>Open/Closed</b> — adding a new listener for a new feature (e.g., a
 *       haptic feedback service) requires zero changes to existing controllers.</li>
 *   <li><b>Testability</b> — events can be fired in unit tests without a real
 *       JavaFX stage.</li>
 * </ul>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Synchronous dispatch on the caller's thread. Because all UI events
 *       originate on the JavaFX Application Thread, subscribers also run on
 *       that thread, which is required for any UI manipulation.</li>
 *   <li>Singleton — one bus, all components share the same channel.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 *   // Subscribe (typically done in MediaEventHandler):
 *   MultimediaEventBus.getInstance().subscribe(MultimediaEvent.TIMER_STOP, e -> handleStop());
 *
 *   // Publish (from a controller):
 *   MultimediaEventBus.getInstance().publish(MultimediaEvent.TIMER_STOP);
 * </pre>
 */
public final class MultimediaEventBus {

    private static final Logger LOG = Logger.getLogger(MultimediaEventBus.class.getName());

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static volatile MultimediaEventBus instance;

    public static MultimediaEventBus getInstance() {
        if (instance == null) {
            synchronized (MultimediaEventBus.class) {
                if (instance == null) {
                    instance = new MultimediaEventBus();
                }
            }
        }
        return instance;
    }

    // ─── Internal state ───────────────────────────────────────────────────────
    private final Map<MultimediaEvent, List<Consumer<MultimediaEvent>>> listeners =
            new EnumMap<>(MultimediaEvent.class);

    private MultimediaEventBus() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Registers a listener for the given event type.
     *
     * @param event    the event to listen for
     * @param listener callback invoked when the event is published
     */
    public void subscribe(MultimediaEvent event, Consumer<MultimediaEvent> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Removes all listeners for a given event type.
     * Useful when a controller is torn down or replaced.
     *
     * @param event the event whose listeners should be cleared
     */
    public void clearListeners(MultimediaEvent event) {
        List<Consumer<MultimediaEvent>> list = listeners.get(event);
        if (list != null) list.clear();
    }

    /**
     * Removes all listeners for every event type.
     * Typically called at application shutdown.
     */
    public void clearAll() {
        listeners.clear();
    }

    /**
     * Dispatches the given event to all registered listeners synchronously.
     *
     * <p>Exceptions thrown by individual listeners are caught and logged so
     * that one bad listener cannot prevent other listeners from receiving the
     * event.
     *
     * @param event the event to publish
     */
    public void publish(MultimediaEvent event) {
        List<Consumer<MultimediaEvent>> list = listeners.get(event);
        if (list == null || list.isEmpty()) {
            LOG.fine("No listeners for event: " + event);
            return;
        }
        LOG.fine("Publishing event: " + event + " to " + list.size() + " listener(s)");
        // Iterate over a snapshot to allow listeners to unsubscribe safely
        new ArrayList<>(list).forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception ex) {
                LOG.warning("Listener threw exception for event " + event + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
}
