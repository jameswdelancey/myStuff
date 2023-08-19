package com.github.jameswdelancey.myStuff.apps.fileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FileManagerApp {
    // Schedule the task to run every 10 h
    private static final int SLEEP_TIME_BETWEEN_RUNS = 10 * 60 * 60 * 1000;
    private static final String DIRECTORY_PATH_CAM0 = "D:\\cam0\\ts";
    private static final String DIRECTORY_PATH_CAM1 = "D:\\cam1\\ts";
    private static final String DIRECTORY_PATH_CAM2 = "D:\\cam2\\ts";
    private static final String DIRECTORY_PATH_CAM3 = "D:\\cam3\\ts";
    private static final String DIRECTORY_PATH_CAM4 = "D:\\cam4\\ts";
    private static final String DIRECTORY_PATH_CAM5 = "D:\\cam5\\ts";
    private static final String DIRECTORY_PATH_CAM6 = "D:\\cam6\\ts";
    private static final String DIRECTORY_PATH_CAM7 = "D:\\cam7\\ts";
    private static final String DIRECTORY_PATH_STILL_IMAGES = "D:\\still_images";

    private static final double MIN_FREE_SPACE_PERCENTAGE = 0.25; // 20%
    private static final int DELETE_BATCH_SIZE = 100;
    private static final String DRIVE_PATH_FREESPACE_CHECKABLE = "D:\\";

    private List<File> files = new ArrayList<>();

    public FileManagerApp() {
        Timer timer = new Timer();
        timer.schedule(new FileCheckTask(), 0, SLEEP_TIME_BETWEEN_RUNS);
    }

    private void updateFileList() {
        files.clear();
        File[] cam0Files = new File(DIRECTORY_PATH_CAM0).listFiles();
        File[] cam1Files = new File(DIRECTORY_PATH_CAM1).listFiles();
        File[] cam2Files = new File(DIRECTORY_PATH_CAM2).listFiles();
        File[] cam3Files = new File(DIRECTORY_PATH_CAM3).listFiles();
        File[] cam4Files = new File(DIRECTORY_PATH_CAM4).listFiles();
        File[] cam5Files = new File(DIRECTORY_PATH_CAM5).listFiles();
        File[] cam6Files = new File(DIRECTORY_PATH_CAM6).listFiles();
        File[] cam7Files = new File(DIRECTORY_PATH_CAM7).listFiles();
        File[] camStillImageFiles = new File(DIRECTORY_PATH_STILL_IMAGES).listFiles();

        if (cam0Files != null) {
            files.addAll(Arrays.asList(cam0Files));
        }
        if (cam1Files != null) {
            files.addAll(Arrays.asList(cam1Files));
        }
        if (cam2Files != null) {
            files.addAll(Arrays.asList(cam2Files));
        }
        if (cam3Files != null) {
            files.addAll(Arrays.asList(cam3Files));
        }
        if (cam4Files != null) {
            files.addAll(Arrays.asList(cam4Files));
        }
        if (cam5Files != null) {
            files.addAll(Arrays.asList(cam5Files));
        }
        if (cam6Files != null) {
            files.addAll(Arrays.asList(cam6Files));
        }
        if (cam7Files != null) {
            files.addAll(Arrays.asList(cam7Files));
        }
        if (camStillImageFiles != null) {
            files.addAll(Arrays.asList(camStillImageFiles));
        }

        files.sort(Comparator.comparingLong(File::lastModified));
    }

    private void deleteFilesBatch(List<File> fileList) {
        for (int i = 0; i < DELETE_BATCH_SIZE && 0 < fileList.size(); i++) {
            File file = fileList.remove(0);
            if (file.delete()) {
                System.out.println("Deleted file: " + file.getName());
            } else {
                System.err.println("Failed to delete file: " + file.getName());
            }
        }
    }

    private class FileCheckTask extends TimerTask {
        @Override
        public void run() {
            if (files.isEmpty()) {
                updateFileList();
            }

            long totalSpace = new File(DRIVE_PATH_FREESPACE_CHECKABLE).getTotalSpace(); // Total available space
            long freeSpace = new File(DRIVE_PATH_FREESPACE_CHECKABLE).getFreeSpace(); // Free space
            double freeSpacePercentage = (double) freeSpace / totalSpace;

            while (freeSpacePercentage < MIN_FREE_SPACE_PERCENTAGE && !files.isEmpty()) {
                System.out.println("Low free space detected. Deleting files...");
                deleteFilesBatch(files);

                totalSpace = new File(DRIVE_PATH_FREESPACE_CHECKABLE).getTotalSpace(); // Total available space
                freeSpace = new File(DRIVE_PATH_FREESPACE_CHECKABLE).getFreeSpace(); // Free space
                freeSpacePercentage = (double) freeSpace / totalSpace;
            }

            System.out.println("Free space is sufficient or files are empty." + " Free space percentage: "
                    + freeSpacePercentage + " Files size: " + files.size());
        }
    }

    public static void main(String[] args) {
        new FileManagerApp();
    }
}
