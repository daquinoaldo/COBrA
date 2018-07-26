package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.constants.Images;
import com.aldodaquino.cobra.gui.constants.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Application Logo for the {@link com.aldodaquino.cobra.gui.panels.StarterPanel}.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Logo extends JPanel {

    /**
     * Constructor.
     */
    public Logo() {
        // icon
        Image icon = Images.logo.getImage()
                .getScaledInstance(Dimensions.LOGO_SIZE, Dimensions.LOGO_SIZE, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(icon), JLabel.CENTER);

        // title
        JLabel title = new JLabel(Strings.appName);
        title.setForeground(Color.BLACK);
        title.setHorizontalAlignment(JLabel.CENTER);
        // find out how much the font can grow in width and calculate the corresponding font size
        Font labelFont = title.getFont();
        String labelText = title.getText();
        int stringWidth = title.getFontMetrics(labelFont).stringWidth(labelText);
        double widthRatio = (double) Dimensions.LOGO_SIZE / (double)stringWidth;
        int newFontSize = (int) (labelFont.getSize() * widthRatio);
        // set the new font size
        Utils.setFontSize(title, newFontSize);

        // put components in a container
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(iconLabel);
        container.add(title);

        // prepare this panel
        setAlignmentX(Component.CENTER_ALIGNMENT);
        add(container);
    }
}
