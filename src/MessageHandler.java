
import java.util.List;

public class MessageHandler {

    private UserManager userManager;

    public MessageHandler(UserManager userManager) {
        this.userManager = userManager;
    }

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
