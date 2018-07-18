package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;

import java.util.List;

public class AuthorContentTable extends ContentTable {

    public AuthorContentTable(CatalogManager catalogManager, List<Content> contents) {
        super(catalogManager, contents);
        // remove the author column
        removeColumn(getColumnModel().getColumn(3));
    }

}
