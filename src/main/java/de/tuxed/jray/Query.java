package de.tuxed.jray;

import java.util.LinkedList;
import java.util.List;

public class Query {

	private final Project project;

	public Query(Project project) {
		this.project = project;
	}
	
	public List<ClassInfo> queryByClass(String classQuery) {
		return query(classQuery, null);
	}

	public List<ClassInfo> queryByMethod(String methodQuery) {
		return query("*", methodQuery);
	}

	public List<ClassInfo> query(String classQuery, String methodQuery) {
		List<ClassInfo> result = new LinkedList<ClassInfo>();
		for (Library l : project.getLibraries()) {
			List<ClassInfo> queryClasses = l.query(classQuery, methodQuery);
			result.addAll(queryClasses);
		}

		return result;
	}

}
