package de.tuxed.jray;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

public class Project {

	private List<Library> libraries = new LinkedList<Library>();

	public Project(List<String> classPathEntries) {
		// Construct classpath to create a BCEL Repository
		String classpath = ".";
		for (String cpe : classPathEntries)
			classpath += ":" + cpe;
		SyntheticRepository repository = SyntheticRepository.getInstance(new ClassPath(classpath));

		// Create Library instances
		for (String cpe : classPathEntries) {
			File libSource = new File(cpe);
			Library lib = new Library(repository, libSource);
			libraries.add(lib);
		}
	}

	public List<Library> getLibraries() {
		return libraries;
	}

}
