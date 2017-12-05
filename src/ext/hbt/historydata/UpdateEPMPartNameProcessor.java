package ext.hbt.historydata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.build.EPMBuildRule;
import wt.epm.retriever.ResultGraph;
import wt.fc.BinaryLink;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTArrayList;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import wt.log4j.LogR;
import wt.method.RemoteMethodServer;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.config.LatestConfigSpec;
import wt.part.WTPart;
import wt.part.WTPartMasterIdentity;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import com.ptc.core.components.beans.ObjectBean;
import com.ptc.core.components.forms.DefaultObjectFormProcessor;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.uwgm.common.autoassociate.AutoAssociateHelper;

/**
 * 更新EPM文档的名称和部件的名称，规则为“编号#名称”
 * @author yaoy
 */

public class UpdateEPMPartNameProcessor extends DefaultObjectFormProcessor {

	private static String CLASSNAME = UpdateEPMPartNameProcessor.class
			.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);

    @Override
	public FormResult doOperation(NmCommandBean nmcommandbean,
			List<ObjectBean> paramList) throws WTException {
		// 绕过权限
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		FormResult formresult = new FormResult();
		try {
			SessionServerHelper.manager.setAccessEnforced(false);
			// WTPart part = null;
			java.util.Locale locale = SessionHelper.getLocale();
			// 获取excel文件
			File file;
			file = getImportFile(nmcommandbean);
			if (file == null) {
				FeedbackMessage feedbackmessage = new FeedbackMessage(
						FeedbackType.FAILURE, locale, "未获取到数据导入文档！", null,
						new String[] { "" });
				formresult.addFeedbackMessage(feedbackmessage);
				formresult.setStatus(FormProcessingStatus.FAILURE);
				return formresult;
			} else {
				FileInputStream fs = new FileInputStream(file);
				Workbook wb = null;
				wb = new HSSFWorkbook(fs);
				if (!RemoteMethodServer.ServerFlag) {

					String method = "startDataImport";
					try {

						RemoteMethodServer.getDefault().invoke(method,
								DataImport.class.getName(), null,
								new Class[] { Workbook.class },
								new Object[] { wb });
					} catch (Exception exp) {
						exp.printStackTrace();
					}
				} else {
					// 获取数据并更新CAD文档和相关部件的名称
					startDataImport(wb);
				}
			}
			FeedbackMessage feedbackmessage = new FeedbackMessage(
					FeedbackType.SUCCESS, locale, "CAD图档导入完成！", null,
					new String[] { "" });
			formresult.addFeedbackMessage(feedbackmessage);
			formresult.setStatus(FormProcessingStatus.SUCCESS);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(bool);
		}
		return formresult;
	}

	/**
	 * 开始读取数据
	 * 
	 * @param wb
	 * excel表格
	 * @param
	 * @throws Exception
	 */

	public void startDataImport(Workbook wb) throws Exception {

		List list = DataImport.startDataImport(wb);// 获取excel表格里面的数据，存放在list中
		loger.debug("list = " + list.size());
		System.out.println("list = " + list.size());
		java.util.Locale locale = SessionHelper.getLocale();
		FormResult formresult = new FormResult();
		EPMDocument epmdoc = null;
		int successCount = 0;

		for (int i = 0; i < list.size(); i++) {
			Object obj = list.get(i);
			if (obj instanceof ExcelData) {
				// 获取属性
				String number = ((ExcelData) obj).getEpmnumber();// 编号
				String number1=number;
				epmdoc = getEPMByNumber(number);
				if (epmdoc != null) {

					if (number.contains(".ASM")) {
						number = number.replaceAll(".ASM", "");
					}
					if (number.contains(".PRT")) {
						number = number.replaceAll(".PRT", "");
					}
					if (number.contains(".asm")) {
						number = number.replaceAll(".asm", "");
					}
					if (number.contains(".prt")) {
						number = number.replaceAll(".prt", "");
					}
					if (number.contains(".drw")) {
						number = number.replaceAll(".drw", "");
					}
					if (number.contains(".DRW")) {
						number = number.replaceAll(".DRW", "");
					}
					String newName = number + "#" + epmdoc.getName();
					loger.debug("编号为" + number + "的CAD图档已经查找到，开始更新。。。");
					// System.out.println("编号为" + number +
					// "的CAD图档已经存在！开始更新。。。");
					
					updateEpm(epmdoc, newName);
					WTPart part = getRelatedPart(epmdoc);
					if (part != null) {

						String partname = part.getName();
						if (partname.contains("#") == false) {
							newName = number + "#" + partname;
							updateWtpart(part, newName);
						}
					}
					successCount = successCount + 1;
					System.out.println("--->>第" + successCount + "个数据:"
							+ number1 + "的名称已经更新成功，请检查");
				}
			}
		}
	}

	/**
	 * 更新CAD文档名称
	 * 
	 * @throws Exception
	 */

	public static EPMDocument updateEpm(EPMDocument epmdoc, String newName)
			throws Exception {

		// 更新EPMDocument的名称

		loger.debug("编号为" + epmdoc.getNumber() + "的CAD图档已经更新结束!");

		EPMDocumentMaster identified = (EPMDocumentMaster) epmdoc.getMaster();

		try {
			EPMDocumentMasterIdentity identity = (EPMDocumentMasterIdentity) identified
					.getIdentificationObject();
			identity.setName(newName);
			IdentityHelper.service.changeIdentity(identified, identity);
			identified = (EPMDocumentMaster) PersistenceHelper.manager
					.refresh(identified);
			epmdoc = (EPMDocument) PersistenceHelper.manager.refresh(epmdoc);
		} catch (WTPropertyVetoException e) {
			System.out.println("changeCADNumber WTPropertyVetoException");
			e.printStackTrace();
		} catch (ObjectNoLongerExistsException e) {
			System.out.println("changeCADNumber ObjectNoLongerExistsException");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("changeCADNumber WTException");
			e.printStackTrace();
		}

		return epmdoc;

	}

	/**
	 * 更新部件名称
	 * 
	 * @throws Exception
	 */

	public static WTPart updateWtpart(WTPart part, String newName)
			throws Exception {
		Identified master = (Identified) part.getMaster();
		WTPartMasterIdentity masteridentity = (WTPartMasterIdentity) master
				.getIdentificationObject();
		if (newName.trim().length() > 0) {
			masteridentity.setName(newName);
		}
		master = IdentityHelper.service.changeIdentity(master, masteridentity); // 更新名称
		PersistenceServerHelper.manager.update(part.getMaster());
		return part;

	}

	/**
	 * 获取上载的导入文件
	 * 
	 * @param nmcommandbean
	 *            获取当前文件
	 * @return file 返回上载的文件
	 * @throws WTException
	 *             异常
	 */

	protected File getImportFile(NmCommandBean nmcommandbean)
			throws WTException {
		File file = null;
		if ((HashMap) nmcommandbean.getMap().get("fileUploadMap") != null) {
			loger.debug("====获取到文件 ");
			file = (File) ((HashMap) nmcommandbean.getMap()
					.get("fileUploadMap")).get("bomFile");
		}
		return file;
	}

	/**
	 * 根据CAD文档获取零部件
	 * 
	 * @param nmcommandbean
	 * @return file
	 * @throws WTException
	 *             异常
	 */

	public static WTPart getRelatedPart(EPMDocument epmdoc) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			String method = "getActivelyAssociatedPart";
			try {
				return (WTPart) RemoteMethodServer.getDefault().invoke(method,
						CLASSNAME, null, new Class[] { EPMDocument.class },
						new Object[] { epmdoc });
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
		WTPart part = null;
		try {
			WTArrayList wtarraylist = new WTArrayList();
			wtarraylist.add(epmdoc);
			wt.epm.retriever.ResultGraph resultgraph = AutoAssociateHelper
					.getAssociatedResultGraph(wtarraylist, true);
			part = getAssociatedPart(epmdoc, resultgraph);
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
		}
		return part;
	}

	/**
	 * 反编译查询AutoAssociateHelper中的该方法
	 * 
	 * @param paramEPMDocument
	 * @param paramResultGraph
	 * @return
	 * @throws WTException
	 */
	public static WTPart getAssociatedPart(EPMDocument paramEPMDocument,
			ResultGraph paramResultGraph) throws WTException {
		if ((paramResultGraph == null) || (paramEPMDocument == null)) {
			return null;
		}
		ResultGraph.Node localNode = paramResultGraph.getNode(paramEPMDocument);
		for (ResultGraph.Link localLink : localNode.getAdjacentLinks()) {
			BinaryLink localBinaryLink = localLink.getLinkObject();
			if ((localBinaryLink instanceof EPMBuildRule)) {
				WTPart localWTPart = (WTPart) localBinaryLink
						.getOtherObject(paramEPMDocument);
				return localWTPart;
			}
		}

		return null;
	}

	/**
	 * 根据图档编号查询最新版本的EPM
	 * 
	 * @param number
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEPMByNumber(String number) throws WTException {
		EPMDocument epm = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		qs.appendWhere(new SearchCondition(EPMDocument.class,
				EPMDocument.NUMBER, "=", number, false));
		QueryResult qr = PersistenceHelper.manager.find(qs);
		LatestConfigSpec ls = new LatestConfigSpec();
		qr = ls.process(qr);
		if (qr.hasMoreElements()) {
			epm = (EPMDocument) qr.nextElement();
		}
		return epm;
	}
}