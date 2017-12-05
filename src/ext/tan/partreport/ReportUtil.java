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
package ext.tan.partreport;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.ptc.core.lwc.server.PersistableAdapter;

import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTProperties;

public class ReportUtil {
	private static final String CLASSNAME = ReportUtil.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);

	public static HashMap getObjectValues(Persistable targetObj, List ibanameList)
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

	public static String getTemplatePath() throws WTException
	{
		try
		{
			WTProperties wtProperties = WTProperties.getLocalProperties();
			String path = wtProperties.getProperty("ext.tan.partReportTemplatePath");
			if (path == null || path.trim().length() == 0)
			{
				String rootname = wtProperties.getProperty("wt.codebase.location");
				path = rootname + File.separator + "ext" + File.separator + "tan" + File.separator + "config";// �ļ��ݴ�·��
			}
			return path;
		}
		catch (Exception e)
		{
			throw new WTException(e);
		}
	}
	
	public static String getNameStr(String oldStr,String nameFlex,boolean isbefore)  
	{
		String newstr=oldStr;
		int index=oldStr.indexOf(nameFlex);
		//存在#
		if(index!=-1)
		{
			if(isbefore)
				newstr=oldStr.substring(0,index);
			else
				newstr=oldStr.substring(index+1,oldStr.length());
		}
		return newstr;
	}
	
	public static String getStrBeforeChs(String str)
	{
	   StringBuffer buf=new StringBuffer("");
	   for(int i=0;i<str.length();i++)
	   {
		   String chrStr=String.valueOf(str.charAt(i));
		   byte[] chr=chrStr.getBytes();
		   //在windchill服务中，获取到的中文字符的字节数是2
		   if(chr.length==2)
			   break;
		   buf.append(chrStr);
	   }
	   return buf.toString();
	}
	 
	public static int getAllCount(HashMap allMap) throws IOException
	{
		int sum = 0;
		Iterator keysets = allMap.keySet().iterator();
		while (keysets.hasNext())
		{
			Object key=keysets.next();
			List temlist = (List) allMap.get(key);
			if (temlist!=null && !temlist.isEmpty())
				sum = sum + temlist.size();
			else
				sum=sum+1;
		}
		return sum;
	}

}
