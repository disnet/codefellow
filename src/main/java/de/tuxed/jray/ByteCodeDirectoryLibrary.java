package de.tuxed.jray;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import org.apache.bcel.util.SyntheticRepository;

public class ByteCodeDirectoryLibrary extends AbstractLibrary {

    public ByteCodeDirectoryLibrary(SyntheticRepository repository, String[] lib) {
        super(repository, lib);
    }

    @Override
    protected void parse() {
        File source = new File(getPath());
        LinkedList<File> found = new LinkedList<File>();
        FsUtils.findRecursively(source.getAbsolutePath(), found, new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return checkIfFileShouldBeParsed(pathname.getAbsolutePath());
            }
        });

        for (File f : found) {
            String cn = f.getAbsolutePath().substring(source.getAbsolutePath().length() + 1);
            cn = createClassNameFromPath(cn);
            ClassInfo ci;
            try {
                ci = createClassInfo(cn);
                getClassInfoList().add(ci);
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
