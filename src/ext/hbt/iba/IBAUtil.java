/*
 * bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC and is
 * subject to the terms of a software license agreement. You shall not disclose
 * such confidential information and shall use it only in accordance with the
 * terms of the license agreement.
 *
 * ecwti
 */
package ext.hbt.iba;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.type.TypedUtility;
import wt.util.WTException;

import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;

public class IBAUtil {
	private static final String CLASSNAME = IBAUtil.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);

	public static Map<String, Object> getAllAttribute(Persistable p)
	{
		Map dataMap = new HashMap();
		try
		{
			TypeIdentifier ti = TypedUtility.getTypeIdentifier(p);
		    TypeDefinitionReadView tdrv = TypeDefinitionServiceHelper.service.getTypeDefView(ti);
		    Collection viewcol=  tdrv.getAllAttributes();
		    ArrayList allname=new ArrayList();
		    Iterator iter=viewcol.iterator();
		    while(iter.hasNext())
		    {
		    	AttributeDefinitionReadView attvi=(AttributeDefinitionReadView)iter.next();
		    	allname.add(attvi.getName());
		    }
		    if(!allname.isEmpty())
		    	dataMap= getIBAValues(p,allname);
	
		}
		catch (WTException e)
		{
			e.printStackTrace();
		}
		return dataMap;
	}

	public static HashMap getIBAValues(Persistable targetObj, List ibanameList)
	{
		HashMap map = new HashMap();
		try
		{
			if (ibanameList != null && !ibanameList.isEmpty())
			{
				PersistableAdapter obj = new PersistableAdapter(targetObj, null, null, null);
				obj.load(ibanameList);
				String tempname = null;
				for (int i = 0; i < ibanameList.size(); i++)
				{
					tempname = (String) ibanameList.get(i);
					map.put(tempname, obj.get(tempname));
				}
			}
		}
		catch (Exception e)
		{
			loger.error(e.getMessage(), e);
		}
		return map;
	}

	public static Object getIBAValue(Persistable targetObj, String ibaname)
	{
		Object ibavalue = null;
		try
		{
			PersistableAdapter obj = new PersistableAdapter(targetObj, null, null, null);
			obj.load(ibaname);
			ibavalue = obj.get(ibaname);
		}
		catch (Exception e)
		{
			loger.error(e.getMessage(), e);
		}
		return ibavalue;
	}

}
