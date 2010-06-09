package de.tuxed.jray;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class FsUtils {

	public static List<String> getAllUniqueJarFiles(String rootDirectory) {
		Set<String> fileNames = new HashSet<String>();
		List<File> tmp = new LinkedList<File>();
		List<String> result = new LinkedList<String>();
		
		getAllJarFiles(rootDirectory, tmp);
		
		for (File f : tmp) {
			if (!fileNames.contains(f.getName())) {
				fileNames.add(f.getName());
				result.add(f.getAbsolutePath());
			}
		}
		
		return result;
	}

	private static void getAllJarFiles(String rootDirectory, List<File> collected) {
		File root = new File(rootDirectory);
		for (File entry : root.listFiles()) {
			if (entry.getAbsolutePath().endsWith(".jar"))
				collected.add(entry);
			else if (entry.isDirectory())
				getAllJarFiles(entry.getAbsolutePath(), collected);
		}
	}

}
