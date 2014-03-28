import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class SendThread implements Runnable {

    public int nodeCnt;
    Map<Integer, SctpChannel> clntSock;
    public int currNode;
    Project1 conn;
    Lock lock;

    public SendThread() {

    }

    public SendThread(int nodeCnt, Map<Integer, SctpChannel> clntSock,
            int currNode, Lock lock) {
        this.nodeCnt = nodeCnt;
        this.clntSock = clntSock;
        this.currNode = currNode;
        this.lock = lock;
    }

    public void setConn(Project1 conn) {
        this.conn = conn;
    }

    @Override
    public void run() {
        // VectorClock oldclock = Project1.vectorClock;
        String msgStr = "Message";
        int i = 0;
        String msgType = "";

        // for loop to send messages to all others except self

        while (true) {
            while (!Project1.messageQueue.isEmpty()) {
                // System.out.println("Size of Message Queue\t"
                // + Project1.messageQueue.size());
                System.out.println("\nClock before Sending ");
                Project1.vectorClock.displayClock();
                Message1 msg = Project1.messageQueue.peek();
                if (msg.getMsg().equalsIgnoreCase("token")) {
                	System.out.println("\n Fulfilled Requests Vector in send thread");
                	msg.token.displayfulfilledRequestsVector();
                	System.out.println("\n UnFulfilled Requests Queue in send thread");
                	msg.token.displayQueue();
                	
                	
                    unicastMessage(msg.getReceiverId(), msg);
                    Project1.messageQueue.remove();
                    break;
                }
                synchronized (Project1.vectorClock) {
                    msg.setVectorClock(Project1.vectorClock);
                }
                if(!msg.getMsg().equalsIgnoreCase("token") && !msg.getMsg().equalsIgnoreCase("Bye"))
                {
                synchronized (Project1.vectorClock) {
                    Project1.vectorClock.sendEvent();
                    System.out.println("\nClock After Sending \t");
                    Project1.vectorClock.displayClock();
                }
                }

                for (int id : clntSock.keySet()) {

                    // lock.lock();
                    try {
                        if (id != currNode) {
                            i++;

                            sendMessage(clntSock.get(id), msg);
                            System.out.println("\nSending message from "
                                    + currNode + "(Self)" + " to " + id + " - "
                                    + msg.getMsg());

                        } else {
                            continue;
                        }
                    } catch (CharacterCodingException e) {
                        e.printStackTrace();
                    } finally {
                        // lock.unlock();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                msgType = Project1.messageQueue.peek().getMessageType();
                Project1.messageQueue.remove();
                /*
                 * synchronized (Project1.vectorClock) {
                 * Project1.vectorClock.sendEvent();
                 * System.out.println("\nClock After Sending \t");
                 * Project1.vectorClock.displayClock(); }
                 */

            }
            if (msgType.equalsIgnoreCase("Bye")) {
                break;
            }

        }

        // for loop to send BYE msg to all except me
        /*
         * for(int id : clntSock.keySet()) {
         * 
         * if(id!=currNode) { try {
         * 
         * Message msgTerm = new Message(currNode, "Bye");
         * 
         * sendMessage(clntSock.get(id),msgTerm.toString());
         * System.out.println(); System.out.println( currNode + " to " + id +
         * " - " + "Bye"); System.out.println(); } catch
         * (CharacterCodingException e) { e.printStackTrace(); } } } try {
         * Thread.sleep(3000); } catch (InterruptedException e) {
         * e.printStackTrace(); }
         */

    }

    void unicastMessage(int sendTo, Message1 msgToSend) {
        SctpChannel clientSocket = clntSock.get(new Integer(sendTo));
        System.out.println("\nSending token from process\t"
                + Project1.processNo + "\t to" + sendTo);
        try {
            sendMessage(clientSocket, msgToSend);
            /*
             * synchronized (Project1.vectorClock) {
             * Project1.vectorClock.sendEvent();
             * System.out.println("\nClock After Sending Token \t");
             * Project1.vectorClock.displayClock(); }
             */
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void sendMessage(SctpChannel clientSock, Message1 Message)
            throws CharacterCodingException {

        // prepare byte buffer to send massage
        ByteBuffer sendBuffer = ByteBuffer.allocate(10000);
        sendBuffer.clear();

        // serialize the message
        byte[] serializedMsg = null;
        try {
            serializedMsg = serialize(Message);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Reset a pointer to point to the start of buffer
        sendBuffer.put(serializedMsg);
        sendBuffer.flip();

        try {
            // Send a message in the channel
            MessageInfo messageInfo = MessageInfo.createOutgoing(null, 0);
            clientSock.send(sendBuffer, messageInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static byte[] serialize(Object obj) throws IOException {
        ObjectOutputStream out;// = new ObjectOutputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        return bos.toByteArray();
    }

}
