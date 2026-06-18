package com.focusmaxxing;

/**
 * A separate Launcher class that DOES NOT extend Application.
 * This is a standard workaround to launch JavaFX 11+ applications from the classpath
 * (without a module-info.java) to prevent "JavaFX runtime components are missing" errors.
 */
public class Main {
    public static void main(String[] args) {
        FocusMaxxingApp.launch(FocusMaxxingApp.class, args);
    }
}
