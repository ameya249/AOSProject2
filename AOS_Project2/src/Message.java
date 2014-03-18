
public class Message {

	int sendID;
	String msg;
	public Message(int sendID, String msg) {
		this.sendID = sendID;
		this.msg = msg;
	}


	public Message() {
	}


	public String toString()
	{
		return sendID + "/" + msg + "/";
	}

}
