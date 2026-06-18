package com.focusmaxxing.util.multimedia;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton audio utility that preloads, caches, and plays sound assets.
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li><b>Low latency</b> — {@link Media} objects are created once at startup;
 *       only {@link MediaPlayer} instantiation happens at play-time, which is far
 *       cheaper than parsing the audio file again.</li>
 *   <li><b>Overlap-safe</b> — every {@link #play(AudioClip)} call creates a
 *       fresh {@link MediaPlayer} from the pre-parsed {@link Media}, allowing
 *       sounds to overlap (e.g., click.wav can fire while a popup sound plays).</li>
 *   <li><b>Resource-safe</b> — players are set to {@code DISPOSE} after playback
 *       to release native resources automatically.</li>
 *   <li><b>Singleton</b> — one instance holds all cached {@link Media} objects;
 *       no re-parsing across controller re-instantiation.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 *   // Preload once at app startup or first controller init:
 *   AudioPlayer.getInstance().preloadAll();
 *
 *   // Play anywhere, zero extra setup:
 *   AudioPlayer.getInstance().play(AudioClip.CLICK);
 * </pre>
 */
public final class AudioPlayer {

    private static final Logger LOG = Logger.getLogger(AudioPlayer.class.getName());

    // ─── Resource paths (relative to the classpath root) ─────────────────────
    /**
     * Enum of every audio asset in the project.
     * Adding a new sound = adding one constant here and calling preloadAll() handles the rest.
     */
    public enum AudioClip {
        CLICK       ("/com/focusmaxxing/sound/ui/click.wav"),
        POPUP       ("/com/focusmaxxing/sound/ui/PopUp.mp3"),
        STOP_SKIP   ("/com/focusmaxxing/sound/timer/stop&skip.wav"),
        POMODORO_DONE("/com/focusmaxxing/sound/timer/pomodoroDone.mp3");

        private final String resourcePath;

        AudioClip(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        public String getResourcePath() {
            return resourcePath;
        }
    }

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static volatile AudioPlayer instance;

    public static AudioPlayer getInstance() {
        if (instance == null) {
            synchronized (AudioPlayer.class) {
                if (instance == null) {
                    instance = new AudioPlayer();
                }
            }
        }
        return instance;
    }

    // ─── Internal state ───────────────────────────────────────────────────────
    /** Parsed Media objects — expensive to create, cheap to reuse. */
    private final Map<AudioClip, Media> mediaCache = new EnumMap<>(AudioClip.class);

    private AudioPlayer() {
        // Private — use getInstance()
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Preloads all {@link AudioClip} assets into the media cache.
     * Call this once during application or controller initialization so that
     * the first playback has minimal latency.
     *
     * <p>Safe to call multiple times — already-cached entries are skipped.
     */
    public void preloadAll() {
        for (AudioClip clip : AudioClip.values()) {
            preload(clip);
        }
    }

    /**
     * Preloads a single clip. Idempotent.
     *
     * @param clip the clip to preload
     */
    public void preload(AudioClip clip) {
        if (mediaCache.containsKey(clip)) return;

        URL url = getClass().getResource(clip.getResourcePath());
        if (url == null) {
            LOG.warning("Audio resource not found: " + clip.getResourcePath());
            return;
        }
        try {
            Media media = new Media(url.toExternalForm());
            mediaCache.put(clip, media);
            LOG.fine("Preloaded audio: " + clip.name());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to preload audio: " + clip.name(), e);
        }
    }

    /**
     * Plays the given audio clip immediately.
     *
     * <ul>
     *   <li>If the clip is not preloaded yet, it will be loaded on demand
     *       (with a small one-time latency).</li>
     *   <li>Multiple calls can overlap freely — each call owns its own
     *       {@link MediaPlayer} instance.</li>
     *   <li>The player is disposed automatically when playback finishes.</li>
     * </ul>
     *
     * @param clip the clip to play
     * @return the active {@link MediaPlayer} so the caller can stop it early if needed
     */
    public MediaPlayer play(AudioClip clip) {
        // Ensure preloaded (lazy fallback)
        if (!mediaCache.containsKey(clip)) {
            preload(clip);
        }

        Media media = mediaCache.get(clip);
        if (media == null) {
            LOG.warning("Cannot play: media not available for " + clip.name());
            return null;
        }

        MediaPlayer player = new MediaPlayer(media);

        // Auto-dispose native resources when done — critical for preventing leaks
        player.setOnEndOfMedia(() -> {
            player.stop();
            player.dispose();
        });
        player.setOnError(() -> {
            LOG.warning("MediaPlayer error for " + clip.name() + ": " + player.getError());
            player.dispose();
        });

        player.play();
        return player;
    }

    /**
     * Clears the media cache and releases all cached {@link Media} objects.
     * Useful during application shutdown or when all audio is no longer needed.
     */
    public void shutdown() {
        mediaCache.clear();
    }
}
