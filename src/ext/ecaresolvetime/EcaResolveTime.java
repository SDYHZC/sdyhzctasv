package ext.ecaresolvetime;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import com.ptc.enterprise.slickum.model.QuerySpec;

import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeActivity2Master;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleHistory;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.part.WTPart;
import wt.query.AttributeRange;
import wt.query.SearchCondition;
import wt.util.WTException;

public class EcaResolveTime {

	private static final Logger logger = LogR.getLogger(EcaResolveTime.class
			.getName());


/**获取ECA设置为“已解决”的日期
 * 
 */	
	public static void GetEcaResolveTime(WTChangeActivity2 eca) throws WTException, IOException {
		eca.getLifeCycleState()
		
	}
	
	
}

private void queryCADLifeCycleHistory(Timestamp start, Timestamp end, boolean isFirstPub, String[] states)
		throws Exception
	{   
		if(VERBOSE)
		System.out.println(">>>" + CLASSNAME + ".queryCADLifeCycleHistory()");
		//wt.lifecycle.ObjectHistory为业务对象与LifeCycleHistory的关联
		//wt.lifecycle.ObjectHistory两个关联角色为："theLifeCycleManaged", "history"
		String state="";
		QueryResult qr=null;
		QuerySpec qs= new QuerySpec();
		AttributeRange range = new AttributeRange(start, end);
		SearchCondition sc1 = new SearchCondition(LifeCycleHistory.class, "thePersistInfo.createStamp", true, range);      
		qs.appendWhere(sc1);		        
		qs.appendAnd();		
		SearchCondition sc2 = new SearchCondition(LifeCycleHistory.class, LifeCycleHistory.LIFE_CYCLE_NAME, SearchCondition.LIKE, "CADDocumentLifeCycle%");
		qs.appendWhere(sc2);		
		qs.appendAnd();
		SearchCondition sc3 = new SearchCondition(LifeCycleHistory.class, LifeCycleHistory.ACTION, SearchCondition.LIKE, "Set_State");
		qs.appendWhere(sc3);		
		qs.appendAnd();
				
		qs.appendOpenParen();		
		for(int i=0; i<states.length; i++)
		{ 
			if(i!=0)
				qs.appendOr();
			SearchCondition sc4 = new SearchCondition(LifeCycleHistory.class, LifeCycleHistory.STATE, SearchCondition.EQUAL, states[i]);
			qs.appendWhere(sc4);
		}	
		qs.appendCloseParen() ;
		
		qr= PersistenceHelper.manager.find(qs);
		if(VERBOSE)
		System.out.println("	found " + qr.size() + " LifeCycleHistory.");
		//query ObjectHistory, then get the EPMDocument
		while(qr.hasMoreElements())
		{
			Object history= qr.nextElement();
			QueryResult qr2= PersistenceHelper.manager.navigate((Persistable)history, "theLifeCycleManaged", ObjectHistory.class, true);
			boolean flag=false;
			while(qr2.hasMoreElements())
			{
				Object element= qr2.nextElement();
				EPMDocument cad= (EPMDocument)element;
				String version = wt.vc.VersionControlHelper.getVersionIdentifier((wt.vc.Versioned)cad).getValue();
				if(isFirstPub)
				{
					if(!version.equals("A"))
						continue;
				}
				else
				{
					if(version.equals("A"))
						continue;
				}
			 	flag=true;
			 	history2cad.put(history.toString(), cad);
			}
			if(flag)
				historyV.addElement(history);	   
		}     
		if(VERBOSE)
		{
			System.out.println("	found " + historyV.size() + "CAD document versions");
			System.out.println(">>>" + CLASSNAME + ".queryCADLifeCycleHistory()");
		}
	}