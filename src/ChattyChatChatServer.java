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
        Socket client = null;
        try {
            listener = new ServerSocket(port);
            System.out.println("Server is connected to port " + port);
            int clientNumber = 1;

            while (true) {

                try {
                    System.out.println("Waiting for client connection...");
                    client = listener.accept();
                    System.out.println("Connected to client " + clientNumber + ": " + client);

                    // Create a new thread for each connection
                    Thread t = new Thread(new ClientHandler(client, clientNumber));
                    t.start();
                    clientNumber++;
                } catch (IOException e) {
                    System.out.println("Error connecting to client: " + clientNumber);
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            System.out.println("Error listening for client connection");
            System.out.println(e.getMessage());
        } finally {
            if (listener != null) {
                try {
                    listener.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return;
    }
}