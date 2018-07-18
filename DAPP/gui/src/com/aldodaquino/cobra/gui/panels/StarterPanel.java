package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.*;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.Status;

import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class StarterPanel extends UpgradablePanel {

    private static final Dimension padding = new Dimension(65, 35);

    private final Status status = new Status();
    private final Consumer<Status> whenDone;

    private final JPanel loginForm;
    private JPanel catalogForm;
    private JPanel roleForm;

    private final GridBagConstraints replacingPosition;

    public StarterPanel(Consumer<Status> whenDone) {
        this.whenDone = whenDone;

        // init components
        JPanel logo = new Logo();
        loginForm = new LoginForm(this::loginCallback);
        
        // making the panel
        setBorder(ComponentFactory.newBorder(padding.width, padding.height));

        // add logo in (1, 1)
        add(logo, newGBC(1, 1));

        // add spacer in (1, 2)
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 2));

        // add login form in (1, 3)
        replacingPosition = newGBC(1, 3);
        add(loginForm, replacingPosition);
    }

    /* CALLBACKS */
    private void loginCallback(String privateKey) {
        // set the status
        try {
            status.login(privateKey);
            // change form
            catalogForm = new CatalogForm(status, this::connectCallback, this::deployCallback);
            replaceComponent(loginForm, catalogForm, replacingPosition);
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
            Utils.showErrorDialog(e.getMessage());
        }
    }

    private void connectCallback(String catalogAddress) {
        doAsync(() -> {
            try {
                status.connectCatalog(catalogAddress);
                postConnect();
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.showErrorDialog(e.getMessage());
            }
        });
    }

    private void deployCallback() {
        doAsync(() -> {
            try {
                status.deployCatalog();
                postConnect();
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.showErrorDialog(e.getMessage());
            }
        });
    }

    private void postConnect() throws OperationNotSupportedException {
        Runnable deleteCallback = status.isCatalogOwner() ? this::deleteCallback : null;
        roleForm = new RoleForm(status, this::browseCallback, this::manageCallback, this::disconnectCallback,
                deleteCallback);
        replaceComponent(catalogForm, roleForm, replacingPosition);
    }

    private void disconnectCallback() {
        status.disconnectCatalog();
        replaceComponent(roleForm, catalogForm, replacingPosition);
    }

    private void deleteCallback() {
        if (!Utils.showConfirmDialog("Do you really want to delete this catalog?")) return;
        doAsync(() -> {
            if (status.getCatalogManager().suicide()) {
                Utils.showMessageDialog("Catalog deleted.");
                disconnectCallback();
            } else Utils.showErrorDialog("UNKNOWN ERROR: the catalog is not deleted.");
        });
    }

    private void browseCallback() {
        status.setRole(Status.ROLE_CUSTOMER);
        whenDone.accept(status);
    }

    private void manageCallback() {
        status.setRole(Status.ROLE_AUTHOR);
        whenDone.accept(status);
    }

}
