package ext.tzc.tasv.printApplicationFrom.processor;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.tzc.tasv.printApplicationFrom.util.PrintApplicationFromHelp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.pom.Transaction;

public class CreatePrintApplicationFromProcessor
  implements RemoteAccess
{
  public static FormResult execute(NmCommandBean nmcommandbean)
  {
    FormResult formresult = new FormResult();
    Transaction tran = new Transaction();
    ArrayList printList = new ArrayList();
    ArrayList viewList = new ArrayList();
    File file = null;
    WTDocument doc = null;
    try {
      tran.start();
      Object object = nmcommandbean.getPrimaryOid().getWtRef().getObject();
      if ((object instanceof WTPart)) {
        WTPart part = (WTPart)object;
        printList.add(part);
        String name = part.getName();
        String tagName = name.substring(0, name.indexOf("-") + 1);
        boolean stateFlag = false;
        String state = part.getState().getState().toString();
        if (state.startsWith("RELEASED")) {
          stateFlag = true;
        }
        PrintApplicationFromHelp.getChildPart(part, printList, viewList, tagName, stateFlag, true);
        WTReference containerRefer = part.getContainerReference();
        String namePrefix = name.substring(0, name.indexOf("#"));
        doc = PrintApplicationFromHelp.createPrintApplicationFrom(containerRefer, namePrefix);
        String docNumber = doc.getNumber();
        file = PrintApplicationFromHelp.createExcel(docNumber, printList, viewList, namePrefix, tagName, stateFlag);
        if (file.exists()) {
          PrintApplicationFromHelp.addPrimaryContent(doc, file);
          file.delete();
        }
      } else if ((object instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)object;
        QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
        while (qr.hasMoreElements()) {
          Object object01 = qr.nextElement();
          if ((object01 instanceof WTChangeActivity2)) {
            WTChangeActivity2 eca = (WTChangeActivity2)object01;
            QueryResult ecaqr = ChangeHelper2.service.getChangeablesAfter(eca);
            while (ecaqr.hasMoreElements()) {
              Object obj = ecaqr.nextElement();
              if ((obj instanceof EPMDocument)) {
                EPMDocument epm = (EPMDocument)obj;
                String cadName = epm.getCADName();
                if ((cadName.endsWith(".dwg")) || (cadName.endsWith(".DWG"))) {
                  printList.add(epm);
                }
              }
            }
          }
        }
        String ecnName = ecn.getName().replaceAll("/", "-");
        WTReference containerRefer = ecn.getContainerReference();
        doc = PrintApplicationFromHelp.createPrintApplicationFrom(containerRefer, ecnName);
        String docNumber = doc.getNumber();
        file = PrintApplicationFromHelp.createExcel(docNumber, printList, viewList, ecnName, null, false);
        if (file.exists()) {
          PrintApplicationFromHelp.addPrimaryContent(doc, file);
          file.delete();
        }
      }else if(object instanceof WTChangeActivity2){
    	  WTChangeActivity2 eca = (WTChangeActivity2)object;
          QueryResult ecaqr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ecaqr.hasMoreElements()) {
            Object obj = ecaqr.nextElement();
            if ((obj instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)obj;
              String cadName = epm.getCADName();
              if ((cadName.endsWith(".dwg")) || (cadName.endsWith(".DWG"))) {
                printList.add(epm);
              }
            }
          }
          String ecnName = eca.getName();
          WTReference containerRefer = eca.getContainerReference();
          doc = PrintApplicationFromHelp.createPrintApplicationFrom(containerRefer, ecnName);
          String docNumber = doc.getNumber();
          file = PrintApplicationFromHelp.createExcel(docNumber, printList, viewList, ecnName, null, false);
          if (file.exists()) {
            PrintApplicationFromHelp.addPrimaryContent(doc, file);
            file.delete();
          }
      }
      tran.commit();
    }
    catch (wt.util.WTException e)
    {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (tran != null)
        tran.rollback();
    }
    if ((file != null) && 
      (file.exists())) {
      file.delete();
    }

    String uString = PrintApplicationFromHelp.getObjUrl(doc);

    formresult.setStatus(FormProcessingStatus.SUCCESS);
    formresult.setNextAction(FormResultAction.FORWARD);
    formresult.setURL(uString);
    return formresult;
  }
}