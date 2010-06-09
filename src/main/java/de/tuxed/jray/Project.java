package de.tuxed.jray;

import java.util.LinkedList;
import java.util.List;

import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;

public class Project {

    private List<Library> libraries = new LinkedList<Library>();

    public Project(String classPath, List<String[]> active) {
	// Construct classpath to create a BCEL Repository
	SyntheticRepository repository = SyntheticRepository.getInstance(new ClassPath(classPath));

	// Create Library instances
	for (String[] a : active) {
	    Library lib = new Library(repository, a);
	    libraries.add(lib);
	}
    }

    public List<Library> getLibraries() {
	return libraries;
    }

}
