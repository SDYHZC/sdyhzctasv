package ext.pdf;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.wvs.server.util.Util;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentType;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleService;
import wt.lifecycle.Phase;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.IterationIdentifier;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;

public class docUtil
{
  public static String getRootPath()
  {
    String dbPath = "";
    try {
      WTProperties properties = WTProperties.getLocalProperties();
      dbPath = properties.getProperty("WNC/Windchill.dir");
    }
    catch (Exception exp) {
      exp.printStackTrace();
    }
    return dbPath;
  }

  public static void downloadpdf2(EPMDocument EPMdoc, ArrayList arraylist)
    throws WTException, PropertyVetoException, IOException
  {
    System.out.println("-----------");
    Representation representation = RepresentationHelper.service
      .getDefaultRepresentation(EPMdoc);
    if (representation != null) {
      ContentHolder ch = ContentHelper.service
        .getContents(EPMdoc);
      representation = (Representation)ContentHelper.service
        .getContents(representation);
      Vector vector1 = ContentHelper.getContentList(representation);
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        if ((contentitem instanceof ApplicationData)) {
          ApplicationData applicationdata = (ApplicationData)contentitem;
          InputStream in = ContentServerHelper.service
            .findContentStream(applicationdata);
          String filename = applicationdata.getFileName();
          System.out.println("filename:" + filename);
          String extention = Util.getExtension(filename);
          System.out.println("extention:" + extention);
          String removeExtention = Util.removeExtension(filename);
          System.out.println("removeExtention:" + removeExtention);
          if (extention.equalsIgnoreCase("PDF")) {
            String path = "d:\\test\\" + filename;
            downloadFile(in, path);
            String doctypeString = EPMdoc.getDocType().toString();
            System.out.println(doctypeString);
            System.out.println(EPMdoc.getDocType().getDisplay(Locale.SIMPLIFIED_CHINESE));
            System.out.println(EPMdoc.getAuthoringApplication().toString());
          }
        }
      }
    }
  }

  public static void downloadpdf1(WTDocument doc, ArrayList arraylist)
    throws WTException, PropertyVetoException, IOException
  {
    Representation representation = RepresentationHelper.service
      .getDefaultRepresentation(doc);
    if (representation != null) {
      ContentHolder ch = ContentHelper.service
        .getContents(doc);
      representation = (Representation)ContentHelper.service
        .getContents(representation);
      Vector vector1 = ContentHelper.getContentList(representation);
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        if ((contentitem instanceof ApplicationData)) {
          ApplicationData applicationdata = (ApplicationData)contentitem;
          InputStream in = ContentServerHelper.service
            .findContentStream(applicationdata);
          String filename = applicationdata.getFileName();
          System.out.println("filename:" + filename);
          String extention = Util.getExtension(filename);
          System.out.println("extention:" + extention);
          String removeExtention = Util.removeExtension(filename);
          System.out.println("removeExtention:" + removeExtention);
          if (extention.equalsIgnoreCase("PDF")) {
            String path = "d:\\test\\" + filename;
            downloadFile(in, path);
            String doctypeString = doc.getDocType().toString();
            System.out.println(doctypeString);
            System.out.println(doc.getDocType().getDisplay(Locale.SIMPLIFIED_CHINESE));
          }
        }
      }
    }
  }

  protected static void downloadFile(InputStream in, String path)
    throws FileNotFoundException
  {
    FileOutputStream outputStream1 = new FileOutputStream(path);
    InputStream inputStream = in;
    byte[] buffer = new byte[1024];
    try
    {
      int len;
      while ((len = inputStream.read(buffer)) > 0)
      {
      //  int len;
        outputStream1.write(buffer, 0, len);
      }
      inputStream.close();
      outputStream1.flush();
      outputStream1.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("download is completed!");
  }

  public static ContentHolder removeAttachment(ContentHolder doc, String attachName) throws WTException, PropertyVetoException
  {
    try {
      ContentHolder ch2 = ContentHelper.service.getContents(doc);
      Vector apps = ContentHelper.getApplicationData(ch2);
      for (Enumeration e = apps.elements(); e.hasMoreElements(); ) {
        ApplicationData contentItem = (ApplicationData)e.nextElement();
        String contentItemStr = contentItem.getFileName().toLowerCase();
        String attachNameStr = attachName.toLowerCase();
        if (attachNameStr.indexOf(contentItemStr) != -1)
          ContentServerHelper.service.deleteContent(ch2, contentItem);
      }
    }
    catch (WTPropertyVetoException wtpve)
    {
      wtpve.printStackTrace();
    }

    return doc;
  }

  public static ContentHolder removeAllAttachment(ContentHolder doc) throws WTException, PropertyVetoException
  {
    try {
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      Vector apps = ContentHelper.getApplicationData(ch2);
      for (Enumeration e = apps.elements(); e.hasMoreElements(); )
      {
        ApplicationData contentItem = (ApplicationData)e.nextElement();
        ContentServerHelper.service.deleteContent(ch2, contentItem);
      }
    }
    catch (WTPropertyVetoException wtpve)
    {
      wtpve.printStackTrace();
    }

    return doc;
  }

  public static void uploadDRWpdf(ContentHolder doc, String filePath)
    throws WTException, PropertyVetoException, IOException
  {
    ContentHolder ch2 = ContentHelper.service
      .getContents(doc);
    Vector apps = ContentHelper.getApplicationData(ch2);
    System.out.println("apps====>" + apps.size());
    ApplicationData contentItem = null;
    for (Enumeration e = apps.elements(); e.hasMoreElements(); ) {
      ApplicationData contentItem1 = (ApplicationData)e.nextElement();
      System.out.println("contentItem.getFileName()======>" + contentItem1.getFileName());
      if (filePath.indexOf(contentItem1.getFileName()) > 0) {
        contentItem1.setRole(ContentRoleType.SECONDARY);
        contentItem = contentItem1;
      }
    }
    ApplicationData returnAppData;
    if (contentItem != null) {
      System.out.println("contentItem.getFileName()======>" + contentItem.getFileName());

      returnAppData = ContentServerHelper.service
        .updateContent(ch2, contentItem, filePath);
    }
    else {
      ApplicationData appData2 = ApplicationData.newApplicationData(ch2);
      appData2.setRole(ContentRoleType.SECONDARY);
      ContentServerHelper.service.updateContent(ch2, appData2, filePath);
    }
  }

  public static void uploadpdf(ContentHolder doc, String filePath)
    throws WTException, PropertyVetoException, IOException
  {
    doc = removeAttachment(doc, filePath);
    ContentHolder ch2 = ContentHelper.service
      .getContents(doc);
    ApplicationData appData2 = ApplicationData.newApplicationData(ch2);
    appData2.setRole(ContentRoleType.SECONDARY);

    ContentServerHelper.service.updateContent(ch2, 
      appData2, filePath);
  }

  public static void saveFiletoAttachment(ContentHolder contentholder, FileInputStream fileinputstream)
    throws WTException, WTPropertyVetoException, PropertyVetoException, IOException
  {
    ApplicationData appData2 = 
      ApplicationData.newApplicationData(contentholder);
    appData2.setRole(ContentRoleType.SECONDARY);
    ContentServerHelper.service.updateContent(contentholder, appData2, 
      fileinputstream);
  }

  public static WTDocument getDocumentByName(String docName)
  {
    WTDocument wtdocument = null;
    try {
      System.out.println("docname:" + docName);
      QuerySpec qs = new QuerySpec(WTDocument.class);
      qs.appendWhere(
        new SearchCondition(WTDocument.class, 
        "master>name", "=", docName));
      qs.appendAnd();
      qs.appendWhere(
        new SearchCondition(WTDocument.class, 
        "iterationInfo.latest", "TRUE"));
      LatestConfigSpec latestconfigspec = new LatestConfigSpec();
      qs = latestconfigspec.appendSearchCriteria(qs);
      QueryResult qr = PersistenceHelper.manager.find(qs);

      qr = latestconfigspec.process(qr);
      if (qr.size() >= 1)
        wtdocument = (WTDocument)qr.nextElement();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return wtdocument;
  }

  public static boolean Compare(String pbocName, String filename)
  {
    String filetype = filename.substring(filename.lastIndexOf(".") + 1, 
      filename.length());
    System.out.println(filetype);
    String filename2 = filename.substring(0, filename.lastIndexOf("."));
    System.out.println(filename2);

    if (filetype.equalsIgnoreCase("PDF"))
    {
      if (filename2
        .equalsIgnoreCase(pbocName)) return true;
    }
    return 
      false;
  }

  public static boolean Compare2(String pbocName, String filename)
  {
    String filetype = filename.substring(filename.lastIndexOf(".") + 1, 
      filename.length());
    System.out.println(filetype);
    String filename2 = filename.substring(0, filename.lastIndexOf("."));
    System.out.println(filename2);

    return (filetype.equalsIgnoreCase("PDF")) && 
      (filename2.indexOf(Util.removeExtension(pbocName)) != -1) && 
      (filename2
      .length() == pbocName.length());
  }

  public static String pdfdoctype(WTObject obj)
  {
    String docty = "";
    try {
      TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
      String docType = type.getTypename();
      if (docType.indexOf(".") >= 0) {
        docType = docType.substring(docType.lastIndexOf(".") + 1, 
          docType.length());
      }
      System.out.println("�ĵ�����:" + docType);
      if ((docType.equals("Drawing")) || (docType.equals("KeyPartList")) || 
        (docType.equals("ProTechStandard")))
        docty = "CAD�ĵ�";
      else
        docty = "��ͨ�ĵ�";
    } catch (Exception et) {
      et.printStackTrace();
    }
    return docty;
  }

  public static String getstate(WTObject obj) throws WTException
  {
    String state = "";
    Phase ph = LifeCycleHelper.service
      .getCurrentPhase((LifeCycleManaged)obj);
    System.out.println("ph=============" + ph);
    state = ph.getName();
    return state;
  }

  public static String getbanbenhao(WTObject obj) throws WTException
  {
    String version = "";
    String iterate = "";
    String banbenhao = "";
    version = ((Versioned)obj).getVersionIdentifier().getValue();

    iterate = ((Iterated)obj).getIterationIdentifier().getValue();

    banbenhao = version + "." + iterate;
    return banbenhao;
  }
}