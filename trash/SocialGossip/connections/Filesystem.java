package connections;

import gui.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.StandardOpenOption;

/**
 * Contains method to send a File into a Socket and receive from a Socket and save to File
 */
class Filesystem {

    /**
     * Read the specified File using NIO channels. Send the file to the specified socket.
     * @param file File object, the file to be read
     * @param outChannel Socket where the file will be sent
     */
    public static void readFile(File file, SocketChannel outChannel) {
        try (
                FileChannel inChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
        ) {
            long size = inChannel.size();
            ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
            sizeBuffer.putLong(size);
            sizeBuffer.flip();
            while (sizeBuffer.hasRemaining()) outChannel.write(sizeBuffer);
            
            long transferred = 0;
            while (size - transferred > 0)
                transferred += inChannel.transferTo(transferred, size - transferred, outChannel);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getAbsolutePath());
            Utils.showErrorDialog("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error while reading file: ");
            e.printStackTrace();
            Utils.showErrorDialog("Error while reading file.");
        }
    }
    
    /**
     * Read the specified Socket using NIO, and save the data to File.
     * @param inChannel Socket from where data will be read
     * @param file File object, destination of the data
     */
    public static void writeFile(SocketChannel inChannel, File file) {
        try (
                FileChannel outChannel = FileChannel.open(file.toPath(),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
        ) {
            ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
            while (sizeBuffer.hasRemaining())
                inChannel.read(sizeBuffer);
            sizeBuffer.flip();
            
            long size = sizeBuffer.getLong();
            long transferred = 0;
            
            while (size - transferred > 0)
                transferred += outChannel.transferFrom(inChannel, transferred, size - transferred);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getAbsolutePath());
            Utils.showErrorDialog("File not found: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error while writing file: ");
            e.printStackTrace();
            Utils.showErrorDialog("Error while writing file.");
        }
    }
}
