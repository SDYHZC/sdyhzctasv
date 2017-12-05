package ext.workflow;

import com.basicwtapi.ec.ECModifier;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.cadx.common.util.GenericUtilities;
import com.ptc.windchill.enterprise.change2.commands.RelatedChangesQueryCommands;
import com.ptc.windchill.mpml.processplan.MPMProcessPlan;
import com.ptc.windchill.mpml.processplan.operation.MPMOperation;
import ext.part.PartUtil;
import ext.pdf.SignPDF_lg;
import ext.util.IBAUtility;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;
import wt.change2.ChangeActivity2;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrderIfc;
import wt.change2.ChangeRecord2;
import wt.change2.ChangeRequest2;
import wt.change2.ChangeRequestIfc;
import wt.change2.ChangeService2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.epm.structure.EPMDescribeLink;
import wt.fc.BinaryLink;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.PersistenceManagerSvr;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTCollection;
import wt.folder.Folder;
import wt.inf.container.WTContainerRef;
import wt.introspection.LinkInfo;
import wt.introspection.RoleDescriptor;
import wt.introspection.WTIntrospector;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleService;
import wt.lifecycle.State;
import wt.maturity.MaturityException;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.pom.Transaction;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationIdentifier;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressService;
import wt.vc.wip.Workable;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfDefinerService;
import wt.workflow.definer.WfProcessDefinition;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineServerHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfEngineServiceSvr;
import wt.workflow.engine.WfProcess;

public class ECNUtil
{
  private static final String PRIMARY_BUSINESS_OBJECT = "primaryBusinessObject";
  private static final String CLASSNAME = WfUtil.class.getName();

  public static String getChangeRequestType(WTObject obj) {
    String typestr = null;
    try {
      if ((obj instanceof WTChangeRequest2)) {
        WTChangeRequest2 cn = (WTChangeRequest2)obj;
        IBAUtility ibaUtil = new IBAUtility(cn);
        String cnType = ibaUtil.getIBAValue("ECRType");
        if (cnType == null) {
          cnType = "";
        }
        if (cnType.equals("SJGGQQ"))
          typestr = "EBOM_Change";
        else if (cnType.equals("GYGGQQ"))
          typestr = "MBOM_Change";
        else
          typestr = "";
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return typestr;
  }

  public static String getChangeNoticeType(WTObject obj) {
    String typestr = null;
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 cn = (WTChangeOrder2)obj;
        IBAUtility ibaUtil = new IBAUtility(cn);
        String cnType = ibaUtil.getIBAValue("ChangeNoticeType");
        if (cnType == null) {
          cnType = "";
        }
        if (cnType.equals("设计BOM变更"))
          typestr = "EBOM_Change";
        else if ((cnType.equals("制造BOM变更")) || (cnType.equals("工艺路线变更")))
          typestr = "MBOM_Change";
        else
          typestr = "GZ_Change";
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return typestr;
  }

  public static void startProcess(WTChangeRequest2 ecn, String workflowtemplatename) throws WTException
  {
    String chgname = ecn.getName();
    WTPrincipalReference pf = ecn.getCreator();
    WTContainerRef containerRef = ecn.getContainerReference();

    WfProcessDefinition wfprocessdefinition = WfDefinerHelper.service
      .getProcessDefinition(workflowtemplatename);
    WfProcessTemplate processTemplate = wfprocessdefinition
      .getProcessTemplate();

    WfProcessTemplate template = processTemplate;
    SessionHelper.manager.setAdministrator();
    try
    {
      WfProcess aWfProcess = WfEngineHelper.service.createProcess(
        template, ecn);

      aWfProcess.setName(chgname);

      aWfProcess.setCreator(pf);
      aWfProcess.setContainerReference(containerRef);

      aWfProcess.setTeamTemplateId(ecn.getTeamTemplateId());
      ProcessData aProcessData = aWfProcess.getContext();
      aProcessData.setValue("primaryBusinessObject", 
        ecn);
      WfProcess process = WfEngineServerHelper.service
        .setPrimaryBusinessObject(aWfProcess, ecn);

      WfEngineHelper.service.startProcessImmediate(process, aProcessData, 
        0L);
    }
    catch (WTPropertyVetoException pve)
    {
      throw new WTException(pve);
    }
  }

  public static void startProcess(WTChangeOrder2 ecn, String workflowtemplatename) throws WTException
  {
    String chgname = ecn.getName();
    WTPrincipalReference pf = ecn.getCreator();
    WTContainerRef containerRef = ecn.getContainerReference();

    WfProcessDefinition wfprocessdefinition = WfDefinerHelper.service
      .getProcessDefinition(workflowtemplatename);
    WfProcessTemplate processTemplate = wfprocessdefinition
      .getProcessTemplate();

    WfProcessTemplate template = processTemplate;

    long time = System.currentTimeMillis();
    SessionContext previous_context = SessionContext.newContext();
    SessionHelper.manager.setAdministrator();
    try
    {
      WfProcess aWfProcess = WfEngineHelper.service.createProcess(
        template, ecn);

      aWfProcess.setName(chgname);

      aWfProcess.setCreator(pf);
      aWfProcess.setContainerReference(containerRef);

      aWfProcess.setTeamTemplateId(ecn.getTeamTemplateId());
      ProcessData aProcessData = aWfProcess.getContext();
      aProcessData.setValue("primaryBusinessObject", 
        ecn);
      WfProcess process = WfEngineServerHelper.service
        .setPrimaryBusinessObject(aWfProcess, ecn);

      WfEngineHelper.service.startProcessImmediate(process, aProcessData, 
        0L);
    }
    catch (WTPropertyVetoException pve)
    {
      throw new WTException(pve);
    }
  }

  public static boolean CheckECRChangeables(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof ChangeRequest2)) {
        ChangeRequest2 changeRequest = (ChangeRequest2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeables(changeRequest, true);
        if (qr.size() == 0)
          flag = true;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static String CheckedOutECRData(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof ChangeRequest2)) {
        ChangeRequest2 changeRequest = (ChangeRequest2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeables(changeRequest, true);
        while (qr.hasMoreElements()) {
          Persistable persistable = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)persistable))
            errorlog = errorlog + "<br>对象" + persistable.getIdentity() + "处于检出状态,因此不能提交.";
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String CheckECRDataState(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof ChangeRequest2)) {
        ChangeRequest2 changeRequest = (ChangeRequest2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeables(changeRequest, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if ((!currentState.equalsIgnoreCase("APPROVED")) && (!currentState.equalsIgnoreCase("APPROVED_M")))
              errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "不是已批准状态,因此不能提交.";
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static boolean ECNBeforeDataChecked(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof ChangeOrderIfc)) {
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore((ChangeOrderIfc)obj, true);
        if (qr.size() == 0)
          flag = true;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static String ECNBeforeDataCheckedView(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof WTPart)) {
            pobj = (WTPart)pobj;
            if (((WTPart)pobj).getViewName().equals("Manufacturing"))
              errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "为制造视图,因此不能提交.";
          }
          else if ((pobj instanceof MPMProcessPlan)) {
            MPMProcessPlan plan = (MPMProcessPlan)pobj;
            errorlog = errorlog + "<br>对象" + plan.getIdentity() + "为工艺计划,因此不能提交.";
          } else if ((pobj instanceof MPMOperation)) {
            MPMOperation oper = (MPMOperation)pobj;
            errorlog = errorlog + "<br>对象" + oper.getIdentity() + "为工序,因此不能提交.";
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNAfterDataCheckedView(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof WTPart)) {
            pobj = (WTPart)pobj;
            if (((WTPart)pobj).getViewName().equals("Manufacturing"))
              errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "为制造视图,因此不能提交.";
          }
          else if ((pobj instanceof MPMProcessPlan)) {
            MPMProcessPlan plan = (MPMProcessPlan)pobj;
            errorlog = errorlog + "<br>对象" + plan.getIdentity() + "为工艺计划,因此不能提交.";
          } else if ((pobj instanceof MPMOperation)) {
            MPMOperation oper = (MPMOperation)pobj;
            errorlog = errorlog + "<br>对象" + oper.getIdentity() + "为工序,因此不能提交.";
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNBeforeDataCheckedMView(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof WTPart)) {
            pobj = (WTPart)pobj;
            if (((WTPart)pobj).getViewName().equals("Design"))
              errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "为设计视图,因此不能提交.";
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNAfterDataCheckedMView(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof WTPart)) {
            pobj = (WTPart)pobj;
            if (((WTPart)pobj).getViewName().equals("Design"))
              errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "为设计视图,因此不能提交.";
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNBeforeDataInECN(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        QueryResult qrs;
        for (; qr.hasMoreElements(); 
          qrs.hasMoreElements())
        {
          Changeable2 changeable2 = (Changeable2)qr.nextElement();

          qrs = RelatedChangesQueryCommands.getRelatedAffectingChangeNotices(changeable2);
          continue;
          WTObject wobj = (WTObject)qrs.nextElement();
          if ((wobj instanceof WTChangeOrder2)) {
            WTChangeOrder2 changeOrder = (WTChangeOrder2)wobj;
            String currentState = changeOrder.getLifeCycleState().toString();
            if ((!changeOrder.getNumber().equals(ecn.getNumber())) && (!currentState.equals("RESOLVED"))) {
              errorlog = errorlog + "<br>变更通告" + changeOrder.getNumber() + "的受影响对象中包含" + changeable2.getIdentity() + "，请先移除.";
            }

          }

        }

      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNAfterDataInECN(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        Iterator iter;
        for (; qr.hasMoreElements(); 
          iter.hasNext())
        {
          Changeable2 changeable2 = (Changeable2)qr.nextElement();

          WTCollection qrw = (WTCollection)RelatedChangesQueryCommands.getRelatedResultingChangeNotices(changeable2);
          iter = qrw.iterator();
          continue;
          ObjectReference or = (ObjectReference)iter.next();
          WTChangeOrder2 changeOrder = (WTChangeOrder2)or.getObject();
          String currentState = changeOrder.getLifeCycleState().toString();
          if ((!changeOrder.getNumber().equals(ecn.getNumber())) && (!currentState.equals("RESOLVED")))
            errorlog = errorlog + "<br>变更通告" + changeOrder.getNumber() + "的产生对象中包含" + changeable2.getIdentity() + "，请先移除.";
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNBeforeDataCheckedOut(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)pobj))
            errorlog = errorlog + "<br>对象" + pobj.getIdentity() + "处于检出状态,因此不能提交.";
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static boolean ZJDEBOMDataChecked(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if (((pobj instanceof WTPart)) && 
            (((WTPart)pobj).getViewName().equals("Manufacturing"))) {
            flag = true;
            break;
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static boolean ZJDMBOMDataChecked(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if (((pobj instanceof WTPart)) && 
            (((WTPart)pobj).getViewName().equals("Design"))) {
            flag = true;
            break;
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static boolean ZJDEBOMDataCheckState(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if (!currentState.equalsIgnoreCase("APPROVED")) {
              flag = true;
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static boolean ZJDMBOMDataCheckState(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if (!currentState.equalsIgnoreCase("APPROVED_M")) {
              flag = true;
              break;
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static String ECNBeforeDataCheckState(WTObject obj) {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(ecn, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if ((!currentState.equalsIgnoreCase("APPROVED")) && (!currentState.equalsIgnoreCase("APPROVED_M")) && 
              (!currentState.equalsIgnoreCase("APPROVED_P")))
            {
              errorlog = errorlog + "<br>受影响的对象中" + pobj.getIdentity() + "不是已批准状态,因此不能提交.";
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static boolean ECNAfterDataChecked(WTObject obj)
  {
    boolean flag = false;
    try {
      if ((obj instanceof ChangeOrderIfc)) {
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter((ChangeOrderIfc)obj, true);
        if (qr.size() == 0)
          flag = true;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static String ECNDataChecked(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)pobj))
            errorlog = errorlog + "<br>产生的对象中" + pobj.getIdentity() + "处于检出状态,因此不能提交.";
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static String ECNAfterDataCheckState(WTObject obj)
  {
    String errorlog = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if ((!currentState.equalsIgnoreCase("INWORK")) && 
              (!currentState.equalsIgnoreCase("INWORK_M")) && 
              (!currentState.equalsIgnoreCase("INWORK_P")) && 
              (!currentState.equalsIgnoreCase("REWORKS")) && 
              (!currentState.equalsIgnoreCase("REWORKS_M")) && 
              (!currentState.equalsIgnoreCase("REWORKS_P")))
            {
              errorlog = errorlog + "<br>产生的对象中" + pobj.getIdentity() + "不是正在工作或重新工作状态,因此不能提交.";
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static void setECNState(WTObject obj, String ECNState)
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "setECNState";
      Class[] argTypes = { WTObject.class, String.class };
      Object[] argValues = { obj, ECNState };
      try {
        RemoteMethodServer.getDefault().invoke(method, CLASSNAME, null, 
          argTypes, argValues);
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
      }
      return;
    }

    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try
    {
      try
      {
        if ((obj instanceof LifeCycleManaged)) {
          State stateDist = State.toState(ECNState);
          LifeCycleHelper.service.setLifeCycleState(
            (LifeCycleManaged)obj, stateDist);
        }
      }
      catch (Exception e1) {
        e1.printStackTrace();
      }

      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn, 
        true);
      while (qr.hasMoreElements())
        try {
          ChangeActivity2 ca = (ChangeActivity2)qr.nextElement();
          State stateDist = State.toState(ECNState);
          LifeCycleHelper.service.setLifeCycleState(
            ca, stateDist);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access);
    }
  }

  public static void setECNDataState(WTObject obj, String ECNState)
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "setECNDataState";
      Class[] argTypes = { WTObject.class, String.class };
      Object[] argValues = { obj, ECNState };
      try
      {
        RemoteMethodServer.getDefault().invoke(method, CLASSNAME, null, 
          argTypes, argValues);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return;
    }
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      if ((obj instanceof ChangeOrderIfc)) {
        ChangeOrderIfc ecn = (ChangeOrderIfc)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, 
          true);
        while (qr.hasMoreElements())
          try {
            Persistable persistable = (Persistable)qr.nextElement();

            if ((persistable instanceof WTPart)) {
              String stname = ((WTPart)persistable)
                .getLifeCycleState().toString();
              String sttemp = ECNState;
              if (stname.lastIndexOf("_M") > 0) {
                sttemp = ECNState + "_M";
              }
              if (stname.lastIndexOf("_P") > 0) {
                sttemp = ECNState + "_P";
              }
              State stateDist = State.toState(sttemp);
              LifeCycleHelper.service.setLifeCycleState(
                (LifeCycleManaged)persistable, stateDist);
            } else if ((persistable instanceof LifeCycleManaged)) {
              State stateDist = State.toState(ECNState);
              LifeCycleHelper.service.setLifeCycleState(
                (LifeCycleManaged)persistable, stateDist);
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access);
    }
  }

  public static void CheckECRChangeables(ChangeRequestIfc changeRequestIfc)
    throws WTException
  {
    try
    {
      QueryResult qr = ChangeHelper2.service
        .getChangeables(changeRequestIfc);
      if (qr.size() == 0)
      {
        throw new WTException("没有添加受影响数据，不能提交签审!");
      }

      while (qr.hasMoreElements()) {
        Persistable persistable = (Persistable)qr.nextElement();
        if (WorkInProgressHelper.isCheckedOut((Workable)persistable)) {
          throw new WTException("对象‘" + persistable.getIdentity() + 
            "’处于检出状态，只有检入后才能申请变更!");
        }
        String currentState = "";
        if ((persistable instanceof LifeCycleManaged)) {
          currentState = ((LifeCycleManaged)persistable)
            .getLifeCycleState().toString();

          if (!currentState.equalsIgnoreCase("APPROVED"))
          {
            throw new WTException("对象‘" + persistable.getIdentity() + 
              "’只有处于已批准状态才能申请变更!");
          }
        }
      }
    } catch (WTException e) {
      e.printStackTrace();
    }
  }

  public static boolean verifyIfECNCreate(ChangeRequest2 changeRequest)
  {
    try
    {
      QueryResult qr = ChangeHelper2.service.getChangeOrders(changeRequest);
      if (qr.hasMoreElements())
        return true;
    }
    catch (ChangeException2 e)
    {
      e.printStackTrace();
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    return false;
  }

  public static void CheckECNBeforeData(WTObject obj)
  {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 chg = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore(
          chg, true);
        if (qr.size() == 0)
        {
          throw new WTException("没有添加改前数据，不能提交签审!");
        }
        while (qr.hasMoreElements()) {
          Persistable persistable = (Persistable)qr.nextElement();

          if (WorkInProgressHelper.isCheckedOut((Workable)persistable)) {
            throw new WTException("改前数据‘" + 
              persistable.getIdentity() + 
              "’处于检出状态，只有检入后才能提交!");
          }
          String currentState = "";
          if ((persistable instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)persistable)
              .getLifeCycleState().toString();

            if ((!currentState.equalsIgnoreCase("APPROVED")) && 
              (!currentState.equalsIgnoreCase("APPROVED_M")))
            {
              throw new WTException("改前数据‘" + 
                persistable.getIdentity() + "’只有处于已批准状态才能提交!");
            }
          }
        }
      }
    } catch (Exception e) { e.printStackTrace(); }
  }

  public static void ConfirmECNAfterData(WTObject obj)
  {
    try
    {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 chg = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(chg, true);

        while (qr.hasMoreElements()) {
          Persistable persistable = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)persistable)) {
            throw new WTException("改后数据‘" + persistable.getIdentity() + 
              "’处于检出状态，只有检入后才能提交!");
          }
          String currentState = "";
          if ((persistable instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)persistable)
              .getLifeCycleState().toString();

            if ((!currentState.equalsIgnoreCase("INWORK")) && (!currentState.equalsIgnoreCase("INWORK_M")) && (!currentState.equalsIgnoreCase("REWORKS")) && (!currentState.equalsIgnoreCase("REWORKS_M")))
            {
              throw new WTException("改后数据‘" + persistable.getIdentity() + 
                "’只有处于正在工作状态才能提交!");
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void CheckECNAfterData(WTObject obj)
  {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 chg = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(chg);
        if (qr.size() == 0)
        {
          throw new WTException("没有添加改后数据，不能提交签审!");
        }
        while (qr.hasMoreElements()) {
          Persistable persistable = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)persistable)) {
            throw new WTException("改后数据‘" + persistable.getIdentity() + 
              "’处于检出状态，只有检入后才能提交!");
          }
          String currentState = "";
          if ((persistable instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)persistable)
              .getLifeCycleState().toString();

            if ((!currentState.equalsIgnoreCase("INWORK")) && (!currentState.equalsIgnoreCase("INWORK_M")) && (!currentState.equalsIgnoreCase("REWORKS")) && (!currentState.equalsIgnoreCase("REWORKS_M")))
            {
              throw new WTException("改后数据‘" + persistable.getIdentity() + 
                "’只有处于正在工作状态才能提交!");
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void removeBeforeSJDoc(WTObject obj)
  {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 chg = (WTChangeOrder2)obj;
        QueryResult qrs = ChangeHelper2.service.getChangeActivities(chg, true);
        QueryResult qr;
        for (; qrs.hasMoreElements(); 
          qr.hasMoreElements())
        {
          ChangeActivity2 ca = (ChangeActivity2)qrs.nextElement();

          qr = ChangeHelper2.service.getChangeablesBefore(ca, true);
          continue;
          Persistable persistable = (Persistable)qr.nextElement();

          if ((persistable instanceof WTDocument)) {
            WTDocument doc = (WTDocument)persistable;
            TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);

            String docType = type.getTypename();
            String docSubType = "";
            IBAUtility ibaUtil = new IBAUtility(doc);

            if (docType.indexOf(".") >= 0) {
              docType = docType.substring(docType.lastIndexOf(".") + 1, 
                docType.length());
            }

            if (docType.equals("SJWJ")) {
              String Docsubtype = ibaUtil.getIBAValue("Docsubtype").trim();
              Class link_class = ChangeRecord2.class;
              ChangeHelper2.service.unattachChangeable(doc, ca, link_class, WTIntrospector.getLinkInfo(link_class).getRole("theChangeable2").getOtherRole().getName());
            } else if (docType.equals("GYWJ")) {
              Class link_class = ChangeRecord2.class;
              ChangeHelper2.service.unattachChangeable(doc, ca, link_class, WTIntrospector.getLinkInfo(link_class).getRole("theChangeable2").getOtherRole().getName());
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void removeSJDoc(WTObject obj)
  {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 chg = (WTChangeOrder2)obj;
        QueryResult qrs = ChangeHelper2.service.getChangeActivities(chg, true);
        QueryResult qr;
        for (; qrs.hasMoreElements(); 
          qr.hasMoreElements())
        {
          ChangeActivity2 ca = (ChangeActivity2)qrs.nextElement();

          qr = ChangeHelper2.service.getChangeablesAfter(ca, true);
          continue;
          Persistable persistable = (Persistable)qr.nextElement();

          if ((persistable instanceof WTDocument)) {
            WTDocument doc = (WTDocument)persistable;
            TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);

            String docType = type.getTypename();
            String docSubType = "";
            IBAUtility ibaUtil = new IBAUtility(doc);

            if (docType.indexOf(".") >= 0) {
              docType = docType.substring(docType.lastIndexOf(".") + 1, 
                docType.length());
            }

            if (docType.equals("SJWJ")) {
              Class link_class = ChangeRecord2.class;
              ChangeHelper2.service.unattachChangeable(doc, ca, link_class, WTIntrospector.getLinkInfo(link_class).getRole("theChangeable2").getOtherRole().getName());
            }
            else if (docType.equals("GYWJ")) {
              Class link_class = ChangeRecord2.class;
              ChangeHelper2.service.unattachChangeable(doc, ca, link_class, WTIntrospector.getLinkInfo(link_class).getRole("theChangeable2").getOtherRole().getName());
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void setECRState(WTObject pbo)
  {
    try {
      if ((pbo instanceof WTChangeOrder2)) {
        WTChangeOrder2 wto = (WTChangeOrder2)pbo;
        QueryResult qr = ChangeHelper2.service.getChangeRequest(wto, true);
        while (qr.hasMoreElements()) {
          WTObject obj = (WTObject)qr.nextElement();
          if ((obj instanceof ChangeRequest2)) {
            boolean flag = true;
            ChangeRequest2 cr = (ChangeRequest2)obj;
            QueryResult qr2 = ChangeHelper2.service.getChangeOrders(cr, true);
            while (qr2.hasMoreElements()) {
              WTObject wtobj = (WTObject)qr2.nextElement();
              if ((wtobj instanceof WTChangeOrder2)) {
                WTChangeOrder2 wto2 = (WTChangeOrder2)wtobj;
                String coState = wto2.getLifeCycleState().toString();
                if (!coState.equals("RESOLVED")) {
                  flag = false;
                }
              }
            }
            if (flag)
            {
              State stateDist = State.toState("RESOLVED");
              LifeCycleHelper.service.setLifeCycleState(
                cr, stateDist);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setZJDECNStatus(WTObject pbo) throws MaturityException, WTException { if ((pbo instanceof ChangeOrderIfc)) {
      Vector vec = new Vector();
      Vector vec1 = new Vector();
      String cStatus = "";
      QueryResult qrBefore = ChangeHelper2.service.getChangeablesBefore((ChangeOrderIfc)pbo, true);
      while (qrBefore.hasMoreElements()) {
        WTObject objAfter = (WTObject)qrBefore.nextElement();
        if ((objAfter instanceof WTPart)) {
          vec = CollectionGuide.getAllObj((WTPart)objAfter, vec1);
          if ((vec != null) && (vec.size() > 0))
            for (int i = 0; i < vec.size(); i++) {
              WTObject obj = (WTObject)vec.get(i);
              cStatus = ((LifeCycleManaged)obj).getLifeCycleState().toString();
              if (((obj instanceof EPMDocument)) || ((obj instanceof WTPart)))
                if ("RELEASED_S".equals(cStatus))
                  LifeCycleHelper.service.setLifeCycleState(
                    (LifeCycleManaged)obj, 
                    State.toState("Temporary_D"));
                else if ("Temporary_D".equals(cStatus))
                  LifeCycleHelper.service.setLifeCycleState(
                    (LifeCycleManaged)obj, 
                    State.toState("RELEASED_S"));
            }
        }
      }
    }
  }

  public static void setZJDECNStatus(Vector vec)
    throws MaturityException, WTException, WTPropertyVetoException
  {
    String cStatus = "";
    for (int i = 0; i < vec.size(); i++) {
      WTObject obj = (WTObject)vec.get(i);
      cStatus = ((LifeCycleManaged)obj).getLifeCycleState().toString();
      System.out.println("zyj--test-cstatus:" + cStatus);
      if ((((obj instanceof EPMDocument)) || ((obj instanceof WTPart))) && 
        ("RELEASED_S".equals(cStatus))) {
        System.out.println("zyj--test-111111");
        LifeCycleManaged lcm = (LifeCycleManaged)obj;
        lcm = LifeCycleHelper.service.setLifeCycleState(lcm, State.toState("Temporary_D"));
        cStatus = lcm.getLifeCycleState().toString();
        System.out.println(cStatus + "=======111111zyj=====");

        if ((lcm instanceof WTPart)) {
          System.out.println("zyj--test--status==:" + lcm.getLifeCycleState().toString());
          lcm = (WTPart)VersionControlHelper.service.newVersion((WTPart)lcm);
          lcm = (WTPart)PersistenceHelper.manager.save(lcm);
          setZJDState((WTObject)lcm, "RELEASED_D");
        }
        if ((lcm instanceof EPMDocument)) {
          System.out.println("zyj--test--status==:" + lcm.getLifeCycleState().toString());
          lcm = (EPMDocument)VersionControlHelper.service.newVersion((EPMDocument)lcm);
          lcm = (EPMDocument)PersistenceHelper.manager.save(lcm);
          setZJDState((WTObject)lcm, "RELEASED_D");
        }
      }
    }
  }

  public static void setStatus(Vector vec)
    throws MaturityException, WTException, WTPropertyVetoException
  {
    for (int i = 0; i < vec.size(); i++) {
      WTObject obj = (WTObject)vec.get(i);
      LifeCycleManaged lcm = (LifeCycleManaged)obj;
      lcm = LifeCycleHelper.service.setLifeCycleState(lcm, State.toState("RELEASED_S"));
      System.out.println("zyj--ts--:" + lcm.getLifeCycleState().toString());
    }
  }

  public static void createNewVersion(Vector versionVector) {
    try {
      Iterator it = versionVector.iterator();
      while (it.hasNext()) {
        Persistable pobj = (Persistable)it.next();

        if ((pobj instanceof WTPart)) {
          System.out.println("zyj--test--status:" + ((LifeCycleManaged)pobj).getLifeCycleState().toString());
          pobj = (WTPart)VersionControlHelper.service.newVersion((WTPart)pobj);
          pobj = (WTPart)PersistenceHelper.manager.save(pobj);
          setZJDState((WTObject)pobj, "RELEASED_D");
        }
        if ((pobj instanceof WTDocument)) {
          pobj = (WTDocument)VersionControlHelper.service.newVersion((WTDocument)pobj);
          pobj = (WTDocument)PersistenceHelper.manager.save(pobj);
          setZJDState((WTObject)pobj, "APPROVED");
        }
        if ((pobj instanceof EPMDocument)) {
          System.out.println("zyj--test--status:" + ((LifeCycleManaged)pobj).getLifeCycleState().toString());
          pobj = (EPMDocument)VersionControlHelper.service.newVersion((EPMDocument)pobj);
          pobj = (EPMDocument)PersistenceHelper.manager.save(pobj);
          setZJDState((WTObject)pobj, "RELEASED_D");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setZJDState(WTObject obj, String state) {
    try {
      if (!(obj instanceof LifeCycleManaged))
        return;
      LifeCycleManaged lcm = (LifeCycleManaged)obj;
      LifeCycleHelper.service.setLifeCycleState(lcm, 
        State.toState(state));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void reviewsPartAndEPM(WTObject pbo) throws Exception
  {
    if ((pbo instanceof ChangeOrderIfc)) {
      Vector vec = new Vector();
      Vector vec1 = new Vector();
      Vector vec2 = new Vector();
      Vector vec3 = new Vector();
      WTChangeOrder2 eco = null;
      if ((pbo instanceof WTChangeOrder2)) {
        eco = (WTChangeOrder2)pbo;
      }
      WTChangeActivity2 eca = null;
      QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(eco);
      while (qrActivities.hasMoreElements()) {
        Object objActivities = qrActivities.nextElement();
        if ((objActivities instanceof WTChangeActivity2)) {
          eca = (WTChangeActivity2)objActivities;
        }
      }

      ECModifier modify = new ECModifier();
      QueryResult qrBefore = ChangeHelper2.service.getChangeablesBefore((ChangeOrderIfc)pbo, true);
      while (qrBefore.hasMoreElements()) {
        WTObject objAfter = (WTObject)qrBefore.nextElement();
        if ((objAfter instanceof WTPart)) {
          vec = CollectionGuide.getAllObj((WTPart)objAfter, vec1);
          System.out.println("zyj--test-修订前");
          setZJDECNStatus(vec);

          rebuildLink(vec);
          System.out.println("zyj--test-修订后");
          setStatus(vec);
          vec2 = getDocVector(vec);
          SignPDF_lg.signPBO_zjd(vec2);
          vec3 = getVector(vec);
          System.out.println("zyj--test--vec3.size:" + vec3.size());
          modify.addECAResultItem(eca, vec3);
        }
      }
    }
  }

  public static boolean rebuildLink(Vector dv) {
    boolean flag = false;
    try
    {
      Iterator it = dv.iterator();
      while (it.hasNext()) {
        WTObject obj = (WTObject)it.next();
        if ((obj instanceof WTPart)) {
          WTPart dpart = (WTPart)obj;
          WTPart newdpart = getDesignPartItem(dpart);
          Vector cadV = new Vector();
          PartUtil.getOneCADVector(dpart, cadV);
          Iterator cadit = cadV.iterator();
          while (cadit.hasNext()) {
            WTObject objs = (WTObject)cadit.next();
            if ((objs instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)objs;
              EPMDocument newepm = getLatestEpmItem(epm);

              System.out.println("zyj--test--newepm version:" + newepm.getVersionIdentifier().getValue() + "." + newepm.getIterationIdentifier().getValue());
              System.out.println("zyj--test--newepm:" + newepm + "==newdpart:" + newdpart);
              System.out.println("zyj--test--oldepm:" + epm);

              removeEPMToPart(newdpart, epm);
            }
            else if ((objs instanceof WTDocument)) {
              WTDocument doc = (WTDocument)objs;
              WTDocument newdoc = getLatestDocItem(doc);

              removeDescDocToPart(newdpart, doc);

              WTPartDescribeLink linkObj = WTPartDescribeLink.newWTPartDescribeLink(newdpart, newdoc);
              PersistenceServerHelper.manager.insert(linkObj);
            }
          }
        }
      }
      flag = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return flag;
  }

  public static WTPart getDesignPartItem(WTPart part) {
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      Mastered master = part.getMaster();
      QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
      QueryResult qr;
      Mastered master;
      while (qr.hasMoreElements()) {
        WTPart partobj = (WTPart)qr.nextElement();
        if (partobj.getViewName().equals("Design")) {
          WTPart localWTPart1 = partobj; return localWTPart1;
        }
      }
    } catch (Exception e) { e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access);
    }
    return null;
  }

  public static EPMDocument getLatestEpmItem(EPMDocument epm) {
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      Mastered master = epm.getMaster();
      QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
      if (qr.hasMoreElements())
        return (EPMDocument)qr.nextElement();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access); } SessionServerHelper.manager.setAccessEnforced(access);

    return null;
  }

  public static WTPart getLatestPartItem(WTPart part) {
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      Mastered master = part.getMaster();
      QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
      if (qr.hasMoreElements())
        return (WTPart)qr.nextElement();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access); } SessionServerHelper.manager.setAccessEnforced(access);

    return null;
  }

  public static void removeEPMToPart(WTPart partRoleA, EPMDocument epmRoleB) {
    try {
      boolean isCheckOut1 = WorkInProgressHelper.isCheckedOut(partRoleA);
      if (!isCheckOut1) {
        Folder checkOutFolder = WorkInProgressHelper.service.getCheckoutFolder();
        try
        {
          System.out.println("zyj--test--正在检出part");
          CheckoutLink checkOutLink = WorkInProgressHelper.service.checkout(partRoleA, checkOutFolder, "");
          partRoleA = (WTPart)checkOutLink.getWorkingCopy();
        } catch (WTPropertyVetoException e) {
          e.printStackTrace();
        }
      }

      BinaryLink link = GenericUtilities.findLink(partRoleA, epmRoleB);
      BinaryLink link1 = GenericUtilities.findLink(epmRoleB, partRoleA);
      System.out.println("zyj--test--link:" + link);
      GenericUtilities.deleteLink(link);
      GenericUtilities.deleteLink(link1);
      EPMDocument newepm = getLatestEpmItem(epmRoleB);
      EPMDescribeLink linkObj = EPMDescribeLink.newEPMDescribeLink(partRoleA, newepm);
      PersistenceServerHelper.manager.insert(linkObj);
      if (WorkInProgressHelper.isCheckedOut(partRoleA))
      {
        partRoleA = (WTPart)WorkInProgressHelper.service.checkin(partRoleA, "");
        System.out.println("zyj-test--part已检入");
      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static WTDocument getLatestDocItem(WTDocument doc) {
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      Mastered master = doc.getMaster();
      QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
      if (qr.hasMoreElements())
        return (WTDocument)qr.nextElement();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access); } SessionServerHelper.manager.setAccessEnforced(access);

    return null;
  }

  public static void removeDescDocToPart(WTPart partRoleA, WTDocument docRoleB)
  {
    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
    Transaction tx = new Transaction();
    try {
      tx.start();

      QueryResult queryresult = PersistenceHelper.manager.find(WTPartDescribeLink.class, partRoleA, 
        "describes", docRoleB);
      if ((queryresult == null) || (queryresult.size() == 0))
        return;
      QueryResult queryresult;
      WTPartDescribeLink link = (WTPartDescribeLink)queryresult.nextElement();
      PersistenceServerHelper.manager.remove(link);

      tx.commit();
      tx = null;
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (tx != null) {
        tx.rollback();
      }
      SessionServerHelper.manager.setAccessEnforced(enforce);
    }
  }

  public static Vector getDocVector(Vector fullvector) {
    Vector docvector = new Vector();
    Iterator it = fullvector.iterator();
    while (it.hasNext()) {
      WTObject obj = (WTObject)it.next();
      if ((obj instanceof EPMDocument)) {
        EPMDocument epm = (EPMDocument)obj;
        EPMDocument newepm = getLatestEpmItem(epm);
        docvector.add(newepm);
      } else if ((obj instanceof WTDocument)) {
        WTDocument doc = (WTDocument)obj;
        WTDocument newdoc = getLatestDocItem(doc);
        docvector.add(newdoc);
      }
    }
    return docvector;
  }

  public static Vector getVector(Vector fullvector) {
    Vector vector = new Vector();
    Iterator it = fullvector.iterator();
    while (it.hasNext()) {
      WTObject obj = (WTObject)it.next();
      if ((obj instanceof EPMDocument)) {
        EPMDocument epm = (EPMDocument)obj;
        EPMDocument newepm = getLatestEpmItem(epm);
        vector.add(newepm);
      } else if ((obj instanceof WTDocument)) {
        WTDocument doc = (WTDocument)obj;
        WTDocument newdoc = getLatestDocItem(doc);
        vector.add(newdoc);
      } else if ((obj instanceof WTPart)) {
        WTPart part = (WTPart)obj;
        WTPart newPart = getLatestPartItem(part);
        vector.add(newPart);
      }
    }
    return vector;
  }
}