package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.StarPanel;
import com.aldodaquino.cobra.gui.components.UpgradablePanel;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.aldodaquino.cobra.gui.components.UpgradablePanel.newGBC;

public class ContentInfoPanel extends AsyncPanel {

    private static final String WINDOW_TITLE = "About the author";

    private final CatalogManager catalogManager;
    private final Content content;

    private ContentInfoPanel(CatalogManager catalogManager, String address) {
        this.catalogManager = catalogManager;
        content = catalogManager.getContentInfo(address);

        // prepare content
        JLabel mainLabel = new JLabel(content.name);
        Utils.setFontSize(mainLabel, mainLabel.getFont().getSize() * 2);
        JLabel addressLabel = new JLabel("Address: " + content.address);
        JPanel authorLabel = prepareLink("Author: ", content.author,
                new AuthorInfoPanel(catalogManager, content.author), AuthorInfoPanel.WINDOW_TITLE);
        JPanel genreLabel = prepareLink("Genre: ", content.genre,
                new GenreInfoPanel(catalogManager, content.genre), GenreInfoPanel.WINDOW_TITLE);
        JLabel priceLabel = new JLabel("price: " + content.price);
        JLabel viewsLabel = new JLabel("Views: " + content.views);
        JPanel averageRatingLabel = prepareStar("Average rating: ", content.averageRating);
        JPanel enjoyLabel = prepareStar("Enjoy: ", content.enjoy);
        JPanel priceFairnessLabel = prepareStar("Value for money: ", content.priceFairness);
        JPanel contentMeaningLabel = prepareStar("Content meaning: ", content.contentMeaning);
        JButton viewButton = ComponentFactory.newButton("View", e -> view());
        JButton giftButton = ComponentFactory.newButton("Gift this content", e -> gift());

        // add all to the panel
        setLayout(new GridBagLayout());
        add(mainLabel, newGBC(1, 1));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 2));
        add(addressLabel, newGBC(1, 3));
        add(authorLabel, newGBC(1, 4));
        add(genreLabel, newGBC(1, 5));
        add(priceLabel, newGBC(1, 6));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 7));
        add(viewsLabel, newGBC(1, 8));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 9));
        add(averageRatingLabel, newGBC(1, 10));
        add(enjoyLabel, newGBC(1, 11));
        add(priceFairnessLabel, newGBC(1, 12));
        add(contentMeaningLabel, newGBC(1, 13));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 14));
        add(viewButton, newGBC(1, 15));
        add(giftButton, newGBC(1, 16));

    }

    /**
     * Open a new window with this panel.
     * @param catalogManager the catalog manager in Status.
     * @param address of the content.
     */
    public static void newWindow(CatalogManager catalogManager, String address) {
        Utils.newWindow(WINDOW_TITLE, new ContentInfoPanel(catalogManager, address), false);
    }

    private void view() {
        doAsync(() -> {
            // Check if the user has access and ask for buy if he hasn't
            if (!catalogManager.isPremium() && !catalogManager.hasAccess(content.address)) {
                if(!Utils.newConfirmDialog("You don't have access to this content. Do you want to buy it for "
                        + content.price + "?")) return; // doesn't have access and doesn't want to buy the access
                if (catalogManager.buyContent(content.address, content.price)) Utils.newMessageDialog("Content bought.");
                else {
                    Utils.newErrorDialog("Cannot buy this content. You may have bought it previously.");
                    return;
                }
            }
            //TODO: view consume content
        });
    }

    private void gift() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
                doAsync(() -> {
                    if (catalogManager.giftContent(content.address, user, content.price)) Utils.newMessageDialog("Content gifted.");
                    else Utils.newErrorDialog("Cannot gift this content. The user may have already bought it.");
                }));
        Utils.newWindow("Gift content", pickUserPanel, false);
    }

    private JPanel prepareLink(String label, String value, JPanel onClickPanel, String windowTitle) {
        JLabel link = new JLabel("<html><a href=\"about:" + value + "\">" + value + "</a>");
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Utils.newWindow(windowTitle, onClickPanel, false);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(label), UpgradablePanel.newGBC(1, 1));
        panel.add(link, UpgradablePanel.newGBC(2, 1));
        return panel;
    }

    private JPanel prepareStar(String label, int rating) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(label), UpgradablePanel.newGBC(1, 1));
        panel.add(new StarPanel(rating), UpgradablePanel.newGBC(2, 1));
        return panel;
    }

}
