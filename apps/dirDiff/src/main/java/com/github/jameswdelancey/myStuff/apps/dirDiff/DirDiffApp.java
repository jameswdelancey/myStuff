package com.github.jameswdelancey.myStuff.apps.dirDiff;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DirDiffApp {
    public static void main(String[] args) {
        // TODO: Replace these with the directories you want to compare
        String baseDir1 = "/path/to/first/directory";
        String baseDir2 = "/path/to/second/directory";

        List<File> files1 = listFilesRecursive(new File(baseDir1));
        List<File> files2 = listFilesRecursive(new File(baseDir2));

        compareDirectories(files1, files2);
    }

    // Recursively list all files in a directory and its subdirectories
    public static List<File> listFilesRecursive(File directory) {
        List<File> fileList = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(listFilesRecursive(file));
                } else {
                    // TODO: Convert dir to relative path
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }

    // Compare files based on name and size
    public static void compareDirectories(List<File> files1, List<File> files2) {
        for (File file1 : files1) {
            for (File file2 : files2) {
                if (file1.getName().equals(file2.getName()) && file1.length() == file2.length()) {
                    System.out.println("File found in both directories: " + file1.getAbsolutePath());
                }
            }
        }
    }
}
