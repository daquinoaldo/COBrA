package connections;

import misc.Configuration;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * UDP connections class.
 * Contains methods to send an UDP message to the server and to receive the UDP multicast messages (the rooms broadcast)
 */
public class Multicast {
    @SuppressWarnings("CanBeFinal")
    private static MulticastSocket ms = null;
    @SuppressWarnings("CanBeFinal")
    private static DatagramSocket outputDatagramSocket;

    private static final HashMap<InetAddress, String> addressToRoomName = new HashMap<>();

    static {
        try {
            ms = new MulticastSocket(Configuration.MULTICAST_PORT);
            ms.setReuseAddress(true);

            outputDatagramSocket = new DatagramSocket();
        }
        catch (IOException e) {
            System.err.println("Fatal error while binding UDP port:");
            e.printStackTrace();
            System.exit(1);
        }

        Thread listener = new Thread(() -> {
            while (!Thread.interrupted()) {
                byte[] buffer = new byte[8192];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    ms.receive(packet);
                } catch (IOException e) {
                    System.err.println("Error while receiving multicast packet");
                    e.printStackTrace();
                    continue;
                }

                // Extract data byte from packet and convert them to String
                int size = packet.getLength();
                byte[] data = packet.getData();
                String stringData = new String(data, 0, size, StandardCharsets.UTF_8);

                Json.parseChatMessage(stringData);
            }
        });
        listener.start();
    }

    /**
     * Sends a datagramPacket to the server
     * @param request the json stringified request
     */
    public static void send(String request) {
        try {
            InetAddress destAddress = InetAddress.getByName(Configuration.SERVE_HOSTNAME);
            byte[] data = request.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, destAddress, Configuration.UDP_PORT);
            outputDatagramSocket.send(datagramPacket);
        }
        catch (IOException e) {
            System.err.println("Error while sending UDP packet");
            e.printStackTrace();
        }
    }

    /**
     * Join a multicast group, called when the user joins a room (or at login with the joined rooms)
     * @param roomName the joined room name
     * @param address the multicast address of the room in quad-dotted decimal notation (239.x.x.x)
     * @throws IllegalArgumentException if the address is not valid (not a multicast address)
     */
    public static void joinGroup(String roomName, InetAddress address) throws IllegalArgumentException {
        if (!address.isMulticastAddress()) {
            throw new IllegalArgumentException("Not a valid multicast address: " + address.getHostName());
        }

        if (addressToRoomName.containsValue(roomName))
            return;

        try {
            ms.joinGroup(address);
            addressToRoomName.put(address, roomName);
        }
        catch (IOException e) {
            System.err.println("Error while joining group: " + address.getHostName());
            e.printStackTrace();
        }
    }

    /**
     * Leave a multicast group, used when a joined room is deleted
     * @param address InetAddress of the group to leave
     */
    public static void leaveGroup(InetAddress address) {
        if (!addressToRoomName.containsKey(address))
            return; // no group to leave from
        
        try {
            ms.leaveGroup(address);
            addressToRoomName.remove(address);
        }
        catch (IOException e) {
            System.err.println("Error while leaving multicast group: " + address.getHostName());
            e.printStackTrace();
        }
    }
}
