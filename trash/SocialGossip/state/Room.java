package state;

import connections.Multicast;
import gui.panels.ChatPanel;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Implementation of the abstract class Chat,
 * a bit more complex than Friend because it includes methods for handling UDP sockets
 * @see Chat the abstract class
 * @see Friend the other implementation
 */
public class Room extends Chat {
    private InetAddress address;
    private String creator;
    
    public Room(String name, String mcAddress, String creator, boolean subscribed) throws UnknownHostException {
        super(Chat.TYPE_ROOM, name);
        if (mcAddress == null || creator == null)
            throw new IllegalArgumentException("Invalid chat parameters: <" + mcAddress + "," + creator + ">");

        this.address = InetAddress.getByName(mcAddress);
        this.creator = creator;
        chatPanel = new ChatPanel(this);
        this.setStatus(subscribed);
    }
    
    public void leaveMulticastGroup() {
        Multicast.leaveGroup(this.address);
    }

    public String getCreator() {
        return creator;
    }

    public boolean isSubscribed() {
        return getFlag();
    }

    public void setStatus(boolean subscribed) {
        setFlag(subscribed);
        if (subscribed) Multicast.joinGroup(getName(), this.address);
        else leaveMulticastGroup();
    }
}
