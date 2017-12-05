package ext.pdf;

import com.lowagie.text.Document;
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

import ext.util.IBAUtility;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.vc.VersionControlHelper;
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

public class SignBaobiao1
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
  
  private static String riqi1 = "";

  private static String version = "";
  
  private static String cph = "";
  public final static char[] upper = "O一二三四五六七八九十".toCharArray();

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

  private static void writeText(PdfContentByte cb, BaseFont bf, Hashtable ht, Hashtable htrevise,  String filetype)
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
            cb.showTextAligned(0, cStatus, Float.parseFloat((x + 10.0D) * cc+""), y * cy, rotation);
            cb.endText();
          }
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
  private static void writeTextTemp(PdfContentByte cb, BaseFont bf, Hashtable ht, Hashtable htrevise, String filetype)
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
		            cb.showTextAligned(0, cStatus, Float.parseFloat((x + 10.0D) * cc+""), y * cy, rotation);
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
		        if (key.equalsIgnoreCase("ProductNo")) {
			          System.out.println("zyj--test--TYBJ==========");
			          cb.beginText();
			          cb.setFontAndSize(bf, fontsize);
			          cb.showTextAligned(0, cph, x * cc, y * cy, rotation);
			          cb.endText();
			        }
		        if (key.equalsIgnoreCase("RIQI")) {
		          System.out.println("zyj--test--RIQI==========" + riqi1);
		          cb.beginText();
		          cb.setFontAndSize(bf, fontsize);
		          cb.showTextAligned(0, riqi1, x * cc, y * cy, rotation);
		          cb.endText();
		        }
		      }
		    }
		  }
  /**
   * 根据小写数字格式的日期转换成大写格式的日期
   * @param date
   * @return
   */
  public static String getUpperDate(String date) {
      //支持yyyy-MM-dd、yyyy/MM/dd、yyyyMMdd等格式
      if(date == null) return null;
      //非数字的都去掉
      date = date.replaceAll("\\D", "");
      if(date.length() != 8) return null;
      StringBuilder sb = new StringBuilder();
      for (int i=0;i<4;i++) {//年
          sb.append(upper[Integer.parseInt(date.substring(i, i+1))]);
      }
      sb.append("年");//拼接年
      int month = Integer.parseInt(date.substring(4, 6));
      if(month <= 10) {
          sb.append(upper[month]);
      } else {
          sb.append("十").append(upper[month%10]);
      }
      sb.append("月");//拼接月

      int day = Integer.parseInt(date.substring(6));
      if (day <= 10) {
          sb.append(upper[day]);
      } else if(day < 20) {
          sb.append("十").append(upper[day % 10]);
      } else {
          sb.append(upper[day / 10]).append("十");
          int tmp = day % 10;
          if (tmp != 0) sb.append(upper[tmp]);
      }
      sb.append("日");//拼接日
      return sb.toString();
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

  public static String writeText(Hashtable htrevise, String filetype, String fileName,  WTDocument wtdoc)
  {
	  String outfilename="";
    try {
      String oldfileName = fileName;
      Hashtable hts = InitXml();
      String writeType = "";
      System.out.println("zyj--test--filetype:" + filetype + "==============");
      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("bzjmxb")) {
        writeType = filetype.substring(filetype.length() - 6)+"1";
      }
      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("wgjmxb")) {
        writeType = filetype.substring(filetype.length() - 6)+"1";
      }
      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("zcmxb")) {
        writeType = filetype.substring(filetype.length() - 5)+"1";
      }
      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("fzmxb")) {
        writeType = filetype.substring(filetype.length() - 5)+"1";
      }
      System.out.println("zyj--test--writeType:" + writeType);
      if (writeType == null) {
        return "";
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
      outfilename = path + "temp_" + "new_" + fileName.substring(ii + 1);
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
          writeText(cb, bf, htp, htrevise, filetype);
          document.newPage();
        } else if (htpOthers != null) {
          System.out.println("zyj--test--多页");
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeText(cb, bf, htpOthers, htrevise, filetype);
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
//      pdfFileold.delete();
//      pdfFile.delete();

      File tempPdfFile = new File(outfilename);
      tempPdfFile.renameTo(pdfFileold);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return outfilename;
  }
  public static String writeTextTemp(Hashtable htrevise, String filetype, String fileName, WTDocument wtdoc)
  {
	  String outfilename="";
    try {
      String oldfileName = fileName;
      Hashtable hts = InitXml();
      String writeType = "";
      System.out.println("zyj--test--filetype:" + filetype + "==============");
      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("bzjmxb")) {
        writeType = "bzjfm";
      }
      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("wgjmxb")) {
        writeType = "wgjfm";
      }
      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("zcmxb")) {
        writeType = "zcfm";
      }
      
      System.out.println("zyj--test--writeType:" + writeType);
      if (writeType == null) {
        return "";
      }
      Hashtable htf = (Hashtable)hts.get(writeType);
      System.out.println("zyj--20170726--fileName:" + fileName);
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
        System.out.println("zyj-20160726--path--" + path);
        System.out.println("zyj-20160726--fileName--" + fileName);
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

      int ii = fileName.lastIndexOf("\\");
      outfilename = path + "temp_" + "new_" + fileName.substring(ii + 1);
      System.out.println("zyj--test--outfilename:" + outfilename + "==================ii:"+ii);
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
          writeTextTemp(cb, bf, htp, htrevise, filetype);
          document.newPage();
        } else if (htpOthers != null) {
          System.out.println("zyj--test--多页");
          PdfImportedPage pagei = writer.getImportedPage(reader, i);
          cb.addTemplate(pagei, 0.0F, 0.0F);
          writeTextTemp(cb, bf, htpOthers, htrevise,  filetype);
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
    return outfilename;
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


  public static void changePdfRevise(WTDocument wtdoc, ObjectReference self)
  {
    try
    {
      boolean repReady = true;
      String absoluteFileName = "";
      String type = "";
      if (wtdoc != null) {
        type = getSoftType(wtdoc);
        absoluteFileName = downloadpdf2(wtdoc);
        if (absoluteFileName != null) {
          String version = VersionControlHelper.getVersionIdentifier(wtdoc).getValue();

          //absoluteFileName = absoluteFileName.replace(".pdf", "_" + version + ".pdf");
          Hashtable revise = getReviews(self);
          IBAUtil ibautil = new IBAUtil(wtdoc);
          bianhao = ibautil.getIBAValue("WJBH");
          cph = ibautil.getIBAValue("CHEXING");
          String typename = "";
          String usertime = (String)revise.get("审定");
          usertime = usertime.replaceAll(";;;", " ").replaceAll("&&&",
				" ");
          int num1 = usertime.indexOf(" ");
          String name = usertime.substring(0, num1);
          String time = usertime.substring(usertime.length() - 10,
				usertime.length());
          riqi1=getUpperDate(time);
          time = time.replaceAll("-", "");
          riqi=time;
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

          String outString = writeText(reset, typename, absoluteFileName,  wtdoc);
          System.out.println("zyj--cs=========absoluteFileName："+ absoluteFileName);
          System.out.println("zyj--cs=========outString："+ outString);
          if (typename.substring(typename.length() - 6).equalsIgnoreCase("bzjmxb") || typename.substring(typename.length() - 6).equalsIgnoreCase("wgjmxb") || typename.substring(typename.length() - 5).equalsIgnoreCase("zcmxb")) {
        	  String fmPath = signPDFTemplate(wtdoc,reset,typename);
              List<InputStream> pdfs = new ArrayList<InputStream>();
    		  pdfs.add(new FileInputStream(fmPath));
    		  pdfs.add(new FileInputStream(outString));
    		  String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    		  String mergePath = wtHome +File.separator +"codebase"+File.separator +"temp"+File.separator +wtdoc.getNumber() +".pdf";
    		  
    		  
    		  OutputStream output = new FileOutputStream(mergePath);
    		  concatPDFs(pdfs, output, true);
              docUtil.uploadpdf(wtdoc, mergePath);
              File pdfFile = new File(mergePath);
              pdfFile.delete();
          }else{
        	  docUtil.uploadpdf(wtdoc, outString);
              File pdfFile = new File(outString);
              //pdfFile.delete();
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
  public static void concatPDFs(List<InputStream> streamOfPDFFiles,
			OutputStream outputStream, boolean paginate) {
		System.out.println("*****************合并路径*******************");
		// 导入页码
		int currentPage = 0;
		int pdfMerge=0;
		Document document = new Document();

		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<PdfReader>();
			Iterator<InputStream> iteratorPDFs = pdfs.iterator();
			

			// Create Readers for the pdfs.
			while (iteratorPDFs.hasNext()) {
				
				InputStream pdf = iteratorPDFs.next();
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();
			PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			PdfImportedPage page;
			Iterator<PdfReader> iteratorPDFReader = readers.iterator();

			// Loop through the PDF files and add to the output.
			while (iteratorPDFReader.hasNext()) {
				PdfReader pdfReader = iteratorPDFReader.next();
				System.out.println("*******pdfReader********"+pdfReader);
				pdfMerge++;
				System.out.println("pdf源文件的數量："+pdfMerge);
	
				// Create a new page in the target for each source page.
				while (currentPage < pdfReader.getNumberOfPages()) {
					document.newPage();
					currentPage++;
					page = writer.getImportedPage(pdfReader,
							currentPage);
					cb.addTemplate(page, 0, 0);

				} 
//				将其他的PDF文件的开始页码归零
				currentPage = 0;
			
			}
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen())
				document.close();
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
  public static String signPDFTemplate(WTDocument doc,Hashtable htrevise, String filetype) throws IOException{
	  String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
	  String templateName="";
	  if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("bzjmxb")) {
		  templateName = wtHome +File.separator +"codebase"+File.separator +"ext"+File.separator +"pdf"+File.separator +"conf"+File.separator +"bzjTemplate.pdf";
	  }
      if (filetype.substring(filetype.length() - 6).equalsIgnoreCase("wgjmxb")) {
    	  templateName = wtHome +File.separator +"codebase"+File.separator +"ext"+File.separator +"pdf"+File.separator +"conf"+File.separator +"wgjTemplate.pdf";
      }
      if (filetype.substring(filetype.length() - 5).equalsIgnoreCase("zcmxb")) {
    	  templateName = wtHome +File.separator +"codebase"+File.separator +"ext"+File.separator +"pdf"+File.separator +"conf"+File.separator +"zcTemplate.pdf";
      }
	  
	  FileInputStream fis = new FileInputStream(templateName);
	  String outFileName = wtHome +File.separator +"codebase"+File.separator +"temp"+File.separator +doc.getNumber()+"_"+getCurrentTime("yyyyMMddHHmmssSSS") +".pdf";
	  System.out.println("zyj--ts--outFileName:"+outFileName);
	  FileOutputStream fos = new FileOutputStream(outFileName);
	  int length = 0;
	  byte[] buffer = new byte[1024]; // 一字节缓冲
	  while((length=fis.read(buffer)) != -1){
	     fos.write(buffer, 0, length);
	  }
	  fos.close();
	  fis.close();
	  writeTextTemp(htrevise, filetype, outFileName, doc);
	  return outFileName;
  }
  /**
	 * 
	 * 根据指定的格式获取系统当前时间
	 * 
	 * @param DATE_FORMAT
	 *            时间的格式
	 * @return 当前时间
	 */
	public static String getCurrentTime(String DATE_FORMAT) {
		Locale locale = WTContext.getContext().getLocale();
		Calendar calendar = Calendar.getInstance(WTContext.getContext()
				.getTimeZone(), locale);
		java.util.Date datDate = calendar.getTime();
		return WTStandardDateFormat.format(datDate, DATE_FORMAT, locale,
				calendar.getTimeZone());
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

  public static void signPdf(Vector<WTObject> vector, ObjectReference self)
  {
    if (vector != null) {
      int size = vector.size();
      for (int i = 0; i < size; i++) {
        WTObject object = (WTObject)vector.get(i);
        if ((object instanceof WTDocument)) {
          WTDocument wtDocument = (WTDocument)object;
          String typename = "";
          try {
            typename = getSoftType(wtDocument);
          } catch (WTException e) {
            continue;
          }
          if ((typename == null) || (typename.length() < 1)) {
            continue;
          }
          System.out.println("打印文档类型为" + typename);
          if ((typename.endsWith(".bzjmxb")) || (typename.endsWith(".wgjmxb")) || (typename.endsWith(".zcmxb")) || (typename.endsWith(".fzmxb")))
            changePdfRevise(wtDocument, self);
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
        	  if(((WTChangeActivity2) objActivities).getName().contains(wtc.getNumber())) continue;
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

    signPdf(vector, self);
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
    	  WTProperties wtproperties = WTProperties.getLocalProperties();
      }
      catch (IOException e)
      {
        WTProperties wtproperties;
        e.printStackTrace();
      }

      if (!key.equalsIgnoreCase("TYBJ"))
        continue;
      cb.beginText();
      cb.setFontAndSize(bf, fontsize);
      cb.showTextAligned(0, "D", Float.parseFloat((x + 19.170000000000002D) * cc+""), y * cy, rotation);
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


}