
package ext.tasv.pdfexport.commands;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.jsp.JspWriter;

import wt.ixb.handlers.netmarkets.JSPFeedback;
import wt.ixb.handlers.netmarkets.NmFeedbackSpec;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTMessage;

import com.ptc.netmarkets.model.NmException;
import com.ptc.netmarkets.model.NmObjectHelper;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.misc.NetmarketURL;

import ext.tasv.pdfexport.util.ExportPdfUtil;

/**
 * 构造下载页面
 */
public class ExportPdfCommands implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final char [] blanks = new char [4096];
	
	public static final String DEFAULT_ARCHIVE_FILE_NAME = "Export_Pdf.zip";
	
	/**
	 * 导出PDF对象
	 * 
	 * @param paramNmCommandBean
	 *            NmCommandBean对象
	 * @throws Throwable
	 *             异常
	 */
	public static void exportObjects(NmCommandBean paramNmCommandBean) throws Throwable {
	
		JSPFeedback localJSPFeedback = null;
		StringBuffer localStringBuffer = new StringBuffer();
		JspWriter localJspWriter = paramNmCommandBean.getOut();
		String str = null;
		NmOid localNmOid = paramNmCommandBean.getPrimaryOid();
		NmFeedbackSpec localNmFeedbackSpec = null;
		try {
			if (paramNmCommandBean.getSessionBean() != null) {
				localNmFeedbackSpec = new NmFeedbackSpec();
				localJSPFeedback = new JSPFeedback(localNmFeedbackSpec);
				localNmFeedbackSpec.setTitle(WTMessage.getLocalizedMessage("com.ptc.netmarkets.model.modelResource" , "15" , null , SessionHelper.getLocale()));
				
				paramNmCommandBean.getSessionBean().getStorage().put("eo1" , localNmFeedbackSpec);
			}
			
			Object localObject1;
			try {
				localStringBuffer.append("<SCRIPT LANGUAGE=\"JavaScript1.2\">\n");
				localStringBuffer.append("document.open();\n");
				localStringBuffer.append("pageLoadComplete=true;\n");
				
				HashMap <String , String> localHashMap = new HashMap <String , String>(2);
				localHashMap.put("portlet" , "poppedup");
				localHashMap.put("feedbackkey" , "eo1");
				localObject1 = NetmarketURL.buildURL(paramNmCommandBean.getUrlFactoryBean() , "util" , "status" , localNmOid , localHashMap , true);
				
				localStringBuffer.append("pageLoadComplete=true; top.window.location='" + (String) localObject1 + "'");
				localStringBuffer.append("</SCRIPT>\n");
				localStringBuffer.append(blanks);
				localJspWriter.print(localStringBuffer.toString());
				localJspWriter.flush();
			} catch (IOException localIOException1) {
				localIOException1.printStackTrace();
			}
			
			try {
				String path = ExportPdfUtil.download(paramNmCommandBean);
				File file = new File(path);
				URL url = NmObjectHelper.constructOutputURL(file , DEFAULT_ARCHIVE_FILE_NAME);
				str = url.toString();
			} catch (WTException localWTException) {
				localObject1 = new NmException(localWTException);
				if (localNmFeedbackSpec != null)
					localNmFeedbackSpec.setException((Exception) localObject1);
				throw (WTException) localObject1;
			}
		} finally {
			try {
				localStringBuffer = new StringBuffer();
				localStringBuffer.append("<SCRIPT LANGUAGE=\"JavaScript1.2\">\n");
				if (str != null) {
					if (localNmFeedbackSpec != null)
						localNmFeedbackSpec.addMessage("@@@@");
					paramNmCommandBean.getSessionBean().getStorage().put("urlToDownload" , str);
				}
				
				localStringBuffer.append("if (ie4) document.all.item(\"msg\").innerHTML=\"\";\n");
				
				localStringBuffer.append("document.close();\n");
				localStringBuffer.append("</SCRIPT>\n");
				localStringBuffer.append(blanks);
				
				localJspWriter.print(localStringBuffer.toString());
				localJspWriter.flush();
			} catch (IOException localIOException3) {
				localIOException3.printStackTrace();
			}
			
			if (localJSPFeedback != null)
				localJSPFeedback.destroy();
		}
	}
	
}
