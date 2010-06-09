package de.tuxed.jray;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Throwable {

	List<String[]> projects = FsUtils.getAllProjectOutputDirectories(".");
	List<String[]> libs = FsUtils.getAllUniqueJarFiles(".");
	libs.addAll(projects);

	String cp = ".";
	for (String[] lib : libs) {
	    cp += ":" + lib[1];
	}
	Project prj = new Project(cp, projects);
	System.out.println("Project initialized.");

	// TESTS
	Query q = new Query(prj);

	System.out.println("all \"*util*.H\" classes with \"getClass\" method:");
	List<ClassInfo> query = q.query("*util*.H*", "getClass");
	for (ClassInfo classInfo : query) {
	    String c = classInfo.getClassName();
	    System.out.println(c);
	}

	System.out.println();

	System.out.println("all classes with \"send\" method:");
	query = q.queryByMethod("send");
	for (ClassInfo classInfo : query) {
	    String c = classInfo.getClassName();
	    System.out.println(c);
	}

    }

}
