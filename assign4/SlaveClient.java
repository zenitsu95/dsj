import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SlaveClient {

    public static void main(String[] args) {
        new SlaveClient().initiateSlaveClient(8080);
    }

    public void initiateSlaveClient(int port) {
        try {
            Socket slaveClient = new Socket("127.0.0.1", port);
            System.out.println("Starting to receive time from server\n");

            Thread sendTimeThread = new Thread(() -> startSendingTime(slaveClient));
            sendTimeThread.start();

            System.out.println("Starting to receive synchronized time from server\n");

            Thread receiveTimeThread = new Thread(() -> startReceivingTime(slaveClient));
            receiveTimeThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSendingTime(Socket slaveClient) {
        try {
            PrintWriter out = new PrintWriter(slaveClient.getOutputStream(), true);
            while (true) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                out.println(currentTime);
                System.out.println("Recent time sent successfully\n");
                Thread.sleep(5000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startReceivingTime(Socket slaveClient) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(slaveClient.getInputStream()));
            String serverTime;
            while ((serverTime = in.readLine()) != null) {
                System.out.println("Synchronized time at the client is: " + serverTime + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
