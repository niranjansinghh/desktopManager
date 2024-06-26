import java.io.*;
import java.net.*;

public class Server {
    private ObjectInputStream inFromClient;
    private ObjectOutputStream outToClient;

    public Server() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server is running. Waiting for a client...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromClient = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                String request = (String) inFromClient.readObject();

                switch (request) {
                    case "newFolder":
                        createNewFolder(inFromClient);
                        break;
                    case "renameFolder":
                        renameFolder(inFromClient);
                        break;
                    case "fileTransfer":
                        sendFile(inFromClient, outToClient);
                        break;
                    case "help":
                        sendHelp(outToClient);
                        break;
                    default:
                        // Handle unknown requests
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewFolder(ObjectInputStream inFromClient) throws IOException, ClassNotFoundException {
        String folderName = (String) inFromClient.readObject();
        File newFolder = new File(folderName);

        if (newFolder.mkdir()) {
            System.out.println("New folder created: " + folderName);
            // Print the location where the new folder is added
            System.out.println("Location: " + newFolder.getAbsolutePath());
        } else {
            System.out.println("Failed to create a new folder.");
        }
    }

    private void renameFolder(ObjectInputStream inFromClient) throws IOException, ClassNotFoundException {
        String oldName = (String) inFromClient.readObject();
        String newName = (String) inFromClient.readObject();

        File oldFolder = new File(oldName);
        File newFolder = new File(newName);

        if (oldFolder.exists() && oldFolder.isDirectory()) {
            if (oldFolder.renameTo(newFolder)) {
                System.out.println("Folder renamed from " + oldName + " to " + newName);
                // Print the location where folder renaming happens
                System.out.println("Location: " + newFolder.getAbsolutePath());
            } else {
                System.out.println("Failed to rename the folder.");
            }
        } else {
            System.out.println("Folder doesn't exist or is not a directory.");
        }
    }

    private void sendFile(ObjectInputStream inFromClient, ObjectOutputStream outToClient) throws IOException, ClassNotFoundException {
        String fileName = (String) inFromClient.readObject();
        File fileToSend = new File(fileName);

        if (fileToSend.exists() && fileToSend.isFile()) {
            outToClient.writeObject("FileExists");

            try (FileInputStream fileInputStream = new FileInputStream(fileToSend)) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outToClient.write(buffer, 0, bytesRead);
                    outToClient.flush();
                }
            }
            System.out.println("File sent to the client: " + fileName);
            // Print the location where the file is saved
            System.out.println("Location: " + fileToSend.getAbsolutePath());
        } else {
            outToClient.writeObject("FileNotExists");
            System.out.println("File does not exist.");
        }
    }

    private void sendHelp(ObjectOutputStream outToClient) throws IOException {
        String helpMessage = "Available commands:\n" +
                "1. newFolder - Create a new folder (Provide folder name).\n" +
                "2. renameFolder - Rename a folder (Provide old and new folder names).\n" +
                "3. fileTransfer - Transfer a file to the client (Provide file name).\n" +
                "4. help - Display help message.\n";

        outToClient.writeObject(helpMessage);
        System.out.println("Help message sent to the client.");
    }

    public static void main(String[] args) {
        new Server();
    }
}