package ext.tan.signature;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.workflow.engine.WfProcess;
import ext.hbt.signature.SignUtil;
import ext.hbt.signature.SignatureDataUtil;
import ext.hbt.workflow.WorkFlowUtil;

public class UGSignatureUtil {
	private static final Logger logger = LogR.getLogger(SignUtil.class.getName());

	public static void signInWorkflow(WTObject pbo, ObjectReference self) throws WTException
	{
		//获取最终签字的对象
		ArrayList allneeds=SignatureDataUtil.getAllNeedPrint(pbo, self);
		//泰航只先签名UG类型的s
		ArrayList finalPrints = getAllUG(allneeds);
		//获取坐标，key：类型，value：坐标具体信息
		HashMap cooCacheMap=new HashMap();
		
		WfProcess process=WorkFlowUtil.getProcess(self);
		logger.debug("    process==="+process.getName());
		HashMap routesInfoMap=SignatureDataUtil.getRoutesInfo(process);
		//循环进行对象的签字
		for(int i=0;i<finalPrints.size();i++)
		{  
			WTObject pobj =(WTObject) finalPrints.get(i);
		//	HashMap attrInfor= (HashMap) IBAUtil.getAllAttribute(pobj);
            //流程的签审信息及一些其他各对象的共有信息
		    HashMap signInforMap=new HashMap();
		    signInforMap.putAll(routesInfoMap);
		//    signInforMap.putAll(attrInfor);
			SignUtil.signObject(pobj,signInforMap,cooCacheMap);
		}
	}
	
	public static ArrayList getAllUG(ArrayList allneeds) throws WTException
	{
		ArrayList arraylist = new ArrayList();
		for(int i=0;i<allneeds.size();i++)
		{
			WTObject obj=(WTObject) allneeds.get(i);
			if(obj instanceof EPMDocument)
			{
				EPMDocument epm=(EPMDocument)obj;
				String epmapp=epm.getAuthoringApplication().toString();
				logger.debug("        getAuthoringApplication==="+epmapp);
		        if (epmapp.equals("UG"))
		        {
		        	arraylist.add(epm);
			}
		  }
		}
		logger.debug("    getAllUG==="+arraylist);
		return arraylist;
	}
	
	
	
	
}
