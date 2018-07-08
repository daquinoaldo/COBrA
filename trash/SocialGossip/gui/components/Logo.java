package gui.components;

import gui.constants.Colors;
import gui.constants.Icons;

import javax.swing.*;
import java.awt.*;

/**
 * Application Logo for the Login and Registration Panels
 */
public class Logo extends JPanel {
    public Logo() { this(128); }
    
    public Logo(int iconSize) {
        this.setOpaque(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Colors.background);
    
        Image icon = Icons.logo.getImage().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(icon), JLabel.CENTER);
        iconLabel.setMaximumSize(new Dimension(200, iconSize));
    
        JLabel title = new JLabel("Social Gossip");
        title.setMaximumSize(new Dimension(200, iconSize));
        title.setForeground(Colors.accent);
        title.setFont(new Font("Arial", Font.PLAIN, iconSize/5));
        title.setHorizontalAlignment(JLabel.CENTER);
    
        this.add(iconLabel);
        this.add(title);
    }
}
