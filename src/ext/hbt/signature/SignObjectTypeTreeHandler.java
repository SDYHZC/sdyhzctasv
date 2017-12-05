package ext.hbt.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;

import com.ptc.core.components.beans.TreeHandlerAdapter;

import ext.hbt.reviewobjecttype.ObjectTypeUtil;
import ext.hbt.reviewobjecttype.model.TypeItem;

public class SignObjectTypeTreeHandler extends TreeHandlerAdapter {

	private static final Logger LOGGER = LogR.getLogger(SignObjectTypeTreeHandler.class.getName());

	public SignObjectTypeTreeHandler()
	{}

	public List getRootNodes() throws WTException
	{
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		try
		{
			SessionServerHelper.manager.setAccessEnforced(false);
			ArrayList arraylist = new ArrayList();
			QuerySpec qs = new QuerySpec(TypeItem.class);
			SearchCondition sc1 = new SearchCondition(TypeItem.class, TypeItem.ITEM_TYPE, SearchCondition.EQUAL, "1");
			qs.appendSearchCondition(sc1);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements())
			{
				arraylist.add(qr.nextElement());
			}
			return arraylist;
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(bool);
		}
	}

	/**
	 * 获取子节点
	 * 
	 * @return Map
	 * @throws WTException
	 */
	public Map getNodes(List list) throws WTException
	{
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		try
		{
			SessionServerHelper.manager.setAccessEnforced(false);
			HashMap hashmap = new HashMap();
			Iterator iterator = list.iterator();
			do
			{
				if (!iterator.hasNext())
				{
					break;
				}
				Object obj = iterator.next();
				if (obj instanceof TypeItem)
				{
					ArrayList arraylist = ObjectTypeUtil.getSubTypeTreeItem((TypeItem) obj);
					hashmap.put(obj, arraylist);
				}
			}
			while (true);
			return hashmap;
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(bool);
		}
	}

}
