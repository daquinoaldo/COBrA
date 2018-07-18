package com.aldodaquino.cobra.gui.components;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.main.CatalogManager;

import javax.swing.*;

public class newContentWidget extends JPanel {

    private final CatalogManager catalogManager;
    private final JSpinner numberSpinner;

    public newContentWidget(CatalogManager catalogManager) {
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
        String[][] rows = catalogManager.getNewContentsList((int) numberSpinner.getValue());
        String[] colNames = {"Name", "Address"};
        JScrollPane tableContainer = new JScrollPane(new JTable(rows, colNames));
        Utils.createFixedWindow("New content list", tableContainer, false);
    }

}
