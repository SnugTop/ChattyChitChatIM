public class CommandParser {

    private UserManager userManager;
    private MessageHandler messageHandler;

    public CommandParser(UserManager userManager, MessageHandler messageHandler) {
        this.userManager = userManager;
        this.messageHandler = messageHandler;
    }

    public void parseCommand(String input, ClientHandler clientHandler) {
        if (input.startsWith("/nick ")) {
            handleNickCommand(input, clientHandler);
        } else if (input.startsWith("/dm ")) {
            handleDMCommand(input, clientHandler);
        } else if (!input.equals("/quit")) {
            messageHandler.broadcastMessage(input, clientHandler);
        }
    }

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
