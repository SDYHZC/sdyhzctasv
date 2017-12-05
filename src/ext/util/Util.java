package ext.util;

import java.util.Vector;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentType;
import wt.epm.build.EPMBuildHistory;
import wt.epm.structure.EPMReferenceLink;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.util.WTException;

public class Util
  implements RemoteAccess
{
  public static boolean isCADDRAWING(Object obj)
  {
    if ((obj instanceof EPMDocument)) {
      EPMDocument emp = (EPMDocument)obj;
      String doctypeString = emp.getDocType().toString();
      if (doctypeString.equalsIgnoreCase("CADDRAWING")) {
        return true;
      }
      if (emp.getAuthoringApplication().toString()
        .equalsIgnoreCase("ACAD")) {
        return true;
      }
    }
    return false;
  }

  public static Vector<EPMDocument> getDescCAD(WTPart part, Vector<EPMDocument> vec)
    throws WTException
  {
    QueryResult qr0 = PersistenceHelper.manager.navigate(part, "builtBy", 
      EPMBuildHistory.class);
    while (qr0.hasMoreElements()) {
      EPMDocument epm = (EPMDocument)qr0.nextElement();
      if (epm.getAuthoringApplication().toString().equalsIgnoreCase(
        "PROE")) {
        QueryResult refsDoc = PersistenceHelper.manager.navigate(epm
          .getMaster(), "referencedBy", 
          EPMReferenceLink.class, false);
        while (refsDoc.hasMoreElements())
        {
          EPMReferenceLink link = (EPMReferenceLink)refsDoc
            .nextElement();
          if (link.getDepType() == 4) {
            EPMDocument oo = link.getReferencedBy();
            vec.add(oo);
          }
        }
      } else if ((epm.getAuthoringApplication().toString().equalsIgnoreCase(
        "ACAD")) && (epm.getDocType().toString().equalsIgnoreCase("CADCOMPONENT"))) {
        vec.add(epm);
      }
    }
    return vec;
  }
}