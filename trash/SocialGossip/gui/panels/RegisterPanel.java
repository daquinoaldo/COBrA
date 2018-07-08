package gui.panels;

import connections.Json;
import gui.constants.Colors;
import gui.Utils;
import gui.components.Logo;
import gui.components.RegisterForm;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

class RegisterPanel extends JPanel {
    private static final Dimension padding = new Dimension(65, 35);
    private static final int maxWidth = 200;
    private final BiConsumer<String, String> onUserRegistered;
    
    RegisterPanel(BiConsumer<String, String> onUserRegistered) {
        this.onUserRegistered = onUserRegistered;
        
        // Init components
        JPanel logoPanel = new Logo(64);
        logoPanel.setPreferredSize(new Dimension(maxWidth, 100));
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel helpText = new JLabel("<html>" +
                "Pick an username (letters and numbers only), type a password twice and choose your language." +
                "</html>");
        helpText.setPreferredSize(new Dimension(maxWidth, 100));
        helpText.setForeground(Colors.accent);
        helpText.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    
        JPanel registerPanel = new RegisterForm(this::registerCallback);
        registerPanel.setPreferredSize(new Dimension(maxWidth, 200));
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Making the panel
        setBackground(Colors.background);
        setBorder(BorderFactory.createEmptyBorder(padding.height, padding.width, padding.height, padding.width));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        add(logoPanel);
        add(helpText);
        add(registerPanel);
    }
    
    private void registerCallback(String username, String password, String language) {
        Utils.enableComponents(this, false);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    
        // make register request to server
        boolean result = Json.register(username, password, language);
        
        // if successful run the callback
        if (result) {
            onUserRegistered.accept(username, password);
        }
        else {
            Utils.enableComponents(this, true);
            this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
