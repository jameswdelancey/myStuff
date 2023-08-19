package com.github.jameswdelancey.myStuff.apps.fileWatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class FileWatcherApp {
    // Constant threshold for maximum age of a file (in milliseconds)
    // private static final long MAX_FILE_AGE = 86400000; // 24 hours
    private static final long MAX_FILE_AGE = 1800L;
    private static final int SLEEP_TIME_BETWEEN_RUNS = 10 * 60 * 1000;
    private static final String[][] DIRECTORES_AND_COMMANDS = {
            { "D:\\cam0\\ts", "taskkill /F /PID %d", "D:\\cam0\\pid\\ffmpeg_cam0.pid" },
            { "D:\\cam1\\ts", "taskkill /F /PID %d", "D:\\cam1\\pid\\ffmpeg_cam1.pid" },
            { "D:\\cam2\\ts", "taskkill /F /PID %d", "D:\\cam2\\pid\\ffmpeg_cam2.pid" },
            { "D:\\cam3\\ts", "taskkill /F /PID %d", "D:\\cam3\\pid\\ffmpeg_cam3.pid" },
            { "D:\\cam4\\ts", "taskkill /F /PID %d", "D:\\cam4\\pid\\ffmpeg_cam4.pid" },
            { "D:\\cam5\\ts", "taskkill /F /PID %d", "D:\\cam5\\pid\\ffmpeg_cam5.pid" },
            { "D:\\cam6\\ts", "taskkill /F /PID %d", "D:\\cam6\\pid\\ffmpeg_cam6.pid" },
            { "D:\\cam7\\ts", "taskkill /F /PID %d", "D:\\cam7\\pid\\ffmpeg_cam7.pid" },
    };

    public static void main(String[] args) {
        new FileWatcherApp();
    }

    public FileWatcherApp() {
        Timer timer = new Timer();
        timer.schedule(new FileCheckTask(), 0, SLEEP_TIME_BETWEEN_RUNS);
    }

    private class FileCheckTask extends TimerTask {
        @Override
        public void run() {

            for (String[] dirAndCmd : DIRECTORES_AND_COMMANDS) {
                String directoryPath = dirAndCmd[0];
                String command = dirAndCmd[1];
                String pidFilePath = dirAndCmd[2];

                processFilesInDirectory(directoryPath, command, pidFilePath);
            }
        }

        public String readAndTrimFileContent(String filePath) {
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }

        private void processFilesInDirectory(String directoryPath, String command, String pidFilePath) {
            File directory = new File(directoryPath);

            if (!directory.isDirectory()) {
                System.out.println("Invalid directory: " + directoryPath);
                return;
            }

            File[] files = directory.listFiles();
            File newestFile = null;

            for (File file : files) {
                if (file.isFile() && (newestFile == null || file.lastModified() > newestFile.lastModified())) {
                    newestFile = file;
                }
            }

            if (newestFile != null) {
                long currentTime = System.currentTimeMillis();
                long fileAge = currentTime - newestFile.lastModified();

                if (fileAge > MAX_FILE_AGE) {
                    System.out.println("Running command for directory: " + directoryPath);
                    try {
                        String pid = readAndTrimFileContent(pidFilePath);
                        command = String.format(command, Integer.parseInt(pid));
                        Process process = Runtime.getRuntime().exec(command, null, directory);
                        int exitCode = process.waitFor();
                        System.out.println("Command exited with code: " + exitCode);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Newest file is within threshold for directory: " + directoryPath);
                }
            } else {
                System.out.println("No files found in directory: " + directoryPath);
            }
        }
    }
}
