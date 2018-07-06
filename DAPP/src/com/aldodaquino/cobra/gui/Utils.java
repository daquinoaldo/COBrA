package com.aldodaquino.cobra.gui;

import com.aldodaquino.cobra.gui.components.ComponentFactory;

import javax.swing.*;

/**
 * Utilities for the gui
 */
public class Utils {

    /**
     * Shows a dialog, running on another thread.
     * @param msg the dialog message.
     */
    public static void showMessageDialog(String msg) {
        showDialog("Info", msg, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows an error dialog, running on another thread.
     * @param msg the error message.
     */
    public static void showErrorDialog(String msg) {
        showDialog("Error", msg, JOptionPane.WARNING_MESSAGE);
    }
    
    private static void showDialog(String title, String msg, int type) {
        Thread t = new Thread(() -> JOptionPane.showMessageDialog(null, msg, title, type));
        t.start();
    }

    /**
     * Shows a confirmation dialog (yes/no).
     * @param msg the question.
     * @return true for yes, false for no.
     */
    public static boolean showConfirmDialog(String msg) {
        return JOptionPane.showConfirmDialog(null, msg,"Warning", JOptionPane.YES_NO_OPTION) ==
                JOptionPane.YES_OPTION;
    }

    /**
     * Run a runnable asynchronously. Shows a loading panel during the loading.
     * @param runnable to be run.
     * @param window in which show the loading panel.
     */
    public static void doAsync(Runnable runnable, JFrame window) {
        startLoading(window);
        new Thread(() -> {
            runnable.run();
            stopLoading(window);
        }).start();
    }

    private static void startLoading(JFrame window) {
        window.setGlassPane(ComponentFactory.newSpinner());
        window.getGlassPane().setVisible(true);
    }

    private static void stopLoading(JFrame window) {
        window.getGlassPane().setVisible(false);
    }



    /*
     * Call the setEnabled function for each component in a container. Used by the Login and Register Panels
     * @param container for which components are to be enabled
     * @param enable true to enable, false to disable
     */
    /*public static void enableComponents(Container container, boolean enable) {
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
    public static File saveFileDialog(Strings defaultName) {
        return fileDialog(false, defaultName);
    }*/

    /*
     * Show the file selection dialog for file choosing and saving
     * @param isOpenDialog if true pick a file, if false choose where to save the incoming file
     * @param filename should be null if isOpenDialog, otherwise specify the original filename of the incoming file
     * @return a File
     */
    /*private static File fileDialog(boolean isOpenDialog, Strings filename) {
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
                aFileIsSelected = showConfirmDialog("The file will be overwritten. Are you sure?");
            } else if (!isOpenDialog && selected.exists() && !selected.canWrite()) {
                showErrorDialog("Can't write in the specified path. Please try again.");
            } else if (isOpenDialog && !selected.canRead()) {
                showErrorDialog("Can't read the selected file. Please try again.");
            } else {
                aFileIsSelected = true;
            }
            
        } while (!aFileIsSelected);
        
        return selected;
    }*/

}
