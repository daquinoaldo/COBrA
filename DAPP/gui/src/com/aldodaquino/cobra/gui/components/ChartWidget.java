package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.main.CatalogManager;

public class ChartWidget extends InfoPanel {

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
