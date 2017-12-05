package ext.part;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.mpml.processplan.MPMProcessPlan;
import com.ptc.windchill.mpml.processplan.MPMProcessPlanHelper;
import com.ptc.windchill.mpml.processplan.MPMProcessPlanService;
import com.ptc.windchill.mpml.processplan.operation.MPMOperation;
import com.ptc.windchill.mpml.processplan.operation.MPMOperationMaster;
import com.ptc.windchill.mpml.processplan.operation.MPMOperationUsageLink;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleService;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.part.PartDocHelper;
import wt.part.PartDocService;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartService;
import wt.part.WTPartStandardConfigSpec;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.config.OwnershipIndependentLatestConfigSpec;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewService;

public class PartUtil
  implements RemoteAccess
{
  public static WTPartConfigSpec wtPartStandardConfigSpec;
  public static WTPartConfigSpec wtPartManufactureConfigSpec;
  public static WTPartConfigSpec wtPartgzConfigSpec;

  static
  {
    try
    {
      wtPartStandardConfigSpec = WTPartConfigSpec.newWTPartConfigSpec(
        WTPartStandardConfigSpec.newWTPartStandardConfigSpec(ViewHelper.service.getView("Design"), null));

      wtPartManufactureConfigSpec = WTPartConfigSpec.newWTPartConfigSpec(
        WTPartStandardConfigSpec.newWTPartStandardConfigSpec(ViewHelper.service.getView("Manufacturing"), null));
      wtPartgzConfigSpec = WTPartHelper.service.findWTPartConfigSpec();
    } catch (WTException wte) {
      System.out.println(wte);
    }
  }

  public static void getReviewStructure(WTPart part, Vector allVector)
  {
    try {
      String cState = null;

      QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof LifeCycleManaged)) {
          cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
          if ((!cState.equals("INWORK")) && (!cState.equals("APPROVED")) && (!cState.equals("OBSOLESCENCE"))) {
            if ((obj instanceof WTDocument)) {
              WTDocument doc = (WTDocument)obj;

              TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
              String docType = type.getTypename();

              if (docType.indexOf("TYWJ") >= 0)
                allVector.add(doc);
            }
            else {
              EPMDocument epm = (EPMDocument)obj;
              allVector.add(epm);
            }
          }
        }

      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          cState = childpart.getLifeCycleState().toString();

          if ((!cState.equals("INWORK")) && (!cState.equals("APPROVED")) && (!cState.equals("OBSOLESCENCE")))
            allVector.add(childpart);
          else if (cState.equals("INWORK")) {
            getReviewStructure(childpart, allVector);
          }
        }
        else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getReviewPartStructure(WTPart part, Vector allVector)
  {
    try
    {
      String cState = null;
      cState = part.getLifeCycleState().toString();
      if (cState.equals("INWORK")) {
        allVector.add(part);

        QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
        while (qr.hasMoreElements()) {
          Persistable obj = (Persistable)qr.nextElement();
          if ((obj instanceof LifeCycleManaged)) {
            cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
            if (cState.equals("INWORK")) {
              if ((obj instanceof WTDocument)) {
                WTDocument doc = (WTDocument)obj;

                TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
                String docType = type.getTypename();

                if (docType.indexOf("TYWJ") >= 0)
                  allVector.add(doc);
              }
              else {
                EPMDocument epm = (EPMDocument)obj;
                allVector.add(epm);
              }
            }
          }

        }

        QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
        while (subNodes.hasMoreElements()) {
          Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
          if ((aSubNodePair[1] instanceof WTPart)) {
            WTPart childpart = (WTPart)aSubNodePair[1];

            getReviewPartStructure(childpart, allVector);
          } else if (!(aSubNodePair[1] instanceof WTPartMaster));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static MPMOperation getLatestIteration(MPMOperationMaster operationMaster)
    throws WTException, WTPropertyVetoException
  {
    QueryResult qrm = VersionControlHelper.service.allIterationsOf(operationMaster);
    qrm = new OwnershipIndependentLatestConfigSpec().process(qrm);
    if (qrm.hasMoreElements()) {
      MPMOperation prt1 = (MPMOperation)qrm.nextElement();
      return prt1;
    }
    return null;
  }

  public static void getReviewMBOM(WTPart part, Vector allVector)
  {
    try
    {
      String cState = null;

      QueryResult qr = MPMProcessPlanHelper.service.getMPMProcessPlans(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof Iterated)) {
          obj = VersionControlHelper.getLatestIteration((Iterated)obj);
        }
        if ((obj instanceof LifeCycleManaged)) {
          cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
          if ((!cState.equals("INWORK")) && (!cState.equals("APPROVED")) && (!cState.equals("OBSOLESCENCE"))) {
            allVector.add(obj);
            QueryResult qrs = MPMProcessPlanHelper.service
              .getMPMOperationUsageLinks((MPMProcessPlan)obj);
            while (qrs.hasMoreElements()) {
              MPMOperationUsageLink oper = (MPMOperationUsageLink)qrs.nextElement();
              MPMOperationMaster operationMaster = (MPMOperationMaster)oper.getUses();
              MPMOperation operation = getLatestIteration(operationMaster);

              if ((operation instanceof Iterated)) {
                operation = (MPMOperation)
                  VersionControlHelper.getLatestIteration(operation);
              }
              if ((operation instanceof LifeCycleManaged)) {
                cState = operation.getLifeCycleState().toString();
                if ((!cState.equals("INWORK")) && (!cState.equals("APPROVED")) && (!cState.equals("OBSOLESCENCE"))) {
                  allVector.add(operation);
                }
              }
            }
          }
        }
      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);

      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          cState = childpart.getLifeCycleState().toString();
          if ((!cState.equals("INWORK")) && (!cState.equals("INWORK_M")) && (!cState.equals("APPROVED")) && (!cState.equals("APPROVED_M")) && (!cState.equals("OBSOLESCENCE")) && (!cState.equals("OBSOLESCENCE_M")))
            allVector.add(childpart);
          else if (cState.equals("INWORK_M")) {
            getReviewMBOM(childpart, allVector);
          }
        }
        else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getReviewMBOMStructure(WTPart part, Vector allVector)
  {
    try
    {
      String cState = null;
      cState = part.getLifeCycleState().toString();
      if ((cState.equals("INWORK_M")) || (cState.equals("INWORK"))) {
        allVector.add(part);

        QueryResult qr = MPMProcessPlanHelper.service.getMPMProcessPlans(part);
        while (qr.hasMoreElements()) {
          Persistable obj = (Persistable)qr.nextElement();
          if ((obj instanceof Iterated)) {
            obj = VersionControlHelper.getLatestIteration((Iterated)obj);
          }
          if ((obj instanceof LifeCycleManaged)) {
            cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
            if (cState.equals("INWORK")) {
              allVector.add(obj);
              QueryResult qrs = MPMProcessPlanHelper.service
                .getMPMOperationUsageLinks((MPMProcessPlan)obj);
              while (qrs.hasMoreElements()) {
                MPMOperationUsageLink oper = (MPMOperationUsageLink)qrs.nextElement();
                MPMOperationMaster operationMaster = (MPMOperationMaster)oper.getUses();
                MPMOperation operation = getLatestIteration(operationMaster);

                if ((operation instanceof Iterated)) {
                  operation = (MPMOperation)
                    VersionControlHelper.getLatestIteration(operation);
                }
                if ((operation instanceof LifeCycleManaged)) {
                  cState = operation.getLifeCycleState().toString();
                  if (cState.equals("INWORK")) {
                    allVector.add(operation);
                  }
                }
              }
            }
          }
        }

        QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);

        while (subNodes.hasMoreElements()) {
          Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
          if ((aSubNodePair[1] instanceof WTPart)) {
            WTPart childpart = (WTPart)aSubNodePair[1];
            getReviewMBOMStructure(childpart, allVector);
          } else if (!(aSubNodePair[1] instanceof WTPartMaster));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getAllMBOM(WTPart part, Vector allVector)
  {
    try {
      allVector.add(part);

      QueryResult qr = MPMProcessPlanHelper.service.getMPMProcessPlans(part);
      QueryResult qrs;
      for (; qr.hasMoreElements(); 
        qrs.hasMoreElements())
      {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof Iterated)) {
          obj = VersionControlHelper.getLatestIteration((Iterated)obj);
        }

        allVector.add(obj);
        qrs = MPMProcessPlanHelper.service
          .getMPMOperationUsageLinks((MPMProcessPlan)obj);
        continue;
        MPMOperationUsageLink oper = (MPMOperationUsageLink)qrs.nextElement();
        MPMOperationMaster operationMaster = (MPMOperationMaster)oper.getUses();
        MPMOperation operation = getLatestIteration(operationMaster);

        if ((operation instanceof Iterated)) {
          operation = (MPMOperation)
            VersionControlHelper.getLatestIteration(operation);
          allVector.add(operation);
        }

      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);

      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          getAllMBOM(childpart, allVector);
        } else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getAllMBOMPart(WTPart part, Vector allVector)
  {
    try {
      allVector.add(part);
      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);

      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          getAllMBOMPart(childpart, allVector);
        } else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getAllEBOM(WTPart part, Vector allVector)
  {
    try {
      allVector.add(part);

      QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)obj;

          TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
          String docType = type.getTypename();

          if (docType.indexOf("TYWJ") >= 0) {
            allVector.add(doc);
          }

        }
        else
        {
          EPMDocument epm = (EPMDocument)obj;
          allVector.add(epm);
        }

      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];

          getAllEBOM(childpart, allVector);
        } else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getAllEBOMPart(WTPart part, Vector allVector)
  {
    try {
      allVector.add(part);
      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];

          getAllEBOMPart(childpart, allVector);
        } else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getOneCADVector(WTPart part, Vector cadV)
  {
    try
    {
      QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)obj;

          TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
          String docType = type.getTypename();

          if (docType.indexOf("TYWJ") >= 0)
            cadV.add(doc);
        }
        else {
          EPMDocument epm = (EPMDocument)obj;
          cadV.add(epm);
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void getOneMPMPlan(WTPart part, Vector planV)
  {
    try
    {
      QueryResult qr = MPMProcessPlanHelper.service.getMPMProcessPlans(part);
      QueryResult qrs;
      for (; qr.hasMoreElements(); 
        qrs.hasMoreElements())
      {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof Iterated)) {
          obj = VersionControlHelper.getLatestIteration((Iterated)obj);
        }

        planV.add(obj);

        qrs = MPMProcessPlanHelper.service
          .getMPMOperationUsageLinks((MPMProcessPlan)obj);
        continue;
        MPMOperationUsageLink oper = (MPMOperationUsageLink)qrs.nextElement();
        MPMOperationMaster operationMaster = (MPMOperationMaster)oper.getUses();
        MPMOperation operation = getLatestIteration(operationMaster);

        if ((operation instanceof Iterated)) {
          operation = (MPMOperation)
            VersionControlHelper.getLatestIteration(operation);
          planV.add(operation);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getAllGZ(WTPart part, Vector gzVector)
  {
    try {
      gzVector.add(part);

      QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)obj;

          TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
          String docType = type.getTypename();
          if (docType.indexOf("TYWJ") >= 0)
            gzVector.add(doc);
        }
        else {
          EPMDocument epm = (EPMDocument)obj;
          gzVector.add(epm);
        }

      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          getAllGZ(childpart, gzVector);
        }
        else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getReviewgz(WTPart part, Vector allVector)
  {
    try
    {
      String cState = null;

      QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
      while (qr.hasMoreElements()) {
        Persistable obj = (Persistable)qr.nextElement();
        if ((obj instanceof LifeCycleManaged)) {
          cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
          if ((!cState.equals("INWORK")) && (!cState.equals("APPROVED")) && (!cState.equals("OBSOLESCENCE"))) {
            if ((obj instanceof WTDocument)) {
              WTDocument doc = (WTDocument)obj;

              TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
              String docType = type.getTypename();

              if (docType.indexOf("TYWJ") >= 0)
                allVector.add(doc);
            }
            else {
              EPMDocument epm = (EPMDocument)obj;
              allVector.add(epm);
            }
          }

        }

      }

      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          cState = childpart.getLifeCycleState().toString();
          if ((!cState.equals("INWORK")) && (!cState.equals("INWORK_M")) && (!cState.equals("APPROVED")) && (!cState.equals("APPROVED_M")) && (!cState.equals("OBSOLESCENCE")) && (!cState.equals("OBSOLESCENCE_M")))
            allVector.add(childpart);
          else if (cState.equals("INWORK_M")) {
            getReviewgz(childpart, allVector);
          }
        }
        else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void getReviewgzStructure(WTPart part, Vector allVector)
  {
    try {
      String cState = part.getLifeCycleState().toString();
      if ((cState.equals("INWORK_M")) || (cState.equals("INWORK"))) {
        allVector.add(part);

        QueryResult qr = PartDocHelper.service.getAssociatedDocuments(part);
        while (qr.hasMoreElements()) {
          Persistable obj = (Persistable)qr.nextElement();
          if ((obj instanceof LifeCycleManaged)) {
            cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
            if (cState.equals("INWORK")) {
              if ((obj instanceof WTDocument)) {
                WTDocument doc = (WTDocument)obj;

                TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(doc);
                String docType = type.getTypename();

                if (docType.indexOf("TYWJ") >= 0)
                  allVector.add(doc);
              }
              else {
                EPMDocument epm = (EPMDocument)obj;
                allVector.add(epm);
              }
            }

          }

        }

        QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartManufactureConfigSpec);
        while (subNodes.hasMoreElements()) {
          Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
          if ((aSubNodePair[1] instanceof WTPart)) {
            WTPart childpart = (WTPart)aSubNodePair[1];
            getReviewgzStructure(childpart, allVector);
          } else if (!(aSubNodePair[1] instanceof WTPartMaster));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static Persistable getPersistable(String oid)
  {
    try {
      WTReference wtref = new ReferenceFactory().getReference(oid);
      if (wtref != null)
        return wtref.getObject();
    } catch (WTException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void getAllPart(WTPart part, Vector vectors)
  {
    vectors.add(part);
    try {
      QueryResult subNodes = WTPartHelper.service.getUsesWTParts(part, wtPartStandardConfigSpec);
      while (subNodes.hasMoreElements()) {
        Persistable[] aSubNodePair = (Persistable[])subNodes.nextElement();
        if ((aSubNodePair[1] instanceof WTPart)) {
          WTPart childpart = (WTPart)aSubNodePair[1];
          getAllPart(childpart, vectors);
        } else if (!(aSubNodePair[1] instanceof WTPartMaster));
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static Vector getPartStructure(Vector partVector)
  {
    Vector checkVector = new Vector();
    Iterator it = partVector.iterator();
    while (it.hasNext()) {
      WTPart partobj = (WTPart)it.next();
      String cState = partobj.getLifeCycleState().toString();
      if ((cState.equals("REVIEWS")) || (cState.equals("UNDERREVIEW")) || (cState.equals("REVIEWS_M")) || 
        (cState.equals("UNDERREVIEW_M"))) {
        checkVector.add(partobj);
      }
    }
    return checkVector;
  }

  public static void setLifecycleTemplate(WTObject obj, String lcname)
  {
    try
    {
      if ((obj instanceof LifeCycleManaged)) {
        LifeCycleTemplate lctold = (LifeCycleTemplate)((LifeCycleManaged)obj)
          .getLifeCycleTemplate().getObject();
        WTContainerRef siteRef = lctold.getContainerReference();
        LifeCycleTemplateReference LCTemplate = LifeCycleHelper.service
          .getLifeCycleTemplateReference(lcname, siteRef);

        LifeCycleHelper.service.reassign((LifeCycleManaged)obj, LCTemplate);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static boolean isbzj(String number) {
    if ((number.startsWith("GB")) || (number.startsWith("JB")) || (number.startsWith("0")) || 
      (number.startsWith("D"))) {
      return true;
    }
    return false;
  }
}