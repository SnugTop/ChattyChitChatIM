
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChattyChatChatServer {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java ChattyChatChatServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(port);
            System.out.println("Server is connected to port " + port);
            int clientNumber = 1;

            UserManager userManager = new UserManager();
            MessageHandler messageHandler = new MessageHandler(userManager);

            while (true) {
                try {
                    System.out.println("Waiting for client connection...");
                    Socket client = listener.accept();
                    System.out.println("Connected to client " + clientNumber + ": " + client);

                    ClientHandler handler = new ClientHandler(client, clientNumber, userManager, messageHandler);

                    Thread t = new Thread(handler);
                    t.start();
                    clientNumber++;
                } catch (IOException e) {
                    System.out.println("Error connecting to client: " + clientNumber);
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("Error listening for client connections");
            System.out.println(e.getMessage());
        } finally {
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
