import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
    public static boolean hasToken;
    public static boolean isUsingCS = false;

    Lock lock = new ReentrantLock();

    public static void main(String args[]) throws NumberFormatException,
            IOException, InterruptedException {

        Project1 conn = new Project1();
        String argument = args[0];
        conn.hasToken = Boolean.parseBoolean(args[1]);

        conn.readConfig(); // Read the config file

        conn.processNo = Integer.parseInt(argument);

        Project1.vectorClock = new VectorClock(Project1.no_of_nodes,
                Project1.processNo);
        // if (conn.hasToken) {
        Project1.token = new Token(new int[Project1.no_of_nodes],
                new LinkedList<Integer>());
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
        if (Project1.processNo == 2 || Project1.processNo == 1) {
            Application appobj = new Application();
            appobj.application_start();
        }

        /*
         * for (int i = 0; i < 5; i++) { Message1 msg = new Message1("Hello",
         * Project1.processNo); // msg.setVectorClock(Project1.vectorClock);
         * Project1.messageQueue.add(msg); }
         */

        /*
         * Message1 requestMsg = new Message1("request", Project1.processNo);
         * Project1.messageQueue.add(requestMsg);
         */

        /*
         * Message1 msgTerm = new Message1("Bye", Project1.processNo); //
         * msgTerm.setVectorClock(Project1.vectorClock);
         * Project1.messageQueue.add(msgTerm);
         */

        try {
            send.join();

            recv.join();

            System.out.println("\n ********PROGRAM OVER***********");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void readConfig() {
        allNodes = new HashMap<Integer, NodeInfo>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(
                    "/home/ameya/workspace/AOS_Project2/config/config.txt"));
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

    public void cs_enter() {
        if (Project1.hasToken) {
            System.out.println("Process \t" + Project1.processNo
                    + "\t has entered CS at \t" + System.currentTimeMillis());
            Project1.isUsingCS = true;
        } else {
            Message1 request = new Message1("request", Project1.processNo);
            Project1.messageQueue.add(request);
        }
    }

    public void cs_leave() {
        // Project1.token.fulfilledRequestsVector[Project1.processNo]++;
        System.out.println("\n Inside CS Leave \n");
        Project1.isUsingCS = false;
        // Increment filledRequestsVector
        int[] fulfilledRequests = Project1.token.getFulfilledRequestsVector();
        fulfilledRequests[Project1.processNo]++;
        Project1.token.setFulfilledRequestsVector(fulfilledRequests);

        // Compare vector clock and fulfilled requests vector
        int[] vectorClock = Project1.vectorClock.getV();
        if (!Project1.token.unfulfilledRequestsQueue.isEmpty()) {
            Queue<Integer> UnfulfilledReqQueue = Project1.token
                    .getUnfulfilledRequestsQueue();
            for (int i = 0; i < vectorClock.length; i++) {
                if (vectorClock[i] > fulfilledRequests[i]) {
                    if (!UnfulfilledReqQueue.contains(i))
                        UnfulfilledReqQueue.add(i);
                }
            }
            int toGiveToken = UnfulfilledReqQueue.poll();
            Message1 tokenMsg = new Message1("token", Project1.processNo);
            tokenMsg.setReceiverId(toGiveToken);
            Project1.messageQueue.add(tokenMsg);
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
            System.out.println(i + " has joined ");
        }

        System.out.println(processNo + " Joined now");

        // Accept connections from higher sockets
        if (processNo != (no_of_nodes - 1)) {
            System.out.println("Waiting for other nodes to join.");
        }

        for (int i = processNo + 1; i < no_of_nodes; ++i) {
            SctpChannel clientSock;
            try {
                clientSock = serverSock.accept();
                clntSock.put(i, clientSock);
                System.out.println(i + " Joined the Chat");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
