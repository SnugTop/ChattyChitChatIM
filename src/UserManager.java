import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final Map<String, List<ClientHandler>> nicknames = new ConcurrentHashMap<>();

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

    public Map<String, ClientHandler> getClients() {
        return Collections.unmodifiableMap(new HashMap<>(clients));
    }

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

    public void updateClientKey(String oldKey, String newKey) {
        ClientHandler client = clients.remove(oldKey);
        if (client != null) {
            clients.put(newKey, client);
        }
    }

    public List<ClientHandler> getClientsByNickname(String nickname) {
        List<ClientHandler> handlers = nicknames.get(nickname);
        if (handlers == null) {
            return Collections.emptyList();
        }
        synchronized (handlers) {
            return new ArrayList<>(handlers);
        }
    }

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

    private void addClientToNewNickname(String newNickname, ClientHandler client) {
        synchronized (nicknames) {
            nicknames.computeIfAbsent(newNickname, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(client);
        }
    }
}
