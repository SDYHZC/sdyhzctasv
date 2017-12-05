package ext.workflow;

import java.util.Vector;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.lifecycle.LifeCycleManaged;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartStandardConfigSpec;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.struct.StructHelper;
import wt.vc.views.ViewHelper;

public class CollectionGuide 
{
	public static WTPartConfigSpec wtPartStandardConfigSpec;
	static {
		try {
			// 获取最新设计视图配置规范
			wtPartStandardConfigSpec = WTPartConfigSpec.newWTPartConfigSpec(WTPartStandardConfigSpec
					.newWTPartStandardConfigSpec(ViewHelper.service.getView("Design"), null));
		} catch (WTException wte) {
			System.out.println(wte);
		}
	}
	public static Vector getAllObj(WTPart part,Vector vecAllObj) throws WTException
	{
		String cState = null;			
		cState = part.getLifeCycleState().toString();
		//此处根据状态定义，只查找状态为RELEASED_S
		if (cState.equals("RELEASED_S")&& !vecAllObj.contains(part)) 
		{
			vecAllObj.add(part);
			// get docs
			QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
			while (qr.hasMoreElements()) 
			{
				Persistable obj = (Persistable) qr.nextElement();
				if (obj instanceof LifeCycleManaged) 
				{
					cState = ((LifeCycleManaged) obj).getLifeCycleState().toString();
					if (cState.equals("RELEASED_S")) 
					{
						if (obj instanceof WTDocument) 
						{
							WTDocument doc = (WTDocument) obj;
							vecAllObj.add(doc);
						} 
						else 
						{
							EPMDocument epm = (EPMDocument) obj;
							if(!vecAllObj.contains(epm)){
								vecAllObj.add(epm);
							}							
							vecAllObj.addAll(getSubEPM(epm));
						}
					}
				}
			  }
			QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
			while (subNodes.hasMoreElements()) {
				Persistable aSubNodePair[] = (Persistable[]) subNodes.nextElement();
				if (aSubNodePair[1] instanceof WTPart) {
					WTPart childpart = (WTPart) aSubNodePair[1];
					System.out.println("zyj--test--childpart:"+childpart);
					getAllObj(childpart, vecAllObj);						
				} else if (aSubNodePair[1] instanceof WTPartMaster) {
					continue;
				}
			}
		}
		
		return vecAllObj;
		
	}
	//递归获取所有子阶EPMDocument
	public static Vector getSubEPM(EPMDocument epm) throws WTException
	{
	    Vector allVector = new Vector();
	    QueryResult qs = StructHelper.service.navigateUses(epm);
        while(qs.hasMoreElements())
        {
            EPMDocument epm1 = (EPMDocument)getLatestObject((EPMDocumentMaster)qs.nextElement());
            String cState=epm1.getLifeCycleState().toString();
            if(cState.equals("RELEASED_S"))
            {
                allVector.add(epm1);
          }                
          allVector.addAll(getSubEPM(epm1));
            
        }
        return allVector;
	}
	//获取对象最新版本
	public static RevisionControlled getLatestObject(Mastered master) throws PersistenceException, WTException
	{
		if (master != null)
		{
			QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
			return getLatestObject(queryResult);
		}
		return null;
	}

	public static RevisionControlled getLatestObject(QueryResult queryresult) throws WTException
	{
		RevisionControlled rc = null;
		if (queryresult != null)
		{
			while (queryresult.hasMoreElements())
			{
				RevisionControlled obj = ((RevisionControlled) queryresult.nextElement());
				if (rc == null || obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))
				{
					rc = obj;
				}
			}
			if (rc != null)
			{
				return (RevisionControlled) wt.vc.VersionControlHelper.getLatestIteration(rc, false);
			} else
			{
				return rc;
			}
		}
		return rc;
	}
}
