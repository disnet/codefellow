package de.tuxed.jray.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import java.awt.Font;
import javax.swing.ImageIcon;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;

class ClassInfoCellRenderer extends JLabel implements ListCellRenderer {

    private static final ImageIcon ICON_CLASS = new ImageIcon(ClassInfoCellRenderer.class.getResource("icon_class.gif"));
    private static final ImageIcon ICON_INTERFACE = new ImageIcon(ClassInfoCellRenderer.class.getResource("icon_interface.png"));
    private static final ImageIcon ICON_OBJECT = new ImageIcon(ClassInfoCellRenderer.class.getResource("icon_object.gif"));

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JavaClass jc = (JavaClass) value;

        setFont(new Font("Arial", Font.PLAIN, 12));
        setText(getHtml(jc));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());
        setOpaque(true);
        return this;
    }

    private String getHtml(JavaClass jc) {
        String cn = jc.getClassName().substring(jc.getClassName().lastIndexOf(".") + 1);

        // Icon
        boolean isInterface = !jc.isClass();
        if (isInterface)
            setIcon(ICON_INTERFACE);
        else {
            if (cn.endsWith("$")) {
                cn = cn.substring(0, cn.length() - 1);
                setIcon(ICON_OBJECT);
            }
            else
                setIcon(ICON_CLASS);
        }

        // Class name
        if (jc.isAbstract()) {
            cn = "<i>" + cn + "</i>";
        }
        cn = "<b>" + cn + "</b>";

        // Access
        String access = Utility.accessToString(jc.getAccessFlags(), true);
        // TODO



        // TODO

        // Package
        String pn = jc.getClassName().substring(0, jc.getClassName().lastIndexOf("."));

        return "<html>" + cn + " " + pn + "</html>";
    }
}
