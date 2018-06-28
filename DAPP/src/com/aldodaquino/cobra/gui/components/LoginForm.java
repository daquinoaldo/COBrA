package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Login Form under the Logo in the Login Panel
 */
public class LoginForm extends JPanel {
    // private key field: on enter submit
    private JTextField privateKeyInput;
    // address field: on enter focus on privateKeyInput
    private JTextField addressInput;
    // the callback to call if the input data are correct
    private final BiConsumer<String, String> loginCallback;
    
    public LoginForm(BiConsumer<String, String> loginCallback) {
        this.loginCallback = loginCallback;

        // set layout (vertical)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // label over the input fields
        JLabel addressLabel = new JLabel("Address:");
        JLabel privateKeyLabel = new JLabel("Private key:");

        // private key field: on enter submit
        privateKeyInput = ComponentFactory.newPasswordField(e -> submit());
        // address field: on enter focus on privateKeyInput
        addressInput = ComponentFactory.newTextField(e -> privateKeyInput.grabFocus());

        // send button
        JButton sendButton = ComponentFactory.newButton("Login", e -> submit());

        // spacer, empty box to outdistance things
        Component spacer = Dimensions.V_SPACER;

        // add all to the panel
        this.add(addressLabel);
        this.add(addressInput);
        this.add(spacer);
        this.add(privateKeyLabel);
        this.add(privateKeyInput);
        this.add(spacer);
        this.add(sendButton);
    }

    /**
     * Submit form action. Called when the submit button is clicked or enter key is pressed from the private key field.
     */
    private void submit() {
        // get input data
        String address = addressInput.getText().trim();
        String privateKey = privateKeyInput.getText().trim();

        // remove "0x" from the address
        if (address.length() > 2 && address.substring(0, 1).equals("0x"))
            address = address.substring(2);

        // check the length  of the inputs and validate the form
        if (address.length() == 40 && privateKey.length() == 64)
            this.loginCallback.accept(address, privateKey);
        else Utils.showErrorDialog("Check the values entered in the fields.");
    }
}
