
package ext.tasv.pdfexport.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import ext.tasv.pdfexport.resource.ExportPDFActionsRB;

/**
 * @Author: gzhou
 * @Date: 2014-12-14
 * @Description:
 */
public class ReadXMLUtil {
	
	private static String RESOURCE = null;
	
	private static Locale LOCALE = null;
	
	private static WTProperties wtProperties;
	
	private static String CODEBASE_LOCATION = "";
	
	private String FILE_PATH = "";
	
	/**
	 * @param pATH
	 * @param fILE_PATH
	 */
	public ReadXMLUtil(String fILE_PATH) {
	
		FILE_PATH = CODEBASE_LOCATION + File.separator + fILE_PATH;
	}
	
	static {
		try {
			RESOURCE = ExportPDFActionsRB.class.getName();
			LOCALE = SessionHelper.manager.getLocale();
			wtProperties = WTProperties.getLocalProperties();
			CODEBASE_LOCATION = wtProperties.getProperty("wt.codebase.location" , "");
		} catch (Throwable throwable) {
			throw new ExceptionInInitializerError(throwable);
		}
	}
	
	/**
	 * @throws WTException
	 * @Author: gzhou
	 * @Date: 2014-8-20 下午04:30:40
	 * @Description:
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 */
	public Vector <String> getBusinessRuleFromXML(String lable) throws WTException {
	
		Vector <String> vc = new Vector <String>();
		SAXReader reader = new SAXReader();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(FILE_PATH);
		} catch (FileNotFoundException e) {
			throw new WTException(e.getLocalizedMessage());
		}
		Document document = null;
		try {
			document = reader.read(fis);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		// 读取XML文件
		
		Element root = document.getRootElement();// 得到根节点
		Element pm25_list = root.element(lable);
		if (pm25_list == null)
			throw new WTException(WTMessage.getLocalizedMessage(RESOURCE , "PRIVATE_CONSTANT_6" , null , LOCALE) + lable);
		@SuppressWarnings ("unchecked")
		List <Element> typeList = pm25_list.elements();
		
		for (Element type : typeList) {
			String value = type.attributeValue("name");
			vc.add(value);
		}
		return vc;
	}
}
