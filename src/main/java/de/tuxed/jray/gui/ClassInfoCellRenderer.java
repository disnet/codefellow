package de.tuxed.jray.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.tuxed.jray.ClassInfo;

class ClassInfoCellRenderer extends JLabel implements ListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	ClassInfo ci = (ClassInfo) value;
	
	String cn = ci.getClassName().substring(ci.getClassName().lastIndexOf(".") + 1);
	String pn = ci.getClassName().substring(0, ci.getClassName().lastIndexOf("."));
	setText(cn + " - " + pn);
	
	if (isSelected) {
	    setBackground(list.getSelectionBackground());
	    setForeground(list.getSelectionForeground());
	} else {
	    setBackground(list.getBackground());
	    setForeground(list.getForeground());
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	setOpaque(true);
	return this;
    }
}
