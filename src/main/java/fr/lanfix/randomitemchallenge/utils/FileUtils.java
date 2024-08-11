package fr.lanfix.randomitemchallenge.utils;

import java.io.FilenameFilter;

public class FileUtils {

    public static FilenameFilter endsWithFilenameFilter(String end) {
        return (dir, name) -> name.endsWith(end);
    }

}
