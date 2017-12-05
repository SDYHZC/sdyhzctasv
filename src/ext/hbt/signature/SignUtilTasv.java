package ext.hbt.signature;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.workflow.engine.WfProcess;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import ext.hbt.iba.IBAUtil;
import ext.hbt.workflow.WorkFlowUtil;

public class SignUtilTasv {
	private static final Logger logger = LogR.getLogger(SignUtilTasv.class.getName());
	public static String TEMP_PATH;
	public static WTProperties wtProperties;
	static
	{
		try
		{
			wtProperties = WTProperties.getLocalProperties();
			TEMP_PATH = wtProperties.getProperty("wt.temp");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	public static void signInWorkflow(WTObject pbo, ObjectReference self) throws WTException
	{
		//获取最终签字的对象
		ArrayList allneeds=SignatureDataUtil.getAllNeedPrint(pbo, self);
		//获取坐标，key：类型，value：坐标具体信息
		HashMap cooCacheMap=new HashMap();
		
		WfProcess process=WorkFlowUtil.getProcess(self);
		logger.debug("    process==="+process.getName());
		HashMap routesInfoMap=SignatureDataUtil.getRoutesInfo(process);
		//循环进行对象的签字
		for(int i=0;i<allneeds.size();i++)
		{  
			WTObject pobj =(WTObject) allneeds.get(i);
			HashMap attrInfor= (HashMap) IBAUtil.getAllAttribute(pobj);
            //流程的签审信息及一些其他各对象的共有信息
		    HashMap signInforMap=new HashMap();
		    signInforMap.putAll(routesInfoMap);
		    signInforMap.putAll(attrInfor);
			SignUtilTasv.signObject(pobj,signInforMap,cooCacheMap);
		}
	}
	
	public static void signObject(WTObject pbo, HashMap inforHm, HashMap cooCachHm) throws WTException
	{
		// 获取坐标的类型
		logger.debug("   signObject name ===" + pbo.getDisplayIdentifier());
		File tempFile =null;
		String printname =null;
		String newname=null;
		String lifeCycleState="";
		if (pbo instanceof EPMDocument)
		{
			EPMDocument epm = (EPMDocument) pbo;
			lifeCycleState=epm.getLifeCycleState().toString();
			
		}
		try
		{
			String objtype = SignatureDataUtilTasv.getSigneObjectType(pbo);
			ApplicationData sourceApp = SignatureDataUtilTasv.getgetPDFInContent((ContentHolder) pbo);
			if (sourceApp != null)
			{
				String tempPath = TEMP_PATH + File.separator + SignatureConstant.PRINT_TEMP + File.separator;
				String sourename = sourceApp.getFileName();
				printname=tempPath+ SignatureConstant.PRINT_FILE_PRF + sourename;
				//newname=SignatureConstant.PRINT_FILE_PRF + sourename;
				newname=SignatureConstant.PRINT_FILE_PRF + sourename;
				
				logger.debug("   source file name " + sourename);
				logger.debug("   tempPath " + tempPath);
				logger.debug("   printname " + printname);
				logger.debug("   newname " + newname);
				
				InputStream is = ContentServerHelper.service.findContentStream(sourceApp);
				PdfReader reader = new PdfReader(is);
				int pageNumber = reader.getNumberOfPages();
				logger.debug("   getNumberOfPages " + pageNumber);

				// 新生成的文件名
				tempFile= new File(printname);
				PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(tempFile));
				HashMap cooMap = null;
				boolean islastpage=false;
				logger.debug("   reader " + reader);
				for (int pageNo = 0; pageNo < pageNumber; pageNo++)
				{
					String finalCooType = PDFUtilTasv.getFinalObjType(reader, pageNo+1, objtype, pbo);
					// 从缓存里面读取坐标信息
					cooMap = getCoordInfor(finalCooType, cooCachHm);
					
					logger.debug("   getCoordInfor ===" + cooMap);
					if(cooMap==null||cooMap.isEmpty())
						continue;
					if(pageNo==pageNumber-1)
						islastpage=true;
					PDFUtilTasv.writeToPage(lifeCycleState,stamp, cooMap, inforHm,pageNo+1,islastpage);
				}
				stamp.close();
			}
		}
		//出现错误情况的处理
		catch (DocumentException e)
		{
			throw new WTException(e);
		}
		catch (IOException e)
		{
			throw new WTException(e);
		}
		if(tempFile !=null &&  newname !=null)
		{
			Transaction trx = new Transaction();
			try
			{
				trx.start();
				updatePrintFile(tempFile, newname, (ContentHolder) pbo);
				trx.commit();
				trx = null;
			}
			finally
			{
				if (trx != null)
				{
					trx.rollback();
				}
			}
		}      
	}

	public static HashMap getCoordInfor(String finalCooType, HashMap cooHm) throws WTException
	{
		HashMap cooConfigMap = null;
		if (cooHm.get(finalCooType) != null)
			cooConfigMap = (HashMap) cooHm.get(finalCooType);
		else
		{
			cooConfigMap = SignatureDataUtilTasv.getCooConfigByType(finalCooType);
			cooHm.put(finalCooType, cooConfigMap);
		}
		return cooConfigMap;
	}

	public static void updatePrintFile(File newFile, String printname, ContentHolder holder) throws WTException
	{
		try
		{
			// 将电子签名后的文件更到文档对象的附件中；如果附件中已经存在同名的文件，则更新；
			ApplicationData eSignFile = null;
			holder = ContentHelper.service.getContents(holder);
			QueryResult qr = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
			while(qr.hasMoreElements())
			{
				ContentItem contentItem = (ContentItem) qr.nextElement();
				if (contentItem instanceof ApplicationData)
				{
					String fileName = ((ApplicationData) contentItem).getFileName();
					logger.debug("   old file name " + fileName);
					if (fileName.equals(printname))
					{
						eSignFile = ((ApplicationData) contentItem);
						logger.debug("    got old existed print file");
					}
				}
			}
			if (eSignFile == null)
			{
				eSignFile = ApplicationData.newApplicationData(holder);
				eSignFile.setFileName(printname);
				eSignFile.setRole(ContentRoleType.SECONDARY);
				eSignFile = ContentServerHelper.service.updateContent(holder, eSignFile, new FileInputStream(newFile), true);
				logger.debug("    create new Secondary file=" + printname);
			}
			else
			{
				eSignFile = ContentServerHelper.service.updateContent(holder, eSignFile, new FileInputStream(newFile), false);
				logger.debug("    update Secondary file=" + printname);
			}
		}
		catch (FileNotFoundException e)
		{
			throw new WTException(e);
		}
		catch (PropertyVetoException e)
		{
			throw new WTException(e);
		}
		catch (IOException e)
		{
			throw new WTException(e);
		}
		finally
		{
			if(newFile.exists())
				newFile.delete();
		}
	}

}
