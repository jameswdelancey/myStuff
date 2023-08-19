package com.github.jameswdelancey.myStuff.apps.fileProcessor;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileProcessorApp {
    // Schedule the task to run every 11 h
    private static final int SLEEP_TIME_BETWEEN_RUNS = 11 * 60 * 60 * 1000;

    public static void main(String[] args) {
        new FileProcessorApp();
    }

    public FileProcessorApp() {
        Timer timer = new Timer();
        timer.schedule(new FileCheckTask(), 1, SLEEP_TIME_BETWEEN_RUNS);
    }

    private class FileCheckTask extends TimerTask {
        private static final String BASE_PATH = "D:\\ftproot"; // Change this to your source path
        // private static final List<String> DENY_LIST = Arrays.asList(
        // "/path/to/deny/file1.txt",
        // "/path/to/deny/file2.txt"
        // // Add more files to the deny list if needed
        // );
        private static final String DESTINATION_PATH = "D:\\still_images"; // Change this to your destination path

        @Override
        public void run() {
            // FileProcessorApp fileProcessor = new FileProcessorApp();
            try {
                List<Path> directories = createDirectoriesList(BASE_PATH);
                removeDeniedFiles(directories);
                deleteEmptyDirectories(BASE_PATH);
                moveFilesToDestination(directories);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private List<Path> createDirectoriesList(String path) throws IOException {
            List<Path> directories = new ArrayList<>();
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    directories.add(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            Collections.reverse(directories); // Reverse to process deepest directories first
            return directories;
        }

        private void removeDeniedFiles(List<Path> directories) {
            for (Path directory : directories) {
                try {
                    Files.walk(directory)
                            .filter(Files::isRegularFile)
                    // .forEach(file -> {
                    // if (DENY_LIST.contains(file.toString())) {
                    // try {
                    // Files.delete(file);
                    // } catch (IOException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // })
                    ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void deleteEmptyDirectories(String path) throws IOException {
            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (Files.isDirectory(dir) && Files.list(dir).count() == 0) {
                        System.out.println("Deleting empty directory: " + dir);
                        try {
                            Files.delete(dir);
                        } catch (java.nio.file.AccessDeniedException e) {
                            System.out.println("Access denied: " + dir);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        private void moveFilesToDestination(List<Path> directories) throws IOException {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            final int[] filesMoved = { 0 };
            for (Path directory : directories) {
                Files.walk(directory)
                        .filter(Files::isRegularFile)
                        // extension is mp4
                        .filter(file -> file.getFileName().toString().endsWith(".mp4"))
                        .forEach(file -> {
                            try {
                                String filename = file.getFileName().toString();
                                String extension = filename.substring(filename.lastIndexOf(".") + 1);
                                long creationTime = Files.readAttributes(file, BasicFileAttributes.class).creationTime()
                                        .toMillis();
                                String timestamp = dateFormat.format(new Date(creationTime));

                                Path destination = Paths.get(DESTINATION_PATH, timestamp + "." + extension);
                                int version = 1;
                                while (Files.exists(destination)) {
                                    destination = Paths.get(DESTINATION_PATH,
                                            timestamp + "_" + version + "." + extension);
                                    version++;
                                }

                                Files.move(file, destination);
                                filesMoved[0]++;
                            } catch (java.nio.file.AccessDeniedException e) {
                                System.out.println("Access denied: " + e);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            System.out.println("Moved " + filesMoved[0] + " files.");
        }
    }
}
