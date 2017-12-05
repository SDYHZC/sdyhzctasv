package ext.tan.print;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.httpgw.URLFactory;
import wt.log4j.LogR;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfProcess;
import ext.hbt.iba.IBAUtil;
import ext.hbt.signature.SignUtil;
import ext.hbt.signature.SignatureConstant;
import ext.hbt.signature.SignatureDataUtil;
import ext.hbt.workflow.WorkFlowUtil;
import ext.print.zipFileUtil;

public class PrintRequestUtil {
	private static final Logger logger = LogR.getLogger(SignUtil.class.getName());

	public static URL getDetailReportUrl(WTObject pbo) throws Exception
	{
		WTDocument doc = (WTDocument) pbo;
		ContentHolder contentholder = ContentHelper.service.getContents(doc);
		ContentItem item = ContentHelper.service.getPrimary(doc);
		ApplicationData appData = (ApplicationData) item;
		InputStream is = ContentServerHelper.service.findContentStream(appData);
		HSSFWorkbook wb = new HSSFWorkbook(is);
		HSSFSheet sheet = wb.getSheetAt(0);
		int rows = sheet.getPhysicalNumberOfRows();
		String downpath = getTempFilePath();
		String outZipFile = downpath + doc.getNumber() + "_detail.zip";
		String inputPath = downpath + doc.getNumber() + "_detail" + File.separatorChar;
		for (int r = 2; r < rows; r++)
		{
			HSSFRow row = sheet.getRow(r);
			String number = row.getCell(1).getStringCellValue();
			WTPartMaster master = getWTPartByNumber(number);
			if (master == null)
				continue;
			String name = master.getName();
			int i = name.indexOf("#");
			if (i > 0)
				name = name.substring(i, name.length());
			WTDocument detaildoc = getWTDocumentByNumber(name);
			if (detaildoc == null)
				continue;
			// 可视化中的pdf
			ApplicationData app = SignatureDataUtil.getPDFInRep((Representable) detaildoc);
			// 附件中的pdf
			if (app == null)
				app = SignatureDataUtil.getgetPDFInContent((ContentHolder) detaildoc);
			// 主文件
			if (app == null)
			{
				QueryResult contents = ContentHelper.service.getContentsByRole(detaildoc, ContentRoleType.PRIMARY);
				while (contents.hasMoreElements())
				{
					ContentItem cItem = (ContentItem) contents.nextElement();
					logger.debug("   PRIMARY contentItem  ===" + cItem.getDisplayIdentifier());
					app = (ApplicationData) cItem;
					break;
				}
			}
			if (app == null)
				continue;
			ContentServerHelper.service.writeContentStream(app, inputPath + app.getFileName());
		}
		zipFileUtil.zipFile(outZipFile, inputPath);
		int pos = outZipFile.indexOf("download");
		outZipFile = outZipFile.substring(pos);
		URLFactory urlfactory = new URLFactory();
		String strUrl = urlfactory.getHREF(outZipFile);
		URL url = new URL(strUrl);
		return url;
	}

	/**
	 * 获取默认表示法中的PDF文件
	 * 
	 * @param representable
	 * @return ApplicationData
	 * @throws WTException
	 */
	public static ApplicationData getPDFInRep(Representable representable) throws WTException
	{
		logger.debug("   getPDFInRep===" + representable);
		ApplicationData app = null;

		try
		{
			Representation representation = RepresentationHelper.service.getDefaultRepresentation(representable);
			if (representation != null)
			{
				logger.debug("     representation =" + representation.getName());

				representation = (Representation) ContentHelper.service.getContents(representation);

				Vector contents = ContentHelper.getContentListAll(representation);
				for (int i = 0; i < contents.size(); i++)
				{
					ContentItem contentItem = (ContentItem) contents.get(i);
					if (isPdfApp(contentItem))
					{
						app = (ApplicationData) contentItem;
						break;
					}
				}
			}
			return app;
		}
		catch (PropertyVetoException e)
		{
			throw new WTException(e);
		}
	}

	/**
	 * 获取内容中的PDF文件(如果附件中没有满足规则的pdf，则直接获取主文件的pdf)
	 * 
	 * @param contentHolder
	 *            对象
	 * @return ApplicationData
	 * @throws WTException
	 */
	public static ApplicationData getgetPDFInContent(ContentHolder cholder) throws WTException
	{
		logger.debug("   getgetPDFInContent  ===" + cholder);
		ApplicationData app = null;
		QueryResult contents = ContentHelper.service.getContentsByRole(cholder, ContentRoleType.SECONDARY);
		while (contents.hasMoreElements())
		{
			ContentItem cItem = (ContentItem) contents.nextElement();
			logger.debug("   SECONDARY contentItem  ===" + cItem.getDisplayIdentifier());
			if (cItem != null && cItem instanceof ApplicationData)
			{
				ApplicationData tempapp = (ApplicationData) cItem;
				String fileName = tempapp.getFileName();
				logger.debug("      SECONDARY ApplicationData.FileName=" + fileName);
				if (fileName.startsWith(SignatureConstant.PRINT_FILE_SOU) && fileName.toLowerCase().endsWith(".pdf"))
				{
					app = tempapp;
					break;
				}
			}
		}
		return app;
	}

	public static boolean isPdfApp(ContentItem item) throws WTException
	{
		boolean ispdf = false;
		if (item != null && item instanceof ApplicationData)
		{
			ApplicationData tempapp = (ApplicationData) item;
			String fileName = tempapp.getFileName();
			logger.debug("      Rep ApplicationData=" + fileName);
			if (fileName.toLowerCase().endsWith(".pdf"))
			{
				ispdf = true;
			}
		}
		return ispdf;
	}

	public static WTPartMaster getWTPartByNumber(String number) throws Exception
	{
		WTPartMaster master = null;
		if ((number != null) && (number.length() > 0))
		{
			QuerySpec qs = new QuerySpec(WTPartMaster.class);
			SearchCondition sc = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, number);
			qs.appendWhere(sc);
			QueryResult results = PersistenceHelper.manager.find(qs);
			if (results.hasMoreElements())
				master = (WTPartMaster) results.nextElement();
			return master;
		}
		return master;
	}

	public static WTDocument getWTDocumentByNumber(String number) throws Exception
	{
		WTDocumentMaster master = null;
		WTDocument doc = null;
		if ((number != null) && (number.length() > 0))
		{
			QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
			SearchCondition sc = new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NUMBER, SearchCondition.EQUAL, number);
			qs.appendWhere(sc);
			QueryResult results = PersistenceHelper.manager.find(qs);
			if (results.hasMoreElements())
				master = (WTDocumentMaster) results.nextElement();
			if (master != null)
			{
				QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
				if (queryResult.hasMoreElements())
					doc = (WTDocument) queryResult.nextElement();
			}

		}
		return doc;
	}
	public static String getTempFilePath()
	  {
	    String tempFolderPath = null;
	    Properties props;
		try {
			props = WTProperties.getLocalProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String  WT_HOME = props.getProperty("wt.home");
	    String tmpPath = WT_HOME + File.separatorChar + "codebase" + File.separatorChar + "download" + 
	      File.separatorChar;
	    long tmpTime = new Date().getTime();
	    String tmpStr = Long.toString(tmpTime);
	    tempFolderPath = tmpPath + tmpStr + File.separatorChar;
	    File dir = new File(tempFolderPath);
	    if (!dir.exists()) {
	      dir.mkdirs();
	    }

	    return tempFolderPath;
	  }

}
