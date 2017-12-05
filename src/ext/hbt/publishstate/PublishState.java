package ext.hbt.publishstate;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildHistory;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMReferenceType;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.series.MultilevelSeries;
import wt.series.Series;
import wt.session.SessionServerHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.IterationIdentifier;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;

public class PublishState {

	private static final Logger logger = LogR.getLogger(PublishState.class
			.getName());


/**判断选择的发布状态是否唯一
 * 
 * @param publishtoS
 * @param publishtoC
 * @param publishtoD
 * @throws WTException
 * @throws IOException
 */
		
	public static void StateValidate(boolean publishtoC, boolean publishtoS
			) throws WTException, IOException {
		
		System.out.println("发布状态C、S分别为" + publishtoC + publishtoS
				);
		// 判断选择的流程发布状态是否唯一
		if ((publishtoS && publishtoC)) {
			throw new WTException("只允许选择一个有效发布状态！");
		} else if (!publishtoS && !publishtoC) {
			throw new WTException("请选择一个有效的发布状态！");
		}
	}
	
/**判断产生的对象是否能发布到指定状态
 * @param pbo
 * @param self
 * @param publishtoS
 * @param publishtoC
 * @throws ChangeException2
 * @throws WTException
 */
	public static void JudgeReviewObjState(WTObject pbo, ObjectReference self,
			boolean publishtoS, boolean publishtoC)
			throws ChangeException2, WTException {

		ArrayList arraylist = new ArrayList();
		// 如果是ECA则获取ECA的产生对象
		if (pbo instanceof WTChangeActivity2) {
			WTChangeActivity2 eca = (WTChangeActivity2) pbo;
			QueryResult qr = ChangeHelper2.service.getChangeablesAfter(eca);
			while (qr.hasMoreElements()) {
				WTObject obj = (WTObject) qr.nextElement();
				arraylist.add(obj);
			}
		}

		// 如果是ECN则获取ECN所有产生的对象
		else if (pbo instanceof WTChangeOrder2) {
			WTChangeOrder2 ecn = (WTChangeOrder2) pbo;
			QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn);
			while (qr.hasMoreElements()) {
				WTObject obj = (WTObject) qr.nextElement();
				arraylist.add(obj);
			}
		}

		for (int i = 0; i < arraylist.size(); i++) {
			WTObject obj = (WTObject) arraylist.get(i);
			if (obj instanceof EPMDocument) {
				EPMDocument epmdoc = (EPMDocument) obj;
				String state = epmdoc.getState().toString();
				System.out.println("CAD文档的状态为" + state);
				if (publishtoS) {
					if (state.equals("INWORK_D")) {
						throw new WTException("ECN产生的对象中的CAD文档"
								+ epmdoc.getName()
								+ "不能从“正在工作_D”状态发布到“正在工作_S”状态");
					}
				} else if (publishtoC) {
					if (state.equals("INWORK_S")) {
						throw new WTException("ECN产生的对象中的CAD文档"
								+ epmdoc.getName()
								+ "不能从“正在工作_S”状态发布到“正在工作_C”状态");
					}
				} else if (publishtoS) {
					if (state.equals("INWORK_D")) {
						throw new WTException("ECN产生的对象中的CAD文档"
								+ epmdoc.getName()
								+ "不能从“正在工作_D”状态发布到“正在工作_S”状态");
					}
				}
			} else if (obj instanceof WTPart) {
				WTPart part = (WTPart) obj;
				String pstate = part.getState().toString();
				System.out.println("部件的状态为" + pstate);
				if (publishtoS) {
					if (pstate.equals("INWORK_D")) {
						throw new WTException("ECN产生的对象中的部件" + part.getName()
								+ "不能从“正在工作_D”状态发布到“正在工作_S”状态");
					}
				} else if (publishtoC) {
					if (pstate.equals("INWORK_S")) {
						throw new WTException("ECN产生的对象中的部件" + part.getName()
								+ "不能从“正在工作_S”状态发布到“正在工作_C”状态");
					}
				} else if (publishtoS) {
					if (pstate.equals("INWORK_D")) {
						throw new WTException("ECN产生的对象中的部件" + part.getName()
								+ "不能从“正在工作_D”状态发布到“正在工作_S”状态");
					}
				}
			}
		}
	}
	
/** 设置签审对象的发布状态
 * @param pbo
 * @param publishtoS
 * @param publishtoC
 * @throws MaturityException
 * @throws WTException
 */
	public static void setReviewObjState(WTObject pbo, boolean publishtoS,
			boolean publishtoC) throws MaturityException,
			WTException {
		System.out.println("publishtoS===publishtoC" + publishtoS+publishtoC);
	
		
		if ((pbo != null) && ((pbo instanceof WTChangeOrder2))) {
			WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
			QueryResult qrActivities = ChangeHelper2.service
					.getChangeActivities(wtc);
			while (qrActivities.hasMoreElements()) {
				Object objActivities = qrActivities.nextElement();

				if ((objActivities instanceof WTChangeActivity2)) {
					if(((WTChangeActivity2) objActivities).getName().contains(wtc.getNumber())) continue;
					QueryResult qrAfter = ChangeHelper2.service
							.getChangeablesAfter((WTChangeActivity2) objActivities);

					while (qrAfter.hasMoreElements()) {
						WTObject objAfter = (WTObject) qrAfter.nextElement();

						if ((objAfter instanceof WTDocument)) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("APPROVED"));
						} else {
						 String version =((RevisionControlled)objAfter).getVersionIdentifier ().getValue();                 
							if (publishtoS) {
								if (version.startsWith("C")) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								} else if(version.startsWith("S")){
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								}else if (version.startsWith("D")) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}
							}
							
							if (publishtoC) {
								if(version.startsWith("C")){
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_C"));
								}else if(version.startsWith("S")){
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								}else if (version.startsWith("D")) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}
							}																		 
							System.out.println("第二次发布状态S、C分别为" + publishtoS + publishtoC
									);
						}
					}
				}
			}
		}else if(pbo instanceof WTChangeActivity2){
			WTChangeActivity2 eca = (WTChangeActivity2) pbo;
			QueryResult qrAfter = ChangeHelper2.service
					.getChangeablesAfter((WTChangeActivity2) eca);

			while (qrAfter.hasMoreElements()) {
				WTObject objAfter = (WTObject) qrAfter.nextElement();
				if ((objAfter instanceof WTDocument)) {
					LifeCycleHelper.service.setLifeCycleState(
							(LifeCycleManaged) objAfter,
							State.toState("APPROVED"));
				} else {
				 String version =((RevisionControlled)objAfter).getVersionIdentifier ().getValue();                 
					if (publishtoS) {
						if (version.startsWith("C")) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_S"));
						} else if(version.startsWith("S")){
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_S"));
						}else if (version.startsWith("D")) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_D"));
						}
					}
					if (publishtoC) {
						if(version.startsWith("C")){
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_C"));
						}else if(version.startsWith("S")){
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_S"));
						}else if (version.startsWith("D")) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_D"));
						}
					}																		 
					System.out.println("第二次发布状态S、C分别为" + publishtoS + publishtoC);
				}
			}
		}
	}
	
	
	/** 补充设置签审对象中C阶段的发布状态
	 * @param pbo
	 * @param publishtoS
	 * @param publishtoC
	 * @throws MaturityException
	 * @throws WTException
	 */
	
	public static void setReviewObjStateC(WTObject pbo) throws MaturityException,
			WTException {
		WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
		if ((wtc != null) && ((wtc instanceof WTChangeOrder2))) {
			QueryResult qrActivities = ChangeHelper2.service
					.getChangeActivities(wtc);
			while (qrActivities.hasMoreElements()) {
				Object objActivities = qrActivities.nextElement();

				if ((objActivities instanceof WTChangeActivity2)) {
					QueryResult qrAfter = ChangeHelper2.service
							.getChangeablesAfter((WTChangeActivity2) objActivities);

					while (qrAfter.hasMoreElements()) {
						WTObject objAfter = (WTObject) qrAfter.nextElement();
						String version = ((RevisionControlled) objAfter)
								.getVersionIdentifier().getValue();
						System.out.println("========version======="+version);
						char ver = version.charAt(0);
						System.out.println("========ver======="+version);
						if (version.startsWith("C")) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_C"));
						}
					}
				}
			}
		}
	}
	
	/** 设置签审对象的版本修订及发布状态(更改流程中C变为S或C)
	 * @param pbo
	 * @param publishtoS
	 * @param publishtoC
	 * @throws MaturityException
	 * @throws WTException
	 * @throws WTPropertyVetoException 
	 */
		public static void setReviewObjStateChange(WTObject pbo, boolean publishtoS,
				boolean publishtoC) throws MaturityException,
				WTException, WTPropertyVetoException {
			System.out.println("publishtoS===publishtoC" + publishtoS+publishtoC);
		
			if ((pbo != null) && ((pbo instanceof WTChangeOrder2))) {
				WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
				QueryResult qrActivities = ChangeHelper2.service
						.getChangeActivities(wtc);
				while (qrActivities.hasMoreElements()) {
					Object objActivities = qrActivities.nextElement();

					if ((objActivities instanceof WTChangeActivity2)) {
						if(((WTChangeActivity2) objActivities).getName().contains(wtc.getNumber())) continue;
						setReviewObjReversion(publishtoS, (WTChangeActivity2) objActivities);
					}
				}
			}else if(pbo instanceof WTChangeActivity2){
				WTChangeActivity2 eca = (WTChangeActivity2) pbo;
				setReviewObjReversion(publishtoS, (WTChangeActivity2) eca);
			}
		}

	/**
	 * @param publishtoS
	 * @param objActivities
	 * @throws WTException
	 * @throws ChangeException2
	 * @throws LifeCycleException
	 * @throws QueryException
	 * @throws VersionControlException
	 * @throws WTPropertyVetoException
	 */
	public static void setReviewObjReversion(boolean publishtoS,
			WTChangeActivity2 objActivities) throws WTException, ChangeException2,
			LifeCycleException, QueryException, VersionControlException,
			WTPropertyVetoException {
		QueryResult qrAfter = ChangeHelper2.service
				.getChangeablesAfter((WTChangeActivity2) objActivities);

		while (qrAfter.hasMoreElements()) {
			WTObject objAfter = (WTObject) qrAfter.nextElement();

			if ((objAfter instanceof WTDocument)) {
				LifeCycleHelper.service.setLifeCycleState(
						(LifeCycleManaged) objAfter,
						State.toState("APPROVED"));
			} else {
			String version =((RevisionControlled)objAfter).getVersionIdentifier ().getValue();	                        
				if (publishtoS) {
					EPMDocument newEpm=null;
					EPMDocument newEpm1=null;
					EPMDocument tempEpm=null;
					EPMDocument tempEpm1=null;
					EPMDocument tempEpm2=null;
					
					WTPart newPart=null;
					WTPart tempPart=null;
					if (version.startsWith("C")) {
						System.out.println("========version---S======"+version);
						//修订版本
						if(objAfter instanceof WTPart){
							tempPart=(WTPart)objAfter;	
							System.out.println("========tempPart======"+tempPart);
							tempEpm=getBuiltEPMDocByPart(tempPart);
							System.out.println("========tempEpm======"+tempEpm);
							//获取工程图
							if(tempEpm.getCADName().endsWith("asm")
									||tempEpm.getCADName().endsWith("ASM")
									||tempEpm.getCADName().endsWith("prt")
									||tempEpm.getCADName().endsWith("PRT")){
								QuerySpec qs= new QuerySpec(EPMReferenceLink.class);
								
								QueryResult qr1= EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster)tempEpm.getMaster(), qs, true);
								System.out.println("========qr1======"+qr1);
								QueryResult qr2=EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster)tempEpm.getMaster(), qs);
								System.out.println("========qr2======"+qr2);
								
								while(qr1.hasMoreElements())
								{
									//Persistable[] apersistable = (Persistable[]) qr1.nextElement();
									Object obj=(Object)qr1.nextElement();
									System.out.println("========obj======"+obj);
									EPMDocument referencedBy= (EPMDocument)obj;
									String cadName=referencedBy.getCADName();
									System.out.println("========cadName======"+cadName);
									String cadName1=tempEpm.getCADName();
									System.out.println("========cadName1======"+cadName1);
									cadName=cadName.substring(0, cadName.length()-4);
									System.out.println("========cadName2======"+cadName);
									cadName1=cadName1.substring(0, cadName1.length()-4);
									System.out.println("========cadName3======"+cadName);
									if(cadName1.equals(cadName)){
										tempEpm1=referencedBy;
										break;
									}
								}
							}
							
							newPart=(WTPart) VersionControlHelper.service.newVersion((Versioned)tempPart);
																	PersistenceHelper.manager.save(newPart);
							System.out.println("========newPart======"+newPart);
							LifeCycleHelper.service.setLifeCycleState(
									newPart,State.toState("RELEASED_S"));
							
							System.out.println("========newPart Create Successfull======");
							
							if(tempEpm!=null){
								newEpm=(EPMDocument) VersionControlHelper.service.newVersion((Versioned)tempEpm);						
								PersistenceHelper.manager.save(newEpm);
								System.out.println("========newEpm======"+newEpm);
								LifeCycleHelper.service.setLifeCycleState(
										newEpm,State.toState("RELEASED_S"));
								
								System.out.println("========newEpm Create Successfull======");	
								
								createBuildRule(newEpm,newPart);
							}
							if(tempEpm1!=null){
								tempEpm1=(EPMDocument) getLatestPersistableByNumber(tempEpm1.getNumber(), EPMDocument.class);
								String vs = ((Versioned)tempEpm1).getVersionIdentifier().getValue();
							    String iterate = ((Iterated)tempEpm1).getIterationIdentifier().getValue();
							    String banbenhao = vs + "." + iterate;
								System.out.println("========newEpm1======"+tempEpm1.getNumber()+"==banbenhao:"+banbenhao);
								newEpm1=(EPMDocument) VersionControlHelper.service.newVersion((Versioned)tempEpm1);						
								PersistenceHelper.manager.save(newEpm1);
								LifeCycleHelper.service.setLifeCycleState(
										newEpm1,State.toState("RELEASED_S"));
								
								System.out.println("========newEpm1 Create Successfull======");	
								
							}
						}else if(objAfter instanceof EPMDocument){
							EPMDocument tempEpmSkel=(EPMDocument)objAfter;
							System.out.println("=======tempEpmSkel=========="+tempEpmSkel);
							String tempEpmSkelName1=tempEpmSkel.getCADName();
							if(tempEpmSkelName1.contains("SKEL")||
									tempEpmSkelName1.contains("skel")){
								EPMDocument newTempEpmSkel=(EPMDocument) VersionControlHelper.service.newVersion((Versioned)tempEpmSkel);
								PersistenceHelper.manager.save(newTempEpmSkel);
								System.out.println("========newTempEpmSkel======"+newTempEpmSkel);
								LifeCycleHelper.service.setLifeCycleState(
										newTempEpmSkel,State.toState("RELEASED_S"));
								System.out.println("========newTempEpmSkel Create Successfull==");
							}
							
							/*String subEpmType =tempEpmSkel.getDocSubType().toString();
							System.out.println("=======subEpmType=========="+subEpmType);*/
							
						}
					}
				}	
			}
		}
	}	
		
		//EPM文档和部件创建所有者关系
		public static void createBuildRule(EPMDocument epmdoc, WTPart part)
				throws WTException {
			EPMBuildRule link = EPMBuildRule.newEPMBuildRule(epmdoc, part);
			PersistenceServerHelper.manager.insert(link);
			EPMBuildHistory build = EPMBuildHistory.newEPMBuildHistory(epmdoc, part,
					link.getUniqueID());
			PersistenceServerHelper.manager.insert(build);
			System.out.println("	the build rule link created.");
		}
		
	
		/**
		 * 根据零部件，获取其相关联的主cad文件
		 * @param part
		 * 想要获取相关cad文件的零部件
		 **/
		public static EPMDocument getBuiltEPMDocByPart(WTPart part)
				throws WTException {
			QueryResult qr = PersistenceHelper.manager.navigate(part,
					"buildSource", EPMBuildRule.class, true);
			while (qr.hasMoreElements()) {
				Persistable persistable = (Persistable) qr.nextElement();
				if (persistable instanceof EPMDocument) {
					EPMDocument cad = (EPMDocument) persistable;
					return cad;
				}
			}
			// if there is no build rule, query cad doc by build history
			qr = PersistenceHelper.manager.navigate(part, "builtBy",
					EPMBuildHistory.class, true);
			while (qr.hasMoreElements()) {
				Persistable persistable = (Persistable) qr.nextElement();
				if (persistable instanceof EPMDocument) {
					EPMDocument cad = (EPMDocument) persistable;
					return cad;
				}
			}
			return null;
		}
		
		/**
		 * 根据零部件，获取其相关联的所有cad文档，包括相关CAD文档的参考文档
		 * 
		 * @param part
		 *            想要获取相关cad文档的零部件
		 * @return 返回该零部件的关联cad文档集合。如果零部件为空，返回空；如果零部件没有关联cad，返回空
		 **/

		public static WTArrayList getRelatedCADListByPart(WTPart part)
				throws WTException {
			WTArrayList epmlist = new WTArrayList();
			if (part == null) {
				return epmlist;
			}
			// 获取part相关的所有文档，包括CAD文档和普通文档,我们只需要取CAD文档
			QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part,
					true);// PartDocHelper.service.getAssociatedDocuments(part);
			while (qr.hasMoreElements()) {
				Object wto = (Object) qr.nextElement();
				if (wto instanceof EPMDocument) {
					EPMDocument epmdoc = (EPMDocument) wto;
					if (epmdoc != null) {
						EPMDocument cad = getLatestEPMDocByMaster((EPMDocumentMaster) epmdoc
								.getMaster());
						epmlist.add(cad);
						QuerySpec qs= new QuerySpec(EPMReferenceLink.class);
						QueryResult qr1= EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster)cad.getMaster(), qs, true);
						while(qr1.hasMoreElements())
						{
							EPMDocument referencedBy= (EPMDocument)qr1.nextElement();
								epmlist.add(referencedBy);
						}
					}
				}
			}
			return epmlist;
		}
		
		// 根据EPM主数据获取EPM
		public static EPMDocument getLatestEPMDocByMaster(EPMDocumentMaster master)
				throws WTException {
			if (master == null) {
				return null;
			}
			boolean flag = false;
			Iterated iter = null;
			QueryResult queryresult = VersionControlHelper.service
					.allIterationsOf(master);
			while (queryresult.hasMoreElements() && (!flag)) {
				iter = (Iterated) (queryresult.nextElement());
				flag = iter.isLatestIteration();
			}
			return (EPMDocument) iter;
		}
		
		/** 校验更改通知单的可视化是否生成
		 * @param pbo
		 * @param publishtoS
		 * @param publishtoC
		 * @throws MaturityException
		 * @throws WTException
		 * @throws WTPropertyVetoException 
		 * @throws RemoteException 
		 */
			public static void validateECOPresentation(WTObject pbo) throws MaturityException,
					WTException, WTPropertyVetoException, RemoteException {
			
				if ((pbo != null) && ((pbo instanceof WTChangeOrder2))) {
					WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
					QueryResult qrActivities = ChangeHelper2.service
							.getChangeActivities(wtc);
					while (qrActivities.hasMoreElements()) {
						Object objActivities = qrActivities.nextElement();

						if ((objActivities instanceof WTChangeActivity2)) {
							QueryResult qrAfter = ChangeHelper2.service
									.getChangeablesAfter((WTChangeActivity2) objActivities);

							while (qrAfter.hasMoreElements()) {
								WTObject objAfter = (WTObject) qrAfter.nextElement();
								if ((objAfter instanceof WTDocument)) {
									WTDocument doc=(WTDocument)objAfter;
									// 获取文档的类型
									String docType = ClientTypedUtility
											.getExternalTypeIdentifier(doc);
									int n = docType.lastIndexOf(".");
									docType = docType.substring(n + 1);
								    if("".equals(docType)){
								    	Representation representation = RepresentationHelper.service
												.getDefaultRepresentation(doc);
								    	if(representation==null){
								    		throw new WTException("更改通知单"+doc.getNumber()+"未生成可视化，不能提交流程！");
								    	}
								    }
								} 
							}
						}
					}
				}
			}	
			
			
			
			/** 校验更改通知单是否放到产生的对象中
			 * @param pbo
			 * @param publishtoS
			 * @param publishtoC
			 * @throws MaturityException
			 * @throws WTException
			 * @throws WTPropertyVetoException 
			 * @throws RemoteException 
			 */
				public static void validateECOExist(WTObject pbo) throws MaturityException,
						WTException, WTPropertyVetoException, RemoteException {
				
					if ((pbo != null) && ((pbo instanceof WTChangeOrder2))) {
						int IsExistECO=0;
						WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
						QueryResult qrActivities = ChangeHelper2.service
								.getChangeActivities(wtc);
						while (qrActivities.hasMoreElements()) {
							Object objActivities = qrActivities.nextElement();

							if ((objActivities instanceof WTChangeActivity2)) {
								//先检查受影响对象
								/*QueryResult qr = wt.change2.ChangeHelper2.service
										.getChangeablesBefore((WTChangeActivity2) objActivities);
								while (qr.hasMoreElements()) {
									WTObject objAfter = (WTObject) qr.nextElement();
									if ((objAfter instanceof WTDocument)) {
										
										WTDocument doc=(WTDocument)objAfter;
										// 获取文档的类型
										String docType = ClientTypedUtility
												.getExternalTypeIdentifier(doc);
										int n = docType.lastIndexOf(".");
										docType = docType.substring(n + 1);
									    if("tasvdoc03".equals(docType)){									    	
									    		System.out.println("技术更改通知单"+doc.getNumber()+"应该放到产生的对象中，不能放到受影响的对象，请修改！");									    	
									    }
									} 
								}*/
								
								//再检查产生的对象
								QueryResult qrAfter = ChangeHelper2.service
										.getChangeablesAfter((WTChangeActivity2) objActivities);

								while (qrAfter.hasMoreElements()) {
									WTObject objAfter = (WTObject) qrAfter.nextElement();
									if ((objAfter instanceof WTDocument)) {
										WTDocument doc=(WTDocument)objAfter;
										// 获取文档的类型
										String docType = ClientTypedUtility
												.getExternalTypeIdentifier(doc);
										int n = docType.lastIndexOf(".");
										docType = docType.substring(n + 1);
									    if("tasvdoc03".equals(docType)){
									    	IsExistECO=IsExistECO+1;
									    	Representation representation = RepresentationHelper.service
													.getDefaultRepresentation(doc);
									    	if(representation==null){
									    		throw new WTException("技术更改通知单"+doc.getNumber()+"未生成可视化，不能提交流程！");
									    	}
									    }
									} 
								}
							}
						}
						if(IsExistECO<=0){
							throw new WTException("产生的对象里缺少技术更改通知单，不能提交流程！");
						}
					}
				}	
				public static Persistable getLatestPersistableByNumber(String number, Class thisClass)
			    {
			        Persistable persistable = null;
			        try
			        {
			            int index[] = new int[1];
			            QuerySpec qs = new QuerySpec(thisClass);
			            String attribute = (String)thisClass.getField("NUMBER").get(thisClass);
			            qs.appendWhere(new SearchCondition(thisClass, attribute, "LIKE", number), index);
			            QueryResult qr = PersistenceHelper.manager.find(qs);
			            LatestConfigSpec configSpec = new LatestConfigSpec();
			            qr = configSpec.process(qr);
			            if(qr != null && qr.hasMoreElements())
			                persistable = (Persistable)qr.nextElement();
			        }
			        catch(QueryException e)
			        {
			            e.printStackTrace();
			        }
			        catch(WTException e)
			        {
			            e.printStackTrace();
			        }
			        catch(IllegalArgumentException e)
			        {
			            e.printStackTrace();
			        }
			        catch(SecurityException e)
			        {
			            e.printStackTrace();
			        }
			        catch(IllegalAccessException e)
			        {
			            e.printStackTrace();
			        }
			        catch(NoSuchFieldException e)
			        {
			            e.printStackTrace();
			        }
			        return persistable;
			    }
}
