
package ext.tasv.pdfexport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.identity.IdentityFactory;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.config.ConfigHelper;
import wt.vc.config.ConfigSpec;

import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.tasv.pdfexport.resource.ExportPDFActionsRB;

/**
 * 构造下载路径的Util文件
 * 
 * @author Administrator
 * 
 */
public class ExportPdfUtil implements RemoteAccess , Serializable {

	private static String RESOURCE = "";

	private static Locale LOCALE = null;

	private static String CLASSNAME;

	private static String WT_TEMP;

	private static Logger logger;

	private static String TXTNAME = "";

	static {
		try {
			TXTNAME = "未下载信息.txt";
			RESOURCE = ExportPDFActionsRB.class.getName();
			LOCALE = SessionHelper.manager.getLocale();
			WTProperties wtProperties = WTProperties.getLocalProperties();
			WT_TEMP = wtProperties.getProperty("wt.temp" , "");
			CLASSNAME = ExportPdfUtil.class.getName();
			logger = LogR.getLogger(CLASSNAME);
		} catch (Throwable throwable) {
			throwable.printStackTrace(System.err);
		}
	}

	/**
	 * 构造函数
	 */
	public ExportPdfUtil () {

	}

	/**
	 * 输出调试信息
	 * 
	 * @param info
	 *            需要输出的字符串
	 */
	private static void outDebugInfo ( String info ) {

		logger.debug(CLASSNAME + "$$$$$" + info);
	}

	/**
	 * 打包PDF文件
	 * 
	 * @param cb
	 *            NmCommandBean对象
	 * @return 打包的PDF文件本地路径
	 * @throws Throwable
	 *             Throwable
	 */
	public static String download ( NmCommandBean cb ) throws Throwable {

		String zipFile = "";
		boolean flag = SessionServerHelper.manager.isAccessEnforced();
		try {
			SessionServerHelper.manager.setAccessEnforced(false);

			zipFile = getZipFile();
			outDebugInfo("zipFile=" + zipFile);
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

			out.setEncoding("GBK"); // 在linux下必须设置编码格式
			try {

				doMethod(cb , out);

			} catch (Exception e) {
				throw new WTException(e.getLocalizedMessage());
			} finally {
				out.flush();
				out.close();
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return zipFile;
	}

	/**
	 * 操作方法
	 * 
	 * @param cb
	 *            NmCommandBean对象
	 * @param out
	 *            ZipOutputStream对象
	 * @throws WTException
	 *             WTException
	 * @throws WTPropertyVetoException
	 *             WTPropertyVetoException
	 * @throws IOException
	 *             IOException
	 */
	private static void doMethod ( NmCommandBean cb , ZipOutputStream out ) throws WTException , WTPropertyVetoException , IOException {

		Object object = cb.getActionOid().getRefObject();
		if (object instanceof WTChangeOrder2) {
			WTChangeOrder2 wtchange=(WTChangeOrder2)object;	
			completeBuild(out , wtchange);
		}
	}

	/**
	 * 完成写入所有的Zip文件
	 * 
	 * @param out
	 *            ZipOutputStream对象
	 * @param parts
	 *            需要下载的集合
	 * @throws WTException
	 *             WTException
	 * @throws WTPropertyVetoException
	 *             WTPropertyVetoException
	 * @throws IOException
	 *             IOException
	 */
	private static void completeBuild ( ZipOutputStream out , WTChangeOrder2 wtchange ) throws WTException , WTPropertyVetoException , IOException {

		ReadXMLUtil readXMLUtil = new ReadXMLUtil(ExportConstant.PATH);
		Vector <String> isPrintList = readXMLUtil.getBusinessRuleFromXML(ExportConstant.PDF_PERFIX);
		//Vector <String> associateList = readXMLUtil.getBusinessRuleFromXML(ExportConstant.PART_ASSOCIATE);

		boolean isPrint = false;
		if (isPrintList.size() == 1) {
			isPrint = Boolean.parseBoolean(isPrintList.get(0));
		}
		DoOperation doOper = new DoOperation(isPrint);
		
			QueryResult qr = ChangeHelper2.service.getChangeablesAfter(wtchange);
			outDebugInfo("qr.size=" + qr.size());

			while (qr.hasMoreElements()) {
				WTObject wtobject = (WTObject) qr.nextElement();
				doOper.buildZipSource(out , wtobject);
			}
			outDebugInfo("doOper.pdfList.size=" + doOper.pdfList.size());

		
		writeZip(out , doOper);
	}

	/**
	 * 获取Zip文件的存放路径
	 * 
	 * @return Zip文件路径
	 * @throws WTException
	 *             WTException
	 */
	private static String getZipFile () throws WTException {

		String zipFile = "";
		String filepath = WT_TEMP + File.separator + "temp_pdf";
		File path = new File(filepath);
		if ( ! path.exists()) {
			path.mkdir();
		}
		zipFile = filepath + File.separator + System.currentTimeMillis() + "_" + SessionHelper.getPrincipal().getName() + ".zip";
		return zipFile;
	}

	/**
	 * 写入zip
	 * 
	 * @param out
	 *            ZipOutputStream对象
	 * @param doOper
	 *            DoOperation 对象
	 * @throws WTException
	 *             WTException
	 * @throws IOException
	 *             IOException
	 */
	private static void writeZip ( ZipOutputStream out , DoOperation doOper ) throws WTException , IOException {

		if (doOper.pdfList.size() != 0) {
			doOper.writeWrongTxt(out , doOper.buffer.toString());
			for (ApplicationData sourceFile : doOper.pdfList) {
				doOper.writeToZipEntry(out , sourceFile);

			}
		} else {
			throw new WTException(WTMessage.getLocalizedMessage(RESOURCE , "PRIVATE_CONSTANT_6" , null , LOCALE));
		}

	}

	/**
	 * 一个根据文件下载相关文件的静态内部类
	 * 
	 * @author Administrator
	 * 
	 */
	static class DoOperation {

		private static final String PDF = "PDF";

		private static final String $CAD_NAME = "{$CAD_NAME}";

		private boolean isPrint = false;

		private List <ApplicationData> pdfList = new ArrayList <ApplicationData>();

		private StringBuffer buffer = new StringBuffer();

		/**
		 * @param isPrint
		 */
		public DoOperation ( boolean isPrint ) {

			this.isPrint = isPrint;
		}

		public void writeWrongTxt ( ZipOutputStream out , String string ) throws IOException {

			if ( ! StringUtils.isEmpty(string)) {
				try {
					out.putNextEntry(new ZipEntry(TXTNAME));
					out.write(string.getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					out.closeEntry();
				}
			}
		}

		/**
		 * 构造Zip文件
		 * 
		 * @param out
		 *            ZipOutputStream 对象
		 * @param wtobject
		 *            需要筛选的WTObject对象
		 * @param part 父部件
		 * @throws WTException
		 *             WTException
		 * @throws WTPropertyVetoException
		 *             WTPropertyVetoException
		 * @throws IOException
		 *             IOException
		 */
		private void buildZipSource ( ZipOutputStream out , WTObject wtobject) throws WTException , WTPropertyVetoException , IOException {

			if (wtobject != null) {

				EPMDocument childEPM = null;
				if (wtobject instanceof EPMDocument) {
					childEPM = (EPMDocument) wtobject;
				}
				if (wtobject instanceof ContentHolder) {

					QueryResult qr = getSourceVector(wtobject);
					getDownloadType(qr , childEPM , wtobject);

				}
			}

		}

		/**
		 * 设置EPM文件的显示文件名称
		 * 
		 * @param childEPM
		 *            EPMDocument对象
		 * @param sourceFile
		 *            文件存储的对象
		 * @param fileName
		 * @throws WTPropertyVetoException
		 *             WTPropertyVetoException
		 */
		private String setSourceName ( EPMDocument childEPM , ApplicationData sourceFile , String fileName ) throws WTPropertyVetoException {

			if (childEPM != null) {
				fileName = childEPM.getCADName();
				sourceFile.setFileName(fileName);
			}
			return fileName;
		}

		/**
		 * 往Zip流对象中写入
		 * 
		 * @param out
		 *            ZipOutputStream对象
		 * @param sourceFile
		 *            写入的ApplicationData对象
		 * @throws WTException
		 *             WTException
		 * @throws IOException
		 *             IOException
		 */
		private void writeToZipEntry ( ZipOutputStream out , ApplicationData sourceFile ) throws WTException , IOException {

			InputStream is = ContentServerHelper.service.findContentStream(sourceFile);
			byte [] buffer = new byte [1024];
			try {
				out.putNextEntry(new ZipEntry(sourceFile.getFileName()));
				int len;
				while ((len = is.read(buffer)) > 0) {
					out.write(buffer , 0 , len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				out.closeEntry();
				is.close();
			}
		}

		/**
		 * 获取相关附件中是否有签名的PDF文件，如果有加入到pdfList中
		 * 
		 * @param qr
		 *            相关内容集合
		 * @param childEPM
		 *            EPMDocument对象
		 * @param wtobject 父件
		 * @param part 父部件
		 */
		private void getDownloadType ( QueryResult qr , EPMDocument childEPM , WTObject wtobject ) {

			while ((qr != null) && qr.hasMoreElements()) {

				Object obj = qr.nextElement();
				if (obj instanceof ApplicationData) {

					ApplicationData app = (ApplicationData) obj;

					String fileName = app.getFileName();
					if (fileName.equals($CAD_NAME)) {
						try {
							fileName = setSourceName(childEPM , app , fileName);
						} catch (WTPropertyVetoException e) {
							e.printStackTrace();
						}
					}
					String fileType = fileName.substring(fileName.lastIndexOf(".") + 1 , fileName.length());

					outDebugInfo("fileType=" + fileType + "&&&&&&& fileName= " + fileName);

					
					if (fileType.equalsIgnoreCase(PDF)) 
					{
						pdfList.add(app);
						 }
		}
			}
}

		/**
		 * 获取可扫描的附件
		 * 
		 * @param wtobject
		 *            待扫描的对象
		 * @return QueryResult
		 * @throws WTException
		 *             WTException
		 */
		private QueryResult getSourceVector ( WTObject wtobject ) throws WTException {

			ContentHolder holder = (ContentHolder) wtobject;
			QueryResult qr = getSecondaryContents(holder);

			return qr;
		}
		/**
		 * 获取对象的附件;
		 * @author 
		 * @param holder 含有附件内容的对象,例如:文档,CAD图档、变更请求,变更通告等变更对象
		 * @return
		 * @throws WTException
		 */
		public static QueryResult getSecondaryContents(ContentHolder holder) throws WTException {
			try {
				//初始化:现将文件内容列表加载到内存中
				holder = ContentHelper.service.getContents(holder);
				//然后获取附件
				QueryResult qr = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
				return qr;
			} catch (Exception exception) {
				throw new WTException(exception);
			}
		}
	}
}
