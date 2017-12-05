package ext.hbt.reviewobjecttype;

import java.util.HashMap;
import java.util.List;

import wt.fc.ObjectReference;
import wt.fc.PersistenceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.hbt.reviewobjecttype.model.TypeItem;

public class AddSubTypeItemProcessor extends DefaultObjectFormProcessor {
	public AddSubTypeItemProcessor()
	{

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException
	{
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		try
		{
			Object obj = nmcommandbean.getActionOid().getRefObject();
			String tableID = nmcommandbean.getRequest().getParameter("tableID");

			if (obj instanceof TypeItem)
			{
				TypeItem item = (TypeItem) obj;
				String oldfullname = item.getFullTypeName();
				// 读取页面的相关信息
				HashMap texthtp = nmcommandbean.getText();
				HashMap textareahtp = nmcommandbean.getTextArea();
				String name = (String) texthtp.get("InputTheTypeName");
				String comment = (String) textareahtp.get("comment");
				TypeItem newitem = TypeItem.newTypeItem();
				// 签审类型
				//if (tableID.equals("table__hbt_reviewObjectType_Tree_TABLE") || tableID.equals("hbt_reviewObjectType_Tree"))
				//	newitem.setItemType("00");
				// 签字类型
				//else if (tableID.equals("table__hbt_signObjectType_Tree_TABLE") || tableID.equals("hbt_signObjectType_Tree"))
					newitem.setItemType("11");
				newitem.setFullTypeName(oldfullname + ObjectTypeConstant.TYPE_SPLIT_IN + name);
				newitem.setTypeName(name);
				newitem.setParentTypeItem(ObjectReference.newObjectReference(item));
				if (comment != null && comment.trim().length() > 0)
					newitem.setTypecomment(comment);
				PersistenceHelper.manager.save(newitem);
			}

		}
		catch (WTPropertyVetoException e)
		{
			throw new WTException(e);
		}
		return formresult;
	}
}
