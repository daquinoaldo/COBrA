package com.aldodaquino.cobra.gui.components;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionListener;

/**
 * Components factory: makes components creation faster and easier.
 */
public class ComponentFactory {

    static JButton newButton(String text, ActionListener e) {
        JButton button = new JButton(text);
        button.addActionListener(e);
        return button;
    }

    static JTextField newTextField(ActionListener e) {
        return newField(e, false);
    }

    static JTextField newPasswordField(ActionListener e) {
        return newField(e, true);
    }

    private static JTextField newField(ActionListener e, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.addActionListener(e);
        return field;
    }

    public static Border newBorder(int width, int height) {
        return BorderFactory.createEmptyBorder(height, width,  height, width);
    }







    

    /* Configuration */
    /*private static final Border inputBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.logo, 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
    );

    private static final Color mainButtonBackground = Colors.logo;
    private static final Color mainButtonText = Colors.lightText;
    private static final Color secondaryButtonBackground = Colors.background;
    private static final Color secondaryButtonText = Colors.logo;*/
    /* Dropdown select */
    /*@SuppressWarnings("unchecked")
    public static JComboBox getComboBox(Strings[] values, ActionListener e) {
        class MyRenderer extends JLabel implements ListCellRenderer {
            private MyRenderer() {
                setOpaque(true);
                setFont(new Font("Arial", Font.PLAIN, 14));
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (isSelected) {
                    setBackground(mainButtonBackground);
                    setForeground(mainButtonText);
                }
                else {
                    setBackground(secondaryButtonBackground);
                    setForeground(secondaryButtonText);
                }
                setText(value.toString());
                return this;
            }
        }
    
        class MyEditor extends BasicComboBoxEditor {
            private final JLabel label = new JLabel();
            private final JPanel panel = new JPanel();
            private Object selectedItem;
        
            private MyEditor() {
                label.setOpaque(false);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setForeground(secondaryButtonText);
            
                panel.setBorder(inputBorder);
                panel.setLayout(new FlowLayout(FlowLayout.CENTER));
                panel.add(label);
                panel.setBackground(secondaryButtonBackground);
            }
        
            public Component getEditorComponent() {
                return this.panel;
            }
        
            public Object getItem() {
                return this.selectedItem.toString();
            }
        
            public void setItem(Object item) {
                this.selectedItem = item;
                label.setText(item.toString());
            }
        }
        
        JComboBox box = new JComboBox(values);
        
        box.setEditable(true);
        box.setUI(new ColorArrowUI());
        box.setRenderer(new MyRenderer());
        box.setEditor(new MyEditor());
        
        box.addActionListener(e);
        return box;
    }*/
}
