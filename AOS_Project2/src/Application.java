
public class Application {
	static MutualExclusionService obj = new MutualExclusionServiceImpl();
	
	static boolean canRaiseRequest= true;
	static int noOfRequests = 10;
	static boolean canexecute_cs_leave=false;
	static boolean canTerminate=false;
	static boolean insideCSLeave=false;
    void application_start() throws InterruptedException {
        //Project1 obj = new Project1();
        //obj.cs_enter();
    	
    	
    	
    	while(true)
    	{
    		if(canRaiseRequest)
    		{
    	//	Thread.sleep(400);
    			canexecute_cs_leave=false;
    			obj.cs_enter();
    		}
    		
    		if(canexecute_cs_leave && !insideCSLeave)
    		{
    			obj.cs_leave();
    			noOfRequests--;
    			
    			if(noOfRequests==0 && !insideCSLeave)
    			{
    				canTerminate=true;
    			}
    			
    		}
		
    		
    		if(noOfRequests==0)
    		{
    		
    			break;
    			
    		}
    		
    	}
    	
    	if(canTerminate && !insideCSLeave)
    	{
    	//	Thread.sleep(2000);
    		Message1 msgTerm = new Message1("Bye", Project1.processNo); //
	        msgTerm.setVectorClock(Project1.vectorClock);
	        Project1.messageQueue.add(msgTerm);	
    	}
    	
/*
		if(noOfRequests==0 )
		{
			Thread.sleep(6000*Project1.no_of_nodes);
	    	Message1 msgTerm = new Message1("Bye", Project1.processNo); //
	        msgTerm.setVectorClock(Project1.vectorClock);
	        Project1.messageQueue.add(msgTerm);		
    	}*/
		
    }
	
}
		
    	
    
    