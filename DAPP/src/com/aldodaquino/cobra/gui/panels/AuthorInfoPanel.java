package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.InfoPanel;
import com.aldodaquino.cobra.main.CatalogManager;

public class AuthorInfoPanel extends InfoPanel {

    public AuthorInfoPanel(CatalogManager catalogManager, String author) {
        super(author);

        new Thread(() -> latestLabel.update(catalogManager.getLatestByAuthor(author))).start();
        new Thread(() -> mostPopularLabel.update(catalogManager.getMostPopularByAuthor(author))).start();
        new Thread(() -> highestRatedLabel.update(catalogManager.getMostRatedByAuthor(author, null))).start();
        new Thread(() -> mostEnjoyedLabel.update(catalogManager.getMostRatedByAuthor(author, "enjoy"))).start();
        new Thread(() -> biggestPriceFairnessLabel.update(catalogManager.getMostRatedByAuthor(author,
                "value for money"))).start();
        new Thread(() -> highestContentMeaningLabel.update(catalogManager.getMostRatedByAuthor(author,
                "content"))).start();
    }

}
