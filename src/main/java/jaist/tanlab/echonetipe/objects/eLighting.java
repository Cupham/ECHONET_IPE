package jaist.tanlab.echonetipe.objects;


import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.osgi.framework.BundleContext;

import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.object.EchonetObjectException;
import echowand.object.ObjectData;
import echowand.object.RemoteObject;
import echowand.service.Service;
import echowand.service.result.GetListener;
import echowand.service.result.GetResult;
import echowand.service.result.ObserveListener;
import echowand.service.result.ObserveResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;
import jaist.tanlab.echonetipe.Activator;
import jaist.tanlab.echonetipe.controllers.RequestSender;
import jaist.tanlab.echonetipe.controllers.TaskManager;
import jaist.tanlab.echonetipe.utils.EchonetDataConverter;
import jaist.tanlab.echonetipe.utils.ObixUtil;
import jaist.tanlab.echonetipe.utils.SampleConstants;

public class eLighting extends eDataObject{
	private static Log logger = LogFactory.getLog(eLighting.class);
	
	private int illuminateLevel;
	private EOJ eoj;
	private Node node;
	private boolean operationStatus;
	Timer timer;
	
	
	public int getIlluminateLevel() {
		return illuminateLevel;
	}
	public void setIlluminateLevel(int illuminateLevel) {
		if(this.getIlluminateLevel() != illuminateLevel) {
			this.illuminateLevel = illuminateLevel;
		}
	}
	
	public boolean isOperationStatus() {
		return operationStatus;
	}
	public void setOperationStatus(boolean operationStatus) {
		if(operationStatus != isOperationStatus()) {
			this.operationStatus = operationStatus;
			TaskManager.updateState(operationStatus, getAppID(), this.getInstallLocation(), getDeviceType());
		}
	}
	public eLighting() {
		super();
		this.groupCode= (byte)0x02;
		this.classCode=(byte)0x90;
	}
	public eLighting(EOJ eoj, Node node) {
		super(node,eoj);
		this.groupCode= (byte) 0x02;
		this.classCode = (byte) 0x90;
		this.instanceCode = eoj.getInstanceCode();
		this.eoj = eoj;
		this.node = node;
		TaskManager.counter+=1;
		setAppID("LIGHT_"+TaskManager.counter);
		setDeviceType("Light");
		
	}
	
	@Override
	public String ToString() {
		StringBuilder rs = new StringBuilder();
		rs.append("Operation Status: " + (this.isOperationStatus() ? "ON" : "OFF"));
		rs.append("Illuminance level: " + this.illuminateLevel + " %");
		return null;
	}
	public void getData(Service service){
		LinkedList<EPC> epcs = new LinkedList<EPC>();
		epcs.add(EPC.x80);
		epcs.add(EPC.xB0);
		try {
			service.doGet(node, eoj, epcs, 5000, new GetListener() {
				@Override
			    public void receive(GetResult result, ResultFrame resultFrame, ResultData resultData) {
					if (resultData.isEmpty()) {
						return;
					}
					switch (resultData.getEPC()) 
					{
					case x80:
						if(EchonetDataConverter.dataToInteger(resultData) == 48) {
							setOperationStatus(true);
						} else {
							setOperationStatus(false);
						}
						logger.info(String.format("Lighting:%s {EPC:0x80, EDT: 0x%02X}=={OperationStatus:%s}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],isOperationStatus()));	
						break;
					case xB0:
						int level = EchonetDataConverter.dataToInteger(resultData);
						setIlluminateLevel(level);
						logger.info(String.format("Lighting:%s {EPC:0xB0, EDT: 0x%02X}=={Illumination Level = :%d}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],getIlluminateLevel()));	
						break;
						
					default:
						logger.error("Something happended when loading lighting device!!");
						break;
					}	
				}	
			});
		} catch (SubnetException e) {
			e.printStackTrace();
		}
	}
	public void observeStatus(Service service) {
		LinkedList<EPC> epcs = new LinkedList<EPC>();
		epcs.add(EPC.x80);
		epcs.add(EPC.xB0);
		try {
			service.doObserve(node, eoj, epcs, new ObserveListener() {
				@Override
			    public void receive(ObserveResult result, ResultFrame resultFrame, ResultData resultData) {
					if (resultData.isEmpty()) {
						return;
					}
					switch (resultData.getEPC()) 
					{
					case x80:
						if(EchonetDataConverter.dataToInteger(resultData) == 48) {
							setOperationStatus(true);
						} else {
							setOperationStatus(false);
						}
						logger.info(String.format("OB:Lighting:%s {EPC:0x80, EDT: 0x%02X}=={OperationStatus:%s}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],isOperationStatus()));	
						break;
					case xB0:
						int level = EchonetDataConverter.dataToInteger(resultData);
						setIlluminateLevel(level);
						logger.info(String.format("OB:Lighting:%s {EPC:0xB0, EDT: 0x%02X}=={Illumination Level = :%d}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],getIlluminateLevel()));	
						break;
						
					default:
						logger.error("Something happended when loading lighting device!!");
						break;
					}	
				}	
			});
		} catch (SubnetException e) {
			e.printStackTrace();
		}
		
	}
	public boolean setOn() {
		boolean rs = false;
		if(isOperationStatus()) {
			logger.info("Light is already ON! nothing to do");
			rs = true;
		} else {
			if(executeCommand(EPC.x80, new ObjectData((byte) 0x30))) {
				this.operationStatus = true;
				rs= true;
			} else {
				rs = false;
			}
		}
		return rs;
		
	}
	public boolean setOff() {
		boolean rs = false;
		if(!isOperationStatus()) {
			logger.info("Light is already OFF! nothing to do");
			rs = true;
		} else {
			if(executeCommand(EPC.x80, new ObjectData((byte) 0x31))) {
				this.operationStatus = false;
				rs= true;
			} else {
				rs = false;
			}
		}
		return rs;
	}
	public boolean toogle() {
		if(isOperationStatus()) {
			return setOff();
		} else {
			return setOn();
		}
	}
	public boolean executeCommand(EPC epc, ObjectData data) {
		boolean rs = false;
		TaskManager.service.registerRemoteEOJ(this.node, this.eoj);
		RemoteObject remoteObject = TaskManager.service.getRemoteObject(node, eoj);
		logger.info(String.format("Execute command [IP:%s, EOJ:%s, Data:%s]",this.node.getNodeInfo().toString(),this.eoj,data));
		try {
			if (remoteObject.setData(epc, data)) {
				rs= true;
				logger.info(String.format("Completed: [IP:%s, EOJ:%s, Data:%s]",this.node.getNodeInfo().toString(),this.eoj,data));
			}
		} catch (EchonetObjectException e) {
			logger.error("Can not find object: " +e.toString());
			rs= false;
		}
		return rs;
	}
	
	@Override
	public void ParseDataFromEOJ(Service service){
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				getData(service);
			}
		}, 0, 10000);	
		observeStatus(service);
	}
	@Override
	public void conStructSDT(BundleContext context) {
		Container container = new Container();
		container.getLabels().add("Light");
		container.setMaxNrOfInstances(BigInteger.valueOf(0));
		
		AE ae = new AE();
		ae.setRequestReachability(true);
		ae.getPointOfAccess().add("sample");
		TaskManager.deviceList.put(getAppID(), this);
		ae.setAppID(getAppID());
		ae.setName(getAppID());
		
		ResponsePrimitive response = RequestSender.createAE(ae);
		
		if(response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			container = new Container();
			container.setMaxNrOfInstances(BigInteger.valueOf(10));
			
			container.setName(SampleConstants.DESC);
			logger.info(RequestSender.createContainer(response.getLocation(), container));
			container.setName(SampleConstants.DATA);
			logger.info(RequestSender.createContainer(response.getLocation(), container));
			
			String content = ObixUtil.getDescriptorRep(SampleConstants.CSE_ID, SampleConstants.DATA, getAppID(), this.getInstallLocation(), getDeviceType());
			ContentInstance contentInstance = new ContentInstance();
			contentInstance.setContent(content);
			contentInstance.setContentInfo(MimeMediaType.OBIX);
			RequestSender.createContentInstance(
					SampleConstants.CSE_PREFIX + "/" + getAppID() + "/" + SampleConstants.DESC, contentInstance);
			
			content = ObixUtil.getStateRep(isOperationStatus(), getAppID(),this.getInstallLocation(), getDeviceType());
			contentInstance.setContent(content);
			RequestSender.createContentInstance(
					SampleConstants.CSE_PREFIX + "/" + getAppID() + "/" + SampleConstants.DATA, contentInstance);	
		}
	
	}

}
