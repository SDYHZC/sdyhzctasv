package ext.hbt.reviewobjecttype;

import java.util.HashMap;
import java.util.List;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.hbt.reviewobjecttype.model.TypeItem;

public class ChangeSubTypeItemProcessor extends DefaultObjectFormProcessor {
	public ChangeSubTypeItemProcessor()
	{

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException
	{
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		try
		{
			Object obj = nmcommandbean.getActionOid().getRefObject();
			if (obj instanceof TypeItem)
			{
				TypeItem item = (TypeItem) obj;
				String oldfullname = item.getFullTypeName();
				// 读取页面的相关信息
				HashMap texthtp = nmcommandbean.getText();
				HashMap textareahtp = nmcommandbean.getTextArea();
				String name = (String) texthtp.get("InputTheTypeName");
				String comment = (String) textareahtp.get("comment");
				if(comment==null)
					comment="";
				item.setTypeName(name);
				item.setTypecomment(comment);
				item=(TypeItem) PersistenceHelper.manager.save(item);
				String currentfull=ObjectTypeUtil.getFullPath(item);
				item.setFullTypeName(currentfull);
				WTCollection col=new WTHashSet();
				col.add(item);
				updateAllSubTypeItem(item, col, currentfull);
				//递归更新所有子节点的全名
				PersistenceHelper.manager.save(col);
			}

		}
		catch (WTPropertyVetoException e)
		{
			throw new WTException(e);
		}
		return formresult;
	}
	
	public static void updateAllSubTypeItem(TypeItem item, WTCollection arraylist,String currentname) throws WTException, WTPropertyVetoException
	{
		QuerySpec qs = new QuerySpec(TypeItem.class);
		SearchCondition sc1 = new SearchCondition(TypeItem.class, "parentTypeItem.key", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(item));
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
		{
			TypeItem subitem = (TypeItem) qr.nextElement();
			String needname=currentname+ObjectTypeConstant.TYPE_SPLIT_IN+subitem.getTypeName();
			subitem.setFullTypeName(needname);
			arraylist.add(subitem);
			updateAllSubTypeItem(subitem, arraylist,needname);
		}
	}

}
