package com.focusmaxxing.util.component;

import com.focusmaxxing.util.multimedia.TimerState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Self-contained JavaFX component that displays the {@code ProgressTimer.gif}
 * animation inside a styled card frame, and automatically shows/hides itself
 * based on the current {@link TimerState}.
 *
 * <h2>Frame Design</h2>
 * <ul>
 *   <li>Soft green gradient background</li>
 *   <li>Rounded corners (radius 20)</li>
 *   <li>Layered drop shadow for depth</li>
 *   <li>Small label "FOKUS" below the GIF</li>
 *   <li>Thin accent border in the app's primary green</li>
 * </ul>
 *
 * <h2>Visibility Rules</h2>
 * <ul>
 *   <li>{@link TimerState#RUNNING} → visible</li>
 *   <li>Any other state → hidden and unmanaged (no layout space consumed)</li>
 * </ul>
 *
 * <h2>Integration</h2>
 * <pre>
 *   ProgressGifComponent gifComp = new ProgressGifComponent();
 *   timerAreaHBox.getChildren().add(gifComp.getRoot());
 *   gifComp.setState(TimerState.RUNNING);  // shows
 *   gifComp.setState(TimerState.STOPPED);  // hides + collapses layout
 * </pre>
 */
public final class ProgressGifComponent {

    private static final Logger LOG = Logger.getLogger(ProgressGifComponent.class.getName());

    private static final String GIF_PATH =
            "/com/focusmaxxing/images/character/ProgressTimer.gif";

    // ─── State ────────────────────────────────────────────────────────────────
    private final ObjectProperty<TimerState> timerState =
            new SimpleObjectProperty<>(TimerState.IDLE);

    // ─── Root node ────────────────────────────────────────────────────────────
    private final StackPane root;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public ProgressGifComponent() {
        root = buildComponent();

        timerState.addListener((obs, oldVal, newVal) -> {
            boolean visible = newVal != null && newVal.isGifVisible();
            root.setVisible(visible);
            root.setManaged(visible); // collapse layout space when hidden
        });

        // Start hidden, taking no layout space
        root.setVisible(false);
        root.setManaged(false);
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public StackPane getRoot()                             { return root; }
    public void setState(TimerState state)                 { timerState.set(state); }
    public ObjectProperty<TimerState> timerStateProperty() { return timerState; }
    public TimerState getState()                           { return timerState.get(); }

    // ─── Builder ──────────────────────────────────────────────────────────────

    private StackPane buildComponent() {
        // ── Load GIF ──
        URL gifUrl = getClass().getResource(GIF_PATH);
        if (gifUrl == null) LOG.warning("ProgressTimer.gif not found at: " + GIF_PATH);

        ImageView imageView = new ImageView();
        if (gifUrl != null) {
            imageView.setImage(new Image(gifUrl.toExternalForm()));
        }
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // ── "FOKUS" label below the GIF ──
        Text label = new Text("FOKUS");
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setFill(Color.web("#78a890"));

        // ── Inner content: GIF + label stacked vertically ──
        VBox content = new VBox(8, imageView, label);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(16, 20, 16, 20));

        // ── Rounded clip so the background doesn't bleed outside corners ──
        Rectangle clip = new Rectangle();
        clip.setArcWidth(24);
        clip.setArcHeight(24);

        // ── Card frame ──
        StackPane card = new StackPane(content);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            // Soft gradient: very light green → slightly deeper green
            "-fx-background-color: linear-gradient(to bottom, #f0f8f3, #e2f0e8);" +
            "-fx-background-radius: 20;" +
            // Border: thin primary-green accent line
            "-fx-border-color: #78a890;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 20;" +
            // Layered drop shadow: larger diffuse + smaller sharp offset
            "-fx-effect: dropshadow(three-pass-box, rgba(120,168,144,0.45), 18, 0, 0, 5);"
        );
        card.setPrefSize(170, 180);
        card.setMaxSize(170, 180);

        // Apply clip so the bg gradient is clipped to rounded rect
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        // ── Outer wrapper with padding so shadow isn't clipped ──
        StackPane outer = new StackPane(card);
        outer.setPadding(new Insets(8));
        outer.setAlignment(Pos.CENTER);

        return outer;
    }
}
