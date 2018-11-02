/*******************************************************************************
 * Copyright (c) 2013-2016 LAAS-CNRS (www.laas.fr)
 * 7 Colonel Roche 31077 Toulouse - France
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 *     Thierry Monteil : Project manager, technical co-manager
 *     Mahdi Ben Alaya : Technical co-manager
 *     Samir Medjiah : Technical co-manager
 *     Khalil Drira : Strategy expert
 *     Guillaume Garzone : Developer
 *     François Aïssaoui : Developer
 *
 * New contributors :
 *******************************************************************************/
package jaist.tanlab.echonetipe.utils;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.ShortName;
import org.eclipse.om2m.commons.obix.Bool;
import org.eclipse.om2m.commons.obix.Contract;
import org.eclipse.om2m.commons.obix.Obj;
import org.eclipse.om2m.commons.obix.Op;
import org.eclipse.om2m.commons.obix.Str;
import org.eclipse.om2m.commons.obix.Uri;
import org.eclipse.om2m.commons.obix.io.ObixEncoder;

public class ObixUtil {
	
	/**
	 * Returns an obix XML representation describing the lamp.
	 * @param cseId - SclBase id
	 * @param appId - Application Id
	 * @param stateCont - the STATE container id
	 * @return Obix XML representation
	 */
	public static String getDescriptorRep(String cseId, String stateCont,String id , String location, String deviceType) {
		String prefix = cseId+"/"+ Constants.CSE_NAME + "/" + id;
		// oBIX
		Obj obj = new Obj();
		obj.add(new Str("type",deviceType));
		obj.add(new Str("location",location));
		obj.add(new Str("appId",id));
		// OP GetState from SCL DataBase
		Op opState = new Op();
		opState.setName("getState");
		opState.setHref(new Uri(prefix  +"/"+stateCont+"/"+ ShortName.LATEST));
		opState.setIs(new Contract("retrieve"));
		opState.setIn(new Contract("obix:Nil"));
		opState.setOut(new Contract("obix:Nil"));
		obj.add(opState);
		// OP GetState from SCL IPU
		Op opStateDirect = new Op();
		opStateDirect.setName("getState(Direct)");
		opStateDirect.setHref(new Uri(prefix + "?op="+ Operations.GET_STATE_DIRECT+"&lampid=" + id));
		opStateDirect.setIs(new Contract("execute"));
		opStateDirect.setIn(new Contract("obix:Nil"));
		opStateDirect.setOut(new Contract("obix:Nil"));
		obj.add(opStateDirect);
		// OP SwitchON
		Op opON = new Op();
		opON.setName("switchON");
		opON.setHref(new Uri(prefix + "?op="+ Operations.SET_ON +"&lampid=" + id));
		opON.setIs(new Contract("execute"));
		opON.setIn(new Contract("obix:Nil"));
		opON.setOut(new Contract("obix:Nil"));
		obj.add(opON);
		// OP SwitchOFF
		Op opOFF = new Op();
		opOFF.setName("switchOFF");
		opOFF.setHref(new Uri(prefix  + "?op=" + Operations.SET_OFF + "&lampid=" + id));
		opOFF.setIs(new Contract("execute"));
		opOFF.setIn(new Contract("obix:Nil"));
		opOFF.setOut(new Contract("obix:Nil"));
		obj.add(opOFF);
		// OP Toggle
		Op opToggle = new Op();
		opToggle.setName("toggle");
		opToggle.setHref(new Uri(prefix + "?op="+ Operations.TOGGLE +"&lampid=" + id));
		opToggle.setIs(new Contract("execute"));
		opToggle.setIn(new Contract("obix:Nil"));
		opToggle.setOut(new Contract("obix:Nil"));
		obj.add(opToggle);

		return ObixEncoder.toString(obj);
	}

	/**
	 * Returns an obix XML representation describing the current state.
	 * @param lampId - Application Id
	 * @param value - current lamp state
	 * @return Obix XML representation
	 */
	public static String getStateRep(boolean value, String id, String location, String deviceType) {
		// oBIX
		Obj obj = new Obj();
		obj.add(new Str("type",deviceType));
		obj.add(new Str("location",location));
		obj.add(new Str("lampId",id));
		obj.add(new Bool("state",value));
		return ObixEncoder.toString(obj);
	}
	
}
