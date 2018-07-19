package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;

import com.aldodaquino.cobra.gui.HttpHelper;

import javax.swing.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Form under the Logo in the Login Panel
 */
class DeployContentPanel extends AsyncPanel {

    private static final String DEPLOY_API_PATH = "/deploy";

    // fields
    private final JTextField addressField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JTextField genreField;
    private final JTextField priceField;

    private final Status status;
    private final Runnable deployCallback;


    DeployContentPanel(Status status, Runnable deployCallback) {
        this.status = status;
        this.deployCallback = deployCallback;

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // label over the input fields
        JLabel introLabel = new JLabel("The deployed content will be placed in your author server. Specify the " +
                "address and the port (default 8080) of a server running an author server instance. The author " +
                "server must always be online so that the content is accessible.");
        JLabel addressLabel = new JLabel("Address (IP or domain:");
        JLabel portLabel = new JLabel("Port:");
        JLabel nameLabel = new JLabel("Name:");
        JLabel genreLabel = new JLabel("Genre:");
        JLabel priceLabel = new JLabel("Price:");

        // input field
        priceField = ComponentFactory.newTextField(e -> deploy());
        genreField = ComponentFactory.newTextField(e -> priceField.grabFocus());
        nameField = ComponentFactory.newTextField(e -> genreField.grabFocus());
        portField = ComponentFactory.newTextField(e -> nameField.grabFocus());
        portField.setText("8080");
        addressField = ComponentFactory.newTextField(e -> portField.grabFocus());
        addressField.setText("localhost");

        // send button
        JButton sendButton = ComponentFactory.newButton("Deploy", e -> deploy());

        // add all to the panel
        add(introLabel);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L));
        add(addressLabel);
        add(addressField);
        add(ComponentFactory.newVSpacer());
        add(portLabel);
        add(portField);
        add(ComponentFactory.newVSpacer());
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
        String url = addressField.getText().trim();
        if (url.length() == 0) {
            Utils.newErrorDialog("You must specify an url.");
            return;
        }
        try {
            String portS = portField.getText().trim();
            int port = portS.equals("") ? 8080 : Integer.parseInt(portS);
            if (port <= 0) throw new NumberFormatException();
            url += ":" + port + DEPLOY_API_PATH;
        } catch (NumberFormatException e) {
            Utils.newErrorDialog("Invalid port number.");
            return;
        }

        String name = nameField.getText().trim();
        if (name.length() == 0) {
            Utils.newErrorDialog("You must specify a name.");
            return;
        }
        String genre = genreField.getText().trim();
        String priceS = priceField.getText().trim();
        BigInteger price;
        try {
            price = new BigInteger(priceS.length() != 0 ? priceS : "0");
        } catch (NumberFormatException e) {
            Utils.newErrorDialog("Invalid port number.");
            return;
        }

        // make the request
        Map<String, String> parameters = new HashMap<>();
        parameters.put("privateKey", status.getPrivateKey());
        parameters.put("name", name);
        parameters.put("genre", genre);
        parameters.put("price", price.toString());

        HttpHelper.Response response = HttpHelper.makePost(url, parameters);
        if (response.code != 200) Utils.newErrorDialog("HTTP ERROR " + response.code + ": " + response.data);

        // close the widow
        deployCallback.run();
        window.dispose();
    }
}
