import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ClockServer {

    // Shared map to hold client info
    private static final ConcurrentHashMap<String, ClientInfo> clientData = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        initiateClockServer(8080);
    }

    static class ClientInfo {
        public Date clockTime;
        public long timeDifferenceMillis;
        public Socket connector;

        public ClientInfo(Date clockTime, long timeDiff, Socket connector) {
            this.clockTime = clockTime;
            this.timeDifferenceMillis = timeDiff;
            this.connector = connector;
        }
    }

    public static void initiateClockServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Socket at master node created successfully\n");
            System.out.println("Clock server started...\n");
            System.out.println("Starting to make connections...\n");

            // Thread to accept connections
            new Thread(() -> startConnecting(serverSocket)).start();

            // Thread to synchronize clocks
            new Thread(ClockServer::synchronizeAllClocks).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startConnecting(ServerSocket serverSocket) {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                System.out.println(clientAddress + " got connected successfully");

                // Thread to receive time from this client
                new Thread(() -> startReceivingClockTime(clientSocket, clientAddress)).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void startReceivingClockTime(Socket socket, String address) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            while (true) {
                String timeStr = in.readLine();
                if (timeStr == null) break;

                Date clientTime = formatter.parse(timeStr);
                long currentTimeMillis = System.currentTimeMillis();
                long clientTimeMillis = clientTime.getTime();
                long diff = currentTimeMillis - clientTimeMillis;

                clientData.put(address, new ClientInfo(clientTime, diff, socket));
                System.out.println("Client Data updated with: " + address + "\n");

                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println("Connection lost with: " + address);
            clientData.remove(address);
        }
    }

    public static long getAverageClockDiff() {
        Collection<ClientInfo> clients = clientData.values();
        long totalDiff = clients.stream()
                                .mapToLong(c -> c.timeDifferenceMillis)
                                .sum();
        return totalDiff / (clients.size() == 0 ? 1 : clients.size());
    }

    public static void synchronizeAllClocks() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        while (true) {
            try {
                System.out.println("New synchronization cycle started.");
                System.out.println("Number of clients to be synchronized: " + clientData.size());

                if (!clientData.isEmpty()) {
                    long avgDiff = getAverageClockDiff();
                    long synchronizedTimeMillis = System.currentTimeMillis() + avgDiff;

                    String synchronizedTimeStr = formatter.format(new Date(synchronizedTimeMillis));

                    for (Map.Entry<String, ClientInfo> entry : clientData.entrySet()) {
                        try {
                            PrintWriter out = new PrintWriter(entry.getValue().connector.getOutputStream(), true);
                            out.println(synchronizedTimeStr);
                        } catch (IOException e) {
                            System.out.println("Something went wrong while sending time to " + entry.getKey());
                        }
                    }
                } else {
                    System.out.println("No client data. Synchronization not applicable.");
                }

                System.out.println("\n\n");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
