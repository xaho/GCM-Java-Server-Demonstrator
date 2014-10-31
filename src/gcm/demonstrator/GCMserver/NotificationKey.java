package gcm.demonstrator.GCMserver;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class NotificationKey {
	public String 								KeyName,
												KeyID;
	public ArrayList<String> 					RegIDs;
    private static ArrayList<NotificationKey> 	nkeys = new ArrayList<NotificationKey>();
	
	public NotificationKey (String KeyName, String KeyID, ArrayList<String> RegIDs)
	{
		this.KeyID = KeyID;
		this.KeyName = KeyName;
		this.RegIDs = RegIDs;
	}
	
	public static void loadNotificationKeys()
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
            		
            		System.out.println("getNotificationKeys: \nkeyName: " + keyName + "\nkeyID: " + keyID );
            		
            		ArrayList<String> regIDs = new ArrayList<String>();       		
            		JSONArray jArr = (JSONArray)jobj.get("regIDs");	//get the array containing the regIDs of NotificationKey
            		for (int j = 0; j < jArr.size(); j++)			//walk through all regIDs
            		{
            			regIDs.add((String)jArr.get(j));			//add regID to array of regIDs
            			System.out.println("regID: " + jArr.get(j));
            		}
            		result.add(new NotificationKey(keyName,keyID,regIDs));//add NotificationKey to array with all fields filled
            	}
            }
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Something went wrong with accessing clients.txt");
    	}
    	nkeys = result;
    }

	public static ArrayList<NotificationKey> getNotificationKeys()
	{
		return nkeys;
	}
	
	public static NotificationKey getNotificationKey(ArrayList<String> regIDs)
    {
    	for (NotificationKey nk : nkeys)
    	{
    		if ((nk.RegIDs.size() == regIDs.size()) && nk.RegIDs.containsAll(regIDs))
    		{
    			return nk;
    		}
    		else
    			System.out.println("");
    	}
    	return null;
    }

	public static void setNotificationKeys(ArrayList<NotificationKey> nkeys)
	{
		NotificationKey.nkeys = nkeys;
	}
	
	@SuppressWarnings("unchecked")
 	public static void saveNotificationKeys(ArrayList<NotificationKey> nkeys)
	{
		NotificationKey.nkeys = nkeys;
		//TODO: write nkeys to txt file using JSON
		JSONArray jarrroot = new JSONArray();
		for (NotificationKey nk : NotificationKey.nkeys)
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

		System.out.println(jarrroot);
	}
}
