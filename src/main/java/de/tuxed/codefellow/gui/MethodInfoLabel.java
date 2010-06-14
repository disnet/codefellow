package de.tuxed.codefellow.gui;

import de.tuxed.codefellow.MethodInfo;
import java.awt.Font;

import javax.swing.JLabel;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

class MethodInfoLabel extends JLabel  {

    public MethodInfoLabel(MethodInfo methodInfo) {
        setFont(new Font("Arial", Font.PLAIN, 12));
        setText(getHtml(methodInfo));
    }

    private String getHtml(MethodInfo mi) {
        Method m = mi.getMethod();

        // Name
        String mn = m.getName();
        mn = decodeScalaMethodNames(mn);
        mn = mn.replace("<", "&lt;");
        mn = mn.replace(">", "&gt;");
        mn = mn.replace("%", "&#37;");
        String name = "<b>" + mn + "</b>";

        // Return type
        String returnType = m.getReturnType().toString();

        // Arguments
        StringBuilder args = new StringBuilder("(");
        for (int i = 0; i < m.getArgumentTypes().length; i++) {
            Type a = m.getArgumentTypes()[i];
            args.append(a.toString());
            if (!((i + 1) == m.getArgumentTypes().length)) {
                args.append(", ");
            }
        }
        args.append(")");

        // Access
        //String access = Utility.accessToString(m.getAccessFlags());

        // Class
        String c = "<font color='#c68200'>" + mi.getJavaClass().getClassName() + "</font>";

        return "<html>" + name + args + ": " + returnType + " " + c + "</html>";

    }

    private String decodeScalaMethodNames(String mn) {
        mn = mn.replace("$tilde", "~");
        mn = mn.replace("$eq", "=");
        mn = mn.replace("$less", "<");
        mn = mn.replace("$greater", ">");
        mn = mn.replace("$bang", "!");
        mn = mn.replace("$hash", "#");
        mn = mn.replace("$percent", "%");
        mn = mn.replace("$up", "^");
        mn = mn.replace("$amp", "&");
        mn = mn.replace("$bar", "|");
        mn = mn.replace("$times", "*");
        mn = mn.replace("$div", "/");
        mn = mn.replace("$plus", "+");
        mn = mn.replace("$minus", "-");
        mn = mn.replace("$colon", ":");
        mn = mn.replace("$bslash", "\\");
        mn = mn.replace("$qmark", "?");
        mn = mn.replace("$at", "@");
        return mn;
    }
}
