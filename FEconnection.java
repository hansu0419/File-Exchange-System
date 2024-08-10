import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FEconnection {
    private Socket socket;
    private DataInputStream in;
    private static DataOutputStream out;


    public FEconnection(Socket socket) {
        this.socket = socket;
        try{
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void storeFile (boolean storeORget) {
        if (storeORget == true){
        try {
        // Receive the filename from the client
        String filename = receiveString();
        System.out.println("Received filename: " + filename);

        // Receive the file size from the client
        long fileSize = in.readLong();
        System.out.println("Received file size: " + fileSize + " bytes");

        // Receive the file content from the client
        byte[] fileContent = new byte[(int) fileSize];
        in.readFully(fileContent);
        System.out.println("Received file content.");

        // Save the file to the server directory
        Path filePath = Paths.get(FEserver.FILES_PATH, filename);
        Files.write(filePath, fileContent, StandardOpenOption.CREATE);
        System.out.println("File stored in server directory: " + filePath);

        // Notify the client about successful file storage
        sendString("File stored successfully!");

    } catch (IOException e) {
        // e.printStackTrace();
    }
    }else if (storeORget == false){
        try {
        // Receive the filename from the client
        String filename = receiveString();
        System.out.println("Received filename: " + filename);

        // Receive the file size from the client
        long fileSize = in.readLong();
        System.out.println("Received file size: " + fileSize + " bytes");

        // Receive the file content from the client
        byte[] fileContent = new byte[(int) fileSize];
        in.readFully(fileContent);
        System.out.println("Received file content.");

        // Save the file to the server directory
        Path filePath = Paths.get(FEclient.CLIENT_PATH, filename);
        Files.write(filePath, fileContent, StandardOpenOption.CREATE);
        System.out.println("File stored in server directory: " + filePath);

        // Notify the client about successful file storage
        sendString("File stored successfully!");

    } catch (IOException e) {
        // e.printStackTrace();
    }
    }
}    
    public void handleCommands() {
         try {
            while (true) {
                String command = receiveString();
                switch (command){
                    case "/store": storeFile(true);
                    case "/get": storeFile(false);
                    case "/dir": printFileNamesAndTypes();
                    case "/leave": break;
                    default:sendString("Invalid command. Try again.");
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public void sendFile() {
        try{
            sendMenu();
            int index = getSelectedFileIndex();
            sendSelectedFile(index);
        } catch (IOException e) {
            
        }
    }

    private void sendSelectedFile(int index) throws IOException {
        File[] fileList = new File(FEserver.FILES_PATH).listFiles();
        File selectedFile = fileList[index];
        List<String> fileLines = Files.readAllLines(selectedFile.toPath());
        String fileContent = String.join("\n",fileLines);
        out.writeUTF(fileContent);
    }

    private int getSelectedFileIndex() throws IOException {
        String input = in.readUTF();
        return Integer.parseInt(input)-1;
    }

    private void sendMenu() throws IOException {
        String menu = "** Files **\n";
        File[] fileList = new File(FEserver.FILES_PATH).listFiles();
        out.writeUTF(""+fileList.length);

        for (int i = 0; i < fileList.length; i++) {
            menu += String.format("* %d - %s\n",i+1, fileList[i].getName());
        }
        out.writeUTF(menu);
    }

    public String receiveString() throws IOException {
        return in.readUTF();
    }

    public void sendString(String message) throws IOException {
        out.writeUTF(message);
    }

    public void close() {
        try{
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    private static void printFileNamesAndTypes() {
        List<String> fileNames = new ArrayList<>();
    
        try {
            Files.list(Paths.get(FEserver.FILES_PATH))
                .forEach(path -> {
                    try {
                        fileNames.add(path.getFileName().toString());
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                });
    
            // Assuming "out" is a DataOutputStream
            for (String fileName : fileNames) {
                try {
                    out.writeUTF(fileName);
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
    
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }
    


    
}