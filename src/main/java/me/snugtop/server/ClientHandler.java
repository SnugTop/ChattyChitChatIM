package me.snugtop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication for an individual client connected to the
 * ChattyChatChat server.
 * This class is responsible for processing incoming messages from the client,
 * executing
 * chat commands, and managing the client's state, such as its nickname.
 * It runs in a separate thread for each client to facilitate concurrent
 * handling of multiple clients.
 */
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private int clientNumber;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    private UserManager userManager;
    private MessageHandler messageHandler;
    private CommandParser commandParser;

    /**
     * Constructs a new ClientHandler for handling communication with a single
     * client.
     *
     * @param clientSocket   The socket representing the client's connection.
     * @param clientNumber   The unique number assigned to the client.
     * @param userManager    The UserManager instance for managing client
     *                       information.
     * @param messageHandler The MessageHandler instance for handling message
     *                       broadcasting.
     */
    public ClientHandler(Socket clientSocket, int clientNumber, UserManager userManager,
                         MessageHandler messageHandler) {
        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.userManager = userManager;
        this.messageHandler = messageHandler;
        this.commandParser = new CommandParser(userManager, messageHandler);
    }

    /**
     * The main execution method for the ClientHandler thread.
     * Manages reading input from the client, processing commands, and handling
     * client disconnections.
     */
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

    /**
     * Returns the display name for the client. If the client has set a nickname,
     * the nickname is returned. Otherwise, the client number is returned.
     *
     * This was I decided to not use the nickname as the key for the client in the
     * UserManager, since the nickname can be changed. As well as not force the
     * client
     * to have a nickname.
     *
     * @return
     */
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

    /**
     * Performs cleanup tasks when a client disconnects. This includes broadcasting
     * a disconnection message,
     * removing the client from the user manager, and closing streams and the socket
     * connection.
     */
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

    /**
     * Handles the disconnection of a client due to an error. This includes logging
     * the error message and
     * broadcasting a disconnection message to other clients.
     *
     * @param errorMessage The error message associated with the client
     *                     disconnection.
     */
    private void handleClientDisconnection(String errorMessage) {
        System.out.println(errorMessage);
        if (getDisplayName() != null && !getDisplayName().isEmpty()) {
            messageHandler.broadcastMessage(getDisplayName() + " has been disconnected.", this);
        }
    }

}
