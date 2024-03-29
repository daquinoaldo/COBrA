package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Status;
import com.aldodaquino.cobra.gui.panels.AuthorInfoPanel;
import com.aldodaquino.cobra.gui.panels.GenreInfoPanel;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * A JTable for the {@link com.aldodaquino.cobra.gui.panels.AuthorPanel}.
 * @author Aldo D'Aquino.
 * @version 1.0.
 */
public class AuthorContentTable extends JTable {

    private static final String[] colNames = {"Address", "Name", "Author", "Genre", "Views", "Enjoy", "Price fairness",
            "Content meaning", "Price"};

    private static Object[][] prepareRows(List<Content> contents) {
        Object[][] rows = new Object[contents.size()][colNames.length];
        for (int i = 0; i < contents.size(); i++) {
            String address = contents.get(i).address;
            rows[i][0] = address;
            rows[i][1] = contents.get(i).name;
            rows[i][2] = contents.get(i).author;
            rows[i][3] = contents.get(i).genre;
            rows[i][4] = contents.get(i).views;
            rows[i][5] = contents.get(i).enjoy;
            rows[i][6] = contents.get(i).priceFairness;
            rows[i][7] = contents.get(i).contentMeaning;
            rows[i][8] = contents.get(i).price;
        }
        return rows;
    }

    /**
     * Constructor.
     * @param status the Status object.
     * @param contents a List of Content objects.
     */
    public AuthorContentTable(Status status, List<Content> contents) {
        super(prepareRows(contents), colNames);

        // render author and genre as link style
        TableCellRenderer linkRenderer = (table, value, arg2, arg3, arg4, arg5) ->
                new JLabel("<html><a href=\"about:" + value + "\">" + value + "</a>");
        getColumnModel().getColumn(2).setCellRenderer(linkRenderer);
        getColumnModel().getColumn(3).setCellRenderer(linkRenderer);

        // mouse listener for author and genre click and hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(new Point(e.getX(), e.getY()));
                int col = columnAtPoint(new Point(e.getX(), e.getY()));
                String cellContent = (String) getModel().getValueAt(row, col);
                if (col == 2) AuthorInfoPanel.newWindow(status, cellContent);
                if (col == 3) GenreInfoPanel.newWindow(status, cellContent);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                int col = columnAtPoint(new Point(e.getX(), e.getY()));
                if (col == 2 || col == 3) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                int col = columnAtPoint(new Point(e.getX(), e.getY()));
                if (col != 2 && col != 3) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    // Make cells not editable
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
