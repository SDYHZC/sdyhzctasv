package ext.workflow;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.MaturityException;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import com.ptc.core.components.forms.FormResult;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.work.NmWorkItemCommands;

import ext.tasv.rejectOptimization.processor.TableBuilderHelper;
import ext.util.IBAUtility;

public class CustNmWorkItemCommands{
	
	private static boolean SHOW_NESTED_GROUPS;
	
	public static FormResult complete(NmCommandBean nmCommandBean)throws WTException, ParseException{
		//custom code
		//System.out.println("custom complete task-------start---------");
		String workitemoid =nmCommandBean.getRequest().getParameter("oid");
		ReferenceFactory referenceFactory = new ReferenceFactory();
		WorkItem workItem =(WorkItem) referenceFactory.getReference(workitemoid).getObject();
		Persistable pbo =workItem.getPrimaryBusinessObject().getObject();
		WfActivity activity = (WfActivity)workItem.getSource().getObject();
		WfProcess process = activity.getParentProcess();
		//System.out.println("----pname-:"+process.getName()+"+aname:"+activity.getName());
		String paramName1 = "";//记录控制是否启动流程的变量名
		String paramName2 = "";
		boolean flag = true;
		if(pbo instanceof WTChangeOrder2){
			if(process.getName().contains("Tasv_ECN_Workflow")){
				if("更改单发放".equals(activity.getName()) || "通知单接收".equals(activity.getName())){
					//自动创建文档
					//OrganizationServicesHelper.manager.get
					try {
						Enumeration enu = null;
						WTGroup g = null;
						String dep = "";
						IBAUtility iuO = new IBAUtility((IBAHolder)(ChangeOrder2)pbo);
						String cpxh = iuO.getIBAValue("CPXH");
						if(cpxh ==null || cpxh=="" || cpxh=="null"){
							return NmWorkItemCommands.complete(nmCommandBean);
						}
						//检查是否需要创建变更反馈文档
//						System.out.println("before:"+workitemoid);
						Enumeration en = nmCommandBean.getRequest().getParameterNames();
//						HashMap<String, Object> map = nmCommandBean.getParameterMap();
						boolean qd = false;
						boolean bqd = false;
						while(en.hasMoreElements()){
							String k = (String) en.nextElement();
							if(k.contains("buqidong") && !k.endsWith("old")){
								bqd = "on".equals(nmCommandBean.getTextParameter(k))?true:false;
								paramName1 = k;
							}else if(k.contains("qidong") && !k.endsWith("old")){
								qd = "on".equals(nmCommandBean.getTextParameter(k))?true:false;
								paramName2 = k;
							}
						}
//						System.out.println("qd+bqd:"+qd+bqd);
						//20171101过滤旧流程
						Timestamp time = ((ChangeOrder2)pbo).getCreateTimestamp();
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
						Date d1 = df.parse("20171020 00:00:00");
						if(time.after(d1)){
							if(qd && bqd){
								throw new WTException("只能勾选一个变量来判断是否启动变更执行反馈流程，请仔细确认！");
							}else if(!qd && !bqd){
								throw new WTException("请勾选一个变量来判断是否启动变更执行反馈流程！");
							}else if(bqd){
								FormResult rt = NmWorkItemCommands.complete(nmCommandBean);
								flag = SessionServerHelper.manager.setAccessEnforced(false);
								activity = (WfActivity) PersistenceHelper.manager.refresh(activity);
								ProcessData pd = activity.getContext();
								pd.setValue("buqidong", false);
								PersistenceHelper.manager.modify(activity);
								SessionServerHelper.manager.setAccessEnforced(flag);
								return rt;
							}
						}
						flag = SessionServerHelper.manager.setAccessEnforced(false);
						WTDocument doc = WTDocument.newWTDocument();
						//设置文档名称：部门+变更通告编号
						//doc.setName("auto-create-20170825");
						WTPrincipal p = SessionHelper.getPrincipal();
						WTUser u = (WTUser) p;
						enu = u.parentGroups(SHOW_NESTED_GROUPS);
						while(enu.hasMoreElements()){
							g = (WTGroup) ((WTPrincipalReference)enu.nextElement()).getPrincipal();
							if(g.getName().contains("_接收组")){
								dep = g.getName().substring(0,g.getName().length()-4);
							}
						}
						doc.setName("ZXFK:"+((WTChangeOrder2) pbo).getNumber()+":"+dep);
						doc.setDocType(DocumentType.toDocumentType("$$Document"));
						doc.setContainer(getWTContainerByName("变更执行反馈"));
						TypeDefinitionReference tdf = TypedUtilityServiceHelper.service.getTypeDefinitionReference("wt.doc.WTDocument|com.tasv.BGZXFK");
						if(tdf != null){
							doc.setTypeDefinitionReference(tdf);
						}
						doc = (WTDocument) PersistenceHelper.manager.store(doc);
						IBAUtility iuD = new IBAUtility(doc);
						iuD.setIBAValue("CPXH", cpxh);
							//doc = (WTDocument) PersistenceHelper.manager.save(doc);
						iuD.setIBAValue("BMMC", dep);
						iuD.setIBAValue("ECNNO", ((WTChangeOrder2) pbo).getNumber());
						IBAHolder h = iuD.updateAttributeContainer(doc);
						iuD.updateIBAHolder(h);
					} catch (WTPropertyVetoException e) {
						e.printStackTrace();
						throw new WTException("创建变更执行反馈文档出错！");
					}catch (RemoteException e) {
						e.printStackTrace();
						throw new WTException("创建变更执行反馈文档出错！");
					}catch(WTException e){
						e.printStackTrace();
						throw e;
					}finally{
						SessionServerHelper.manager.setAccessEnforced(flag);
					}
				}
			}
		}
		FormResult rt = NmWorkItemCommands.complete(nmCommandBean);
		if(!"".equals(paramName2)){
			flag = SessionServerHelper.manager.setAccessEnforced(false);
			activity = (WfActivity) PersistenceHelper.manager.refresh(activity);
			ProcessData pd = activity.getContext();
			pd.setValue("qidong", false);
			PersistenceHelper.manager.modify(activity);
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return rt;
	}
	
	/**
     * @author caizg
     * @directions 通过名称获取容器对象
     * @param 
     *
   */
   public static WTContainer getWTContainerByName(String containerName){
	   WTContainer con = null;
	   if (containerName == null){
		   throw new IllegalArgumentException("The containerName is null!!!");
	   }
	   try{
		   QuerySpec qs = new QuerySpec(WTContainer.class);
		   qs.appendWhere(new SearchCondition(WTContainer.class,
				   WTContainer.NAME,
				   SearchCondition.EQUAL,
				   containerName.toUpperCase(),
				   false), new int[] { 0 });
		   QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		   if (qr.hasMoreElements()){
			   con = (WTContainer) qr.nextElement();
		   }
	   } catch (WTException e){
		   e.printStackTrace();
	   }
	   return con;
    }
   
   /**
    * 设置变更产生对象的生命周期状态
    * @param primaryBusinessObject
    * @param state
    * @throws MaturityException
    * @throws WTException
    */
   public static void setECNEffectObjectState(WTObject primaryBusinessObject,String state)
		   throws MaturityException, WTException{
	   WTChangeOrder2 wtc = (WTChangeOrder2)primaryBusinessObject;
	   if ((wtc != null) && ((wtc instanceof WTChangeOrder2))){
		   QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(wtc);
		   while (qrActivities.hasMoreElements()) {
			   Object objActivities = qrActivities.nextElement();
			   if ((objActivities instanceof WTChangeActivity2)) {
				   QueryResult qrAfter = ChangeHelper2.service.getChangeablesAfter((WTChangeActivity2)objActivities);
				   while (qrAfter.hasMoreElements()){
					   WTObject objAfter = (WTObject)qrAfter.nextElement();
					   if ((objAfter instanceof WTDocument)) {
						   LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged)objAfter, State.toState(state));
					   }
				   }
			   }
		   }
	   }
   }
   
   /**
    * 是否到达已执行状态
    * @param pbo
    * @return
    * @throws WTException
    */
   public static String isExecute(WTObject pbo) throws WTException{
	   String f = "1";
	   if(pbo instanceof WTChangeOrder2){
		   String num = ((WTChangeOrder2) pbo).getNumber();
		   QuerySpec qs = new QuerySpec(WTDocumentMaster.class);
		   qs.appendWhere(new SearchCondition(WTDocumentMaster.class, WTDocumentMaster.NAME, SearchCondition.LIKE, "ZXFK:"+num+":%"),new int[] { 0 });
		   QueryResult qr = PersistenceServerHelper.manager.query((StatementSpec) qs);
	       //qr = new LatestConfigSpec().process(qr);
		   //System.out.println("ceshi qr.size:"+qr.size());
	       while(qr.hasMoreElements()){
	    	   WTDocumentMaster dm = (WTDocumentMaster) qr.nextElement();
	    	   WTDocument d = (WTDocument) getLatestPersistableByNumber(dm.getNumber(), WTDocument.class);
	    	   if(!"Execution".equalsIgnoreCase(d.getLifeCycleState().toString())){
	    		   return f;
	    	   }
	       }
	       f = "go";
	   }
	   return f;
   }
   
   static
   {
     try
     {
       WTProperties localWTProperties = WTProperties.getLocalProperties();
       SHOW_NESTED_GROUPS = localWTProperties.getProperty("principal.admin.show.nested.group.membership", false);
     } catch (IOException localIOException) {
       localIOException.printStackTrace();
     }
   }
   
   /**
    * 根据对象的number找到最新版本的对象
    * @author gongke
    * @param number    要查询的对象的编号
    * @param thisClass class对象
    * @return 由number标识的最新版本对象
    */
   public static Persistable getLatestPersistableByNumber(String number,Class thisClass) {
       Persistable persistable = null;
       try {
           int[] index = {0};
           QuerySpec qs = new QuerySpec(thisClass);
           String attribute = (String) thisClass.getField("NUMBER").get(
                   thisClass);
           qs.appendWhere(new SearchCondition(thisClass, attribute, SearchCondition.LIKE, number), index);

           QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

           LatestConfigSpec configSpec = new LatestConfigSpec();
           qr = configSpec.process(qr);
           WorkItem work;
           //ContainerTeamHelper.service.

           
           if (qr != null && qr.hasMoreElements()) {
               persistable = (Persistable) qr.nextElement();
           }
       } catch (QueryException e) {
           e.printStackTrace();
       } catch (WTException e) {
           e.printStackTrace();
       } catch (IllegalArgumentException e) {
           e.printStackTrace();
       } catch (SecurityException e) {
           e.printStackTrace();
       } catch (IllegalAccessException e) {
           e.printStackTrace();
       } catch (NoSuchFieldException e) {
           e.printStackTrace();
       }
       return persistable;
   }
}
