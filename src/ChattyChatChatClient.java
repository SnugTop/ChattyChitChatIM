
import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class ChattyChatChatClient {

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
