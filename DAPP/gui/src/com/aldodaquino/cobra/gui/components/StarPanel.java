package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.constants.Images;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.*;

/**
 * The star rater panel. 
 *
 * @author noblemaster
 * @since August 30, 2010
 */
public class StarPanel extends UpgradablePanel {

    private static final int STARS_NUMBER = 5;

    private int rating;
    private final boolean enabled;
    private final List<JLabel> stars = new ArrayList<>(Collections.nCopies(5, null));

    /**
    * Constructor. Initialize an empty StarPanel, enabled.
    */
    public StarPanel() {
        this(0, true);
    }

    /**
     * Constructor. Initialize a StarPanel with the specified rating, disabled.
     * @param rating in the interval [0, 5]. 0 means no rating.
     */
    public StarPanel(int rating) {
        this(rating, false);
    }

    /**
    * Constructor.
    * @param rating the rating in the interval [0, 5]. 0 means no rating.
    * @param enabled true if users can vote, false to show only the rating.
    */
    private StarPanel(int rating, boolean enabled) {
        stars.add(null);

        setRating(rating);
        this.enabled = enabled;
    }

    /**
    * Returns the rating.
    * @return int in the interval [0, 5]. 0 means no rating.
    */
    public int getRating() {
        return rating;
    }

    /**
    * Sets the rating.
    * @param rating in the interval [0, 5]. 0 means no rating.
    */
    private void setRating(int rating) {
        if (rating < 0 || rating > STARS_NUMBER)
            throw new IllegalArgumentException("The rating must appertain at the interval [0, " + STARS_NUMBER + "].");
        this.rating = rating;
        for (int i = 0; i < STARS_NUMBER; i++) {
            JLabel newStar = getStar(i < rating);
            replaceComponent(stars.get(i), newStar, newGBC(i + 1, 0));
            stars.add(i, newStar);
        }
    }

    private JLabel getStar(boolean filled) {
        ImageIcon icon = filled ? Images.filledStar : Images.emptyStar;
        icon = Images.getScaled(icon, Dimensions.STAR_SIZE);
        JLabel label = new JLabel(icon, JLabel.CENTER);
        if (enabled) label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setRating(stars.indexOf(label) + 1);
            }
        });
        return label;
    }

}
