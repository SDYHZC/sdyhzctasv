package ext.hbt.docnumber;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Locale;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;

import org.apache.log4j.Logger;

import wt.fc.*;
import wt.log4j.LogR;
import wt.epm.*;
import wt.part.*;
import wt.services.*;
import wt.util.*;
import wt.session.*;
import wt.type.ClientTypedUtility;
import wt.org.*;
import wt.events.KeyedEvent;
import wt.vc.*;
import wt.query.*;
import wt.epm.build.*;


public class StandardDocNumberService extends StandardManager implements
		DocNumberService, Serializable {
	
	public static boolean VERBOSE;
	public static WTProperties wtproperties;
	private static final String CLASSNAME = StandardDocNumberService.class
			.getName();
	private ServiceEventListenerAdapter listener;
	private static String logs_dir = "";
	private static final Logger LOGGER = LogR
			.getLogger(StandardDocNumberService.class.getName());

	// 文档项目号
	public static String projectnumber = "projectnumber";

	static {
		wtproperties = null;
		try {
			wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.generic.part.verbose",
					false);
			logs_dir = wtproperties.getProperty("wt.logs.dir", "");
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

	public StandardDocNumberService() {
	}

	public String getConceptualClassname ( ) {
		return CLASSNAME;
	}

	public static StandardDocNumberService newStandardDocNumberService()
			throws WTException {
		StandardDocNumberService standardmpereviseservice = new StandardDocNumberService();
		standardmpereviseservice.initialize();
		return standardmpereviseservice;
	}

	protected void performStartupProcess() throws ManagerException {
		System.out.println("Starting Generic 业务联系函文档编码  service..");
		
		listener = new DocNumberServiceEventListener(getConceptualClassname());
		
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey("POST_STORE"));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey("PRE_STORE"));
		getManagerService().addEventListener(listener,
				wt.vc.wip.WorkInProgressServiceEvent.PRE_CHECKOUT);

		// getManagerService().addEventListener(listener,
		// PersistenceManagerEvent.generateEventKey("POST_CHECKIN"));
		// getManagerService().addEventListener(listener,
		// PersistenceManagerEvent.generateEventKey("PRE_CHECKIN"));

	}

	class DocNumberServiceEventListener extends ServiceEventListenerAdapter {
		public void notifyVetoableEvent(Object obj) throws WTException,
				WTPropertyVetoException, IOException, Exception {

			KeyedEvent keyedevent = (KeyedEvent) obj;
			Object wtObject = keyedevent.getEventTarget();
			String eventType = keyedevent.getEventType();

			if (wtObject instanceof WTDocument) { // 监听对象为文档

				if (keyedevent.getEventType().equals("POST_STORE")) {
					WTDocument doc = (WTDocument) wtObject;
					System.out.println("Catch event POST_STORE----doc="
							+ doc.toString());
					processPreStoreEventRule(doc);
				}
				if (keyedevent.getEventType().equals("PRE_CHECKOUT")) {
					WTDocument doc = (WTDocument) wtObject;
					// processPreStoreEventRule(wtdocument);
					System.out.println("Catch event PRE_CHECKOUT----doc="
							+ doc.toString());
				}

			}

			if (keyedevent.getEventType().equals("POST_CHECKIN")) {
				System.out.println("Catch event POST_CHECKIN");
			}
		}

		/** 
		 * 获取文档类型
		 * 
		 * @param doc
		 * @return
		 * @throws IOException
		 * @throws WTException
		 */

		public String getDocType(WTDocument doc) throws IOException,
				WTException {
			String docType = "";
			Locale locale = SessionHelper.manager.getLocale();
			docType = ClientTypedUtility.getLocalizedTypeName(doc, locale);
			return docType;
		}

		/**
		 * 处理文档检入时创建文档编号的问题
		 * 
		 * @param wtdocument
		 * @throws WTException
		 * @throws Exception
		 * @throws java.beans.PropertyVetoException
		 */
		protected synchronized void processPreStoreEventRule(
				WTDocument wtdocument) throws WTException, Exception,
				java.beans.PropertyVetoException {
			
			System.out.println(">>>" + CLASSNAME
					+ ".procesPostStoreEvent()---doc="
					+ wtdocument.getDisplayIdentifier());
			String projectnumber = "";
			String docType = getDocType(wtdocument);
			System.out.println("docType===" + docType);
			boolean isProcess = false;

			Object target = wtdocument;

			// 如果Document不为A版本，则直接返回不处理
			if (target instanceof WTDocument) {
				String version = VersionControlHelper.getVersionIdentifier(
						(Versioned) target).getValue();
				if (version.equals("A"))
					isProcess = true;
			}

			if (isProcess) {
				WTDocument wtdocunment = (WTDocument) target;
				if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(wtdocunment)) {
					System.out
							.println("	>>>>the doc is checked out,ignore it.");
					// wtdocunment =
					// (WTDocument)wt.vc.wip.WorkInProgressHelper.service.originalCopyOf(wtdocunment);
					return;
				}

				projectnumber = wtdocunment.getFolderingInfo().getFolder()
						.getName();
				System.out.println("	projectnumber==" + projectnumber);

				String smalldoctype = (String) IBAUtil.getIBAValue(wtdocunment,
						"SmallDocType");
				if (docType.equals("业务联系函")) {
					// ||smalldoctype.equals("业务联系函-电气")||smalldoctype.equals("业务联系函_机械")||smalldoctype.equals("业务联系函_电气")||smalldoctype.equals("业务联系函")

					System.out.println("	文档小类===" + smalldoctype);

					// System.out.println("Location==="+projectnumber);

					projectnumber = wtdocunment.getFolderingInfo().getFolder()
							.getName();

					// projectnumber=projectnumber.substring(projectnumber.lastIndexOf("/")+1);
					String number = wtdocunment.getNumber();
					LOGGER.info(" wtdocunment number == " + number);
					System.out.println("	projectnumber==" + projectnumber);

					// projectnumber=(String) IBAUtil.getIBAValue(wtdocunment,
					// projectnumber);

					LOGGER.info("	\t projectnumber =" + projectnumber);

					if (number.startsWith(projectnumber.toUpperCase()))

						return;

					// 重新生成编码
					// 获取当前分类的最大码
					String seriaNumber = getSeriaNumber(wtdocunment,
							projectnumber, 3, 0);

					// 如果获取的流水码为空，则直接返回
					if (seriaNumber == null || "".equals(seriaNumber))
						return;

					String newNumber = projectnumber + seriaNumber;

					// 更改文档编码
					Identified master = (Identified) wtdocunment.getMaster();
					WTDocumentMasterIdentity masteridentity = (WTDocumentMasterIdentity) master
							.getIdentificationObject();
					masteridentity.setNumber(newNumber);
					master = IdentityHelper.service.changeIdentity(master,
							masteridentity); // 更新名称
					PersistenceServerHelper.manager.update(wtdocunment
							.getMaster());
					System.out.println("<<<<" + CLASSNAME
							+ ".procesPostStoreEvent()---doc="
							+ wtdocument.getDisplayIdentifier());
				}
			}
		}

		/**
		 * 查询系统中的编号并累加编码
		 * 
		 * @param classificationCode
		 *            分类码
		 * @param iDigit
		 *            流水码位数
		 * @param iMaxNumber
		 *            流水码最大值
		 * @return 最新编码
		 */
		protected String getSeriaNumber(WTDocument wtdocunment,
				String projectnumber, int iDigit, long iMaxNumber) {
			String seriaNumber = "";
			WTUser currentUser = null;
			long iExistMaxNumber = -1;
			try {
				currentUser = (WTUser) SessionHelper.manager.getPrincipal();
				SessionHelper.manager.setAdministrator();

				QuerySpec qs = null;
				QueryResult qr = null;
				qs = new QuerySpec(WTDocumentMaster.class);
				SearchCondition sc = new SearchCondition(
						WTDocumentMaster.class, "number", SearchCondition.LIKE,
						projectnumber.toUpperCase() + "%");
				qs.appendWhere(sc);
				OrderBy orderby = new OrderBy(new ClassAttribute(
						WTDocumentMaster.class, "number"), true);
				qs.appendOrderBy(orderby, 0);
				qr = PersistenceHelper.manager.find(qs);
				if (qr != null && qr.size() > 0) {
					WTDocumentMaster latestDoc = (WTDocumentMaster) qr
							.nextElement();
					String number = latestDoc.getNumber();
					LOGGER.info(new StringBuilder(">>>>>>getSeriaNumber")
							.append("--doc=").append(wtdocunment.getIdentity())
							.append("--latestPartNumber=").append(number)
							.toString());

					// 获取除前缀之外的流水码
					String waterNumber = "";
					try {

						if (number.length() >= projectnumber.length() + iDigit) {
							waterNumber = number.substring(
									projectnumber.length(),
									projectnumber.length() + iDigit);
						} else {
							waterNumber = number.substring(
									projectnumber.length(), number.length());
						}
					} catch (StringIndexOutOfBoundsException e) {
						Object params[] = { wtdocunment.getIdentity(), iDigit,
								number };
						LOGGER.warn(new StringBuilder(">>>>>>getSeriaNumber")
								.append("--wtdocunment=")
								.append(wtdocunment.getIdentity())
								.append("--seria-number-digit=").append(iDigit)
								.append("--latestPartNumber=").append(number)
								.toString());
						e.printStackTrace();
						return "";
					}
					try {
						iExistMaxNumber = Long.valueOf(waterNumber).longValue();
					} catch (NumberFormatException e) {
						Object params[] = { wtdocunment.getIdentity(), number,
								waterNumber };
						LOGGER.warn(new StringBuilder(">>>>>>getSeriaNumber")
								.append("--wtdocunment=")
								.append(wtdocunment.getIdentity())
								.append("--error=").append("").toString());
						e.printStackTrace();
						return "";
					}
				}

				// 取系统中现有最大只跟定义最大值的大值
				if (iExistMaxNumber > iMaxNumber)
					iMaxNumber = iExistMaxNumber;

				// 取最大值加1
				iMaxNumber += 1;

				LOGGER.info(new StringBuilder(">>>>>>getSeriaNumber")
						.append("--part=").append(wtdocunment.getIdentity())
						.append("--iMaxNumber=").append(iMaxNumber).toString());

				// 设置流水码部分的位数
				String forrmat = "%0" + iDigit + "d";
				seriaNumber = String.format(forrmat,
						new Object[] { Long.valueOf(iMaxNumber) });
			} catch (WTException e) {
				// errorMessage.append(WTMessage.getLocalizedMessage(RESOURCE,
				// "1",null, LOCALE));
				e.printStackTrace();
			} finally {
				if (currentUser != null) {
					try {
						SessionHelper.manager.setPrincipal(currentUser
								.getName());
					} catch (WTException e) {
						e.printStackTrace();
					}
				}
			}
			LOGGER.info(new StringBuilder(">>>>>>getSeriaNumber")
					.append("--wtdocunment=").append(wtdocunment.getIdentity())
					.append("--seriaNumber=").append(seriaNumber).toString());
			return seriaNumber;
		}

		public DocNumberServiceEventListener(String s) {
			super(s);
		}
	}
}