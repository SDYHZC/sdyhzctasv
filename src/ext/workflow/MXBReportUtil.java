package ext.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTRuntimeException;

public class MXBReportUtil 
{
	/**
	 * 获取零件与零件的使用关系
	 * @param parentPart
	 * @param childPartMaster
	 * @return
	 * @throws WTException
	 */
	public WTPartUsageLink getPartUsageLink(WTPart parentPart, WTPartMaster childPartMaster) throws WTException
	{
		WTPartUsageLink partusagelink = null;
		if (parentPart != null && childPartMaster != null)
		{
			QueryResult queryresult = PersistenceHelper.manager.find(WTPartUsageLink.class, parentPart, WTPartUsageLink.USED_BY_ROLE, childPartMaster);
			while (queryresult.hasMoreElements())
				partusagelink = (WTPartUsageLink) queryresult.nextElement();
		}
		return partusagelink;
	}
	/**
	 * 根据oid获取对象
	 * @param strOid
	 * @return
	 * @throws WTException
	 */
	public static Persistable getPersistableByOid(String strOid) throws WTException
	{
		if (strOid != null && strOid.trim().length() > 0)
		{
			ReferenceFactory referencefactory = new ReferenceFactory();
			WTReference wtreference = referencefactory.getReference(strOid);
			if (wtreference != null)
			{
				try
				{
					Persistable persistable = wtreference.getObject();
					return persistable;
				} catch (WTRuntimeException e)
				{
					return null;
				}
			}
		}
		return null;
	}
	/**
	 * 将现有的模板复制到另外一个文件中，使该文件拥有相类似的内容
	 * templateName
	 * outFile
	 */
	public static String copyExcelMode(String templateName, String outFile) throws IOException{
		
		String templateFile = templateName;
		WTProperties props = WTProperties.getLocalProperties();
		// TemplateFile是模板文件
		templateFile = props.getProperty("wt.home") + File.separatorChar + "codebase" + File.separatorChar
				+ "ext" +  File.separatorChar +"workflow" + File.separatorChar 
				+ "template" + File.separatorChar + templateFile;
		String outFilePath = props.getProperty("wt.home") + File.separatorChar + "codebase"
				+ File.separatorChar + "temp" + File.separatorChar + outFile;
		
		XSSFWorkbook wb = readFile(templateFile);
		XSSFSheet s = wb.getSheetAt(0);
		FileOutputStream stream = new FileOutputStream(outFilePath);
		wb.write(stream);
		stream.close();
		return outFilePath;
	}
	public static XSSFWorkbook readFile(String filename) throws IOException
	{
		return new XSSFWorkbook(new FileInputStream(filename));
	}
}
