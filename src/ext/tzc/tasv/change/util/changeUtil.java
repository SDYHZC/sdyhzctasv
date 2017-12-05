package ext.tzc.tasv.change.util;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.part.WTPart;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityService;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.baseline.ManagedBaseline;

public class changeUtil
{
  public static void modifySECONDARYNameByObject(ContentHolder holder)
  {
    try
    {
      QueryResult qr = ContentHelper.service.getContentsByRole(holder, ContentRoleType.SECONDARY);
      while (qr.hasMoreElements()) {
        ApplicationData applicationdata = (ApplicationData)qr.nextElement();
        String fileName = applicationdata.getFileName();
        if ((fileName.endsWith(".pdf")) && (fileName.startsWith("已签名_"))) {
          String targetFileName = fileName.replace("已签名_", "批准_temp_");
          targetFileName = targetFileName.substring(0, targetFileName.lastIndexOf("_")) + ".pdf";
          applicationdata.setFileName(targetFileName);
          PersistenceHelper.manager.modify(applicationdata);
        }

      }

    }
    catch (WTException e)
    {
      e.printStackTrace();
    }
    catch (WTPropertyVetoException e) {
      e.printStackTrace();
    }
  }

  public static String getTypeByObject(WTObject obj) {
    String pType = null;
    try {
      if ((obj instanceof WTPart)) {
        WTPart part = (WTPart)obj;
        pType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(part);
      } else if ((obj instanceof WTDocument)) {
        WTDocument doc = (WTDocument)obj;
        pType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(doc);
      } else if ((obj instanceof ManagedBaseline)) {
        ManagedBaseline baseline = (ManagedBaseline)obj;
        pType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(baseline);
      }
      int m = pType.lastIndexOf(".");
      pType = pType.substring(m + 1);
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    return pType;
  }

  public static void copy(FileInputStream input, FileOutputStream output)
  {
    try {
      byte[] b = new byte[5120];
      int len;
      while ((len = input.read(b)) != -1)
      {
        output.write(b, 0, len);
      }
      output.flush();
      output.close();
      input.close();
    } catch (Exception e) {
      System.out.println("files in the dolder may be using by someone");
    }
  }

  public static void updateAttachment(ContentHolder holder, String filePath, ContentRoleType contentType)
  {
    boolean flagAccess = SessionServerHelper.manager
      .setAccessEnforced(false);
    try
    {
      QueryResult qr = ContentHelper.service.getContentsByRole(holder, 
        ContentRoleType.SECONDARY);
      while (qr.hasMoreElements()) {
        ApplicationData oAppData = (ApplicationData)qr.nextElement();
        String fileName = oAppData.getFileName();
        if ((fileName.endsWith(".pdf")) && (fileName.startsWith("已签名_"))) {
          ContentServerHelper.service.deleteContent(holder, oAppData);
        }
      }
      ApplicationData applicationData = ApplicationData.newApplicationData(holder);
      applicationData.setRole(contentType);
      applicationData = (ApplicationData)PersistenceHelper.manager
        .save(applicationData);
      applicationData = ContentServerHelper.service.updateContent(holder, 
        applicationData, filePath);
      applicationData = (ApplicationData)PersistenceHelper.manager
        .modify(applicationData);
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (WTPropertyVetoException e) {
      e.printStackTrace();
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (PropertyVetoException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(flagAccess);
    }
  }
}