package ext.tzc.tasv.change.fileprint;

import ext.pdf.ReSignHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;

public class SignWFUtil
{
  public static void fileSign(WTObject pbo, ObjectReference self)
    throws WTRuntimeException, WTException
  {
    WfProcess process = null;
    if ((self.getObject() instanceof WfProcess))
      process = (WfProcess)self.getObject();
    else {
      process = ((WfAssignedActivity)self.getObject()).getParentProcess();
    }
    if ((pbo instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)pbo;
      QueryResult qr = ChangeHelper2.service.getChangeActivities(ecn);
      while (qr.hasMoreElements()) {
        Object object = qr.nextElement();
        if ((object instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)object;
          if(eca.getName().contains(ecn.getNumber())) continue;
          QueryResult ablesAfterQr = ChangeHelper2.service.getChangeablesAfter(eca);
          while (ablesAfterQr.hasMoreElements()) {
            Object object2 = ablesAfterQr.nextElement();
            if ((object2 instanceof EPMDocument)) {
              EPMDocument epm = (EPMDocument)object2;
              if (ReSignHelper.isUG(epm))
                ReSignHelper.signUG(epm, process, pbo);
              else
                ReSignHelper.signEPM(epm, process, pbo);
            }
            else if ((object2 instanceof WTDocument)) {
              ReSignHelper.signDoc((WTDocument)object2, process, pbo);
            }
          }
        }
      }
    }else if(pbo instanceof WTChangeActivity2){
    	WTChangeActivity2 eca = (WTChangeActivity2) pbo;
    	QueryResult ablesAfterQr = ChangeHelper2.service.getChangeablesAfter(eca);
    	while (ablesAfterQr.hasMoreElements()) {
    		Object object2 = ablesAfterQr.nextElement();
    		if ((object2 instanceof EPMDocument)) {
    			EPMDocument epm = (EPMDocument)object2;
    			if (ReSignHelper.isUG(epm))
    				ReSignHelper.signUG(epm, process, pbo);
    			else
    				ReSignHelper.signEPM(epm, process, pbo);
    		}
    		else if ((object2 instanceof WTDocument)) {
    			ReSignHelper.signDoc((WTDocument)object2, process, pbo);
    		}
    	}
    }
  }

  public static String getChangeValue(String value)
  {
    String newString = "";
    int count = 0;
    Pattern p = Pattern.compile("[一-龥]+");
    Matcher m = p.matcher(value);
    while (m.find()) {
      count = m.group(0).length();
    }
    if (count == 2) {
      value = value.substring(0, 2) + "  " + value.substring(2, value.length());
      newString = value.substring(0, 1) + "  " + value.substring(1, value.length());
    } else if (count == 3) {
      newString = value.substring(0, 3) + "  " + value.substring(3, value.length());
    } else {
      newString = value;
    }
    newString = newString.replace("-", ".");

    return newString;
  }
}