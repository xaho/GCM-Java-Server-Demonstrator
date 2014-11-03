package gcm.demonstrator.GCMserver;

import java.util.ArrayList;

public class NotificationKey {
	//Fields
	public String 								KeyName,
												KeyID;
	public ArrayList<String> 					RegIDs;
	
    //Constructor
	public NotificationKey (String KeyName, String KeyID, ArrayList<String> RegIDs)
	{
		this.KeyID = KeyID;
		this.KeyName = KeyName;
		this.RegIDs = RegIDs;
	}
}
