package ru.netology;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/events.html", "/events.js");
        final var server = new Server();

        for (String path : validPaths) {
            server.addHandler(Methods.GET, path, (request, response) -> {
                final var filePath = Path.of(".", "public", request.url());
                final var mimeType = Files.probeContentType(filePath);
                final var length = Files.size(filePath);
                response.write((
                        "HTTP/1.1 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nConnection: close\r\n\r\n".formatted(mimeType, length)
                ).getBytes());
                Files.copy(filePath, response);
                response.flush();
            });
        }

        server.addHandler(Methods.GET, "/classic.html", (request, response) -> {
            final var filePath = Path.of(".", "public", request.url());
            final var mimeType = Files.probeContentType(filePath);
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            response.write((
                    "HTTP/1.1 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\nConnection: close\r\n\r\n".formatted(mimeType, content.length)
            ).getBytes());
            response.write(content);
            response.flush();
        });

        server.listen(9999);
    }
}