package de.tuxed.codefellow.gui;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.bcel.classfile.JavaClass;

class ClassInfoListCellRenderer implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JavaClass jc = (JavaClass) value;
        ClassInfoLabel label = new ClassInfoLabel(jc);
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        label.setEnabled(list.isEnabled());
        label.setOpaque(true);
        return label;
    }
}
