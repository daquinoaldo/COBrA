package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Colors;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;
import java.awt.*;

/**
 * Login Form under the Logo in the Login Panel
 */
public class UserInfo extends UpgradablePanel {

    private final Status status;
    private String account;
    private JLabel premiumLabel;
    private GridBagConstraints replacingPosition;

    public UserInfo(Status status) {
        this.status = status;

        // account label
        account = status.getUserAddress();
        JLabel accountLabel = new JLabel(account);

        // premium label
        premiumLabel = newPremiumLabel();
        replacingPosition = UpgradablePanel.newGBC(1, 2);

        // update status button
        JButton updateButton = ComponentFactory.newButton("Update status", e -> update());

        // add to the panel
        add(accountLabel, UpgradablePanel.newGBC(1, 1));
        add(premiumLabel, replacingPosition);
        add(updateButton, UpgradablePanel.newGBC(1, 3));
    }

    public void update() {
        JLabel newPremiumLabel = newPremiumLabel();
        replaceComponent(premiumLabel, newPremiumLabel, replacingPosition);
        premiumLabel = newPremiumLabel;
    }

    private JLabel newPremiumLabel() {
        Boolean isPremium = status.getCatalogManager().isPremium(account);
        JLabel newPremiumLabel;
        if (isPremium) {
            newPremiumLabel = new JLabel("Premium user");
            newPremiumLabel.setForeground(Colors.GREEN);
        } else {
            newPremiumLabel = new JLabel("Not Premium user");
            newPremiumLabel.setForeground(Colors.RED);
        }
        return newPremiumLabel;
    }

}
