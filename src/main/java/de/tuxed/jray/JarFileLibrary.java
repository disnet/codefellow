package de.tuxed.jray;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.util.SyntheticRepository;

public class JarFileLibrary extends AbstractLibrary {

    public JarFileLibrary(SyntheticRepository repository, String[] lib) {
        super(repository, lib);
    }

    @Override
    protected void parse() {
        File source = new File(getPath());
        try {
            JarFile jarFile = new JarFile(source);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!checkIfFileShouldBeParsed(entry.getName())) {
                    continue;
                }

                String className = createClassNameFromPath(entry.getName());
                ClassCache ci = createClassInfo(className);
                getClassInfoList().add(ci);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
