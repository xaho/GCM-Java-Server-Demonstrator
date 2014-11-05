package gcm.demonstrator.GCMserver;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class NotificationKeyManager {

	//Variables
    public ArrayList<NotificationKey> 	nkeys = new ArrayList<NotificationKey>();
    
    //Constructor
    public NotificationKeyManager()
    {
    	loadNotificationKeys();
    }
    
    //Logging function
    private void log(String log)
    {
    	System.out.println("NKM: "+log);
    }
    
    //Fill NotificationKeys ArrayList with NotificationKeys retrieved from storage (currently a txt file)
	private void loadNotificationKeys()
    {
    	ArrayList<NotificationKey> result = new ArrayList<NotificationKey>();
    	//read line from txt files
    	try 
    	{
    		List<String> lines = Files.readAllLines(Paths.get("notificationkeys.txt"),
                    Charset.defaultCharset());
            for (String line : lines) {
            	JSONParser parser = new JSONParser();
            	JSONArray jArrRoot = (JSONArray)parser.parse(line);	//get array of NotificationKeys
            	for (int i = 0; i < jArrRoot.size(); i++)
            	{
            		JSONObject jobj = (JSONObject)jArrRoot.get(i);	//get one NotificationKey of array
            		
            		String keyName = (String)jobj.get("keyName");	//get the keyName of NotificationKey
            		String keyID = (String)jobj.get("keyID");		//get the keyID of NotificationKey
            		
            		log("loadNotificationKeys: keyName: " + keyName); 
            		log("loadNotificationKeys: keyID: " + keyID );
            		
            		ArrayList<String> regIDs = new ArrayList<String>();       		
            		JSONArray jArr = (JSONArray)jobj.get("regIDs");	//get the array containing the regIDs of NotificationKey
            		for (int j = 0; j < jArr.size(); j++)			//walk through all regIDs
            		{
            			regIDs.add((String)jArr.get(j));			//add regID to array of regIDs
            			log("loadNotificationKeys: regID: " + jArr.get(j));
            		}
            		result.add(new NotificationKey(keyName,keyID,regIDs));//add NotificationKey to array with all fields filled
            	}
            }
    	}
    	catch (Exception ex)
    	{
    		log("Something went wrong with accessing notificationkeys.txt");
    	}
    	nkeys = result;
    }

	//Save NotificationKeys (currently to txt file)
	@SuppressWarnings("unchecked")
 	private void saveNotificationKeys()
	{
		JSONArray jarrroot = new JSONArray();
		for (NotificationKey nk : this.nkeys)
		{
			JSONArray jarr = new JSONArray();//fill array with regIDs
			for (String regID : nk.RegIDs)
			{
				jarr.add(regID);
			}
			
			JSONObject jobj = new JSONObject();
			jobj.put("keyName", nk.KeyName);
			jobj.put("keyID", nk.KeyID);        
			jobj.put("regIDs",jarr);
			jarrroot.add(jobj);
		}
		Path path = Paths.get("notificationkeys.txt");
		try
		{
			List<String> lines = new ArrayList<String>(); 
			lines.add(jarrroot.toString());
			Files.write(path, lines, Charset.defaultCharset());
		}
		catch (Exception ex)
		{
			log(ex.toString());
		}
		log("saveNotificationKeys: " + jarrroot);
	}
	
	//Request NotificationKey with given registrationIDs	
	public NotificationKey getNotificationKey(ArrayList<String> regIDs)
    {
    	for (NotificationKey nk : nkeys)
    	{
    		if ((nk.RegIDs.size() == regIDs.size()) && nk.RegIDs.containsAll(regIDs))
    		{
    			return nk;
    		}
    	}
    	return null;
    }

	//Add NotificationKey to list 
	public void addNotificationKey(NotificationKey nk) {
		this.nkeys.add(nk);
		saveNotificationKeys();
	}

	//TODO: Use updateNotificationKey(NotificationKey nk) to replace existing key with updated one (removed user/added user/changed name)
	//OR... seperate methods: addUser(...); removeUser(...); changeName(...);
}
