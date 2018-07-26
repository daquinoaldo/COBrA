package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Starter Panel.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class LoginForm extends JPanel {

    private final JTextField privateKeyInput;
    private final Consumer<String> loginCallback;

    /**
     * Constructor.
     * @param loginCallback a String Consumer called if the login button is clicked or enter is pressed and the private
     *                      key is valid.
     */
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
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        add(sendButton);
    }

    /**
     * Submit form action.
     * Called when the login button is clicked or enter key is pressed from the private key field.
     */
    private void login() {
        // get input data
        String privateKey = privateKeyInput.getText().trim();

        // check the length  of the inputs and validate the form
        if (privateKey.length() == 64) loginCallback.accept(privateKey);
        else Utils.newErrorDialog("Check the values entered in the fields.");
    }
}
