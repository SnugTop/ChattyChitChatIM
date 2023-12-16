package me.snugtop.server;

import java.util.List;

/**
 * Handles the broadcasting and direct messaging functionalities in the
 * ChattyChatChat chat application.
 * This class is responsible for sending messages either to all clients or
 * specifically to clients
 * with a certain nickname.
 */
public class MessageHandler {

    private UserManager userManager;

    /**
     * Constructs a MessageHandler with a reference to the UserManager.
     *
     * @param userManager The UserManager that manages client information and
     *                    nicknames.
     */
    public MessageHandler(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Broadcasts a message to all clients except the sender.
     * If the message is null or empty, it does not get broadcasted.
     *
     * @param message The message to be broadcasted.
     * @param sender  The ClientHandler representing the client who sent the
     *                message.
     */
    public void broadcastMessage(String message, ClientHandler sender) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        String senderIdentity = sender.getDisplayName();
        String formattedMessage = senderIdentity + ": " + message;

        for (ClientHandler client : userManager.getClients().values()) {
            if (!client.equals(sender)) {
                client.sendMessage(formattedMessage);
            }
        }
    }

    /**
     * Sends a direct message to clients identified by a specific nickname.
     * If no clients have the specified nickname, a notification is sent to the
     * sender.
     *
     * @param senderHandler     The ClientHandler representing the client who is
     *                          sending the direct message.
     * @param recipientNickname The nickname of the intended recipient(s) of the
     *                          message.
     * @param message           The message to be sent.
     */
    public void sendDirectMessage(ClientHandler senderHandler, String recipientNickname, String message) {
        List<ClientHandler> recipients = userManager.getClientsByNickname(recipientNickname);
        String senderIdentity = senderHandler.getDisplayName();
        String formattedMessage = senderIdentity + " (DM): " + message;

        for (ClientHandler recipient : recipients) {
            recipient.sendMessage(formattedMessage);
        }

        if (recipients.isEmpty()) {
            senderHandler.sendMessage("The user " + recipientNickname + " is not online.");
        }
    }
}
