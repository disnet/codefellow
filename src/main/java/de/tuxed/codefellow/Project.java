package de.tuxed.codefellow;

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
        Set<JavaClass> result = new HashSet<JavaClass>();
        for (AbstractLibrary l : libraries) {
            List<JavaClass> queryClasses = l.query(classQuery, methodQuery);
            result.addAll(queryClasses);
        }
        return new LinkedList(result);
    }

    public List<MethodInfo> getAllUniqueMethodsForJavaClass(JavaClass jc) {
        return getAllUniqueMethodsForJavaClass(jc, null);
    }

    public List<MethodInfo> getAllUniqueMethodsForJavaClass(JavaClass jc, Matcher matcher) {
        Set<MethodInfo> ms = new HashSet<MethodInfo>();
        ms.addAll(Arrays.asList(MethodInfo.createAllMethodInfosFromClass(jc)));

        try {
            for (JavaClass superClass : jc.getSuperClasses()) {
                ms.addAll(Arrays.asList(MethodInfo.createAllMethodInfosFromClass(superClass)));
            }
            for (JavaClass superInterface : jc.getAllInterfaces()) {
                ms.addAll(Arrays.asList(MethodInfo.createAllMethodInfosFromClass(superInterface)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (matcher != null) {
            Set<MethodInfo> tmp = new HashSet<MethodInfo>(ms);
            for (MethodInfo mi : tmp) {
                if (!matcher.reset(mi.getMethod().getName()).find()) {
                    ms.remove(mi);
                }
            }
        }

        List<MethodInfo> ordered = new LinkedList<MethodInfo>();
        ordered.addAll(ms);
        Collections.sort(ordered, new Comparator<MethodInfo>() {

            @Override
            public int compare(MethodInfo m1, MethodInfo m2) {
                return m1.getMethod().getName().compareTo(m2.getMethod().getName());
            }
        });
        return ordered;
    }
}
