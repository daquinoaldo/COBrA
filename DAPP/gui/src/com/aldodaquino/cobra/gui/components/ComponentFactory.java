package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Images;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Components factory: makes components creation faster and easier.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class ComponentFactory {

    /**
     * Returns a new JButton.
     * @param text the text of the button.
     * @param e the ActionListener of the button.
     * @return a JButton.
     */
    public static JButton newButton(String text, ActionListener e) {
        JButton button = new JButton(text);
        button.addActionListener(e);
        return button;
    }

    /**
     * Returns a new JTextField.
     * @param e the ActionListener called when enter is pressed.
     * @return a JTextField.
     */
    public static JTextField newTextField(ActionListener e) {
        return newField(e, false);
    }

    /**
     * Returns a new JTextField for password. Characters are replaced with dots.
     * @param e the ActionListener called when enter is pressed.
     * @return a JTextField.
     */
    static JTextField newPasswordField(ActionListener e) {
        return newField(e, true);
    }

    // inner class
    private static JTextField newField(ActionListener e, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.addActionListener(e);
        return field;
    }

    /**
     * Returns a new border with the specified dimensions.
     * @param width the border width.
     * @param height the border height.
     * @return a Border.
     */
    public static Border newBorder(int width, int height) {
        return BorderFactory.createEmptyBorder(height, width,  height, width);
    }

    /**
     * Returns a vertical spacer of the specified dimensions.
     * @param dimension the dimensions.
     * @return a Component.
     */
    public static Component newVSpacer(Dimension dimension) {
        return Box.createRigidArea(dimension);
    }

    /**
     * Returns a panel with a border with the specified title centered and a vertical layout.
     * @param title the title string.
     * @return a JPanel.
     */
    static JPanel newTitledBorderPanel(String title) {
        JPanel panel = new JPanel();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        titledBorder.setTitleJustification(TitledBorder.CENTER);
        panel.setBorder(titledBorder);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Return a new panel with a centered JLabel containing a loading message with a spinner.
     * Used in the {@link AsyncPanel}.
     * @return a JPanel.
     */
    static JPanel newSpinner() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("loading... ", Images.loading, JLabel.CENTER));
        return panel;
    }
}
