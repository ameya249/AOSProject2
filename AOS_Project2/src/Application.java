import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Application {
    static MutualExclusionService obj = new MutualExclusionServiceImpl();

    static boolean canRaiseRequest = true;
    static int noOfRequests = 3;
    static boolean canexecute_cs_leave = false;
    static boolean canTerminate = false;
    static boolean insideCSLeave = false;
    static int carNo = (Project1.processNo + 1) * 1000;

    void application_start() throws Exception {

        while (true) {
            if (canRaiseRequest) {

                canexecute_cs_leave = false;
                obj.cs_enter();
            }

            if (canexecute_cs_leave && !insideCSLeave) {
                obj.cs_leave();
                noOfRequests--;

                if (noOfRequests == 0 && !insideCSLeave) {
                    canTerminate = true;
                }

            }

            if (noOfRequests == 0) {

                break;

            }

        }

        if (canTerminate && !insideCSLeave) {
            Thread.sleep(3000);
            Message1 msgTerm = new Message1("Bye", Project1.processNo); //
            msgTerm.setVectorClock(Project1.vectorClock);
            Project1.messageQueue.add(msgTerm);
        }

    }

    static void writeToFile(String toPrint) {
        // String content = "Process No:\t" + Project1.processNo + "\t" +
        // toPrint;

        // File file = new File("./config/SharedResource.txt");
        /*
         * FileChannel channel=null;
         * 
         * try { channel = new RandomAccessFile("./config/SharedResource.txt",
         * "rw").getChannel(); } catch (FileNotFoundException e1) { // TODO
         * Auto-generated catch block e1.printStackTrace(); }
         * 
         * // if file doesnt exists, then create it //if (!file.exists()) {
         * //file.createNewFile(); //} FileLock fileLock=null; try { fileLock =
         * channel.lock(); } catch (IOException e1) { // TODO Auto-generated
         * catch block e1.printStackTrace(); }
         */
        // FileWriter fw = new FileWriter(file.getAbsoluteFile());
        // BufferedWriter bw = new BufferedWriter(fw);
        // bw.write(content);
        // bw.close();

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter("./config/SharedResource.txt", true)));
            out.println(toPrint);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * try { Object fileLock = channel.tryLock(); }
         * 
         * 
         * catch (OverlappingFileLockException e) { // File is already locked in
         * this thread or virtual machine e.printStackTrace(); } catch
         * (IOException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); } }
         * 
         * // Release the lock try { fileLock.release(); } catch (IOException e)
         * { // TODO Auto-generated catch block e.printStackTrace(); }
         * 
         * 
         * // Close the file
         * 
         * try { channel.close(); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */

        // System.out.println("Done");
    }

    public static String[] readAssignedSlots() {
        Scanner sc = null;
        try {
            sc = new Scanner(new FileReader("./config/parkingSlots.txt"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<String> lines = new ArrayList<String>();
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }
        sc.close();

        String[] arr = lines.toArray(new String[0]);
        return arr;
    }

    public static void writeSlots(String[] slots) {
        BufferedWriter writer = null;
        try {

            writer = new BufferedWriter(new FileWriter(
                    "./config/parkingSlots.txt"));
            for (int i = 0; i < slots.length; i++) {
                writer.write(slots[i]);
                writer.newLine();
                writer.flush();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }
}
