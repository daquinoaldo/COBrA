package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.constants.Dimensions;

import javax.swing.*;

/**
 * Login Form under the Logo in the Login Panel
 */
public class RoleForm extends JPanel {

    /**
     * Constructor.
     * @param browseCallback callback for the "Browser contents" button.
     * @param manageCallback callback for the "Manage my contents" button.
     * @param deleteCallback callback for the "Delete catalogManager" contents button. If null the button is not shown.
     */
    public RoleForm(Status status, Runnable browseCallback, Runnable manageCallback, Runnable disconnectCallback,
                    Runnable deleteCallback) {

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Labels
        JLabel title = new JLabel("What do you want to do?");

        // Buttons
        JButton browseButton = ComponentFactory.newButton("Browse contents", e -> browseCallback.run());
        JButton manageButton = ComponentFactory.newButton("Manage my contents", e -> manageCallback.run());
        JButton disconnectButton = ComponentFactory.newButton("Disconnect from catalogManager",
                e -> disconnectCallback.run());

        // add all to the panel
        add(new UserInfo(status));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L));
        add(title);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        add(browseButton);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        add(manageButton);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M));
        add(disconnectButton);

        // only for catalogManager owner
        if (deleteCallback != null) {
            add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
            add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L));
            JButton deleteButton = ComponentFactory.newButton("Delete catalogManager", e -> deleteCallback.run());
            add(deleteButton);
        }
    }

}
