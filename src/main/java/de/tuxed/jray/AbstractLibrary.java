package de.tuxed.jray;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.SyntheticRepository;

public abstract class AbstractLibrary {

    private static final Matcher CLASSFILE_MATCHER = Pattern.compile(".*\\$\\d+.class$").matcher("");
    private String name;
    private String path;
    private final List<ClassCache> classInfoList = new ArrayList<ClassCache>();
    private final SyntheticRepository repository;

    public AbstractLibrary(SyntheticRepository repository, String[] lib) {
        this.repository = repository;
        this.name = lib[0];
        this.path = lib[1];
        File source = new File(path);

        System.out.println("Analysing " + path);
        parse();
    }

    protected abstract void parse();

    protected String createClassNameFromPath(String path) {
        String classname = path.replace("/", ".");
        classname = classname.substring(0, classname.length() - 6);
        return classname;
    }

    protected boolean checkIfFileShouldBeParsed(String name) {
        if (!name.endsWith(".class")) {
            return false;
        } else if (CLASSFILE_MATCHER.reset(name).find()) {
            return false;
        }

        return true;
    }

    protected ClassCache createClassInfo(String className) throws ClassNotFoundException {
        JavaClass jc = repository.loadClass(className);

        ClassCache ci = new ClassCache();
        ci.setClassName(jc.getClassName());

        // Create the full super class and interfaces graph
        Set<JavaClass> classGraph = new HashSet<JavaClass>();
        classGraph.add(jc);
        try {
            classGraph.addAll(Arrays.asList(jc.getSuperClasses()));
            classGraph.addAll(Arrays.asList(jc.getAllInterfaces()));
        } catch (Throwable t) {
            System.out.println(t.getMessage());
        }
        
        Set<String> methods = new HashSet<String>();
        for (JavaClass superType : classGraph) {
            for (int i = 0; i < superType.getMethods().length; i++) {
                Method m = superType.getMethods()[i];
                methods.add(m.getName());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String m : methods) {
            sb.append(m + "\n");
        }
        ci.setMethodNames(sb.toString());
        return ci;
    }

    public List<JavaClass> query(String classQuery, String methodQuery) {
        classQuery = classQuery.replace(".", "\\.");
        classQuery = classQuery.replace("*", ".*");
        LinkedList<JavaClass> result = new LinkedList<JavaClass>();
        Pattern p = Pattern.compile(classQuery);
        Matcher m = p.matcher("");

        for (ClassCache ci : classInfoList) {
            if (m.reset(ci.getClassName()).find()) {
                try {
                    if (methodQuery != null) {
                        if (ci.containsMethodWithName(methodQuery)) {
                            result.add(repository.loadClass(ci.getClassName()));
                        }
                    } else {
                        result.add(repository.loadClass(ci.getClassName()));
                    }
                } catch (Throwable t) {
                    System.out.println(t.getMessage());
                }
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public List<ClassCache> getClassInfoList() {
        return classInfoList;
    }
}

