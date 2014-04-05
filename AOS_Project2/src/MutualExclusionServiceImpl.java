import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;

public class MutualExclusionServiceImpl implements MutualExclusionService {

    @Override
    public void cs_enter() throws Exception {

        Application.canRaiseRequest = false;
        BufferedReader br = null;

        if (Project1.hasToken) {
            int counter = 0;
            int parkingCounter = 0;
            try {
                br = new BufferedReader(new FileReader("./config/mutex.txt"));
                String sCurrentLine;

                while ((sCurrentLine = br.readLine()) != null) {
                    counter = Integer.parseInt(sCurrentLine);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (counter > 0) {
                // System.out.println("Mutual Exclusion Violated");
                // throw new Exception("Mutual Exclusion Violated");
                throw new MutualExclusionException("Mutual Exclusion Violated");
            } else {
                counter++;
                overWriteFile(counter);
            }

            System.out.println("Process \t" + Project1.processNo
                    + "\t has entered CS at \t" + System.currentTimeMillis());
            Project1.isUsingCS = true;
            String fileContent = "has entered CS at \t"
                    + System.currentTimeMillis();
            Project1.token.nextSlot++;
            Application.carNo++;
            String parkingDetails = "Parking Gate \t" + Project1.processNo
                    + "\t has started assigning parking slots \n Car No \t"
                    + Application.carNo + "\t from parking gate \t"
                    + Project1.processNo + " \t has been assigned to slot \t"
                    + Project1.token.nextSlot;
            Application.writeToFile(parkingDetails);

            Application.canexecute_cs_leave = true;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // cs_leave();
        } else {
            Message1 request = new Message1("request", Project1.processNo);
            System.out.println("Adding request for Process "
                    + Project1.processNo + "to the sending queue \n");
            Project1.messageQueue.add(request);

        }
    }

    @Override
    public void cs_leave() {

        Application.insideCSLeave = true;
        // Project1.token.fulfilledRequestsVector[Project1.processNo]++;
        // System.out.println("\n Inside CS Leave \n");

        // Increment filledRequestsVector
        // synchronized(Project1.token)
        // {

        int[] fulfilledRequests = Project1.token.getFulfilledRequestsVector();

        if (Project1.vectorClock.getV()[Project1.processNo] != Project1.token
                .getFulfilledRequestsVector()[Project1.processNo]) {

            fulfilledRequests[Project1.processNo]++;

            Project1.token.setFulfilledRequestsVector(fulfilledRequests);

            System.out
                    .println("\n FulilledRequestsVector size after adding current fulfilled request is \t "
                            + Project1.token.getFulfilledRequestsVector().length);
            synchronized (Project1.token) {
                Project1.token.displayfulfilledRequestsVector();
            }

        }

        // Compare vector clock and fulfilled requests vector
        int[] vectorClock = Project1.vectorClock.getV();
        // if (!Project1.token.unfulfilledRequestsQueue.isEmpty()) {
        Queue<Integer> UnfulfilledReqQueue = Project1.token
                .getUnfulfilledRequestsQueue();
        for (int i = 0; i < vectorClock.length; i++) {
            // System.out.println("fulfilledRequests["+i+"]\t"+fulfilledRequests[i]+"vectorClock["+i+"]\t"+vectorClock[i]
            // );
            if (vectorClock[i] > fulfilledRequests[i]) {
                // Project1.vectorClock.displayClock();

                if (!UnfulfilledReqQueue.contains(i)) {
                    UnfulfilledReqQueue.add(i);

                }
            }
        }
        System.out.println("\n Queue After Adding unfulfiiled Requests");
        for (Integer k : UnfulfilledReqQueue)
            System.out.print("\n \t" + k);

        String fileContent = "has left CS at \t" + System.currentTimeMillis();
        String parkingDetails = "Parking gate \t" + Project1.processNo
                + "\t is done assigning parking slots";
        Application.writeToFile(parkingDetails);
        BufferedReader br = null;
        int counter = 0;
        try {
            br = new BufferedReader(new FileReader("./config/mutex.txt"));
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                counter = Integer.parseInt(sCurrentLine);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        counter--;
        overWriteFile(counter);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Project1.isUsingCS = false;

        if (!Project1.token.unfulfilledRequestsQueue.isEmpty()) {
            int toGiveToken = UnfulfilledReqQueue.poll();
            System.out
                    .println("\n UnfullfilledRequestQueue in the Token to be sent After Popping\n");
            for (Integer k : UnfulfilledReqQueue)
                System.out.print("\n \t" + k);

            Project1.token.setUnfulfilledRequestsQueue(UnfulfilledReqQueue);
            Message1 tokenMsg = new Message1("token", Project1.processNo,
                    Project1.token);
            System.out
                    .println("\nFulilledRequestsVector in the Token to be sent\n");
            Project1.token.displayfulfilledRequestsVector();
            tokenMsg.setReceiverId(toGiveToken);
            // tokenMsg.setVectorClock(Project1.vectorClock);
            Project1.messageQueue.add(tokenMsg);
            System.out
                    .println("\n Token message added to the send Queue by process "
                            + Project1.processNo
                            + " from cs_exit and to be sent to process "
                            + toGiveToken + "\n");
            Project1.hasToken = false;
        }

        Application.insideCSLeave = false;
        Application.canRaiseRequest = true;
    }

    public static void overWriteFile(int counter) {
        try {
            PrintWriter printWriter = new PrintWriter("./config/mutex.txt");
            printWriter.print(counter);
            printWriter.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
