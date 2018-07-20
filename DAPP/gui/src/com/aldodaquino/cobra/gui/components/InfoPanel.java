package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;

import javax.swing.*;
import java.awt.*;

import static com.aldodaquino.cobra.gui.constants.Dimensions.INFO_PANEL_PADDING;

public class InfoPanel extends JPanel {

    protected final LabelPanel latestLabel;
    protected final LabelPanel mostPopularLabel;
    protected final LabelPanel highestRatedLabel;
    protected final LabelPanel mostEnjoyedLabel;
    protected final LabelPanel biggestPriceFairnessLabel;
    protected final LabelPanel highestContentMeaningLabel;

    protected InfoPanel(Status status, String mainLabelString) {

        // set layout and border
        setLayout(new GridBagLayout());
        setBorder(ComponentFactory.newBorder(INFO_PANEL_PADDING.width, INFO_PANEL_PADDING.height));

        // prepare content
        JLabel mainLabel = new JLabel(mainLabelString);
        Utils.setFontSize(mainLabel, mainLabel.getFont().getSize() * 2);
        latestLabel = new LabelPanel(status, "Latest release: ");
        mostPopularLabel = new LabelPanel(status, "Most popular content: ");
        highestRatedLabel = new LabelPanel(status, "Highest rated content: ");
        mostEnjoyedLabel = new LabelPanel(status, "Most enjoyed content: ");
        biggestPriceFairnessLabel = new LabelPanel(status, "Biggest value for money content: ");
        highestContentMeaningLabel = new LabelPanel(status, "Highest rated for content meaning: ");

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

    }

}
