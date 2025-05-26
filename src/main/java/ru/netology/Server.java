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
