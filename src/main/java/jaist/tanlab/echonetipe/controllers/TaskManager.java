package jaist.tanlab.echonetipe.controllers;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.core.service.CseService;

import echowand.common.EOJ;
import echowand.logic.TooManyObjectsException;
import echowand.monitor.Monitor;
import echowand.monitor.MonitorListener;
import echowand.net.Inet4Subnet;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.object.EchonetObjectException;
import echowand.service.Core;
import echowand.service.Service;
import jaist.tanlab.echonetipe.objects.EchonetLiteDevice;
import jaist.tanlab.echonetipe.objects.eDataObject;
import jaist.tanlab.echonetipe.objects.eLighting;
import jaist.tanlab.echonetipe.utils.ObixUtil;
import jaist.tanlab.echonetipe.utils.SampleConstants;

public class TaskManager {
	private static Log logger = LogFactory.getLog(TaskManager.class);
	private static Core core = null;
	public static Service service = null;
	private static ArrayList<EchonetLiteDevice> echonetDevices;
	public static Map<String, Object> deviceList;
	private static String NIF;
	public static CseService CSE;
	public static int counter;

	
	public static void start() throws SubnetException, SocketException, TooManyObjectsException {
		logger.info("Init ECHONET Interface");
		counter = 0;
		NIF = "wlp1s0";
		echonetDevices = new ArrayList<EchonetLiteDevice>();
		deviceList = new HashMap<String, Object>();
		initialEchonetInterface(NIF);
	}
	public static void stop() {
		try {
			core.stopService();
		} catch (SubnetException e) {
			logger.error("Can not stop the subnet: " + e.toString());
		}
	}
	
	public static void setCse(CseService cse){
		CSE = cse;
	}
	
	public static boolean initialEchonetInterface(String NIF) throws SocketException, SubnetException, TooManyObjectsException {
		boolean isSuccessed = false;
		
		if(service == null) {
			
			NetworkInterface nif = NetworkInterface.getByName(NIF);
			core = new Core(Inet4Subnet.startSubnet(nif));
			core.startService();
			service = new Service(core);
			
			Monitor monitor = new Monitor(core);
			monitor.addMonitorListener(new MonitorListener() {
	            @Override
	            public void detectEOJsJoined(Monitor monitor, Node node, List<EOJ> eojs) {	            	
	            	System.out.println("initialEchonetInterface: detectEOJsJoined: " + node + " " + eojs);
	                EchonetLiteDevice eDevice = new EchonetLiteDevice(node);
	              //  NodeProfileObject profile = null;        
	                for(EOJ eoj :  eojs) {        	       
	            	    if(eoj.isProfileObject()) {

	                	} else if(eoj.isDeviceObject()) {
	                		try {

								eDevice.parseDataObject(eoj,node);
							} catch (EchonetObjectException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                	}
	                }
	                echonetDevices.add(eDevice);    
	            }

	            @Override
	            public void detectEOJsExpired(Monitor monitor, Node node, List<EOJ> eojs) {
	            	System.out.println("initialEchonetInterface: detectEOJsExpired: " + node + " " + eojs);
	            }
			});
			monitor.start();
			isSuccessed = true;
		} 
		
		if(isSuccessed) {
			System.out.println("Initilized ECHONET API successfully!");
		}
		return isSuccessed;
	}
	
	private static eDataObject getObjectFromID (String id) {
		eDataObject rs = null;
		for(EchonetLiteDevice device : echonetDevices) {
			for(String devID: device.getDataObjectList().keySet()) {
				if(devID.equals(id) ) {
					rs= (eDataObject)device.getDataObjectList().get(id);
				} else {
					logger.info(String.format("Device with ID %s is not exist",id));
				}
			}
			
		}
		return rs;
		
	}
	public static void updateState(boolean value,String appID, String location, String deviceType){
		// Send the information to the CSE
		String targetID = SampleConstants.CSE_PREFIX + "/" + appID + "/" + SampleConstants.DATA;
		ContentInstance cin = new ContentInstance();
		cin.setContent(ObixUtil.getStateRep(value, appID,location,deviceType));
		cin.setContentInfo(MimeMediaType.OBIX + ":" + MimeMediaType.ENCOD_PLAIN);
		RequestSender.createContentInstance(targetID, cin);
	}
	public static void setState(String appID, boolean status){
		eDataObject obj = getObjectFromID(appID);
		if(obj != null && appID.contains("LIGHT")) {
			eLighting light = (eLighting)obj;
			if(status) {
				light.setOn();
			} else {
				light.setOff();
			}
		}
	}
	
	public static void toggle(String appID){
		eDataObject obj = getObjectFromID(appID);
		if(obj != null && appID.contains("LIGHT")) {
			eLighting light = (eLighting)obj;
			light.toogle();
		}
	}
	
	public static String getFormatedLampState(String appID){
		eDataObject obj = getObjectFromID(appID);
		if(obj != null && appID.contains("LIGHT")) {
			eLighting light = (eLighting)obj;
			return ObixUtil.getStateRep(light.isOperationStatus(), appID, light.getInstallLocation(), light.getDeviceType());
		} else {
			logger.error("Can not get State!!");
			return null;
		}
	}
	

}
