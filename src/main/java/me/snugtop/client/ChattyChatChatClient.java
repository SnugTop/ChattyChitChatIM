package me.snugtop.client;

import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * The {@code ChattyChatChatClient} class is responsible for establishing a
 * connection to the
 * ChattyChatChat server and facilitating two-way communication. It reads user
 * input from the
 * console and sends it to the server, and also listens for and displays
 * messages from the server.
 *
 */
public class ChattyChatChatClient {

    /**
     * The main entry point for the ChattyChatChat client.
     * Connects to the server using the specified host and port, and handles
     * sending and receiving messages.
     *
     * @param args Command-line arguments, expects two arguments: the server's
     *             hostname and port.
     */
    private static volatile boolean running = true;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChattyChatChatClient <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket server = new Socket(host, port);
            PrintWriter out = new PrintWriter(server.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()))) {

            System.out.println("Connected to server at " + host + ":" + port);

            startServerMessageListener(server);

            handleUserInput(stdIn, out);

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            System.out.println("Client is shutting down.");
        }
    }

    /**
     * Starts a new thread to listen for messages from the server.
     * Messages received from the server are printed to the console.
     *
     * @param server The socket connected to the server.
     */
    private static void startServerMessageListener(Socket server) {
        new Thread(() -> {
            try (BufferedReader threadIn = new BufferedReader(new InputStreamReader(server.getInputStream()))) {
                String response;
                while (running && (response = threadIn.readLine()) != null) {
                    System.out.println(response);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Connection to server lost: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Handles user input from the console. Continuously reads lines from the
     * console
     * and sends them to the server. The loop terminates when the user inputs
     * "/quit" or
     * when the end of the stream is reached (indicating a disconnection).
     *
     * @param stdIn The BufferedReader to read from the console.
     * @param out   The PrintWriter to send messages to the server.
     * @throws IOException If an I/O error occurs while reading from or writing to
     *                     the socket.
     */
    private static void handleUserInput(BufferedReader stdIn, PrintWriter out) throws IOException {
        String input;
        while (running) {
            input = stdIn.readLine();
            if (input == null || input.trim().equalsIgnoreCase("/quit")) {
                running = false;
                out.println(input != null ? input.trim() : "/quit");
                System.out.println("Disconnecting from the server...");
            } else if (!input.trim().isEmpty()) {
                out.println(input.trim());
            } else {
                System.out.println("Cannot send an empty message.");
            }
        }
    }
}
