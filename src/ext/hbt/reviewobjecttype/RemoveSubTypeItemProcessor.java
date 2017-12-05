package ext.hbt.reviewobjecttype;

import java.util.ArrayList;
import java.util.List;

import wt.fc.PersistenceHelper;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.util.WTException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.hbt.reviewobjecttype.model.TypeItem;
import ext.hbt.signature.SignatureDataUtil;

public class RemoveSubTypeItemProcessor extends DefaultObjectFormProcessor {
	public RemoveSubTypeItemProcessor()
	{

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException
	{
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		Object obj = nmcommandbean.getActionOid().getRefObject();
		String tableID = nmcommandbean.getRequest().getParameter("tableID");
        System.out.println("          tableID=="+tableID);
		WTSet set = new WTHashSet();
		// 获取当前节点(单个对象删除）
		if (obj instanceof TypeItem)
		{
			TypeItem item=(TypeItem)obj;
			ArrayList allitem=new ArrayList();
			//获取所有的子类型
			ObjectTypeUtil.getAllSubTypeItem(item,allitem);
			allitem.add(item);
			// 签审类型
			/*
			if (tableID.equals("table__hbt_reviewObjectType_Tree_TABLE") || tableID.equals("hbt_reviewObjectType_Tree"))
			{
				//判断各个类型是否被使用，如果有，则不能进行删除。
				for(int i=0;i<allitem.size();i++)
				{
					TypeItem tempitem=(TypeItem)allitem.get(i);
					ArrayList treearray=ObjectTypeUtil.getRelatedTreeItem(tempitem);
					if(!treearray.isEmpty())
						throw new WTException(tempitem.getTypeName()+"  is used!");
				}
			}
			*/
			// 签字类型
			//else if (tableID.equals("table__hbt_signObjectType_Tree_TABLE") || tableID.equals("hbt_signObjectType_Tree"))
			//{
				for(int i=0;i<allitem.size();i++)
				{
					TypeItem tempitem=(TypeItem)allitem.get(i);
					ArrayList coordarray=SignatureDataUtil.getCoordinateItems(tempitem);
					if(!coordarray.isEmpty())
						set.addAll(coordarray);
				}
			//}
			set.addAll(allitem);
 		}
		if (!set.isEmpty())
			PersistenceHelper.manager.delete(set);

		return formresult;
	}

}
