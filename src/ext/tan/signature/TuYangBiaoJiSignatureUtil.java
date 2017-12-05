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
import ext.hbt.signature.SignUtilTasv;

public class TuYangBiaoJiSignatureUtil {
	private static final Logger logger = LogR.getLogger(SignUtil.class.getName());

	public static void signInWorkflow(WTObject pbo, ObjectReference self) throws WTException
	{
		//获取最终签字的对象
		ArrayList allneeds=SignatureDataUtil.getAllNeedPrint(pbo, self);
		//获取坐标，key：类型，value：坐标具体信息
		HashMap cooCacheMap=new HashMap();
		HashMap attrInfor=new HashMap();
		attrInfor.put("C", "C");
		attrInfor.put("S", "S");
		attrInfor.put("D", "D");
		//循环进行对象的签字
		for(int i=0;i<allneeds.size();i++)
		{  
			WTObject pobj =(WTObject) allneeds.get(i);
			SignUtilTasv.signObject(pobj,attrInfor,cooCacheMap);
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
