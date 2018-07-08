package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.javautils.TriConsumer;

import javax.swing.*;
import java.math.BigInteger;

/**
 * Login Form under the Logo in the Login Panel
 */
class DeployContentPanel extends AsyncPanel {
    // fields
    private final JTextField nameField;
    private final JTextField genreField;
    private final JTextField priceField;
    // the callback to call if the input data are correct
    private final TriConsumer<String, String, BigInteger> deployCallback;

    DeployContentPanel(TriConsumer<String, String, BigInteger> deployCallback) {
        this.deployCallback = deployCallback;

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // label over the input fields
        JLabel nameLabel = new JLabel("Name:");
        JLabel genreLabel = new JLabel("Genre:");
        JLabel priceLabel = new JLabel("Price:");

        // input field
        priceField = ComponentFactory.newTextField(e -> deploy());
        genreField = ComponentFactory.newTextField(e -> priceField.grabFocus());
        nameField = ComponentFactory.newTextField(e -> genreField.grabFocus());

        // send button
        JButton sendButton = ComponentFactory.newButton("Deploy", e -> deploy());

        // add all to the panel
        add(nameLabel);
        add(nameField);
        add(ComponentFactory.newVSpacer());
        add(genreLabel);
        add(genreField);
        add(ComponentFactory.newVSpacer());
        add(priceLabel);
        add(priceField);
        add(ComponentFactory.newVSpacer());
        add(sendButton);
    }

    /**
     * Submit form action. Called when the deploy button is clicked or enter key is pressed from the private key field.
     */
    private void deploy() {
        // get input data
        String name = nameField.getText().trim();
        String genre = genreField.getText().trim();
        String priceS = priceField.getText().trim();
        BigInteger price = new BigInteger(priceS.length() != 0 ? priceS : "0");

        // check the length  of the inputs and validate the form
        if (name.length() != 0) deployCallback.accept(name, genre, price);
        else Utils.showErrorDialog("You must specify a name.");

        // close the widow
        window.dispose();
    }
}
