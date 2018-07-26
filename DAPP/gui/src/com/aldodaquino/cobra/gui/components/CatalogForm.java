package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * A JPanel used in the {@link StarPanel}. Ask the user which catalog to connect to.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class CatalogForm extends JPanel {

    private final JTextField catalogAddressField;

    private final Consumer<String> connectCallback;

    /**
     * Constructor.
     * @param connectCallback a callback to be invoked when the deploy button is clicked or the form is submitted and
     *                        the catalog address is correct.
     * @param deployCallback a callback invoked when the deploy button is clicked.
     */
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
        connectPanel.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        connectPanel.add(catalogAddressLabel);
        connectPanel.add(catalogAddressField);
        connectPanel.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        connectPanel.add(connectButton);

        // titled border panel for catalogManager connection
        JPanel deployPanel = ComponentFactory.newTitledBorderPanel("New catalogManager");
        deployPanel.add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        deployPanel.add(deployButton);

        // add all to the panel
        add(connectPanel);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M));
        add(deployPanel);
    }

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
