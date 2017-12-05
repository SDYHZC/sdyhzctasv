package ext.workflow;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import wt.access.AdHocControlled;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipalReference;
import wt.project.Role;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.workflow.engine.WfProcess;

public class ECAUtil {
	
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
	        WfProcess wfp = null;
	        if (wfp != null) {
	          Team team = (Team)wfp.getTeamId().getObject();
	          Role myRole = Role.toRole(roleName);
	          Enumeration enumeration = team.getPrincipalTarget(myRole);
	          while (enumeration.hasMoreElements()) {
	            arraylist.add(enumeration.nextElement());
	          }
	          if ((pbo instanceof PromotionNotice)) {
	            PromotionNotice pn = (PromotionNotice)pbo;
	            QueryResult qr = MaturityHelper.service
	              .getPromotionTargets(pn);
	            int j=0;
	            for ( j = 0; qr.hasMoreElements()&&
	              (arraylist != null) && 
	              (j < arraylist.size());j++)
	            {
	              AdHocControlled obj = (AdHocControlled)qr.nextElement();
	              WTPrincipalReference wtprincipalreference = (WTPrincipalReference)arraylist.get(j);
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
}
