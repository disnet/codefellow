package de.tuxed.jray;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.SyntheticRepository;

public class Library {

    private String name;
    private File source;
    private final List<ClassInfo> classInfoList = new ArrayList<ClassInfo>();
    private final SyntheticRepository repository;

    public Library(SyntheticRepository repository, String[] lib) {
	this.name = lib[0];
	this.source = new File(lib[1]);
	System.out.println("Analysing " + source.getAbsolutePath());
	this.repository = repository;

	if (source.isFile())
	    generateClassListFromJar(source);
	else if (source.isDirectory())
	    generateClassListFromDirectory(source);
    }

    private void generateClassListFromJar(File source) {
	System.out.println("Analysing " + source.getName());
	try {
	    JarFile jarFile = new JarFile(source);
	    Enumeration<JarEntry> entries = jarFile.entries();
	    while (entries.hasMoreElements()) {
		JarEntry entry = entries.nextElement();
		if (!entry.getName().endsWith(".class"))
		    continue;

		String className = createClassNameFromPath(entry.getName());
		ClassInfo ci = createClassInfo(className);
		classInfoList.add(ci);
	    }
	} catch (Throwable t) {
	    throw new RuntimeException(t);
	}
    }

    private void generateClassListFromDirectory(File source) {
	LinkedList<File> found = new LinkedList<File>();
	FsUtils.findRecursively(source.getAbsolutePath(), found, new FileFilter() {
	    public boolean accept(File pathname) {
		return pathname.getAbsolutePath().endsWith(".class");
	    }
	});

	for (File f : found) {
	    String cn = f.getAbsolutePath().substring(source.getAbsolutePath().length() + 1);
	    cn = createClassNameFromPath(cn);
	    ClassInfo ci;
	    try {
		ci = createClassInfo(cn);
		classInfoList.add(ci);
	    } catch (ClassNotFoundException e) {
		System.out.println(e.getMessage());
	    }
	}
    }

    private String createClassNameFromPath(String path) {
	String classname = path.replace("/", ".");
	classname = classname.substring(0, classname.length() - 6);
	return classname;
    }

    private ClassInfo createClassInfo(String className) throws ClassNotFoundException {
	JavaClass jc = repository.loadClass(className);

	ClassInfo ci = new ClassInfo();
	ci.setClassName(jc.getClassName());
	ci.setFilename(jc.getFileName());

	// Create the full super class and interfaces graph
	Set<JavaClass> classGraph = new HashSet<JavaClass>();
	classGraph.add(jc);
	try {
	    classGraph.addAll(Arrays.asList(jc.getSuperClasses()));
	    classGraph.addAll(Arrays.asList(jc.getAllInterfaces()));
	} catch (Throwable t) {
	    System.out.println(t.getMessage());
	}

	List<String> methods = new LinkedList<String>();
	for (JavaClass superType : classGraph) {
	    for (int i = 0; i < superType.getMethods().length; i++) {
		Method m = superType.getMethods()[i];
		String methodInfo = createMethodInfo(m);
		methods.add(methodInfo);
	    }
	}
	String[] array = methods.toArray(new String[0]);
	ci.setMethodInfos(array);
	return ci;
    }

    private String createMethodInfo(Method m) {
	return m.getName() + " " + m.getSignature();
    }

    public List<ClassInfo> query(String classQuery, String methodQuery) {
	classQuery = classQuery.replace(".", "\\.");
	classQuery = classQuery.replace("*", ".*");
	LinkedList<ClassInfo> result = new LinkedList<ClassInfo>();
	Pattern p = Pattern.compile(classQuery);
	Matcher m = p.matcher("");

	for (ClassInfo ci : classInfoList) {
	    if (m.reset(ci.getClassName()).find()) {
		if (methodQuery != null) {
		    if (ci.containsMethodWithName(methodQuery))
			result.add(ci);
		} else {
		    result.add(ci);
		}
	    }
	}
	return result;
    }

    public String getName() {
	return name;
    }

    public File getPath() {
	return source;
    }

}
