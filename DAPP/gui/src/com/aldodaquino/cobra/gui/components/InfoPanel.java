package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;

import javax.swing.*;
import java.awt.*;

import static com.aldodaquino.cobra.gui.constants.Dimensions.INFO_PANEL_PADDING;

/**
 * Info panel, superclass of {@link com.aldodaquino.cobra.gui.panels.AuthorInfoPanel},
 * {@link com.aldodaquino.cobra.gui.panels.GenreInfoPanel} and {@link ChartWidget}.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class InfoPanel extends JPanel {

    protected final LabelPanel latestLabel;
    protected final LabelPanel mostPopularLabel;
    protected final LabelPanel highestRatedLabel;
    protected final LabelPanel mostEnjoyedLabel;
    protected final LabelPanel biggestPriceFairnessLabel;
    protected final LabelPanel highestContentMeaningLabel;

    /**
     * Constructor. Can be invoked only by its children.
     * @param status the Status object.
     * @param mainLabelString the String to be putted in the top of the panel, with a bigger font.
     */
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
        JButton registerForNewContentsButton = ComponentFactory.newButton("Register/unregister for new contents",
                e -> status.getCatalogManager().listenNewContentAvailable(mainLabelString, (address, name) ->
                        Utils.newMessageDialog("New content available: " + name + ".")));

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
        add(registerForNewContentsButton, UpgradablePanel.newGBC(1, 11));
    }

}
