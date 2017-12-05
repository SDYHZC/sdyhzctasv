package ext.tzc.tasv.change.workflow;

import com.ptc.wvs.common.ui.PublishResult;
import com.ptc.wvs.server.publish.Publish;
import com.ptc.wvs.server.util.PublishUtils;
import ext.tzc.tasv.change.util.changeUtil;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.filter.NavigationCriteria;
import wt.method.RemoteMethodServer;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.project.Role;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.TeamService;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.vc.wip.WorkInProgressHelper;
import wt.viewmarkup.DerivedImage;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;
import wt.workflow.work.WfAssignedActivity;

public class ChangeWorkFlowUtil
{
  public static void modifyAttachMent(WTObject pbo)
    throws ChangeException2, WTException
  {
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)object;
          QueryResult ablesAfterQr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ablesAfterQr.hasMoreElements()) {
            Object object2 = ablesAfterQr.nextElement();
            if ((object2 instanceof WTDocument)) {
              WTDocument doc = (WTDocument)object2;
              String type = changeUtil.getTypeByObject(doc);

              changeUtil.modifySECONDARYNameByObject(doc);
            }
          }
        }
      }
    }
  }

  public static void checkWorkFlowRoles(ObjectReference self)
    throws WTRuntimeException, WTException
  {
    WfProcess process = null;
    if ((self.getObject() instanceof WfProcess))
      process = (WfProcess)self.getObject();
    else {
      process = ((WfAssignedActivity)self.getObject()).getParentProcess();
    }
    TeamManaged object = process;
    Team team = TeamHelper.service.getTeam(object);
    HashMap map = TeamHelper.service.findAllParticipantsByRole(team);
    List<WTPrincipalReference> list = (List)map.get(Role.toRole("RECIPIENT"));
    for (WTPrincipalReference wtp : list) {
      WTPrincipal principal = wtp.getPrincipal();
      if ((principal instanceof WTGroup)) {
        WTGroup group = (WTGroup)principal;
        String groupName = group.getName();
        if (!groupName.endsWith("_接收组"))
          throw new WTException("收件人角色中添加的组不是接收组，请确认！");
      }
    }
  }

  public static void checkEPMAttachment(WTObject pbo)
    throws ChangeException2, WTException
  {
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          checkEPMAttachmentInECA((WTChangeActivity2) object);
        }
      }
    }else if(pbo instanceof WTChangeActivity2){
    	checkEPMAttachmentInECA((WTChangeActivity2) pbo);
    }
  }

/**
 * 检查ECA中对象的EPM附件是否符合标准
 * @author caizg
 * @param object
 * @throws WTException
 * @throws ChangeException2
 */
public static void checkEPMAttachmentInECA(WTChangeActivity2 object) throws WTException,
		ChangeException2 {
	WTChangeActivity2 eca = (WTChangeActivity2)object;
	QueryResult ablesAfterQr = ChangeHelper2.service.getChangeablesAfter(eca);
	while (ablesAfterQr.hasMoreElements()) {
		Object object2 = ablesAfterQr.nextElement();
		if ((object2 instanceof EPMDocument)) {
			EPMDocument epm = (EPMDocument)object2;
			String cadName = epm.getCADName();
			if (cadName.endsWith(".dwg")) {
				QueryResult attachqr = ContentHelper.service.getContentsByRole(epm, ContentRoleType.SECONDARY);
				if (attachqr.size() != 1) {
					if (attachqr.size() == 0) {
						throw new WTException(epm.getNumber() + "图档没有附件,请确认！");
					}
					throw new WTException(epm.getNumber() + "图档的附件数量不正确,请确认！");
				}
				ApplicationData applicationdata = (ApplicationData)attachqr.nextElement();
				String fileName = applicationdata.getFileName();
				if (!fileName.endsWith(".pdf")) {
					throw new WTException(epm.getNumber() + "图档的附件格式不正确,请确认！");
				}
				long attachmentMTime = applicationdata.getModifyTimestamp().getTime();
				QueryResult primaryqr = ContentHelper.service.getContentsByRole(epm, ContentRoleType.PRIMARY);
				if (primaryqr.size() > 0) {
					ApplicationData primarydata = (ApplicationData)primaryqr.nextElement();
					if (!primarydata.getFileName().endsWith(".dwg")) {
						long primaryMTime = primarydata.getModifyTimestamp().getTime();
						if (primaryMTime > attachmentMTime)
							throw new WTException(epm.getNumber() + "图档的附件没有更新,请确认！");
					}
					else {
						throw new WTException(epm.getNumber() + "图档的主内容格式不正确,请确认！");
					}
				} else {
					throw new WTException(epm.getNumber() + "图档没有主内容,请确认！");
				}
			}
		}
	}
}

  public static void checkPublishVersion(WTObject pbo)
    throws ChangeException2, WTException, RemoteException, InvocationTargetException
  {
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)object;
          QueryResult ablesAfterQr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ablesAfterQr.hasMoreElements()) {
            Object object2 = ablesAfterQr.nextElement();
            if ((object2 instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)object2;
              String cadName = epm.getCADName();
              if (cadName.endsWith(".dwg")) {
                QueryResult localQueryResult = PublishUtils.getRepresentations(epm);
                while (localQueryResult.hasMoreElements()) {
                  DerivedImage image = (DerivedImage)localQueryResult.nextElement();
                  EPMDocument cad = (EPMDocument)image.getDerivedFromReference().getObject();
                  if (!epm.equals(cad))
                    updateRepresentation(epm);
                }
              }
            }
          }
        }
      }
    }
  }

  public static void updateRepresentation(EPMDocument epm)
    throws WTException, RemoteException, InvocationTargetException
  {
    QueryResult localQueryResult = PublishUtils.getRepresentations(epm);
    while (localQueryResult.hasMoreElements()) {
      Representation defalutRep = (Representation)localQueryResult.nextElement();
      RepresentationHelper.service.deleteRepresentation(defalutRep, true);
      epm = (EPMDocument)PersistenceHelper.manager.refresh(epm);
      doCusPublish(epm, true, "", "");
    }
  }

  public static boolean doCusPublish(Persistable publishedObj, boolean isDefault, String repName, String repDescription)
    throws WTException, RemoteException, InvocationTargetException
  {
    if (!RemoteMethodServer.ServerFlag) {
      return ((Boolean)RemoteMethodServer.getDefault().invoke("doCusPublish", 
    	ChangeWorkFlowUtil.class.getName(), null, 
        new Class[] { Persistable.class, Boolean.TYPE, String.class, String.class }, 
        new Object[] { publishedObj, Boolean.valueOf(isDefault), repName, repDescription })).booleanValue();
    }

    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      if (publishedObj == null) {
        throw new WTException(" Input publishedObj is null...");
      }
      boolean isSuccess = false;
      String msg = "";

      ReferenceFactory rf = new ReferenceFactory();
      String objRef = rf.getReferenceString(publishedObj);

      NavigationCriteria navigationCriteria = PublishUtils.getNavigationCriteriaFor(
        PublishUtils.getObjectFromRef(objRef), null);

      PublishResult result = Publish.doPublish(false, true, objRef, null, 
        navigationCriteria, isDefault, repName, repDescription, 1, null, 
        1, null);

      if (result.isMarkedNotPublishable())
      {
        isSuccess = false;
      } else if (!result.isSuccessful())
      {
        isSuccess = false;
      } else {
        msg = "Job submitted to publisher to create new representation " + repName + ".";
        isSuccess = true;
      }
      boolean bool1 = isSuccess;
      return bool1;
    } finally {
      SessionServerHelper.manager.setAccessEnforced(enforce);
    }
  }

  public static void isCheckedOut(WTObject pbo)
    throws ChangeException2, WTException
  {	
	//增加PBO是ECA的情况
	  if(pbo instanceof WTChangeActivity2){
		  WTChangeActivity2 eca = (WTChangeActivity2) pbo;
		  QueryResult qr = ChangeHelper2.service.getChangeOrder(eca);
		  if(qr.hasMoreElements()){
			  WTChangeOrder2 o = (WTChangeOrder2) qr.nextElement();
			  pbo = o;
		  }
	  }
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)object;
          QueryResult ecaqr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ecaqr.hasMoreElements()) {
            Object obj = ecaqr.nextElement();
            if ((obj instanceof WTPart)) {
              WTPart part = (WTPart)obj;
              if (WorkInProgressHelper.isCheckedOut(part)) {
                throw new WTException(part.getNumber() + "物料已检出，请确认！");
              }
            }
            if ((obj instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)obj;
              if (WorkInProgressHelper.isCheckedOut(epm)) {
                throw new WTException(epm.getNumber() + "图档已检出，请确认！");
              }
            }
            if ((obj instanceof WTDocument)) {
              WTDocument doc = (WTDocument)obj;
              if (WorkInProgressHelper.isCheckedOut(doc))
                throw new WTException(doc.getNumber() + "文档已检出，请确认！");
            }
          }
        }
      }
    }
    else if ((pbo instanceof WTDocument)) {
      WTDocument doc = (WTDocument)pbo;
      if (WorkInProgressHelper.isCheckedOut(doc))
        throw new WTException(doc.getNumber() + "文档已检出，请确认！");
    }
  }

  public static void checkIsInWfProcess(WTObject pbo)
    throws WTRuntimeException, WTException
  {
    WTChangeActivity2 eca;
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          eca = (WTChangeActivity2)object;
          QueryResult ecaqr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ecaqr.hasMoreElements()) {
            Changeable2 changeable = (Changeable2)ecaqr.nextElement();
            Set<WTChangeOrder2> set = getChangeNoticeByChangeableAfter(changeable, ecn);
            for (WTChangeOrder2 eco : set) {
              WfProcess process = getRelatedProcess(eco);
              if (process != null) {
                String state = process.getState().getFullDisplay();
                if ("正在运行".equals(state)) {
                  if ((changeable instanceof WTDocument)) {
                    WTDocument doc = (WTDocument)changeable;
                    throw new WTException(doc.getNumber() + "文档在" + eco.getNumber() + "更改通告进程中，请联系管理员确认！");
                  }
                  if ((changeable instanceof WTPart)) {
                    WTPart part = (WTPart)changeable;
                    throw new WTException(part.getNumber() + "物料在" + eco.getNumber() + "更改通告进程中，请联系管理员确认！");
                  }
                  if ((changeable instanceof EPMDocument)) {
                    EPMDocument epm = (EPMDocument)changeable;
                    throw new WTException(epm.getNumber() + "图档在" + eco.getNumber() + "更改通告进程中，请联系管理员确认！");
                  }
                }
              }
            }
            if ((changeable instanceof WTDocument)) {
              WTDocument doc = (WTDocument)changeable;
              WfProcess process = getRelatedProcess(doc);
              if (process != null) {
                String state = process.getState().getFullDisplay();
                if ("正在运行".equals(state)) {
                  throw new WTException(doc.getNumber() + "文档在" + doc.getNumber() + "文档进程中，请联系管理员确认！");
                }
              }
            }
          }
        }
      }
    }
    else if ((pbo instanceof WTDocument)) {
      WTDocument doc = (WTDocument)pbo;
      Set<WTChangeOrder2> set = getChangeNoticeByChangeableAfter(doc, null);
      String state;
      for (WTChangeOrder2 eco : set) {
        WfProcess process = getRelatedProcess(eco);
        if (process != null) {
          state = process.getState().getFullDisplay();
          if ("正在运行".equals(state)) {
            throw new WTException(doc.getNumber() + "文档在" + eco.getNumber() + "更改通告进程中，请联系管理员确认！");
          }
        }
      }
      WfProcess process = getRelatedProcess(doc);
      if (process != null) {
        Set<WfProcess> processSet = getProcessArray(doc, process);
        for (WfProcess wfProcess : processSet) {
          state = wfProcess.getState().getFullDisplay();
          if ("正在运行".equals(state))
            throw new WTException(doc.getNumber() + "文档在" + doc.getNumber() + "文档进程中，请联系管理员确认！");
        }
      }
    }
  }

  public static void checkIsPublish(WTObject pbo)
    throws ChangeException2, WTException, PropertyVetoException
  {
	  //增加PBO是ECA的情况
	  if(pbo instanceof WTChangeActivity2){
		  WTChangeActivity2 eca = (WTChangeActivity2) pbo;
		  QueryResult qr = ChangeHelper2.service.getChangeOrder(eca);
		  if(qr.hasMoreElements()){
			  WTChangeOrder2 o = (WTChangeOrder2) qr.nextElement();
			  pbo = o;
		  }
	  }
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)object;
          QueryResult ecaqr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ecaqr.hasMoreElements()) {
            Object obj = ecaqr.nextElement();
            if ((obj instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)obj;
              String cadName = epm.getCADName();
              if (cadName.endsWith(".drw")) {
                boolean flag = downloadpdf2(epm).booleanValue();
                if (!flag) {
                  throw new WTException(epm.getNumber() + "图档没有发布可视化，请确认！");
                }
              }
            }
            if ((obj instanceof WTDocument)) {
              WTDocument doc = (WTDocument)obj;
              boolean flag = downloadpdf2(doc).booleanValue();
              if (!flag)
                throw new WTException(doc.getNumber() + "文档没有发布可视化，请确认！");
            }
          }
        }
      }
    }
    else if ((pbo instanceof WTDocument)) {
      WTDocument doc = (WTDocument)pbo;
      boolean flag = downloadpdf2(doc).booleanValue();
      if (!flag)
        throw new WTException(doc.getNumber() + "文档没有发布可视化，请确认！");
    }
  }

  public static WfProcess getRelatedProcess(WTObject pbo) throws WTException
  {
    WfProcess ret = null;
    QueryResult qrProcs = WfEngineHelper.service.getAssociatedProcesses(pbo, null, null);
    if ((qrProcs.hasMoreElements()) && 
      (qrProcs.hasMoreElements())) {
      WfProcess proc = (WfProcess)qrProcs.nextElement();
      ret = proc;
    }

    return ret;
  }
  public static Set<WfProcess> getProcessArray(WTObject pbo, WfProcess process) throws WTException {
    Set processSet = new HashSet();
    QueryResult qrProcs = WfEngineHelper.service.getAssociatedProcesses(pbo, null, null);
    if (qrProcs.hasMoreElements()) {
      while (qrProcs.hasMoreElements()) {
        WfProcess proc = (WfProcess)qrProcs.nextElement();
        if (!process.equals(proc)) {
          processSet.add(proc);
        }
      }
    }
    return processSet;
  }

  public static Set<WTChangeOrder2> getChangeNoticeByChangeableAfter(Changeable2 changeable, WTChangeOrder2 ecn) throws ChangeException2, WTException
  {
    Set results = new HashSet();
    try {
      if (!RemoteMethodServer.ServerFlag) {
        return (Set)RemoteMethodServer.getDefault().invoke(
          "getChangeNoticeByChangeableAfter", ChangeWorkFlowUtil.class.getName(), null, 
          new Class[] { Changeable2.class }, new Object[] { changeable });
      }
      boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
      try {
        QueryResult qur = ChangeHelper2.service.getUniqueImplementedChangeOrders(changeable);
        while (qur.hasMoreElements()) {
          WTChangeOrder2 cho = (WTChangeOrder2)qur.nextElement();
          if (ecn != null) {
            if (!cho.equals(ecn))
              results.add(cho);
          }
          else
            results.add(cho);
        }
      }
      finally {
        SessionServerHelper.manager.setAccessEnforced(enforce);
      }
    }
    catch (RemoteException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return results;
  }

  public static Boolean downloadpdf2(Representable representable)
    throws WTException, PropertyVetoException
  {
    Representation representation = RepresentationHelper.service.getDefaultRepresentation(representable);
    if (representation != null) {
      ContentHolder ch = ContentHelper.service.getContents(representable);
      representation = (Representation)ContentHelper.service.getContents(representation);
      Vector vector1 = ContentHelper.getContentList(representation);
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        if ((contentitem instanceof ApplicationData)) {
          ApplicationData applicationdata = (ApplicationData)contentitem;
          InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
          String filename = applicationdata.getFileName();
          if (filename.endsWith(".pdf")) {
            return Boolean.valueOf(true);
          }
        }
      }
    }
    return Boolean.valueOf(false);
  }

  public static void main(String[] args)
  {
  }
}