package gui.components;

import gui.constants.Colors;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Input factory for the Login and Registration Forms
 */
class InputFactory {
    /* Configuration */
    private static final Border inputBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colors.accent, 1),
            BorderFactory.createEmptyBorder(2, 10, 2, 10)
    );
    private static final Color inputTextColor = Colors.accent;
    
    private static final Color mainButtonBackground = Colors.accent;
    private static final Color mainButtonText = Colors.lightText;
    private static final Color secondaryButtonBackground = Colors.background;
    private static final Color secondaryButtonText = Colors.accent;
    
    /* End configuration */
    
    /* Text inputs */
    private static JTextField makeField(String defaultText, ActionListener e, boolean isPassword) {
        JTextField field = isPassword ? new JPasswordField(defaultText) : new JTextField(defaultText);
        field.setBorder(inputBorder);                    // box border
        field.setForeground(inputTextColor);              // text color
        field.setHorizontalAlignment(JTextField.CENTER); // text align center
        field.addActionListener(e);
    
        return field;
    }
    
    public static JTextField getTextInput(String defaultText, ActionListener e) {
        return makeField(defaultText, e, false);
    }
    
    public static JTextField getPasswordInput(String defaultText, ActionListener e) {
        return makeField(defaultText, e, true);
    }
    
    /* Buttons */
    private static JButton makeButton(String text, ActionListener e, Color backgroundColor, Color textColor, boolean paintBorders) {
        JButton btn = new JButton(text);
        btn.addActionListener(e);
        btn.setBackground(backgroundColor);
        btn.setForeground(textColor);
        btn.setBorderPainted(paintBorders);
        return btn;
    }
    
    public static JButton getMainButton(String text, ActionListener e) {
        return makeButton(text, e, mainButtonBackground, mainButtonText, true);
    }
    
    public static JButton getSecondaryButton(String text, ActionListener e) {
        return makeButton(text, e, secondaryButtonBackground, secondaryButtonText, false);
    }
    
    /* Dropdown select */
    @SuppressWarnings("unchecked")
    public static JComboBox getComboBox(String[] values, ActionListener e) {
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
    }
}
