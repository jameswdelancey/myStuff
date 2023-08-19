package com.github.jameswdelancey.myStuff.apps.simpleWebServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SimpleWebServerApp {

    public class HttpServerHandler extends IoHandlerAdapter {
        private final String rootDirectory = "D:\\"; // Replace with your root directory

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            // Handle HTTP requests here
            String request = message.toString();

            // Check if it's a GET request and extract the requested path
            if (request.startsWith("GET")) {
                String[] requestLines = request.split("\r\n");
                String[] requestParts = requestLines[0].split(" ");
                String requestPath = requestParts[1];

                // Construct the file path based on the root directory and requested path
                String filePath = rootDirectory + requestPath;

                // Check if the requested path is a directory or a file
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        // Generate a directory listing
                        String[] files = file.list();
                        String responsePayload = generateDirectoryListing(requestPath, files);
                        String response = "HTTP/1.1 200 OK\r\nContent-Length: " + responsePayload.length() + "\r\n\r\n"
                                + responsePayload;
                        session.write(response);
                    } else {
                        // Serve the requested file
                        StringBuilder response = new StringBuilder();
                        response.append("HTTP/1.1 200 OK\r\n");
                        response.append("Content-Length: ").append(file.length()).append("\r\n");
                        // Determine the appropriate content type
                        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                            response.append("Content-Type: image/jpeg\r\n\r\n");
                        } else if (filePath.endsWith(".png")) {
                            response.append("Content-Type: image/png\r\n\r\n");
                        } else if (filePath.endsWith(".gif")) {
                            response.append("Content-Type: image/gif\r\n\r\n");
                        } else {
                            response.append("Content-Type: text/html\r\n\r\n");
                        }

                        // Read the file and send it to the client
                        session.write(response.toString());
                        // session.write(new FileRegion(file));
                    }
                } else {
                    // Return a 404 Not Found response
                    String notFoundResponse = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n";
                    session.write(notFoundResponse);
                }
            }
        }

        private String generateDirectoryListing(String requestPath, String[] files) {
            StringBuilder builder = new StringBuilder();
            // remove trailing slash from requestPath
            if (requestPath.endsWith("/")) {
                requestPath = requestPath.substring(0, requestPath.length() - 1);
            }
            builder.append("<html><body><h1>Directory Listing: ").append(requestPath).append("</h1><ul>");

            // Generate links to files and directories
            for (String fileName : files) {
                builder.append("<li><a href=\"").append(requestPath).append("/").append(fileName).append("\">")
                        .append(fileName).append("</a></li>");
            }

            builder.append("</ul></body></html>");
            return builder.toString();
        }

        /**
         * Read the file and returns the byte array
         * 
         * @param file
         * @return the bytes of the file
         */
        private byte[] readFile(String file) {
            ByteArrayOutputStream bos = null;
            try {
                File f = new File(file);
                FileInputStream fis = new FileInputStream(f);
                byte[] buffer = new byte[1024];
                bos = new ByteArrayOutputStream();
                for (int len; (len = fis.read(buffer)) != -1;) {
                    bos.write(buffer, 0, len);
                }
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (IOException e2) {
                System.err.println(e2.getMessage());
            }
            return bos != null ? bos.toByteArray() : null;
        }
    }

    public static void main(String[] args) throws IOException {
        IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory()));
        acceptor.setHandler(new SimpleWebServerApp().new HttpServerHandler());
        acceptor.bind(new InetSocketAddress(8080));
        System.out.println("HTTP server started on port 8080");
    }

}

// import org.apache.mina.core.service.IoHandlerAdapter;
// import org.apache.mina.core.service.IoService;
// import org.apache.mina.core.service.IoServiceListener;
// import org.apache.mina.core.session.IdleStatus;
// import org.apache.mina.core.session.IoSession;
// import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

// import java.io.File;
// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;

// public class SimpleWebServerApp {
// private static final int PORT = 8080; // Change this to the desired port
// private static final String ROOT_DIRECTORY = "D:\\"; // Change this to the
// root directory you want to serve

// public static void main(String[] args) {
// try {
// NioSocketAcceptor acceptor = new NioSocketAcceptor();
// HttpServer server = new HttpServer();

// // Create and set up the HTTP service handler
// ServerHttpHandler httpHandler = new ServerHttpHandler();
// httpHandler.setDefaultResponse(HttpResponseStatus.NOT_FOUND);
// httpHandler.addHttpHandler("/", new RootPageHandler());

// // Set up the file upload handler
// FileUploadHandler fileUploadHandler = new SimpleFileUploadHandler();
// fileUploadHandler.setUploadDir(new File(ROOT_DIRECTORY, "uploads")); //
// Change this to your desired upload
// // directory

// // Create the HTTP service
// ServerHttpService httpService = new ServerHttpService();
// httpService.addHandler(httpHandler);
// httpService.addFileUploadHandler(fileUploadHandler);

// // Create the HTTP server context
// MinaHttpServerContext context = new MinaHttpServerContext();
// context.setService(httpService);

// // Bind the server to the specified port
// acceptor.setHandler(new MinaHttpServiceHandler(context));
// acceptor.bind(PORT);

// System.out.println("Web server is running on port " + PORT);
// } catch (IOException e) {
// e.printStackTrace();
// }
// }

// private static class RootPageHandler implements ServerHttpHandler {
// @Override
// public void handle(MinaHttpServerRequest request, MinaHttpServerResponse
// response) {
// try {
// File rootDir = new File(ROOT_DIRECTORY);
// File[] files = rootDir.listFiles();

// StringBuilder htmlBuilder = new StringBuilder();
// htmlBuilder.append("<html><body>");

// if (files != null) {
// for (File file : files) {
// if (file.isDirectory()) {
// htmlBuilder.append("<a href=\"").append(file.getName()).append("/\">")
// .append(file.getName()).append("/</a><br>");
// } else if (isImageFile(file.getName())) {
// htmlBuilder.append("<a href=\"").append(file.getName()).append("\">")
// .append(file.getName()).append("</a><br>");
// }
// }
// }

// htmlBuilder.append("</body></html>");

// HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
// HttpResponseStatus.OK);
// httpResponse.setContent(htmlBuilder.toString().getBytes());
// response.setStatus(HttpStatus.SUCCESS_OK);
// response.write(httpResponse);

// } catch (Exception e) {
// e.printStackTrace();
// }
// }

// private boolean isImageFile(String fileName) {
// String[] imageExtensions = { ".jpg", ".jpeg", ".png", ".gif" };
// for (String ext : imageExtensions) {
// if (fileName.endsWith(ext)) {
// return true;
// }
// }
// return false;
// }
// }
// }
