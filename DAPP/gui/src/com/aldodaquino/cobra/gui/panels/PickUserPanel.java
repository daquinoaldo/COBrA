package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.Utils;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Login Panel
 */
class PickUserPanel extends AsyncPanel {
    private final JTextField addressField;
    // the callback to call if the input data are correct
    private final Consumer<String> giftCallback;

    PickUserPanel(Consumer<String> giftCallback) {
        this.giftCallback = giftCallback;

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // label over the input fields
        JLabel addressLabel = new JLabel("User address:");

        // input field
        addressField = ComponentFactory.newTextField(e -> gift());

        // send button
        JButton sendButton = ComponentFactory.newButton("Deploy", e -> gift());

        // add all to the panel
        add(addressLabel);
        add(addressField);
        add(ComponentFactory.newVSpacer());
        add(sendButton);
    }

    /**
     * Submit form action. Called when the gift button is clicked or enter key is pressed from the private key field.
     */
    private void gift() {
        // get input data
        String address = addressField.getText().trim();

        // add "0x" to the address if not present
        if (address.length() == 40)
            address = "0x" + address;

        // check the length  of the inputs and validate the form
        if (address.length() == 42)
            giftCallback.accept(address);
        else Utils.showErrorDialog("You must specify an address.");

        // close the widow
        window.dispose();
    }
}
