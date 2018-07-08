package gui.components;

import javax.swing.*;
import java.awt.event.MouseListener;

import gui.constants.Dimensions;

class ListPanel extends JPanel {

    /**
     * List panel for the Main Panel: it consists of a label and a scroll panel with a list of elements
     * @param text the label
     * @param elems the String[] list
     * @param mouseListener for the elements of the list (used for the double click actions), can be null
     */
    ListPanel(String text, String[] elems, MouseListener mouseListener) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(Dimensions.V_PADDING_BORDER);

        JLabel label = new JLabel(text);

        JList<String> list = new JList<>(elems);
        if (mouseListener != null) list.addMouseListener(mouseListener);

        JScrollPane panel = new JScrollPane(list);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        this.add(label);
        this.add(Dimensions.V_SPACER);
        this.add(panel);
    }

}