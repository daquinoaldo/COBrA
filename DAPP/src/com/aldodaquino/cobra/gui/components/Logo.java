package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Colors;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.constants.Images;
import com.aldodaquino.cobra.gui.constants.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Application Logo for the Login and Registration Panels
 */
public class Logo extends JPanel {

    public Logo() {
        this(Dimensions.ICON_SIZE);
    }
    
    private Logo(int iconSize) {
        // icon
        Image icon = Images.logo.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(icon), JLabel.CENTER);

        // title
        JLabel title = new JLabel(Strings.appName);
        title.setForeground(Colors.logo);
        title.setFont(new Font("Arial", Font.PLAIN, iconSize/5));
        title.setHorizontalAlignment(JLabel.CENTER);

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
