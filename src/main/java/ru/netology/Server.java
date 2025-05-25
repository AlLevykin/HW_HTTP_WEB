package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;
    private final ConcurrentHashMap<String, Handler> handlers = new ConcurrentHashMap<>();

    public void addHandler(Methods method, String path, Handler handler) {
        handlers.put("%s:%s".formatted(method, path), handler);
    }

    private void handle(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final Request request;
            try {
                request = RequestBuilder.build(in.readLine());
            } catch (Exception e) {
                out.write((
                        """
                                HTTP/1.1 500 Internal Server Error\r
                                Content-Length: 0\r
                                Connection: close\r
                                \r
                                """
                ).getBytes());
                out.flush();
                return;
            }

            Handler handler = handlers.get("%s:%s".formatted(request.method(), request.url()));

            if (handler == null) {
                out.write((
                        """
                                HTTP/1.1 404 Not Found\r
                                Content-Length: 0\r
                                Connection: close\r
                                \r
                                """
                ).getBytes());
                out.flush();
                return;
            }

            handler.handle(request, out);


//            final var parts = requestLine.split(" ");
//
//            if (parts.length != 3) {
//                // just close socket
//                return;
//            }
//
//            final var path = parts[1];
//            if (!validPaths.contains(path)) {
//                out.write((
//                        "HTTP/1.1 404 Not Found\r\n" +
//                                "Content-Length: 0\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.flush();
//                return;
//            }
//
//            final var filePath = Path.of(".", "public", path);
//            final var mimeType = Files.probeContentType(filePath);
//
//            // special case for classic
//            if (path.equals("/classic.html")) {
//                final var template = Files.readString(filePath);
//                final var content = template.replace(
//                        "{time}",
//                        LocalDateTime.now().toString()
//                ).getBytes();
//                out.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Type: " + mimeType + "\r\n" +
//                                "Content-Length: " + content.length + "\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                out.write(content);
//                out.flush();
//                return;
//            }
//
//            final var length = Files.size(filePath);
//            out.write((
//                    "HTTP/1.1 200 OK\r\n" +
//                            "Content-Type: " + mimeType + "\r\n" +
//                            "Content-Length: " + length + "\r\n" +
//                            "Connection: close\r\n" +
//                            "\r\n"
//            ).getBytes());
//            Files.copy(filePath, out);
//            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void listen(int port) throws IOException {
        try (ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
             ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(() -> handle(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
