package com.github.jameswdelancey.myStuff.apps.processManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class ProcessManagerApp {
    private static final String JSON_FILE = "commands.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static class Command {
        private String command;
        private String name;

        public String getCommand() {
            return command;
        }

        public String getName() {
            return name;
        }
    }

    public static void main(String[] args) {
        try {
            String[] args2 = { "taskkill", "/F", "/IM", "ffmpeg.exe" };
            Process process = Runtime.getRuntime().exec(args2);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        List<Command> commands = null;
        try {
            InputStream inputStream = ProcessManagerApp.class.getClassLoader().getResourceAsStream(JSON_FILE);

            if (inputStream != null) {
                commands = objectMapper.readValue(inputStream,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Command.class));

                for (Command cmd : commands) {
                    System.out.println("Command: " + cmd.getCommand());
                    System.out.println("Name: " + cmd.getName());
                }
            } else {
                System.err.println("Resource not found: " + JSON_FILE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Exit if no commands were found
        if (commands == null) {
            System.err.println("No commands found");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(commands.size());

        for (Command cmd : commands) {
            executor.execute(() -> startAndMonitorProcess(cmd.getCommand(), cmd.getName()));
        }

        executor.shutdown();
    }

    private static void startAndMonitorProcess(String command, String name) {
        while (true) {
            try {
                Process process = Runtime.getRuntime().exec(command.split(" "));
                System.out.println("Started process for %s".formatted(name));
                savePidToFile(process, name);

                process.waitFor();
                System.out.println("Process for %s finished".formatted(name));

                // Wait for 5 seconds before restarting
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void savePidToFile(Process process, String name) {
        long pid = process.pid();
        File pidFile = new File("D:\\%s\\pid\\ffmpeg_%s.pid".formatted(name, name));

        try (FileWriter writer = new FileWriter(pidFile)) {
            writer.write(Long.toString(pid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
