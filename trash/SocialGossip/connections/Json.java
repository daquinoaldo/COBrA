package connections;

import misc.Endpoints;
import state.Friend;
import state.User;
import state.Message;
import state.Room;
import gui.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static misc.Endpoints.*;

/**
 * Contains all methods to generate the request Json and sending it to the server.
 */
@SuppressWarnings("unchecked")
public class Json {

    /* ************************* *
     * REQUEST RECEIVING SECTION *
     * ************************* */

    /**
     * Parse a json
     * @param s the json string to parse
     * @return the parsed JSONObject
     */
    private static JSONObject parse(String s) {
        if (s == null) return null;
        try {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(s);
        }
        catch (ParseException e) {
            System.err.println("Got an invalid JSON: " + s);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse a request received on the message socket: it can be a private message or a request to receive a file.
     * @param jsonString the request to parse
     */
    public static void parsePrivateMessageRequest(String jsonString) {
        // First parse the json
        JSONObject request = parse(jsonString);
        if (request == null) return;
        
        JSONObject payload = (JSONObject) request.get("params");
        String endpoint = (String) request.get("endpoint");
        
        if (endpoint == null) {
            // not a request
            String status = (String) request.get("status");
            String message = (String) request.get("message");
            if (status != null && !status.equals("ok") && message != null)
                Utils.showErrorDialog(message);
            return;
        }
        
        switch (endpoint) {
            case MSG2FRIEND:
                // Got a message from friend
                String sender = (String) payload.get("sender");
                String text = (String) payload.get("text");
                Message msg = new Message(sender, text);
                User.getFriend(sender).newMessage(msg);
                break;
                
            case FILE2FRIEND:
                // Got a request to send a file to me
                String filename = (String) payload.get("filename");
                String fromUsername = (String) payload.get("from");
                String hostname = (String) payload.get("hostname");
                Integer port = Integer.parseInt((String) payload.get("port"));
    
                if (filename == null || fromUsername == null || hostname == null || port < 1024) {
                    System.err.println("Invalid File2Friend request");
                    System.err.println(fromUsername + "@" + hostname + ":" + port + " - " + filename);
                    return; // malformed request
                }
                
                boolean confirm = Utils.showConfirmationDialog(fromUsername + " wants to send you a file. Save it?");
                if (!confirm) break;
                
                // Opens a dialog to select destination file
                File destFile = Utils.saveFileDialog(filename);
                if (destFile == null) {
                    User.getFriend(fromUsername).newMessage(
                            new Message("SYSTEM", "Download from " + fromUsername + " aborted."));
                    break;
                }

                // Receives the file
                User.getFriend(fromUsername).newMessage(
                        new Message("SYSTEM", "Starting download from "+fromUsername+"."));
                Connection.receiveFile(destFile, hostname, port);
                User.getFriend(fromUsername).newMessage(
                        new Message("SYSTEM", "Download from " + fromUsername + " completed.") );
                break;
                
            case ROOM_MESSAGE:
                // If a room message is here, it's probably an error. They should arrive on the UDP multicast socket!
                String status = (String) payload.get("status");
                String message = (String) payload.get("message");
                if (status != null && status.equals("err")) {
                    String roomName = (String) payload.get("recipient");
                    Room room = User.getRoom(roomName);
                    if (room != null) room.newMessage(new Message("SYSTEM", message));
                    else Utils.showErrorDialog("Room error: " + message);
                }
                else System.err.println("Invalid chat message request received.");
                break;
        }
    }

    /**
     * Parse a request received on the UDP multicast socket.
     * @param jsonString the request to parse
     */
    public static void parseChatMessage(String jsonString) {
        JSONObject request = parse(jsonString);
    
        String roomName = (String) request.get("recipient");
        String chatClosed = (String) request.get("chat_closed");
        if (chatClosed != null && chatClosed.length() > 0) {
            // Room has been closed
            if (roomName == null) return;
            Utils.showErrorDialog(roomName + " has been closed.");
            Room room = User.getRoom(roomName);
            if (room == null) return;
            room.leaveMulticastGroup();
            room.closeWindow();
            if (!User.removeRoom(roomName))
                System.err.println("Can't remove room "+roomName+" from User.");
            return;
        }
        
        String sender = (String) request.get("sender");
        String text = (String) request.get("text");
        
        if (roomName == null || roomName.length() == 0 ||
                sender == null || sender.length() == 0 ||
                text == null || text.length() == 0)
            return; // Malformed request
        
        if (sender.equals(User.username()))
            return; // my message
        
        Room room = User.getRoom(roomName);
        if (room == null) {
            System.err.println("Got a message for a non-subscribed room: " + roomName);
            return;
        }

        // sends the parsed message to the room that will handle it
        room.newMessage(new Message(sender, text));
    }


    /* *********************** *
     * REQUEST SENDING SECTION *
     * *********************** */

    /**
     * Heartbeat: message sent periodically to the server to inform it that we are online.
     * If the server stop receive the heartbeat close the socket with the client.
     */
    public static void heartbeat() {
        makeMsgRequest(HEARTBEAT, null);
    }

    public static void login(String username, String password) {
        if (username == null || password == null || username.length() == 0 || password.length() == 0)
            throw new IllegalArgumentException("Username and password must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("password", password);
        
        JSONObject reply = makeRequest(LOGIN, parameters);
        if (reply == null)
            return;
        
        makeMsgRequest(LOGIN, parameters);
        
        User.setLoggedIn(true);
        User.setUsername(username);

        listFriends();
        chatList();
    }
    
    public static boolean register(String username, String password, String language) {
        if (username == null || password == null || language == null ||
                username.length() == 0 || password.length() == 0 || language.length() == 0)
            throw new IllegalArgumentException("Username, password and language must be a non-empty string.");
    
        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", username);
        parameters.put("password", password);
        parameters.put("language", language);

        JSONObject result = makeRequest(Endpoints.REGISTER, parameters);
        return result != null;
    }
    
    public static boolean lookup(String username) {
        if (username == null || username.length() == 0)
            throw new IllegalArgumentException("Username must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", username);

        JSONObject result = makeRequest(Endpoints.LOOKUP, parameters);
        return result != null;
    }
    
    public static boolean friendship(String username) {
        if (username == null || username.length() == 0)
            throw new IllegalArgumentException("Username must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", username);

        JSONObject result = makeRequest(Endpoints.FRIENDSHIP, parameters);
        return result != null;
    }

    /**
     * Check if another user is online.
     * It was not required, but it is necessary to know if the friend added with friendship is online or not.
     * @param username of the user we want to check
     * @return true if online, false othewise
     */
    public static boolean isOnline(String username) {
        if (username == null || username.length() == 0)
            throw new IllegalArgumentException("Username must be a non-empty string.");
        
        Map<String, String> parameters = new HashMap<>();
        parameters.put("username", username);

        JSONObject result = makeRequest(Endpoints.IS_ONLINE, parameters);
        return result != null && (boolean) result.get("online");
    }

    /**
     * Request the friends list and when it receives it updates the one saved in User
     */
    private static void listFriends() {
        JSONObject result = makeRequest(Endpoints.LIST_FRIEND, null);
        if(result == null) return;

        // Update the state
        JSONArray jsonArray = (JSONArray) result.get("friends");
        if (jsonArray == null) return; // no friends yet

        HashMap<String, Friend> friends = new HashMap<>();
        for (Object jsonObject : jsonArray) {
            String username = (String) ((JSONObject) jsonObject).get("username");
            boolean online = (boolean) ((JSONObject) jsonObject).get("online");
            friends.put(username, new Friend(username, online));
        }

        User.updateFriendList(friends);
    }

    /**
     * Send the request to create a new room and if it receives a positive response, add it to the state
     * @param roomName the name of the room you want create
     * @return true if success, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean createRoom(String roomName) {
        if (roomName == null || roomName.length() == 0)
            throw new IllegalArgumentException("The room name must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("room", roomName);

        JSONObject result = makeRequest(Endpoints.CREATE_ROOM, parameters);
        if (result == null) return false;

        // Update the state
        String address = (String) result.get("address");
        return User.addRoom(roomName, address, User.username(), true);
    }

    public static boolean addMe(String room) {
        if (room == null || room.length() == 0)
            throw new IllegalArgumentException("The room name must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("room", room);

        JSONObject result = makeRequest(Endpoints.ADD_ME, parameters);
        return result != null;
    }

    /**
     * Request the room list and when it receives it updates the one saved in User
     */
    public static void chatList() {
        JSONObject result = makeRequest(Endpoints.CHAT_LIST, null);
        if(result == null) return;

        // Update the state
        JSONArray jsonArray = (JSONArray) result.get("rooms");
        if (jsonArray == null) return; // no chats yet

        HashMap<String, Room> rooms = new HashMap<>();
        for (Object jsonObject : jsonArray) {
            String name = (String) ((JSONObject) jsonObject).get("name");
            String address = (String) ((JSONObject) jsonObject).get("address");
            String creator = (String) ((JSONObject) jsonObject).get("creator");
            boolean subscribed = (boolean) ((JSONObject) jsonObject).get("subscribed");
            try {
                rooms.put(name, new Room(name, address, creator, subscribed));
            }
            catch (UnknownHostException e) {
                System.err.println("Unable to join the room " + name);
                e.printStackTrace();
            }
        }

        User.updateRoomList(rooms);
    }

    public static void closeRoom(String roomName) {
        if (roomName == null || roomName.length() == 0)
            throw new IllegalArgumentException("The room name must be a non-empty string.");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("room", roomName);

        JSONObject result = makeRequest(Endpoints.CLOSE_ROOM, parameters);
        if(result == null) System.err.println("Json: Error in closing room "+roomName+".");
    }

    /**
     * Helper: creates the payload for a message request
     * @param recipient of the message, can be an username or a room name
     * @param text the message content
     * @return the JSONObject to send as payload
     */
    private static JSONObject genericMsg(String recipient, String text) {
        JSONObject req = new JSONObject();
        req.put("sender", User.username());
        req.put("recipient", recipient);
        req.put("text", text);
        return req;
    }

    /**
     * Sends a message to another user
     * @param recipient the username of the recipient of the message
     * @param text the message content
     */
    public static void sendMsg(String recipient, String text) {
        JSONObject req = genericMsg(recipient, text);
        makeMsgRequest(Endpoints.MSG2FRIEND, req);
    }

    /**
     * Sends a message to a chat room
     * @param room the room name
     * @param text the message content
     */
    public static void sendChatMsg(String room, String text) {
        JSONObject req = genericMsg(room, text);
        Multicast.send(req.toJSONString());
    }

    /**
     * Sends a file request
     * @param recipient the recipient username
     */
    public static void sendFileRequest(String recipient) {
        // Select the file to send, if the user press cancel this method returns
        File file = Utils.openFileDialog();
        if (file == null) return;

        ServerSocketChannel serverSocketChannel = Connection.openFileSocket();
        if (serverSocketChannel == null) {
            Utils.showErrorDialog("Error while opening server socket.");
            return;
        }

        JSONObject payload = new JSONObject();
        payload.put("filename", file.getName());
        payload.put("from", User.username());
        payload.put("to", recipient);
        payload.put("port", Integer.toString(serverSocketChannel.socket().getLocalPort()));
    
        JSONObject result = makeRequest(FILE2FRIEND, payload);
    
        if (result != null) {
            User.getFriend(recipient).newMessage( new Message("SYSTEM", "Starting upload to "+recipient+".") );
            Connection.startFileSender(serverSocketChannel, file, () -> User.getFriend(recipient).newMessage(
                    new Message("SYSTEM", "Completed upload to "+recipient+".")));
        }
    }

    /* *********************** *
     * REQUEST SENDING HELPERS *
     * *********************** */
    
    /**
     * @return true if server reply doesn't contain errors, false otherwise
     */
    private static boolean isReplyOk(JSONObject reply) {
        return reply != null && reply.get("status").equals("ok");
    }

    private static JSONObject makeRequest(String endpoint, Map params) throws IllegalArgumentException {
        return makeGenericRequest(endpoint, params, false);
    }

    private static void makeMsgRequest(String endpoint, Map params) throws IllegalArgumentException {
        makeGenericRequest(endpoint, params, true);
    }

    /**
     * Build a JSON to be sent to the server.
     * @param endpoint String representing the type of the request, must be specified
     * @param params payload: Map with key-value pairs to be included in the request, can be null
     * @return JSONObject with response, or null if an error occurred
     * @throws IllegalArgumentException if an invalid endpoint is specified
     */
    private static synchronized JSONObject makeGenericRequest(String endpoint, Map params, boolean isMsgRequest)
            throws IllegalArgumentException {
        if (endpoint == null || endpoint.length() == 0)
            throw new IllegalArgumentException("Invalid endpoint specified.");

        JSONObject jsonParams = new JSONObject();
        if(params != null) jsonParams.putAll(params); // it can be null if the request has no parameters

        JSONObject request = new JSONObject();
        request.put("endpoint", endpoint);
        request.put("params", jsonParams);


        // send request to the server using the right socket
        if (isMsgRequest) {
            Connection.sendMsgRequest(request.toJSONString());
            return null;
        }
        
        String responseString = Connection.sendRequest(request.toJSONString());
        
        if (responseString == null) {
            Utils.showErrorDialog("Can't reach server. Check internet connection and try again.");
            return null;
        }
    
        JSONObject response = parse(responseString);
        if (isReplyOk(response)) {
            JSONObject result = (JSONObject) response.get("result");
            if (result == null) result = new JSONObject();
            return result;
        }
        else {
            String msg = (String) response.get("message");
            Utils.showErrorDialog(msg != null && msg.length() > 0 ? msg : "Unknown error.");
            return null;
        }
    }
}
