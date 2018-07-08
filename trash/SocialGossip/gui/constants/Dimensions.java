package gui.constants;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Dimensions {
    // Panels dimensions
    public static final Dimension CHAT_PANE = new Dimension(600, 400);

    // Inner-panel measures
    private static final int PADDING = 10;

    // Elements
    public static final Border PADDING_BORDER = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
    public static final Border V_PADDING_BORDER = BorderFactory.createEmptyBorder(PADDING, 0, PADDING, 0);
    public static final Component V_SPACER = Box.createRigidArea(new Dimension(0,5));
}
