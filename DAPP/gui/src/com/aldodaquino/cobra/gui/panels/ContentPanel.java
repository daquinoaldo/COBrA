package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.UpgradablePanel;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends AsyncPanel {

    private final CatalogManager catalogManager;
    private final String address;

    public ContentPanel(CatalogManager catalogManager, String address) {
        this.catalogManager = catalogManager;
        this.address = address;

        // prepare content
        Content content = catalogManager.getContentInfo(address);
        JLabel mainLabel = new JLabel(content.name);
        Utils.setFontSize(mainLabel, mainLabel.getFont().getSize() * 2);
        JLabel addressLabel = new JLabel("Address: " + content.address);
        JLabel authorLabel = new JLabel("Author: " + content.author);
        JLabel genreLabel = new JLabel("Genre: " + content.genre);
        JLabel priceLabel = new JLabel("price: " + content.price);
        JLabel viewsLabel = new JLabel("Views: " + content.views);
        JLabel averageRatingLabel = new JLabel("Average rating: " + content.averageRating);
        JLabel enjoyLabel = new JLabel("Enjoy: " + content.enjoy);
        JLabel priceFairnessLabel = new JLabel("Value for money: " + content.priceFairness);
        JLabel contentMeaningLabel = new JLabel("Content meaning: " + content.contentMeaning);
        JButton viewButton = ComponentFactory.newButton("View", e -> buySelected());
        JButton giftButton = ComponentFactory.newButton("Gift this content", e -> giftSelected());

        // add all to the panel
        setLayout(new GridBagLayout());
        add(mainLabel, UpgradablePanel.newGBC(1, 1));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 2));
        add(addressLabel, UpgradablePanel.newGBC(1, 3));
        add(authorLabel, UpgradablePanel.newGBC(1, 4));
        add(genreLabel, UpgradablePanel.newGBC(1, 5));
        add(priceLabel, UpgradablePanel.newGBC(1, 6));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 7));
        add(viewsLabel, UpgradablePanel.newGBC(1, 8));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 9));
        add(averageRatingLabel, UpgradablePanel.newGBC(1, 10));
        add(enjoyLabel, UpgradablePanel.newGBC(1, 11));
        add(priceFairnessLabel, UpgradablePanel.newGBC(1, 12));
        add(contentMeaningLabel, UpgradablePanel.newGBC(1, 13));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 14));
        add(viewButton, UpgradablePanel.newGBC(1, 15));
        add(giftButton, UpgradablePanel.newGBC(1, 16));

    }

    private void buySelected() {
        doAsync(() -> {
            if (catalogManager.buyContent(address)) Utils.showMessageDialog("Content bought.");
            else Utils.showErrorDialog("Cannot buy this content. You may have bought it previously.");
        });
    }

    private void giftSelected() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
                doAsync(() -> {
                    if (catalogManager.giftContent(address, user)) Utils.showMessageDialog("Content gifted.");
                    else Utils.showErrorDialog("Cannot gift this content. The user may have already bought it.");
                }));
        Utils.createWindow("Gift content", pickUserPanel, false);
    }

}
