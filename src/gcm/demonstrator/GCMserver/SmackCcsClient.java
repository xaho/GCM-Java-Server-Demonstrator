package gcm.demonstrator.GCMserver;

//License: Open Source Apache
//Source: http://www.igniterealtime.org/projects/smack/
//Download: http://download.igniterealtime.org/smack/smack_4_0_5.zip
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;

//License: Apache license 2.0
//Source: https://code.google.com/p/json-simple/
//Download: https://json-simple.googlecode.com/files/json-simple-1.1.1.jar
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;				
import org.json.simple.parser.ParseException;

//License: ?? based on: http://xmlpull.org/ -> "The XmlPull API is in public domain in hope that it will be embraced
	//by Java developers (conformance tests are under LGPL license to minimize risk of incompatible implementations)."
//Source: http://kxml.org/ for info, http://sourceforge.net/projects/kxml/ for download
//Download: http://netcologne.dl.sourceforge.net/project/kxml/kxml2/2.3.0/kxml2-2.3.0.jar 
	//do not use http://www.xmlpull.org/v1/download/xmlpull_1_0_5.jar it doesn't have NextText() method...
import org.xmlpull.v1.XmlPullParser;

//License: Apache License
//Source: https://hc.apache.org/downloads.cgi
//Download: http://apache.mirror.triple-it.nl//httpcomponents/httpclient/binary/httpcomponents-client-4.3.5-bin.zip
	//NOTE: Using the Apache HTTPCore & HTTPClient libraries for creating and managing notificationKeys, 
	//there is an issue somewhere in the library that it does not include an Apache Logging library, 
	//this should be added manually to the project or else the program will crash when running (compiling will work just fine)
	//License: Apache License
	//Source: http://commons.apache.org/proper/commons-logging/download_logging.cgi
	//Download: http://mirror.nl.webzilla.com/apache//commons/logging/binaries/commons-logging-1.2-bin.zip
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import gcm.demonstrator.GCMserver.NotificationKey;

/**
 * Sample Smack implementation of a client for GCM Cloud Connection Server. This
 * code can be run as a standalone CCS client.
 *
 * <p>For illustration purposes only.
 */
public class SmackCcsClient extends Frame{

    private static final Logger logger = Logger.getLogger("SmackCcsClient");

    private static final String GCM_SERVER = "gcm.googleapis.com";
    private static final int GCM_PORT = 5235;

    private static final String GCM_ELEMENT_NAME = "gcm";
    private static final String GCM_NAMESPACE = "google:mobile:data";
    
    private static ArrayList<String> clients = new ArrayList<String>();
    private static NotificationKeyManager NKM = new NotificationKeyManager();

    private static boolean useGUI = false;
    static {

        ProviderManager.addExtensionProvider(GCM_ELEMENT_NAME, GCM_NAMESPACE,
            new PacketExtensionProvider() {
                @Override
                public PacketExtension parseExtension(XmlPullParser parser) throws
                        Exception {
                    String json = parser.nextText();
                    return new GcmPacketExtension(json);
                }
            });
    }
    
    public SmackCcsClient()
    {
    	if (useGUI)
    	{
	    	setLayout(new FlowLayout());
	    	Panel p1 = new Panel();
	    	Panel p2 = new Panel();
	    	
	    	Choice ch1 = new Choice();
	    	ch1.add("Device: ");
	    	ch1.add("Group of devices: ");
	    	p1.add(ch1);
	    	
	    	java.awt.List list1 = new java.awt.List();
	    	p1.add(list1);
	    	    	
	    	Checkbox cb1 = new Checkbox("Collapsible");
	    	p1.add(cb1);
	    	Checkbox cb2 = new Checkbox("Delay while idle");
	    	p1.add(cb2);
	
	    	Label lbl3 = new Label("Time to live: ");
	    	p1.add(lbl3);
	    	
	    	Choice ch2 = new Choice();
	    	ch2.add("0 Seconds");
	    	ch2.add("4 Weeks");
	    	p1.add(ch2);
	    	
	    	Button b1 = new Button("Send notification");
	    	p1.add(b1);
	    	
	    	Button b2 = new Button("Add device to group");
	    	p2.add(b2);
	    	
	    	add(p1);
	    	add(p2);
	    	setTitle("AWT Counter");  // "super" Frame sets title
	        setSize(1280, 480);        // "super" Frame sets initial window size
	        setVisible(true);  
    	}
    }
    
    private static void log(String log)
    {
    	System.out.println("SCC: " + log);
    }

    private XMPPConnection connection;

    /**
     * Indicates whether the connection is in draining state, which means that it
     * will not accept any new downstream messages.
     */
    protected volatile boolean connectionDraining = false;

    /**
     * Sends a downstream message to GCM.
     *
     * @return true if the message has been successfully sent.
     */
    public boolean sendDownstreamMessage(String jsonRequest) throws
            NotConnectedException {
        if (!connectionDraining) {
            send(jsonRequest);
            return true;
        }
        logger.info("Dropping downstream message since the connection is draining");
        return false;
    }

    /**
     * Returns a random message id to uniquely identify a message.
     *
     * <p>Note: This is generated by a pseudo random number generator for
     * illustration purpose, and is not guaranteed to be unique.
     */
    public String nextMessageId() {
        return "m-" + UUID.randomUUID().toString();
    }

    /**
     * Sends a packet with contents provided.
     */
    protected void send(String jsonRequest) throws NotConnectedException {
        Packet request = new GcmPacketExtension(jsonRequest).toPacket();
        connection.sendPacket(request);
    }

    /**
     * Handles an upstream data message from a device application.
     *
     * <p>This sample echo server sends an echo message back to the device.
     * Subclasses should override this method to properly process upstream messages.
     */
    protected void handleUpstreamMessage(Map<String, Object> jsonObject) {
        // PackageName of the application that sent this message.
        String category = (String) jsonObject.get("category");
        String from = (String) jsonObject.get("from");
        @SuppressWarnings("unchecked")
        Map<String, String> payload = (Map<String, String>) jsonObject.get("data");
        payload.put("ECHO", "Application: " + category);

        // Send an ECHO response back
        String echo = createJsonMessage(from, nextMessageId(), payload,
                "echo:CollapseKey", null, false);

        try {
            sendDownstreamMessage(echo);
        } catch (NotConnectedException e) {
            logger.log(Level.WARNING, "Not connected anymore, echo message is not sent", e);
        }
    }

    /**
     * Handles an ACK.
     *
     * <p>Logs a INFO message, but subclasses could override it to
     * properly handle ACKs.
     */
    protected void handleAckReceipt(Map<String, Object> jsonObject) {
        String messageId = (String) jsonObject.get("message_id");
        String from = (String) jsonObject.get("from");
        logger.log(Level.INFO, "handleAckReceipt() from: " + from + ", messageId: " + messageId);
    }

    /**
     * Handles a NACK.
     *
     * <p>Logs a INFO message, but subclasses could override it to
     * properly handle NACKs.
     */
    protected void handleNackReceipt(Map<String, Object> jsonObject) {
        String messageId = (String) jsonObject.get("message_id");
        String from = (String) jsonObject.get("from");
        logger.log(Level.INFO, "handleNackReceipt() from: " + from + ", messageId: " + messageId);
    }

    protected void handleControlMessage(Map<String, Object> jsonObject) {
        logger.log(Level.INFO, "handleControlMessage(): " + jsonObject);
        String controlType = (String) jsonObject.get("control_type");
        if ("CONNECTION_DRAINING".equals(controlType)) {
            connectionDraining = true;
        } else {
            logger.log(Level.INFO, "Unrecognized control type: %s. This could happen if new features are " + "added to the CCS protocol.", controlType);
        }
    }

    /**
     * Creates a JSON encoded GCM message.
     *
     * @param to RegistrationId of the target device or NotificationKey of target  (Required).
     * @param messageId Unique messageId for which CCS will send an
     *         "ack/nack" (Required).
     * @param payload Message content intended for the application. (Optional).
     * @param collapseKey GCM collapse_key parameter (Optional).
     * @param timeToLive GCM time_to_live parameter (Optional, 0 to 2,419,200 seconds, maximum when null, 0 results in deliver or delete).
     * @param delayWhileIdle GCM delay_while_idle parameter (Optional, false when null, true == wait for idle to come online, false == send immediately).
     * @return JSON encoded GCM message.
     */
    public static String createJsonMessage(String to, String messageId,
            Map<String, String> payload, String collapseKey, Long timeToLive,
            Boolean delayWhileIdle) {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("to", to);
        if (collapseKey != null) {
            message.put("collapse_key", collapseKey);
        }
        if (timeToLive != null) {
            message.put("time_to_live", timeToLive);
        }
        /*if (delayWhileIdle != null && delayWhileIdle) {
            message.put("delay_while_idle", true);
        }*/
        message.put("delay_while_idle",false);
      message.put("message_id", messageId);
      message.put("data", payload);
      return JSONValue.toJSONString(message);
    }

    /**
     * Creates a JSON encoded ACK message for an upstream message received
     * from an application.
     *
     * @param to RegistrationId of the device who sent the upstream message.
     * @param messageId messageId of the upstream message to be acknowledged to CCS.
     * @return JSON encoded ack.
     */
        protected static String createJsonAck(String to, String messageId) {
        Map<String, Object> message = new HashMap<String, Object>();
        message.put("message_type", "ack");
        message.put("to", to);
        message.put("message_id", messageId);
        return JSONValue.toJSONString(message);
    }

    /**
     * Connects to GCM Cloud Connection Server using the supplied credentials.
     *
     * @param senderId Your GCM project number
     * @param apiKey API Key of your project
     */
    public void connect(long senderId, String apiKey)
            throws XMPPException, IOException, SmackException {
        ConnectionConfiguration config =
                new ConnectionConfiguration(GCM_SERVER, GCM_PORT);
        config.setSecurityMode(SecurityMode.enabled);
        config.setReconnectionAllowed(true);
        config.setRosterLoadedAtLogin(false);
        config.setSendPresence(false);
        config.setSocketFactory(SSLSocketFactory.getDefault());

        connection = new XMPPTCPConnection(config);
        connection.connect();

        connection.addConnectionListener(new LoggingConnectionListener());

        // Handle incoming packets
        connection.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(Packet packet) {
                logger.log(Level.INFO, "Received: " + packet.toXML());
                Message incomingMessage = (Message) packet;
                GcmPacketExtension gcmPacket =
                        (GcmPacketExtension) incomingMessage.
                        getExtension(GCM_NAMESPACE);
                String json = gcmPacket.getJson();
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonObject =
                            (Map<String, Object>) JSONValue.
                            parseWithException(json);

                    // present for "ack"/"nack", null otherwise
                    Object messageType = jsonObject.get("message_type");

                    if (messageType == null) {
                        // Normal upstream data message
                        handleUpstreamMessage(jsonObject);

                        // Send ACK to CCS
                        String messageId = (String) jsonObject.get("message_id");
                        String from = (String) jsonObject.get("from");
                        String ack = createJsonAck(from, messageId);
                        send(ack);
                    } else if ("ack".equals(messageType.toString())) {
                          // Process Ack
                          handleAckReceipt(jsonObject);
                    } else if ("nack".equals(messageType.toString())) {
                          // Process Nack
                          handleNackReceipt(jsonObject);
                    } else if ("control".equals(messageType.toString())) {
                          // Process control message
                          handleControlMessage(jsonObject);
                    } else {
                          logger.log(Level.WARNING,
                                  "Unrecognized message type (%s)",
                                  messageType.toString());
                    }
                } catch (ParseException e) {
                    logger.log(Level.SEVERE, "Error parsing JSON " + json, e);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to process packet", e);
                }
            }
        }, new PacketTypeFilter(Message.class));

        // Log all outgoing packets
        connection.addPacketInterceptor(new PacketInterceptor() {
            @Override
                public void interceptPacket(Packet packet) {
                    logger.log(Level.INFO, "Sent: {0}", packet.toXML());
                }
            }, new PacketTypeFilter(Message.class));

        connection.login(senderId + "@gcm.googleapis.com", apiKey);
    }
    
    public static ArrayList<String> getClients()
    {
    	ArrayList<String> result = new ArrayList<String>();
    	
    	try 
    	{
    		List<String> lines = Files.readAllLines(Paths.get("clients.txt"),
                    Charset.defaultCharset());
            for (String line : lines) {
                clients.add(line);
            }
    	}
    	catch (Exception ex)
    	{
    		log("Something went wrong with accessing clients.txt");
    	}
    	
    	return result;
    }
    
    private static NotificationKey removeUserNotificationKey(NotificationKey nk)
    {
    	
    	return nk;
    }
    
    private static NotificationKey generateNotificationKey(ArrayList<String> regIDs, String name) 
    {
    	try
    	{
			// TODO Auto-generated method stub
	    	log("TODO: send message to generate NotificationKey.");
	    	log("TODO: save generated NotificationKey.");
	    	// Create GCM notification headers
		    CloseableHttpClient client = HttpClients.createDefault();
		    HttpPost post = new HttpPost("https://android.googleapis.com/gcm/notification");
		    post.addHeader("Content-Type", "application/json");
		    post.addHeader("project_id", "673775556614");
		    post.addHeader("Authorization", "key=" + "AIzaSyByVnTjZjWDyNr5EmyWm56rJo3dppW9PAg");
		    Map<String, Object> body = new HashMap<String, Object>();
			body.put("operation", "create");
		    body.put("notification_key_name", name);
		    body.put("registration_ids", regIDs);
		    String temp = JSONObject.toJSONString(body);
		    StringEntity stringEntity = new StringEntity(temp, "UTF-8");
		    stringEntity.setContentType("application/json;charset=UTF-8");
		    stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
		    
		    post.setEntity(stringEntity);
		    try
		    {
				CloseableHttpResponse response = client.execute(post);
				String responseString = EntityUtils.toString(response.getEntity());
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
				{
				    logger.info("Notification Key Response: " + responseString);
				    JSONObject key = (JSONObject) JSONValue.parse(responseString);
				    if (key.containsKey("notification_key"))
				    {
				    	//TODO: create notificationkey object and add to nkeys etc.
				    	log((String)key.get("notification_key"));
				    	NotificationKey nk = new NotificationKey(name, (String)key.get("notification_key"),regIDs);
				    	NKM.addNotificationKey(nk);
				    	return nk;
				    }
				}
				else 
				{
				    logger.warning("Error with Notification key: " + responseString);
				}
		    }
		    catch (IOException e)
		    {
		    	e.printStackTrace();
		    }
		    finally
		    {
				client.close();
		    }
	    	NotificationKey nk = new NotificationKey(name,"Received KeyID",regIDs);
	    	NKM.addNotificationKey(nk);
    	}
    	catch (Exception ex)
    	{
    	    ex.printStackTrace();
    	}
		return null;
	}
    
    
	public static ArrayList<Integer> getIntsFromLine()
    {
    	ArrayList<Integer> ints = new ArrayList<>();
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			String input = br.readLine();
			Scanner linescanner = new Scanner(input);
			while (linescanner.hasNextInt())
			{
				int val = linescanner.nextInt();
				ints.add(val);
			}
			linescanner.close();
		}
		catch(Exception ex)
		{
			
		}
		return ints;
    }
	
	public static void printComplexMenu(String menu)
	{
		switch (menu)
		{
		case "1":
			log("");log("What should the server do?");
			log("1. Send notification");
			log("2. Add device to notificationKey (group of devices)");
			log("3. Remove device form notificationKey (group of devices)");
			//log("4. ");
		    //log("5. Send regular notification to notificationkey which one client can dismiss for others.");
			break;
		case "1.1":
			log("Send notification to what device(s)? (Seperate multiple devices with \" \"");
			break;
		case "1.1.1":
			log("Should the earlier notifications still in queue be dismissed? (Collapsible)");
			break;
		case "1.1.1.1":
			log("What should the time_to_live be in seconds? (Maximum of 2419200 (4 weeks))");
			break;
		case "1.1.1.1.1":
			log("Should the message delay_while_idle? (Send message when client seeks connection or push it)");
			break;
		case "1.2":
			log("What group are we going to add a device to?");
			break;
		case "1.2.1":
			log("What device are we going to add?");
			break;
		case "1.3":
			log("What group are we going to remove a device from?");
			break;
		case "1.3.1":
			log("What device are we going to remove?");
			break;
		}
	    log("8. Toggle printing menu.");log("9. Stop server.");log("0. Return to previous menu.");log("Current menu: " + menu);log("Input choice please:");
	}
	
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
        final long senderId = 673775556614L; 
        //Your GCM sender id, shown on console.developer.google.com -> select Project -> Project Number
        //(Should be suffixed with L in java code to indicate Long int value) 
        final String password = "AIzaSyByVnTjZjWDyNr5EmyWm56rJo3dppW9PAg"; 
        //Shown as API key on console.developer.google.com -> select Project -> APIs & Auth -> Credentials -> API key 
        //(Only when one has already been generated) 

        SmackCcsClient ccsClient = new SmackCcsClient();
        ccsClient.connect(senderId, password);
        log("connected method done");
        logger.log(Level.INFO, "connected");

        //toRegId is an ID obtained by Android device when registering at GCM servers, should be transferred from application to FlexC server somehow.
        String messageId = ccsClient.nextMessageId();
        Map<String, String> payload = new HashMap<String, String>();
        payload.put("Hello", "World");
        //payload.put("CCS", "Dummy Message");
        //payload.put("EmbeddedMessageId", messageId);
        String collapseKey = "sample";
        String message;
        Long timeToLive = 10000L;

        ///testing area
        
        ///end of testing area
        boolean exit = false;
        boolean exitSubmenu = false;
        boolean printmenu = true;
        
        int selection = 0;
        Scanner scanner = new Scanner(System.in);
        
        getClients();//fill clients ArrayList with RegIDs to know where to send notifications to.
        String menu = "1";
        
        
        while (!exit)
        {
        	if (useGUI)
    		{
				printComplexMenu(menu);
				
		    	
		    	selection = scanner.nextInt();
		    	if (Integer.toString(selection).equals("0") && menu != "1")
		    	{
		    		menu = menu.substring(0, menu.length()-2);
		    		log("if");
		    	}
		    	else
		    	{
		    		menu = menu.concat("." + Integer.toString(selection));
		    		log("else: "+ Integer.toString(selection));
		    	}
    		}
        	else
        	{
	        	//Show user options
	        	if (printmenu)
	        		printMenu();
	        	
	        	try 
	        	{
	        		exitSubmenu = false;
	        		selection = scanner.nextInt();
	        		switch(selection)
	        		{
	        		case 1: 
	        			while (!exitSubmenu)
	        			{
		        			
		        			try 
		        			{
		        				printSubMenu(selection);
		        				selection = scanner.nextInt();

								messageId = ccsClient.nextMessageId();
								payload.clear(); payload.put("Type", "Regular");
					            message = createJsonMessage(clients.get(selection), messageId, payload,
					                    null, timeToLive, false);        
					            ccsClient.sendDownstreamMessage(message);
					            exitSubmenu = true;
		        			}
		        			catch (Exception ex)
		        			{
		                		log("Invalid input, try again.");
		        			}
	        			}
	        			break;
	        		case 2: 
	        			while (!exitSubmenu)
	        			{
		        			
		        			try 
		        			{
		        				printSubMenu(selection);
		        				selection = scanner.nextInt();

								messageId = ccsClient.nextMessageId();
								payload.clear(); payload.put("Type", "Collapsible");
					            message = createJsonMessage(clients.get(selection), messageId, payload,
					                    collapseKey, timeToLive, false);        
					            ccsClient.sendDownstreamMessage(message);
					            exitSubmenu = true;
		        			}
		        			catch (Exception ex)
		        			{
		                		log("Invalid input, try again.");
		        			}
	        			}
	        			break;
	        		case 3:
	        			while (!exitSubmenu)
	        			{
		        			
		        			try 
		        			{
		        				printSubMenu(selection);
		        				ArrayList<Integer> IDs; 
		        				IDs = getIntsFromLine();//TODO: Filter duplicates?
		        				if (IDs.size() > 0)
		        				{
			        				ArrayList<String> regIDs = new ArrayList<String>();
			        				for (int ID : IDs)
			        				{
			        					regIDs.add(clients.get(ID));
			        				}
			        				
			        				NotificationKey nk = NKM.getNotificationKey(regIDs);
			        				if (nk != null)
			        				{
				        				log("Found NotificationKey:\nKeyName: " + nk.KeyName);
				        				log("KeyID: " + nk.KeyID);
				        				for (String regID : nk.RegIDs)
				        				{
				        					log("RegID: "+regID);
				        				}
			        				}
			        				else
			        				{
			        			    	log("No NotificationKey found with entered IDs. \n" +
			        			    			"Enter a name to create a new NotificationKey or enter \"abort\" to abort.");
			        					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			        					String name = br.readLine();
			        					if (!name.equals("abort") && !name.equals("")) //&& name does not exist yet
			        					{
			        						nk = generateNotificationKey(regIDs, name);
			        					}
			        				}
									messageId = ccsClient.nextMessageId();
									payload.clear(); payload.put("Type", "NotificationKey");
			    					message = createJsonMessage(nk.KeyID, messageId, payload,
						                    null, timeToLive, false);        
						            ccsClient.sendDownstreamMessage(message);
	    	        				exitSubmenu = true;
		        				}
		        				else 
		        				{
		        					log("No valid input received");
		        				}
		        			}
		        			catch (Exception ex)
		        			{
		                		log("Invalid input, try again.");
		        			}
	        			}
	        			break;
			        case 8://toggle menu
			        	if (printmenu)
			        		printmenu = false;
			        	else
			        		printmenu = true;
			        	break;
			        case 9://stop server?
			        	exit = true;
			        	log("Stopping server");
			        	break;
			    	default: 
			    		break;
	        		}
	        	}
	        	catch (Exception e)
	        	{
	        		log("Invalid input, try again.");
	        	}
        	}
        }
        scanner.close();
    }

	private static void printSubMenu(int submenu)
    {
    	if (submenu == 1 || submenu == 2)
    	{
        	log("");
            log("What client should the notification be sent to?");
    	}
    	else if (submenu == 3)
    	{
        	log("");
            log("What clients should the notification be sent to?");
        	log("(Seperate multiple clients by a space \" \")");
    	}
        for (int i = 0; i < clients.size(); i++)
        {
        	log("Client " + i);
        }
    }
    
    private static void printMenu()
    {
    	log("");
    	log("");
        log("What should the server do?");
        log("1. Send regular notification to client.");
        log("2. Send collapsible notification to client. (Will only deliver last message to app)");
        log("3. Send regular notification to multiple clients using notificationkey.");
        log("4. Send collapsible notification to multiple clients using notificationkey.");
        //log("5. Send regular notification to notificationkey which one client can dismiss for others.");
        log("8. Toggle printing menu.");
        log("9. Stop server.");
    	log("");
    	log("Input choice please:");
    }

    /**
     * XMPP Packet Extension for GCM Cloud Connection Server.
     */
    private static final class GcmPacketExtension extends DefaultPacketExtension {

        private final String json;

        public GcmPacketExtension(String json) {
            super(GCM_ELEMENT_NAME, GCM_NAMESPACE);
            this.json = json;
        }

        public String getJson() {
            return json;
        }

        @Override
        public String toXML() {
            return String.format("<%s xmlns=\"%s\">%s</%s>",
                    GCM_ELEMENT_NAME, GCM_NAMESPACE,
                    StringUtils.escapeForXML(json), GCM_ELEMENT_NAME);
        }

        public Packet toPacket() {
            Message message = new Message();
            message.addExtension(this);
            return message;
        }
    }

    private static final class LoggingConnectionListener
            implements ConnectionListener {

        @Override
        public void connected(XMPPConnection xmppConnection) {
            logger.info("Connected.");
        }

        @Override
        public void authenticated(XMPPConnection xmppConnection) {
            logger.info("Authenticated.");
        }

        @Override
        public void reconnectionSuccessful() {
            logger.info("Reconnecting..");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            logger.log(Level.INFO, "Reconnection failed.. ", e);
        }

        @Override
        public void reconnectingIn(int seconds) {
            logger.log(Level.INFO, "Reconnecting in " + seconds + "seconds");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            logger.info("Connection closed on error: " + e.toString());
        }

        @Override
        public void connectionClosed() {
            logger.info("Connection closed.");
        }
    }
}

//To implement: 
//GenerateNotificationKey -> key already exists with identical regIDs Error with Notification key: {"error":"notification_key already exists"}
//{"notification_key":"APA91bF1JQgPKvIoHiuywYa0-FZSEzISl55xojeZsfdQaMrU_ZKYwy3opkLzqiObOXE5gCb15_pzZG2U_MmUFEktKOXCUNJKkIEWIkcVlBhOYDLCNrlxgP0"}