package com.aldodaquino.cobra.gui.panels;

import com.aldodaquino.cobra.gui.components.CatalogForm;
import com.aldodaquino.cobra.gui.components.ComponentFactory;
import com.aldodaquino.cobra.gui.components.LoginForm;
import com.aldodaquino.cobra.gui.components.Logo;
import com.aldodaquino.cobra.gui.constants.Dimensions;
import com.aldodaquino.cobra.main.Status;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private static final Dimension padding = new Dimension(65, 35);

    private Status status = new Status();

    private JPanel loginForm;
    private JPanel catalogForm;

    public LoginPanel() {
        // Init components
        JPanel logo = new Logo();
        loginForm = new LoginForm(this::loginCallback);
        
        // Making the panel
        setBorder(ComponentFactory.newBorder(padding.width, padding.height));
        //setLayout(new GridLayout(2,1,3,50));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(logo);
        add(ComponentFactory.newVSpacer(Dimensions.BIG_V_SPACER));
        add(loginForm);
    }
    
    private void loginCallback(String privateKey) {
        // set the status
        status.login(privateKey);

        // change form
        catalogForm = new CatalogForm(this::connectCallback, this::deployCallback);
        replaceComponent(loginForm, catalogForm);

        /*remove(loginForm);
        add(catalogForm);
        JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
        window.revalidate();
        window.repaint();
        window.pack();*/
    }

    private void connectCallback(String catalogAddress) {
        status.connect(catalogAddress);
    }

    private void deployCallback() {
        status.deploy();
    }

    private void replaceComponent(Component toBeReplaced, Component replacement) {
        remove(toBeReplaced);
        add(replacement);
        JFrame window = (JFrame) SwingUtilities.getWindowAncestor(this);
        window.revalidate();
        window.repaint();
        window.pack();
    }
}
