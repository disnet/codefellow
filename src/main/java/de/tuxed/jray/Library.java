package de.tuxed.jray;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.util.SyntheticRepository;

public class Library {

	private final Map<String, ClassInfo> classInfoMap = new HashMap<String, ClassInfo>();
	private final SyntheticRepository repository;

	public Library(SyntheticRepository repository, File source) {
		this.repository = repository;

		if (source.isFile())
			generateClassListFromJar(source);
		else if (source.isDirectory())
			generateClassListFromDirectory(source);
	}

	private void generateClassListFromJar(File source) {
		System.out.println("##### analysing jar file: " + source.getName());
		try {
			JarFile jarFile = new JarFile(source);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (!entry.getName().endsWith(".class"))
					continue;

				String className = entry.getName().replace("/", ".");
				className = className.substring(0, className.length() - 6);
				ClassInfo ci = createClassInfo(className);
				classInfoMap.put(ci.getClassName(), ci);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private List<String> generateClassListFromDirectory(File source) {
		throw new RuntimeException("not implemented yet");
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

		for (JavaClass superType : classGraph) {
			String[] methods = new String[superType.getMethods().length];
			for (int i = 0; i < methods.length; i++) {
				Method m = superType.getMethods()[i];
				String methodInfo = createMethodInfo(m);
				methods[i] = methodInfo;
			}
		}
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
		for (String key : classInfoMap.keySet()) {
			if (m.reset(key).find()) {
				ClassInfo ci = classInfoMap.get(key);
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

}
