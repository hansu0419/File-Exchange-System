import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FEclient{
   
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    public static boolean connection = false;
    public static final String CLIENT_PATH = "CSNETWK MP\\CSNETWK MP\\clientFiles";
    public static String username = null;
    static boolean joined = false;
    static boolean bastin = false;
    private void leaveServer() {
        try {
            // Notify the server that the client is leaving
            out.writeUTF("/leave");

            // Close the connection
            socket.close();
            in.close();
            out.close();
            if (bastin == true) {
                try {
                deleteContents(username);
                bastin = false;
                username = null;
                } catch (IOException e) {
                    System.err.println("An error occurred while reading the file: " + e.getMessage());
                }
                System.out.println("Disconnected from the server.");
                connection = false;
            } 
                
           
        } catch (IOException e) {
            System.out.println("Error: An error occurred while leaving the server - ");
        }
    }

    public static void main (String [] args){
        
        
        int check = 1; //used for knowing there is a username already
        boolean end = true;
        System.out.print("To begin, please enter a command. Enter /? for the list of commands: ");
        Scanner sc = new Scanner(System.in);
        String command = sc.nextLine();
        FEclient client = new FEclient();
    
        while(end) {

            if (command.equals("/?")) {
                System.out.println("Available Commands:\n");
                System.out.println("-----------------------------------------------------------------------------------");
                System.out.println("/join <server_ip_add> <port>    |Connect to the server application");   
                System.out.println("/leave                          |Disconnect to the server application"); 
                System.out.println("/register <handle>              |Register a unique handle or alias"); 
                System.out.println("/store <filename>               |Send file to server"); 
                System.out.println("/dir                            |Request directory file list from a server");
                System.out.println("/get <filename>                 |Fetch a file from a server"); 
                System.out.println("/?                              |Get command help for all input syntax references."); 
                System.out.println("/end                            |End the Application"); 
                System.out.println("-----------------------------------------------------------------------------------");
                
            } else if (command.startsWith("/join ")) {
                    String[] commandParts = command.split("\\s+");
                    String ip_add = commandParts[1];
                    int port = Integer.parseInt(commandParts[2]);
                    client = new FEclient(ip_add, port);               
                    
                

            } else if (command.equals("/leave")) {
                if (connection == true) {
                    client.leaveServer();
                    connection = false;
                    username = null;
                    check = 1;
                    System.out.println("Connection closed. Thank you!");
                } else {
                    System.out.println("Error: Disconnection failed. Please connect to the server first.");
                }
            } else if (command.startsWith("/register ")) {
                username = command.substring("/register ".length());
               
                try {
                    if (joined == false ) {
                        System.out.println("Error: Not connected to Server - join first");
                    } else {
                        boolean found = checkFile(username);
                    if (found) {
                        if (check == 1 && joined == false) {
                            System.out.println("Error: Registration failed. Handle or alias already exists.");
                        } else {
                            System.out.println("Error: Registration failed. Handle or alias already exists.");
                        }
                        
                    } else{
                        if (check == 0) {
                            System.out.println("Error: Registration failed. Handle or alias already exists.");
                        }
                        else if (joined == true){
                            System.out.println("Welcome " + username + "!");
                            check = 0; 
                            bastin = true;
                            addContent(username);
                        }
                        
                    }
                    }
                } catch (IOException e) {
                    System.err.println("An error occurred while reading the file: " + e.getMessage());
                }


           } else if (command.startsWith("/store ")) {
            if (bastin == true) {
               String filePath = command.substring("/store ".length()); 
               client.storeFile(filePath);
            } else {
                System.out.println("Error: Register first!");
            }
        } else if (command.startsWith("/dir")) {
            if (connection == true && bastin == true) {
                client.getDir();
            } else {
            System.out.println("Error: Directory failed. Please connect to the server first.");
            }
            
        } else if (command.startsWith("/get ")) {
            if (bastin == true) {
            String fileName = command.substring("/get ".length());
            client.getFile(fileName);
            } else {
                System.out.println("Error: Register first");
            }
        }else if (command.startsWith("/end")) {
            end = true;
            break;
        }else {
                System.out.println("Error: Command not found.");
                System.out.print("To begin, please enter a command. Enter /? for the list of commands: ");
                command = sc.nextLine();
        }
        System.out.print("To begin, please enter a command. Enter /? for the list of commands: ");
        command = sc.nextLine();
        }

        try {
            deleteContents(username);
        } catch (IOException e) {
            System.err.println("An error occurred while reading the file: " + e.getMessage());
        }
        sc.close();
    }

    private boolean isConnected() {
        return connection;
    }

    public void storeFile(String fileName) {
        try {
        if (!isConnected()) {
            System.out.println("Error: Not connected to the server. Please connect first.");
            return;
        }

        String filePath = CLIENT_PATH + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Error: The specified file does not exist.");
            return;
        }

        // Notify the server that the client wants to store a file
        out.writeUTF("/store");

        // Send the file name to the server
        out.writeUTF(fileName);

        // Read the file content into a byte array
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Send the file size to the server
        out.writeLong(fileBytes.length);

        // Send the file content to the server
        out.write(fileBytes);
        out.flush();

        System.out.println("File '" + fileName + "' sent to the server successfully.");
        socket.close();
        in.close();
        out.close();
        socket = new Socket("127.0.0.1", 12345);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
        System.err.println("Error: An error occurred while sending the file - " + e.getMessage());
    }
    
}
public void getFile(String fileName) {
        try {
        if (!isConnected()) {
            System.out.println("Error: Not connected to the server. Please connect first.");
            return;
        }

        // Check if the file exists
        String filePath = FEserver.FILES_PATH + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("Error: The specified file does not exist.");
            return;
        }

        // Notify the server that the client wants to store a file
        out.writeUTF("/get");

        // Send the file name to the server
        out.writeUTF(fileName);

        // Read the file content into a byte array
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        // Send the file size to the server
        out.writeLong(fileBytes.length);

        // Send the file content to the server
        out.write(fileBytes);
        out.flush();

        System.out.println("File '" + fileName + "' sent to the client successfully.");
        socket.close();
        in.close();
        out.close();
        socket = new Socket("127.0.0.1", 12345);
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(socket.getOutputStream());
    } catch (IOException e) {
        System.err.println("Error: An error occurred while sending the file - " + e.getMessage());
    }
    
}
    public FEclient(){

    }
    public FEclient(String ip_add, int port) {
        if (!connection){
        try {
            if (port < 0 || port > 65535) {
                System.err.println("Error: Invalid port number. Please use a port between 0 and 65535.");
                return;
            }
            if (!ip_add.equals("127.0.0.1")) {
                System.err.println("Error: Incorrect IP.");
                return;
            }
            socket = new Socket(ip_add, port);
            System.out.println("Connection to the File Exchange Server is successful!");
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            connection = true;
            joined = true;
    
        } catch (UnknownHostException e) {
            System.err.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
        } catch (IOException e) {
            if (e instanceof java.net.ConnectException && e.getMessage().contains("Connection refused")) {
                System.err.println("Error: Connection refused. Please check if the server is running and the port is correct.");
            } else {
                System.err.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
                e.printStackTrace();
            }
        }
        }else{
             System.err.println("Error: You are already connected to a server");
        }
    }

    private static boolean checkFile(String searchText) throws IOException {
        Path path = Paths.get("CSNETWK MP\\CSNETWK MP\\userList.txt");

        List<String> lines = Files.readAllLines(path);

        boolean found = lines.stream().anyMatch(line -> line.contains(searchText));

        if (!found) {
            lines.add(searchText);
        }

        return found;
    }

    private static void addContent(String searchText) throws IOException {
        Path path = Paths.get("CSNETWK MP\\CSNETWK MP\\userList.txt");

        List<String> lines = Files.readAllLines(path);
            lines.add(searchText);
            Files.write(path, lines, StandardOpenOption.WRITE);
    }

    private static boolean deleteContents(String searchText) throws IOException {
        Path path = Paths.get("CSNETWK MP\\CSNETWK MP\\userList.txt");

        // Read all lines from the file
        List<String> lines = Files.readAllLines(path);

        // Identify the index of the line to be deleted
        int indexToRemove = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(searchText)) {
                indexToRemove = i;
                break;
            }
        }

        // If the line is found, remove it and write the updated lines back to the file
        if (indexToRemove != -1) {
            lines.remove(indexToRemove);
            Files.write(path, lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        }

        return false;
    }

    // public void getDir() {
    //     try {
    //     if (!isConnected()) {
    //         System.out.println("Error: Not connected to the server. Please connect first.");
    //         return;
    //     }

    //     // Notify the server that the client wants to store a file
    //     out.writeUTF("/dir");

    //     String message = in.readUTF();
    //     System.out.println(message);
    
    //  } catch (IOException e) {
    //     System.err.println("Error: An error occurred while accesing the directory - " + e.getMessage());
    //  }

    public void getDir() {
    try {
        if (!isConnected()) {
            System.out.println("Error: Not connected to the server. Please connect first.");
            return;
        }

        // Notify the server that the client wants to get the directory information
        out.writeUTF("/dir");

        // Receive the file names from the server
        List<String> fileNames = receiveFileNames();

        if (fileNames.isEmpty()) {
            System.out.println("No files found in the directory.");
        } else {
            System.out.println("Files in the directory:");
            for (String fileName : fileNames) {
                System.out.println(fileName);
            }
        }
    } catch (IOException e) {
        System.err.println("Error: An error occurred while accessing the directory - " + e.getMessage());
    }
}

private List<String> receiveFileNames() {
    List<String> fileNames = new ArrayList<>();

    try {
        Files.list(Paths.get(FEserver.FILES_PATH))
            .forEach(path -> {
                try {
                    fileNames.add(path.getFileName().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    } catch (IOException e) {
        e.printStackTrace();
    }

    // Send the list of file names to the server
    fileNames.forEach(fileName -> {
        try {
            out.writeUTF(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    });

    return fileNames;
}

    
}

    

