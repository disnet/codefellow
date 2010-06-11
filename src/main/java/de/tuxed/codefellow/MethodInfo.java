
package de.tuxed.codefellow;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

/**
 * A simple wrapper to store the class information
 */
public class MethodInfo {
    
    public static MethodInfo[] getMethodInfosFromClass(JavaClass javaClass) {
        MethodInfo[] result = new MethodInfo[javaClass.getMethods().length];
        for (int i = 0; i < result.length; i++) {
            Method m = javaClass.getMethods()[i];
            result[i] = new MethodInfo(javaClass, m);
        }
        return result;
    }

    private final JavaClass javaClass;
    private final Method method;

    public MethodInfo(JavaClass javaClass, Method method) {
        this.javaClass = javaClass;
        this.method = method;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public Method getMethod() {
        return method;
    }

}
