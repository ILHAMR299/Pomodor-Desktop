package com.focusmaxxing.model;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH;

    @Override
    public String toString() {
        return switch (this) {
            case LOW -> "Rendah";
            case MEDIUM -> "Sedang";
            case HIGH -> "Tinggi";
        };
    }
}
