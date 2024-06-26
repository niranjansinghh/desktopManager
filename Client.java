import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private ObjectOutputStream outToServer;
    private ObjectInputStream inFromServer;
    private JTextArea textArea;

    public Client() {
        setTitle("Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);

        try {
            Socket socket = new Socket("localhost", 12345);
            outToServer = new ObjectOutputStream(socket.getOutputStream());
            inFromServer = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add buttons and action listeners
        JButton newFolderButton = new JButton("New Folder");
        newFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest("newFolder");
            }
        });

        JButton renameFolderButton = new JButton("Rename Folder");
        renameFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest("renameFolder");
            }
        });

        JButton fileTransferButton = new JButton("File Transfer");
        fileTransferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest("fileTransfer");
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(newFolderButton);
        buttonPanel.add(renameFolderButton);
        buttonPanel.add(fileTransferButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void sendRequest(String request) {
        try {
            outToServer.writeObject(request);

            switch (request) {
                case "newFolder":
                    String folderName = JOptionPane.showInputDialog("Enter folder name:");
                    outToServer.writeObject(folderName);
                    break;
                case "renameFolder":
                    String oldName = JOptionPane.showInputDialog("Enter old folder name:");
                    String newName = JOptionPane.showInputDialog("Enter new folder name:");
                    outToServer.writeObject(oldName);
                    outToServer.writeObject(newName);
                    break;
                case "fileTransfer":
                    String fileName = JOptionPane.showInputDialog("Enter file name:");
                    outToServer.writeObject(fileName);
                    receiveFile();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            String response = (String) inFromServer.readObject();

            if (response.equals("FileExists")) {
                JFileChooser fileChooser = new JFileChooser();
                int userChoice = fileChooser.showSaveDialog(this);

                if (userChoice == JFileChooser.APPROVE_OPTION) {
                    File receivedFile = fileChooser.getSelectedFile();
                    try (FileOutputStream fileOutputStream = new FileOutputStream(receivedFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = inFromServer.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        textArea.append("File received: " + receivedFile.getAbsolutePath() + "\n");
                    }
                }
            } else if (response.equals("FileNotExists")) {
                textArea.append("File does not exist on the server.\n");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client().setVisible(true);
            }
        });
    }
}