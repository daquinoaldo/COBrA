package connections;

import state.User;
import misc.Configuration;
import gui.Utils;

import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * TCP connections class.
 * Contains methods to send private messages and files.
 * It is the lowest level and is invoked by the json class that is responsible for generating the right requests sent
 * through these sockets.
 * There are 2 sockets, each with its reader and writer:
 * - primary for the operational request: login, register, lookup, friendship, listFriend createRoom, addMe, chatList,
 * addMe (required) and the auxiliar isOnline to check if a friend is online;
 * - secondary for the messages request: msg2friend, file2friend, roomMessage (required) and the auxiliar heartbeat to
 * notify the server that we are still online.
 */
public class Connection {
    private static final String host = Configuration.SERVE_HOSTNAME;

    @SuppressWarnings("CanBeFinal")
    private static BufferedWriter primaryWriter;
    @SuppressWarnings("CanBeFinal")
    private static BufferedReader primaryReader;
    
    @SuppressWarnings("CanBeFinal")
    private static BufferedWriter msgWriter;
    @SuppressWarnings("CanBeFinal")
    private static BufferedReader msgReader;

    /* Static initializer */
    static {
        try {
            Socket primarySocket = new Socket(host, Configuration.PRIMARY_PORT);
            primaryWriter = new BufferedWriter(new OutputStreamWriter(primarySocket.getOutputStream()));
            primaryReader = new BufferedReader(new InputStreamReader(primarySocket.getInputStream()));
    
            Socket msgSocket = new Socket(host, Configuration.MSG_PORT);
            msgWriter = new BufferedWriter(new OutputStreamWriter(msgSocket.getOutputStream()));
            msgReader = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));
        }
        catch (IOException e) {
            System.err.println("Fatal error: can't establish connection with the server.");
            e.printStackTrace();
            System.exit(1);
        }
        
        Thread msgRequestListener = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    String msgRequest = msgReader.readLine();
                    Json.parsePrivateMessageRequest(msgRequest);
                }
                catch (IOException e) {
                    System.err.println("Error while reading message socket");
                    e.printStackTrace();
                }
            }
        });
        msgRequestListener.start();
        
        Thread heart = new Thread(() -> {
            while (!Thread.interrupted()) {
                Json.heartbeat();
                try { Thread.sleep(2000); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        User.addLoginListener((loggedIn) -> {
            if (loggedIn) heart.start();
            else heart.interrupt();
        });
    }
    
    /**
     * Force static initializer to be triggered.
     */
    @SuppressWarnings("EmptyMethod")
    public static void init() {}
    
    /**
     * Send a String to the server through the primary connection. Wait for a reply and return it as a String.
     * @param request String to be sent.
     * @return String returned by the server, can be null.
     */
    public synchronized static String sendRequest(String request) {
        return send(primaryWriter, primaryReader, request);
    }
    
    /**
     * Send a String to the server through the message connection. Wait for a reply and return it as a String.
     * @param request String to be sent.
     */
    public synchronized static void sendMsgRequest(String request) {
        send(msgWriter, null, request);
    }

    /**
     * Send a request to the server
     * @param writer of the socket, primary for operational request, secondary for message
     * @param reader of the socket, primary for operational request, secondary for message
     * @param request the json stringified request: must include the ENDPOINT and a param object (payload, can be null)
     * @return the response string (a json stringified)
     */
    private synchronized static String send(BufferedWriter writer, BufferedReader reader, String request) {
        if (writer == null)
            return null;
        
        try {
            writer.write(request);
            writer.newLine();
            writer.flush();
            
            if (reader == null)
                return null;
            else
                return reader.readLine();
        }
        catch (IOException e) {
            System.err.println("Fatal error occurred while communicating with the server.");
            e.printStackTrace();
            System.exit(1);
        }
    
        return null;
    }

    /**
     * Receive a file from a friend
     * @param destFile the destination file where save data
     * @param hostname of the sender
     * @param port of the sender
     */
    public static void receiveFile(File destFile, String hostname, int port) {
        Thread asyncWriter = new Thread(() -> {
            int failedCount = 0;
            boolean stop = false;
            do {
                try (SocketChannel socket = SocketChannel.open(new InetSocketAddress(hostname, port))) {
                    System.out.println("Started download: " + destFile.getAbsolutePath());
                    Filesystem.writeFile(socket, destFile);
                    System.out.println("Download finished.");
                    stop = true;
                } catch (IOException ioEx) {
                    if (failedCount < 3) {
                        try {
                            failedCount++;
                            Thread.sleep(5000);
                        } catch (InterruptedException intExc) {
                            intExc.printStackTrace();
                            break;
                        }
                    }
                    else {
                        stop = true;
                        System.err.println("Can't connect to the other peer");
                        ioEx.printStackTrace();
                        
                    }
                }
            } while (!stop);
        });
        asyncWriter.start();
    }

    /**
     * Used in Json.sendFileRequest: opens a socket on which waits an incoming connection from the file recipient
     * @return a ServerSocketChannel
     */
    public static ServerSocketChannel openFileSocket() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(null);
            return serverSocketChannel;
        }
        catch (IOException e) {
            System.err.println("Error while opening p2p socket for sending file");
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Used in Json.sendFileRequest:
     * when the other client has connected to the socket it behaves as a server and sends the file
     * @param serverSocket the ServerSocketChannel opened in openFileSocket
     * @param file the File to be sent
     * @param callback to be run when the upload is finished
     */
    public static void startFileSender(ServerSocketChannel serverSocket, File file, Runnable callback) {
        Thread listener = new Thread(() -> {
            try {
                serverSocket.socket().setSoTimeout(60000);
                SocketChannel socketChannel = serverSocket.accept();
                System.out.println("Started upload: " + file.getAbsolutePath());
                Filesystem.readFile(file, socketChannel);
                System.out.println("Upload finished.");
                callback.run();
            }
            catch (SocketTimeoutException e) {
                Utils.showErrorDialog("The user did not accept your request.");
            }
            catch (IOException e) {
                System.err.println("Error while accepting connection from other peer");
                e.printStackTrace();
            }
        });
        listener.start();
    }
}
