package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.InfoPanel;
import com.aldodaquino.cobra.main.CatalogManager;

/**
 * Shows the author charts in the catalog.
 * @see InfoPanel the parent class.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class AuthorInfoPanel extends InfoPanel {

    static final String WINDOW_TITLE = "About the author";

    /**
     * Constructor.
     * @param status the Status object.
     * @param author the author address.
     */
    AuthorInfoPanel(Status status, String author) {
        super(status, author);
        CatalogManager catalogManager = status.getCatalogManager();

        new Thread(() -> latestLabel.update(catalogManager.getLatestByAuthor(author))).start();
        new Thread(() -> mostPopularLabel.update(catalogManager.getMostPopularByAuthor(author))).start();
        new Thread(() -> highestRatedLabel.update(catalogManager.getMostRatedByAuthor(author, null))).start();
        new Thread(() -> mostEnjoyedLabel.update(catalogManager.getMostRatedByAuthor(author, "enjoy"))).start();
        new Thread(() -> biggestPriceFairnessLabel.update(catalogManager.getMostRatedByAuthor(author,
                "value for money"))).start();
        new Thread(() -> highestContentMeaningLabel.update(catalogManager.getMostRatedByAuthor(author,
                "content"))).start();
    }

    /**
     * Open a new window with this panel.
     * @param status the Status.
     * @param author of the content.
     */
    public static void newWindow(Status status, String author) {
        Utils.newWindow(WINDOW_TITLE, new AuthorInfoPanel(status, author), false);
    }

}
