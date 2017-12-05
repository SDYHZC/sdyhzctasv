package ext.tasv;

import ext.print.IBAHelper;
import ext.util.Util;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrderIfc;
import wt.change2.ChangeService2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleService;
import wt.lifecycle.State;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.Promotable;
import wt.maturity.PromotionNotice;
import wt.maturity.PromotionTarget;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.PersistenceException;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.team.Team;
import wt.team.TeamReference;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.BaselineService;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressService;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;

public class WfUtil
{
  static String wthome;
  static String docreviewfile = "codebase/ext/sddl/docreview.properties";
private static WTChangeOrder2 wtc;
private static Object WTChangeOrder2;

  static {
    try { WTProperties wtproperties = WTProperties.getLocalProperties();
      wthome = wtproperties.getProperty("wt.home");
      docreviewfile = wthome + "/" + docreviewfile;
    } catch (Throwable throwable) {
      throwable.printStackTrace(System.err);
      throw new ExceptionInInitializerError(throwable);
    }
  }

  public static String getDocReviewRoute(String docType) {
    Properties prop = new Properties();
    try {
      FileInputStream fis = new FileInputStream(docreviewfile);
      prop.load(fis);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    String t1 = prop.getProperty(docType, "001000");
    return t1;
  }

  public static boolean getReviewRoute(WTObject pbo, int shenhe)
    throws WTException
  {
    WTDocument doc = null;
    if ((pbo instanceof WTDocument)) {
      doc = (WTDocument)pbo;
      String docType = IBAHelper.getSoftType(doc);
      docType = docType.substring(docType.lastIndexOf(".") + 1);

      String reviewString = getDocReviewRoute(docType);
      if (reviewString.charAt(shenhe) == '1') {
        return true;
      }
    }
    return false;
  }

  public static void setPNTargetsState(WTObject primaryBusinessObject, String state)
    throws MaturityException, WTException
  {
    PromotionNotice pn = (PromotionNotice)primaryBusinessObject;
    QueryResult qr = MaturityHelper.service
      .getPromotionTargets(pn);
    while (qr.hasMoreElements()) {
      WTObject obj = (WTObject)qr.nextElement();
      LifeCycleHelper.service.setLifeCycleState(
        (LifeCycleManaged)obj, 
        State.toState(state));
    }
  }

  public static void setPNTargetsRELEASEDState(WTObject primaryBusinessObject)
    throws MaturityException, WTException
  {
    PromotionNotice pn = (PromotionNotice)primaryBusinessObject;
    QueryResult qr = MaturityHelper.service
      .getPromotionTargets(pn);
    while (qr.hasMoreElements()) {
      WTObject obj = (WTObject)qr.nextElement();
      if ((obj instanceof WTDocument)) {
        LifeCycleHelper.service.setLifeCycleState(
          (LifeCycleManaged)obj, 
          State.toState("APPROVED"));
      } else {
        System.out.println("zyj--test--epmdoc");
        String version = ((RevisionControlled)obj).getVersionIdentifier().getValue();
        System.out.println("zyj--test--version:" + version);
        System.out.println("zyj--test--charAt:" + version.charAt(0));
        System.out.println("S".equals(version.charAt(0)));
        String ver = version.charAt(0)+"";
        if ("S".equals(ver)) {
          System.out.println("zyj--test--S");
          LifeCycleHelper.service.setLifeCycleState(
            (LifeCycleManaged)obj, 
            State.toState("RELEASED_S"));
        } else if ("D".equals(ver)) {
          System.out.println("zyj--test--D");
          LifeCycleHelper.service.setLifeCycleState(
            (LifeCycleManaged)obj, 
            State.toState("RELEASED_D"));
        }
      }
    }
  }

  public static void setECNReleasedState(WTObject primaryBusinessObject)
    throws MaturityException, WTException
  {
    if ((primaryBusinessObject != null) && ((primaryBusinessObject instanceof WTChangeOrder2)))
    {
    	WTChangeOrder2 wtc = (WTChangeOrder2)primaryBusinessObject;
      QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(wtc);
      while (qrActivities.hasMoreElements()) {
        Object objActivities = qrActivities.nextElement();

        if ((objActivities instanceof WTChangeActivity2)) {
        	if(((WTChangeActivity2) objActivities).getName().contains(wtc.getNumber())) continue;
          QueryResult qrAfter = ChangeHelper2.service
            .getChangeablesAfter((WTChangeActivity2)objActivities);

          while (qrAfter.hasMoreElements())
          {
            WTObject objAfter = (WTObject)qrAfter.nextElement();

            if ((objAfter instanceof WTDocument)) {
              LifeCycleHelper.service.setLifeCycleState(
                (LifeCycleManaged)objAfter, 
                State.toState("APPROVED"));
            } else {
              System.out.println("zyj--test--epmdoc");
              String version = ((RevisionControlled)objAfter).getVersionIdentifier().getValue();
              System.out.println("zyj--test--version:" + version);
              String ver = version.charAt(0)+"";
              if ("S".equals(ver)) {
                System.out.println("zyj--test--S");
                LifeCycleHelper.service.setLifeCycleState(
                  (LifeCycleManaged)objAfter, 
                  State.toState("RELEASED_S"));
              } else if ("D".equals(ver)) {
                System.out.println("zyj--test--D");
                LifeCycleHelper.service.setLifeCycleState(
                  (LifeCycleManaged)objAfter, 
                  State.toState("RELEASED_D"));
              }
            }
          }
        }
      }
    }else if(primaryBusinessObject instanceof WTChangeActivity2){
    	WTChangeActivity2 eca = (WTChangeActivity2) primaryBusinessObject;
    	QueryResult qrAfter = ChangeHelper2.service.getChangeablesAfter((WTChangeActivity2)eca);
    	while (qrAfter.hasMoreElements()){
    		WTObject objAfter = (WTObject)qrAfter.nextElement();
    		if ((objAfter instanceof WTDocument)) {
    			LifeCycleHelper.service.setLifeCycleState(
    					(LifeCycleManaged)objAfter, 
    					State.toState("APPROVED"));
    		} else {
    			System.out.println("zyj--test--epmdoc");
    			String version = ((RevisionControlled)objAfter).getVersionIdentifier().getValue();
    			System.out.println("zyj--test--version:" + version);
    			String ver = version.charAt(0)+"";
    			if ("S".equals(ver)) {
    				System.out.println("zyj--test--S");
    				LifeCycleHelper.service.setLifeCycleState(
    						(LifeCycleManaged)objAfter, 
    						State.toState("RELEASED_S"));
    			} else if ("D".equals(ver)) {
    				System.out.println("zyj--test--D");
    				LifeCycleHelper.service.setLifeCycleState(
    						(LifeCycleManaged)objAfter, 
    						State.toState("RELEASED_D"));
    			}
    		}
    	}
    }
  }

  public static void setZCECNStatus(WTObject pbo) throws MaturityException, WTException
  {
    String cStatus = "";
    if ((pbo instanceof ChangeOrderIfc)) {
      QueryResult qrBefore = ChangeHelper2.service.getChangeablesBefore((ChangeOrderIfc)pbo, true);
      while (qrBefore.hasMoreElements())
      {
        WTObject obj = (WTObject)qrBefore.nextElement();
        cStatus = ((LifeCycleManaged)obj).getLifeCycleState().toString();
        if (((obj instanceof EPMDocument)) || ((obj instanceof WTPart)))
          if ("RELEASED_S".equals(cStatus))
            LifeCycleHelper.service.setLifeCycleState(
              (LifeCycleManaged)obj, 
              State.toState("Temporary_D"));
          else if ("Temporary_D".equals(cStatus))
            LifeCycleHelper.service.setLifeCycleState(
              (LifeCycleManaged)obj, 
              State.toState("RELEASED_D"));
      }
    }
  }

  public static void setState(WTObject pbo, String state)
    throws MaturityException, WTException
  {
    if ((pbo instanceof WTDocument)) {
      System.out.println("zyj--test--doc--");
      LifeCycleHelper.service.setLifeCycleState(
        (LifeCycleManaged)pbo, 
        State.toState(state));
    }
  }

  public static void verifyIfRoleSet(ObjectReference processRef, String role) throws WTException {
    WfProcess wfp = getProcess(processRef);
    Team team = (Team)wfp.getTeamId().getObject();
    Role myRole = null;
    myRole = Role.toRole(role);
    Enumeration enumPrin = team.getPrincipalTarget(myRole);

    if (!enumPrin.hasMoreElements())
      throw new WTException(myRole.getDisplay());
  }

  public static WfProcess getProcess(ObjectReference processRef)
    throws WTException
  {
    WfAssignedActivity mySelf = (WfAssignedActivity)processRef
      .getObject();

    WfProcess wfp = mySelf.getParentProcess();
    return wfp;
  }

  public static void verifyIfRoleSet(ObjectReference processRef, Vector vec)
    throws WTException
  {
    String roles = "";
    for (int i = 0; i < vec.size(); i++)
    {
      String role = vec.get(i).toString();
      WfProcess wfp = getProcess(processRef);
      Team team = (Team)wfp.getTeamId().getObject();
      Role myRole = null;
      myRole = Role.toRole(role);
      Enumeration enumPrin = team.getPrincipalTarget(myRole);
      if (!enumPrin.hasMoreElements())
        roles = roles + myRole.getDisplay() + "、";
    }
    if ((roles != null) && (!"".equals(roles)))
    {
      roles = roles.substring(0, roles.length() - 1);
      throw new WTException("角色" + roles + "不能为空！");
    }
  }

  public static boolean isCheckout(Workable obj)
    throws WTException
  {
    return WorkInProgressHelper.isCheckedOut(obj);
  }

  public static boolean isAllCheckinForPN(WTObject primaryBusinessObject) throws MaturityException, WTException
  {
    PromotionNotice pn = (PromotionNotice)primaryBusinessObject;
    QueryResult qr = MaturityHelper.service
      .getPromotionTargets(pn);
    while (qr.hasMoreElements()) {
      Workable objWorkable = (Workable)qr.nextElement();
      if (isCheckout(objWorkable)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isAllLatestForPN(WTObject primaryBusinessObject) throws MaturityException, WTException
  {
    PromotionNotice pn = (PromotionNotice)primaryBusinessObject;
    QueryResult qr = MaturityHelper.service
      .getPromotionTargets(pn);
    while (qr.hasMoreElements()) {
      Iterated iterated = (Iterated)qr.nextElement();
      Iterated latested = 
        VersionControlHelper.getLatestIteration(iterated);
      if (!latested.toString().equals(iterated.toString())) {
        return false;
      }
    }
    return true;
  }

  public static Mastered getMastered(WTObject obj) {
    if ((obj instanceof WTPart)) {
      return ((WTPart)obj).getMaster();
    }
    if ((obj instanceof WTDocument)) {
      return ((WTDocument)obj).getMaster();
    }
    if ((obj instanceof EPMDocument)) {
      return ((EPMDocument)obj).getMaster();
    }

    return null;
  }

  public static WTObject updatePNcollection(WTObject obj)
    throws WTPropertyVetoException, WTException
  {
    String state = "";
    String pnname = "";
    Vector vector = new Vector();
    if ((obj instanceof PromotionNotice)) {
      System.out.println("===promotionNotice===========");
      PromotionNotice pn = (PromotionNotice)obj;
      pnname = pn.getName();

      QueryResult qr = MaturityHelper.service
        .getPromotionTargets(pn);
      while (qr.hasMoreElements()) {
        WTObject obj1 = (WTObject)qr.nextElement();
        vector.add(obj1);
      }
    } else if ((obj instanceof WTDocument)) {
      System.out.println("===WTDocument===========");
      vector.add(obj);
    } else if ((obj instanceof EPMDocument)) {
      System.out.println("===EPMDocument===========");
      vector.add(obj);
    } else {
      System.out.println("===else===========");
    }

    WTSet set1 = new WTHashSet();
    WTSet set2 = new WTHashSet();
    int size = vector.size();
    for (int i = 0; i < size; i++) {
      WTObject qrv = (WTObject)vector.get(i);

      set1.add(qrv);
      System.out.println("set1;" + qrv.toString());
      RevisionControlled revcon = getLatestObject(getMastered(qrv));
      set2.add(revcon);
      System.out.println("set2;" + revcon.toString());
    }

    PromotionNotice pn1 = (PromotionNotice)obj;
    MaturityHelper.service.deletePromotionTargets(pn1, set1);
    MaturityHelper.service.savePromotionTargets(pn1, set2);
    return pn1;
  }
  public static void updateLastedPromotable(PromotionNotice promotionNotice) throws WTException {
    QueryResult qr = MaturityHelper.service.getPromotionTargets(promotionNotice, false);
    System.out.println("qr:" + qr.size() + "=====================");
    WTHashSet newHashSet = new WTHashSet();
    WTCollection collection = new WTHashSet();
    try { while (qr.hasMoreElements()) {
        PromotionTarget target = (PromotionTarget)qr.nextElement();
        Promotable promotable = target.getPromotable();
        if ((promotable instanceof Iterated)) { System.out.println("==========1===========");

          if (((promotable instanceof Workable)) && 
            (WorkInProgressHelper.isCheckedOut((Workable)promotable))) {
            promotable = (Promotable)WorkInProgressHelper.service.checkin(
              (Workable)promotable, "AutoCheckin");
            System.out.println("==========2===========");
            newHashSet.add(promotable); target.setPromotable(promotable); collection.add(target);
          }
          else if (!VersionControlHelper.isLatestIteration(promotable)) {
            System.out.println("==========3===========");
            promotable = (Promotable)
              VersionControlHelper.getLatestIteration(promotable);
            if (((promotable instanceof Workable)) && 
              (WorkInProgressHelper.isCheckedOut((Workable)promotable))) {
              System.out.println("==========4===========");
              promotable = (Promotable)WorkInProgressHelper.service.checkin(
                (Workable)promotable, "AutoCheckin");
            }newHashSet.add(promotable); target.setPromotable(promotable); collection.add(target);
          } } 
      } } catch (WTPropertyVetoException e) {
      throw new WTException(e);
    }collection = PersistenceHelper.manager.save(collection);
    if (newHashSet.size() > 0) {
      System.out.println("==========5===========");
      MaturityBaseline baseline = promotionNotice.getConfiguration();
      BaselineHelper.service.addToBaseline(newHashSet, baseline);
    }
    System.out.println("newHashSet:" + newHashSet + "==============newHashSet.size:" + newHashSet.size());
  }

  public static RevisionControlled getLatestObject(Mastered master) throws WTException
  {
    QueryResult queryResult = VersionControlHelper.service
      .allVersionsOf(master);
    return (RevisionControlled)queryResult.nextElement();
  }

  public static boolean isLatestVersion(Versioned versioned)
  {
    try
    {
      QueryResult queryResult = VersionControlHelper.service
        .allVersionsOf(versioned);
      if (queryResult.hasMoreElements()) {
        Versioned latestversioned = (Versioned)queryResult
          .nextElement();
        String sourceversion = versioned.getVersionIdentifier()
          .getValue();
        String latestversion = latestversioned.getVersionIdentifier()
          .getValue();
        if (sourceversion.equals(latestversion))
          return true;
      }
    } catch (PersistenceException pe) {
      System.out.println("This is not a Versioned object.");
    } catch (WTException wte) {
      System.out.println("This is not a Versioned object.");
    }
    return false;
  }

  public static HashMap recordReview(String route, HashMap reviews) throws WTException
  {
    if (reviews == null)
      reviews = new HashMap();
    WTUser usr = (WTUser)SessionHelper.manager
      .getPrincipal();
    String usrName = usr.getName();
    DateFormat df = DateFormat.getDateInstance(
      1, Locale.CHINA);
    Date date = new Date(System.currentTimeMillis());
    String timeString = df.format(date);
    String[] usertime = { usrName, timeString };
    reviews.put(route, usertime);
    return reviews;
  }

  public static HashMap recordReviewepm(String route, HashMap reviews) throws WTException
  {
    if (reviews == null)
      reviews = new HashMap();
    WTUser usr = (WTUser)SessionHelper.manager
      .getPrincipal();
    String usrName = usr.getName();
    DateFormat df = DateFormat.getDateInstance(
      1, Locale.CHINA);
    Date date = new Date(System.currentTimeMillis());
    String timeString = df.format(date);
    String[] usertime = { usrName, timeString };
    reviews.put(route, usertime);
    return reviews;
  }

  public static void printReviews(HashMap reviews) {
    Iterator it = reviews.keySet().iterator();
    while (it.hasNext()) {
      String routeString = (String)it.next();
      String usr = ((String[])reviews.get(routeString))[0];
      System.out.println(routeString + "  " + usr);
    }
    System.out.println("--------");
  }

  public static boolean verifyifAfterChangeableReviewedForECN(WTObject wto, String state) throws WTException
  {
    QueryResult queryresult_activity = ChangeHelper2.service
      .getChangeActivities((WTChangeOrder2)wto);
    QueryResult queryresult_after;
    while(queryresult_activity.hasMoreElements()){
    	WTChangeActivity2 activity = (WTChangeActivity2)queryresult_activity
    			.nextElement();
    	queryresult_after = ChangeHelper2.service
    			.getChangeablesAfter(activity);
    	while(queryresult_after.hasMoreElements())
    	{
    		Changeable2 changeable_after = (Changeable2)queryresult_after
    				.nextElement();
    		if (((changeable_after instanceof WTPart)) || 
    				((changeable_after instanceof EPMDocument)) || 
    				((changeable_after instanceof WTDocument)))
    		{
    			if (!((LifeCycleManaged)changeable_after)
    					.getLifeCycleState().toString().equals(state)) {
    				return false;
    			}
    		}
    	}
    }

    return true;
  }

  public static boolean verifyifAfterChangeableReviewedForCA(WTObject wto, String state) throws WTException
  {
    WTChangeActivity2 activity = (WTChangeActivity2)wto;

    QueryResult queryresult_after = ChangeHelper2.service
      .getChangeablesAfter(activity);
    while (queryresult_after.hasMoreElements()) {
      Changeable2 changeable_after = (Changeable2)queryresult_after
        .nextElement();
      if (((changeable_after instanceof WTPart)) || 
        ((changeable_after instanceof EPMDocument)) || 
        ((changeable_after instanceof WTDocument)))
      {
        if (!((LifeCycleManaged)changeable_after)
          .getLifeCycleState().toString().equals(state)) {
          return false;
        }
      }
    }
    return true;
  }

  public static void getDrawingForPN(WTObject pbo, Vector<EPMDocument> vec) throws MaturityException, WTException
  {
    PromotionNotice pn = (PromotionNotice)pbo;
    QueryResult qr = MaturityHelper.service
      .getPromotionTargets(pn);
    while (qr.hasMoreElements()) {
      Object object = qr.nextElement();
      if (Util.isCADDRAWING(object))
        vec.add((EPMDocument)object);
    }
  }
}