package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.util.List;

public class ContentTable extends JTable {

    private static final String[] colNames = {"Address", "Name", "Genre", "Views", "Enjoy", "Price fairness",
            "Content meaning", "Price"};

    public ContentTable(List<Content> contents) {
        super(prepareRows(contents), colNames);
    }

    private static Object[][] prepareRows(List<Content> contents) {
        Object[][] rows = new Object[contents.size()][colNames.length];
        for (int i = 0; i < contents.size(); i++) {
            String address = contents.get(i).address;
            rows[i][0] = address;
            rows[i][1] = contents.get(i).name;
            rows[i][2] = contents.get(i).genre;
            rows[i][3] = contents.get(i).views;
            rows[i][4] = contents.get(i).enjoy;
            rows[i][5] = contents.get(i).priceFairness;
            rows[i][6] = contents.get(i).contentMeaning;
            rows[i][7] = contents.get(i).price;
        }
        return rows;
    }



}
