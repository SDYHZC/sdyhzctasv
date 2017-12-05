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
package ext.hbt.signature;

import java.util.ArrayList;
import java.util.Locale;

import org.apache.log4j.Logger;

import wt.fc.ReferenceFactory;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.session.SessionHelper;
import wt.util.WTException;
import ext.hbt.reviewobjecttype.model.TypeItem;
import ext.hbt.signature.model.CoordinateItem;

public class SignatureCommand implements RemoteAccess
{
	private static final String CLASSNAME = SignatureCommand.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	private static  Locale locale;
	static
	{
		try
		{
			locale = SessionHelper.manager.getLocale();
		}
		catch (Throwable throwable)
		{
			throw new ExceptionInInitializerError(throwable);
		}
	}
   
	public static String getObjectType(String typeitemoid) throws WTException
	{
		String type="";
		ReferenceFactory referencefactory = new ReferenceFactory();
		TypeItem typeitem = (TypeItem) referencefactory.getReference(typeitemoid).getObject();
		if(typeitem!=null)
		{
			type=typeitem.getTypeName();
		}
		return type;
	}
	public static String getObjectType(CoordinateItem item) throws WTException
	{
		String type="";
		TypeItem typeitem = (TypeItem) item.getTargetTypeRef().getObject();
		if(typeitem!=null)
		{
			type=typeitem.getTypeName();
		}
		return type;
	}
	public static ArrayList getAttrTypeCombox(CoordinateItem  item ) throws WTException
	{
		ArrayList list =new ArrayList();
		ArrayList inArray=new ArrayList();
		ArrayList outArray=new ArrayList();
		inArray.add(SignatureConstant.ATTR_TYPE_VALUE_ACT);
		inArray.add(SignatureConstant.ATTR_TYPE_VALUE_ATTR);
		inArray.add(SignatureConstant.ATTR_TYPE_VALUE_OTHER);
		outArray.add("活动");
		outArray.add("属性");
		outArray.add("其它");
		ArrayList defArray=new ArrayList();
		if(item==null)
			defArray.add(SignatureConstant.ATTR_TYPE_VALUE_ACT);
		else
			defArray.add(item.getAttrType());
		list.add(inArray);
		list.add(outArray);
		list.add(defArray);
		return list;
	}
	public static ArrayList getFormTypeCombox(CoordinateItem  item ) throws WTException
	{
		ArrayList list =new ArrayList();
		ArrayList inArray=new ArrayList();
		ArrayList outArray=new ArrayList();
		inArray.add(SignatureConstant.DEFAULT_FONT_TYPE);
 		outArray.add("华文宋体");
		ArrayList defArray=new ArrayList();
		if(item==null)
			defArray.add(SignatureConstant.DEFAULT_FONT_TYPE);
		else
			defArray.add(item.getFontType());
		list.add(inArray);
		list.add(outArray);
		list.add(defArray);
		return list;
	}
	public static ArrayList getContentTypeCombox(CoordinateItem  item ) throws WTException
	{
		ArrayList list =new ArrayList();
		ArrayList inArray=new ArrayList();
		ArrayList outArray=new ArrayList();
		inArray.add(SignatureConstant.DEFAULT_CONTENT_TYPE);
		inArray.add(SignatureConstant.CONTENT_TYPE_IMAGE);
 		outArray.add("文本");
 		outArray.add("图片");
		ArrayList defArray=new ArrayList();
		if(item==null)
			defArray.add(SignatureConstant.DEFAULT_CONTENT_TYPE);
		else
			defArray.add(item.getContentType());
		list.add(inArray);
		list.add(outArray);
		list.add(defArray);
		return list;
	}
}
