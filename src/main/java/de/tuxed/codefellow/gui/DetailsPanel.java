/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DetailsPanel.java
 *
 * Created on Jun 11, 2010, 3:36:48 PM
 */
package de.tuxed.codefellow.gui;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.apache.bcel.classfile.JavaClass;

/**
 *
 * @author roman
 */
public class DetailsPanel extends javax.swing.JPanel {

    private final JavaClass javaClass;

    /** Creates new form DetailsPanel */
    public DetailsPanel(JavaClass javaClass) {
        this.javaClass = javaClass;
        initComponents();

        classHierarchy.setRootVisible(false);
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        addSuperTypeNodes(root, javaClass);
        TreeModel model = new DefaultTreeModel(root);
        classHierarchy.setModel(model);
        classHierarchy.setCellRenderer(new ClassInfoTreeCellRenderer());
    }

    private void addSuperTypeNodes(DefaultMutableTreeNode parent, JavaClass type) {
        try {
            // Superclass
            JavaClass superClass = type.getSuperClass();
            DefaultMutableTreeNode superNode = new DefaultMutableTreeNode(superClass);
            parent.add(superNode);
            if (!superClass.getClassName().equals(Object.class.getName())) {
                addSuperTypeNodes(superNode, superClass);
            }

            // Interfaces
            DefaultMutableTreeNode interfaceNode;
            for (JavaClass ic : type.getInterfaces()) {
                interfaceNode = new DefaultMutableTreeNode(ic);
                parent.add(interfaceNode);
//                addSuperTypeNodes(interfaceNode, ic);
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        classHierarchy = new javax.swing.JTree();

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        classHierarchy.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1.setViewportView(classHierarchy);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(223, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree classHierarchy;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
