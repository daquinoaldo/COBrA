package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.main.CatalogManager;

public class ChartBanner extends InfoPanel {

    public ChartBanner(CatalogManager catalogManager) {
        super("Charts");

        latestLabel.update(catalogManager.getLatest());
        mostPopularLabel.update(catalogManager.getMostPopular());
        highestRatedLabel.update(catalogManager.getMostRated(null));
        mostEnjoyedLabel.update(catalogManager.getMostRated("enjoy"));
        biggestPriceFairnessLabel.update(catalogManager.getMostRated("value for money"));
        highestContentMeaningLabel.update(catalogManager.getMostRated("content"));
    }

}
