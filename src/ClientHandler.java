import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private int clientNumber;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private static final Map<String, ClientHandler> clients = new HashMap<>();

    private static final Object lock = new Object();

    public ClientHandler(Socket clientSocket, int clientNumber) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Enter your UUUsername:");
            username = in.readLine();
            synchronized (lock) {
                clients.put(username, this);
            }

            out.println("Hello, " + username + "! You are client #" + clientNumber + ".");
            broadcastMessage("Server: " + username + " has joined the chat.");

            String input;
            while ((input = in.readLine()) != null) {
                if (input.startsWith("/nick ")) {
                    handleNickCommand(input);
                } else if (input.startsWith("/dm ")) {
                    handleDMCommand(input);
                } else if (input.equals("/quit")) {
                    break;
                } else {
                    broadcastMessage(username + ": " + input);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client #" + clientNumber);
            System.out.println(e.getMessage());
        } finally {
            synchronized (lock) {
                if (username != null) {
                    clients.remove(username);
                }
            }
            if (username != null) {
                clients.remove(username);
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
                System.out.println(e.getMessage());
            }
            System.out.println("Connection with client #" + clientNumber + " closed");
        }
    }

    private void handleNickCommand(String input) {
        String[] parts = input.split(" ", 2);
        if (parts.length == 2) {
            String newNickname = parts[1];
            clients.remove(username);
            username = newNickname;
            clients.put(username, this);
            out.println("Your nickname is now set to " + username);
        } else {
            out.println("Invalid /nick command format");
        }
    }

    private void handleDMCommand(String input) {
        String[] parts = input.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String message = parts[2];
            sendDirectMessage(username, recipient, message);
        } else {
            out.println("Invalid /dm command format");
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients.values()) {
            if (!client.equals(this)) {
                client.out.println(message);
            }
        }
    }

    private void sendDirectMessage(String sender, String recipient, String message) {
        ClientHandler receiver = clients.get(recipient);
        if (receiver != null) {
            receiver.out.println(sender + " (DM): " + message);
        } else {
            out.println("The user " + recipient + " is not online.");
        }
    }
}
