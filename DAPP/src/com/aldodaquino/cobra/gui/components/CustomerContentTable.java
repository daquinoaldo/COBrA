package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.panels.AuthorInfoPanel;
import com.aldodaquino.cobra.gui.panels.GenreInfoPanel;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class CustomerContentTable extends ContentTable {

    private final CatalogManager catalogManager;

    public CustomerContentTable(CatalogManager catalogManager, List<Content> contents) {
        super(contents);

        this.catalogManager = catalogManager;

        // mouse listener for author and genre click and hover
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = rowAtPoint(new Point(e.getX(), e.getY()));
                int col = columnAtPoint(new Point(e.getX(), e.getY()));
                String cellContent = (String) getModel().getValueAt(row, col);
                if (col == 2) showAuthorInfo(cellContent);
                if (col == 3) showGenreInfo(cellContent);
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

        // render author and genre as link style
        TableCellRenderer linkRenderer = (table, value, arg2, arg3, arg4, arg5) ->
                new JLabel("<html><a href=\"about:" + value + "\">" + value + "</a>");
        getColumnModel().getColumn(2).setCellRenderer(linkRenderer);
        getColumnModel().getColumn(3).setCellRenderer(linkRenderer);
    }

    // Make cells not editable
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    private void showAuthorInfo(String author) {
        JPanel authorInfoPanel = new AuthorInfoPanel(catalogManager, author);
        Utils.createFixedWindow("About the author", authorInfoPanel, false);
    }

    private void showGenreInfo(String genre) {
        JPanel genreInfoPanel = new GenreInfoPanel(catalogManager, genre);
        Utils.createFixedWindow("About the genre", genreInfoPanel, false);
    }

}
