package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Login Panel
 */
public class CatalogForm extends JPanel {
    // catalog address field
    private JTextField catalogAddressField;
    // the callback to call if the input data are correct
    private final Consumer<String> connectCallback;

    public CatalogForm(Consumer<String> connectCallback, Runnable deployCallback) {
        this.connectCallback = connectCallback;

        // set layout (vertical)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Labels
        JLabel catalogAddressLabel = new JLabel("Catalog address:");
        JLabel orLabel = new JLabel("----- OR -----");

        // catalog address field: on enter connect
        catalogAddressField = ComponentFactory.newTextField(e -> connect());

        // Buttons
        JButton connectButton = ComponentFactory.newButton("Connect", e -> connect());
        JButton deployButton = ComponentFactory.newButton("Deploy", e -> deployCallback.run());

        // titled border panel for catalog connection
        JPanel connectPanel = ComponentFactory.newTitledBorderPanel("Existent catalog");
        connectPanel.add(ComponentFactory.newVSpacer());
        connectPanel.add(catalogAddressLabel);
        connectPanel.add(catalogAddressField);
        connectPanel.add(ComponentFactory.newVSpacer());
        connectPanel.add(connectButton);

        // titled border panel for catalog connection
        JPanel deployPanel = ComponentFactory.newTitledBorderPanel("New catalog");
        deployPanel.add(ComponentFactory.newVSpacer());
        deployPanel.add(deployButton);

        // add all to the panel
        this.add(connectPanel);
        this.add(ComponentFactory.newVSpacer(Dimensions.M_V_SPACER));
        this.add(deployPanel);
    }

    /**
     * Submit form action. Called when the connect button is clicked or enter key is pressed from the address field.
     */
    private void connect() {
        // get input data
        String address = catalogAddressField.getText().trim();

        // remove "0x" from the address
        if (address.length() > 2 && address.substring(0, 1).equals("0x"))
            address = address.substring(2);

        // check the length  of the inputs and validate the form
        if (address.length() == 40)
            connectCallback.accept(address);
        else Utils.showErrorDialog("Check the values entered in the fields.");
    }

}
