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
package ext.hbt.reviewobjecttype;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import ext.hbt.reviewobjecttype.model.TypeItem;

public class ObjectTypeUtil {
	private static final String CLASSNAME = ObjectTypeUtil.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);


	public static ArrayList getSubTypeTreeItem(TypeItem item) throws WTException
	{
		ArrayList arraylist = new ArrayList();
		QuerySpec qs = new QuerySpec(TypeItem.class);
		SearchCondition sc1 = new SearchCondition(TypeItem.class, "parentTypeItem.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(item));
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
		{
			arraylist.add(qr.nextElement());
		}
		return arraylist;
	}
	public static void getAllSubTypeItem(TypeItem item, ArrayList arraylist) throws WTException
	{
		QuerySpec qs = new QuerySpec(TypeItem.class);
		SearchCondition sc1 = new SearchCondition(TypeItem.class, "parentTypeItem.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(item));
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
		{
			TypeItem subitem = (TypeItem) qr.nextElement();
			arraylist.add(subitem);
			getAllSubTypeItem(subitem, arraylist);
		}
	}
	
	public static String getFullPath(TypeItem item) throws WTException
	{
		String currentname=item.getTypeName();
		do
		{
			ObjectReference ref=item.getParentTypeItem();
			if(ref==null)
				break;
			item=(TypeItem) ref.getObject();
			if(item==null)
				break;
			String tempname=item.getTypeName();
			currentname=tempname+ObjectTypeConstant.TYPE_SPLIT_IN+currentname;
		}while(true);
		return currentname;
	}
	
}
