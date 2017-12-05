package ext.hbt.signature;

import java.util.ArrayList;
import java.util.List;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.WTObject;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.util.WTException;

import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NmContext;

import ext.hbt.signature.model.CoordinateItem;

public class RemoveSignatureProcessor extends DefaultObjectFormProcessor {
	public RemoveSignatureProcessor()
	{

	}

	public FormResult doOperation(NmCommandBean nmcommandbean, List list) throws WTException
	{
		FormResult formresult = new FormResult(FormProcessingStatus.SUCCESS);
		NmOid nmoid = nmcommandbean.getActionOid();
		Object obj = null;
		if (nmoid != null)
			obj = nmcommandbean.getActionOid().getRefObject();
		// 获取当前节点(单个对象删除）
		if (obj instanceof CoordinateItem)
		{
			PersistenceHelper.manager.delete((Persistable) obj);
		}
		// 多个对象一起删除
		else
		{
			ArrayList arraylist = nmcommandbean.getSelected();
			WTSet set = new WTHashSet();
			for (int i = 0; i < arraylist.size(); i++)
			{
				NmContext nmcontext = (NmContext) arraylist.get(i);
				NmOid tempnmoid = nmcontext.getTargetOid();
				if (tempnmoid.getWtRef() != null)
				{
					WTObject selectobject = (WTObject) tempnmoid.getWtRef().getObject();
					if (selectobject instanceof CoordinateItem)
					{
						set.add(selectobject);
					}
				}
			}
			if (!set.isEmpty())
				PersistenceHelper.manager.delete(set);
		}

		return formresult;
	}

}
