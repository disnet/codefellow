package de.tuxed.jray;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FsUtils {

    public static List<String[]> getAllUniqueJarFiles(String rootDirectory) {
        Set<String> fileNames = new HashSet<String>();
        List<File> tmp = new LinkedList<File>();
        List<String[]> result = new LinkedList<String[]>();

        findRecursively(rootDirectory, tmp, new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.getAbsolutePath().endsWith(".jar");
            }
        });

        for (File f : tmp) {
            if (!fileNames.contains(f.getName())) {
                fileNames.add(f.getName());
                result.add(new String[]{f.getName(), f.getAbsolutePath()});
            }
        }

        return result;
    }

    public static List<String[]> getAllProjectOutputDirectories(String rootDirectory) {
        List<File> tmp = new LinkedList<File>();
        List<String[]> result = new LinkedList<String[]>();
        final Matcher matcher = Pattern.compile(".*/(.*)/target/(.*classes)$").matcher("");

        findRecursively(rootDirectory, tmp, new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    return false;
                }

                String p = f.getAbsolutePath();
                boolean isOutputDir = matcher.reset(p).find();
                if (isOutputDir) {
                    return true;
                }

                return false;
            }
        });

        for (File f : tmp) {
            matcher.reset(f.getAbsolutePath()).find();
            String description = matcher.group(1) + "/" + matcher.group(2);
            result.add(new String[]{description, f.getAbsolutePath()});
        }
        return result;
    }

    public static void findRecursively(String rootDirectory, List<File> collected, FileFilter filter) {
        File root = new File(rootDirectory);
        for (File entry : root.listFiles()) {
            if (filter.accept(entry)) {
                collected.add(entry);
            } else if (entry.isDirectory()) {
                findRecursively(entry.getAbsolutePath(), collected, filter);
            }
        }
    }
}
