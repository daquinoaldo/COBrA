package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.constants.Images;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Utilities for the GUI.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Utils {

    /**
     * Show a new centered Window with fixed dimensions and not resizable.
     * @param title of the Window.
     * @param panel to show in the Window body.
     * @param exitOnClose if true exit the Client when the Window is close.
     * @return the generated JFrame.
     */
    public static JFrame newWindow(String title, JComponent panel, boolean exitOnClose) {
        JFrame window = new JFrame(title);                                  // create a window
        window.setIconImage(Images.logo.getImage());                         // set logo as application icon
        window.setContentPane(panel);                                       // put a panel inside the window
        window.pack();                               // resize the window based on content size
        window.setLocationRelativeTo(null);                                 // center the window
        if (exitOnClose)
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); // exit program when window gets closed
        window.setVisible(true);                                            // show it
        return window;
    }

    /**
     * Shows a dialog, running on another thread.
     * @param msg the dialog message.
     */
    public static void newMessageDialog(String msg) {
        newDialog("Info", msg, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an error dialog, running on another thread.
     * @param msg the error message.
     */
    public static void newErrorDialog(String msg) {
        newDialog("Error", msg, JOptionPane.WARNING_MESSAGE);
    }

    // auxiliary function
    private static void newDialog(String title, String msg, int type) {
        Thread t = new Thread(() -> JOptionPane.showMessageDialog(null, msg, title, type));
        t.start();
    }

    /**
     * Shows an error dialog that advice that the program will close, then exit the program.
     * @param errorMessage the error message.
     */
    public static void newExitDialog(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, "ERROR! Exiting...",
                JOptionPane.ERROR_MESSAGE);
        System.err.println("Exiting from the program. Reason: " + errorMessage);
        System.exit(1);
    }

    /**
     * Shows a confirmation dialog (yes/no).
     * @param msg the question.
     * @return true for yes, false for no.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean newConfirmDialog(String msg) {
        return JOptionPane.showConfirmDialog(null, msg,"Warning", JOptionPane.YES_NO_OPTION) ==
                JOptionPane.YES_OPTION;
    }

    /**
     * Set the font size of a label.
     * @param label of which set the font size.
     * @param fontSize the size to set.
     */
    public static void setFontSize(JLabel label, int fontSize) {
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, fontSize));
    }

    /**
     * Show the file selection dialog for file choosing and saving.
     * @return a File.
     */
    public static File openFileDialog() {
        return fileDialog(true, null);
    }

    /**
     * Show the file selection dialog for file choosing and saving.
     * @param defaultName specify the original filename of the incoming file.
     * @return a File.
     */
    public static File saveFileDialog(String defaultName) {
        return fileDialog(false, defaultName);
    }

    // auxiliary function
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
                aFileIsSelected = newConfirmDialog("The file will be overwritten. Are you sure?");
            } else if (!isOpenDialog && selected.exists() && !selected.canWrite()) {
                newErrorDialog("Can't write in the specified path. Please try again.");
            } else if (isOpenDialog && !selected.canRead()) {
                newErrorDialog("Can't read the selected file. Please try again.");
            } else {
                aFileIsSelected = true;
            }

        } while (!aFileIsSelected);

        return selected;
    }

}
