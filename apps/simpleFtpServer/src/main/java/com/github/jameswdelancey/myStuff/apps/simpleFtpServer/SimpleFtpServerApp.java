package com.github.jameswdelancey.myStuff.apps.simpleFtpServer;

import java.util.List;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.UserFactory;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SimpleFtpServerApp {
    private static final String PROPERTIES_FILE = "users.properties";
    private static class UserProperties {
        private String name;
        private String password;
        private String homeDirectory;
        public UserProperties(String name, String password, String homeDirectory) {
            this.name = name;
            this.password = password;
            this.homeDirectory = homeDirectory;
        }
        public String getName() {
            return name;
        }
        public String getPassword() {
            return password;
        }
        public String getHomeDirectory() {
            return homeDirectory;
        }
    }

    private static UserProperties loadUserPropertiesFromPropertiesFile(String propertiesFile) {
        Properties properties = new Properties();
        InputStream inputStream = SimpleFtpServerApp.class.getClassLoader().getResourceAsStream(propertiesFile);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = properties.getProperty("name");
        String password = properties.getProperty("password");
        String homeDirectory = properties.getProperty("homeDirectory");
        return new UserProperties(name, password, homeDirectory);
    }


    public static void main(String[] args) {

        UserProperties userProperties = loadUserPropertiesFromPropertiesFile(PROPERTIES_FILE);

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2121); // Define your desired port
        serverFactory.addListener("default", factory.createListener());

        UserFactory userFactory = new UserFactory();
        userFactory.setName(userProperties.getName());
        userFactory.setPassword(userProperties.getPassword());
        userFactory.setHomeDirectory(userProperties.getHomeDirectory());
        userFactory.setEnabled(true);
        userFactory.setAuthorities(List.of(new WritePermission()));
        User user = userFactory.createUser();

        UserManager userManager = serverFactory.getUserManager();
        try {
            userManager.save(user);
        } catch (FtpException e) {
            e.printStackTrace();
        }

        FtpServer server = serverFactory.createServer();

        try {
            server.start();
            System.out.println("FTP server started");
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }
}
