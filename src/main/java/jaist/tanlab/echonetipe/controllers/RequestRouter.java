package jaist.tanlab.echonetipe.controllers;

import org.eclipse.om2m.interworking.service.InterworkingService;

import jaist.tanlab.echonetipe.utils.Operations;
import jaist.tanlab.echonetipe.utils.SampleConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.exceptions.BadRequestException;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;

public class RequestRouter implements InterworkingService{
	private static Log LOGGER = LogFactory.getLog(RequestRouter.class);
	@Override
	public ResponsePrimitive doExecute(RequestPrimitive request) {
		ResponsePrimitive response = new ResponsePrimitive(request);
		if(request.getQueryStrings().containsKey("op")){
			String operation = request.getQueryStrings().get("op").get(0);
			Operations op = Operations.getOperationFromString(operation);
			String lampid= null;
			if(request.getQueryStrings().containsKey("lampid")){
				lampid = request.getQueryStrings().get("lampid").get(0);
			}
			LOGGER.info("Received request in Sample IPE: op=" + operation + " ; lampid=" + lampid);
			switch(op){
			case SET_ON:
				TaskManager.setState(lampid, true);
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case SET_OFF:
				TaskManager.setState(lampid, false);
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case TOGGLE:
				TaskManager.toggle(lampid);
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			case GET_STATE:
				// Shall not get there...
				throw new BadRequestException();
			case GET_STATE_DIRECT:
				String content = TaskManager.getFormatedLampState(lampid);
				response.setContent(content);
				request.setReturnContentType(MimeMediaType.OBIX);
				response.setResponseStatusCode(ResponseStatusCode.OK);
				break;
			default:
				throw new BadRequestException();
			}
		}
		if(response.getResponseStatusCode() == null){
			response.setResponseStatusCode(ResponseStatusCode.BAD_REQUEST);
		}
		return response;
	}

	@Override
	public String getAPOCPath() {
		// TODO Auto-generated method stub
		return SampleConstants.POA;
	}

}
