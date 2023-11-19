
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private int clientNumber;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    private UserManager userManager;
    private MessageHandler messageHandler;
    private CommandParser commandParser;

    public ClientHandler(Socket clientSocket, int clientNumber, UserManager userManager,
            MessageHandler messageHandler) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.userManager = userManager;
        this.messageHandler = messageHandler;
        this.commandParser = new CommandParser(userManager, messageHandler);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println("Hello! You are client #" + clientNumber + ".");
            userManager.addClient(String.valueOf(clientNumber), this);
            messageHandler.broadcastMessage("has joined the chat.", this);

            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("/quit")) {
                    break;
                }
                commandParser.parseCommand(input, this);
            }
        } catch (IOException e) {
            handleClientDisconnection("Error with client #" + clientNumber + ": " + e.getMessage());
        } finally {
            cleanupClient();
        }
    }

    public void sendMessage(String message) {
        try {
            out.println(message);
        } catch (Exception e) {
            System.out.println("Error sending message to client #" + clientNumber + ": " + e.getMessage());
        }
    }

    public String getDisplayName() {
        // Use nickname if set, otherwise use "Client #<clientNumber>"
        return (nickname != null && !nickname.isEmpty()) ? nickname : "Client #" + clientNumber;
    }

    public void setNickname(String newNickname) {
        if (newNickname == null || newNickname.trim().isEmpty()) {
            return;
        }
        this.nickname = newNickname.trim();
    }

    public String getNickname() {
        return nickname;
    }

    private void closeStreamsAndConnection() {
        if (out != null) {
            out.close();
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing input stream for client #" + clientNumber + ": " + e.getMessage());
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing socket for client #" + clientNumber + ": " + e.getMessage());
        }
    }

    private void cleanupClient() {
        String displayName = getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            messageHandler.broadcastMessage("has left the chat.", this);
        }

        userManager.removeClient(displayName);
        closeStreamsAndConnection();
        System.out.println(displayName + " has disconnected.");
        System.out.println("Waiting for client connection...");
    }

    private void handleClientDisconnection(String errorMessage) {
        System.out.println(errorMessage);
        if (getDisplayName() != null && !getDisplayName().isEmpty()) {
            messageHandler.broadcastMessage(getDisplayName() + " has been disconnected.", this);
        }
    }

}
