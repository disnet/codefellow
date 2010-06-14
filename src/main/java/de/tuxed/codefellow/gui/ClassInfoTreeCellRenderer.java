package de.tuxed.codefellow.gui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.apache.bcel.classfile.JavaClass;

class ClassInfoTreeCellRenderer implements TreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        JavaClass jc = (JavaClass) node.getUserObject();
        ClassInfoLabel label = new ClassInfoLabel(jc);

        if (selected) {
//            label.setBackground(tree.getSelectionBackground());
//            label.setForeground(tree.getSelectionForeground());
        } else {
            label.setBackground(tree.getBackground());
            label.setForeground(tree.getForeground());
        }
        label.setEnabled(tree.isEnabled());

        label.setOpaque(true);
        return label;
    }
}
