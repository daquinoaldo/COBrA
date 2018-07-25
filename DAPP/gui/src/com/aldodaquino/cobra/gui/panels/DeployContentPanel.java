package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.connections.API;
import com.aldodaquino.cobra.connections.FileExchange;
import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.connections.HttpHelper;

import javax.swing.*;
import java.io.File;
import java.math.BigInteger;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Login Panel
 */
class DeployContentPanel extends AsyncPanel {

    // fields
    private final JTextField addressField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JTextField genreField;
    private final JTextField priceField;

    private final Status status;
    private final Consumer<String> deployCallback;

    private File file;

    DeployContentPanel(Status status, Consumer<String> deployCallback) {
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
        JLabel selectFileLabel = new JLabel("Pick the content file:");

        // input field
        priceField = ComponentFactory.newTextField(e -> deploy());
        genreField = ComponentFactory.newTextField(e -> priceField.grabFocus());
        nameField = ComponentFactory.newTextField(e -> genreField.grabFocus());
        portField = ComponentFactory.newTextField(e -> nameField.grabFocus());
        portField.setText("8080");
        addressField = ComponentFactory.newTextField(e -> portField.grabFocus());
        addressField.setText("localhost");

        // buttons
        JButton selectFileButton = ComponentFactory.newButton("Open", e -> file = Utils.openFileDialog());
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
        add(selectFileLabel);
        add(selectFileButton);
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
            url = "http://" + url + ":" + port + API.DEPLOY_API_PATH;
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

        // open the socket for the file
        if (file == null) {
            Utils.newErrorDialog("You must choose a file to be uploaded.");
            return;
        }

        ServerSocketChannel serverSocketChannel = FileExchange.openFileSocket();
        if (serverSocketChannel == null) {
            Utils.newErrorDialog("Error while opening server socket.");
            return;
        }

        int port = serverSocketChannel.socket().getLocalPort();
        FileExchange.startFileSender(serverSocketChannel, file,
                () -> Utils.newMessageDialog("File uploaded successfully."));

        // make the request
        Map<String, String> parameters = new HashMap<>();
        parameters.put("privateKey", status.getPrivateKey());
        parameters.put("name", name);
        parameters.put("genre", genre);
        parameters.put("price", price.toString());
        parameters.put("port", Integer.toString(port));
        parameters.put("filename", file.getName());

        HttpHelper.Response response = HttpHelper.makePost(url, parameters);
        if (response.code != 200) {
            Utils.newErrorDialog("HTTP ERROR " + response.code + ": " + response.data);
            return;
        }

        // close the widow
        deployCallback.accept(response.data);
        window.dispose();
    }
}
