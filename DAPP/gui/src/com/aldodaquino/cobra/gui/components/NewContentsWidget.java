package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;
import com.aldodaquino.cobra.main.Content;

import javax.swing.*;
import java.util.List;

public class NewContentsWidget extends JPanel {

    private final CatalogManager catalogManager;
    private final JSpinner numberSpinner;

    public NewContentsWidget(CatalogManager catalogManager) {
        this.catalogManager = catalogManager;

        JLabel label1 = new JLabel("Get");
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 0, 100, 1);
        numberSpinner = new JSpinner(spinnerModel);
        JLabel label2 = new JLabel("new contents");
        JButton goButton = ComponentFactory.newButton("Go", e -> getNewContentList());

        add(label1);
        add(numberSpinner);
        add(label2);
        add(goButton);
    }

    private void getNewContentList() {
        List<Content> contents = catalogManager.getNewContentList((int) numberSpinner.getValue());
        JList contentList = new ContentList(catalogManager, contents);
        Utils.newWindow("New content list", contentList, false);
    }

}
