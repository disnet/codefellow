package de.tuxed.jray;

import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Throwable {
		
		List<String> l = FsUtils.getAllUniqueJarFiles(".");
		for (String s : l) {
			System.out.println(s);
		}
		

		Project prj = new Project(l);
		System.out.println("Project initialized.");
//
//		// TESTS
//		Query q = new Query(prj);
//
//		System.out.println("all \"*util*.N\" classes with \"getClass\" method:");
//		List<ClassInfo> query = q.query(Arrays.asList("scala", "wicket"), "*util*.N*", "getClass");
//		for (ClassInfo classInfo : query) {
//			String c = classInfo.getClassName();
//			System.out.println(c);
//		}
//
//		System.out.println();
//
//		System.out.println("all classes with \"applyTo\" method:");
//		query = q.queryByMethod(Arrays.asList("scala", "wicket"), "applyTo");
//		for (ClassInfo classInfo : query) {
//			String c = classInfo.getClassName();
//			System.out.println(c);
//		}

	}

}
