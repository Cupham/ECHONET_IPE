package jaist.tanlab.echonetipe.objects;


import java.util.HashMap;

import java.util.Map;

import echowand.common.EOJ;
import echowand.net.Node;
import echowand.object.EchonetObjectException;
import jaist.tanlab.echonetipe.Activator;
import jaist.tanlab.echonetipe.controllers.TaskManager;

public class EchonetLiteDevice {
	
	private NodeProfileObject profileObj;
	private Node node;
	private Map<String, eDataObject> dataObjList;
	public EchonetLiteDevice() {
		this.dataObjList = new HashMap<String,eDataObject>();
		this.profileObj = null;
		this.node = null;
	}
	public EchonetLiteDevice(Node node) {
		this.dataObjList = new HashMap<String,eDataObject>();
		this.profileObj = null;
		this.node = node;
	}
	
	public boolean addDataObject(eDataObject dataObj) throws EchonetObjectException {
		if(this.dataObjList == null) {
			this.dataObjList = new HashMap<String,eDataObject>();
		}
		this.dataObjList.put(dataObj.getAppID(),dataObj);
		return true;
	}
	public boolean parseDataObject(EOJ eoj, Node node) throws EchonetObjectException{
		byte classGroupCode = eoj.getClassGroupCode();
		byte classCode = eoj.getClassCode();
		eDataObject dataObj = null;
		//init object class 
		
		switch (classGroupCode) {		
		case (byte) (0x00): // Sensor-related Device Class Group
			switch(classCode) {
			case (byte) (0x11): //temperature sensor
				//System.out.println("   			Creating TemperatureSensor object from ECHONET frame...");
				//dataObj = new eTemperatureSensor(eoj, node);
				break;
			case (byte) (0x12): //humidity sensor
				//TODO: implement humidity sensor class
				break;
			default:
				return false;
			}
		break;
		
		case (byte)(0x01): //air conditioner related class
			switch (classCode) {
			case (byte)(0x30):
				//System.out.println("   			Creating Air-Conditioner object from ECHONET frame...");
				//dataObj = new eAirConditioner(eoj, node);
				break;
			default:
				return false;
			}	
		break;	
		
		case (byte)(0x02): //air conditioner related class
			switch (classCode) {
			case (byte)(0x90):
				System.out.println("   			Creating Lighting object from ECHONET frame...");
				dataObj = new eLighting(eoj, node);
				break;
			default:
				return false;
			}	
		break;	
		
			//TODO: implement other device class here
		default:
			return false;
			
		}
		if(dataObj != null) {
			dataObj.ParseProfileObjectFromEPC(TaskManager.service);
			dataObj.ParseDataFromEOJ(TaskManager.service);
			dataObj.conStructSDT(Activator.context);
			this.addDataObject(dataObj);
			return true;
		}
		return false;
	}
	
	
	@Override
	public String toString() {
		StringBuilder rs = new StringBuilder();
		rs.append("\r\n*********************************************");
		rs.append("\r\n>Node IP: " + this.node.getNodeInfo().toString());
		rs.append("\r\n>Node Profile Object: \r\n");
		rs.append(this.profileObj.toString());
		rs.append("\r\n>Data Object: "+dataObjList.size()+" devices\r\n");
		
		for (eDataObject dataObj : dataObjList.values()) {
			rs.append("\r\n\t####################\r\n");
			rs.append("\t"+ dataObj.ToString()+"\r\n");
			rs.append("\t####################\r\n");
		}
		rs.append("*********************************************\r\n");
		return rs.toString();
	}
	// getter setter
	
	public NodeProfileObject getProfileObj() {
		return profileObj;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public void setProfileObj(NodeProfileObject profileObj) {
		if(!profileObj.equals(this.profileObj)) {
			this.profileObj = profileObj;
		}
		
	}
	public Map<String,eDataObject> getDataObjectList() {
		return dataObjList;
		
	}
}
