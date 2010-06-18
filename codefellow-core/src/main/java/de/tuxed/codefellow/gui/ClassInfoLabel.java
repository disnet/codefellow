package de.tuxed.codefellow.gui;


import javax.swing.JLabel;

import java.awt.Font;
import javax.swing.ImageIcon;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;

class ClassInfoLabel extends JLabel {

    private static final ImageIcon ICON_CLASS = new ImageIcon(ClassInfoLabel.class.getResource("icon_class.gif"));
    private static final ImageIcon ICON_INTERFACE = new ImageIcon(ClassInfoLabel.class.getResource("icon_interface.png"));
    private static final ImageIcon ICON_OBJECT = new ImageIcon(ClassInfoLabel.class.getResource("icon_object.gif"));

    public ClassInfoLabel(JavaClass javaClass) {
        setFont(new Font("Arial", Font.PLAIN, 12));
        setText(getHtml(javaClass));
    }

    private String getHtml(JavaClass jc) {
        String cn = jc.getClassName().substring(jc.getClassName().lastIndexOf(".") + 1);

        // Icon
        boolean isInterface = !jc.isClass();
        if (isInterface) {
            setIcon(ICON_INTERFACE);
        } else {
            if (cn.endsWith("$")) {
                cn = cn.substring(0, cn.length() - 1);
                setIcon(ICON_OBJECT);
            } else {
                setIcon(ICON_CLASS);
            }
        }

        // Class name
        if (jc.isAbstract()) {
            cn = "<i>" + cn + "</i>";
        }
        cn = "<b>" + cn + "</b>";

        // Access
        String access = Utility.accessToString(jc.getAccessFlags(), true);
        // TODO



        // Package
        String pn = jc.getClassName().substring(0, jc.getClassName().lastIndexOf("."));
        pn = "<font color='#c68200'>" + pn + "</font>";

        return "<html>" + cn + " " + pn + "</html>";
    }
}
