package gui;

import gui.constants.Icons;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 * Utilities for the gui
 */
public class Utils {
    
    public static void showErrorDialog(String msg) {
        showDialog("Error", msg, WARNING_MESSAGE);
    }
    
    public static void showInfoDialog(String msg) {
        showDialog("Info", msg, INFORMATION_MESSAGE);
    }
    
    private static void showDialog(String title, String msg, int type) {
        Thread t = new Thread(() -> JOptionPane.showMessageDialog(null, msg,title, type));
        t.start();
    }
    
    public static boolean showConfirmationDialog(String msg) {
        return JOptionPane.showConfirmDialog(null, msg,"Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /**
     * Create and show a new Window
     * @param title of the Window
     * @param panel to show in the Window body
     * @param dimension of the Window
     * @return the generated JFrame
     */
    public static JFrame createWindow(String title, JPanel panel, Dimension dimension) {
        JFrame window = new JFrame(title);                                  // create a window
        window.setIconImage(Icons.logo.getImage());                         // set logo as application icon
        window.setSize(dimension);                                          // set specified size
        window.setContentPane(panel);                                       // put a panel inside the window
        window.setLocationRelativeTo(null);                                 // center the window
        window.setVisible(true);                                            // show it
        return window;
    }

    /**
     * Create and show a new centered Window with fixed dimensions and not resizable.
     * @param title of the Window
     * @param panel to show in the Window body
     * @param exitOnClose if true exit the Client when the Window is close
     * @param alwaysOnTop if true the Window is showed always on top
     * @return the generated JFrame
     */
    public static JFrame createFixedWindow(String title, JPanel panel, boolean exitOnClose, boolean alwaysOnTop) {
        JFrame window = new JFrame(title);                                  // create a window
        window.setIconImage(Icons.logo.getImage());                         // set logo as application icon
        window.setContentPane(panel);                                       // put a panel inside the window
        window.pack();                                                      // resize the window based on content size
        window.setLocationRelativeTo(null);                                 // center the window
        if (exitOnClose)
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // exit program when window gets closed
        window.setAlwaysOnTop(alwaysOnTop);
        window.setResizable(false);                                         // unresizable window
        window.setVisible(true);                                            // show it
        return window;
    }

    /**
     * Call the setEnabled function for each component in a container. Used by the Login and Register Panels
     * @param container for which components are to be enabled
     * @param enable true to enable, false to disable
     */
    public static void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }
    
    public static File openFileDialog() {
        return fileDialog(true, null);
    }
    public static File saveFileDialog(String defaultName) {
        return fileDialog(false, defaultName);
    }

    /**
     * Show the file selection dialog for file choosing and saving
     * @param isOpenDialog if true pick a file, if false choose where to save the incoming file
     * @param filename should be null if isOpenDialog, otherwise specify the original filename of the incoming file
     * @return a File
     */
    private static File fileDialog(boolean isOpenDialog, String filename) {
        File selected = null;
        boolean aFileIsSelected = false;
    
        JFileChooser chooser = null;
        LookAndFeel previousLF = UIManager.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            chooser = new JFileChooser();
            UIManager.setLookAndFeel(previousLF);
        } catch (IllegalAccessException | UnsupportedLookAndFeelException | InstantiationException |
                ClassNotFoundException e) {
            e.printStackTrace();
        }
    
        if (chooser == null) chooser = new JFileChooser();
        if (filename != null) chooser.setSelectedFile(new File(filename));
        
        do {
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnStatus = isOpenDialog ? chooser.showOpenDialog(null) : chooser.showSaveDialog(null);
            
            if (returnStatus == JFileChooser.APPROVE_OPTION)
                selected = chooser.getSelectedFile();
            else continue;
    
            if (!isOpenDialog && selected.exists()) {
                aFileIsSelected = showConfirmationDialog("The file will be overwritten. Are you sure?");
            } else if (!isOpenDialog && selected.exists() && !selected.canWrite()) {
                showErrorDialog("Can't write in the specified path. Please try again.");
            } else if (isOpenDialog && !selected.canRead()) {
                showErrorDialog("Can't read the selected file. Please try again.");
            } else {
                aFileIsSelected = true;
            }
            
        } while (!aFileIsSelected);
        
        return selected;
    }

    /**
     * Validate the username for the Login and Register Forms
     * @param username to validate
     * @return true if is valid, false if not
     */
    public static boolean isValidUsername(String username) {
        Pattern p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(username);
        return username.length() > 0 && !m.find();
    }
}
