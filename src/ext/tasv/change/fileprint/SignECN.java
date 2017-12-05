package ext.tasv.change.fileprint;

import com.lowagie.text.Image;
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
import ext.tasv.document.util.IBAUtil;
import ext.tasv.document.util.IBAUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrderIfc;
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
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.inf.team.ContainerTeamHelper;
import wt.lifecycle.State;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.PromotionNotice;
import wt.org.OrganizationServicesHelper;
import wt.org.OrganizationServicesManager;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.type.TypedUtilityService;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.BaselineService;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfRequesterActivity;
import wt.workflow.engine.WfState;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignment;
import wt.workflow.work.WfBallot;

public class SignECN
{
  static float hqspace = 5.5F;

  static float cc = 2.834381F;

  static float cy = 2.835017F;
  private static String path;
  private static String wthome;
  private static Image jpeg;
  private static String docTime = "";

  private static String bianhao = "";

  private static Properties properties = new Properties();

  private static Map zuMap = new HashMap();

  static {
    try { WTProperties wtproperties = WTProperties.getLocalProperties();
      wthome = wtproperties.getProperty("wt.home");
      path = wthome + "/temp/";
      zuMap.put("质量部_接收组", "质量部_接收组");
      zuMap.put("生产计划部_接收组", "生产计划部_接收组");
      zuMap.put("军品科研计划部_接收组", "军品科研计划部_接收组");
      zuMap.put("外协部_接收组", "外协部_接收组");
      zuMap.put("物资管理部_接收组", "物资管理部_接收组");
      zuMap.put("军品设计部_接收组", "军品设计部_接收组");
      zuMap.put("民品设计部_接收组", "民品设计部_接收组");
      zuMap.put("零部件设计部_接收组", "零部件设计部_接收组");
      zuMap.put("工艺部_接收组", "工艺部_接收组");
      zuMap.put("试验中心_接收组", "试验中心_接收组");
      zuMap.put("研究院项目管理部_接收组", "研究院项目管理部_接收组");
      zuMap.put("信息中心_接收组", "信息中心_接收组");
      zuMap.put("矿用自卸车业务部_接收组", "矿用自卸车业务部_接收组");
      zuMap.put("油田车业务部_接收组", "油田车业务部_接收组");
      zuMap.put("特种车业务部_接收组", "特种车业务部_接收组");
      zuMap.put("专用车业务部_接收组", "专用车业务部_接收组");
      zuMap.put("国际市场部_接收组", "国际市场部_接收组");
      zuMap.put("售后服务部_接收组", "售后服务部_接收组");
      zuMap.put("总装一车间_接收组", "总装一车间_接收组");
      zuMap.put("总装二车间_接收组", "总装二车间_接收组");
      zuMap.put("车桥车间_接收组", "车桥车间_接收组");
      zuMap.put("车架车身车间_接收组", "车架车身车间_接收组");
      zuMap.put("涂漆车间_接收组", "涂漆车间_接收组");
      zuMap.put("热处理车间_接收组", "热处理车间_接收组");
      zuMap.put("采购部_接收组", "采购部_接收组");
      zuMap.put("精益制造部_接收组", "精益制造部_接收组");
      zuMap.put("备份_接收组", "备份_接收组");
      String propertiespath = "";
      WTProperties props = WTProperties.getLocalProperties();
      propertiespath = props.getProperty("wt.home") + File.separatorChar + "codebase" + File.separatorChar + 
        "netmarkets" + File.separatorChar + "jsp" + File.separatorChar + 
        "ext" + File.separatorChar + "WTVariance" + File.separatorChar + "department.properties";
      System.out.println("zyj--test--propertiespath:" + propertiespath);
      properties.load(new FileInputStream(new File(propertiespath)));
    }
    catch (Throwable localThrowable)
    {
    }
  }

  private static Hashtable InitXml()
    throws Exception
  {
    Hashtable ht = new Hashtable();
    Hashtable ht1 = new Hashtable();
    Hashtable ht2 = new Hashtable();
    Hashtable ht3 = new Hashtable();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder bulider = factory.newDocumentBuilder();
    org.w3c.dom.Document doc = bulider.parse(wthome + "/codebase/ext/pdf/signpdf.xml");
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
          String datex = nodeSection.getElementsByTagName("datex")
            .item(0).getFirstChild().getNodeValue().trim();
          String datey = nodeSection.getElementsByTagName("datey")
            .item(0).getFirstChild().getNodeValue().trim();
          String yearx = nodeSection.getElementsByTagName("yearx")
            .item(0).getFirstChild().getNodeValue().trim();
          String yeary = nodeSection.getElementsByTagName("yeary")
            .item(0).getFirstChild().getNodeValue().trim();
          String monthx = nodeSection.getElementsByTagName("monthx")
            .item(0).getFirstChild().getNodeValue().trim();
          String monthy = nodeSection.getElementsByTagName("monthy")
            .item(0).getFirstChild().getNodeValue().trim();
          String dayx = nodeSection.getElementsByTagName("dayx")
            .item(0).getFirstChild().getNodeValue().trim();
          String dayy = nodeSection.getElementsByTagName("dayy")
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
          ht3.put("datex", datex);
          ht3.put("datey", datey);
          ht3.put("yearx", yearx);
          ht3.put("yeary", yeary);
          ht3.put("monthx", monthx);
          ht3.put("monthy", monthy);
          ht3.put("dayx", dayx);
          ht3.put("dayy", dayy);
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

  private static String getNodeValue(Element element, String childElementName)
  {
    String value = element.getElementsByTagName(childElementName).item(0)
      .getNodeValue();
    if (value != null)
      value = "";
    return value;
  }

  private static void writeText(PdfContentByte cb, BaseFont bf, Hashtable pageMap, Hashtable signMap, Vector<String> timeVec, String type, WTDocument doc)
    throws Exception
  {
    Enumeration keys = pageMap.keys();

    String doctime = doc.getCreateTimestamp().toString().substring(0, 10).replaceFirst("-", "年").replaceFirst("-", "月").concat("日");
    System.out.println("文档创建时间：" + doctime);
    while (keys.hasMoreElements())
    {
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
                SignUtil.CC, y * SignUtil.CY, rotation);
              cb.endText();
            } else if (i == 1) {
              System.out.println("会签第二个人信息users【1】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], 
                (float)((x + 1.9D) * 
                SignUtil.CC), y * SignUtil.CY, rotation);
              cb.endText();
            } else if (i == 2) {
              System.out.println("会签第三个人信息users【2】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], x * 
                SignUtil.CC, (float)((y - 0.62D) * SignUtil.CY), rotation);
              cb.endText();
            } else if (i == 3) {
              System.out.println("会签第四个人信息users【3】:" + users[i]);
              cb.beginText();
              cb.setFontAndSize(bf, namefontsize);
              cb.showTextAligned(0, users[i], (float)(x + 1.9D) * 
                SignUtil.CC, (float)(y - 0.62D) * SignUtil.CY, rotation);
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
            SignUtil.CC, y * SignUtil.CY, rotation);
          cb.endText();
        }

      }
      else if ("docNo".equals(key)) {
        cb.beginText();
        cb.setFontAndSize(bf, namefontsize);
        cb.showTextAligned(0, doc.getNumber(), x * 
          SignUtil.CC, y * SignUtil.CY, rotation);
        cb.endText();
      } else if ("date".equals(key)) {
        cb.beginText();
        cb.setFontAndSize(bf, namefontsize);
        cb.showTextAligned(0, doctime, x * 
          SignUtil.CC, y * SignUtil.CY, rotation);
        cb.endText();
      }
    }
  }

  public static String getType(String sourceType, WTDocument wtdoc)
  {
    String xihuafenlei = "---";
    if (wtdoc != null) {
      IBAUtil ibUtil = new IBAUtil(wtdoc);
      xihuafenlei = ibUtil.getIBAValue("WENJIANFENLEI");
    }
    String returnType = null;
    try {
      Properties prop = new Properties();
      String filename = wthome + "/codebase/ext/pdf/type.properties";
      FileInputStream fis = new FileInputStream(filename);
      prop.load(fis);
      Enumeration enump = prop.keys();
      while (enump.hasMoreElements()) {
        String t = (String)enump.nextElement();
        String t2 = new String(t.getBytes("ISO8859-1"), "GB2312");
        String t1 = prop.getProperty(t);
        System.out.println("t.getBytes:" + t.getBytes("ISO8859-1"));
        System.out.println("t2:" + t2 + "==============sourceType:" + sourceType);
        System.out.println("t2.indexOf(sourceType):" + t2.indexOf(sourceType) + "======sourceType.indexOf(t2):" + sourceType.indexOf(t2));
        if ((t2.indexOf(sourceType) != -1) || 
          (sourceType.indexOf(t2) != -1)) {
          returnType = t1;

          break;
        }
        System.out.println("xihuafenlei:" + xihuafenlei);
        if ((xihuafenlei == null) || (xihuafenlei.length() <= 0) || (
          (t2.indexOf(xihuafenlei) == -1) && 
          (xihuafenlei.indexOf(t2) == -1))) continue;
        returnType = t1;
        break;
      }

    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return returnType;
  }

  public static Hashtable InitXmlDoc()
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    System.out.println("进入xmL *********************  解析");
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

  public static void writeText(Hashtable signMap, String type, String fileName, Vector<String> timeVec, WTDocument wtdoc, WTObject pbo)
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    Hashtable xmlMap = InitXmlDoc();

    System.out.println("***********xmlMap*********" + xmlMap);
    Hashtable typeMap = (Hashtable)xmlMap.get(type);
    System.out.println("***********typeMap*********" + typeMap);
    System.out.println("***********type*********" + type);
    try {
      String oldfileName = fileName;

      PdfReader reader = new PdfReader(
        fileName);

      int pages = reader.getNumberOfPages();

      Rectangle psize = reader.getPageSize(1);
      float height = psize.getHeight();
      float width = psize.getWidth();
      Rectangle psize2 = reader.getPageSizeWithRotation(1);
      Rectangle rectPageSize = null;

      if ((psize2.getWidth() != width) && (psize2.getHeight() != height))
      {
        rectPageSize = new Rectangle(height, width);
        System.out.println("111height-==>" + rectPageSize.getHeight() + "|||width-==>" + rectPageSize.getWidth());
        System.out.println(rectPageSize.getRotation());
        rectPageSize = rectPageSize.rotate();
        System.out.println("222height-==>" + rectPageSize.getHeight() + "|||width-==>" + rectPageSize.getWidth());
        System.out.println(rectPageSize.getRotation());
        rectPageSize = rectPageSize.rotate();
        System.out.println("333height-==>" + rectPageSize.getHeight() + "|||width-==>" + rectPageSize.getWidth());
        System.out.println(rectPageSize.getRotation());
        rectPageSize = rectPageSize.rotate();
        System.out.println("444height-==>" + rectPageSize.getHeight() + "|||width-==>" + rectPageSize.getWidth());
        System.out.println(rectPageSize.getRotation());

        com.lowagie.text.Document documentnew = new com.lowagie.text.Document(rectPageSize);
        int ii = fileName.lastIndexOf("/");
        fileName = path + "temp_" + fileName.substring(ii + 1);
        System.out.println("zyj-test--path--" + path);
        System.out.println("zyj-test--fileName--" + fileName);
        PdfWriter writernew = PdfWriter.getInstance(documentnew, new FileOutputStream(fileName));
        documentnew.open();

        PdfContentByte cbnew = writernew.getDirectContent();
        BaseFont bfnew = BaseFont.createFont(wtHome + File.separator + 
          "codebase" + File.separator + "ext" + File.separator + 
          "tasv" + File.separator + "document" + File.separator + 
          "conf" + File.separator + "simsun.ttc,1", 
          "Identity-H", true);

        cbnew.setFontAndSize(bfnew, 12.0F);

        for (int i = 1; i <= pages; i++)
        {
          PdfImportedPage pagei = writernew.getImportedPage(reader, i);
          cbnew.addTemplate(pagei, 0.0F, 0.0F);
          documentnew.newPage();
        }
        documentnew.close();
        writernew.close();
        reader = new PdfReader(fileName);
        pages = reader.getNumberOfPages();
        psize = reader.getPageSize(1);
        height = psize.getHeight();
        width = psize.getWidth();
        rectPageSize = new Rectangle(width, height);
      } else {
        rectPageSize = new Rectangle(width, height);
      }
      com.lowagie.text.Document document = new com.lowagie.text.Document(
        rectPageSize);

      int ii = fileName.lastIndexOf("/");
      String outfilename = path + "批准_" + fileName.substring(ii + 1);
      System.out.println("===outfilename:" + outfilename + "==================");
      PdfWriter writer = PdfWriter.getInstance(document, 
        new FileOutputStream(outfilename));
      document.open();

      PdfContentByte cb = writer.getDirectContent();

      BaseFont bf = BaseFont.createFont(wtHome + File.separator + 
        "codebase" + File.separator + "ext" + File.separator + 
        "tasv" + File.separator + "document" + File.separator + 
        "conf" + File.separator + "simsun.ttc,1", 
        "Identity-H", true);

      cb.setFontAndSize(bf, 12.0F);

      System.out.println("pages************************" + pages);

      for (int i = 1; i <= pages; i++) {
        Hashtable pageMap = null;
        if (i == 1) {
          pageMap = (Hashtable)typeMap.get(String.valueOf(i));
          System.out.println("i=1**********************" + i);
        } else {
          pageMap = (Hashtable)typeMap.get(String.valueOf(1));
          System.out.println("i!=1**********************" + i);
        }
        if (pageMap != null) {
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&&");
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);

          writeText(cb, bf, pageMap, signMap, timeVec, type, wtdoc);
          document.newPage();
        }
        else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          document.newPage();
        }

      }

      document.close();
      File pdfFileold = new File(oldfileName);
      File pdfFile = new File(fileName);

      File tempPdfFile = new File(outfilename);
      uploadpdf(wtdoc, outfilename);
      System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYY");

      System.out.println("11111111111111111111111");
      pdfFileold.delete();
      pdfFile.delete();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static String getSoftAtribute(WTObject obj) {
    String wenjianfenlei = "";
    if ((obj instanceof WTDocument)) {
      WTDocument doc = (WTDocument)obj;
      try {
        IBAUtil ibUtil = new IBAUtil(doc);
        wenjianfenlei = ibUtil.getIBAValue("WENJIANFENLEI");
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return wenjianfenlei;
  }

  public static String tufu(String fileName) {
    String a0h = "A0H";
    String a1h = "A1H";
    String a2h = "A2H";
    String a3h = "A3H";
    String a4h = "A4H";
    String a4s = "A4S";
    String a02 = "A02";
    String a03 = "A03";
    String a13 = "A13";
    String a14 = "A14";
    String a23 = "A23";
    String a24 = "A24";
    String a25 = "A25";
    String a33 = "A33";
    String a34 = "A34";
    String a35 = "A35";
    String a36 = "A36";
    String a37 = "A37";
    String a43 = "A43";
    String a44 = "A44";
    String a45 = "A45";
    String a46 = "A46";
    String a47 = "A47";
    String a48 = "A48";
    String a49 = "A49";
    String a0x3_5l = "a0x3_5l";
    String a0x4l = "a0x4l";
    try
    {
      PdfReader reader = new PdfReader(
        fileName);
      Rectangle rect = reader.getPageSizeWithRotation(1);
      float width = rect.getWidth();
      float height = rect.getHeight();
      String k = Float.toString(width);
      String g = Float.toString(height);
      System.out.println("tufu--width--" + Float.toString(width) + "tufu--height--" + Float.toString(height));
      if ((width > 3368.0F) && (width < 3398.0F) && (height > 2378.0F) && (height < 2408.0F)) {
        System.out.println("tufu: - -! A0H");
        return a0h;
      }

      if ((width > 2378.0F) && (width < 2408.0F) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A1H");
        return a1h;
      }

      if ((width > 1675.0F) && (width < 1705.0F) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A2H");
        return a2h;
      }
      if ((width > 1180.0F) && (width < 1200.0F) && (height > 832.0F) && (height < 852.0F)) {
        System.out.println("tufu: - -! A3H");
        return a3h;
      }if ((582.0D < width) && (width < 612.0D) && (height > 832.0D) && (height < 852.0D)) {
        System.out.println("tufu: - -! A4s");
        return a4s;
      }if ((832.0D < width) && (width < 852.0D) && (height > 582.0F) && (height < 612.0F)) {
        System.out.println("tufu: - -! A4h");
        return a4h;
      }if ((4750.0600000000004D < width) && (width < 4770.0600000000004D) && (height > 3350.8699999999999D) && (height < 3379.8699999999999D)) {
        System.out.println("tufu: - -! A02");
        return a02;
      }if ((7130.0900000000001D < width) && (width < 7150.0900000000001D) && (height > 3350.8699999999999D) && (height < 3379.8699999999999D)) {
        System.out.println("tufu: - -! A03");
        return a03;
      }if ((5030.8900000000003D < width) && (width < 5080.8900000000003D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A13");
        return a13;
      }if ((6710.7399999999998D < width) && (width < 6739.7399999999998D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A14");
        return a14;
      }if ((3550.6300000000001D < width) && (width < 3580.6300000000001D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A23");
        return a23;
      }if ((4750.0600000000004D < width) && (width < 4779.0600000000004D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A24");
        return a24;
      }if ((5930.6599999999999D < width) && (width < 5959.6599999999999D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A25");
        return a25;
      }if ((2511.5300000000002D < width) && (width < 2539.5300000000002D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A33");
        return a33;
      }if ((3350.8699999999999D < width) && (width < 3379.8699999999999D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A34");
        return a34;
      }if ((4190.3800000000001D < width) && (width < 4219.3800000000001D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A35");
        return a35;
      }if ((5030.8900000000003D < width) && (width < 5059.8900000000003D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A36");
        return a36;
      }if ((5870.3999999999996D < width) && (width < 5899.3999999999996D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A37");
        return a37;
      }if ((1770.9000000000001D < width) && (width < 1799.9000000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A43");
        return a43;
      }if ((2365.0300000000002D < width) && (width < 2399.0300000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A44");
        return a44;
      }if ((2960.3299999999999D < width) && (width < 2989.3299999999999D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A45");
        return a45;
      }if ((3550.6300000000001D < width) && (width < 3579.6300000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A46");
        return a46;
      }if ((4150.9300000000003D < width) && (width < 4179.9300000000003D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A47");
        return a47;
      }if ((4750.0600000000004D < width) && (width < 4779.0600000000004D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A48");
        return a48;
      }if ((5340.3599999999997D < width) && (width < 5369.3599999999997D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A49");
        return a49;
      }if ((11000.0F < width) && (width < 12000.0F) && (height > 2200.0F) && (height < 2500.0F)) {
        System.out.println("tufu: - -! a0x3_5l");
        return a0x3_5l;
      }if ((13000.0F < width) && (width < 13600.0F) && (height > 2200.0F) && (height < 2500.0F)) {
        System.out.println("tufu: - -! a0x4l");
        return a0x4l;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
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
      list.add("编制");
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
              time = endTime.replaceAll("/", "").trim();
            else if (endTime.contains("-")) {
              time = endTime.replaceAll("-", "").trim();
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

  public static Hashtable getReviews(ObjectReference selfRef)
    throws Exception
  {
    Hashtable revise = new Hashtable();
    Enumeration enu1 = null;
    Enumeration enumeration = null;
    try
    {
      WfProcess self = (WfProcess)selfRef.getObject();
      enu1 = WfEngineHelper.service.getProcessSteps(self, null);
      Vector v = new Vector();
      while (enu1.hasMoreElements()) {
        Object obj = enu1.nextElement();
        if ((obj instanceof WfAssignedActivity)) {
          v.addElement(obj);
        } else if ((obj instanceof WfRequesterActivity)) {
          WfContainer con = ((WfRequesterActivity)obj)
            .getPerformer();
          Enumeration enu2 = null;
          enu2 = WfEngineHelper.service.getProcessSteps(con, null);
          while (enu2.hasMoreElements()) {
            Object obj2 = enu2.nextElement();
            if ((obj2 instanceof WfAssignedActivity)) {
              v.addElement(obj2);
            }
          }
        }
      }
      enumeration = v.elements();

      while (enumeration.hasMoreElements()) {
        WfAssignedActivity wfactivity = 
          (WfAssignedActivity)enumeration
          .nextElement();

        if ((wfactivity == null) || 
          (!(wfactivity instanceof WfAssignedActivity))) {
          continue;
        }
        if (!wfactivity.getState().toString()
          .equalsIgnoreCase("CLOSED_COMPLETED_EXECUTED"))
          continue;
        revise.put(wfactivity.getName(), 
          getPrincipalName(wfactivity));
      }
    }
    catch (Exception exp) {
      exp.printStackTrace();
    }

    return revise;
  }

  private static String getPrincipalName(WfActivity wfa)
    throws Exception
  {
    String field = "";
    String str = "";
    String strfull = "";
    Enumeration en1 = null;
    Enumeration en2 = null;
    en1 = ((WfAssignedActivity)wfa).getAssignments();
    String time;
    if ((wfa.getStartTime() != null) && (wfa.getEndTime() != null))
      time = WTStandardDateFormat.format(wfa.getEndTime(), "yyyy-MM-dd");
    else {
      time = "";
    }

    for (int i = 0; (en1 != null) && (en1.hasMoreElements()); i++) {
      WfAssignment wfassignment = (WfAssignment)en1.nextElement();
      en2 = wfassignment.checkBallotStatus().elements();
      for (int j = 0; (en2 != null) && (en2.hasMoreElements()); j++) {
        WfBallot wfballot = (WfBallot)en2.nextElement();

        WTPrincipal wtp = wfballot.getVoter().getPrincipal();

        if ((wtp instanceof WTUser)) {
          str = ((WTUser)wtp).getName().toString();

          strfull = ((WTUser)wtp).getFullName().toString();
          if (strfull.indexOf(",") > 0) {
            strfull = strfull.replaceFirst(",", "");
          }
          strfull = strfull.replaceAll(" ", "");
        }
        else if ((wtp instanceof WTGroup)) {
          str = ((WTGroup)wtp).getName().toString();
        }if ((str != null) && (str.length() > 0)) {
          if (field == "")
            field = strfull + ";;;" + time;
          else
            field = field + "&&&" + str + ";;;" + time;
        }
      }
    }
    return field;
  }

  public static String downloadpdf2(Representable doc) throws WTException, PropertyVetoException, IOException
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
          filename = unescape(filename);
          if (filename.indexOf(".pdf") > 0) {
            String absoluteFileName = path + filename;
            downloadFile(in, absoluteFileName);
            return absoluteFileName;
          }
        }
      }
    }
    return null;
  }

  public static ContentHolder removeAttachment(ContentHolder doc, String attachName) throws WTException, PropertyVetoException
  {
    try
    {
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

  public static void changePdfRevise(WTDocument wtdoc, ObjectReference self, Vector<String> timeVec, WTObject pbo)
  {
    try
    {
      String absoluteFileName = "";
      if (wtdoc != null) {
        absoluteFileName = downloadpdf2(wtdoc);
        if (absoluteFileName != null) {
          Hashtable revise = getSignInfo(self);
          String type = getTypeByObject(wtdoc);
          System.out.println("文档类型**********************" + type);

          Hashtable reset = new Hashtable();

          writeText(revise, type, absoluteFileName, timeVec, wtdoc, pbo);

          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        }
        else {
          ContentHolder ch = ContentHelper.service
            .getContents(wtdoc);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          for (int i = 0; i < attachmentList.size(); i++) {
            ApplicationData appDataPDF = 
              (ApplicationData)attachmentList
              .get(i);
            String fileName = appDataPDF.getFileName();
            System.out.println("============aaa============" + fileName + "---------");
            if (fileName.toLowerCase().endsWith(".pdf")) {
              int pdf = fileName.lastIndexOf("-");
              if (pdf < 0) {
                pdf = fileName.lastIndexOf(".");
              }

              absoluteFileName = path + fileName;
              ContentServerHelper.service.writeContentStream(
                appDataPDF, absoluteFileName);

              Hashtable revise = getSignInfo(self);

              String type = getTypeByObject(wtdoc);
              writeText(revise, type, absoluteFileName, timeVec, 
                wtdoc, pbo);
              ContentServerHelper.service.updateContent(ch, 
                appDataPDF, absoluteFileName);

              File pdfFile = new File(absoluteFileName);
              pdfFile.delete();
            }
          }
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (WTException e) {
      e.printStackTrace();
    } catch (PropertyVetoException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Vector<String> setTime(Vector<String> timeVec) {
    SimpleDateFormat fmDate = new SimpleDateFormat("yyyy-mm-dd");
    String str = fmDate.format(new Date());
    timeVec.add(str);
    return timeVec;
  }

  public static String getSoftType(WTObject obj) throws WTException {
    String typename = "";
    TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
    typename = type.getTypename();
    return typename;
  }

  private static void debug(String s) {
    StackTraceElement ste = new Throwable().getStackTrace()[2];
    String ss = ste.getFileName() + "." + ste.getLineNumber() + ": ";
    System.out.println(ss + s);
  }

  public static String wtDeel(String typename, String fileName) {
    String s = typename;
    String s1 = null;
    String s2 = null;
    String s3 = null;
    if ((!s.equals("�㲿��ͼ(��(���ƹ����ͼ)")) && (!s.equals("����ԭ��ͼ")) && 
      (!s.equals("l��ʾ��ͼ(����l��ͼ)")) && (!s.equals("��jͼ")) && 
      (!s.equals("�������ͼ")) && (!s.equals("����ͼ"))) {
      return s;
    }
    s1 = "autoCAD";
    s2 = tufu(fileName);
    s3 = s1 + "_" + s2;
    return s3;
  }

  public static Hashtable resetWF(Hashtable ht)
  {
    Enumeration keys = ht.keys();
    Hashtable resetTable = new Hashtable();

    while (keys.hasMoreElements()) {
      String key = (String)keys.nextElement();
      String str = (String)ht.get(key);
      String ss = null;
      if ((key.equals("编制")) || (key.equals("提交审签")) || 
        (key.equals("提交升级")))
        ss = "编制";
      else {
        ss = key;
      }
      resetTable.put(ss, str);
    }

    return resetTable;
  }

  public static void signECN(Vector<WTObject> vector, ObjectReference self, Vector<String> timeVec, WTObject pbo)
  {
    if (vector != null) {
      int size = vector.size();
      for (int i = 0; i < size; i++) {
        WTObject object = (WTObject)vector.get(i);
        if ((object instanceof WTDocument)) {
          WTDocument wtDocument = (WTDocument)object;
          docTime = wtDocument.getCreateTimestamp().toString();
          bianhao = wtDocument.getNumber();
          System.out.println("zyj--test--docTIME:" + docTime + "===bianhao:" + bianhao);
          changePdfRevise(wtDocument, self, timeVec, pbo);
        }
      }
    }
  }

  public static Vector getChangeAfters(WTObject pbo) throws Exception {
    Vector vec = new Vector();
    QueryResult qr = ChangeHelper2.service
      .getChangeActivities((ChangeOrderIfc)pbo);

    while (qr.hasMoreElements()) {
      Persistable persistable = (Persistable)qr.nextElement();
      if ((persistable instanceof WTChangeActivity2)) {
        WTChangeActivity2 caifc = (WTChangeActivity2)persistable;
        QueryResult qrObj = ChangeHelper2.service
          .getChangeablesAfter(caifc);
        while (qrObj.hasMoreElements()) {
          Object obj = qrObj.nextElement();
          if ((obj instanceof WTDocument)) {
            WTDocument wt = (WTDocument)obj;
            System.out.println("����WTDoc����ӵ�����,������" + wt.toString());
            vec.add(wt);
          } else if ((obj instanceof EPMDocument)) {
            EPMDocument epm = (EPMDocument)obj;
            System.out.println("����EPMDoc����ӵ�����,������" + 
              epm.toString());
            vec.add(epm);
          }
        }
      }
    }
    return vec;
  }

  public static void signPBO(WTObject pbo, ObjectReference self) throws MaturityException, WTException
  {
    Vector vector = new Vector();
    Vector timeVec = new Vector();
    if ((pbo instanceof WTChangeOrder2))
    {
      WTChangeOrder2 wtc = (WTChangeOrder2)pbo;
      if ((wtc != null) && ((wtc instanceof WTChangeOrder2)))
      {
        Vector changeAfter = new Vector();

        QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(wtc);

        while (qrActivities.hasMoreElements()) {
          Object objActivities = qrActivities.nextElement();

          if ((objActivities instanceof WTChangeActivity2)) {
        	  if(((WTChangeActivity2) objActivities).getName().contains(wtc.getNumber())){
        		  continue;
        	  }
            QueryResult qrAfter = ChangeHelper2.service
              .getChangeablesAfter((WTChangeActivity2)objActivities);

            while (qrAfter.hasMoreElements())
            {
              WTObject objAfter = (WTObject)qrAfter.nextElement();

              if ((objAfter instanceof WTDocument)) {
                vector.add(objAfter);
              }
              if ((objAfter instanceof EPMDocument)) {
                vector.add(objAfter);
              }
              if ((objAfter instanceof WTPart)) {
                vector.add(objAfter);
              }
            }
          }
        }
      }
    }else if(pbo instanceof WTChangeActivity2){
    	WTChangeActivity2 eca = (WTChangeActivity2) pbo;
    	QueryResult qrAfter = ChangeHelper2.service
    			.getChangeablesAfter((WTChangeActivity2)eca);

    	while (qrAfter.hasMoreElements())
    	{
    		WTObject objAfter = (WTObject)qrAfter.nextElement();

    		if ((objAfter instanceof WTDocument)) {
    			vector.add(objAfter);
    		}
    		if ((objAfter instanceof EPMDocument)) {
    			vector.add(objAfter);
    		}
    		if ((objAfter instanceof WTPart)) {
    			vector.add(objAfter);
    		}
    	}
    }

    signECN(vector, self, timeVec, pbo);
  }

  public static String getTime() {
    DateFormat df = DateFormat.getDateInstance(
      1, Locale.CHINA);

    Date date = new Date(System.currentTimeMillis());

    DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd");
    String s = formatter.format(date);

    String r = s.replace("-", ".");
    return r;
  }

  public static String downloadpdf(Representable doc) throws WTException, PropertyVetoException, IOException {
    if (doc != null) {
      ContentHolder contentHolder = ContentHelper.service.getContents(doc);
      ContentItem contentitem = ContentHelper.getPrimary((FormatContentHolder)contentHolder);

      ApplicationData applicationdata = (ApplicationData)contentitem;
      InputStream in = ContentServerHelper.service
        .findContentStream(applicationdata);
      String filename = applicationdata.getFileName();
      if ((filename.indexOf(".xls") > 0) || (filename.indexOf(".xlsx") > 0) || (filename.indexOf(".XLS") > 0) || (filename.indexOf(".XLSX") > 0))
      {
        String absoluteFileName = path + filename;
        downloadFile(in, absoluteFileName);
        return absoluteFileName;
      }
    }
    return null;
  }

  public static void downloadFile(InputStream in, String path) throws FileNotFoundException
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
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("download is completed!");
  }

  public static String CheckOut(WTObject obj) {
    String errorlog = "";
    try {
      if (WorkInProgressHelper.isCheckedOut((Workable)obj))
        errorlog = errorlog + "对象" + obj.getIdentity() + "处于检出状态,因此不能提交.";
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return errorlog;
  }

  public static void updatePromotionTargets(WTObject pbo)
    throws MaturityException, WTException, WTPropertyVetoException
  {
    Transaction trx = new Transaction();
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try
    {
      trx.start();
      WTSet new_set = new WTHashSet();
      WTSet new_set2 = new WTHashSet();
      if ((pbo instanceof PromotionNotice))
      {
        PromotionNotice pn = (PromotionNotice)pbo;
        MaturityBaseline baseline = pn.getConfiguration();
        QueryResult qr = MaturityHelper.service.getPromotionTargets(pn);
        while (qr.hasMoreElements())
        {
          WTObject obj = (WTObject)qr.nextElement();
          new_set.add(obj);
        }
        QueryResult qr2 = MaturityHelper.service.getBaselineItems(pn);
        while (qr2.hasMoreElements())
        {
          WTObject obj = (WTObject)qr2.nextElement();
          new_set2.add(obj);
        }
        MaturityHelper.service.deletePromotionTargets(pn, new_set);
        BaselineHelper.service.removeFromBaseline(new_set2, baseline);

        MaturityHelper.service.savePromotionTargets(pn, new_set2);
        BaselineHelper.service.addToBaseline(new_set2, baseline);

        new_set = null;
        trx.commit();
        trx = null;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      trx.rollback();
      trx = null;
    }
    finally {
      SessionServerHelper.manager.setAccessEnforced(access);
    }
  }

  public static String getType(WTObject obj) throws WTException, UnsupportedEncodingException {
    String softAttr = "";
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 eco = (WTChangeOrder2)obj;
      IBAUtility ibUtil = new IBAUtility(eco);
      softAttr = ibUtil.getIBAValue("GENGGAILEIXING");
    }
    return softAttr;
  }

  public static String unescape(String s) {
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
  public static Map getUserGroup(Properties properties) {
    Map user_group = new HashMap();
    try {
      Set grp = zuMap.keySet();
      List group_names = new ArrayList();
      group_names.addAll(grp);
      QuerySpec qs = new QuerySpec(WTGroup.class);
      SearchCondition sc = new SearchCondition(WTGroup.class, "name", 
        "LIKE", "%接收组%");
      qs.appendWhere(sc);

      System.out.println("zyj--test--group.size:" + group_names.size());
      for (int i = 0; i < group_names.size(); i++) {
        sc = new SearchCondition(WTGroup.class, "name", 
          "=", (String)group_names.get(i));
        qs.appendOr();
        qs.appendWhere(sc);
      }
      QueryResult qr = PersistenceHelper.manager.find(qs);

      while (qr.hasMoreElements()) {
        WTGroup wtgroup = (WTGroup)qr.nextElement();
        Enumeration em = OrganizationServicesHelper.manager.members(wtgroup);
        while (em.hasMoreElements()) {
          Object user = em.nextElement();
          if ((user instanceof WTUser)) {
            System.out.println("zyj==test==wtgroup.getName():" + wtgroup.getName());
            System.out.println("zyj==test==properties.get(wtgroup.getName()):" + zuMap.get(wtgroup.getName()));
            user_group.put(((WTUser)user).getAuthenticationName(), zuMap.get(wtgroup.getName()));
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return user_group;
  }

  public static Hashtable getReviews(WfProcess self, Properties properties) throws Exception {
    String str = new String();
    Hashtable revise = new Hashtable();
    Enumeration enu1 = null;
    Enumeration enumeration = null;
    if (self == null) {
      return revise;
    }
    try
    {
      Map user_group = getUserGroup(properties);
      QueryResult qr = NmWorkflowHelper.service.getVotingEventsForProcess(self);
      while (qr.hasMoreElements()) {
        WfVotingEventAudit wfvotingeventaudit = (WfVotingEventAudit)qr.nextElement();
        String activityName = wfvotingeventaudit.getActivityName();

        WTPrincipalReference wtprincipalreference = wfvotingeventaudit.getUserRef();
        WTPrincipal wtprincipal = (WTPrincipal)wtprincipalreference.getObject();
        String userName = null;
        String name = null;
        if ((wtprincipal instanceof WTUser)) {
          userName = ((WTUser)wtprincipal).getFullName();
          name = ((WTUser)wtprincipal).getName();
        }
        else if ((wtprincipal instanceof WTGroup)) {
          userName = ContainerTeamHelper.getDisplayName((WTGroup)wtprincipal, null);
        }
        Timestamp timestamp = wfvotingeventaudit.getTimestamp();

        if (revise.get(activityName) != null) {
          Vector nameVector = (Vector)revise.get(activityName);
          nameVector.add(name + ";;" + userName.replaceAll(",", "").replaceAll(" ", "") + 
            "__" + timestamp + "_=_" + user_group.get(((WTUser)wtprincipal).getAuthenticationName()) + "_==_" + " ");
          revise.put(activityName, nameVector);
        } else {
          Vector nameVector = new Vector();
          nameVector.add(name + ";;" + userName.replaceAll(",", "").trim().replaceAll(" ", "") + 
            "__" + timestamp + "_=_" + user_group.get(((WTUser)wtprincipal).getAuthenticationName()) + "_==_" + " ");
          revise.put(activityName, nameVector);
        }
      }
    } catch (Exception exp) {
      exp.printStackTrace();
    }

    return revise;
  }
  public static WfProcess getProcess(Persistable persistable) throws Exception {
    Enumeration eu2 = WfEngineHelper.service.getAssociatedProcesses(persistable, WfState.OPEN_RUNNING);
    Enumeration eu = WfEngineHelper.service.getAssociatedProcesses(persistable, WfState.CLOSED_COMPLETED);
    boolean isApproved = false;
    WfProcess last_process = null;
    WfProcess process = null;
    if (eu.hasMoreElements()) {
      process = (WfProcess)eu.nextElement();
      return process;
    }
    if ((persistable instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)persistable;
      QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(ecn);
      while (qrActivities.hasMoreElements()) {
        Object objActivities = qrActivities.nextElement();

        if ((objActivities instanceof WTChangeActivity2)) {
          QueryResult qrAfter = ChangeHelper2.service
            .getChangeablesAfter((WTChangeActivity2)objActivities);

          while (qrAfter.hasMoreElements())
          {
            WTObject objAfter = (WTObject)qrAfter.nextElement();
            if ((objAfter instanceof WTDocument)) {
              WTDocument doc = (WTDocument)objAfter;
              if (doc.getLifeCycleState().toString().equals("APPROVED")) {
                isApproved = true;
                break;
              }
            }
          }
        }
      }
      if ((eu2.hasMoreElements()) && (isApproved)) {
        process = (WfProcess)eu2.nextElement();
        return process;
      }
    }
    System.out.println("zyj==test==process:" + process);
    return process;
  }
}