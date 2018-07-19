package com.aldodaquino.cobra.gui.constants;


import javax.swing.*;
import java.awt.*;

public class Images {

    public static final ImageIcon logo = new ImageIcon(Images.class.getResource("/logo.png"));
    public static final ImageIcon loading = new ImageIcon(Images.class.getResource("/loading.gif"));
    public static final ImageIcon emptyStar = new ImageIcon(Images.class.getResource("/empty-star.png"));
    public static final ImageIcon filledStar = new ImageIcon(Images.class.getResource("/filled-star.png"));

    public static ImageIcon getScaled(ImageIcon icon, int size) {
        return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

}
