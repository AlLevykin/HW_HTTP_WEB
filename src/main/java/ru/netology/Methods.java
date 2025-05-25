package ru.netology;

public enum Methods {
    GET("GET"),
    POST("POST");

    private final String name;

    Methods(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
