package com.focusmaxxing.util.component;

import com.focusmaxxing.util.multimedia.AudioPlayer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Reusable modal popup dialog that displays a title, message, optional GIF,
 * and optional audio. Audio started when the popup opens is stopped
 * immediately when the user dismisses the dialog.
 *
 * <h2>Usage</h2>
 * <pre>
 *   // Info dialog (single OK button):
 *   new PopupDialog.Builder()
 *       .title("Selesai!")
 *       .gifResourcePath("/com/focusmaxxing/images/character/PomodoroDone.gif")
 *       .audioClip(AudioPlayer.AudioClip.POMODORO_DONE)
 *       .build().showAndWait();
 *
 *   // Confirmation dialog:
 *   boolean confirmed = new PopupDialog.Builder()
 *       .title("Konfirmasi Logout")
 *       .message("Apakah Anda yakin ingin logout?")
 *       .audioClip(AudioPlayer.AudioClip.POPUP)
 *       .confirmText("Ya, Logout")
 *       .cancelText("Batal")
 *       .build().showAndWait();
 * </pre>
 */
public final class PopupDialog {

    private static final Logger LOG = Logger.getLogger(PopupDialog.class.getName());

    // ─── Palette (matches the app's CSS theme) ────────────────────────────────
    private static final String COLOR_PRIMARY    = "#78a890";
    private static final String COLOR_BG         = "#f4f9f4";
    private static final String COLOR_CARD       = "white";
    private static final String COLOR_TEXT       = "#4a5c50";
    private static final String COLOR_TEXT_LIGHT = "#888888";

    // ─── Fields ───────────────────────────────────────────────────────────────
    private final String title;
    private final String message;
    private final String gifResourcePath;
    private final AudioPlayer.AudioClip audioClip;
    private final String confirmText;
    private final String cancelText;
    private final double gifWidth;
    private final double gifHeight;

    private PopupDialog(Builder b) {
        this.title           = b.title;
        this.message         = b.message;
        this.gifResourcePath = b.gifResourcePath;
        this.audioClip       = b.audioClip;
        this.confirmText     = b.confirmText;
        this.cancelText      = b.cancelText;
        this.gifWidth        = b.gifWidth;
        this.gifHeight       = b.gifHeight;
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Shows the dialog modally and blocks until dismissed.
     * Any audio started when the popup appears is stopped when the popup closes.
     *
     * @return {@code true} if the user pressed the confirm button, {@code false} otherwise.
     */
    public boolean showAndWait() {
        final boolean[] confirmed = {false};
        // Holds the active MediaPlayer so we can stop it on dismiss
        final MediaPlayer[] activePlayer = {null};

        // ── Root card ──
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle(
                "-fx-background-color: " + COLOR_CARD + ";" +
                "-fx-background-radius: 20;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 24, 0, 0, 8);"
        );
        root.setMaxWidth(400);

        // ── GIF (optional) ──
        if (gifResourcePath != null && !gifResourcePath.isBlank()) {
            URL gifUrl = getClass().getResource(gifResourcePath);
            if (gifUrl != null) {
                Image gif = new Image(gifUrl.toExternalForm());
                ImageView gifView = new ImageView(gif);
                gifView.setFitWidth(gifWidth);
                gifView.setFitHeight(gifHeight);
                gifView.setPreserveRatio(true);
                gifView.setSmooth(true);

                // Styled frame around the GIF
                StackPane gifFrame = new StackPane(gifView);
                gifFrame.setStyle(
                        "-fx-background-color: " + COLOR_BG + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 12;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(120,168,144,0.3), 14, 0, 0, 4);"
                );
                root.getChildren().add(gifFrame);
            } else {
                LOG.warning("GIF resource not found: " + gifResourcePath);
            }
        }

        // ── Title ──
        if (title != null && !title.isBlank()) {
            Text titleText = new Text(title);
            titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            titleText.setFill(Color.web(COLOR_TEXT));
            titleText.setTextAlignment(TextAlignment.CENTER);
            root.getChildren().add(titleText);
        }

        // ── Message ──
        if (message != null && !message.isBlank()) {
            Text msgText = new Text(message);
            msgText.setFont(Font.font("Segoe UI", 14));
            msgText.setFill(Color.web(COLOR_TEXT_LIGHT));
            msgText.setTextAlignment(TextAlignment.CENTER);
            msgText.setWrappingWidth(320);
            root.getChildren().add(msgText);
        }

        // ── Buttons ──
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmBtn = styledButton(confirmText, COLOR_PRIMARY, "white");
        buttonBox.getChildren().add(confirmBtn);

        if (cancelText != null) {
            Button cancelBtn = styledButton(cancelText, "#e0e0e0", COLOR_TEXT);
            buttonBox.getChildren().add(cancelBtn);
            cancelBtn.setOnAction(e -> {
                stopActivePlayer(activePlayer);
                ((Stage) cancelBtn.getScene().getWindow()).close();
            });
        }

        root.getChildren().add(buttonBox);

        // ── Scene & Stage ──
        StackPane wrapper = new StackPane(root);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(wrapper);
        scene.setFill(Color.TRANSPARENT);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(title != null ? title : "");

        // Confirm: stop audio + close
        confirmBtn.setOnAction(e -> {
            confirmed[0] = true;
            stopActivePlayer(activePlayer);
            stage.close();
        });

        // Also stop audio if user closes via OS (alt-F4, etc.)
        stage.setOnHidden(e -> stopActivePlayer(activePlayer));

        // Play audio synchronously when popup appears — capture the player
        if (audioClip != null) {
            stage.setOnShown(e -> {
                MediaPlayer player = AudioPlayer.getInstance().play(audioClip);
                activePlayer[0] = player;
            });
        }

        stage.showAndWait();
        return confirmed[0];
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Stops and disposes the active player if one is running. */
    private void stopActivePlayer(MediaPlayer[] activePlayer) {
        MediaPlayer player = activePlayer[0];
        if (player != null) {
            try {
                player.stop();
                player.dispose();
            } catch (Exception ex) {
                LOG.fine("Error stopping player: " + ex.getMessage());
            }
            activePlayer[0] = null;
        }
    }

    private Button styledButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 20;" +
                "-fx-padding: 9 26 9 26;" +
                "-fx-cursor: hand;"
        );
        // Subtle hover feedback
        final String hoverBg = bgColor.equals(COLOR_PRIMARY) ? "#65927b" : "#cccccc";
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(bgColor, hoverBg)));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(hoverBg, bgColor)));
        return btn;
    }

    // ─── Builder ──────────────────────────────────────────────────────────────

    public static final class Builder {
        private String title           = "";
        private String message         = "";
        private String gifResourcePath = null;
        private AudioPlayer.AudioClip audioClip = null;
        private String confirmText     = "OK";
        private String cancelText      = null;   // null = no cancel button
        private double gifWidth        = 180;
        private double gifHeight       = 180;

        public Builder title(String title)               { this.title = title; return this; }
        public Builder message(String message)           { this.message = message; return this; }
        public Builder gifResourcePath(String path)      { this.gifResourcePath = path; return this; }
        public Builder audioClip(AudioPlayer.AudioClip c){ this.audioClip = c; return this; }
        public Builder confirmText(String text)          { this.confirmText = text; return this; }
        public Builder cancelText(String text)           { this.cancelText = text; return this; }
        public Builder gifSize(double w, double h)       { this.gifWidth = w; this.gifHeight = h; return this; }

        public PopupDialog build() { return new PopupDialog(this); }
    }
}
