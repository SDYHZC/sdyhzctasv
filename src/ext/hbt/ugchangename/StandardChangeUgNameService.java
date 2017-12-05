package ext.hbt.ugchangename;

import java.beans.PropertyVetoException;
import java.io.*;
import org.apache.log4j.Logger;
import wt.fc.*;
import wt.log4j.LogR;
import wt.epm.*;
import wt.services.*;
import wt.util.*;
import wt.events.KeyedEvent;
import wt.vc.*;


@SuppressWarnings("serial")
public class StandardChangeUgNameService extends StandardManager implements
		ChangeUgNameService, Serializable {
	public static boolean VERBOSE;
	public static WTProperties wtproperties;
	private static final String CLASSNAME = StandardChangeUgNameService.class
			.getName();
	private ServiceEventListenerAdapter listener;
	private static final Logger LOGGER = LogR
			.getLogger(StandardChangeUgNameService.class.getName());

	static {
		wtproperties = null;
		try {
			wtproperties = WTProperties.getLocalProperties();
			wtproperties.getProperty("wt.logs.dir", "");
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

	public StandardChangeUgNameService() {
	}

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	public static StandardChangeUgNameService newStandardChangeUgNameService()
			throws WTException {
		StandardChangeUgNameService standardmpereviseservice = new StandardChangeUgNameService();
		standardmpereviseservice.initialize();
		return standardmpereviseservice;
	}

	protected void performStartupProcess() throws ManagerException {
		System.out.println("Starting Generic UG图纸重命名  service..");
		listener = new ChangeUgNameServiceEventListener(
				getConceptualClassname());
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey("POST_STORE"));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey("PRE_STORE"));
	}

	class ChangeUgNameServiceEventListener extends ServiceEventListenerAdapter {
		public void notifyVetoableEvent(Object obj) throws WTException,
				WTPropertyVetoException, IOException, Exception {

			KeyedEvent keyedevent = (KeyedEvent) obj;
			Object wtObject = keyedevent.getEventTarget();
			String eventType = keyedevent.getEventType();

			if (wtObject instanceof EPMDocument) {
				System.out.println("开始监听");
				System.out.println("事件为" + keyedevent.getEventType());
				LOGGER.info(" keyedevent is "+eventType);
				// 监听对象为CAD文档
				EPMDocument epm = (EPMDocument) wtObject;
				String version = VersionControlHelper.getVersionIdentifier(
						(Versioned) epm).getValue();
				if (version.equals("A")) {
					if (eventType.equals(PersistenceManagerEvent.POST_STORE)
							|| eventType
									.equals(PersistenceManagerEvent.PRE_STORE)) {						
						String epmapp = epm.getAuthoringApplication()
								.toString();
						LOGGER.info(" epmapp is "+epmapp);
						if (epmapp.equals("UG")) {
							String number = epm.getNumber();
							String name = epm.getName();
							String newName = (number + "#" + name).toString();
							changeCADName(epm, newName);
						}

					}
				}
			}
		}

		/**
		 * 修改CAD文档名称
		 * 
		 * @param epm 需要修改名称的EPMDocument
		 * @return
		 * @throws PropertyVetoException
		 */
		protected void changeCADName(EPMDocument epm, String newName)
				throws PropertyVetoException {

			if (epm == null)
				return;
			EPMDocumentMaster identified = (EPMDocumentMaster) epm.getMaster();
			LOGGER.info(" ----start renaming the CAD Document="
					+ identified.getDisplayIdentity());
			try {
				EPMDocumentMasterIdentity identity = (EPMDocumentMasterIdentity) identified
						.getIdentificationObject();
				identity.setName(newName);
				IdentityHelper.service.changeIdentity(identified, identity);
				identified = (EPMDocumentMaster) PersistenceHelper.manager
						.refresh(identified);
			} catch (WTPropertyVetoException e) {
				System.out.println("changeCADNumber WTPropertyVetoException");
				e.printStackTrace();
			} catch (ObjectNoLongerExistsException e) {
				System.out
						.println("changeCADNumber ObjectNoLongerExistsException");
				e.printStackTrace();
			} catch (WTException e) {
				System.out.println("changeCADNumber WTException");
				e.printStackTrace();
			}

			LOGGER.info(" CAD document number is updated.");
			LOGGER.info(" -----finish to rename the CAD Document="
					+ identified.getDisplayIdentity());
		}

		public ChangeUgNameServiceEventListener(String s) {
			super(s);
		}
	}
}