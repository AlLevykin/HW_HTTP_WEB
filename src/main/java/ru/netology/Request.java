package ru.netology;

public record Request(
        Methods method,
        String url,
        String protocol
) { }