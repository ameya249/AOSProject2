import java.util.Queue;

public class Token {
    int[] fulfilledRequestsVector;
    Queue<Integer> unfulfilledRequestsQueue;

    public Token(int[] fulfilledRequestsVector,
            Queue<Integer> unfulfilledRequestsQueue) {
        super();
        this.fulfilledRequestsVector = fulfilledRequestsVector;
        this.unfulfilledRequestsQueue = unfulfilledRequestsQueue;
    }

    void sendToken() {
        Project1.hasToken = false;

    }

    void receiveToken() {

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

}
