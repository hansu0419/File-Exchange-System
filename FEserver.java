import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FEserver{
    
    public static final String FILES_PATH = "CSNETWK MP\\CSNETWK MP\\serverFiles";
    private ServerSocket serverSocket;
    public static final String IP_ADDRESS = "127.0.0.1";
    public static final int PORT = 12345;

    public FEserver() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP_ADDRESS, PORT));
            acceptConnections();

        } catch (IOException e) {
            
        }
    }

    private void acceptConnections() throws IOException {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (clientSocket.isConnected()) {
                    new Thread(() -> {
                        FEconnection client = new FEconnection(clientSocket);
                        client.handleCommands();
                        client.close();
                    }).start();
                }
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new FEserver();
    }
}
