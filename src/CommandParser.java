/**
 * The {@code CommandParser} class is responsible for parsing and handling
 * commands
 * received from clients in the ChattyChatChat chat application.
 * It interprets commands like changing nicknames or sending direct messages and
 * delegates
 * the handling to appropriate methods or classes.
 */
public class CommandParser {

    private UserManager userManager;
    private MessageHandler messageHandler;

    /**
     * Constructs a CommandParser with the specified UserManager and MessageHandler.
     *
     * @param userManager    The UserManager to manage client information and
     *                       nicknames.
     * @param messageHandler The MessageHandler to handle sending messages to
     *                       clients.
     */
    public CommandParser(UserManager userManager, MessageHandler messageHandler) {
        this.userManager = userManager;
        this.messageHandler = messageHandler;
    }

    /**
     * Parses and processes a command from a client.
     * This method identifies the type of command and delegates to the appropriate
     * handling method.
     *
     * @param input         The command input received from the client.
     * @param clientHandler The ClientHandler associated with the client sending the
     *                      command.
     */
    public void parseCommand(String input, ClientHandler clientHandler) {
        if (input.startsWith("/nick ")) {
            handleNickCommand(input, clientHandler);
        } else if (input.startsWith("/dm ")) {
            handleDMCommand(input, clientHandler);
        } else if (!input.equals("/quit")) {
            messageHandler.broadcastMessage(input, clientHandler);
        }
    }

    /**
     * Handles the '/nick' command for changing a client's nickname.
     *
     * @param input         The command input containing the nickname to be set.
     * @param clientHandler The ClientHandler associated with the client sending the
     *                      command.
     */
    public void handleNickCommand(String input, ClientHandler clientHandler) {
        String[] parts = input.split(" ", 2);
        if (parts.length == 2) {
            String newNickname = parts[1].trim();
            if (isValidNickname(newNickname)) {
                clientHandler.setNickname(newNickname);
                userManager.updateNickname(clientHandler.getDisplayName(), newNickname, clientHandler);
                clientHandler.sendMessage("Your nickname is now set to " + newNickname);
            } else {
                clientHandler.sendMessage("Invalid nickname format.");
            }
        } else {
            clientHandler.sendMessage("Invalid /nick command format.");
        }
    }

    private boolean isValidNickname(String nickname) {
        return nickname != null && !nickname.trim().isEmpty() && !nickname.contains(" ") && !nickname.startsWith("/");
    }

    /**
     * Handles the '/dm' command for sending a direct message to a specific client.
     *
     * @param input         The command input containing the recipient's name and
     *                      the message.
     * @param clientHandler The ClientHandler associated with the client sending the
     *                      direct message.
     */
    private void handleDMCommand(String input, ClientHandler clientHandler) {
        String[] parts = input.split(" ", 3);
        if (parts.length == 3) {
            String recipient = parts[1];
            String message = parts[2];
            messageHandler.sendDirectMessage(clientHandler, recipient, message);
        } else {
            clientHandler.sendMessage("Invalid /dm command format");
        }
    }
}
