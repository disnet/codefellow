package de.tuxed.jray;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;

import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

public class Project {

    private List<AbstractLibrary> libraries = new LinkedList<AbstractLibrary>();
    private final SyntheticRepository repository;

    public Project(String classPath, List<String[]> active) {
        // Construct classpath to create a BCEL Repository
        repository = SyntheticRepository.getInstance(new ClassPath(classPath));

        // Create Library instances
        for (String[] a : active) {
            String path = a[1];
            if (path.endsWith((".jar"))) {
                libraries.add(new JarFileLibrary(repository, a));
            } else if (new File(path).isDirectory()) {
                libraries.add(new ByteCodeDirectoryLibrary(repository, a));
            }
        }
    }

    public List<JavaClass> query(String classQuery, String methodQuery) {
        List<JavaClass> result = new LinkedList<JavaClass>();
        for (AbstractLibrary l : libraries) {
            List<JavaClass> queryClasses = l.query(classQuery, methodQuery);
            result.addAll(queryClasses);
        }
        return result;
    }

    public List<Method> getAllUniqueMethodsForJavaClass(JavaClass jc) {
        return getAllUniqueMethodsForJavaClass(jc, null);
    }

    public List<Method> getAllUniqueMethodsForJavaClass(JavaClass jc, Matcher matcher) {
        Set<Method> ms = new HashSet<Method>();
        ms.addAll(Arrays.asList(jc.getMethods()));

        try {
            for (JavaClass superClass : jc.getSuperClasses()) {
                ms.addAll(Arrays.asList(superClass.getMethods()));
            }
            for (JavaClass superInterface : jc.getAllInterfaces()) {
                ms.addAll(Arrays.asList(superInterface.getMethods()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (matcher != null) {
            Set<Method> tmp = new HashSet<Method>(ms);
            for (Method m : tmp) {
                if (!matcher.reset(m.getName()).find()) {
                    ms.remove(m);
                }
            }
        }

        List<Method> ordered = new LinkedList<Method>();
        ordered.addAll(ms);
        Collections.sort(ordered, new Comparator<Method>() {

            @Override
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });
        return ordered;
    }
}
