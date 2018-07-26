package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.AsyncPanel;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.StarPanel;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.CatalogManager;

import javax.swing.*;

/**
 * A panel to ask the user to vote a content consumed recently.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
class VotingPanel extends AsyncPanel {

    /**
     * Constructor.
     * @param contentName the content name.
     * @param contentAddress the content address.
     * @param catalogManager the CatalogManager loaded or deployed by the user.
     */
    VotingPanel(String contentName, String contentAddress, CatalogManager catalogManager) {

        // prepare components
        JLabel infoLabel = new JLabel("You can now rate for the content " + contentName + ".");
        JLabel enjoyLabel = new JLabel("Enjoy:");
        StarPanel enjoyStars = new StarPanel();
        JLabel valueForMoneyLabel = new JLabel("Value for money:");
        StarPanel valueForMoneyStars = new StarPanel();
        JLabel contentMeaningLabel = new JLabel("Content meaning:");
        StarPanel contentMeaningStars = new StarPanel();
        JButton voteButton = ComponentFactory.newButton("Vote", e -> {
            catalogManager.vote(contentAddress, enjoyStars.getRating(), valueForMoneyStars.getRating(),
                    contentMeaningStars.getRating());
            window.dispose();
        });

        // prepare the panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(infoLabel);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L));
        add(enjoyLabel);
        add(enjoyStars);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        add(valueForMoneyLabel);
        add(valueForMoneyStars);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S));
        add(contentMeaningLabel);
        add(contentMeaningStars);
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M));
        add(voteButton);
    }
}
