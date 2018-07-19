package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Login Form under the Logo in the Login Panel
 */
public class CatalogForm extends JPanel {
    // catalogManager address field
    private final JTextField catalogAddressField;
    // the callback to call if the input data are correct
    private final Consumer<String> connectCallback;

    public CatalogForm(Consumer<String> connectCallback, Runnable deployCallback) {
        this.connectCallback = connectCallback;

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Labels
        JLabel catalogAddressLabel = new JLabel("Catalog address:");

        // catalogManager address field: on enter connect
        catalogAddressField = ComponentFactory.newTextField(e -> connect());

        // Buttons
        JButton connectButton = ComponentFactory.newButton("Connect", e -> connect());
        JButton deployButton = ComponentFactory.newButton("Deploy", e -> deployCallback.run());

        // titled border panel for catalogManager connection
        JPanel connectPanel = ComponentFactory.newTitledBorderPanel("Existent catalogManager");
        connectPanel.add(ComponentFactory.newVSpacer());
        connectPanel.add(catalogAddressLabel);
        connectPanel.add(catalogAddressField);
        connectPanel.add(ComponentFactory.newVSpacer());
        connectPanel.add(connectButton);

        // titled border panel for catalogManager connection
        JPanel deployPanel = ComponentFactory.newTitledBorderPanel("New catalogManager");
        deployPanel.add(ComponentFactory.newVSpacer());
        deployPanel.add(deployButton);

        // add all to the panel
        add(connectPanel);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M));
        add(deployPanel);
    }

    /**
     * Submit form action. Called when the connect button is clicked or enter key is pressed from the address field.
     */
    private void connect() {
        // get input data
        String address = catalogAddressField.getText().trim();

        // add "0x" to the address if not present
        if (address.length() == 40)
            address = "0x" + address;

        // check the length  of the inputs and validate the form
        if (address.length() == 42)
            connectCallback.accept(address);
        else Utils.newErrorDialog("Check the values entered in the fields.");
    }

}
