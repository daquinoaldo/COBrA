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

    private JLabel catalogAddressLabel;
    private final GridBagConstraints catalogAddressPosition;

    private JLabel accountAddressLabel;
    private final GridBagConstraints accountAddressPosition;

    private JLabel premiumLabel;
    private final GridBagConstraints premiumPosition;

    public UserInfo(Status status) {
        this.status = status;

        // catalog label
        JLabel catalog = new JLabel("Catalog:");
        catalogAddressLabel = newCatalogAddressLabel();
        catalogAddressPosition = UpgradablePanel.newGBC(1, 2);

        // account label
        JLabel accountLabel = new JLabel("Account:");
        accountAddressLabel = newAccountAddressLabel();
        accountAddressPosition = UpgradablePanel.newGBC(1, 5);

        // premium label
        premiumLabel = newPremiumLabel();
        premiumPosition = UpgradablePanel.newGBC(1, 6);

        // add to the panel
        add(catalog, UpgradablePanel.newGBC(1, 1));
        add(catalogAddressLabel, catalogAddressPosition);
        add(ComponentFactory.newVSpacer(), UpgradablePanel.newGBC(1, 3));
        add(accountLabel, UpgradablePanel.newGBC(1, 4));
        add(accountAddressLabel, accountAddressPosition);
        add(premiumLabel, premiumPosition);
    }

    public void updateStatus() {
        JLabel newCatalogAddressLabel = newCatalogAddressLabel();
        replaceComponent(catalogAddressLabel, newCatalogAddressLabel, catalogAddressPosition);
        catalogAddressLabel = newCatalogAddressLabel;

        JLabel newAccountAddressLabel = newAccountAddressLabel();
        replaceComponent(accountAddressLabel, newAccountAddressLabel, accountAddressPosition);
        accountAddressLabel = newAccountAddressLabel;

        JLabel newPremiumLabel = newPremiumLabel();
        replaceComponent(premiumLabel, newPremiumLabel, premiumPosition);
        premiumLabel = newPremiumLabel;
    }

    private JLabel newAccountAddressLabel() {
        String account;
        try {
            account = status.getUserAddress();
        } catch (OperationNotSupportedException e) {
            // not logged in
            account = "not logged in";
        }
        return new JLabel(account);
    }

    private JLabel newCatalogAddressLabel() {
        return new JLabel(status.getCatalogManager() == null ? "not connected"
                : status.getCatalogManager().getAddress());
    }

    private JLabel newPremiumLabel() {
        if (status.getCatalogManager() == null) return new JLabel();    // catalog not connected
        Boolean isPremium;
        try {
            isPremium = status.getCatalogManager().isPremium(status.getUserAddress());
        } catch (OperationNotSupportedException e) {
            // not logged in
            return new JLabel();
        }
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
