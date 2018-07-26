package com.aldodaquino.cobra.gui.constants;


import javax.swing.*;
import java.awt.*;

/**
 * Images used in the Graphic Interface.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class Images {

    public static final ImageIcon logo = new ImageIcon(Images.class.getResource("/logo.png"));
    public static final ImageIcon loading = new ImageIcon(Images.class.getResource("/loading.gif"));
    public static final ImageIcon emptyStar = new ImageIcon(Images.class.getResource("/empty-star.png"));
    public static final ImageIcon filledStar = new ImageIcon(Images.class.getResource("/filled-star.png"));

    /**
     * Returns the scaled version of an image.
     * @param icon the original image.
     * @param size the size that you want the final image to have.
     * @return another ImageIcon, scaled.
     */
    public static ImageIcon getScaled(ImageIcon icon, int size) {
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

}
