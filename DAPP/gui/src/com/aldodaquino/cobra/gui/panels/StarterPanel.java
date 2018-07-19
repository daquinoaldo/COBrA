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

    private final UserInfo userInfo;
    private final JPanel loginForm;
    private JPanel catalogForm;
    private JPanel roleForm;

    private final GridBagConstraints replacingPosition;

    public StarterPanel(Consumer<Status> whenDone) {
        this.whenDone = whenDone;

        // init components
        JPanel logo = new Logo();
        userInfo = new UserInfo(status);
        loginForm = new LoginForm(this::loginCallback);
        replacingPosition = newGBC(1, 5);
        
        // prepare the panel and add components
        setBorder(ComponentFactory.newBorder(padding.width, padding.height));
        add(logo, newGBC(1, 1));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 2));
        add(userInfo, newGBC(1, 3));
        add(ComponentFactory.newVSpacer(Dimensions.V_SPACER_L), newGBC(1, 4));
        add(loginForm, replacingPosition);
    }

    // minimum size to fit all component
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /* CALLBACKS */
    private void loginCallback(String privateKey) {
        doAsync(() -> {
            try {
                // set the status
                status.login(privateKey);
                // update the user info
                userInfo.updateStatus();
                // change form
                catalogForm = new CatalogForm(this::connectCallback, this::deployCallback);
                replaceComponent(loginForm, catalogForm, replacingPosition);
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.newErrorDialog(e.getMessage());
            }
        });
    }

    private void connectCallback(String catalogAddress) {
        doAsync(() -> {
            try {
                status.connectCatalog(catalogAddress);
                postConnect();
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
                Utils.newErrorDialog(e.getMessage());
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
                Utils.newErrorDialog(e.getMessage());
            }
        });
    }

    private void postConnect() throws OperationNotSupportedException {
        userInfo.updateStatus();
        Runnable deleteCallback = status.isCatalogOwner() ? this::deleteCallback : null;
        roleForm = new RoleForm(this::browseCallback, this::manageCallback, this::disconnectCallback, deleteCallback);
        replaceComponent(catalogForm, roleForm, replacingPosition);
    }

    private void disconnectCallback() {
        doAsync(() -> {
            status.disconnectCatalog();
            userInfo.updateStatus();
            replaceComponent(roleForm, catalogForm, replacingPosition);
        });
    }

    private void deleteCallback() {
        if (!Utils.newConfirmDialog("Do you really want to delete this catalog?")) return;
        doAsync(() -> {
            if (status.getCatalogManager().suicide()) {
                Utils.newMessageDialog("Catalog deleted.");
                disconnectCallback();
            } else Utils.newErrorDialog("UNKNOWN ERROR: the catalog is not deleted.");
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
