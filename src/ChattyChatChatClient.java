import java.net.Socket;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class ChattyChatChatClient {

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

            System.out.println("Connected to server: " + server);

            new Thread(() -> {
                try (BufferedReader threadIn = new BufferedReader(new InputStreamReader(server.getInputStream()))) {
                    String response;
                    while ((response = threadIn.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server lost.");
                }
            }).start();

            String input;
            while ((input = stdIn.readLine()) != null) {
                out.println(input);
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}
