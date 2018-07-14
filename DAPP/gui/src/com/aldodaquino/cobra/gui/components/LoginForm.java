package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Login Panel
 */
public class LoginForm extends JPanel {
    // private key field: on enter login
    private final JTextField privateKeyInput;
    // the callback to call if the input data are correct
    private final Consumer<String> loginCallback;
    
    public LoginForm(Consumer<String> loginCallback) {
        this.loginCallback = loginCallback;

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // label over the input fields
        JLabel privateKeyLabel = new JLabel("Private key:");

        // private key field: on enter login
        privateKeyInput = ComponentFactory.newPasswordField(e -> login());

        // send button
        JButton sendButton = ComponentFactory.newButton("Login", e -> login());

        // add all to the panel
        add(privateKeyLabel);
        add(privateKeyInput);
        add(ComponentFactory.newVSpacer());
        add(sendButton);
    }

    /**
     * Submit form action. Called when the login button is clicked or enter key is pressed from the private key field.
     */
    private void login() {
        // get input data
        String privateKey = privateKeyInput.getText().trim();

        // check the length  of the inputs and validate the form
        if (privateKey.length() == 64) loginCallback.accept(privateKey);
        else Utils.showErrorDialog("Check the values entered in the fields.");
    }
}
