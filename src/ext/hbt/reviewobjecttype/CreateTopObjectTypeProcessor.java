package ext.hbt.reviewobjecttype;

import java.util.HashMap;
import java.util.List;

import wt.fc.PersistenceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.hbt.reviewobjecttype.model.TypeItem;

public class CreateTopObjectTypeProcessor extends DefaultObjectFormProcessor {

	public CreateTopObjectTypeProcessor()
	{

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException
	{
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		// 读取页面的相关信息
		HashMap texthtp = nmcommandbean.getText();
		HashMap textareahtp = nmcommandbean.getTextArea();
		String name = (String) texthtp.get("InputTheTypeName");
		String comment=(String) textareahtp.get("comment");
		String tableID =nmcommandbean.getRequest().getParameter("tableID");
		TypeItem item=TypeItem.newTypeItem();
		try
		{
			// 签审类型
			//if (tableID.equals("table__hbt_reviewObjectType_Tree_TABLE") || tableID.equals("hbt_reviewObjectType_Tree"))
			//	item.setItemType("0");
			// 签字类型
			//else if (tableID.equals("table__hbt_signObjectType_Tree_TABLE") || tableID.equals("hbt_signObjectType_Tree"))
				item.setItemType("1");
			item.setFullTypeName(name);
			item.setTypeName(name);
			if(comment!=null&&comment.trim().length()>0)
				item.setTypecomment(comment);
		}
		catch (WTPropertyVetoException e)
		{
			throw new WTException(e);
		}
		PersistenceHelper.manager.save(item);
		return formresult;
	}
}
