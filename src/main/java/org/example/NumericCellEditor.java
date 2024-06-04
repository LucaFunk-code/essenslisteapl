package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericCellEditor extends DefaultCellEditor {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d*\\.?\\d*$");
    private JTextField textField;

    public NumericCellEditor() {
        super(new JTextField());
        this.textField = (JTextField) getComponent();
        textField.setHorizontalAlignment(JTextField.RIGHT);
    }

    @Override
    public boolean stopCellEditing() {
        String value = (String) super.getCellEditorValue();
        if (value != null && !value.isEmpty() && !isNumeric(value)) {
            textField.setBorder(BorderFactory.createLineBorder(Color.RED));
            return false; // Don't stop editing if the value is not numeric
        } else {
            return super.stopCellEditing();
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setBorder(UIManager.getBorder("Table.cellBorder")); // Reset border
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    private boolean isNumeric(String str) {
        Matcher matcher = NUMBER_PATTERN.matcher(str);
        return matcher.matches();
    }
}
