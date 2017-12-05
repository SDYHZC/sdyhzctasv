package ext.tasv.change.fileprint;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.ptc.core.components.rendering.guicomponents.DateDisplayComponent;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;
import com.ptc.netmarkets.workflow.NmWorkflowService;
import com.ptc.windchill.enterprise.workflow.WorkflowDataUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
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
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
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
import wt.pom.PersistenceException;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.series.MultilevelSeries;
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
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;

public class SignUtil
  implements RemoteAccess, Serializable
{
  private static final long serialVersionUID = 1L;
  static float CC = 28.35F;
  static float CY = 28.35F;

  public static void main(String[] args)
    throws PropertyVetoException, IOException, WTException
  {
    RemoteMethodServer rmi = RemoteMethodServer.getDefault();
    rmi.setUserName("wcadmin");
    rmi.setPassword("pdm");

    String oid = "VR%3Awt.change2.WTChangeOrder2%3A194467".replaceAll("%3A", ":");
    WTChangeOrder2 ecn = (WTChangeOrder2)getObjectByOid(oid);
    try
    {
      Hashtable signMap = new Hashtable();
      signMap.put("设计", "张三  2014/01/02");
      signMap.put("校对", "李四  2014/01/02");
      signMap.put("工艺", "王五 2014/01/02");
      signMap.put("审核", "赵六  2014/01/02");
      signMap.put("会签", "Jason  2014/01/02;;;Laurence  2014/01/02;;;刘七  2014/01/02");
      signMap.put("标准化", "周八 2014/01/02");
      signMap.put("审定", "龙九 2014/01/02");

      WTObject obj = ecn;
      rmi.invoke("signEcn", SignUtil.class.getName(), null, 
        new Class[] { WTObject.class, Hashtable.class }, 
        new Object[] { obj, signMap });
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  public static Object getObjectByOid(String oid)
  {
    Object obj = null;
    ReferenceFactory rf = new ReferenceFactory();
    try {
      obj = rf.getReference(oid).getObject();
    } catch (WTRuntimeException e) {
      e.printStackTrace();
    } catch (WTException e) {
      e.printStackTrace();
    }
    return obj;
  }

  public static void execute(WTObject obj, ObjectReference self)
    throws WTException, PropertyVetoException, IOException
  {
    Hashtable table = getSignInfo(self);
    signEcn(obj, table);
  }

  public static Hashtable<String, String> getSignInfo(ObjectReference self)
    throws IOException
  {
    String signInfo = "";
    Hashtable table = new Hashtable();
    try {
      WfProcess process = null;
      if ((self.getObject() instanceof WfProcess))
        process = (WfProcess)self.getObject();
      else {
        process = ((WfAssignedActivity)self.getObject())
          .getParentProcess();
      }
      QueryResult qr = NmWorkflowHelper.service
        .getVotingEventsForProcess(process);
      List list = new ArrayList();
      list.add("设计");
      list.add("校对");
      list.add("审核");
      list.add("工艺");
      list.add("标准化");
      list.add("会签");
      list.add("审定");
      WorkflowDataUtility wdu = new WorkflowDataUtility();
      while (qr.hasMoreElements()) {
        WfVotingEventAudit voteEvent = (WfVotingEventAudit)qr
          .nextElement();
        String activityName = voteEvent.getActivityName();
        System.out.println("===========+++++----" + activityName);
        if (list.contains(activityName))
          if ("会签".equals(activityName)) {
            WTUser user = (WTUser)voteEvent.getUserRef()
              .getPrincipal();

            String name = user.getFullName();

            String endTime = ((DateDisplayComponent)wdu.getDataValue(
              "workCompletedDate", voteEvent, null))
              .getDisplayValue().substring(0, 10);
            System.out.println("endTime*************" + endTime);

            String time = "";
            if (endTime.contains("/"))
              time = endTime.replaceAll("/", ".").trim();
            else if (endTime.contains("-")) {
              time = endTime.replaceAll("-", ".").trim();
            }

            System.out.println("time*************" + time);

            if ("".equals(signInfo))
              signInfo = name + time;
            else {
              signInfo = signInfo + ";;;" + name + time;
            }

            table.put(activityName, signInfo);
            System.out.println("table is  会签用户信息----------->>>>" + table);
            System.out.println("signInfo ----------->>>>" + signInfo);
          }
          else
          {
            WTUser user = (WTUser)voteEvent.getUserRef()
              .getPrincipal();

            String name = user.getFullName();

            String endTime = ((DateDisplayComponent)wdu.getDataValue(
              "workCompletedDate", voteEvent, null))
              .getDisplayValue().substring(0, 10).replaceAll("/", "").trim();
            System.out.println("签字日期**************************" + endTime);

            String time = "";
            if (endTime.contains("/"))
              time = endTime.replaceAll("/", ".").trim();
            else if (endTime.contains("-")) {
              time = endTime.replaceAll("-", ".").trim();
            }

            System.out.println("time*************" + time);

            Set set = table.keySet();
            if (!set.contains(activityName)) {
              table.put(activityName, name + time);
              System.out.println("signInfo ----------->>>>" + name + time);
            }
            System.out.println("table is ----------->>>>" + table);
          }
      }
    }
    catch (WTException e)
    {
      e.printStackTrace();
    }
    return table;
  }

  public static void signEcn(WTObject obj, Hashtable signMap)
    throws WTException, PropertyVetoException, IOException
  {
    String type = "";
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qreca = ChangeHelper2.service.getChangeActivities(ecn);
      while (qreca.hasMoreElements()) {
        Object objeca = qreca.nextElement();
        if ((objeca instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)objeca;
          QueryResult qrdoc = ChangeHelper2.service
            .getChangeablesAfter(eca);
          while (qrdoc.hasMoreElements()) {
            Object objdoc = qrdoc.nextElement();
            if ((objdoc instanceof WTDocument)) {
              WTDocument doc = (WTDocument)objdoc;
              doc = (WTDocument)getLatestRevision((Master)doc.getMaster());
              String doctype = getTypeByObject(doc);
              if ("tasvdoc01".equals(doctype))
              {
                type = "tasvdoc01";
              }
              String ty = getSoftType(doc);
              if(!ty.contains("bzjmxb") && !ty.contains("fzmxb") && !ty.contains("wgjmxb") && !ty.contains("zcmxb") ){
	              String visualPath = getVisualPdfPath(doc);
	
	              System.out.println("可视化路径：visualPath：" + 
	                visualPath);
	              String signPath = signPDF(visualPath, signMap, type, doc);
	              System.out.println("signPath*****************" + signPath);
	              uploadpdf(doc, signPath);
	
	              File signPathPDF = new File(visualPath);
	              if (signPathPDF.exists()) {
	                signPathPDF.delete();
	              }
	
	              File visualPathFile = new File(visualPath);
	              if (visualPathFile.exists())
	                visualPathFile.delete();
              }
            }
          }
        }
      }
    }
  }
  public static String getSoftType(WTObject obj) throws WTException {
	    String typename = "";
	    TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
	    typename = type.getTypename();
	    return typename;
	  }
  public static RevisionControlled getLatestRevision(Master master)
  {
    RevisionControlled rc = null;
    if (master != null) {
      try {
        QueryResult qr = VersionControlHelper.service.allVersionsOf(master);
        while (qr.hasMoreElements()) {
          RevisionControlled obj = (RevisionControlled)qr.nextElement();
          if ((rc == null) || (obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))) {
            rc = obj;
          }
        }
        if (rc != null)
          rc = (RevisionControlled)VersionControlHelper.getLatestIteration(rc, false);
      }
      catch (PersistenceException e) {
        e.printStackTrace();
      } catch (VersionControlException e) {
        e.printStackTrace();
      } catch (WTException e) {
        e.printStackTrace();
      }
    }
    return rc;
  }

  public static String getTypeByObject(WTObject obj)
  {
    String pType = null;
    try {
      if ((obj instanceof WTPart)) {
        WTPart part = (WTPart)obj;
        pType = TypedUtilityServiceHelper.service
          .getExternalTypeIdentifier(part);
      } else if ((obj instanceof WTDocument)) {
        WTDocument doc = (WTDocument)obj;
        pType = TypedUtilityServiceHelper.service
          .getExternalTypeIdentifier(doc);
      } else if ((obj instanceof WTChangeActivity2)) {
        WTChangeActivity2 eca = (WTChangeActivity2)obj;
        pType = TypedUtilityServiceHelper.service
          .getExternalTypeIdentifier(eca);
      } else if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        pType = TypedUtilityServiceHelper.service
          .getExternalTypeIdentifier(ecn);
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

  public static String getVisualPdfPath(Representable doc)
    throws WTException, PropertyVetoException, IOException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    Representation representation = RepresentationHelper.service
      .getDefaultRepresentation(doc);
    System.out.println("doc:" + doc.getClass().getName() + 
      "==================");
    System.out.println("representation:" + representation + 
      "==================");
    if (representation != null) {
      ContentHolder ch = ContentHelper.service
        .getContents(doc);
      representation = (Representation)ContentHelper.service
        .getContents(representation);
      System.out.println("representation2:" + representation + 
        "==================");
      Vector vector1 = ContentHelper.getContentList(representation);
      System.out.println("vector1.size:" + vector1.size() + 
        "========================");
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        System.out.println("contentitem:" + contentitem + 
          "=================");
        if ((contentitem instanceof ApplicationData)) {
          ApplicationData applicationdata = (ApplicationData)contentitem;
          System.out.println("=============applicationdata:" + 
            applicationdata);
          InputStream in = ContentServerHelper.service
            .findContentStream(applicationdata);
          String filename = applicationdata.getFileName();

          filename = unescape(filename);
          if (filename.indexOf(".pdf") != -1) {
            String absoluteFileName = wtHome + File.separator + 
              "temp" + File.separator + filename;
            File viualFile = new File(absoluteFileName);

            if (viualFile.exists()) {
              viualFile.delete();
            }
            downloadFile(in, absoluteFileName);
            return absoluteFileName;
          }
        }
      }
    }
    return null;
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

  public static void uploadpdf(ContentHolder doc, String filePath)
    throws WTException, PropertyVetoException, IOException
  {
    boolean flagAccess = SessionServerHelper.manager
      .setAccessEnforced(false);
    System.out.println("--------filePath-------" + filePath);
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

  public static ContentHolder removeAttachment(ContentHolder doc, String filePath) throws WTException, PropertyVetoException
  {
    try
    {
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      Vector apps = ContentHelper.getApplicationData(ch2);
      for (Enumeration e = apps.elements(); e.hasMoreElements(); ) {
        ApplicationData appData = (ApplicationData)e.nextElement();
        String contentName = appData.getFileName().toLowerCase();
        System.out.println("文档主内容名---contentName--->>>" + contentName);
        if ((filePath == null) || ("".equals(filePath)))
          continue;
        String fileFullName = filePath.substring(filePath
          .lastIndexOf(File.separator) + 1);
        String fileName = fileFullName.substring(0, 
          fileFullName.lastIndexOf("."));
        System.out
          .println("可视化PDF文件全名---fileName--->>>" + fileName);
        if ((!fileFullName.equals(contentName)) || 
          (filePath.indexOf(".pdf") == -1)) continue;
        ContentServerHelper.service.deleteContent(ch2, appData);
        System.out.println("-------同名签名文件删除--------------");
      }

    }
    catch (WTPropertyVetoException wtpve)
    {
      wtpve.printStackTrace();
    }

    return doc;
  }

  public static String signPDF(String visualPath, Hashtable signMap, String writeType, WTDocument doc)
    throws IOException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    String tempPath = wtHome + File.separator + "temp" + File.separator;
    String outPath = "";
    try
    {
      PdfReader reader = new PdfReader(visualPath);
      int pages = reader.getNumberOfPages();
      System.out.println("pages:____________________" + pages);
      Rectangle psize = reader.getPageSize(1);
      System.out.println("psize_______________:" + psize.toString());
      System.out.println("psize_______________:" + psize);
      float height = psize.getHeight();
      System.out.println("height_______________:" + height);

      float width = psize.getWidth();
      System.out.println("width_______________:" + width);
      Rectangle rectPageSize = new Rectangle(width, height);
      com.lowagie.text.Document document = new com.lowagie.text.Document(
        rectPageSize);
      System.out.println("rectPageSize.getHeight():" + rectPageSize.getHeight());
      System.out.println("rectPageSize.getWidth():" + rectPageSize.getWidth());

      String fileFullName = visualPath.substring(visualPath
        .lastIndexOf(File.separator) + 1);

      String prefix = fileFullName
        .substring(0, fileFullName.indexOf("."));

      String suffix = fileFullName.substring(fileFullName.indexOf("."));

      String signName = "已签名_" + prefix + "_" + 
        getCurrentTime("yyyyMMddHHmmssSSS") + suffix;
      System.out.println("====signName 签名后文件名====" + signName);
      outPath = tempPath + signName;

      PdfWriter writer = PdfWriter.getInstance(document, 
        new FileOutputStream(outPath));
      document.open();
      PdfContentByte cb = writer.getDirectContent();
      BaseFont bf = BaseFont.createFont(wtHome + File.separator + 
        "codebase" + File.separator + "ext" + File.separator + 
        "tasv" + File.separator + "document" + File.separator + 
        "conf" + File.separator + "simsun.ttc,1", 
        "Identity-H", true);
      cb.setFontAndSize(bf, 12.0F);
      Hashtable xmlMap = new Hashtable();
      xmlMap = InitXml_DWG();
      System.out.println("xmlMap________________________-" + xmlMap);
      System.out.println("------writeType-------" + writeType);
      Hashtable typeMap = (Hashtable)xmlMap.get(writeType);
      for (int i = 1; i <= pages; i++) {
        Hashtable pageMap = null;
        if (i == 1)
          pageMap = (Hashtable)typeMap.get(String.valueOf(i));
        else {
          pageMap = (Hashtable)typeMap.get(String.valueOf(1));
        }
        if (pageMap != null) {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeToPdf(cb, bf, pageMap, signMap, doc);
          document.newPage();
        } else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          document.newPage();
        }
      }
      document.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("=====签名后输出文件路径====" + outPath);

    return outPath;
  }

  public static String getCurrentTime(String DATE_FORMAT)
  {
    Locale locale = WTContext.getContext().getLocale();
    Calendar calendar = Calendar.getInstance(WTContext.getContext()
      .getTimeZone(), locale);
    Date datDate = calendar.getTime();
    return WTStandardDateFormat.format(datDate, DATE_FORMAT, locale, 
      calendar.getTimeZone());
  }

  public static Hashtable<String, Hashtable> InitXml_DWG() throws Exception
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    Hashtable ht = new Hashtable();
    Hashtable ht1 = new Hashtable();
    Hashtable ht2 = new Hashtable();
    Hashtable ht3 = new Hashtable();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder bulider = factory.newDocumentBuilder();
    org.w3c.dom.Document doc = bulider.parse(wtHome + File.separator + "codebase" + 
      File.separator + "ext" + File.separator + "tasv" + 
      File.separator + "document" + File.separator + "conf" + 
      File.separator + "changeSign.xml");
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
          String kuan = nodeSection.getElementsByTagName("kuan")
            .item(0).getFirstChild().getNodeValue().trim();
          String chang = nodeSection.getElementsByTagName("chang")
            .item(0).getFirstChild().getNodeValue().trim();
          String namesize = nodeSection
            .getElementsByTagName("namefontsize").item(0)
            .getFirstChild().getNodeValue().trim();
          String datex = nodeSection.getElementsByTagName("datex")
            .item(0).getFirstChild().getNodeValue().trim();
          String datey = nodeSection.getElementsByTagName("datey")
            .item(0).getFirstChild().getNodeValue().trim();
          String fontsize = nodeSection
            .getElementsByTagName("fontsize").item(0)
            .getFirstChild().getNodeValue().trim();
          String rotation = nodeSection
            .getElementsByTagName("rotation").item(0)
            .getFirstChild().getNodeValue().trim();

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

  private static void writeToPdf(PdfContentByte cb, BaseFont bf, Hashtable pageMap, Hashtable signMap, WTDocument doc)
    throws DocumentException, IOException, IOException, WTException
  {
    String doctime = doc.getCreateTimestamp().toString().substring(0, 10).replaceFirst("-", "年").replaceFirst("-", "月").concat("日");
    System.out.println("文档创建时间：" + doctime);

    Enumeration keys = pageMap.keys();
    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();

      Hashtable section = (Hashtable)pageMap.get(key);
      float x = Float.parseFloat((String)section.get("x"));
      float y = Float.parseFloat((String)section.get("y"));
      float chang = Float.parseFloat((String)section.get("chang"));
      float kuan = Float.parseFloat((String)section.get("kuan"));
      float namefontsize = Float.parseFloat(
        (String)section
        .get("namefontsize"));
      float datex = Float.parseFloat((String)section.get("datex"));
      float datey = Float.parseFloat((String)section.get("datey"));
      float fontsize = Float.parseFloat((String)section.get("fontsize"));
      float rotation = Float.parseFloat((String)section.get("rotation"));

      if (signMap.get(key) != null) {
        if ("会签".equals(key)) {
          String usertime = (String)signMap.get(key);
          String[] users = usertime.split(";;;");
          for (int i = 0; i < users.length; i++) {
            if (i == 0) {
              System.out.println("会签第一个人信息users【0】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                CC, y * CY, rotation);
              cb.endText();
            } else if (i == 1) {
              System.out.println("会签第二个人信息users【1】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                CC, (float)((y - 0.75D) * CY), rotation);
              cb.endText();
            } else if (i == 2) {
              System.out.println("会签第二个人信息users【1】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                CC, (float)((y - 1.41D) * CY), rotation);
              cb.endText();
            } else if (i == 3) {
              System.out.println("会签第四个人信息users【3】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                CC, (float)(y - 2.14D) * CY, rotation);
              cb.endText();
            } else if (i == 4) {
              System.out.println("会签第四个人信息users【4】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                CC, (float)(y - 2.8D) * CY, rotation);
              cb.endText();
            }
          }
        }
        else
        {
          String usertime = (String)signMap.get(key);
          System.out.println("zyj--test--usertime:" + usertime);
          cb.beginText();
          cb.setFontAndSize(bf, namefontsize);
          cb.showTextAligned(0, usertime, x * 
            CC, y * CY, rotation);
          cb.endText();
        }

      }
      else if ("docNo".equals(key)) {
        cb.beginText();
        cb.setFontAndSize(bf, namefontsize);
        cb.showTextAligned(0, doc.getNumber(), x * 
          CC, y * CY, rotation);
        cb.endText();
      } else if ("date".equals(key)) {
        cb.beginText();
        cb.setFontAndSize(bf, namefontsize);
        cb.showTextAligned(0, doctime, x * 
          CC, y * CY, rotation);
        cb.endText();
      }
    }
  }

  public static boolean isExistUserPicture(String userName)
    throws IOException
  {
    boolean flag = false;
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    String filePath = userName + ".bmp";
    File signGif = new File(filePath);
    if (signGif.exists()) {
      flag = true;
    }
    return flag;
  }
}