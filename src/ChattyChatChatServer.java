
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The {@code ChattyChatChatServer} class represents a server in the
 * ChattyChatChat chat application.
 * It listens for incoming client connections on a specified port and creates a
 * new {@code ClientHandler}
 * for each connection to manage client communications. This server supports
 * multiple concurrent client connections.
 */
public class ChattyChatChatServer {

    /**
     * The main method that starts the server. It initializes a {@code ServerSocket}
     * to listen on the
     * specified port and continuously accepts incoming client connections. Each
     * client is handled in
     * a separate thread.
     *
     * @param args Command-line arguments, expects a single argument: the port
     *             number on which the server will listen.
     * @throws IOException If an I/O error occurs while setting up the server socket
     *                     or accepting connections.
     */
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
