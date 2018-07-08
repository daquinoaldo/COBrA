package gui.panels;

import connections.Json;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateRoomPanel extends JPanel {

    public CreateRoomPanel() {
        // Search panel
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JTextField textField = new JTextField(30);
        textField.requestFocusInWindow();

        JButton button = new JButton("Create");

        // Search listeners
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!textField.getText().equals("")) {
                    if(!Json.createRoom(textField.getText()))
                        System.err.println("Json.createRoom fails");
                    textField.setText("");
                }
            }
        };
        textField.addActionListener(action);
        button.addActionListener(action);

        this.add(textField);
        this.add(button);
    }

}
