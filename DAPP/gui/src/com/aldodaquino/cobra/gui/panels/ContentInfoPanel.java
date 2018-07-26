package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.connections.API;
import com.aldodaquino.cobra.connections.CobraHttpHelper;
import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.StarPanel;
import com.aldodaquino.cobra.gui.components.UpgradablePanel;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;
import com.aldodaquino.cobra.main.ContentManager;
import com.aldodaquino.javautils.FileExchange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.aldodaquino.cobra.gui.components.UpgradablePanel.newGBC;
import static com.aldodaquino.cobra.gui.constants.Dimensions.INFO_PANEL_PADDING;

public class ContentInfoPanel extends AsyncPanel {

    private static final String WINDOW_TITLE = "About the author";

    private final Status status;
    private final CatalogManager catalogManager;
    private final Content content;
    private final ContentManager contentManager;

    private ContentInfoPanel(Status status, String address) {
        this.status = status;
        catalogManager = status.getCatalogManager();
        content = catalogManager.getContentInfo(address);
        contentManager = new ContentManager(status.credentials, address);

        // prepare content
        JLabel mainLabel = new JLabel(content.name);
        Utils.setFontSize(mainLabel, mainLabel.getFont().getSize() * 2);
        JLabel addressLabel = new JLabel("Address: " + content.address);
        JPanel authorLabel = prepareLink("Author: ", content.author,
                new AuthorInfoPanel(status, content.author), AuthorInfoPanel.WINDOW_TITLE);
        JPanel genreLabel = prepareLink("Genre: ", content.genre,
                new GenreInfoPanel(status, content.genre), GenreInfoPanel.WINDOW_TITLE);
        JLabel priceLabel = new JLabel("price: " + content.price);
        JLabel viewsLabel = new JLabel("Views: " + content.views);
        JPanel averageRatingLabel = prepareStar("Average rating: ", content.averageRating);
        JPanel enjoyLabel = prepareStar("Enjoy: ", content.enjoy);
        JPanel priceFairnessLabel = prepareStar("Value for money: ", content.priceFairness);
        JPanel contentMeaningLabel = prepareStar("Content meaning: ", content.contentMeaning);
        JButton viewButton = ComponentFactory.newButton("View", e -> view());
        JButton giftButton = ComponentFactory.newButton("Gift this content", e -> gift());

        // prepare the panel
        setLayout(new GridBagLayout());
        setBorder(ComponentFactory.newBorder(INFO_PANEL_PADDING.width, INFO_PANEL_PADDING.height));
        add(mainLabel, newGBC(1, 1));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 2));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 4));
        add(addressLabel, newGBC(1, 5));
        add(authorLabel, newGBC(1, 6));
        add(genreLabel, newGBC(1, 7));
        add(priceLabel, newGBC(1, 8));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 9));
        add(viewsLabel, newGBC(1, 10));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 11));
        add(averageRatingLabel, newGBC(1, 12));
        add(enjoyLabel, newGBC(1, 13));
        add(priceFairnessLabel, newGBC(1, 14));
        add(contentMeaningLabel, newGBC(1, 15));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), newGBC(1, 16));
        add(viewButton, newGBC(1, 17));
        add(giftButton, newGBC(1, 18));

    }

    /**
     * Open a new window with this panel.
     * @param status the Status.
     * @param address of the content.
     */
    public static void newWindow(Status status, String address) {
        Utils.newWindow(WINDOW_TITLE, new ContentInfoPanel(status, address), false);
    }

    private void view() {
        doAsync(() -> {
            // Check if the user has access and ask for buy if he hasn't
            if (!catalogManager.isPremium() && !catalogManager.hasAccess(content.address)) {
                if(!Utils.newConfirmDialog("You don't have access to this content. Do you want to buy it for "
                        + content.price + "?")) return; // doesn't have access and doesn't want to buy the access
                if (catalogManager.buyContent(content.address, content.price))
                    catalogManager.listenAccessGranted((addr, name) -> {
                        Utils.newMessageDialog("Content bought.");
                        retrieveContent();
                    });
                else Utils.newErrorDialog("Cannot buy this content. You may have bought it previously.");
            } else retrieveContent();
        });
    }

    private void retrieveContent() {
        // make the request
        Map<String, String> parameters = new HashMap<>();
        parameters.put("privateKey", status.getPrivateKey());
        parameters.put("address", content.address);

        String hostname = contentManager.getHostname();
        int port = contentManager.getPort();
        if (port == 0) {
            Utils.newErrorDialog("The content has an invalid port number. Cannot contact the author's server.");
            return;
        }
        String url = "http://" + hostname + ":" + port + API.ACCESS_API_PATH;

        // get the response and retrieve the socket port number
        CobraHttpHelper.Response response = CobraHttpHelper.makePost(url, parameters);
        if (response.code != 200) Utils.newErrorDialog("HTTP ERROR " + response.code + ": " + response.data);
        Map<String, String> map = CobraHttpHelper.parseJson(response.data);
        int socketPort = Integer.parseInt(map.get("port"));
        String filename = map.get("filename");

        // download the file
        File file = Utils.saveFileDialog(filename);
        FileExchange.receiveFile(file, hostname, socketPort);
    }

    private void gift() {
        JPanel pickUserPanel = new PickUserPanel((String user) ->
                doAsync(() -> {
                    if (catalogManager.giftContent(content.address, user, content.price))
                        catalogManager.listenAccessGranted(user, (address, name) ->
                                Utils.newMessageDialog("Content " + name + " gifted to " + user + "."));
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
