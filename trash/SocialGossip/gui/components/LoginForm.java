package gui.components;

import gui.constants.Colors;
import gui.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.function.*;

/**
 * Login Form under the Logo in the Login Panel
 */
public class LoginForm extends JPanel {
    private final JTextField usernameInput = InputFactory.getTextInput("username", e -> submit());
    private final JTextField passwordInput = InputFactory.getPasswordInput("password", e -> submit());
    private final BiConsumer<String, String> loginCallback;
    
    public LoginForm(BiConsumer<String, String> loginCallback, Runnable registerCallback) {
        this.loginCallback = loginCallback;
        
        this.setBackground(Colors.background);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
        JButton sendBtn = InputFactory.getMainButton("Login", e -> submit());
        JButton registerBtn = InputFactory.getSecondaryButton("Register", e -> registerCallback.run());
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(Colors.background);
        buttonsPanel.add(sendBtn);
        buttonsPanel.add(registerBtn);
    
        this.add(usernameInput);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(passwordInput);
        this.add(Box.createRigidArea(new Dimension(0, 15)));
        this.add(buttonsPanel);
    }

    /**
     * Submit form action
     */
    private void submit() {
        // Submit button or Enter key pressed
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText(); // deprecated, use Char[] passwordInput.getPassword() in production
        
        if (Utils.isValidUsername(username) && password.length() > 0)
            this.loginCallback.accept(username, password);
        else {
            Utils.showErrorDialog("Check the values entered in the fields.");
        }
    }
}
