package me.snugtop.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Manages the users connected to the ChattyChatChat server. This class is
 * designed to be thread-safe,
 * using concurrent collections and synchronized blocks to manage client
 * information safely in a multi-threaded environment.
 *
 * The main data structures used are:
 * - A ConcurrentHashMap for clients, allowing thread-safe operations on client
 * details.
 * - Another ConcurrentHashMap for nicknames, mapping nicknames to lists of
 * ClientHandlers.
 *
 * Where concurrent collections alone are not sufficient to ensure atomicity of
 * operations,
 * synchronized blocks are used to guard complex operations on these
 * collections.
 */
public class UserManager {
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, List<ClientHandler>> nicknames = new ConcurrentHashMap<>();

    /**
     * Adds a client to the manager. This operation is thread-safe due to the use of
     * ConcurrentHashMap.
     *
     * @param username      The username or identifier of the client.
     * @param clientHandler The ClientHandler associated with the client.
     */
    public void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
    }

    public void removeClient(String username) {
        ClientHandler client = clients.remove(username);
        if (client != null) {
            removeClientFromOldNickname(client.getNickname(), client);
        }
    }

    public ClientHandler getClient(String username) {
        return clients.get(username);
    }

    /**
     * Provides an unmodifiable view of the current clients.
     *
     * @return An unmodifiable map of clients.
     */
    public Map<String, ClientHandler> getClients() {
        return Collections.unmodifiableMap(new HashMap<>(clients));
    }

    /**
     * Updates the nickname of a client. This method is synchronized on the
     * nicknames map,
     * ensuring thread safety when modifying the list of client handlers.
     *
     * @param oldNickname The old nickname of the client.
     * @param newNickname The new nickname to be associated with the client.
     * @param client      The ClientHandler associated with the client.
     */
    public void updateNickname(String oldNickname, String newNickname, ClientHandler client) {
        if (!isValidNickname(newNickname)) {
            return;
        }

        removeClientFromOldNickname(oldNickname, client);
        addClientToNewNickname(newNickname, client);
    }

    private boolean isValidNickname(String nickname) {
        return nickname != null && !nickname.trim().isEmpty() && !nickname.contains(" ") && !nickname.startsWith("/");
    }

    /**
     * Updates the key associated with a client in the clients map.
     *
     * @param oldKey The old key or username of the client.
     * @param newKey The new key or username to be associated with the client.
     */
    public void updateClientKey(String oldKey, String newKey) {
        ClientHandler client = clients.remove(oldKey);
        if (client != null) {
            clients.put(newKey, client);
        }
    }

    /**
     * Retrieves a list of ClientHandlers associated with a given nickname.
     * The method is synchronized on the list of handlers for the given nickname,
     * ensuring thread safety when accessing the list.
     *
     * @param nickname The nickname for which to find associated ClientHandlers.
     * @return A list of ClientHandlers associated with the given nickname. If no
     *         clients are found, returns an empty list.
     */
    public List<ClientHandler> getClientsByNickname(String nickname) {
        List<ClientHandler> handlers = nicknames.get(nickname);
        if (handlers == null) {
            return Collections.emptyList();
        }
        synchronized (handlers) {
            return new ArrayList<>(handlers);
        }
    }

    /**
     * Removes a client from the list of handlers associated with an old nickname.
     * This method is synchronized on the nicknames map, ensuring thread safety
     * when modifying the list of client handlers.
     *
     * @param oldNickname The old nickname of the client.
     * @param client      The ClientHandler to be removed.
     */
    private void removeClientFromOldNickname(String oldNickname, ClientHandler client) {
        if (oldNickname == null || oldNickname.isEmpty()) {
            return;
        }

        synchronized (nicknames) {
            List<ClientHandler> oldHandlers = nicknames.get(oldNickname);
            if (oldHandlers != null) {
                oldHandlers.remove(client);
                if (oldHandlers.isEmpty()) {
                    nicknames.remove(oldNickname);
                }
            }
        }
    }

    /**
     * Adds a client to the list of handlers associated with a new nickname.
     * This method is synchronized on the nicknames map, ensuring thread safety
     * when modifying the list of client handlers.
     *
     * @param newNickname The new nickname to be associated with the client.
     * @param client      The ClientHandler to be added.
     */
    private void addClientToNewNickname(String newNickname, ClientHandler client) {
        synchronized (nicknames) {
            nicknames.computeIfAbsent(newNickname, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(client);
        }
    }
}
