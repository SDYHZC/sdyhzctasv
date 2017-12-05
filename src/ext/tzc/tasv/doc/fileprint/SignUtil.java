package ext.tzc.tasv.doc.fileprint;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.ptc.core.components.rendering.guicomponents.DateDisplayComponent;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.netmarkets.workflow.NmWorkflowService;
import com.ptc.windchill.enterprise.workflow.WorkflowDataUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityService;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.util.WTStandardDateFormat;
import wt.vc.baseline.ManagedBaseline;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;

public class SignUtil
  implements RemoteAccess, Serializable
{
  static float CC = 28.35F;
  static float CY = 28.35F;

  public static void execute(WTObject obj, ObjectReference self)
  {
    Hashtable table = getRevise(self);
    try {
      signDOCpdf(obj, table, "A1");
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (PropertyVetoException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void signDOCpdf(WTObject obj, Hashtable<String, String> htrevise, String writeType)
    throws WTException, PropertyVetoException, IOException
  {
    if ((obj instanceof WTDocument)) {
      WTDocument doc = (WTDocument)obj;
      String pdfPath = downloadpdf(doc);
      String signFile = signPDF(doc, pdfPath, htrevise, writeType);
      uploadpdf(doc, signFile);
      File tempPdfFile = new File(pdfPath);
      if (tempPdfFile.exists()) {
        tempPdfFile.delete();
      }
      tempPdfFile = new File(signFile);
      if (tempPdfFile.exists())
        tempPdfFile.delete();
    }
  }

  public static String signPDF(WTDocument doc, String fileName, Hashtable htrevise, String writeType)
  {
    String outfilename = "";
    try {
      String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
      PdfReader reader = new PdfReader(fileName);
      int pages = reader.getNumberOfPages();
      Rectangle psize = reader.getPageSize(3);
      float height = psize.getHeight();
      float width = psize.getWidth();
      Rectangle rectPageSize = new Rectangle(width, height);
      com.lowagie.text.Document document = new com.lowagie.text.Document(rectPageSize);
      int ii = fileName.lastIndexOf(File.separator);
      outfilename = fileName.substring(ii + 1, fileName.length());

      PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outfilename));
      document.open();
      PdfContentByte cb = writer.getDirectContent();
      BaseFont bf = BaseFont.createFont(wtHome + File.separator + 
        "codebase" + File.separator + "ext" + File.separator + 
        "tzc" + File.separator + "tasv" + File.separator + "conf" + File.separator + 
        "simsun.ttc,1", "Identity-H", true);
      cb.setFontAndSize(bf, 12.0F);
      Hashtable hts = InitXml_DWG();
      Hashtable htf = (Hashtable)hts.get(writeType);
      for (int i = 1; i <= pages; i++) {
        Hashtable htp = (Hashtable)htf.get(String.valueOf(i));
        if (htp != null) {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeTextnew(doc, cb, bf, htp, htrevise);
          document.newPage();
        } else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          document.newPage();
        }
      }
      document.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (DocumentException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return outfilename;
  }

  public static void writeTextnew(WTDocument doc, PdfContentByte cb, BaseFont bf, Hashtable ht, Hashtable htrevise)
  {
    Enumeration keys = ht.keys();
    while (keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      Hashtable section = (Hashtable)ht.get(key);
      float x = Float.parseFloat((String)section.get("x"));
      float y = Float.parseFloat((String)section.get("y"));
      float chang = Float.parseFloat((String)section.get("chang"));
      float kuan = Float.parseFloat((String)section.get("kuan"));
      float namefontsize = Float.parseFloat((String)section.get("namefontsize"));
      float datex = Float.parseFloat((String)section.get("datex"));
      float datey = Float.parseFloat((String)section.get("datey"));
      float fontsize = Float.parseFloat((String)section.get("fontsize"));
      float rotation = Float.parseFloat((String)section.get("rotation"));

      if (htrevise.get(key) != null) {
        String usertime = (String)htrevise.get(key);
        String[] msg = usertime.split(";;;");
        if (msg.length > 1) {
          String name = msg[0];
          String time = msg[1];

          cb.beginText();
          cb.setFontAndSize(bf, namefontsize);
          cb.showTextAligned(0, name, x * CC, y * CY, rotation);
          cb.endText();

          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, time, datex * CC, datey * CY, rotation);
          cb.endText();
        }
      }
    }
  }

  public static Hashtable<String, String> getRevise(ObjectReference self)
  {
    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { ObjectReference.class };
      Object[] argValues = { self };
      try {
        return (Hashtable)RemoteMethodServer.getDefault().invoke("getRevise", SignUtil.class.getName(), null, argTypes, argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    Hashtable table = new Hashtable();
    try {
      WfProcess process = null;
      if ((self.getObject() instanceof WfProcess))
        process = (WfProcess)self.getObject();
      else {
        process = ((WfAssignedActivity)self.getObject()).getParentProcess();
      }
      WorkflowDataUtility wdu = new WorkflowDataUtility();
      List list = new ArrayList();
      list.add("编制");
      list.add("校对");
      list.add("审核");
      list.add("标审");
      list.add("会签");
      list.add("批准");
      QueryResult qr = NmWorkflowHelper.service.getVotingEventsForProcess(process);
      while (qr.hasMoreElements()) {
        WfVotingEventAudit voteEvent = (WfVotingEventAudit)qr.nextElement();
        String activityName = voteEvent.getActivityName();
        if (list.contains(activityName)) {
          WTUser user = (WTUser)voteEvent.getUserRef().getPrincipal();
          String userName = user.getFullName();
          String endTime = ((DateDisplayComponent)wdu.getDataValue("workCompletedDate", voteEvent, null)).getDisplayValue();
          endTime = endTime.substring(0, 10);
          endTime = endTime.replaceAll("-", ".");
          endTime = endTime.replaceAll("/", ".");

          Set set = table.keySet();
          if (!set.contains(activityName)) {
            table.put(activityName, userName + ";;;" + endTime);
          }
        }
      }
    }
    catch (WTRuntimeException e)
    {
      e.printStackTrace();
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    return table;
  }

  public static String downloadpdf(Representable representable)
  {
    try
    {
      String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
      Representation representation = RepresentationHelper.service.getDefaultRepresentation(representable);
      if (representation != null) {
        ContentHolder ch = ContentHelper.service.getContents(representable);
        representation = (Representation)ContentHelper.service.getContents(representation);
        Vector vector = ContentHelper.getContentList(representation);
        for (int i = 0; i < vector.size(); i++) {
          ContentItem contentitem = (ContentItem)vector.elementAt(i);
          if ((contentitem instanceof ApplicationData)) {
            ApplicationData applicationdata = (ApplicationData)contentitem;
            InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
            String filename = applicationdata.getFileName();
            filename = unescape(filename);
            if (filename.indexOf(".pdf") != -1) {
              String absoluteFileName = wtHome + File.separator + "temp" + File.separator + filename;
              downloadFile(in, absoluteFileName);
              return absoluteFileName;
            }
          }
        }
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (PropertyVetoException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Hashtable InitXml_DWG()
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    Hashtable ht = new Hashtable();
    Hashtable ht1 = new Hashtable();
    Hashtable ht2 = new Hashtable();
    Hashtable ht3 = new Hashtable();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder bulider = factory.newDocumentBuilder();
    org.w3c.dom.Document doc = bulider.parse(wtHome + File.separator + "codebase" + File.separator + "ext" + File.separator + "tzc" + File.separator + "tasv" + File.separator + "conf" + File.separator + "signpdf_DWG.xml");
    NodeList nl = doc.getElementsByTagName("type");

    for (int i = 0; i < nl.getLength(); i++) {
      Element nodeType = (Element)nl.item(i);
      String name1 = nodeType.getAttribute("name");

      NodeList n2 = nodeType.getElementsByTagName("page");
      ht1 = new Hashtable();
      for (int j = 0; j < n2.getLength(); j++) {
        Element nodePage = (Element)n2.item(j);
        String name2 = nodePage.getAttribute("pageno");
        NodeList n3 = nodePage.getElementsByTagName("section");
        ht2 = new Hashtable();
        for (int k = 0; k < n3.getLength(); k++) {
          ht3 = new Hashtable();
          Element nodeSection = (Element)n3
            .item(k);
          String name3 = nodeSection.getAttribute("name");
          String x = nodeSection.getElementsByTagName("x").item(0)
            .getFirstChild().getNodeValue().trim();
          String y = nodeSection.getElementsByTagName("y").item(0)
            .getFirstChild().getNodeValue().trim();
          String kuan = nodeSection.getElementsByTagName("chang")
            .item(0).getFirstChild().getNodeValue().trim();
          String chang = nodeSection.getElementsByTagName("kuan")
            .item(0).getFirstChild().getNodeValue().trim();
          String namesize = nodeSection.getElementsByTagName("namefontsize")
            .item(0).getFirstChild().getNodeValue().trim();
          String datex = nodeSection.getElementsByTagName("datex")
            .item(0).getFirstChild().getNodeValue().trim();
          String datey = nodeSection.getElementsByTagName("datey")
            .item(0).getFirstChild().getNodeValue().trim();
          String fontsize = nodeSection.getElementsByTagName(
            "fontsize").item(0).getFirstChild().getNodeValue()
            .trim();
          String rotation = nodeSection.getElementsByTagName(
            "rotation").item(0).getFirstChild().getNodeValue()
            .trim();

          ht3.put("x", x);
          ht3.put("y", y);
          ht3.put("chang", kuan);
          ht3.put("kuan", chang);
          ht3.put("namefontsize", namesize);
          ht3.put("datex", datex);
          ht3.put("datey", datey);
          ht3.put("fontsize", fontsize);
          ht3.put("rotation", rotation);
          ht2.put(name3, ht3);
        }
        ht1.put(name2, ht2);
      }
      ht.put(name1, ht1);
    }
    return ht;
  }

  public static String unescape(String s)
  {
    StringBuffer sbuf = new StringBuffer();
    int l = s.length();
    int ch = -1;
    int sumb = 0;
    int i = 0; for (int more = -1; i < l; i++)
    {
      int b;
      switch (ch = s.charAt(i)) {
      case '%':
        i++; ch = s.charAt(i);
        int hb = (Character.isDigit((char)ch) ? ch - 48 : 
          '\n' + Character.toLowerCase((char)ch) - 97) & 0xF;
        i++; ch = s.charAt(i);
        int lb = (Character.isDigit((char)ch) ? ch - 48 : 
          '\n' + Character.toLowerCase((char)ch) - 97) & 0xF;
        b = hb << 4 | lb;
        break;
      case '+':
        b = 32;
        break;
      default:
        b = ch;
      }

      if ((b & 0xC0) == 128) {
        sumb = sumb << 6 | b & 0x3F;
        more--; if (more == 0)
          sbuf.append((char)sumb);
      } else if ((b & 0x80) == 0) {
        sbuf.append((char)b);
      } else if ((b & 0xE0) == 192) {
        sumb = b & 0x1F;
        more = 1;
      } else if ((b & 0xF0) == 224) {
        sumb = b & 0xF;
        more = 2;
      } else if ((b & 0xF8) == 240) {
        sumb = b & 0x7;
        more = 3;
      } else if ((b & 0xFC) == 248) {
        sumb = b & 0x3;
        more = 4;
      } else {
        sumb = b & 0x1;
        more = 5;
      }
    }

    return sbuf.toString();
  }

  public static void downloadFile(InputStream in, String path)
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
        outputStream1.write(buffer, 0, len);
      }
      inputStream.close();
      outputStream1.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getCurrentTime(String DATE_FORMAT)
  {
    Locale locale = WTContext.getContext().getLocale();
    Calendar calendar = Calendar.getInstance(WTContext.getContext().getTimeZone(), locale);
    Date datDate = calendar.getTime();
    return WTStandardDateFormat.format(datDate, DATE_FORMAT, locale, calendar.getTimeZone());
  }

  public static void uploadpdf(ContentHolder doc, String filePath)
    throws WTException, PropertyVetoException, IOException
  {
    boolean flagAccess = SessionServerHelper.manager
      .setAccessEnforced(false);
    try
    {
      doc = removeAttachment(doc, filePath);
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      ApplicationData appData2 = ApplicationData.newApplicationData(ch2);

      appData2.setRole(ContentRoleType.SECONDARY);
      ContentServerHelper.service.updateContent(ch2, appData2, filePath);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(flagAccess);
    }
  }

  public static ContentHolder removeAttachment(ContentHolder doc, String attachName)
    throws WTException, PropertyVetoException
  {
    try
    {
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      Vector apps = ContentHelper.getApplicationData(ch2);
      for (Enumeration e = apps.elements(); e.hasMoreElements(); ) {
        ApplicationData contentItem = (ApplicationData)e.nextElement();

        String contentItemStr = contentItem.getFileName().toLowerCase();

        String attachNameStr = attachName.toLowerCase();
        String fileName = attachNameStr.substring(attachNameStr
          .lastIndexOf(File.separator) + 1, attachNameStr
          .length());

        if ((contentItemStr.indexOf("已签名") != -1) || (contentItemStr.indexOf(".pdf") != -1))
          ContentServerHelper.service.deleteContent(ch2, contentItem);
      }
    }
    catch (WTPropertyVetoException wtpve) {
      wtpve.printStackTrace();
    }

    return doc;
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

  public static void main(String[] args)
  {
    RemoteMethodServer rmi = RemoteMethodServer.getDefault();
    rmi.setUserName("orgadmin");
    rmi.setPassword("wcadmin");
    ReferenceFactory rf = new ReferenceFactory();
    String oid = "VR:wt.doc.WTDocument:50157036";
    try {
      Hashtable htrevise = new Hashtable();
      htrevise.put("编制", "刘志超;;;2016.11.21");
      htrevise.put("校对", "武之剑;;;2016.11.25");
      htrevise.put("审核", "梁俊涛;;;2016.11.28");
      htrevise.put("标准化", "侯宗栋;;;2016.11.28");
      htrevise.put("批准", "朱学斌;;;2016.11.30");
      WTDocument doc = (WTDocument)rf.getReference(oid).getObject();
      WTObject obj = doc;
      rmi.invoke("signDOCpdf", SignUtil.class.getName(), null, new Class[] { WTObject.class, Hashtable.class, String.class }, new Object[] { obj, htrevise, "A4" });
    }
    catch (WTException e1) {
      e1.printStackTrace();
    }
    catch (RemoteException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}