package ext.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.enterprise.change2.commands.RelatedChangesQueryCommands;
import ext.tzc.tasv.change.fileprint.SignWFUtil;
import ext.util.IBAUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrderIfc;
import wt.change2.ChangeService2;
import wt.change2.Changeable2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.epm.EPMAuthoringAppType;
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
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.PromotionNotice;
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
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.vc.VersionIdentifier;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.BaselineService;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfRequesterActivity;
import wt.workflow.engine.WfState;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignment;
import wt.workflow.work.WfBallot;

public class SignPDF_lg
{
  static float hqspace = 5.5F;

  static float cc = 2.834381F;

  static float cy = 2.835017F;
  private static String path;
  private static String wthome;
  private static Image jpeg;
  private static String cStatus = "";

  private static String bianhao = "";

  private static String riqi = "";

  private static String version = "";

  static
  {
    try {
      WTProperties wtproperties = WTProperties.getLocalProperties();
      wthome = wtproperties.getProperty("wt.home");
      path = wthome + "/temp/";
    } catch (Throwable throwable) {
      throw new ExceptionInInitializerError(throwable);
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

  private static void writeText(PdfContentByte cb, BaseFont bf, Hashtable ht, Hashtable htrevise, Vector<String> timeVec, String filetype)
    throws DocumentException, IOException, IOException
  {
    Enumeration keys = ht.keys();
    String timeFlag = "";
    while (keys.hasMoreElements()) {
      System.out.println("keys:" + keys);

      String key = (String)keys.nextElement();
      Hashtable section = (Hashtable)ht.get(key);

      float x = Float.parseFloat((String)section.get("x"));
      float y = Float.parseFloat((String)section.get("y"));
      float chang = Float.parseFloat((String)section.get("chang"));
      float kuan = Float.parseFloat((String)section.get("kuan"));
      float datex = Float.parseFloat((String)section.get("datex"));
      float datey = Float.parseFloat((String)section.get("datey"));
      float fontsize = Float.parseFloat((String)section.get("fontsize"));
      float rotation = Float.parseFloat((String)section.get("rotation"));

      float yearx = Float.parseFloat((String)section.get("yearx"));
      float yeary = Float.parseFloat((String)section.get("yeary"));
      float monthx = Float.parseFloat((String)section.get("monthx"));
      float monthy = Float.parseFloat((String)section.get("monthy"));
      float dayx = Float.parseFloat((String)section.get("dayx"));
      float dayy = Float.parseFloat((String)section.get("dayy"));
      System.out.println("zyj--test--key:" + key);
      if ((htrevise.get(key) != null) && (!"".equals(htrevise.get(key)))) {
        String usertime = (String)htrevise.get(key);
        System.out.println("usertime:" + usertime + "=============");
        usertime = usertime.replaceAll(";;;", " ").replaceAll("&&&", 
          " ");
        int num1 = usertime.indexOf(" ");
        String name = usertime.substring(0, num1);
        if ((filetype.indexOf("com.tasv.tasvdoc07") != -1) || (filetype.indexOf("com.tasv.tasvdoc08") != -1) || (filetype.indexOf("com.tasv.tasvdoc09") != -1) || (filetype.indexOf("com.tasv.tasvdoc10") != -1) || (filetype.indexOf("com.tasv.tasvdoc11") != -1)) {
          name = SignWFUtil.getChangeValue(usertime);
        }
        String picturePath = null;
        String time = usertime.substring(usertime.length() - 10, 
          usertime.length());
        try {
          WTProperties wtproperties = 
            WTProperties.getLocalProperties();
          picturePath = wtproperties.getProperty("wt.home") + 
            "/codebase/ext/pdf/blank";
        } catch (IOException e) {
          e.printStackTrace();
        }

        if ((datex != 0.0F) && (datey != 0.0F)) {
          System.out.println("zyj--test--连签================");
          time = time.replaceAll("-", "");
          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, name, x * cc, y * cy, rotation);
          cb.showTextAligned(0, time, datex * cc, datey * cy, rotation);
          cb.endText();
        } else {
          System.out.println("============默认=============");
          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, name, x * cc, y * cy, rotation);
          cb.endText();
        }
      } else {
        System.out.println("==========zyj--else==========");
        if (key.equalsIgnoreCase("TYBJ")) {
          System.out.println("zyj--test--TYBJ==========");
          cStatus = cStatus.substring(cStatus.length() - 1);
          if ((!"".equals(cStatus)) && ("C".equals(cStatus))) {
            cb.beginText();
            cb.setFontAndSize(bf, fontsize);
            cb.showTextAligned(0, cStatus, x * cc, y * cy, rotation);
            cb.endText();
          } else if ((!"".equals(cStatus)) && ("S".equals(cStatus))) {
            cb.beginText();
            cb.setFontAndSize(bf, fontsize);
            cb.showTextAligned(0, cStatus, Float.parseFloat(((x + 10.0D) * cc)+""), y * cy, rotation);
            cb.endText();
          }
        }

        if (key.equalsIgnoreCase("BIANHAO")) {
          System.out.println("zyj--test--TYBJ==========");
          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, bianhao, x * cc, y * cy, rotation);
          cb.endText();
        }
        if (key.equalsIgnoreCase("TIME")) {
          System.out.println("zyj--test--TYBJ==========");
          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, riqi, x * cc, y * cy, rotation);
          cb.endText();
        }
        if (key.equalsIgnoreCase("VERSION")) {
          System.out.println("zyj--test--VERSION==========" + version);
          cb.beginText();
          cb.setFontAndSize(bf, fontsize);
          cb.showTextAligned(0, version, x * cc, y * cy, rotation);
          cb.endText();
        }
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

  public static void writeText(Hashtable htrevise, String filetype, String fileName, Vector<String> timeVec, WTDocument wtdoc)
  {
    try {
      String oldfileName = fileName;
      Hashtable hts = InitXml();
      String writeType = "";
      System.out.println("zyj--test--filetype:" + filetype + "==============");
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc03")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
//      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("bzjmxb")) {
//        writeType = filetype.substring(filetype.length() - 6);
//      }
//      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("wgjmxb")) {
//        writeType = filetype.substring(filetype.length() - 6);
//      }
//      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("zcmxb")) {
//        writeType = filetype.substring(filetype.length() - 5);
//      }
//      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("fzmxb")) {
//        writeType = filetype.substring(filetype.length() - 5);
//      }

      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc05")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc07")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc08")) {
        IBAUtil ibUtil = new IBAUtil(wtdoc);
        String wenjianfenlei = ibUtil.getIBAValue("WJFL");
        System.out.println("wenjianfenlei:" + wenjianfenlei + "========================");
        writeType = wenjianfenlei;
      }
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc09")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc10")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
      if (filetype.substring(filetype.length() - 9).equalsIgnoreCase("tasvdoc11")) {
        writeType = filetype.substring(filetype.length() - 9);
      }
      System.out.println("zyj--test--writeType:" + writeType);
      if (writeType == null) {
        return;
      }
      Hashtable htf = (Hashtable)hts.get(writeType);

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
        BaseFont bfnew = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", "Identity-H", 
          true);
        cbnew.setFontAndSize(bfnew, 12.0F);
        Hashtable htpOthers = (Hashtable)htf.get("others");
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
      String outfilename = path + "temp_" + "new_" + fileName.substring(ii + 1);
      System.out.println("zyj--test--outfilename:" + outfilename + "==================");
      PdfWriter writer = PdfWriter.getInstance(document, 
        new FileOutputStream(outfilename));
      document.open();

      PdfContentByte cb = writer.getDirectContent();

      BaseFont bf = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", "Identity-H", 
        true);
      cb.setFontAndSize(bf, 12.0F);
      Hashtable htpOthers = (Hashtable)htf.get("others");
      for (int i = 1; i <= pages; i++) {
        Hashtable htp = (Hashtable)htf.get(String.valueOf(i));
        System.out.println("zyj--test--htp:" + htp + "============");
        if (htp != null) {
          System.out.println("zyj--test--第一页");
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeText(cb, bf, htp, htrevise, timeVec, filetype);
          document.newPage();
        } else if (htpOthers != null) {
          System.out.println("zyj--test--多页");
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeText(cb, bf, htpOthers, htrevise, timeVec, filetype);
          document.newPage();
        } else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);

          document.newPage();
        }
      }

      document.close();
      File pdfFileold = new File(oldfileName);
      File pdfFile = new File(fileName);
      pdfFileold.delete();
      pdfFile.delete();

      File tempPdfFile = new File(outfilename);
      tempPdfFile.renameTo(pdfFileold);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getSoftAtribute(WTObject obj) {
    String wenjianfenlei = "";
    if ((obj instanceof WTDocument)) {
      System.out.println("zyj--test--WTDocument====getSoftAtribute============");
      WTDocument doc = (WTDocument)obj;
      try {
        IBAUtil ibUtil = new IBAUtil(doc);
        wenjianfenlei = ibUtil.getIBAValue("WJFL");
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    System.out.println("wenjianfenlei:" + wenjianfenlei + "************");
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
    String A31L = "A31L";
    String A31_25L = "A31.25L";
    String A31_5L = "A31.5L";
    String A31_75L = "A31.75L";
    String A32L = "A32L";
    String A32_25L = "A32.25L";
    String A32_5L = "A32.5L";
    String A32_75L = "A32.75L";
    String A33L = "A33L";
    String A33_25L = "A33.25L";
    String A33_5L = "A33.5L";
    String A33_75L = "A33.75L";
    String A34L = "A34L";
    String A34_25L = "A34.25L";
    String A34_5L = "A34.5L";
    String A34_75L = "A34.75L";
    String A35L = "A35L";
    String A35_25L = "A35.25L";
    String A35_5L = "A35.5L";
    String A35_75L = "A35.75L";
    String A36L = "A36L";
    String A36_25L = "A36.25L";
    String A36_5L = "A36.5L";
    String A36_75L = "A36.75L";
    String A37L = "A37L";
    String A37_25L = "A37.25L";
    String A37_5L = "A37.5L";
    String A37_75L = "A37.75L";
    String A38L = "A38L";
    String A38_25L = "A38.25L";
    String A38_5L = "A38.5L";
    String A38_75L = "A38.75L";
    String A39L = "A39L";
    String A39_25L = "A39.25L";
    String A39_5L = "A39.5L";
    String A39_75L = "A39.75L";
    String A310L = "A310L";
    String A310_25L = "A310.25L";
    String A310_5L = "A310.5L";
    String A310_75L = "A310.75L";
    String A311L = "A311L";
    String A311_25L = "A311.25L";
    String A311_5L = "A311.5L";
    String A311_75L = "A311.75L";
    String A312L = "A312L";
    String A21L = "A21L";
    String A21_25L = "A21.25L";
    String A21_5L = "A21.5L";
    String A21_75L = "A21.75L";
    String A22L = "A22L";
    String A22_25L = "A22.25L";
    String A22_5L = "A22.5L";
    String A22_75L = "A22.75L";
    String A23L = "A23L";
    String A23_25L = "A23.25L";
    String A23_5L = "A23.5L";
    String A23_75L = "A23.75L";
    String A24L = "A24L";
    String A24_25L = "A24.25L";
    String A24_5L = "A24.5L";
    String A24_75L = "A24.75L";
    String A25L = "A25L";
    String A25_25L = "A25.25L";
    String A25_5L = "A25.5L";
    String A25_75L = "A25.75L";
    String A26L = "A26L";
    String A26_25L = "A26.25L";
    String A26_5L = "A26.5L";
    String A26_75L = "A26.75L";
    String A27L = "A27L";
    String A27_25L = "A27.25L";
    String A27_5L = "A27.5L";
    String A27_75L = "A27.75L";
    String A28L = "A28L";
    String A28_25L = "A28.25L";
    String A28_5L = "A28.5L";
    String A28_75L = "A28.75L";
    String A29L = "A29L";
    String A11L = "A11L";
    String A11_25L = "A11.25L";
    String A11_5L = "A11.5L";
    String A11_75L = "A11.75L";
    String A12L = "A12L";
    String A12_25L = "A12.25L";
    String A12_5L = "A12.5L";
    String A12_75L = "A12.75L";
    String A13L = "A13L";
    String A13_25L = "A13.25L";
    String A13_5L = "A13.5L";
    String A13_75L = "A13.75L";
    String A14L = "A14L";
    String A14_25L = "A14.25L";
    String A14_5L = "A14.5L";
    String A14_75L = "A14.75L";
    String A15L = "A15L";
    String A15_25L = "A15.25L";
    String A15_5L = "A15.5L";
    String A15_75L = "A15.75L";
    String A16L = "A16L";
    String A01L = "A01L";
    String A01_25L = "A01.25L";
    String A01_5L = "A01.5L";
    String A01_75L = "A01.75L";
    String A02L = "A02L";
    String A02_25L = "A02.25L";
    String A02_5L = "A02.5L";
    String A02_75L = "A02.75L";
    String A03L = "A03L";
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
      if ((width > 3368.0F) && (width < 3400.0F) && (height > 2378.0F) && (height < 2408.0F)) {
        System.out.println("tufu: - -! A0H");
        return a0h;
      }

      if ((width > 2378.0F) && (width < 2428.0F) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A1H");
        return a1h;
      }

      if ((width > 1675.0F) && (width < 1725.0F) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A2H");
        return a2h;
      }
      if ((width > 1180.0F) && (width < 1220.0F) && (height > 832.0F) && (height < 852.0F)) {
        System.out.println("tufu: - -! A3H");
        return a3h;
      }if ((582.0D < width) && (width < 620.0D) && (height > 832.0D) && (height < 852.0D)) {
        System.out.println("tufu: - -! A4s");
        return a4s;
      }if ((832.0D < width) && (width < 862.0D) && (height > 582.0F) && (height < 612.0F)) {
        System.out.println("tufu: - -! A4h");
        return a4h;
      }if ((4750.0600000000004D < width) && (width < 4780.0600000000004D) && (height > 3350.8699999999999D) && (height < 3379.8699999999999D)) {
        System.out.println("tufu: - -! A02");
        return a02;
      }if ((7130.0900000000001D < width) && (width < 7170.0900000000001D) && (height > 3350.8699999999999D) && (height < 3379.8699999999999D)) {
        System.out.println("tufu: - -! A03");
        return a03;
      }if ((5030.8900000000003D < width) && (width < 5080.8900000000003D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A13");
        return a13;
      }if ((6710.7399999999998D < width) && (width < 6759.7399999999998D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A14");
        return a14;
      }if ((3550.6300000000001D < width) && (width < 3600.6300000000001D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A23");
        return a23;
      }if ((4750.0600000000004D < width) && (width < 4799.0600000000004D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A24");
        return a24;
      }if ((5930.6599999999999D < width) && (width < 5979.6599999999999D) && (height > 1671.02D) && (height < 1699.02D)) {
        System.out.println("tufu: - -! A25");
        return a25;
      }if ((2511.5300000000002D < width) && (width < 2559.5300000000002D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A33");
        return a33;
      }if ((3350.8699999999999D < width) && (width < 3399.8699999999999D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A34");
        return a34;
      }if ((4190.3800000000001D < width) && (width < 4239.3800000000001D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A35");
        return a35;
      }if ((5030.8900000000003D < width) && (width < 5079.8900000000003D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A36");
        return a36;
      }if ((5870.3999999999996D < width) && (width < 5910.3999999999996D) && (height > 1170.5999999999999D) && (height < 1199.5999999999999D)) {
        System.out.println("tufu: - -! A37");
        return a37;
      }if ((1770.9000000000001D < width) && (width < 1810.9000000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A43");
        return a43;
      }if ((2365.0300000000002D < width) && (width < 2410.0300000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A44");
        return a44;
      }if ((2960.3299999999999D < width) && (width < 2999.9899999999998D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A45");
        return a45;
      }if ((3550.6300000000001D < width) && (width < 3599.6300000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A46");
        return a46;
      }if ((4150.9300000000003D < width) && (width < 4199.9300000000003D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A47");
        return a47;
      }if ((4750.0600000000004D < width) && (width < 4799.0600000000004D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A48");
        return a48;
      }if ((5340.3599999999997D < width) && (width < 5389.3599999999997D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A49");
        return a49;
      }if ((1470.5999999999999D < width) && (width < 1499.0F) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A31.25L");
        return A31_25L;
      }if ((1770.0F < width) && (width < 1799.0F) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A31.5L");
        return A31_5L;
      }if ((2070.0500000000002D < width) && (width < 2099.0500000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A31.75L");
        return A31_75L;
      }if ((2660.3499999999999D < width) && (width < 2689.3499999999999D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A32.25L");
        return A32_25L;
      }if ((3250.6500000000001D < width) && (width < 3279.6500000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A32.75L");
        return A32_75L;
      }if ((3850.9499999999998D < width) && (width < 3879.9499999999998D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A33.25L");
        return A33_25L;
      }if ((4440.25D < width) && (width < 4469.25D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A33.75L");
        return A33_75L;
      }if ((5041.5500000000002D < width) && (width < 5069.5500000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A34.25L");
        return A34_25L;
      }if ((5635.8500000000004D < width) && (width < 5659.8500000000004D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A34.75L");
        return A34_75L;
      }if ((5933.0F < width) && (width < 5959.0F) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A35L");
        return A35L;
      }if ((6230.1499999999996D < width) && (width < 6259.1499999999996D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A35.25L");
        return A35_25L;
      }if ((6527.3000000000002D < width) && (width < 6549.3000000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A35.5L");
        return A35_5L;
      }if ((6824.4499999999998D < width) && (width < 6849.4499999999998D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A35.75L");
        return A35_75L;
      }if ((7121.6000000000004D < width) && (width < 7149.6000000000004D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A36L");
        return A36L;
      }if ((7418.75D < width) && (width < 7449.75D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A36.25L");
        return A36_25L;
      }if ((7715.8999999999996D < width) && (width < 7739.8999999999996D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A36.5L");
        return A36_5L;
      }if ((8013.0500000000002D < width) && (width < 8039.0500000000002D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A36.75L");
        return A36_75L;
      }if ((8310.2000000000007D < width) && (width < 8339.2000000000007D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A37L");
        return A37L;
      }if ((8607.3500000000004D < width) && (width < 8639.3500000000004D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A37.25L");
        return A37_25L;
      }if ((8904.5D < width) && (width < 8929.5D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A37.5L");
        return A37_5L;
      }if ((9201.6499999999996D < width) && (width < 9229.6499999999996D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A37.75L");
        return A37_75L;
      }if ((9490.7999999999993D < width) && (width < 9518.7999999999993D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A38L");
        return A38L;
      }if ((9790.9500000000007D < width) && (width < 9819.9500000000007D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A38.25L");
        return A38_25L;
      }if ((10090.1D < width) && (width < 10119.1D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A38.5L");
        return A38_5L;
      }if ((10390.25D < width) && (width < 10419.25D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A38.75L");
        return A38_75L;
      }if ((10680.4D < width) && (width < 10707.4D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A39L");
        return A39L;
      }if ((10980.549999999999D < width) && (width < 11004.549999999999D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A39.25L");
        return A39_25L;
      }if ((11281.700000000001D < width) && (width < 11300.700000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A39.5L");
        return A39_5L;
      }if ((11578.85D < width) && (width < 11598.85D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A39.75L");
        return A39_75L;
      }if ((11876.0F < width) && (width < 11899.0F) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A310L");
        return A310L;
      }if ((12173.15D < width) && (width < 12199.15D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A310.25L");
        return A310_25L;
      }if ((12470.299999999999D < width) && (width < 12499.299999999999D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A310.5L");
        return A310_5L;
      }if ((12767.450000000001D < width) && (width < 12789.450000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A310.75L");
        return A310_75L;
      }if ((13064.6D < width) && (width < 13089.6D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A311L");
        return A311L;
      }if ((13361.75D < width) && (width < 13389.75D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A311.25L");
        return A311_25L;
      }if ((13658.9D < width) && (width < 13679.9D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A311.5L");
        return A311_5L;
      }if ((13956.049999999999D < width) && (width < 13979.049999999999D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A311.75L");
        return A311_75L;
      }if ((14250.200000000001D < width) && (width < 14279.200000000001D) && (height > 825.50999999999999D) && (height < 859.50999999999999D)) {
        System.out.println("tufu: - -! A312L");
        return A312L;
      }if ((width > 2090.2750000000001D) && (width < 2119.2750000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A21.25L");
        return A21_25L;
      }if ((width > 2931.7849999999999D) && (width < 2959.7849999999999D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A21.75L");
        return A21_75L;
      }if ((width > 3772.2950000000001D) && (width < 3799.2950000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A22.25L");
        return A22_25L;
      }if ((width > 4612.8050000000003D) && (width < 4639.8050000000003D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A22.75L");
        return A22_75L;
      }if ((width > 5453.3149999999996D) && (width < 5479.3149999999996D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A23.25L");
        return A23_25L;
      }if ((width > 6293.8249999999998D) && (width < 6319.8249999999998D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A23.75L");
        return A23_75L;
      }if ((width > 6714.0799999999999D) && (width < 6739.0799999999999D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A24L");
        return A24L;
      }if ((width > 7134.335D) && (width < 7159.335D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A24.25L");
        return A24_25L;
      }if ((width > 7554.5900000000001D) && (width < 7579.5900000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A24.5L");
        return A24_5L;
      }if ((width > 7974.8450000000003D) && (width < 7999.8450000000003D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A24.75L");
        return A24_75L;
      }if ((width > 8395.1000000000004D) && (width < 8419.1000000000004D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A25L");
        return A25L;
      }if ((width > 8815.3549999999996D) && (width < 8839.3549999999996D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A25.25L");
        return A25_25L;
      }if ((width > 9235.6100000000006D) && (width < 9259.6100000000006D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A25.5L");
        return A25_5L;
      }if ((width > 9655.8649999999998D) && (width < 9679.8649999999998D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A25.75L");
        return A25_75L;
      }if ((width > 10070.120000000001D) && (width < 10099.120000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A26L");
        return A26L;
      }if ((width > 10496.375D) && (width < 10519.375D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A26.25L");
        return A26_25L;
      }if ((width > 10916.629999999999D) && (width < 10939.629999999999D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A26.5L");
        return A26_5L;
      }if ((width > 11336.885D) && (width < 11359.885D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A26.75L");
        return A26_75L;
      }if ((width > 11750.139999999999D) && (width < 11777.139999999999D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A27L");
        return A27L;
      }if ((width > 12170.395D) && (width < 12199.395D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A27.25L");
        return A27_25L;
      }if ((width > 12597.65D) && (width < 12619.65D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A27.5L");
        return A27_5L;
      }if ((width > 13010.905000000001D) && (width < 13039.905000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A27.75L");
        return A27_75L;
      }if ((width > 13430.16D) && (width < 13459.16D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A28L");
        return A28L;
      }if ((width > 13850.415000000001D) && (width < 13879.415000000001D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A28.25L");
        return A28_25L;
      }if ((width > 14270.67D) && (width < 14299.67D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A28.5L");
        return A28_5L;
      }if ((width > 14690.924999999999D) && (width < 14719.924999999999D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A28.75L");
        return A28_75L;
      }if ((width > 15110.18D) && (width < 15139.18D) && (height > 1183.0F) && (height < 1213.0F)) {
        System.out.println("tufu: - -! A29L");
        return A29L;
      }if ((width > 2960.0374999999999D) && (width < 2989.0374999999999D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A11.25L");
        return A11_25L;
      }if ((width > 4150.0524999999998D) && (width < 4179.0524999999998D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A11.75L");
        return A11_75L;
      }if ((width > 5340.0675000000001D) && (width < 5369.0675000000001D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A12.25L");
        return A12_25L;
      }if ((width > 6530.0825000000004D) && (width < 6559.0825000000004D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A12.75L");
        return A12_75L;
      }if ((width > 7130.0900000000001D) && (width < 7159.0900000000001D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A13L");
        return A13L;
      }if ((width > 7720.0974999999999D) && (width < 7749.0974999999999D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A13.25L");
        return A13_25L;
      }if ((width > 8320.1049999999996D) && (width < 8349.1049999999996D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A13.5L");
        return A13_5L;
      }if ((width > 8910.1124999999993D) && (width < 8939.1124999999993D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A13.75L");
        return A13_75L;
      }if ((width > 9510.1200000000008D) && (width < 9539.1200000000008D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A14L");
        return A14L;
      }if ((width > 10105.127500000001D) && (width < 10129.127500000001D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A14.25L");
        return A14_25L;
      }if ((width > 10700.135D) && (width < 10729.135D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A14.5L");
        return A14_5L;
      }if ((width > 11295.1425D) && (width < 11319.1425D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A14.75L");
        return A14_75L;
      }if ((width > 11890.15D) && (width < 11919.15D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A15L");
        return A15L;
      }if ((width > 12480.157499999999D) && (width < 12509.157499999999D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A15.25L");
        return A15_25L;
      }if ((width > 13080.165000000001D) && (width < 13109.165000000001D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A15.5L");
        return A15_5L;
      }if ((width > 13670.172500000001D) && (width < 13699.172500000001D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A15.75L");
        return A15_75L;
      }if ((width > 14270.18D) && (width < 14299.18D) && (height > 1675.0F) && (height < 1705.0F)) {
        System.out.println("tufu: - -! A16L");
        return A16L;
      }if ((3350.8699999999999D < width) && (width < 3379.8699999999999D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A01L");
        return A01L;
      }if ((4196.0879999999997D < width) && (width < 4219.0879999999997D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A01.25L");
        return A01_25L;
      }if ((5870.5230000000001D < width) && (width < 5899.5230000000001D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A01.75L");
        return A01_75L;
      }if ((7560.9579999999996D < width) && (width < 7589.9579999999996D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A02.25L");
        return A02_25L;
      }if ((8402.1749999999993D < width) && (width < 8429.1749999999993D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A02.5L");
        return A02_5L;
      }if ((9243.393D < width) && (width < 9269.393D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A02.75L");
        return A02_75L;
      }if ((10084.610000000001D < width) && (width < 10104.610000000001D) && (height > 2370.0300000000002D) && (height < 2399.0300000000002D)) {
        System.out.println("tufu: - -! A03L");
        return A03L;
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

  public static void writeTextEPM(Hashtable htrevise, String fileType, String fileName, Vector<String> timeVec)
  {
    try {
      PdfReader reader = new PdfReader(
        fileName);
      int pages = reader.getNumberOfPages();
      Rectangle psize2 = reader.getPageSize(1);
      float height2 = psize2.getHeight();
      float width2 = psize2.getWidth();
      System.out.println("height2:" + height2 + "====width2:" + width2 + "===================");
      Rectangle psize = reader.getPageSizeWithRotation(1);
      float height = psize.getHeight();
      float width = psize.getWidth();
      System.out.println("height:" + height + "====width:" + width + "===================");

      String writeType = "";
      if (width == width2) {
        System.out.println("=========未转==============");
        writeType = "EPMDOCUMENT_" + fileType + tufu(fileName);
      } else {
        System.out.println("=========旋转==============");
      }
      System.out.println("zyj--test--writeType:" + writeType);
      System.out.println("fileType:" + fileType + "===tufu:" + tufu(fileName) + "================");
      Hashtable hts = InitXml();
      Hashtable htf = (Hashtable)hts.get(writeType);
      System.out.println("类型:" + writeType + "================");
      Rectangle rectPageSize = new Rectangle(width2, height2);
      System.out.println("Float.toString(width):" + Float.toString(width) + "Float.toString(height):" + Float.toString(height) + "================");
      com.lowagie.text.Document document = new com.lowagie.text.Document(
        rectPageSize);

      int ii = fileName.lastIndexOf("/");
      String outfilename = path + "temp_" + fileName.substring(ii + 1);
      System.out.println("outfilename:" + outfilename + "===================");
      PdfWriter writer = PdfWriter.getInstance(document, 
        new FileOutputStream(outfilename));
      document.open();

      PdfContentByte cb = writer.getDirectContent();

      BaseFont bf = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", "Identity-H", 
        true);

      cb.setFontAndSize(bf, 10.0F);
      Hashtable htpOthers = (Hashtable)htf.get("others");
      for (int i = 1; i <= pages; i++) {
        Hashtable htp = (Hashtable)htf.get(String.valueOf(i));
        if (htp != null) {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeText(cb, bf, htp, htrevise, timeVec, fileType);
          document.newPage();
        } else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeText(cb, bf, htpOthers, htrevise, timeVec, fileType);
          document.newPage();
        }
      }
      document.close();
      File pdfFile = new File(fileName);
      pdfFile.delete();

      File tempPdfFile = new File(outfilename);
      tempPdfFile.renameTo(pdfFile);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Hashtable getReviews(ObjectReference selfRef) throws Exception
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

  public static void changePdfRevise(WTChangeOrder2 wtc, ObjectReference self, Vector<String> timeVec)
  {
    try {
      ContentHolder ch = ContentHelper.service
        .getContents(wtc);
      Vector attachmentList = 
        ContentHelper.getApplicationData(ch);

      IBAUtil ibautil = new IBAUtil(wtc);
      String typename = ibautil.getIBAValue("�������").trim();

      if (typename != null)
        System.out.println("1+" + typename.toString() + "2=3");
      else {
        System.out.println("typename");
      }
      String jd = ibautil.getIBAValue("��ǰ�׶�");
      typename = "change";

      for (int i = 0; i < attachmentList.size(); i++) {
        ApplicationData appDataPDF = 
          (ApplicationData)attachmentList
          .get(i);

        String fileName = appDataPDF.getFileName();
        if (!fileName.toLowerCase().endsWith(".pdf"))
          continue;
        String absoluteFileName = path + fileName;
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          absoluteFileName);
        Hashtable revise = getReviews(self);

        Hashtable reset = new Hashtable();
        reset = resetWF(revise);
        writeText(reset, typename, absoluteFileName, timeVec, null);

        ApplicationData returnAppData = ContentServerHelper.service
          .updateContent(ch, appDataPDF, absoluteFileName);

        File pdfFile = new File(absoluteFileName);
        pdfFile.delete();
      }
    }
    catch (FileNotFoundException e)
    {
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

  public static String downloadpdf2(Representable doc) throws WTException, PropertyVetoException, IOException
  {
    Representation representation = RepresentationHelper.service
      .getDefaultRepresentation(doc);
    System.out.println("doc:" + doc.getClass().getName() + "==================");
    System.out.println("representation:" + representation + "==================");
    if (representation != null) {
      ContentHolder ch = ContentHelper.service
        .getContents(doc);
      representation = (Representation)ContentHelper.service
        .getContents(representation);
      System.out.println("representation2:" + representation + "==================");
      Vector vector1 = ContentHelper.getContentList(representation);
      System.out.println("vector1.size:" + vector1.size() + "========================");
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        System.out.println("contentitem:" + contentitem + "=================");
        if ((contentitem instanceof ApplicationData)) {
          System.out.println("&&&&&&&&&&&&&&&&&&&&&&");
          ApplicationData applicationdata = (ApplicationData)contentitem;
          System.out.println("=============applicationdata:" + applicationdata);
          InputStream in = ContentServerHelper.service
            .findContentStream(applicationdata);
          String filename = applicationdata.getFileName();
          filename = unescape(filename);
          if (filename.indexOf(".pdf") > 0) {
            String absoluteFileName = path + filename;
            docUtil.downloadFile(in, absoluteFileName);
            return absoluteFileName;
          }
        }
      }
    }
    return null;
  }

  public static void changePdfRevise(EPMDocument wtdoc, ObjectReference self, Vector<String> timeVec)
  {
    try
    {
      System.out.println("wtdoc.getAuthoringApplication().toString():" + wtdoc.getAuthoringApplication().toString() + "===========");
      cStatus = wtdoc.getLifeCycleState().toString();
      version = wtdoc.getVersionIdentifier().getValue();

      if (wtdoc.getAuthoringApplication().toString().equalsIgnoreCase(
        "PROE"))
      {
        String absoluteFileName = downloadpdf2(wtdoc);
        System.out.println("absoluteFileName:" + absoluteFileName + "==============");
        if (absoluteFileName != null)
        {
          Hashtable revise = getReviews(self);
          IBAUtil ibautil = new IBAUtil(
            wtdoc);
          String typename = "PROE";
          Hashtable reset = new Hashtable();
          reset = resetWF(revise);

          writeTextEPM(reset, typename, absoluteFileName, timeVec);
          docUtil.uploadpdf(wtdoc, absoluteFileName);
          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        }
      }
      else if (wtdoc.getAuthoringApplication().toString().equalsIgnoreCase(
        "UG"))
      {
        System.out.println("zyj--test--ug--start!!");
        String absoluteFileName = downloadpdf2(wtdoc);
        System.out.println("absoluteFileName:" + absoluteFileName + "==============");
        if (absoluteFileName != null)
        {
          Hashtable revise = getReviews(self);
          IBAUtil ibautil = new IBAUtil(
            wtdoc);
          String typename = "UG";
          Hashtable reset = new Hashtable();
          reset = resetWF(revise);

          writeTextEPM(reset, typename, absoluteFileName, timeVec);
          docUtil.uploadpdf(wtdoc, absoluteFileName);
          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
          System.out.println("zyj--test--ug--end!!");
        }
      }
      else {
        System.out.println("zyj--test--cad--start");
        ContentHolder ch = ContentHelper.service
          .getContents(wtdoc);
        Vector attachmentList = 
          ContentHelper.getApplicationData(ch);

        for (int i = 0; i < attachmentList.size(); i++) {
          ApplicationData appDataPDF = 
            (ApplicationData)attachmentList
            .get(i);

          String fileName = appDataPDF.getFileName();
          System.out.println("NAME" + fileName);
          if (fileName.toLowerCase().endsWith(".pdf")) {
            int pdf = fileName.lastIndexOf("-");
            if (pdf < 0) {
              pdf = fileName.lastIndexOf(".");
            }

            String absoluteFileName = path + fileName;
            ContentServerHelper.service.writeContentStream(
              appDataPDF, absoluteFileName);
            Hashtable revise = getReviews(self);
            IBAUtil ibautil = new IBAUtil(
              wtdoc);
            String typename = "ACAD";
            Hashtable reset = new Hashtable();
            reset = resetWF(revise);

            writeTextEPM(reset, typename, absoluteFileName, 
              timeVec);
            ContentServerHelper.service.updateContent(ch, 
              appDataPDF, absoluteFileName);

            File pdfFile = new File(absoluteFileName);
            pdfFile.delete();
            System.out.println("zyj--test--cad--end");
          }
        }
      }
    }
    catch (FileNotFoundException e) {
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

  public static void changePdfRevise(WTDocument wtdoc, ObjectReference self, Vector<String> timeVec)
  {
    try
    {
      boolean repReady = true;
      String absoluteFileName = "";
      String type = "";
      if (wtdoc != null) {
        type = getSoftType(wtdoc);
        if(!type.contains("bzjmxb") && !type.contains("fzmxb") && !type.contains("wgjmxb") && !type.contains("zcmxb") ){
        	System.out.println("zyj--20170726--not baobiao");
        absoluteFileName = downloadpdf2(wtdoc);
        if (absoluteFileName != null) {
          Hashtable revise = getReviews(self);
          IBAUtil ibautil = new IBAUtil(wtdoc);
          String typename = "";
          try {
            typename = getSoftType(wtdoc);
          } catch (WTException e) {
            return;
          }
          if ((typename == null) || (typename.length() < 1)) {
            return;
          }
          Hashtable reset = new Hashtable();
          reset = resetWF(revise);
          System.out.println("=========" + typename + "====" + absoluteFileName + "===9");

          writeText(reset, typename, absoluteFileName, timeVec, wtdoc);
          docUtil.uploadpdf(wtdoc, absoluteFileName);
          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        }
        else {
          ContentHolder ch = ContentHelper.service
            .getContents(wtdoc);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          System.out.println("============aaa============");
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
              Hashtable revise = getReviews(self);
              IBAUtil ibautil = new IBAUtil(
                wtdoc);
              String typename = "";
              try {
                typename = getSoftType(wtdoc);
              } catch (WTException e) {
                return;
              }
              if ((typename == null) || (typename.length() < 1)) {
                return;
              }
              System.out.println("========================" + typename);
              Hashtable reset = new Hashtable();
              reset = resetWF(revise);
              System.out.println("============bbb============");

              writeText(reset, typename, absoluteFileName, timeVec, 
                wtdoc);
              ContentServerHelper.service.updateContent(ch, 
                appDataPDF, absoluteFileName);

              File pdfFile = new File(absoluteFileName);
              pdfFile.delete();
            }
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
        ss = "设计";
      else {
        ss = key;
      }
      resetTable.put(ss, str);
    }

    return resetTable;
  }

  public static void signPdf(Vector<WTObject> vector, ObjectReference self, Vector<String> timeVec)
  {
    if (vector != null) {
      int size = vector.size();
      for (int i = 0; i < size; i++) {
        WTObject object = (WTObject)vector.get(i);
        if ((object instanceof WTDocument)) {
          WTDocument wtDocument = (WTDocument)object;
          changePdfRevise(wtDocument, self, timeVec);
        } else if ((object instanceof EPMDocument)) {
          EPMDocument empDocument = (EPMDocument)object;
          changePdfRevise(empDocument, self, timeVec);
        } else if ((object instanceof WTChangeOrder2)) {
          WTChangeOrder2 wtc = (WTChangeOrder2)object;
          changePdfRevise(wtc, self, timeVec);
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
    if ((pbo instanceof PromotionNotice)) {
      PromotionNotice pn = (PromotionNotice)pbo;
      QueryResult qr = MaturityHelper.service
        .getPromotionTargets(pn);
      while (qr.hasMoreElements()) {
        WTObject obj = (WTObject)qr.nextElement();
        vector.add(obj);
      }
    } else if ((pbo instanceof WTDocument)) {
      vector.add(pbo);
    } else if ((pbo instanceof EPMDocument)) {
      vector.add(pbo);
    } else if ((pbo instanceof WTChangeOrder2))
    {
      WTChangeOrder2 wtc = (WTChangeOrder2)pbo;
      if ((wtc != null) && ((wtc instanceof WTChangeOrder2)))
      {
        Vector changeAfter = new Vector();

        QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(wtc);

        while (qrActivities.hasMoreElements()) {
          Object objActivities = qrActivities.nextElement();

          if ((objActivities instanceof WTChangeActivity2)) {
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

    }

    signPdf(vector, self, timeVec);
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
    String cState = "";
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
          cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
          if (cState.indexOf("INWORK") > -1)
            new_set.add(obj);
        }
        QueryResult qr2 = MaturityHelper.service.getBaselineItems(pn);
        while (qr2.hasMoreElements())
        {
          WTObject obj = (WTObject)qr2.nextElement();
          cState = ((LifeCycleManaged)obj).getLifeCycleState().toString();
          if (cState.indexOf("INWORK") > -1)
            new_set2.add(obj);
        }
        if (new_set.size() >= 1) {
          System.out.println("zyj--test--new_set>=1");
          MaturityHelper.service.deletePromotionTargets(pn, new_set);
        }
        if (new_set2.size() >= 1) {
          System.out.println("zyj--test--new_set2>=1");
          BaselineHelper.service.removeFromBaseline(new_set2, baseline);

          MaturityHelper.service.savePromotionTargets(pn, new_set2);
          BaselineHelper.service.addToBaseline(new_set2, baseline);
        }
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
      String type = getSoftType(eco);
      type = type.substring(type.length() - 4);
      System.out.println("zyj--test--ECN type:" + type);
      if ("ECNA".equals(type))
        softAttr = "pt";
      else if ("ECNB".equals(type))
        softAttr = "zc";
      else if ("ECNC".equals(type))
        softAttr = "ECNC";
      else if ("ECND".equals(type)) {
        softAttr = "ECND";
      }
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

  public static void signPBO_zjd(Vector<WTObject> vector) throws MaturityException, WTException
  {
    Vector timeVec = new Vector();
    signPdf_zjd(vector, timeVec);
  }

  public static void signPdf_zjd(Vector<WTObject> vector, Vector<String> timeVec) throws WTException
  {
    Vector vector_change = new Vector();
    if (vector != null) {
      int size = vector.size();
      for (int i = 0; i < size; i++) {
        WTObject object = (WTObject)vector.get(i);
        if ((object instanceof EPMDocument)) {
          EPMDocument empDocument = (EPMDocument)object;
          changePdfRevise_zjd(empDocument, timeVec);
        }
      }
    }
  }

  public static void changePdfRevise_zjd(EPMDocument wtdoc, Vector<String> timeVec) {
    try {
      ContentHolder ch = ContentHelper.service.getContents(wtdoc);
      Vector attachmentList = ContentHelper.getApplicationData(ch);

      for (int i = 0; i < attachmentList.size(); i++) {
        ApplicationData appDataPDF = (ApplicationData)attachmentList.get(i);

        String fileName = appDataPDF.getFileName();
        if (fileName.toLowerCase().endsWith(".pdf")) {
          new String(fileName.getBytes("gb2312"), "utf-8");
          String absoluteFileName = path + fileName;
          ContentServerHelper.service.writeContentStream(appDataPDF, absoluteFileName);
          IBAUtility ibautil = new IBAUtility(wtdoc);
          String typename = wtdoc.getAuthoringApplication().toString();
          if (typename.equalsIgnoreCase("PROE"))
            typename = "PROE";
          else if (typename.equalsIgnoreCase("ACAD"))
            typename = "ACAD";
          else if (typename.equalsIgnoreCase("UG")) {
            typename = "UG";
          }
          writeTextEPMnew(typename, absoluteFileName, timeVec, wtdoc);
          ContentServerHelper.service.updateContent(ch, appDataPDF, absoluteFileName);

          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        }
      }
    }
    catch (FileNotFoundException e) {
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

  public static void writeTextEPMnew(String fileType, String fileName, Vector<String> timeVec, WTObject wtdoc) {
    try {
      PdfReader reader = new PdfReader(
        fileName);
      String oldfileName = fileName;
      int pages = reader.getNumberOfPages();
      Rectangle psize2 = reader.getPageSize(1);
      float height2 = psize2.getHeight();
      float width2 = psize2.getWidth();
      System.out.println("height2:" + height2 + "====width2:" + width2 + "===================");
      Rectangle psize = reader.getPageSizeWithRotation(1);
      float height = psize.getHeight();
      float width = psize.getWidth();
      System.out.println("height:" + height + "====width:" + width + "===================");

      String writeType = "";
      if (width == width2) {
        System.out.println("=========未转==============");
        writeType = "EPMDOCUMENT_" + fileType + tufu(fileName);
      } else {
        System.out.println("=========旋转==============");
      }
      System.out.println("fileType:" + fileType + "===tufu:" + tufu(fileName) + "================");
      Hashtable hts = InitXml();
      Hashtable htf = (Hashtable)hts.get(writeType);
      System.out.println("类型:" + writeType + "================");
      Rectangle rectPageSize = new Rectangle(width2, height2);
      System.out.println("Float.toString(width):" + Float.toString(width) + "Float.toString(height):" + Float.toString(height) + "================");
      com.lowagie.text.Document document = new com.lowagie.text.Document(
        rectPageSize);

      int ii = fileName.lastIndexOf("/");
      String tmpPath = wthome + File.separatorChar + "codebase" + File.separatorChar + "download1" + 
        File.separatorChar;
      String outfilename = tmpPath + fileName.substring(ii + 1);
      System.out.println("outfilename:" + outfilename + "===================");
      System.out.println("filename:" + fileName + "===================");
      PdfWriter writer = PdfWriter.getInstance(document, 
        new FileOutputStream(outfilename));
      document.open();

      PdfContentByte cb = writer.getDirectContent();
      BaseFont bf = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", "Identity-H", 
        true);
      cb.setFontAndSize(bf, 10.0F);
      Hashtable htpOthers = (Hashtable)htf.get("others");
      for (int i = 1; i <= pages; i++) {
        Hashtable htp = (Hashtable)htf.get(String.valueOf(i));
        if (htp != null) {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeTextnew(cb, bf, htp, fileType);
          document.newPage();
        } else {
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeTextnew(cb, bf, htpOthers, fileType);
          document.newPage();
        }
      }
      document.close();
      File pdfFileold = new File(oldfileName);
      File pdfFile = new File(fileName);
      pdfFileold.delete();
      pdfFile.delete();

      File tempPdfFile = new File(outfilename);
      tempPdfFile.renameTo(pdfFileold);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void writeTextnew(PdfContentByte cb, BaseFont bf, Hashtable ht, String filetype) throws DocumentException, IOException, IOException, WTException
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
      float datex = Float.parseFloat((String)section.get("datex"));
      float datey = Float.parseFloat((String)section.get("datey"));
      float fontsize = Float.parseFloat((String)section.get("fontsize"));
      float rotation = Float.parseFloat((String)section.get("rotation"));

      float yearx = Float.parseFloat((String)section.get("yearx"));
      float yeary = Float.parseFloat((String)section.get("yeary"));
      float monthx = Float.parseFloat((String)section.get("monthx"));
      float monthy = Float.parseFloat((String)section.get("monthy"));
      float dayx = Float.parseFloat((String)section.get("dayx"));
      float dayy = Float.parseFloat((String)section.get("dayy"));
      System.out.println("zyj--test--key:" + key);
      try
      {
        WTProperties localWTProperties = WTProperties.getLocalProperties();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      if (!key.equalsIgnoreCase("TYBJ"))
        continue;
      cb.beginText();
      cb.setFontAndSize(bf, fontsize);
      cb.showTextAligned(0, "D", Float.parseFloat(((x + 19.170000000000002D) * cc)+""), y * cy, rotation);
      cb.endText();
    }
  }

  public static String getComment(ObjectReference self, String jd)
    throws WTException
  {
    WfProcess wfProcess = getWfProcess(self);
    WfAssignedActivity wfaa = getWfAssignedActivity(wfProcess, jd);
    String comments = "";

    ProcessData pd = wfaa.getContext();
    if (pd != null)
    {
      comments = pd.getTaskComments();
      System.out.println("commentsxxx===" + comments);
    }

    return comments;
  }

  public static WfProcess getWfProcess(ObjectReference self)
    throws WTException
  {
    if (self == null)
    {
      return null;
    }
    Persistable persistable = self.getObject();
    if ((persistable instanceof WfAssignedActivity))
    {
      return ((WfAssignedActivity)persistable).getParentProcess();
    }
    if ((persistable instanceof WfProcess))
    {
      return (WfProcess)persistable;
    }
    return null;
  }

  public static WfAssignedActivity getWfAssignedActivity(WfProcess wfProcess, String actvityName) throws WTException
  {
    QuerySpec queryspec = new QuerySpec(WfAssignedActivity.class);
    queryspec.appendWhere(new SearchCondition(WfAssignedActivity.class, "name", "=", actvityName.toUpperCase(), false), new int[1]);
    queryspec.appendAnd();
    queryspec.appendWhere(new SearchCondition(WfAssignedActivity.class, "parentProcessRef.key", "=", PersistenceHelper.getObjectIdentifier(wfProcess)), new int[1]);
    QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
    if (queryresult.hasMoreElements())
    {
      return (WfAssignedActivity)queryresult.nextElement();
    }
    return null;
  }
  public static void isVisual(WTObject obj) throws WTException, PropertyVetoException, IOException {
    String visual = "";
    if ((obj instanceof WTDocument)) {
      WTDocument doc = (WTDocument)obj;
      visual = downloadpdf2(doc);
      if (("".equals(visual)) || (visual == null))
      {
        throw new WTException("可视化不成功，请重新发布");
      }
    }
  }

  public static void checkECNDOCVisual(WTObject obj) throws ChangeException2, WTException, PropertyVetoException, IOException
  {
    String visual = "";
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
      while (qr.hasMoreElements()) {
        Persistable pobj = (Persistable)qr.nextElement();
        if ((pobj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)pobj;
          visual = downloadpdf2(doc);
          if (("".equals(visual)) || (visual == null))
            throw new WTException(doc.getName() + "可视化不成功，请重新发布");
        }
      }
    }
  }

  public static void ECNAfterDataInECN(WTObject obj)
  {
    try
    {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);

        while (qr.hasMoreElements())
        {
          Changeable2 changeable2 = (Changeable2)qr.nextElement();
          WTCollection qrw = RelatedChangesQueryCommands.getRelatedResultingChangeNotices(changeable2);
          Iterator iter = qrw.iterator();

          ObjectReference or = (ObjectReference)iter.next();
          WTChangeOrder2 changeOrder = (WTChangeOrder2)or.getObject();
          String currentState = changeOrder.getLifeCycleState().toString();
          if (!changeOrder.getNumber().equals(ecn.getNumber()))
            throw new WTException("变更通告" + changeOrder.getNumber() + "的产生对象中包含" + changeable2.getIdentity() + "，请移除后提交.");
          iter.hasNext();
        }

      }

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void ECNAfterDataChecked(WTObject obj)
  {
    try {
      if ((obj instanceof ChangeOrderIfc)) {
        QueryResult qr = ChangeHelper2.service.getChangeablesBefore((ChangeOrderIfc)obj, true);
        if (qr.size() > 0)
          throw new WTException("受影响对象不为空，请移除后提交");
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void ECNDataChecked(WTObject obj)
  {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if (WorkInProgressHelper.isCheckedOut((Workable)pobj))
            throw new WTException("对象" + pobj.getIdentity() + "处于检出状态,因此不能提交.");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void checkUser(ObjectReference processRef, WTObject obj) throws WTException {
    WfProcess wfp = getProcess(processRef);
    WTPrincipalReference creator = wfp.getCreator();
    WTPrincipalReference modifer = null;
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
      while (qr.hasMoreElements()) {
        Persistable pobj = (Persistable)qr.nextElement();
        if ((pobj instanceof WTPart)) {
          WTPart part = (WTPart)pobj;
          modifer = part.getModifier();
          if (!creator.equals(modifer))
            throw new WTException("对象的修改者与流程提交者不是同一人");
        }
        else if ((pobj instanceof EPMDocument)) {
          EPMDocument empDoc = (EPMDocument)pobj;
          modifer = empDoc.getModifier();
          if (!creator.equals(modifer))
            throw new WTException("对象的修改者与流程提交者不是同一人");
        }
        else if ((pobj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)pobj;
          modifer = doc.getModifier();
          if (!creator.equals(modifer))
            throw new WTException("对象的修改者与流程提交者不是同一人");
        }
      }
    }
  }

  public static WfProcess getProcess(ObjectReference processRef)
    throws WTException
  {
    WfProcess wfprocess = null;
    Persistable persistable = processRef.getObject();
    if ((persistable instanceof WfActivity)) {
      WfActivity mySelf = (WfActivity)processRef.getObject();
      wfprocess = mySelf.getParentProcess();
    } else if ((persistable instanceof WfProcess)) {
      wfprocess = (WfProcess)persistable;
    }
    return wfprocess;
  }

  public static void ECNAfterDataCheckState(WTObject obj) {
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          String currentState = "";
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof LifeCycleManaged)) {
            currentState = ((LifeCycleManaged)pobj)
              .getLifeCycleState().toString();
            if ((!currentState.equalsIgnoreCase("INWORK")) && 
              (!currentState.equalsIgnoreCase("INWORK_D")) && 
              (!currentState.equalsIgnoreCase("INWORK_S")) && 
              (!currentState.equalsIgnoreCase("REWORK")))
            {
              throw new WTException("对象不是正在工作或返工状态");
            }
          }
          String version = "";
          if ((pobj instanceof WTPart)) {
            WTPart pt = (WTPart)pobj;
            version = pt.getVersionIdentifier().getValue();
          } else if ((pobj instanceof EPMDocument)) {
            EPMDocument epm = (EPMDocument)pobj;
            version = epm.getVersionIdentifier().getValue();
          }
          if ((!"".equals(version)) && (!"C01".equals(version)) && (!"S01".equals(version)))
            throw new WTException("新产生的对象中部件/图纸存在不为C01或S01的版本，请移除后提交");
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void checkJSTZD(WTObject obj) throws ChangeException2, WTException {
    Vector vec = new Vector();
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
      while (qr.hasMoreElements()) {
        Persistable pobj = (Persistable)qr.nextElement();
        if ((pobj instanceof WTDocument)) {
          WTDocument doc = (WTDocument)pobj;
          String type = getSoftType(doc);
          if (type.substring(type.length() - 9).equalsIgnoreCase("tasvdoc01")) {
            vec.add(doc);
          }
        }
      }
      if (vec.size() == 0)
        throw new WTException("请给新产生对象添加技术通知单");
    }
  }
}