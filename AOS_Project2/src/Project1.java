import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class Project1 {
    public static final int MESSAGE_SIZE = 10;
    public static int no_of_nodes = 0;
    public static int processNo = 0;
    Map<Integer, NodeInfo> allNodes;
    Map<Integer, SctpChannel> clntSock;
    public static ConcurrentLinkedQueue<Message1> messageQueue = new ConcurrentLinkedQueue<Message1>();
    public static VectorClock vectorClock;
    public static Token token;
    public static int[] testArray;
    public static boolean hasToken;
    public static boolean isUsingCS = false;

    Lock lock = new ReentrantLock();

    public static void main(String args[]) throws Exception {

        Project1 conn = new Project1();
        String argument = args[0];
        conn.hasToken = Boolean.parseBoolean(args[1]);

        conn.readConfig(); // Read the config file

        intializeFiles();

        conn.processNo = Integer.parseInt(argument);

        Project1.vectorClock = new VectorClock(Project1.no_of_nodes,
                Project1.processNo);
        // if (conn.hasToken) {
        Project1.token = new Token(new int[Project1.no_of_nodes],
                new LinkedList<Integer>());
        Project1.testArray = new int[Project1.no_of_nodes];
        // }

        conn.createConnections(conn.processNo); // To create connections,for the
                                                // given process no. accept
                                                // connections from higher nodes
                                                // and connect to lower nodes

        System.out
                .println("-------------------------------------------------------------------------");

        SendThread st = new SendThread(conn.no_of_nodes, conn.clntSock,
                conn.processNo, conn.lock);
        st.setConn(conn);

        RecvThread rt = new RecvThread(conn.no_of_nodes, conn.clntSock,
                conn.processNo, conn.lock);
        rt.setConn(conn);

        Thread send = new Thread(st);
        Thread recv = new Thread(rt);

        send.start();

        recv.start();

        Application appobj = new Application();
        appobj.application_start();

        try {
            send.join();

            recv.join();

            System.out.println("\n ********PROGRAM OVER***********");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void intializeFiles() {
        PrintWriter pw1 = null;
        PrintWriter pw2 = null;
        try {
            pw1 = new PrintWriter("./config/SharedResource.txt");
            pw2 = new PrintWriter("./config/mutex.txt");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pw1.print("");
        pw1.close();

        pw2.print("0");
        pw2.close();

        int numOfreq = 50;

        List<String> s = new ArrayList<String>(50);
        Random rand = new Random();

        // initialize list
        for (int i = 0; i < numOfreq; i++) {
            s.add("");
        }

        // add 'FULL' randomly
        for (int i = 0; i < s.size() / 2; i++) {
            int num = rand.nextInt(50);
            s.set(num, "FULL");
        }

        // add 'EMPTY' to remaining places
        for (int i = 0; i < s.size(); i++) {
            if (!s.get(i).equalsIgnoreCase("FULL")) {
                s.set(i, "EMPTY");
            }
        }
        String[] initialSlots = s.toArray(new String[0]);
        Application.writeSlots(initialSlots);
    }

    public void readConfig() {
        allNodes = new HashMap<Integer, NodeInfo>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("./config/config.txt"));
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.startsWith("#")) {
                    continue;
                }

                else {
                    String[] tokens = sCurrentLine.split(" ");
                    allNodes.put(Integer.parseInt(tokens[0]), new NodeInfo(
                            tokens[1].trim(), Integer.parseInt(tokens[2])));
                    no_of_nodes++;

                }
            }

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
        } catch (NullPointerException e3) {
            e3.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void createConnections(int processNo) {

        clntSock = new HashMap<Integer, SctpChannel>();

        SctpServerChannel serverSock = null;

        try {
            serverSock = SctpServerChannel.open();
            InetSocketAddress serverAddr = new InetSocketAddress(
                    allNodes.get(processNo).serverAddress,
                    allNodes.get(processNo).socketPort);
            serverSock.bind(serverAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Connect to lower nodes
        for (int i = 0; i < processNo; ++i) {

            try {
                SctpChannel ClientSock;
                InetSocketAddress ServerAddr = new InetSocketAddress(
                        allNodes.get(i).serverAddress,
                        allNodes.get(i).socketPort);
                ClientSock = SctpChannel.open();
                ClientSock.connect(ServerAddr);
                clntSock.put(i, ClientSock);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        for (int i = 0; i < processNo; ++i) {
            System.out.println("Process " + i + " is up ");
        }

        System.out.println("Process " + processNo + " Joined now");

        // Accept connections from higher sockets
        if (processNo != (no_of_nodes - 1)) {
            System.out.println("Waiting for other nodes to join.");
        }

        for (int i = processNo + 1; i < no_of_nodes; ++i) {
            SctpChannel clientSock;
            try {
                clientSock = serverSock.accept();
                clntSock.put(i, clientSock);
                System.out.println("Process " + i + " has joined");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
