/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainPanel.java
 *
 * Created on Jun 9, 2010, 8:07:53 PM
 */
package de.tuxed.codefellow.gui;

import de.tuxed.codefellow.FsUtils;
import de.tuxed.codefellow.MethodInfo;
import de.tuxed.codefellow.Project;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.ListModel;
import org.apache.bcel.classfile.JavaClass;

/**
 *
 * @author roman
 */
public class MainPanel extends javax.swing.JPanel {

    private static final ImageIcon ICON_FROMALL = new ImageIcon(ClassInfoListCellRenderer.class.getResource("icon_fromall.png"));
    private static final ImageIcon ICON_FILTER = new ImageIcon(ClassInfoListCellRenderer.class.getResource("icon_filter.png"));
    private static final ImageIcon ICON_LIBRARY = new ImageIcon(ClassInfoListCellRenderer.class.getResource("icon_library.png"));
    private Project project = null;
    private final ScopeWindow scopeWindow;
    private String classPathString = ".";

    /** Creates new form MainPanel */
    public MainPanel() {
        initComponents();

        selectScope.setIcon(ICON_LIBRARY);
        showMethodsFromAllClasses.setIcon(ICON_FROMALL);
        filterMethods.setIcon(ICON_FILTER);

        classList.setCellRenderer(new ClassInfoListCellRenderer());
        methodList.setCellRenderer(new MethodCellRenderer(this));

        List<String[]> libraries = new LinkedList<String[]>();
        libraries.addAll(FsUtils.getAllProjectOutputDirectories("."));
        libraries.addAll(FsUtils.getAllUniqueJarFiles("."));

        this.scopeWindow = new ScopeWindow(this, libraries);

        for (String[] lib : libraries) {
            classPathString += ":" + lib[1];
        }
    }

    public void updateProject() {
        // active projects
        JTable table = scopeWindow.getTable();
        List<String[]> active = new LinkedList<String[]>();
        for (int i = 0; i < table.getModel().getRowCount(); i++) {
            boolean selected = (Boolean) table.getModel().getValueAt(i, 0);
            if (selected) {
                active.add(new String[]{
                            (String) table.getModel().getValueAt(i, 1),
                            (String) table.getModel().getValueAt(i, 2)});
            }
        }
        this.scope.setText(active.size() + " / " + table.getModel().getRowCount());
        this.project = new Project(classPathString, active);
    }

    public void updateClassList() {
        String c = classSearch.getText().trim().equals("") ? "*" : classSearch.getText();
        String m = methodSearch.getText().trim().equals("") ? null : methodSearch.getText();
        final List<JavaClass> result = project.query(c, m);

        ListModel model = new AbstractListModel() {

            @Override
            public int getSize() {
                return result.size();
            }

            @Override
            public Object getElementAt(int index) {
                return result.get(index);
            }
        };
        classList.setModel(model);
        classList.setSelectedIndex(0);
    }

    public void updateMethodList() {
        List<JavaClass> input = new LinkedList<JavaClass>();
        if (showMethodsFromAllClasses.getSelectedObjects() != null) {
            for (int i = 0; i < classList.getModel().getSize(); i++) {
                input.add((JavaClass) classList.getModel().getElementAt(i));
            }
        } else {
            JavaClass selected = (JavaClass) classList.getSelectedValue();
            if (selected != null) {
                input.add(selected);
            }
        }

        final List<MethodInfoContainer> methods = new LinkedList<MethodInfoContainer>();
        Matcher m = null;
        if (filterMethods.getSelectedObjects() != null) {
            m = Pattern.compile(methodSearch.getText()).matcher("");
        }

        for (JavaClass jc : input) {
            for (MethodInfo mi : project.getAllUniqueMethodsForJavaClass(jc, m)) {
                methods.add(new MethodInfoContainer(jc, mi));
            }
        }
        ListModel model = new AbstractListModel() {

            @Override
            public int getSize() {
                return methods.size();
            }

            @Override
            public Object getElementAt(int index) {
                return methods.get(index);
            }
        };
        methodList.setModel(model);
    }

    private void updateDetailsPanel() {
        MethodInfoContainer mi = (MethodInfoContainer) methodList.getSelectedValue();
        if (mi == null)
            return;
        DetailsPanel dp = new DetailsPanel(mi);
        detailsPanel.removeAll();
        detailsPanel.add(dp, BorderLayout.CENTER);
        detailsPanel.validate();
    }

    public boolean isMethodListFiltered() {
        return filterMethods.getSelectedObjects() != null;
    }

    public String getMethodFilter() {
        return methodSearch.getText();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jPopupMenu2 = new javax.swing.JPopupMenu();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        classList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        methodList = new javax.swing.JList();
        filterMethods = new javax.swing.JToggleButton();
        showMethodsFromAllClasses = new javax.swing.JToggleButton();
        detailsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        scope = new javax.swing.JLabel();
        selectScope = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        methodSearch = new javax.swing.JTextField();
        classSearch = new javax.swing.JTextField();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("File");
        jMenuBar2.add(jMenu3);

        jMenu4.setText("Edit");
        jMenuBar2.add(jMenu4);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Result"));

        classList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                classListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(classList);

        methodList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                methodListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(methodList);

        filterMethods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterMethodsActionPerformed(evt);
            }
        });

        showMethodsFromAllClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showMethodsFromAllClassesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(332, Short.MAX_VALUE)
                .addComponent(showMethodsFromAllClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterMethods, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterMethods, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showMethodsFromAllClasses, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
        );

        detailsPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        detailsPanel.setLayout(new java.awt.BorderLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setText("Scope");

        scope.setBackground(new java.awt.Color(255, 255, 255));
        scope.setText("select libraries");
        scope.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        scope.setMaximumSize(new java.awt.Dimension(9, 24));
        scope.setMinimumSize(new java.awt.Dimension(9, 24));
        scope.setOpaque(true);
        scope.setPreferredSize(new java.awt.Dimension(9, 24));

        selectScope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectScopeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scope, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectScope, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(517, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectScope, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(scope, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Search"));

        jLabel2.setText("Class");

        jLabel3.setText("Method");

        methodSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                methodSearchKeyPressed(evt);
            }
        });

        classSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                classSearchKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(methodSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                    .addComponent(classSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(methodSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(detailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(detailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void classSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_classSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            updateClassList();
        }
    }//GEN-LAST:event_classSearchKeyPressed

    private void methodSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_methodSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            updateClassList();
        }
    }//GEN-LAST:event_methodSearchKeyPressed

    private void classListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_classListValueChanged
        updateMethodList();
    }//GEN-LAST:event_classListValueChanged

    private void selectScopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectScopeActionPerformed
        scopeWindow.setVisible(true);
    }//GEN-LAST:event_selectScopeActionPerformed

    private void showMethodsFromAllClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showMethodsFromAllClassesActionPerformed
        updateMethodList();
    }//GEN-LAST:event_showMethodsFromAllClassesActionPerformed

    private void filterMethodsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterMethodsActionPerformed
        updateMethodList();
    }//GEN-LAST:event_filterMethodsActionPerformed

    private void methodListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_methodListValueChanged
        System.out.println("method list value changed");
        updateDetailsPanel();
    }//GEN-LAST:event_methodListValueChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList classList;
    private javax.swing.JTextField classSearch;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JToggleButton filterMethods;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenu2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList methodList;
    private javax.swing.JTextField methodSearch;
    private javax.swing.JLabel scope;
    private javax.swing.JButton selectScope;
    private javax.swing.JToggleButton showMethodsFromAllClasses;
    // End of variables declaration//GEN-END:variables
}
