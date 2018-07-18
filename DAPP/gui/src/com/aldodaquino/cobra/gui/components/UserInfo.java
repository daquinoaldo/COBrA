package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.constants.Colors;
import com.aldodaquino.cobra.gui.Status;

import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import java.awt.*;

/**
 * Login Form under the Logo in the Login Panel
 */
public class UserInfo extends UpgradablePanel {

    private final Status status;
    private String account = null;
    private JLabel premiumLabel;
    private final GridBagConstraints replacingPosition;

    public UserInfo(Status status) {
        this.status = status;

        // catalog label
        JLabel catalog = new JLabel("Catalog:");
        JLabel catalogAddressLabel = new JLabel(status.getCatalogManager() == null ? "not connected"
                : status.getCatalogManager().getAddress());

        // account label
        try {
            account = status.getUserAddress();
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
            Utils.showErrorDialog(e.getMessage());
            System.exit(1);
        }
        JLabel accountLabel = new JLabel("Account:");
        JLabel accountAddressLabel = new JLabel(account);

        // premium label
        premiumLabel = newPremiumLabel();
        replacingPosition = UpgradablePanel.newGBC(1, 6);

        // add to the panel
        add(catalog, UpgradablePanel.newGBC(1, 1));
        add(catalogAddressLabel, UpgradablePanel.newGBC(1, 2));
        add(ComponentFactory.newVSpacer(), UpgradablePanel.newGBC(1, 3));
        add(accountLabel, UpgradablePanel.newGBC(1, 4));
        add(accountAddressLabel, UpgradablePanel.newGBC(1, 5));
        if (premiumLabel != null) add(premiumLabel, replacingPosition);
    }

    public void update() {
        JLabel newPremiumLabel = newPremiumLabel();
        if (newPremiumLabel == null) return;
        replaceComponent(premiumLabel, newPremiumLabel, replacingPosition);
        premiumLabel = newPremiumLabel;
    }

    private JLabel newPremiumLabel() {
        if (status.getCatalogManager() == null) return null;
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
