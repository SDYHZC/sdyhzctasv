package ext.workflow;

import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import wt.access.AccessControlHelper;
import wt.access.AccessControlManager;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceManagerSvr;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipalReference;
import wt.pom.Transaction;
import wt.project.Role;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamReference;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;

public class ObjectUtil
  implements RemoteAccess, Serializable
{
  private static final long serialVersionUID = -7197794643564245476L;

  public static WfProcess getProcess(ObjectReference processRef)
    throws WTException
  {
    WfProcess wfprocess = null;
    try {
      Persistable persistable = processRef.getObject();
      if ((persistable instanceof WfActivity)) {
        WfActivity mySelf = (WfActivity)persistable;
        wfprocess = mySelf.getParentProcess();
      } else if ((persistable instanceof WfProcess)) {
        wfprocess = (WfProcess)persistable;
      }
    }
    catch (WTRuntimeException e) {
      e.printStackTrace();
    }
    return wfprocess;
  }

  private static void setObjectAccess(AdHocControlled adhoccontrolled, WTPrincipalReference principalRef, boolean isadd, Vector vec_permission)
  {
    Transaction tx = new Transaction();
    try {
      tx.start();
      if (isadd) {
        System.out.println("zyj--test--isadd");
        adhoccontrolled = AccessControlHelper.manager.addPermissions(
          adhoccontrolled, principalRef, vec_permission, 
          AdHocAccessKey.WNC_ACCESS_CONTROL);
      } else {
        System.out.println("zyj--test--else");
        adhoccontrolled = AccessControlHelper.manager
          .removePermissions(adhoccontrolled, principalRef, 
          vec_permission, 
          AdHocAccessKey.WNC_ACCESS_CONTROL);
      }
      System.out.println("zyj--test--end");
      PersistenceServerHelper.manager.update(adhoccontrolled, false);
      tx.commit();
      tx = null;
      return;
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (tx != null)
        tx.rollback();
    }
  }

  public static void setObjectAccessByPBO(WTObject pbo, ObjectReference self, String roleName, boolean isadd, Vector vec_permission)
  {
    if (!RemoteMethodServer.ServerFlag) {
      System.out.println("zyj--test--!RemoteMethodServer.ServerFlag");
      try {
        RemoteMethodServer.getDefault().invoke(
          "setObjectAccessByPBO", 
          ObjectUtil.class.getName(), 
          null, 
          new Class[] { WTObject.class, ObjectReference.class, 
          String.class }, 
          new Object[] { pbo, self, roleName });
      }
      catch (RemoteException e) {
        e.printStackTrace();
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      System.out.println("zyj--test--RemoteMethodServer.ServerFlag");
      boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
      try {
        ArrayList arraylist = new ArrayList();
        WfProcess wfp = getProcess(self);
        if (wfp != null) {
          System.out.println("zyj--test--wfp is not null");
          Team team = (Team)wfp.getTeamId().getObject();
          Role myRole = Role.toRole(roleName);
          System.out.println("zyj--test--rolename:" + roleName);
          System.out.println("zyj--test--role:" + myRole);
          Enumeration enumeration = team.getPrincipalTarget(myRole);
          System.out.println("zyj--test--enumeration:" + enumeration.hasMoreElements());
          while (enumeration.hasMoreElements()) {
            System.out.println("zyj--test--arraylist.add:");
            arraylist.add(enumeration.nextElement());
          }
//          if(pbo instanceof WTChangeActivity2){
//        	  WTChangeActivity2 eca = (WTChangeActivity2) pbo;
//        	  pbo = eca;
//          }
          if ((pbo instanceof PromotionNotice)) {
            PromotionNotice pn = (PromotionNotice)pbo;
            QueryResult qr = MaturityHelper.service
              .getPromotionTargets(pn);
            while(qr.hasMoreElements()){
            	AdHocControlled obj = (AdHocControlled)qr
            			.nextElement();
            	for (int j = 0;
            			(arraylist != null) && 
            			(j < arraylist.size());j++)
            	{
            		WTPrincipalReference wtprincipalreference = 
            				(WTPrincipalReference)arraylist
            				.get(j);
            		setObjectAccess(pn, 
            				wtprincipalreference, isadd, vec_permission);
            	}
            }

          }
          else if ((pbo instanceof WTChangeOrder2)) {
            System.out.println("zyj--test--eco");
            WTChangeOrder2 co2 = (WTChangeOrder2)pbo;
            QueryResult qrs = ChangeHelper2.service
              .getChangeablesBefore(co2, true);
            while(qrs.hasMoreElements()){
            	AdHocControlled obj = (AdHocControlled)qrs
            			.nextElement();
            	for (int j = 0;
            			(arraylist != null) && 
            			(j < arraylist.size());j++)
            	{
            		System.out.println("zyj--test--qrs.hasMoreElements()");
            		System.out.println("zyj--test--arraylist:" + arraylist);
            		System.out.println("zyj--test--arraylist.size:" + arraylist.size());
            		
            		System.out.println("zyj--test--arraylist:" + arraylist.size());
            		WTPrincipalReference wtprincipalreference = 
            				(WTPrincipalReference)arraylist
            				.get(j);
            		setObjectAccess(obj, wtprincipalreference, 
            				isadd, vec_permission);
            	}
            }

          }
          else if ((pbo instanceof WTChangeRequest2)) {
            WTChangeRequest2 cr2 = (WTChangeRequest2)pbo;
            QueryResult qr = ChangeHelper2.service
              .getChangeables(cr2);
            while(qr.hasMoreElements()){
            	AdHocControlled obj = (AdHocControlled)qr
            			.nextElement();
            	for (int j=0; 
            			(arraylist != null) && 
            			(j < arraylist.size());j++)
            	{
            		WTPrincipalReference wtprincipalreference = 
            				(WTPrincipalReference)arraylist
            				.get(j);
            		setObjectAccess(obj, wtprincipalreference, 
            				isadd, vec_permission);
            	}
            }

          }

        }

      }
      catch (Exception e)
      {
        e.printStackTrace();
      } finally {
        SessionServerHelper.manager.setAccessEnforced(flag);
      }
    }
  }

  public static void setObjectReviewAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      setObjectAccessByPBO(pbo, self, roleName, true, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setObjectModifyAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      vector.add(AccessPermission.MODIFY);
      setObjectAccessByPBO(pbo, self, roleName, true, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setObjectReversionAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      System.out.println("zyj--test--xiuding--");
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      vector.add(AccessPermission.CREATE);
      vector.add(AccessPermission.REVISE);
      setObjectAccessByPBO(pbo, self, roleName, true, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void removeObjectReviewAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      setObjectAccessByPBO(pbo, self, roleName, false, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void removeObjectModifyAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      vector.add(AccessPermission.MODIFY);
      setObjectAccessByPBO(pbo, self, roleName, false, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void removeObjectReversionAccess(WTObject pbo, ObjectReference self, String roleName)
  {
    try
    {
      Vector vector = new Vector();
      vector.add(AccessPermission.READ);
      vector.add(AccessPermission.CREATE);
      vector.add(AccessPermission.REVISE);
      setObjectAccessByPBO(pbo, self, roleName, false, vector);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}