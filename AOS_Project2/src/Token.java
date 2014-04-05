import java.io.Serializable;
import java.util.Queue;

@SuppressWarnings("serial")
public class Token implements Serializable {
    int[] fulfilledRequestsVector;
    Queue<Integer> unfulfilledRequestsQueue;
    int nextSlot = 0;

    public Token(int[] fulfilledRequestsVector,
            Queue<Integer> unfulfilledRequestsQueue) {
        super();
        this.fulfilledRequestsVector = fulfilledRequestsVector;
        this.unfulfilledRequestsQueue = unfulfilledRequestsQueue;
    }

    public int[] getFulfilledRequestsVector() {
        return fulfilledRequestsVector;
    }

    public void setFulfilledRequestsVector(int[] fulfilledRequestsVector) {
        this.fulfilledRequestsVector = fulfilledRequestsVector;
    }

    public Queue<Integer> getUnfulfilledRequestsQueue() {
        return unfulfilledRequestsQueue;
    }

    public void setUnfulfilledRequestsQueue(
            Queue<Integer> unfulfilledRequestsQueue) {
        this.unfulfilledRequestsQueue = unfulfilledRequestsQueue;
    }

    public void displayfulfilledRequestsVector() {
        for (int i = 0; i < fulfilledRequestsVector.length; i++) {
            System.out.print(fulfilledRequestsVector[i] + "\t");
        }
    }

    public void displayQueue() {
        for (Integer i : this.unfulfilledRequestsQueue) {
            System.out.println(i);
        }
    }

}
