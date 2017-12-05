package ext.tasv.rejectOptimization.processor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import wt.change2.ChangeActivityIfc;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrderIfc;
import wt.change2.ChangeRecord2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.structure.EPMDescribeLink;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.iba.value.IBAHolder;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.vc.Mastered;
import wt.vc.struct.StructHelper;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.hbt.publishstate.PublishState;
import ext.util.IBAUtility;

public class RejectAndCreateECA {
	/**
	 * ECN驳回后自动创建指定子类型的ECA
	 * @param nmCommandBean
	 * @return
	 * @throws WTException
	 */
	public static FormResult rejectCreateECA(NmCommandBean nmCommandBean){
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WTChangeActivity2 eca = null;
			FormResult formResult = new FormResult(FormProcessingStatus.SUCCESS);
			ArrayList<NmOid> list = nmCommandBean.getNmOidSelected();
			String bhyy = TableBuilderHelper.getParam(nmCommandBean, "bhyy");
			System.out.println("list:"+list.size()+":"+list);
			Persistable per = (Persistable) nmCommandBean.getPrimaryOid().getRefObject();
			//System.out.println("per:"+per+"+  bhyy:"+bhyy);
			if(bhyy.isEmpty()){
				FormResult formResult1 = new FormResult(FormProcessingStatus.FAILURE);
				formResult1.setNextAction(FormResultAction.NONE);
				String s = "请填写驳回原因！！！";
                FeedbackMessage fbm = new FeedbackMessage(FeedbackType.FAILURE, SessionHelper.getLocale(),
                        null, null, new String[] { s });    
                formResult1.addFeedbackMessage(fbm);
				return formResult1;
			}
			if(list.size()<1){
				FormResult formResult1 = new FormResult(FormProcessingStatus.FAILURE);
				formResult1.setNextAction(FormResultAction.NONE);
				String s = "驳回前请勾选需要驳回的对象！！！";
                FeedbackMessage fbm = new FeedbackMessage(FeedbackType.FAILURE, SessionHelper.getLocale(),
                        null, null, new String[] { s });    
                formResult1.addFeedbackMessage(fbm);
				return formResult1;
			}
			if(per instanceof WorkItem){
				Set s_eca = new HashSet();
				Set s_del = new HashSet();
				WorkItem wo = (WorkItem) per;
				WfAssignedActivity aa = (WfAssignedActivity) wo.getSource().getObject();
				WfProcess ps = null;
				if(aa != null){
					ps = aa.getParentProcess();
				}
				//通过选择的对象收集全相关的对象
				HashSet s_sel = new HashSet();
				for(NmOid oid:list){
					Object o = oid.getRefObject();
					s_sel.add(o);
					if(o instanceof WTPart){
						WTPart p = (WTPart) o;
						//通过part找二维或者三维
//						QueryResult qr = PartDocHelper.service.getAssociatedDocuments(p);
						QueryResult qr_2 = WTPartHelper.service.getDescribedByDocuments(p,true);
						while (qr_2.hasMoreElements()) {
							System.out.println("找到CAD/动态文档");
							Persistable obj = (Persistable) qr_2.nextElement();
							if(obj instanceof EPMDocument){
								EPMDocument epm = (EPMDocument) obj;
								s_sel.add(epm);
								String e_name =  epm.getCADName();
								System.out.println("asssociate:"+e_name);
								if(e_name.toUpperCase().endsWith(("DRW"))){
									s_sel = getAssociatedEPMByDRW(s_sel,epm);
								}
							}
						}
					}else if(o instanceof EPMDocument){
						EPMDocument e = (EPMDocument) o;
						s_sel.add(e);
						String e_name =  e.getCADName();
						if(e_name.toUpperCase().endsWith("DRW")){
							s_sel = getAssociatedEPMByDRW(s_sel,e);
						}else if(e_name.toUpperCase().endsWith("DWG")){
							QueryResult qr = PersistenceHelper.manager.navigate(e,EPMDescribeLink.DESCRIBES_ROLE, EPMDescribeLink.class,false);
							while (qr.hasMoreElements()) {
								EPMDescribeLink link = (EPMDescribeLink) qr.nextElement();
								WTPart part = link.getDescribes();
								System.out.println("part name"+part.getName());
								s_sel.add(part);
							}
						}else if(e_name.toUpperCase().endsWith("PRT")||e_name.toUpperCase().endsWith("ASM")){
							System.out.println("insert into prt");
//							QueryResult qr = PersistenceHelper.manager.navigate(e, EPMReferenceLink.REFERENCED_BY_ROLE, EPMReferenceLink.class, false);
							s_sel = getRefEPMByPrt(s_sel, e);
						}
					}
				}
				Persistable per1 = wo.getPrimaryBusinessObject().getObject();
				if(per1 instanceof WTChangeActivity2){
					WTChangeActivity2 eca1 = (WTChangeActivity2) per1;
					//查询出ECA下所有的对象
					QueryResult qr_eca = ChangeHelper2.service.getChangeablesAfter(eca1);
					while(qr_eca.hasMoreElements()){
						s_eca.add(qr_eca.nextElement());
					}
					System.out.println("s_sel_all+s_eca:"+s_sel.size()+"+"+s_eca.size());
					s_eca.retainAll(s_sel);
					s_del = s_eca;
					//从ECA中移除选择的对象
					for(Object o:s_del){
						ChangeHelper2.service.unattachChangeable((Changeable2)o, eca1, ChangeRecord2.class, ChangeRecord2.CHANGE_ACTIVITY2_ROLE);
					}
					QueryResult qr = ChangeHelper2.service.getChangeOrder(eca1);
					if(qr.hasMoreElements()){
						WTChangeOrder2 oo = (WTChangeOrder2) qr.nextElement();
						per1 = oo;
					}
				}
				if(per1 instanceof WTChangeOrder2){
					if(s_del.size()==0){
						//从默认ECA中移除选择的对象
						QueryResult qrr = ChangeHelper2.service.getChangeActivities((ChangeOrderIfc) per1);
						while(qrr.hasMoreElements()){
							WTChangeActivity2 ecaa = (WTChangeActivity2) qrr.nextElement();
							if(!ecaa.getName().contains(((WTChangeOrder2) per1).getNumber())){
								QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecaa);
								while(qr.hasMoreElements()){
									s_eca.add(qr.nextElement());
								}
								s_eca.retainAll(s_sel);
								s_del = s_eca;
								//从ECA中移除选择的对象
								for(Object o:s_del){
									ChangeHelper2.service.unattachChangeable((Changeable2)o, ecaa, ChangeRecord2.class, ChangeRecord2.CHANGE_ACTIVITY2_ROLE);
								}
								break;
							}
						}
					}
					WTChangeOrder2 o = (WTChangeOrder2) per1;
					//自动创建ECA
					eca = WTChangeActivity2.newWTChangeActivity2();
					if(eca != null){
						System.out.println("eca num+name:"+eca.getNumber()+"+"+eca.getName());
						//设置ECA容器、子类型、流程团队
						eca.setName("ECN-"+o.getNumber());
						eca.setContainer(o.getContainer());
						eca.setTypeDefinitionReference(TypedUtilityServiceHelper.service.getTypeDefinitionReference("wt.change2.WTChangeActivity2|com.tasv.BHECA"));
						eca.setTeamId(ps.getTeamId());
						eca = (WTChangeActivity2) ChangeHelper2.service.saveChangeActivity(o, eca);
						IBAUtility iba = new IBAUtility(eca);
						iba.setIBAValue("BHYY", bhyy);
						IBAHolder h = iba.updateAttributeContainer(eca);
						iba.updateIBAHolder(h);
						//添加选择的对象
						Vector v = new Vector();
						for(Object oo:s_del){
							v.add(oo);
						}
						ChangeHelper2.service.storeAssociations(ChangeRecord2.class, eca, v);
//						重命名ECA
						Mastered ms = null;
						ms = eca.getMaster();
						ms = ChangeHelper2.service.changeChangeItemMasterIdentity(ms, o.getNumber()+"-"+eca.getNumber(), null, null);
					}
				}
			}
			if(eca != null){
				System.out.println("ECA already auto create success!");
//				formResult.setNextAction(FormResultAction.JAVASCRIPT);
//				formResult.setJavascript("alert('驳回成功');");
				formResult.setNextAction(FormResultAction.NONE);
				String s = "驳回成功！！！";
                FeedbackMessage fbm = new FeedbackMessage(FeedbackType.SUCCESS, SessionHelper.getLocale(),
                        null, null, new String[] { s });    
                formResult.addFeedbackMessage(fbm);
			}
			return formResult;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ECA already auto create faied!");
		}finally{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return null;
	}

	/**
	 * 获取参考方EPM文档
	 * @param s_sel
	 * @param e
	 * @throws QueryException
	 * @throws WTException
	 */
	public static HashSet getRefEPMByPrt(HashSet s_sel, EPMDocument e)
			throws QueryException, WTException {
		QuerySpec qs_r1= new QuerySpec(EPMReferenceLink.class);
		QueryResult qr= EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster)e.getMaster(), qs_r1, true);
		while(qr.hasMoreElements()){
			EPMDocument link = (EPMDocument) qr.nextElement();
			System.out.println("ref by:"+link.getCADName());
			s_sel.add(link);
		}
		return s_sel;
	}
	
	/**
	 * 通过DRW图纸获取相关CAD文档
	 * @param s_sel
	 * @param epm
	 * @return
	 * @author Czg
	 * @throws WTException
	 */
	private static HashSet getAssociatedEPMByDRW(HashSet s_sel, EPMDocument epm) throws WTException {
		//获取相关部件
		QueryResult qr_prt = PersistenceHelper.manager.navigate(epm,EPMDescribeLink.DESCRIBES_ROLE, EPMDescribeLink.class,false);
		while (qr_prt.hasMoreElements()) {
			EPMDescribeLink link = (EPMDescribeLink) qr_prt.nextElement();
			WTPart part = link.getDescribes();
			System.out.println("DESCRIBES_ROLE part name :"+part.getName());
			s_sel.add(part);
		}
		QueryResult qr_prt1 = PersistenceHelper.manager.navigate(epm,EPMDescribeLink.DESCRIBED_BY_ROLE, EPMDescribeLink.class,false);
		while (qr_prt1.hasMoreElements()) {
			EPMDescribeLink link = (EPMDescribeLink) qr_prt1.nextElement();
			WTPart part = link.getDescribes();
			System.out.println("DESCRIBED_BY_ROLE part name :"+part.getName());
			s_sel.add(part);
		}
		//相关EPM
		QuerySpec qs_r1= new QuerySpec(EPMReferenceLink.class);
		QueryResult qr2= EPMStructureHelper.service.navigateReferences(epm, qs_r1, true);
		while(qr2.hasMoreElements())
		{
			EPMDocument referencedBy= PublishState.getLatestEPMDocByMaster((EPMDocumentMaster)qr2.nextElement());
			s_sel.add(referencedBy);
			String name = referencedBy.getCADName();
			System.out.println("refer2:"+name);
			if(name.toUpperCase().endsWith("ASM")){
				s_sel = getAllByASM(s_sel,referencedBy);
			}
		}
		return s_sel;
	}

	private static HashSet getAllByASM(HashSet s_sel, EPMDocument epm) throws WTException {
		QueryResult qs = StructHelper.service.navigateUses(epm);
        while(qs.hasMoreElements()){
            EPMDocument s_epm = (EPMDocument)PublishState.getLatestEPMDocByMaster((EPMDocumentMaster)qs.nextElement());
            s_sel.add(s_epm);
            String name = s_epm.getCADName();
            System.out.println("sub EPM:"+name);
            QueryResult qr_prt = PersistenceHelper.manager.navigate(s_epm,EPMDescribeLink.DESCRIBES_ROLE, EPMDescribeLink.class,false);
    		while (qr_prt.hasMoreElements()) {
    			EPMDescribeLink link = (EPMDescribeLink) qr_prt.nextElement();
    			WTPart part = link.getDescribes();
    			System.out.println("DESCRIBES_ROLE part name :"+part.getName());
    			s_sel.add(part);
    		}
    		QueryResult qr_prt1 = PersistenceHelper.manager.navigate(s_epm,EPMDescribeLink.DESCRIBED_BY_ROLE, EPMDescribeLink.class,false);
    		while (qr_prt1.hasMoreElements()) {
    			EPMDescribeLink link = (EPMDescribeLink) qr_prt1.nextElement();
    			WTPart part = link.getDescribes();
    			System.out.println("DESCRIBED_BY_ROLE part name :"+part.getName());
    			s_sel.add(part);
    		}
            //搜集子件相关对象
            QuerySpec qs_r1= new QuerySpec(EPMReferenceLink.class);
			QueryResult qr= EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster)s_epm.getMaster(), qs_r1, true);
			while(qr.hasMoreElements()){
				EPMDocument link = (EPMDocument) qr.nextElement();
				s_sel.add(link);
			}
            if(name.toUpperCase().endsWith("ASM")){
            	s_sel.addAll(getAllByASM(s_sel, s_epm));
            }
        }
		return s_sel;
	}

	public static boolean checkAllECAState(WTObject obj) throws WTException{
		boolean flag = false;
		if(obj instanceof WTChangeOrder2){
			WTChangeOrder2 o = (WTChangeOrder2) obj;
			QueryResult qr = ChangeHelper2.service.getChangeActivities(o);
			while(qr.hasMoreElements()){
				WTChangeActivity2 eca = (WTChangeActivity2) qr.nextElement();
				String state = eca.getLifeCycleState().toString();
				if(!"RESOLVED".equalsIgnoreCase(state)) return flag;
			}
			flag = true;
		}
		return flag;
	}
	
	public static void setProcessVariables(ObjectReference or,WTObject obj) throws WTException{
		WfProcess ps = getProcess(or);
		if(obj instanceof WTChangeActivity2){
			Boolean toC = false;
			Boolean toS = false;
			QueryResult qr = ChangeHelper2.service.getChangeOrder((ChangeActivityIfc) obj);
			if(qr.hasMoreElements()){
				WTChangeOrder2 o = (WTChangeOrder2) qr.nextElement();
				Enumeration e = WfEngineHelper.service.getAssociatedProcesses(o, null);
				if(e.hasMoreElements()){
					WfProcess p = (WfProcess) e.nextElement();
					ProcessData pd = p.getContext();
					toC = (Boolean) pd.getValue("publishtoC");
					toS = (Boolean) pd.getValue("publishtoS");
				}
			}
			if(ps != null){
				ProcessData pdd = ps.getContext();
				System.out.println("toc+tos:"+toC+toS);
				pdd.setValue("publishtoC", toC);
				pdd.setValue("publishtoS", toS);
				ps = (WfProcess) PersistenceHelper.manager.modify(ps);
			}
		}
	}
	
	// 获取工作流进程
		public static WfProcess getProcess(ObjectReference processRef)
				throws WTException {
			WfProcess wfprocess = null;
			try {
				Persistable persistable = processRef.getObject();
				if (persistable instanceof WfActivity) {
					WfActivity mySelf = (WfActivity) persistable;
					wfprocess = mySelf.getParentProcess();
				} else if (persistable instanceof WfProcess) {
					wfprocess = (WfProcess) persistable;
				}
			} catch (WTRuntimeException e) {
				e.printStackTrace();
			}
			return wfprocess;
		}
}
