package gui.panels;

import connections.Json;
import gui.constants.Colors;
import gui.Utils;
import gui.components.LoginForm;
import gui.components.Logo;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private static final Dimension padding = new Dimension(65, 35);
    private final JPanel loginFormPanel;
    private JFrame registerWindow = null;
    
    public LoginPanel() {
        // Init components
        JPanel logoPanel = new Logo(); // icon and title
        loginFormPanel = new LoginForm(this::loginCallback, this::registerCallback); // form, submit and register buttons
        
        // Making the panel
        setBackground(Colors.background);
        setBorder(BorderFactory.createEmptyBorder(padding.height, padding.width,padding.height, padding.width));
        setLayout(new GridLayout(2,1,3,50));
        add(logoPanel);
        add(loginFormPanel);
    }
    
    private void loginCallback(String username, String password) {
        Json.login(username, password);
        
        if (this.registerWindow != null) {
            // if this method was called after a registration, close the registration form window
            this.registerWindow.dispose();
        }
    }
    
    private void registerCallback() {
        // create a register window and set a callback for a successful registration
        JPanel registerPanel = new RegisterPanel(this::loginCallback);
        this.registerWindow = Utils.createFixedWindow("Register", registerPanel, false, false);
        
        // set listener for window closing (without registration)
        JPanel self = this;
        registerWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // re-enable form elements
                Utils.enableComponents(loginFormPanel, true);
                self.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        // disable form elements
        Utils.enableComponents(loginFormPanel, false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
}
