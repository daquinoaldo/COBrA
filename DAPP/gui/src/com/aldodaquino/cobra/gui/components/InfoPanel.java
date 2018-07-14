package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;

import javax.swing.*;

public class InfoPanel extends AsyncPanel {

    protected LabelPanel latestLabel;
    protected LabelPanel mostPopularLabel;
    protected LabelPanel highestRatedLabel;
    protected LabelPanel mostEnjoyedLabel;
    protected LabelPanel biggestPriceFairnessLabel;
    protected LabelPanel highestContentMeaningLabel;

    public InfoPanel(String mainLabelString) {

        // set layout (vertical)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // prepare content
        JLabel mainLabel = new JLabel(mainLabelString);
        Utils.setFontSize(mainLabel, mainLabel.getFont().getSize() * 2);
        latestLabel = new LabelPanel("Latest release: ");
        mostPopularLabel = new LabelPanel("Most popular content: ");
        highestRatedLabel = new LabelPanel("Highest rated content: ");
        mostEnjoyedLabel = new LabelPanel("Most enjoyed content: ");
        biggestPriceFairnessLabel = new LabelPanel("Biggest value for money content: ");
        highestContentMeaningLabel = new LabelPanel("Highest rated for content meaning: ");

        // add all to the panel
        add(mainLabel, UpgradablePanel.newGBC(1, 1));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 2));
        add(latestLabel, UpgradablePanel.newGBC(1, 3));
        add(mostPopularLabel, UpgradablePanel.newGBC(1, 4));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_M), UpgradablePanel.newGBC(1, 5));
        add(highestRatedLabel, UpgradablePanel.newGBC(1, 6));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_S), UpgradablePanel.newGBC(1, 7));
        add(mostEnjoyedLabel, UpgradablePanel.newGBC(1, 8));
        add(biggestPriceFairnessLabel, UpgradablePanel.newGBC(1, 9));
        add(highestContentMeaningLabel, UpgradablePanel.newGBC(1, 10));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), UpgradablePanel.newGBC(1, 11));

    }

}
