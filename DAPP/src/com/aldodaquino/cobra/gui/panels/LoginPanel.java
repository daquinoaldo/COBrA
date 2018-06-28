package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.LoginForm;
import com.aldodaquino.cobra.gui.components.Logo;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private static final Dimension padding = new Dimension(65, 35);

    public LoginPanel() {
        // Init components
        JPanel logoPanel = new Logo(); // icon and title
        JPanel loginFormPanel = new LoginForm(this::loginCallback);
        
        // Making the panel
        setBorder(ComponentFactory.newBorder(padding.width, padding.height));
        setLayout(new GridLayout(2,1,3,50));
        add(logoPanel);
        add(loginFormPanel);
    }
    
    private void loginCallback(String username, String password) {
        // TODO
    }
}
