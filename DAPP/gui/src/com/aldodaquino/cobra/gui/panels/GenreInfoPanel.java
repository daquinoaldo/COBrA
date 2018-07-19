package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.InfoPanel;
import com.aldodaquino.cobra.main.CatalogManager;

public class GenreInfoPanel extends InfoPanel {

    public static final String WINDOW_TITLE = "About the author";

    public GenreInfoPanel(CatalogManager catalogManager, String genre) {
        super(catalogManager, genre);

        new Thread(() -> latestLabel.update(catalogManager.getLatestByGenre(genre))).start();
        new Thread(() -> mostPopularLabel.update(catalogManager.getMostPopularByGenre(genre))).start();
        new Thread(() -> highestRatedLabel.update(catalogManager.getMostRatedByGenre(genre, null))).start();
        new Thread(() -> mostEnjoyedLabel.update(catalogManager.getMostRatedByGenre(genre, "enjoy"))).start();
        new Thread(() -> biggestPriceFairnessLabel.update(catalogManager.getMostRatedByGenre(genre,
                "value for money"))).start();
        new Thread(() -> highestContentMeaningLabel.update(catalogManager.getMostRatedByGenre(genre,
                "content"))).start();
    }

    /**
     * Open a new window with this panel.
     * @param catalogManager the catalog manager in Status.
     * @param genre of the content.
     */
    public static void newWindow(CatalogManager catalogManager, String genre) {
        Utils.newWindow(WINDOW_TITLE, new GenreInfoPanel(catalogManager, genre), false);
    }

}
