package ext.print;

import com.basicwtapi.util.Select;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.enterprise.copy.server.CoreMetaUtility;
import com.ptc.wvs.server.util.Util;
import ext.pdf.AddFlag;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
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
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentService;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentType;
import wt.fc.ObjectReference;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.PersistenceManagerSvr;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.httpgw.URLFactory;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTUser;
import wt.part.PartDocHelper;
import wt.part.PartDocService;
import wt.part.WTPart;
import wt.projmgmt.admin.Project2;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.IterationInfo;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;
import wt.vc.VersionInfo;
import wt.vc.config.ConfigException;
import wt.vc.config.OwnershipIndependentLatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewManageable;
import wt.vc.views.ViewService;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressService;

public class PrintRequestUtil
  implements RemoteAccess
{
  private static WTProperties properties;
  private static String printdownfolder;
  private static String doctype;
  private static String flag;

  static
  {
    try
    {
      properties = WTProperties.getLocalProperties();
      printdownfolder = properties
        .getProperty("ext.print.foldername", "");
      doctype = properties.getProperty("ext.print.docType", "");
    } catch (Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
  }

  public static WTDocument createdoc(WTReference wtReference, ArrayList selected)
    throws WTException, IOException, WTPropertyVetoException, InvocationTargetException
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "createdoc";
      String klass = PrintRequestUtil.class.getName();
      Class[] types = { WTReference.class, ArrayList.class };
      Object[] vals = { wtReference, selected };

      return (WTDocument)RemoteMethodServer.getDefault().invoke(method, 
        klass, null, types, vals);
    }

    String objType = doctype;
    System.out.println("objType====>" + objType);
    TypeIdentifier objType_ = 
      CoreMetaUtility.getTypeIdentifier(objType);
    System.out.println("objType====>" + objType_);
    WTDocument doc = (WTDocument)
      CoreMetaUtility.newInstance(objType_);

    WTUser user = (WTUser)SessionHelper.manager.getPrincipal();
    String name = user.getFullName();

    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String time = df.format(date);

    String docname = "打印申请单_" + time;
    doc.setName(docname);

    Folder folder = PrintUtil2.getInitFolder(doc, 
      (WTContainerRef)wtReference);
    if (folder != null) {
      FolderHelper.assignLocation(doc, folder);
    }
    doc = (WTDocument)PersistenceHelper.manager.save(doc);

    ArrayList released = new ArrayList();
    ArrayList UNreleased = new ArrayList();

    String fileStr = create(null, selected, UNreleased);

    uploadContent(doc, fileStr, true);

    doc = (WTDocument)PersistenceHelper.manager.refresh(doc);

    return doc;
  }

  public static WTDocument createdocForECO(WTReference wtReference, ArrayList selected, String ecoString)
    throws WTException, IOException, InvocationTargetException, PropertyVetoException
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "createdoc";
      String klass = PrintRequestUtil.class.getName();
      Class[] types = { WTReference.class, ArrayList.class, String.class };
      Object[] vals = { wtReference, selected, ecoString };

      return (WTDocument)RemoteMethodServer.getDefault().invoke(method, 
        klass, null, types, vals);
    }

    String objType = doctype;
    System.out.println("objType====>" + objType);
    TypeIdentifier objType_ = 
      CoreMetaUtility.getTypeIdentifier(objType);
    System.out.println("objType====>" + objType_);
    WTDocument doc = (WTDocument)
      CoreMetaUtility.newInstance(objType_);

    WTUser user = (WTUser)SessionHelper.manager.getPrincipal();
    String name = user.getFullName();

    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String time = df.format(date);

    String docname = "打印申请单_" + ecoString + "_" + time;
    doc.setName(docname);

    Folder folder = PrintUtil2.getInitFolder(doc, 
      (WTContainerRef)wtReference);
    if (folder != null) {
      FolderHelper.assignLocation(doc, folder);
    }
    doc = (WTDocument)PersistenceHelper.manager.save(doc);

    ArrayList released = new ArrayList();
    ArrayList UNreleased = new ArrayList();

    String fileStr = createForECO(doc.getNumber(), selected, UNreleased, ecoString);

    uploadContent(doc, fileStr, true);

    doc = (WTDocument)PersistenceHelper.manager.refresh(doc);

    return doc;
  }

  public static WTDocument createdocForBOM(WTReference wtReference, ArrayList selected, String partString)
    throws WTException, IOException, InvocationTargetException, PropertyVetoException
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "createdoc";
      String klass = PrintRequestUtil.class.getName();
      Class[] types = { WTReference.class, ArrayList.class };
      Object[] vals = { wtReference, selected };

      return (WTDocument)RemoteMethodServer.getDefault().invoke(method, 
        klass, null, types, vals);
    }

    String objType = doctype;
    System.out.println("objType====>" + objType);
    TypeIdentifier objType_ = 
      CoreMetaUtility.getTypeIdentifier(objType);
    System.out.println("objType====>" + objType_);
    WTDocument doc = (WTDocument)
      CoreMetaUtility.newInstance(objType_);

    WTUser user = (WTUser)SessionHelper.manager.getPrincipal();
    String name = user.getFullName();

    Date date = new Date();
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    String time = df.format(date);

    String docname = "打印申请单_" + partString + "_" + time;
    doc.setName(docname);

    Folder folder = PrintUtil2.getInitFolder(doc, 
      (WTContainerRef)wtReference);
    if (folder != null) {
      FolderHelper.assignLocation(doc, folder);
    }
    doc = (WTDocument)PersistenceHelper.manager.save(doc);

    ArrayList released = new ArrayList();
    ArrayList UNreleased = new ArrayList();

    String fileStr = createForBOM(doc.getNumber(), selected, UNreleased, partString);

    uploadContent(doc, fileStr, true);

    doc = (WTDocument)PersistenceHelper.manager.refresh(doc);

    return doc;
  }

  public static void createLink(WTDocument doc, ArrayList released)
    throws WTException
  {
    for (Iterator iterator = released.iterator(); iterator.hasNext(); ) {
      WTDocument wtDocument = (WTDocument)iterator.next();
      WTDocumentHelper.service.createDependencyLink(doc, wtDocument, "");
    }
  }

  private static void uploadContent(ContentHolder content_holder, String fileStr, boolean flag)
  {
    try
    {
      File f = new File(fileStr);
      addContent(content_holder, f, flag);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static HSSFSheet CreatNewExcel(HSSFSheet sheet, HSSFWorkbook wb, HSSFWorkbook wbCreat)
    throws IOException
  {
    String excelName = "新的excel 文件名";

    HSSFSheet sheetCreat = wbCreat.createSheet("sheet1");

    MergerRegion(sheetCreat, sheet);

    int firstRow = sheet.getFirstRowNum();
    int lastRow = sheet.getLastRowNum();
    for (int i = firstRow; i <= lastRow; i++)
    {
      HSSFRow rowCreat = sheetCreat.createRow(i);

      HSSFRow row = sheet.getRow(i);

      HSSFCellStyle cellStyle = null;

      int firstCell = row.getFirstCellNum();
      int lastCell = row.getLastCellNum();
      for (int j = firstCell; j < lastCell; j++)
      {
        sheetCreat.autoSizeColumn(j);

        cellStyle = wbCreat.createCellStyle();

        cellStyle.setBorderTop(row.getCell(j).getCellStyle().getBorderTop());
        cellStyle.setBorderBottom(row.getCell(j).getCellStyle().getBorderBottom());
        cellStyle.setBorderLeft(row.getCell(j).getCellStyle().getBorderLeft());
        cellStyle.setBorderRight(row.getCell(j).getCellStyle().getBorderRight());

        cellStyle.setAlignment(row.getCell(j).getCellStyle().getAlignment());

        cellStyle.setVerticalAlignment(row.getCell(j).getCellStyle().getVerticalAlignment());

        cellStyle.setWrapText(row.getCell(j).getCellStyle().getWrapText());
        rowCreat.createCell(j).setCellStyle(cellStyle);

        rowCreat.getCell(j).getRow().setHeight(row.getCell(j).getRow().getHeight());

        switch (row.getCell(j).getCellType()) {
        case 1:
          String strVal = removeInternalBlank(row.getCell(j).getStringCellValue());

          rowCreat.getCell(j).setCellValue(strVal);
          break;
        case 0:
          rowCreat.getCell(j).setCellValue(row.getCell(j).getNumericCellValue());
          break;
        case 2:
          try
          {
            rowCreat.getCell(j).setCellValue(String.valueOf(row.getCell(j).getNumericCellValue()));
          } catch (IllegalStateException e) {
            try {
              rowCreat.getCell(j).setCellValue(String.valueOf(row.getCell(j).getRichStringCellValue()));
            } catch (Exception ex) {
              rowCreat.getCell(j).setCellValue("公式出错");
            }

          }

        }

      }

    }

    return sheetCreat;
  }

  private static void MergerRegion(HSSFSheet sheetCreat, HSSFSheet sheet)
  {
    int sheetMergerCount = sheet.getNumMergedRegions();
    for (int i = 0; i < sheetMergerCount; i++) {
      Region mergedRegionAt = sheet.getMergedRegionAt(i);
      sheetCreat.addMergedRegion(mergedRegionAt);
    }
  }

  public static String removeInternalBlank(String s)
  {
    Pattern p = Pattern.compile("\\s*|\t|\r|\n");
    Matcher m = p.matcher(s);
    char[] str = s.toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < str.length; i++) {
      if (str[i] != ' ') break;
      sb.append(' ');
    }

    String after = m.replaceAll("");
    return sb.toString() + after;
  }

  private static String checkFileName(String strPath, String checkFilename)
  {
    File file = new File(strPath);
    for (File f : file.listFiles()) {
      if (f.getName().equals(checkFilename)) {
        checkFilename = checkFilename + checkFilename + "_重名";
        checkFileName(strPath, checkFilename);
        break;
      }
    }
    return checkFilename;
  }

  private static void addContent(ContentHolder holder, File afile, boolean flag) throws Exception
  {
    ApplicationData app_data = ApplicationData.newApplicationData(holder);
    app_data.setFileName(afile.getName());
    app_data.setUploadedFromPath(afile.getAbsolutePath());
    if (flag)
      app_data.setRole(ContentRoleType.PRIMARY);
    else {
      app_data.setRole(ContentRoleType.SECONDARY);
    }
    app_data.setFileSize(afile.length());
    holder = (ContentHolder)PersistenceHelper.manager.refresh(holder);
    if (afile.exists())
      app_data = ContentServerHelper.service.updateContent(holder, 
        app_data, afile.getPath());
  }

  private static void copy(String sourceFile, String targetFile)
  {
    FileInputStream input = null;
    FileOutputStream output = null;
    try {
      input = new FileInputStream(sourceFile);
      output = new FileOutputStream(targetFile);
      byte[] b = new byte[5120];
      int len;
      while ((len = input.read(b)) != -1)
      {
        int len;
        output.write(b, 0, len);
      }
      output.flush();
      output.close();
      input.close();
    } catch (Exception e) {
      System.out.println("files in the dolder may be using by someone");
    }
  }

  public static String create(String docNumber, ArrayList released, ArrayList UNreleased)
    throws IOException, WTException
  {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    WTUser user = null;
    try {
      user = (WTUser)SessionHelper.getPrincipal();
    } catch (WTException e) {
      e.printStackTrace();
    }
    String dysqrFullName = user.getFullName();
    Date date = new Date();
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = sd.format(date);
    String filename = PrintUtil2.getTempFilePath(docNumber) + "打印清单.xls";
    String filename_in = PrintUtil2.getTempleteFilePath(docNumber) + "图纸打印申请单.xls";
    System.out.println("filename=======>" + filename);
    System.out.println("filename_in====>" + filename_in);

    copy(filename_in, filename);
    FileOutputStream out = new FileOutputStream(filename);
    FileInputStream in = new FileInputStream(filename_in);

    HSSFWorkbook wb = new HSSFWorkbook(in);
    HSSFSheet s = wb.getSheetAt(0);

    Row r = null;
    Row r_temp = null;
    Cell c = null;
    CellStyle cs2 = wb.createCellStyle();
    cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
    r_temp = s.getRow(2);
    Cell cell_cs = r_temp.getCell(0);
    cs2 = cell_cs.getCellStyle();

    wb.setSheetName(0, "sheet1");

    int size = released.size();
    System.out.println("released==========>" + released);
    for (int rownum = 0; rownum < size; rownum++) {
      Object object = released.get(rownum);

      String number = "";
      String name = "";
      String banben = "";
      String tuzhirongqi = "";
      String docoid = "";
      String modifier = "";
      String chanpindaihao = "";
      String mingcheng = "";
      String statesymbol = "";
      String cailiaoguige = "";
      String beizhu = "";

      String cname = "";
      String state = "";
      String representationable = "无";
      if ((object instanceof WTDocument)) {
        WTDocument doc = (WTDocument)released.get(rownum);
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(doc);
        if (representation != null) {
          representationable = "有";
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");

        mingcheng = iba.getIBAValue("tuwendangmingcheng");
        statesymbol = iba.getIBAValue("statesymbol");
        cailiaoguige = iba.getIBAValue("cailiaoguige");
        beizhu = iba.getIBAValue("beizhu");
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof EPMDocument))
      {
        EPMDocument doc = (EPMDocument)released.get(rownum);
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(doc);
        if (representation != null) {
          representationable = "有";
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        cname = doc.getCADName();
        cname = trimExtension(cname);
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof WTPart)) {
        WTPart part = (WTPart)released.get(rownum);
        QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
        System.out.println(qrepm.getEnumeration());
        while (qrepm.hasMoreElements()) {
          try {
            EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
            System.out.println("epm3d===>" + epm3d);
            EPMDocument epm2d = PrintPartBOMUtil.getDrawing(epm3d, 
              null);
            System.out.println("epm2d===>" + epm2d);
            Representation representation = RepresentationHelper.service
              .getDefaultRepresentation(epm2d);
            System.out.println(epm2d + "epm2d===>" + representation);
            if (representation != null)
              representationable = "有";
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        IBAUtil iba = new IBAUtil(part);
        number = part.getNumber();
        name = part.getName();
        banben = part.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(part);
        modifier = part.getModifierFullName();
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = part.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      }

      r = s.createRow(rownum + 2);

      c = r.createCell(0);
      cell_cs = r_temp.getCell(0);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(rownum + 1);

      c = r.createCell(1);
      cell_cs = r_temp.getCell(1);
      cs2 = cell_cs.getCellStyle();
      c.setCellType(1);
      c.setCellStyle(cs2);
      c.setCellValue(number);

      c = r.createCell(2);
      cell_cs = r_temp.getCell(2);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(name);

      c = r.createCell(3);
      cell_cs = r_temp.getCell(3);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(representationable);

      c = r.createCell(4);
      cell_cs = r_temp.getCell(4);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(banben);

      c = r.createCell(5);
      cell_cs = r_temp.getCell(5);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(state);

      c = r.createCell(6);
      cell_cs = r_temp.getCell(6);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue("1");

      c = r.createCell(7);
      cell_cs = r_temp.getCell(7);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(tuzhirongqi);

      c = r.createCell(8);
      cell_cs = r_temp.getCell(8);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(beizhu);

      c = r.createCell(20);

      c.setCellValue(docoid);
    }

    wb.write(out);
    out.close();
    in.close();
    return filename;
  }

  public static String createForECO(String docNumber, ArrayList released, ArrayList UNreleased, String ecoString)
    throws IOException, WTException, PropertyVetoException
  {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    WTUser user = null;
    try {
      user = (WTUser)SessionHelper.getPrincipal();
    } catch (WTException e) {
      e.printStackTrace();
    }
    String dysqrFullName = user.getFullName();
    Date date = new Date();
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = sd.format(date);
    String filename = PrintUtil2.getTempFilePath(null) + docNumber + "_打印申请单_" + ecoString + ".xls";
    String filename_in = PrintUtil2.getTempleteFilePath(null) + "图纸打印申请单.xls";
    System.out.println("filename=======>" + filename);
    System.out.println("filename_in====>" + filename_in);

    copy(filename_in, filename);
    FileOutputStream out = new FileOutputStream(filename);
    FileInputStream in = new FileInputStream(filename_in);

    HSSFWorkbook wb = new HSSFWorkbook(in);
    HSSFSheet s = wb.getSheetAt(0);

    Row r = null;
    Row r_temp = null;
    Cell c = null;
    CellStyle cs2 = wb.createCellStyle();
    cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
    r_temp = s.getRow(2);
    Cell cell_cs = r_temp.getCell(0);
    cs2 = cell_cs.getCellStyle();

    wb.setSheetName(0, "sheet1");

    int size = released.size();
    System.out.println("released==========>" + released);
    for (int rownum = 0; rownum < size; rownum++) {
      Object object = released.get(rownum);

      String number = "";
      String name = "";
      String banben = "";
      String tuzhirongqi = "";
      String docoid = "";
      String modifier = "";
      String chanpindaihao = "";
      String mingcheng = "";
      String statesymbol = "";
      String cailiaoguige = "";
      String beizhu = "";

      String cname = "";
      String state = "";
      String representationable = "无";
      if ((object instanceof WTDocument)) {
        System.out.println("zyj---test---come in eco doc");
        WTDocument doc = (WTDocument)released.get(rownum);
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(doc);
        if (representation != null) {
          representationable = "有";
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");

        mingcheng = iba.getIBAValue("tuwendangmingcheng");
        statesymbol = iba.getIBAValue("statesymbol");
        cailiaoguige = iba.getIBAValue("cailiaoguige");
        beizhu = iba.getIBAValue("beizhu");
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof EPMDocument))
      {
        System.out.println("zyj---test---come in eco epm");
        EPMDocument doc = (EPMDocument)released.get(rownum);
        Representation representation = null;

        ContentHolder ch = ContentHelper.service
          .getContents(doc);
        Vector attachmentList = 
          ContentHelper.getApplicationData(ch);
        System.out.println("zyj--test--is ACAD:" + doc.getAuthoringApplication().toString());
        if (doc.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
          if (attachmentList.size() > 0)
            representationable = "有";
        }
        else {
          representation = RepresentationHelper.service
            .getDefaultRepresentation(doc);
          System.out.println("zyj--test===representation>" + representation);
          if ((representation != null) || (attachmentList.size() > 0)) {
            representationable = "有";
          }
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        cname = doc.getCADName();
        cname = trimExtension(cname);
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof WTPart)) {
        System.out.println("zyj---test---come in eco part");
        WTPart part = (WTPart)released.get(rownum);
        QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
        System.out.println(qrepm.getEnumeration());
        while (qrepm.hasMoreElements()) {
          try {
            EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
            System.out.println("epm3d===>" + epm3d);
            EPMDocument epm2d = PrintPartBOMUtil.getDrawing(epm3d, 
              null);
            System.out.println("epm2d===>" + epm2d);
            Representation representation = RepresentationHelper.service
              .getDefaultRepresentation(epm2d);
            System.out.println(epm2d + "epm2d===>" + representation);
            if (representation != null)
              representationable = "有";
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        IBAUtil iba = new IBAUtil(part);
        number = part.getNumber();
        name = part.getName();
        banben = part.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(part);
        modifier = part.getModifierFullName();
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = part.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      }

      r = s.createRow(rownum + 2);

      c = r.createCell(0);
      cell_cs = r_temp.getCell(0);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(rownum + 1);

      c = r.createCell(1);
      cell_cs = r_temp.getCell(1);
      cs2 = cell_cs.getCellStyle();
      c.setCellType(1);
      c.setCellStyle(cs2);
      c.setCellValue(number);

      c = r.createCell(2);
      cell_cs = r_temp.getCell(2);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(name);

      c = r.createCell(3);
      cell_cs = r_temp.getCell(3);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(representationable);

      c = r.createCell(4);
      cell_cs = r_temp.getCell(4);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(banben);

      c = r.createCell(5);
      cell_cs = r_temp.getCell(5);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(state);

      c = r.createCell(6);
      cell_cs = r_temp.getCell(6);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue("1");

      c = r.createCell(7);
      cell_cs = r_temp.getCell(7);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(tuzhirongqi);

      c = r.createCell(8);
      cell_cs = r_temp.getCell(8);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(beizhu);

      c = r.createCell(20);

      c.setCellValue(docoid);
    }

    wb.write(out);
    out.close();
    in.close();
    return filename;
  }

  public static String createForBOM(String docNumber, ArrayList released, ArrayList UNreleased, String partString)
    throws IOException, WTException, PropertyVetoException
  {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    WTUser user = null;
    try {
      user = (WTUser)SessionHelper.getPrincipal();
    } catch (WTException e) {
      e.printStackTrace();
    }
    String dysqrFullName = user.getFullName();
    Date date = new Date();
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    String dateStr = sd.format(date);
    String filename = PrintUtil2.getTempFilePath(null) + docNumber + "_打印申请单_" + partString + ".xls";
    String filename_in = PrintUtil2.getTempleteFilePath(null) + "图纸打印申请单.xls";
    System.out.println("BOM===>filename=======>" + filename);
    System.out.println("BOM===>filename_in====>" + filename_in);

    copy(filename_in, filename);
    FileOutputStream out = new FileOutputStream(filename);
    FileInputStream in = new FileInputStream(filename_in);

    HSSFWorkbook wb = new HSSFWorkbook(in);
    HSSFSheet s = wb.getSheetAt(0);

    Row r = null;
    Row r_temp = null;
    Cell c = null;
    CellStyle cs2 = wb.createCellStyle();
    cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
    r_temp = s.getRow(2);
    Cell cell_cs = r_temp.getCell(0);
    cs2 = cell_cs.getCellStyle();

    wb.setSheetName(0, "sheet1");

    int size = released.size();
    System.out.println("released==========>" + released);
    for (int rownum = 0; rownum < size; rownum++) {
      Object object = released.get(rownum);

      String number = "";
      String name = "";
      String banben = "";
      String tuzhirongqi = "";
      String docoid = "";
      String modifier = "";
      String chanpindaihao = "";
      String mingcheng = "";
      String statesymbol = "";
      String cailiaoguige = "";
      String beizhu = "";

      String cname = "";
      String state = "";
      String representationable = "无";
      if ((object instanceof WTDocument)) {
        System.out.println("zyj--test---wtdoc");
        WTDocument doc = (WTDocument)released.get(rownum);
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(doc);
        System.out.println("zyj--test--representation>" + representation);
        ContentHolder ch = ContentHelper.service
          .getContents(doc);
        Vector attachmentList = 
          ContentHelper.getApplicationData(ch);
        if ((representation != null) || (attachmentList.size() > 0)) {
          representationable = "有";
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");

        mingcheng = iba.getIBAValue("tuwendangmingcheng");
        statesymbol = iba.getIBAValue("statesymbol");
        cailiaoguige = iba.getIBAValue("cailiaoguige");
        beizhu = iba.getIBAValue("beizhu");
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof EPMDocument))
      {
        System.out.println("zyj--test---epmDoc");
        EPMDocument doc = (EPMDocument)released.get(rownum);
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(doc);
        ContentHolder ch = ContentHelper.service
          .getContents(doc);
        Vector attachmentList = 
          ContentHelper.getApplicationData(ch);
        System.out.println("zyj--test--representation>" + representation);
        if ((representation != null) || (attachmentList.size() > 0)) {
          representationable = "有";
        }
        IBAUtil iba = new IBAUtil(doc);
        number = doc.getNumber();
        name = doc.getName();
        banben = doc.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(doc);
        modifier = doc.getModifierFullName();
        cname = doc.getCADName();
        cname = trimExtension(cname);
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = doc.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      } else if ((object instanceof WTPart)) {
        System.out.println("zyj--test---wtpart");
        WTPart part = (WTPart)released.get(rownum);
        QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
        System.out.println(qrepm.getEnumeration());
        while (qrepm.hasMoreElements()) {
          try {
            EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
            System.out.println("epm3d===>" + epm3d);
            ContentHolder ch = ContentHelper.service
              .getContents(epm3d);
            Vector attachmentList = 
              ContentHelper.getApplicationData(ch);
            System.out.println("zyj--test--is ACAD:" + epm3d.getAuthoringApplication().toString());
            if (epm3d.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
              if (attachmentList.size() > 0)
                representationable = "有";
            }
            else {
              EPMDocument epm2d = PrintPartBOMUtil.getDrawing(epm3d, 
                null);
              System.out.println("epm2d===>" + epm2d);
              Representation representation = RepresentationHelper.service
                .getDefaultRepresentation(epm2d);
              System.out.println(epm2d + "epm2d===representation>" + representation);
              if ((representation != null) || (attachmentList.size() > 0)) {
                representationable = "有";
              }
            }

          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
        IBAUtil iba = new IBAUtil(part);
        number = part.getNumber();
        name = part.getName();
        banben = part.getIterationDisplayIdentifier().toString();
        tuzhirongqi = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        docoid = referenceFactory.getReferenceString(part);
        modifier = part.getModifierFullName();
        chanpindaihao = iba.getIBAValue("FIRST_APPLICATION_PRODUCTION");
        state = part.getLifeCycleState().getLocalizedMessage(Locale.SIMPLIFIED_CHINESE);
      }

      r = s.createRow(rownum + 2);

      c = r.createCell(0);
      cell_cs = r_temp.getCell(0);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(rownum + 1);

      c = r.createCell(1);
      cell_cs = r_temp.getCell(1);
      cs2 = cell_cs.getCellStyle();
      c.setCellType(1);
      c.setCellStyle(cs2);
      c.setCellValue(number);

      c = r.createCell(2);
      cell_cs = r_temp.getCell(2);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(name);

      c = r.createCell(3);
      cell_cs = r_temp.getCell(3);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(representationable);

      c = r.createCell(4);
      cell_cs = r_temp.getCell(4);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(banben);

      c = r.createCell(5);
      cell_cs = r_temp.getCell(5);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(state);

      c = r.createCell(6);
      cell_cs = r_temp.getCell(6);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue("1");

      c = r.createCell(7);
      cell_cs = r_temp.getCell(7);
      cs2 = cell_cs.getCellStyle();
      c.setCellStyle(cs2);
      c.setCellValue(tuzhirongqi);

      c = r.createCell(8);
      cell_cs = r_temp.getCell(8);
      c.setCellValue(beizhu);

      c = r.createCell(20);

      c.setCellValue(docoid);
    }

    wb.write(out);
    out.close();
    in.close();
    return filename;
  }

  public static WTDocument getNewRelease(String partnumber, String version) {
    String nrNumber = partnumber;
    try
    {
      QuerySpec qs = new QuerySpec(WTDocument.class);
      qs.appendWhere(
        new SearchCondition(WTDocument.class, 
        "master>number", "=", nrNumber
        .toUpperCase(), false));
      qs.appendAnd();

      qs.appendWhere(
        new SearchCondition(WTDocument.class, 
        "versionInfo.identifier", 
        "=", version));
      QueryResult qr = PersistenceHelper.manager.find(qs);

      qr = new OwnershipIndependentLatestConfigSpec().process(qr);
      if (qr.hasMoreElements()) {
        WTDocument doc = (WTDocument)qr.nextElement();
        boolean tydoc = isTYdoc(doc);
        if (!tydoc) {
          return null;
        }
        return doc;
      }
    } catch (WTException e) {
      e.printStackTrace();
    }

    return null;
  }

  public static boolean isTYdoc(WTDocument doc) {
    boolean flag = false;
    if (doc == null) {
      return flag;
    }
    String typeName = TypeIdentifierUtility.getTypeIdentifier(doc)
      .getTypename();
    System.out.println("typeName->" + typeName);
    String tyType = CSRUtils.getProperty("csr.ty.doctype");
    if (typeName.indexOf(tyType) != -1) {
      flag = true;
    }
    return flag;
  }

  public static Persistable getNewRelease(String partnumber, String version, Class class_select) {
    String nrNumber = partnumber;
    try
    {
      List list = Select.from(class_select).where(0, "master>number", "LIKE", partnumber, false).list();
      for (int i = 0; i < list.size(); i++) {
        RevisionControlled rev = (RevisionControlled)list.get(i);

        System.out.println("version===>" + version + "====getSeries=======>" + rev.getVersionInfo().getIdentifier().getValue());
        if (version.startsWith(rev.getVersionInfo().getIdentifier().getValue())) {
          System.out.println("result>>>>>>>>>>>" + Select.latest((Iterated)list.get(i)));
          return Select.latest((Iterated)list.get(i));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static URL zip(WTObject pbo) throws Exception {
    String downpath = PrintUtil2.getTempFilePath(null);
    WTDocument doc = (WTDocument)pbo;
    IBAUtil ibUtil = new IBAUtil(doc);
    flag = ibUtil.getIBAValue("DYBS");
    System.out.println("zyj--test--flag:" + flag);
    String outZipFile = downpath + doc.getNumber() + ".zip";
    String inputPath = downpath + doc.getNumber() + "/";

    File dir = new File(inputPath);
    if ((dir.isDirectory()) && (dir.exists())) {
      dir.delete();
    }
    dir = new File(inputPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    ReferenceFactory referenceFactory = new ReferenceFactory();
    String docdownloadfolder = printdownfolder + doc.getNumber();
    ContentItem item = ContentHelper.service.getPrimary(doc);

    String contentFileName = "";
    if ((item instanceof ApplicationData)) {
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      contentFileName = downpath + contentFileName;
      ContentServerHelper.service.writeContentStream(appData, 
        contentFileName);
      if (contentFileName.endsWith(".xls")) {
        String actcontentFileName = docdownloadfolder + File.separator;
        File file = new File(actcontentFileName);
        if (!file.exists()) {
          file.mkdirs();
        }
        actcontentFileName = actcontentFileName + appData.getFileName();
        ContentServerHelper.service.writeContentStream(appData, 
          actcontentFileName);
      }
    }

    Vector attachmentList = ContentHelper.getApplicationData(doc);
    System.out.println("attachmentList.size========>" + attachmentList.size());
    for (int i = 0; i < attachmentList.size(); i++) {
      ApplicationData applicationdata = 
        (ApplicationData)attachmentList
        .get(i);
      String actcontentFileName = inputPath + File.separator;
      File file = new File(actcontentFileName);
      if (!file.exists()) {
        file.mkdirs();
      }
      actcontentFileName = actcontentFileName + applicationdata.getFileName();
      ContentServerHelper.service.writeContentStream(applicationdata, 
        actcontentFileName);
    }

    if (!contentFileName.endsWith(".xls")) {
      throw new WTException("打印申请单内容不是xls文件.");
    }
    int r = 0;
    int c = 0;

    String filename = contentFileName;
    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    int rows = sheet.getPhysicalNumberOfRows();

    for (r = 1; r < rows; r++) {
      HSSFRow row = sheet.getRow(r);
      if (row != null) {
        int cells = 6;

        HSSFCell cell = row.getCell(20);
        String docoid = null;
        Object object = null;

        System.out.println("cell===>" + cell);
        if (cell != null) {
          try {
            docoid = cell.getStringCellValue();

            System.out.println("docoid===>" + docoid);
            object = referenceFactory.getReference(docoid).getObject();
          }
          catch (Exception e)
          {
            e.printStackTrace();
            object = null;
          }
        }
        if (object == null) {
          if (row.getCell(1) == null) {
            continue;
          }
          row.getCell(1).setCellType(1);
          if ((row.getCell(1).getStringCellValue() == null) || 
            (row.getCell(1).getStringCellValue().equals("")))
          {
            continue;
          }
          row.getCell(1).setCellType(1);
          String number = row.getCell(1).getStringCellValue();
          String version = row.getCell(4).getStringCellValue();

          object = getNewRelease(number, version, WTPart.class);

          if (object == null) {
            object = getNewRelease(number, version, WTDocument.class);
          }
          if (object == null) {
            object = getNewRelease(number, version, EPMDocument.class);
          }
        }
        if (object != null)
        {
          System.out.println("object==========>" + object);
          if ((object != null) && 
            ((object instanceof EPMDocument))) {
            System.out.println("zyj----test----download epm");
            EPMDocument epmDocument = (EPMDocument)object;

            downLoad(epmDocument, inputPath);
          } else if ((object != null) && ((object instanceof WTDocument)) && (object.getClass().getName().endsWith(".pdf"))) {
            System.out.println("zyj---test---download doc");
            WTDocument wtDocument = (WTDocument)object;

            String pdfString = downloadAttachment(wtDocument, inputPath);
            if (pdfString == null)
            {
              downLoad(wtDocument, inputPath);
            }
          } else if ((object != null) && ((object instanceof WTPart))) {
            System.out.println("zyj----test----download part");
            WTPart part = (WTPart)object;
            QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
            if (qrepm.hasMoreElements()) {
              EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
              EPMDocument epm2dDocument = PrintPartBOMUtil.getDrawing(epm3d, 
                null);
              System.out.println("zyj---test---epm2dDocument:" + epm2dDocument);
              if (epm2dDocument == null) {
                Vector vec = get2DEPMDocumentByPart(part);
                if (vec.size() > 0) {
                  for (int i = 0; i < vec.size(); i++) {
                    epm2dDocument = (EPMDocument)vec.get(i);
                  }
                }
              }
              else
              {
                downLoadByState(epm2dDocument, inputPath, part.getLifeCycleState().getDisplay());
              }
            }
          }
        }
      }
    }
    System.out.println("outZipFile========>" + outZipFile);
    System.out.println("inputPath========>" + inputPath);
    zipFileUtil.zipFile(outZipFile, inputPath);
    int pos = outZipFile.indexOf("download");
    outZipFile = outZipFile.substring(pos);
    URLFactory urlfactory = new URLFactory();
    String strUrl = urlfactory.getHREF(outZipFile);
    URL url = new URL(strUrl);
    return url;
  }

  public static String zipFile(WTObject pbo) throws Exception {
    String downpath = PrintUtil2.getTempFilePath(null);
    WTDocument doc = (WTDocument)pbo;

    String outZipFile = downpath + doc.getNumber() + ".zip";
    String inputPath = downpath + doc.getNumber() + "/";

    File dir = new File(inputPath);
    if ((dir.isDirectory()) && (dir.exists())) {
      dir.delete();
    }
    dir = new File(inputPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    ReferenceFactory referenceFactory = new ReferenceFactory();
    String docdownloadfolder = printdownfolder + doc.getNumber();
    ContentItem item = ContentHelper.service.getPrimary(doc);

    String contentFileName = "";
    if ((item instanceof ApplicationData)) {
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      contentFileName = downpath + contentFileName;
      ContentServerHelper.service.writeContentStream(appData, 
        contentFileName);
      if (contentFileName.endsWith(".xls")) {
        String actcontentFileName = docdownloadfolder + File.separator;
        File file = new File(actcontentFileName);
        if (!file.exists()) {
          file.mkdirs();
        }
        actcontentFileName = actcontentFileName + appData.getFileName();
        ContentServerHelper.service.writeContentStream(appData, 
          actcontentFileName);
      }
    }

    Vector attachmentList = ContentHelper.getApplicationData(doc);
    System.out.println("attachmentList.size========>" + attachmentList.size());
    for (int i = 0; i < attachmentList.size(); i++) {
      ApplicationData applicationdata = 
        (ApplicationData)attachmentList
        .get(i);
      String actcontentFileName = inputPath + File.separator;
      File file = new File(actcontentFileName);
      if (!file.exists()) {
        file.mkdirs();
      }
      actcontentFileName = actcontentFileName + applicationdata.getFileName();
      ContentServerHelper.service.writeContentStream(applicationdata, 
        actcontentFileName);
    }

    if (!contentFileName.endsWith(".xls")) {
      throw new WTException("打印申请单内容不是xls文件.");
    }
    int r = 0;
    int c = 0;

    String filename = contentFileName;
    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    int rows = sheet.getPhysicalNumberOfRows();

    for (r = 1; r < rows; r++) {
      HSSFRow row = sheet.getRow(r);
      if (row != null) {
        int cells = 6;

        HSSFCell cell = row.getCell(20);
        String docoid = null;
        Object object = null;

        System.out.println("cell===>" + cell);
        if (cell != null) {
          try {
            docoid = cell.getStringCellValue();
            System.out.println("docoid===>" + docoid);
            object = referenceFactory.getReference(docoid)
              .getObject();
          }
          catch (Exception e) {
            e.printStackTrace();
            object = null;
          }
        }
        if (object == null) {
          if (row.getCell(1) == null) {
            continue;
          }
          row.getCell(1).setCellType(1);
          if ((row.getCell(1).getStringCellValue() == null) || 
            (row.getCell(1).getStringCellValue().equals("")))
          {
            continue;
          }
          row.getCell(1).setCellType(1);
          String number = row.getCell(1).getStringCellValue();
          String version = row.getCell(4).getStringCellValue();

          object = getNewRelease(number, version, WTPart.class);

          if (object == null) {
            object = getNewRelease(number, version, WTDocument.class);
          }
          if (object == null) {
            object = getNewRelease(number, version, EPMDocument.class);
          }
        }
        if (object != null)
        {
          System.out.println("object==========>" + object);
          if ((object != null) && 
            ((object instanceof EPMDocument))) {
            EPMDocument epmDocument = (EPMDocument)object;

            downLoad(epmDocument, inputPath);
          } else if ((object != null) && ((object instanceof WTDocument))) {
            WTDocument wtDocument = (WTDocument)object;

            String pdfString = downloadAttachment(wtDocument, inputPath);
            if (pdfString == null)
            {
              downLoad(wtDocument, inputPath);
            }
          } else if ((object != null) && ((object instanceof WTPart))) {
            WTPart part = (WTPart)object;
            QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
            if (qrepm.hasMoreElements()) {
              EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
              EPMDocument epm2dDocument = PrintPartBOMUtil.getDrawing(epm3d, 
                null);
              if (epm2dDocument != null)
              {
                downLoadByState(epm2dDocument, inputPath, part.getLifeCycleState().getDisplay());
              }
            }
          }
        }
      }
    }
    System.out.println("outZipFile========>" + outZipFile);
    System.out.println("inputPath========>" + inputPath);
    zipFileUtil.zipFile(outZipFile, inputPath);
    int pos = outZipFile.indexOf("download");
    outZipFile = outZipFile.substring(pos);
    URLFactory urlfactory = new URLFactory();
    String strUrl = urlfactory.getHREF(outZipFile);
    URL url = new URL(strUrl);
    return outZipFile;
  }

  public static String downLoad(WTDocument wtdocument, String rootPath) throws WTException, PropertyVetoException, IOException
  {
    String downloadDirectoryStr = "";

    String contentFileName = null;
    ContentItem item = ContentHelper.service
      .getPrimary(wtdocument);
    if ((item instanceof ApplicationData)) {
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      if (contentFileName.toUpperCase().endsWith(".DWG")) {
        downloadDirectoryStr = rootPath + File.separator + "DWG";
      }
      else
      {
        downloadDirectoryStr = rootPath + File.separator + "DOC";
      }
      File file = new File(downloadDirectoryStr + File.separator);
      if (!file.exists()) {
        file.mkdirs();
      }
      downloadDirectoryStr = downloadDirectoryStr + File.separator + contentFileName;
      ContentServerHelper.service.writeContentStream(appData, 
        downloadDirectoryStr);
    }

    return contentFileName;
  }

  public static WTPart getWTPart(String number) {
    return getWTPart(number, "", null);
  }

  public static WTPart getWTPart(String number, String viewName) {
    return getWTPart(number, viewName, null);
  }

  public static WTPart getWTPart(String number, String viewName, String stateDisplay)
  {
    try
    {
      View view = null;
      if (StringUtils.isNotEmpty(viewName)) {
        view = ViewHelper.service.getView(viewName);
      }

      State state = null;
      if (StringUtils.isNotEmpty(stateDisplay)) {
        state = State.toState(stateDisplay);
      }

      return getWTPart(number, view, state);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static WTPart getWTPart(String number, View view, State state) throws WTException, WTPropertyVetoException
  {
    if (StringUtils.isEmpty(number)) {
      return null;
    }
    WTPart wtpart = null;
    WTPart returnpart = null;
    QuerySpec qs = new QuerySpec(WTPart.class);

    SearchCondition scLatestIteration = new SearchCondition(WTPart.class, 
      "iterationInfo.latest", "TRUE");
    qs.appendWhere(scLatestIteration, new int[1]);

    ClassAttribute caNumber = new ClassAttribute(WTPart.class, 
      "master>number");
    SQLFunction upperNumber = SQLFunction.newSQLFunction("UPPER", caNumber);
    SearchCondition scNumber = new SearchCondition(upperNumber, 
      "=", ConstantExpression.newExpression(number
      .toUpperCase()));
    qs.appendAnd();
    qs.appendWhere(scNumber, new int[1]);
    QueryResult qr = PersistenceHelper.manager.find(qs);

    if (qr.hasMoreElements()) {
      wtpart = (WTPart)qr.nextElement();
    }

    if (wtpart == null) {
      return null;
    }

    QueryResult localQueryResult = null;
    if (WorkInProgressHelper.isWorkingCopy(wtpart))
      wtpart = (WTPart)WorkInProgressHelper.service
        .originalCopyOf(wtpart);
    try
    {
      int i = ((wtpart instanceof OneOffVersioned)) && 
        (VersionControlHelper.isAOneOff(wtpart)) ? 
        1 : 0;
      WTContainerRef localWTContainerRef = wtpart
        .getContainerReference();

      if ((i != 0) && 
        (Project2.class.isAssignableFrom(localWTContainerRef
        .getReferencedClass()))) {
        System.out.println("1111:");
        localQueryResult = VersionControlHelper.service
          .iterationsOf(wtpart.getIterationInfo().getBranchId());
      } else {
        System.out.println("2222");
        localQueryResult = VersionControlHelper.service
          .allIterationsOf(wtpart.getMaster());
      }
    } catch (WTException e) {
      e.printStackTrace();
    }

    localQueryResult = process(localQueryResult, view);

    while ((localQueryResult != null) && (localQueryResult.hasMoreElements())) {
      WTPart wtpartTemp = (WTPart)localQueryResult.nextElement();

      System.out.println("wtpartTemp:" + wtpartTemp + "  " + 
        wtpartTemp.getIterationDisplayIdentifier().toString() + 
        "  " + wtpartTemp.getViewName());

      if (!WorkInProgressHelper.isWorkingCopy(wtpartTemp))
      {
        if ((!(wtpartTemp instanceof OneOffVersioned)) || 
          (!VersionControlHelper.isAOneOff(wtpartTemp)))
        {
          if ((state == null) || 
            (state.equals(wtpartTemp.getState().getState())))
          {
            returnpart = wtpartTemp;

            break;
          }
        }
      }
    }
    return returnpart;
  }

  public static QueryResult process(QueryResult paramQueryResult, View view) throws WTException
  {
    if ((paramQueryResult == null) || (!paramQueryResult.hasMoreElements())) {
      return new QueryResult();
    }
    ObjectVector localObjectVector = new ObjectVector();
    HashMap localHashMap = new HashMap();
    while (paramQueryResult.hasMoreElements()) {
      Object localObject1 = paramQueryResult.nextElement();
      if (!(localObject1 instanceof ViewManageable)) {
        Object[] localObject2 = { "results", 
          ViewManageable.class.getName() };
        throw new ConfigException("wt.vc.config.configResource", "1", 
          localObject2);
      }

      if (ViewHelper.getView((ViewManageable)localObject1) == null) {
        localObjectVector.addElement(localObject1);
      } else {
        Object localObject2 = ((ViewManageable)localObject1)
          .getMaster();
        if (localHashMap.get(localObject2) == null) {
          localHashMap.put(localObject2, new ArrayList());
        }
        ((Collection)localHashMap.get(localObject2))
          .add((ViewManageable)localObject1);
      }
    }
    paramQueryResult.reset();
    Iterator localIterator;
    for (Object localObject1 = localHashMap.entrySet().iterator(); ((Iterator)localObject1)
      .hasNext(); 
      localIterator.hasNext())
    {
      Object localObject2 = (Map.Entry)((Iterator)localObject1).next();
      System.out.println("localObject2:" + localObject2);
      Collection localCollection = 
        (Collection)((Map.Entry)localObject2).getValue();

      localCollection = filterByView(localCollection, view);

      localIterator = localCollection.iterator(); continue; ViewManageable localViewManageable = (ViewManageable)localIterator.next();
      localObjectVector.addElement(localViewManageable);
    }

    return new QueryResult(localObjectVector);
  }

  protected static Collection<ViewManageable> filterByView(Collection<ViewManageable> paramCollection, View paramView)
    throws WTException
  {
    ArrayList localHashSet = new ArrayList();

    if ((paramCollection == null) || (paramCollection.isEmpty())) {
      return localHashSet;
    }

    if (paramView == null) {
      localHashSet.addAll(paramCollection);
      return localHashSet;
    }

    View localView = paramView;
    while ((localHashSet.isEmpty()) && (localView != null)) {
      for (ViewManageable localViewManageable : paramCollection) {
        if (PersistenceHelper.isEquivalent(localView, 
          ViewHelper.getView(localViewManageable)))
        {
          localHashSet.add(localViewManageable);
        }
      }
      localView = ViewHelper.service.getParent(localView);
    }
    return localHashSet;
  }

  public static void downloadpdf2(EPMDocument EPMdoc, String rootPath)
    throws WTException, PropertyVetoException, IOException
  {
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
          String extention = Util.getExtension(filename);
          String removeExtention = Util.removeExtension(filename);
          if (extention.equalsIgnoreCase("PDF")) {
            String path = rootPath + filename;
            downloadFile(in, path);
            String doctypeString = EPMdoc.getDocType().toString();
            System.out.println(doctypeString);
            System.out.println(EPMdoc.getDocType().getDisplay(
              Locale.SIMPLIFIED_CHINESE));
            System.out.println(EPMdoc.getAuthoringApplication()
              .toString());
          }
        }
      }
    }
  }

  public static void downloadpdf1(WTDocument doc, String rootPath)
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
            String path = rootPath + filename;
            downloadFile(in, path);
            String doctypeString = doc.getDocType().toString();
            System.out.println(doctypeString);
            System.out.println(doc.getDocType().getDisplay(
              Locale.SIMPLIFIED_CHINESE));
          }
        }
      }
    }
  }

  public static String downloadAttachment(WTDocument doc, String rootPath)
    throws WTException, PropertyVetoException, IOException
  {
    String downloadDirectoryStr = "";
    ContentHolder ch = ContentHelper.service
      .getContents(doc);
    Vector attachmentList = ContentHelper.getApplicationData(ch);
    for (int i = 0; i < attachmentList.size(); i++) {
      ApplicationData appDataPDF = 
        (ApplicationData)attachmentList
        .get(i);

      String contentFileName = appDataPDF.getFileName();
      if ((!contentFileName.toLowerCase().endsWith(".xls")) && (!contentFileName.toLowerCase().endsWith(".xlsx"))) {
        System.out.println("contentFileName==1111==>" + contentFileName);
      } else {
        System.out.println("contentFileName==2222==>" + contentFileName);
        String tempPdfPath = PrintUtil2.getTempFilePath("temppdf");
        String absoluteFileName = tempPdfPath + contentFileName;
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          absoluteFileName);

        String tufu = tufu(absoluteFileName);
        System.out.println("tufu====>" + tufu);
        downloadDirectoryStr = rootPath;

        File file = new File(downloadDirectoryStr + File.separator);
        if (!file.exists()) {
          file.mkdirs();
        }

        downloadDirectoryStr = downloadDirectoryStr + File.separator + contentFileName;
        System.out.println("downloadDirectoryStr=====>" + downloadDirectoryStr);
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          downloadDirectoryStr);
        return downloadDirectoryStr;
      }
    }

    return null;
  }

  public static String downloadpdf2(Representable doc, String rootPath)
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
          if (filename.indexOf(".pdf") > 0)
          {
            String tempPdfPath = 
              PrintUtil2.getTempFilePath("temppdf");
            String absoluteFileName = tempPdfPath + filename;
            downloadFile(in, absoluteFileName);

            String tufu = tufu(absoluteFileName);
            File tempFile = new File(absoluteFileName);
            tempFile.delete();
            in = ContentServerHelper.service
              .findContentStream(applicationdata);
            absoluteFileName = rootPath + File.separator + tufu + 
              File.separator;
            File file = new File(absoluteFileName);
            if (!file.exists()) {
              file.mkdirs();
            }
            String number = "empty";
            if ((doc instanceof WTDocument)) {
              WTDocument document = (WTDocument)doc;
              number = document.getNumber();
            } else if ((doc instanceof EPMDocument)) {
              EPMDocument epmDocument = (EPMDocument)doc;
              number = epmDocument.getNumber();
            }

            absoluteFileName = absoluteFileName + number + ".pdf";
            downloadFile(in, absoluteFileName);
            return absoluteFileName;
          }
        }
      }
    }
    return null;
  }

  public static String downloadpdf3(Representable doc, String rootPath) throws WTException, PropertyVetoException, IOException
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
          if (filename.indexOf(".pdf") > 0)
          {
            String absoluteFileName = rootPath + File.separator + 
              filename;
            downloadFile(in, absoluteFileName);
            return absoluteFileName;
          }
        }
      }
    }
    return null;
  }

  public static WTContainer getWTLibraryContainerByName(String libraryName)
    throws WTException, RemoteException, InvocationTargetException
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "getWTLibraryContainerByName";
      String klass = PrintRequestUtil.class.getName();
      Class[] types = { String.class };
      Object[] vals = { libraryName };

      return (WTContainer)RemoteMethodServer.getDefault().invoke(method, 
        klass, null, types, vals); } WTLibrary wtlibrary = new WTLibrary();
    WTContainer wtcontainer = null;

    WTUser currentuser = (WTUser)SessionHelper.manager.getPrincipal();
    WTUser admin = (WTUser)SessionHelper.manager.setAdministrator();
    WTUser localWTUser1;
    WTUser localWTUser1;
    try { QuerySpec qs = new QuerySpec(WTLibrary.class);
      SearchCondition sc = new SearchCondition(WTLibrary.class, 
        "containerInfo.name", "LIKE", libraryName);
      qs.appendWhere(sc);
      QueryResult qr = PersistenceHelper.manager.find(qs);
      while (qr.hasMoreElements()) {
        wtlibrary = (WTLibrary)qr.nextElement();
        if (wtlibrary != null)
        {
          if ((wtlibrary instanceof WTContainer))
          {
            wtcontainer = wtlibrary;
          }
          else
            wtcontainer = WTContainerHelper.getContainer(wtlibrary);
        }
      }
    } finally
    {
      localWTUser1 = (WTUser)SessionHelper.manager
        .setPrincipal(currentuser.getAuthenticationName());
    }
    return wtcontainer;
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
        int len;
        outputStream1.write(buffer, 0, len);
      }
      inputStream.close();
      outputStream1.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void readXls(String filename, String filename2)
    throws IOException
  {
    int r = 0;
    int c = 0;

    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    int rows = sheet.getPhysicalNumberOfRows();

    for (r = 0; r < rows; r++) {
      HSSFRow row = sheet.getRow(r);
      if (row != null) {
        int cells = 6;

        for (c = 0; c < cells; c++) {
          HSSFCell cell = row.getCell(Short.parseShort(
            String.valueOf(c)));
          String value = null;
          if (cell != null) {
            switch (cell.getCellType())
            {
            case 0:
              value = String.valueOf(cell.getNumericCellValue());
              value = value.substring(0, value.indexOf("."));
              break;
            case 1:
              value = cell.getStringCellValue();
              cell.setCellValue("------------");
              break;
            default:
              value = ""; break;
            }
          }
          else
          {
            value = "";
          }

          System.out.print("--" + value + "--");
        }

        System.out.println("");
      }
    }
    FileOutputStream out = new FileOutputStream(filename2);
    wk.write(out);
    out.close();
  }

  public static WTDocument updateXls(WTObject pbo, String fenshu, String yuanyin)
    throws IOException, WTException, PropertyVetoException
  {
    WTDocument doc = (WTDocument)pbo;
    doc = (WTDocument)PersistenceHelper.manager.refresh(doc);
    String downpath = PrintUtil2.getTempFilePath(null);
    ContentItem item = ContentHelper.service.getPrimary(doc);
    String contentFileName = "";
    if ((item instanceof ApplicationData)) {
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      if (contentFileName.endsWith(".xls"))
      {
        ContentServerHelper.service.writeContentStream(appData, 
          downpath + contentFileName);
      }
    }
    if (!contentFileName.endsWith(".xls")) {
      return doc;
    }
    int r = 0;
    int c = 0;
    String filename = downpath + contentFileName;
    File file = new File(downpath + "to" + File.separator);
    if (!file.exists()) {
      file.mkdirs();
    }
    String filename2 = downpath + "to" + File.separator + contentFileName;

    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    int rows = sheet.getPhysicalNumberOfRows();
    if (rows < 2) {
      return doc;
    }

    HSSFRow row = sheet.getRow(1);
    if (row != null)
    {
      HSSFCell cell = row.getCell(2);
      if (cell == null) {
        cell = row.createCell(2);
      }
      cell.setCellValue(yuanyin);
      cell = row.getCell(3);
      if (cell == null) {
        cell = row.createCell(3);
      }
      cell.setCellValue(fenshu);
    }

    FileOutputStream out = new FileOutputStream(filename2);
    wk.write(out);
    out.close();
    ContentServerHelper.service.updateContent(doc, (ApplicationData)item, 
      filename2);

    doc = (WTDocument)PersistenceHelper.manager.refresh(doc);
    return doc;
  }

  public static void downloadToPrint(WTObject pbo) throws WTException, PropertyVetoException, IOException
  {
    if (!(pbo instanceof WTDocument)) {
      return;
    }

    ReferenceFactory referenceFactory = new ReferenceFactory();
    String downpath = PrintUtil2.getTempFilePath(null);
    WTDocument doc = (WTDocument)pbo;
    String docdownloadfolder = printdownfolder + doc.getNumber();
    ContentItem item = ContentHelper.service.getPrimary(doc);
    String contentFileName = "";
    if ((item instanceof ApplicationData)) {
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      contentFileName = downpath + contentFileName;
      ContentServerHelper.service.writeContentStream(appData, 
        contentFileName);
      if (contentFileName.endsWith(".xls")) {
        String actcontentFileName = docdownloadfolder + File.separator;
        File file = new File(actcontentFileName);
        if (!file.exists()) {
          file.mkdirs();
        }
        actcontentFileName = actcontentFileName + appData.getFileName();
        ContentServerHelper.service.writeContentStream(appData, 
          actcontentFileName);
      }
    }

    if (!contentFileName.endsWith(".xls")) {
      throw new WTException("打印申请单内容不是xls文件.");
    }
    int r = 0;
    int c = 0;

    String filename = contentFileName;
    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    int rows = sheet.getPhysicalNumberOfRows();

    for (r = 1; r < rows; r++) {
      HSSFRow row = sheet.getRow(r);
      if (row != null) {
        int cells = 6;

        HSSFCell cell = row.getCell(20);
        String docoid = null;
        Object object = null;

        if (cell != null) {
          try {
            docoid = cell.getStringCellValue();
            object = referenceFactory.getReference(docoid)
              .getObject();
          }
          catch (Exception e) {
            e.printStackTrace();
            object = null;
          }

        }

        if (object == null) {
          row.getCell(1).setCellType(1);
          cell = row.getCell(1);
          String number = cell.getStringCellValue();
          cell = row.getCell(3);
          String iterateString = cell.getStringCellValue();
          int spaceView = iterateString.indexOf(" ");
          if (spaceView != -1) {
            iterateString = iterateString.substring(0, spaceView);
          }

          object = findPartWithView(number, "Design", iterateString);
        }

        if ((object instanceof EPMDocument)) {
          System.out.println("zyj---test---download epmdoc");
          EPMDocument epmDocument = (EPMDocument)object;

          downLoad(epmDocument, docdownloadfolder);
        } else if ((object instanceof WTDocument)) {
          System.out.println("zyj---test---download doc");
          WTDocument wtDocument = (WTDocument)object;
          String pdfString = downloadpdf2(wtDocument, 
            docdownloadfolder);
          if (pdfString == null)
          {
            downLoad(wtDocument, docdownloadfolder);
          }
        } else if ((object instanceof WTPart)) {
          System.out.println("zyj---test---download part");
          WTPart part = (WTPart)object;
          QueryResult qrepm = PrintPartBOMUtil.getBuildEPM(part);
          if (qrepm.hasMoreElements()) {
            EPMDocument epm3d = (EPMDocument)qrepm.nextElement();
            EPMDocument epm2d = PrintPartBOMUtil.getDrawing(epm3d, 
              null);
            System.out.println("zyj---test---epm2d:" + epm2d);
            downLoad(epm2d, docdownloadfolder);
          }
        }
      }
    }
  }

  public static WTPart findPartWithView(String number, String viewName, String verString) throws WTException
  {
    QueryResult qr = null;

    int i = verString.indexOf(".");
    String version = verString.substring(0, i);
    String iteration = verString.substring(i + 1);
    QuerySpec qs = new QuerySpec(WTPart.class);

    SearchCondition sc = new SearchCondition(WTPart.class, "master>number", 
      "LIKE", number.toUpperCase());
    qs.appendSearchCondition(sc);
    qr = PersistenceHelper.manager.find(qs);

    WTPart part = null;
    while (qr.hasMoreElements()) {
      part = (WTPart)qr.nextElement();
      String viString = part.getIterationDisplayIdentifier().toString();
      System.out.println("viString===>" + viString);
      int spaceview = viString.indexOf(".");
      System.out.println("spaceview===>" + spaceview);
      System.out.println("viString.substring(0, spaceview)===>" + viString.substring(0, spaceview));
      System.out.println("verString====>" + verString);
      if (viString.substring(0, spaceview).equalsIgnoreCase(verString)) {
        return part;
      }
    }
    return null;
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
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public static WTDocument updateIbas(WTObject pbo, String dayinfenshu, String dayinyuanyin)
    throws Exception
  {
    Properties properties = new Properties();
    properties.put("PRINTFENSHU", dayinfenshu);
    properties.put("PRINTYUANYIN", dayinyuanyin);
    WTDocument document = (WTDocument)pbo;
    IBAHelper.updateOrCreateIBAValues(document, properties);
    PersistenceServerHelper.manager.update(document);
    document = (WTDocument)PersistenceHelper.manager.refresh(document);
    return document;
  }

  public static String downLoad(EPMDocument wtdocument, String rootPath) throws WTException, PropertyVetoException, IOException
  {
    System.out.println("wtdocument=========>" + wtdocument);
    String epmType = wtdocument.getAuthoringApplication().toString();
    String downloadDirectoryStr = "";
    String state = wtdocument.getLifeCycleState().getDisplay();
    ContentHolder ch = ContentHelper.service
      .getContents(wtdocument);
    Vector attachmentList = ContentHelper.getApplicationData(ch);
    System.out.println("attachmentList.size========>" + attachmentList.size());
    System.out.println("zipfile=======wtdocument.getLifeCycleState().getDisplay()=========>" + wtdocument.getLifeCycleState().getDisplay());
    if (attachmentList.size() < 1) {
      try {
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(wtdocument);
        representation = (Representation)ContentHelper.service
          .getContents(representation);
        attachmentList = ContentHelper.getContentList(representation);
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    System.out.println("attachmentList.size========>" + attachmentList.size());
    for (int i = 0; i < attachmentList.size(); i++) {
      ApplicationData appDataPDF = 
        (ApplicationData)attachmentList
        .get(i);

      String contentFileName = appDataPDF.getFileName();
      if (!contentFileName.toLowerCase().endsWith(".pdf")) {
        System.out.println("contentFileName==1111==>" + contentFileName);
      } else {
        System.out.println("contentFileName==2222==>" + contentFileName);
        String tempPdfPath = PrintUtil2.getTempFilePath("temppdf");
        String absoluteFileName = tempPdfPath + contentFileName;
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          absoluteFileName);

        String tufu = tufu(absoluteFileName);
        System.out.println("tufu====>" + tufu);
        downloadDirectoryStr = rootPath + File.separator + tufu;
        System.out.println("zyj---test---downloadDirectoryStr:" + downloadDirectoryStr);

        File file = new File(downloadDirectoryStr + File.separator);
        if (!file.exists()) {
          file.mkdirs();
        }
        contentFileName = contentFileName.replaceAll("_drw", "");
        contentFileName = contentFileName.replaceAll("_DRW", "");
        downloadDirectoryStr = downloadDirectoryStr + File.separator + contentFileName;
        System.out.println("downloadDirectoryStr=====>" + downloadDirectoryStr);
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          downloadDirectoryStr);
        if ((!"".equals(flag)) && (!"SX".equals(flag)))
        {
          AddFlag.writeTextEPM(epmType, downloadDirectoryStr, flag);
        }
      }
    }

    return null;
  }

  public static String downLoadByState(EPMDocument wtdocument, String rootPath, String state) throws WTException, PropertyVetoException, IOException
  {
    String epmType = wtdocument.getAuthoringApplication().toString();
    System.out.println("wtdocument=========>" + wtdocument);
    String downloadDirectoryStr = "";
    ContentHolder ch = ContentHelper.service
      .getContents(wtdocument);
    Vector attachmentList = ContentHelper.getApplicationData(ch);
    System.out.println("attachmentList.size========>" + attachmentList.size());
    System.out.println("zipfile=======wtdocument.getLifeCycleState().getDisplay()=========>" + wtdocument.getLifeCycleState().getDisplay());
    if (attachmentList.size() < 1) {
      try {
        Representation representation = RepresentationHelper.service
          .getDefaultRepresentation(wtdocument);
        representation = (Representation)ContentHelper.service
          .getContents(representation);
        attachmentList = ContentHelper.getContentList(representation);
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }

    System.out.println("attachmentList.size========>" + attachmentList.size());
    for (int i = 0; i < attachmentList.size(); i++) {
      ApplicationData appDataPDF = 
        (ApplicationData)attachmentList
        .get(i);

      String contentFileName = appDataPDF.getFileName();
      if (!contentFileName.toLowerCase().endsWith(".pdf")) {
        System.out.println("contentFileName==1111==>" + contentFileName);
      } else {
        System.out.println("contentFileName==2222==>" + contentFileName);
        String tempPdfPath = PrintUtil2.getTempFilePath("temppdf");
        String absoluteFileName = tempPdfPath + contentFileName;
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          absoluteFileName);

        String tufu = tufu(absoluteFileName);
        System.out.println("tufu====>" + tufu);
        downloadDirectoryStr = rootPath + File.separator + tufu;

        File file = new File(downloadDirectoryStr + File.separator);
        if (!file.exists()) {
          file.mkdirs();
        }
        contentFileName = contentFileName.replaceAll("_drw", "");
        contentFileName = contentFileName.replaceAll("_DRW", "");
        downloadDirectoryStr = downloadDirectoryStr + File.separator + contentFileName;
        System.out.println("downloadDirectoryStr=====>" + downloadDirectoryStr);
        ContentServerHelper.service.writeContentStream(appDataPDF, 
          downloadDirectoryStr);
        if ((!"".equals(flag)) && (!"SX".equals(flag)))
        {
          AddFlag.writeTextEPM(epmType, downloadDirectoryStr, flag);
        }
      }
    }

    return null;
  }

  public static String trimExtension(String filename)
  {
    if ((filename != null) && (filename.length() > 0)) {
      int i = filename.lastIndexOf(".");
      if ((i > -1) && (i < filename.length())) {
        return filename.substring(0, i);
      }
    }
    return filename;
  }

  public static String checkXLS(WTObject pbo)
    throws WTException, PropertyVetoException, IOException
  {
    String downpath = PrintUtil2.getTempFilePath(null);
    WTDocument doc = (WTDocument)pbo;
    System.out.println("111111111111");
    String inputPath = downpath + doc.getNumber() + "/";
    System.out.println("222222222222");
    File dir = new File(inputPath);
    if ((dir.isDirectory()) && (dir.exists())) {
      dir.delete();
    }
    System.out.println("333333333333");
    dir = new File(inputPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    System.out.println("44444444444444");

    System.out.println("55555555555");
    ReferenceFactory referenceFactory = new ReferenceFactory();
    System.out.println("666666666666");
    String docdownloadfolder = printdownfolder + doc.getNumber();
    System.out.println("77777777777");
    ContentItem item = ContentHelper.service.getPrimary(doc);
    System.out.println("888888888888888");
    String contentFileName = "";
    System.out.println("99999999999999");
    if ((item instanceof ApplicationData)) {
      System.out.println("000000000000000");
      ApplicationData appData = (ApplicationData)item;
      contentFileName = appData.getFileName();
      contentFileName = downpath + contentFileName;
      ContentServerHelper.service.writeContentStream(appData, 
        contentFileName);
      if (contentFileName.endsWith(".xls")) {
        System.out.println("aaaaaaaaa");
        String actcontentFileName = docdownloadfolder + File.separator;
        File file = new File(actcontentFileName);
        if (!file.exists()) {
          file.mkdirs();
        }
        actcontentFileName = actcontentFileName + appData.getFileName();
        ContentServerHelper.service.writeContentStream(appData, 
          actcontentFileName);
      }
    }

    if (!contentFileName.endsWith(".xls")) {
      System.out.println("bbbbbbbbbbbbb");
      return "打印申请单内容不是xls文件.";
    }
    int r = 0;
    int c = 0;
    System.out.println("cccccccccc");
    String filename = contentFileName;
    POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
    HSSFWorkbook wk = new HSSFWorkbook(fs);
    HSSFSheet sheet = wk.getSheetAt(0);

    System.out.println("ddddddddddd");
    int rows = sheet.getPhysicalNumberOfRows();

    for (r = 2; r < rows; r++) {
      System.out.println("eeeeeeeeeee");
      HSSFRow row = sheet.getRow(r);
      if (row != null)
      {
        HSSFCell cell = row.getCell(20);
        String docoid = null;
        Object object = null;

        if (cell != null) {
          try {
            docoid = cell.getStringCellValue();
            System.out.println("zyj--test--oid--:" + docoid);
            object = referenceFactory.getReference(docoid).getObject();
          }
          catch (Exception e) {
            e.printStackTrace();
            object = null;
          }
        }
        if (object != null)
        {
          System.out.println("ggggggggggg");

          if (object == null);
        }

      }

    }

    return null;
  }

  public static Object getWtDocument(String number, String ver, Class class1) {
    return Select.from(class1).where(0, "master>number", "LIKE", number, false)
      .where(0, "versionInfo.identifier.versionSortId", "LIKE", "%" + ver + "%").first();
  }

  public static Vector get2DEPMDocumentByPart(WTPart part)
  {
    Vector epm2Ds = new Vector();
    try {
      WTCollection col = new WTArrayList();
      col.add(part);
      WTKeyedMap rm = PartDocHelper.service
        .getAssociatedCADDocuments(col);
      WTCollection docs = (WTCollection)rm.get(part);
      if (docs == null) {
        System.out.println("get part:" + part.getNumber() + 
          " 2DDocuments is Empty!");
      } else {
        Iterator itr = docs.iterator();
        while (itr.hasNext()) {
          ObjectReference oRef = (ObjectReference)itr.next();
          Object obj = oRef.getObject();
          System.out.println("get epm obj : " + obj);
          if ((obj instanceof EPMDocument)) {
            EPMDocument epm = (EPMDocument)obj;
            System.out.println("epm number : " + epm.getNumber() + 
              " CADName : " + epm.getCADName() + 
              " doc sub type : " + epm.getDocSubType() + 
              " doc type : " + epm.getDocType());
            if ((epm.getNumber().toUpperCase().endsWith(".DRW")) || (epm.getNumber().toUpperCase().endsWith(".DWG"))) {
              System.out.println("zyj--test--has 2D");
              epm2Ds.add(epm);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return epm2Ds;
  }
}