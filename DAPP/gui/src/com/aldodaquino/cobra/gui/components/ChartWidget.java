package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.main.CatalogManager;

/**
 * An {@link InfoPanel} used in the {@link com.aldodaquino.cobra.gui.panels.CustomerPanel}.
 * Shows the chart of the catalog, that includes: the latest content, the most popular content, and the highest rated
 * content in absolute and for each category.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class ChartWidget extends InfoPanel {

    /**
     * Constructor.
     * @param status the Status object.
     */
    public ChartWidget(Status status) {
        super(status, "Charts");
        CatalogManager catalogManager = status.getCatalogManager();

        latestLabel.update(catalogManager.getLatest());
        mostPopularLabel.update(catalogManager.getMostPopular());
        highestRatedLabel.update(catalogManager.getMostRated(null));
        mostEnjoyedLabel.update(catalogManager.getMostRated("enjoy"));
        biggestPriceFairnessLabel.update(catalogManager.getMostRated("value for money"));
        highestContentMeaningLabel.update(catalogManager.getMostRated("content"));
    }

}
