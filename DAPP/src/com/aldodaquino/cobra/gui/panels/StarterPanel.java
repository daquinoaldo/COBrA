package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.Utils;
import com.aldodaquino.cobra.gui.components.*;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.Status;

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

    private final GridBagConstraints c13;

    public StarterPanel(Consumer<Status> whenDone) {
        this.whenDone = whenDone;

        // init components
        JPanel logo = new Logo();
        loginForm = new LoginForm(this::loginCallback);
        
        // making the panel
        setBorder(ComponentFactory.newBorder(padding.width, padding.height));
        //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new GridBagLayout());

        // add logo in (1, 1)
        GridBagConstraints c11 = new GridBagConstraints();
        c11.gridx = 1;
        c11.gridy = 1;
        add(logo, c11);

        // add spacer in (1, 2)
        GridBagConstraints c12 = new GridBagConstraints();
        c12.gridx = 1;
        c12.gridy = 2;
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), c12);

        // add login form in (1, 3)
        c13 = new GridBagConstraints();
        c13.gridx = 1;
        c13.gridy = 3;
        add(loginForm, c13);
    }

    /* CALLBACKS */
    private void loginCallback(String privateKey) {
        // set the status
        status.login(privateKey);

        // change form
        catalogForm = new CatalogForm(this::connectCallback, this::deployCallback);
        replaceComponent(loginForm, catalogForm);
    }

    private void connectCallback(String catalogAddress) {
        Utils.doAsync(() -> {
            status.connectCatalog(catalogAddress);
            postConnect();
        }, window);
    }

    private void deployCallback() {
        Utils.doAsync(() -> {
            status.deployCatalog();
            postConnect();
        }, window);
    }

    private void postConnect() {
        Runnable deleteCallback = status.isCatalogOwner() ? this::deleteCallback : null;
        roleForm = new RoleForm(this::browseCallback, this::manageCallback, this::disconnectCallback, deleteCallback);
        replaceComponent(catalogForm, roleForm);
    }

    private void disconnectCallback() {
        status.disconnectCatalog();
        replaceComponent(roleForm, catalogForm);
    }

    private void deleteCallback() {
        if (!Utils.showConfirmDialog("Do you really want to delete this catalog?")) return;
        Utils.doAsync(() -> {
            if (status.getCatalogManager().suicide()) {
                Utils.showMessageDialog("Catalog deleted.");
                disconnectCallback();
            } else Utils.showErrorDialog("UNKNOWN ERROR: the catalog is not deleted.");
        }, window);
    }

    private void browseCallback() {
        status.setRole(Status.ROLE_CUSTOMER);
        whenDone.accept(status);
    }

    private void manageCallback() {
        status.setRole(Status.ROLE_AUTHOR);
        whenDone.accept(status);
    }

    /* HELPERS */
    private void replaceComponent(Component toBeReplaced, Component replacement) {
        remove(toBeReplaced);
        add(replacement, c13);
        window.revalidate();
        window.repaint();
        window.pack();
        window.setLocationRelativeTo(null);
    }
}
