package gui.components;

import misc.TriConsumer;
import gui.constants.Colors;
import gui.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * Register Form under the Logo in the Register Panel.
 * Note: there are only a few languages, the translation service supports many others,
 * but we have chosen a small subset just for demo
 */
public class RegisterForm extends JPanel {
    private static final HashMap<String, String> languageList = new HashMap<String, String>(){{
        put("Italiano", "it");
        put("English", "en");
        put("Français", "fr");
        put("Deutsch", "de");
        put("Español", "es");
    }};
    
    private final TriConsumer<String, String, String> registerCallback;
    private final JTextField usernameInput = InputFactory.getTextInput("username", e -> submit());
    private final JTextField passwordInput = InputFactory.getPasswordInput("password", e -> submit());
    private final JTextField passwordInput2 = InputFactory.getPasswordInput("password", e -> submit());
    private final JComboBox languageInput = InputFactory.getComboBox(languageList.keySet().toArray(new String[0]), null);
    
    public RegisterForm(TriConsumer<String, String, String> registerCallback) {
        this.registerCallback = registerCallback;
        this.setBackground(Colors.background);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JButton registerBtn = InputFactory.getMainButton("Register", e -> submit());
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(Colors.background);
        buttonsPanel.add(registerBtn);
        
        this.add(usernameInput);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(passwordInput);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(passwordInput2);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(languageInput);
        this.add(Box.createRigidArea(new Dimension(0, 15)));
        this.add(buttonsPanel);
    }

    /**
     * Submit action
     */
    private void submit() {
        String username = usernameInput.getText();
        String password = passwordInput.getText();
        String password2 = passwordInput2.getText();
        @SuppressWarnings("ConstantConditions")
        String language = languageList.get(languageInput.getSelectedItem().toString());

        // validate username and password
        if (Utils.isValidUsername(username) && password.length() > 0 && password.equals(password2)
                && language.length() == 2)
            registerCallback.accept(username, password, language);
        else Utils.showErrorDialog("Entered data are not correct.");
    }
}
