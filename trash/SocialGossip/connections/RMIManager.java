package connections;

import gui.Utils;
import state.Friend;
import state.Message;
import state.User;
import misc.Configuration;
import remoteinterfaces.ClientCallbackInterface;
import remoteinterfaces.ServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI RMIManager: register the RMI Callbacks at Login
 */
public class RMIManager {

    /**
     * Inner class for Server callbacks, implementation of ClientCallbackInterface
     */
    public static class ClientCallbackImplementation implements ClientCallbackInterface {
        public void newFriend(String username) {
            Utils.showInfoDialog(username + " added you as a friend!");
            User.addFriend(username, true);
        }

        public void changedStatus(String username, boolean isOnline) {
            User.setFriendStatus(username, isOnline);

            // Show a System message if there is a chat window open
            Friend friend = User.getFriend(username);
            if (friend == null) return;
            if (friend.getWindow() == null) return;
            Message systemInfo = new Message("SYSTEM",
                    friend.getUsername() + " is now " + (isOnline ? "online" : "offline") + ".");
            friend.newMessage(systemInfo);
        }
    }

    private static final ClientCallbackInterface callback = new ClientCallbackImplementation();
    @SuppressWarnings("CanBeFinal")
    private static ServerInterface server;
    
    static {
        try {
            Registry registry = LocateRegistry.getRegistry(Configuration.SERVE_HOSTNAME, Configuration.RMI_PORT);
            server = (ServerInterface) registry.lookup(Configuration.RMI_NAME);
            
        }
        catch (RemoteException e) {
            System.err.println("Fatal error: cannot connect to RMI registry.");
            e.printStackTrace();
            System.exit(1);
        }
        catch (NotBoundException e) {
            System.err.println("Fatal error: cannot find " + Configuration.RMI_NAME + " in RMI registry.");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void registerCallback() {
        try {
            ClientCallbackInterface stub = (ClientCallbackInterface) UnicastRemoteObject.exportObject(callback, 0);
            boolean success = server.registerCallback(User.username(), stub);
            if (!success) {
                throw new RemoteException("User offline for the server");
            }
        }
        catch (RemoteException e) {
            System.err.println("Fatal error: can't register callback in RMI server");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
