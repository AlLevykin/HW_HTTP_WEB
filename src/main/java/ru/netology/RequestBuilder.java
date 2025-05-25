package ru.netology;

public class RequestBuilder {
    public static Request build(String requestLine) {
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid request line: %s".formatted(requestLine));
        }

        return new Request(Methods.valueOf(parts[0]), parts[1], parts[2]);
    }
}
