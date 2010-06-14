/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tuxed.codefellow.gui;

import de.tuxed.codefellow.MethodInfo;
import org.apache.bcel.classfile.JavaClass;

/**
 *
 * @author roman
 */
public class MethodInfoContainer {

    private final JavaClass javaClass;
    private final MethodInfo methodInfo;

    public MethodInfoContainer(JavaClass javaClass, MethodInfo methodInfo) {
        this.javaClass = javaClass;
        this.methodInfo = methodInfo;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

}
