package de.tuxed.jray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassInfo {

	private String className = "";

	private String filename = "";

	private String[] methodInfos = null;

	public boolean containsMethodWithName(String methodName) {
		methodName = methodName.replace("*", ".*");
		Pattern p = Pattern.compile(methodName);
		Matcher m = p.matcher("");
		for (String methodInfo : methodInfos) {
			if (m.reset(methodInfo).find()) {
				return true;
			}
		}
		return false;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String name) {
		this.className = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String[] getMethodInfos() {
		return methodInfos;
	}

	public void setMethodInfos(String[] methodInfos) {
		this.methodInfos = methodInfos;
	}

}
