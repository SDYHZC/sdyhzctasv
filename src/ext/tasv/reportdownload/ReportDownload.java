package ext.tasv.reportdownload;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

import ext.hbt.publishstate.PublishState;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.part.WTPart;
import wt.util.WTException;

public class ReportDownload {

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
	
	
	public static void StateValidate(boolean publishtoC, boolean publishtoS,
			boolean publishtoD) throws WTException, IOException {
		System.out.println("发布状态C、S、D分别为" + publishtoC + publishtoS
				+ publishtoD);
		// 判断选择的流程发布状态是否唯一
		if ((publishtoS && publishtoC) || (publishtoC && publishtoD)
				|| (publishtoS && publishtoD)) {
			throw new WTException("只允许选择一个有效发布状态！");
		} else if (!publishtoS && !publishtoC && !publishtoD) {
			throw new WTException("请选择一个有效的发布状态！");
		}
	}
	
/**判断产生的对象是否能发布到指定状态
 * @param pbo
 * @param self
 * @param publishtoS
 * @param publishtoC
 * @param publishtoD
 * @throws ChangeException2
 * @throws WTException
 */
	public static void JudgeReviewObjState(WTObject pbo, ObjectReference self,
			boolean publishtoS, boolean publishtoC, boolean publishtoD)
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
 * @param publishtoD
 * @throws MaturityException
 * @throws WTException
 */
	public static void setReviewObjState(WTObject pbo, boolean publishtoS,
			boolean publishtoC, boolean publishtoD) throws MaturityException,
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

						if ((objAfter instanceof WTDocument)) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("APPROVED"));
						} else {
							 String version =((RevisionControlled)objAfter).getVersionIdentifier ().getValue();
                             char ver = version.charAt(0); 
							if (publishtoS) {
								if ("S".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								} else if ("D".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}else if("C".equals(ver)){
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								}
							}
							
							if (publishtoC) {
								if ("S".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_S"));
								} else if ("D".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}else if("C".equals(ver)){
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_C"));
								}
							}
							
							if (publishtoD) {
								if ("S".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								} else if ("D".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}else if ("C".equals(ver)) {
									LifeCycleHelper.service.setLifeCycleState(
											(LifeCycleManaged) objAfter,
											State.toState("RELEASED_D"));
								}
							}								 
							System.out.println("第二次发布状态S、C、D分别为" + publishtoS + publishtoC
									+ publishtoD);
							
							/*	if (publishtoS) {
								LifeCycleHelper.service.setLifeCycleState(
										(LifeCycleManaged) objAfter,
										State.toState("RELEASED_S"));
							} else if (publishtoC) {
								LifeCycleHelper.service.setLifeCycleState(
										(LifeCycleManaged) objAfter,
										State.toState("RELEASED_C"));
							} else if (publishtoD) {
								LifeCycleHelper.service.setLifeCycleState(
										(LifeCycleManaged) objAfter,
										State.toState("RELEASED_D"));
							}*/
							
						}
					}
				}
			}
		}
	}
	
	
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
						char ver = version.charAt(0);
						if ("C".equals(ver)) {
							LifeCycleHelper.service.setLifeCycleState(
									(LifeCycleManaged) objAfter,
									State.toState("RELEASED_S"));
						}
					}
				}
			}
		}
	}
	
}
