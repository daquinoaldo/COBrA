package com.aldodaquino.cobra.connections;

import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileExchange {

    /**
     * Receive and save a file.
     * @param destFile the destination file where save data.
     * @param hostname of the sender.
     * @param port of the sender.
     */
    public static void receiveFile(File destFile, String hostname, int port) {
        Thread asyncWriter = new Thread(() -> {
            int failedCount = 0;
            boolean stop = false;
            do {
                try (SocketChannel socket = SocketChannel.open(new InetSocketAddress(hostname, port))) {
                    System.out.println("Started download: " + destFile.getAbsolutePath() + ".");
                    Filesystem.writeFile(socket, destFile);
                    System.out.println("Download finished.");
                    stop = true;
                } catch (IOException e) {
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
                        System.err.println("Can't connect to the sender.");
                        e.printStackTrace();

                    }
                }
            } while (!stop);
        });
        asyncWriter.start();
    }

    /**
     * Opens a socket on which waits an incoming connection from the file recipient.
     * @return a ServerSocketChannel.
     */
    public static ServerSocketChannel openFileSocket() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(null);
            return serverSocketChannel;
        }
        catch (IOException e) {
            System.err.println("Error while opening a socket for sending file.");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * When the other client has connected to the socket it behaves as a server and sends the file.
     * @param serverSocket the ServerSocketChannel opened in openFileSocket.
     * @param file the File to be sent.
     * @param callback optional callback to be run when the upload is finished.
     */
    public static void startFileSender(ServerSocketChannel serverSocket, File file, Runnable callback) {
        Thread listener = new Thread(() -> {
            try {
                serverSocket.socket().setSoTimeout(60000);  // 1 minute
                SocketChannel socketChannel = serverSocket.accept();
                System.out.println("Started upload: " + file.getAbsolutePath() + ".");
                Filesystem.readFile(file, socketChannel);
                System.out.println("Upload finished.");
                if (callback != null) callback.run();
            } catch (IOException e) {
                System.err.println("Error while accepting connection to send file.");
                e.printStackTrace();
            }
        });
        listener.start();
    }

}