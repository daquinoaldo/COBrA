package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.InfoPanel;
import com.aldodaquino.cobra.main.CatalogManager;

/**
 * Shows the genre charts in the catalog.
 * @see InfoPanel the parent class.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class GenreInfoPanel extends InfoPanel {

    static final String WINDOW_TITLE = "About the author";

    /**
     * Constructor.
     * @param status the Status object.
     * @param genre the genre.
     */
    GenreInfoPanel(Status status, String genre) {
        super(status, genre);
        CatalogManager catalogManager = status.getCatalogManager();

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
     * @param status the Status.
     * @param genre of the content.
     */
    public static void newWindow(Status status, String genre) {
        Utils.newWindow(WINDOW_TITLE, new GenreInfoPanel(status, genre), false);
    }

}
