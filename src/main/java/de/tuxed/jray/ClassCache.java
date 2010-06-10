package de.tuxed.jray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassCache {

    private String className = "";
    private String methodNames = "";

    public boolean containsMethodWithName(String methodName) {
        methodName = methodName.replace("*", ".*");
        Pattern p = Pattern.compile(methodName, Pattern.MULTILINE);
        Matcher m = p.matcher(methodNames);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        this.className = name;
    }

    public String getMethodNames() {
        return methodNames;
    }

    public void setMethodNames(String methodNames) {
        this.methodNames = methodNames;
    }
}
