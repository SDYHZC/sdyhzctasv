package ext.tasv.document.fileprint;

import ext.tasv.document.processor.OutsourceDetailReport;
import ext.tasv.document.processor.PartDetailReport;
import ext.tasv.document.processor.SignBean;
import ext.tasv.document.processor.SignInfo;
import ext.tasv.document.processor.StandardDetailReport;
import ext.tasv.document.processor.TitleBean;
import ext.tasv.document.processor.TitleInfo;
import ext.tasv.document.processor.WholeDetailReport;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeService2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pom.PersistenceException;
import wt.series.MultilevelSeries;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;

public class SignExcel
  implements RemoteAccess, Serializable
{
  public static void main(String[] args)
    throws PropertyVetoException, IOException, WTException
  {
    RemoteMethodServer rmi = RemoteMethodServer.getDefault();
    rmi.setUserName("wcadmin");
    rmi.setPassword("pdm");

    String ecnoid = "VR%3Awt.change2.WTChangeOrder2%3A224193".replaceAll("%3A", ":");
    WTChangeOrder2 ecn = (WTChangeOrder2)getObjectByOid(ecnoid);

    boolean wholeDetail = false;
    boolean partDetail = true;
    boolean standardDetail = false;
    try
    {
      rmi.invoke("signPartDedail", SignExcel.class.getName(), null, 
        new Class[] { WTChangeOrder2.class }, 
        new Object[] { ecn });
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

  public static void excuteCreateExcel(WTObject obj, ObjectReference self, boolean wholeDetail, boolean partDetail, boolean standardDetail, boolean outsourceDetail)
    throws WTException, IOException, PropertyVetoException
  {
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      QueryResult qreca = ChangeHelper2.service.getChangeActivities(ecn);
      while (qreca.hasMoreElements()) {
        Object objeca = qreca.nextElement();
        if ((objeca instanceof WTChangeActivity2)) {
          WTChangeActivity2 eca = (WTChangeActivity2)objeca;
          QueryResult qrpart = ChangeHelper2.service
            .getChangeablesAfter(eca);
          while (qrpart.hasMoreElements()) {
            Object objpart = qrpart.nextElement();
            if ((objpart instanceof WTPart)) {
              WTPart part = (WTPart)objpart;
              part = (WTPart)getLatestRevision((Master)part
                .getMaster());
              chooseReport(ecn, part, wholeDetail, partDetail, 
                standardDetail, outsourceDetail);
            }
          }
        }
      }
    }
  }

  public static RevisionControlled getLatestRevision(Master master)
  {
    RevisionControlled rc = null;
    if (master != null) {
      try {
        QueryResult qr = VersionControlHelper.service
          .allVersionsOf(master);
        while (qr.hasMoreElements()) {
          RevisionControlled obj = (RevisionControlled)qr
            .nextElement();
          if ((rc != null) && 
            (!obj.getVersionIdentifier()
            .getSeries()
            .greaterThan(
            rc.getVersionIdentifier()
            .getSeries()))) continue;
          rc = obj;
        }

        if (rc != null)
          rc = (RevisionControlled)
            VersionControlHelper.getLatestIteration(rc, false);
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

  public static void chooseReport(WTChangeOrder2 ecn, WTPart part, boolean wholeDetail, boolean partDetail, boolean standardDetail, boolean outsourceDetail)
    throws WTException, IOException, PropertyVetoException
  {
    if (part.isEndItem()) {
      if (wholeDetail) {
        createWholeTruckReport(ecn, part);
      }
      if (partDetail)
      {
        createPartDetailReport(ecn, part);
      }
      if (standardDetail) {
        createStandardDetailReport(ecn, part);
      }
      if (outsourceDetail) {
        createOutsourceDetailReport(ecn, part);
      }
    }
    else
    {
      if (partDetail) {
        createPartDetailReport(ecn, part);
      }
      if (standardDetail) {
        createStandardDetailReport(ecn, part);
      }
      if (outsourceDetail)
        createOutsourceDetailReport(ecn, part);
    }
  }

  private static void createOutsourceDetailReport(WTChangeOrder2 ecn, WTPart part)
    throws WTException, IOException, PropertyVetoException
  {
    TitleBean tb = TitleInfo.getTitleInfo(part);
    List infoList = OutsourceDetailReport.getOutsourceDetailList(part, 1.0D);
    System.out.println("createOutsourceDetailReport_______________" + infoList.size());
    createMultiOutsourceDetailExcel(ecn, infoList, tb);
  }

  private static void createMultiOutsourceDetailExcel(WTChangeOrder2 ecn, List<String[]> list, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    int group = list.size() / 30;
    if (list.size() % 30 != 0) {
      group++;
    }

    for (int i = 0; i < group; i++) {
      List subList = new ArrayList();
      if (i == group - 1) {
        subList = list.subList(i * 30, list.size());
        createOutsourceDetailExcel(ecn, subList, i, group, tb);
      } else {
        subList = list.subList(i * 30, 30 * (i + 1));
        createOutsourceDetailExcel(ecn, subList, i, group, tb);
      }
    }
  }

  private static void createOutsourceDetailExcel(WTChangeOrder2 ecn, List<String[]> subList, int page, int pageSum, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    XSSFSheet sheet = null;
    String tempPath = wtHome + File.separator + "codebase" + File.separator + 
      "ext" + File.separator + "tasv" + File.separator + "document" + 
      File.separator + "conf" + File.separator;
    String inPath = tempPath + "WGJMXB.xlsx";
    String outPath = wtHome + File.separator + "temp" + File.separator + 
      "WGJMXB" + "_" + String.valueOf(page + 1) + ".xlsx";
    String sheetName = "外购件明细表_" + String.valueOf(page + 1);
    FileInputStream fis = new FileInputStream(inPath);
    XSSFWorkbook wb = new XSSFWorkbook(fis);
    sheet = wb.getSheetAt(0);
    wb.setSheetName(0, sheetName);

    setOutsourceTile(sheet, wb, page + 1, pageSum, tb);

    for (int i = 0; i < subList.size(); i++) {
      XSSFRow row1 = sheet.getRow(i + 11);
      String[] str = (String[])subList.get(i);
      for (int j = 0; j < str.length; j++) {
        XSSFCell c0 = row1.getCell(j);
        XSSFCellStyle cellStyle = c0.getCellStyle();
        XSSFFont font = cellStyle.getFont();
        font.setFontHeightInPoints(11);
        cellStyle.setFont(font);
        c0.setCellStyle(cellStyle);
        c0.setCellType(1);
        c0.setCellValue(str[j]);
      }
    }

    OutputStream out = new FileOutputStream(outPath);
    wb.write(out);
    out.flush();
    out.close();
    File outFile = new File(outPath);
    uploadFile(ecn, outPath);
    if (outFile.exists())
      outFile.delete();
  }

  public static void setOutsourceTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, TitleBean tb)
  {
    setCellTableNameAndCellStyle(0, 0, sheet, wb, tb.getOutsource());

    setStateValueAndCellStyle(0, 8, sheet, wb, tb.getState());

    setTopMapNoAndCellStyle(2, 8, sheet, wb, tb.getCode());

    setDateAndCellStyle(1, 10, sheet, wb, tb.getDate());

    setCellValueAndCellStyle(5, 11, sheet, wb, "共 " + pageSum + " 页");

    setCellValueAndCellStyle(6, 11, sheet, wb, "第 " + page + " 页");
  }

  private static void createWholeTruckReport(WTChangeOrder2 ecn, WTPart part)
    throws IOException, WTException, PropertyVetoException
  {
    TitleBean tb = TitleInfo.getTitleInfo(part);
    List infoList = WholeDetailReport.getCPChildPart(part);
    System.out.println(infoList.size());
    createMultiWholeTruckExcel(ecn, infoList, tb);
  }

  public static void createMultiWholeTruckExcel(WTChangeOrder2 ecn, List<String[]> list, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    int group = list.size() / 30;
    if (list.size() % 30 != 0) {
      group++;
    }

    for (int i = 0; i < group; i++) {
      List subList = new ArrayList();
      if (i == group - 1) {
        subList = list.subList(i * 30, list.size());
        createWholeTruckExcel(ecn, subList, i, group, tb);
      } else {
        subList = list.subList(i * 30, 30 * (i + 1));
        createWholeTruckExcel(ecn, subList, i, group, tb);
      }
    }
  }

  private static void createWholeTruckExcel(WTChangeOrder2 ecn, List<String[]> subList, int page, int pageSum, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    XSSFSheet sheet = null;
    String tempPath = wtHome + File.separator + "codebase" + File.separator + 
      "ext" + File.separator + "tasv" + File.separator + "document" + 
      File.separator + "conf" + File.separator;
    String inPath = tempPath + "ZCMXB.xlsx";
    String outPath = wtHome + File.separator + "temp" + File.separator + 
      "ZCMXB" + "_" + String.valueOf(page + 1) + ".xlsx";
    String sheetName = "整车明细表_" + String.valueOf(page + 1);
    FileInputStream fis = new FileInputStream(inPath);
    XSSFWorkbook wb = new XSSFWorkbook(fis);
    sheet = wb.getSheetAt(0);
    wb.setSheetName(0, sheetName);

    setWholeTruckTile(sheet, wb, page + 1, pageSum, tb);

    for (int i = 0; i < subList.size(); i++) {
      XSSFRow row1 = sheet.getRow(i + 10);
      String[] str = (String[])subList.get(i);
      for (int j = 0; j < 11; j++) {
        if (j == 0) {
          XSSFCell c0 = row1.getCell(j);
          XSSFCellStyle cellStyle = c0.getCellStyle();
          XSSFFont font = cellStyle.getFont();
          font.setFontHeightInPoints(11);
          cellStyle.setFont(font);
          c0.setCellStyle(cellStyle);
          c0.setCellType(1);
          c0.setCellValue(str[0]);
          System.out.println("str[0]____________" + str[0]);
        } else if (j == 2) {
          XSSFCell c0 = row1.getCell(j);
          XSSFCellStyle cellStyle = c0.getCellStyle();
          XSSFFont font = cellStyle.getFont();
          font.setFontHeightInPoints(11);
          cellStyle.setFont(font);
          c0.setCellStyle(cellStyle);
          c0.setCellType(1);
          c0.setCellValue(str[1]);
          System.out.println("str[1]____________" + str[1]);
        } else if (j == 4) {
          XSSFCell c0 = row1.getCell(j);
          XSSFCellStyle cellStyle = c0.getCellStyle();
          XSSFFont font = cellStyle.getFont();
          font.setFontHeightInPoints(11);
          cellStyle.setFont(font);
          c0.setCellStyle(cellStyle);
          c0.setCellType(1);
          c0.setCellValue(str[2]);
          System.out.println("str[2]____________" + str[3]);
        } else if (j == 8) {
          XSSFCell c0 = row1.getCell(j);
          XSSFCellStyle cellStyle = c0.getCellStyle();
          XSSFFont font = cellStyle.getFont();
          font.setFontHeightInPoints(11);
          cellStyle.setFont(font);
          c0.setCellStyle(cellStyle);
          c0.setCellType(1);
          c0.setCellValue(str[3]);
          System.out.println("str[3]____________" + str[3]);
        } else if (j == 10) {
          XSSFCell c0 = row1.getCell(j);
          XSSFCellStyle cellStyle = c0.getCellStyle();
          XSSFFont font = cellStyle.getFont();
          font.setFontHeightInPoints(11);
          cellStyle.setFont(font);
          c0.setCellStyle(cellStyle);
          c0.setCellType(1);
          c0.setCellValue(str[4]);
          System.out.println("str[4]____________" + str[4]);
        }
      }
    }

    OutputStream out = new FileOutputStream(outPath);
    wb.write(out);
    out.flush();
    out.close();
    File outFile = new File(outPath);
    uploadFile(ecn, outPath);
    if (outFile.exists())
      outFile.delete();
  }

  public static void setWholeTruckTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, TitleBean tb)
  {
    setCellTableNameAndCellStyle(0, 0, sheet, wb, tb.getWhole());

    setStateValueAndCellStyle(0, 8, sheet, wb, tb.getState());

    setTopMapNoAndCellStyle(2, 8, sheet, wb, tb.getCode());

    setDateAndCellStyle(1, 10, sheet, wb, tb.getDate());

    setCellValueAndCellStyle(5, 11, sheet, wb, "共 " + pageSum + " 页");

    setCellValueAndCellStyle(6, 11, sheet, wb, "第 " + page + " 页");
  }

  private static void createStandardDetailReport(WTChangeOrder2 ecn, WTPart part)
    throws WTException, IOException, PropertyVetoException
  {
    TitleBean tb = TitleInfo.getTitleInfo(part);

    List infoList = StandardDetailReport.sortList(part);
    System.out.println("*********createStandardDetailReport**********" + infoList.size());
    createMultiStandardDetailExcel(ecn, infoList, tb);
  }

  public static void createMultiStandardDetailExcel(WTChangeOrder2 ecn, List<String[]> list, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    int group = list.size() / 30;
    if (list.size() % 30 != 0) {
      group++;
    }

    for (int i = 0; i < group; i++) {
      List subList = new ArrayList();
      if (i == group - 1) {
        subList = list.subList(i * 30, list.size());
        createStandardDetailExcel(ecn, subList, i, group, tb);
      } else {
        subList = list.subList(i * 30, 30 * (i + 1));
        createStandardDetailExcel(ecn, subList, i, group, tb);
      }
    }
  }

  private static void createStandardDetailExcel(WTChangeOrder2 ecn, List<String[]> subList, int page, int pageSum, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    XSSFSheet sheet = null;
    String tempPath = wtHome + File.separator + "codebase" + File.separator + 
      "ext" + File.separator + "tasv" + File.separator + "document" + 
      File.separator + "conf" + File.separator;
    String inPath = tempPath + "BZJMXB.xlsx";
    String outPath = wtHome + File.separator + "temp" + File.separator + 
      "createReport" + File.separator + "BZJMXB" + "_" + 
      String.valueOf(page + 1) + ".xlsx";
    String sheetName = "标准件明细表_" + String.valueOf(page + 1);
    FileInputStream fis = new FileInputStream(inPath);
    XSSFWorkbook wb = new XSSFWorkbook(fis);
    sheet = wb.getSheetAt(0);
    wb.setSheetName(0, sheetName);

    setStandardDetailTile(sheet, wb, page + 1, pageSum, tb);

    for (int i = 0; i < subList.size(); i++) {
      XSSFRow row1 = sheet.getRow(i + 11);
      String[] str = (String[])subList.get(i);
      for (int j = 0; j < 12; j++) {
        System.out.println("0000000000000");
        XSSFCell c0 = row1.getCell(j);
        System.out.println("1111111111111");
        XSSFCellStyle cellStyle = c0.getCellStyle();
        System.out.println("333333333333333333333");
        XSSFFont font = cellStyle.getFont();
        font.setFontHeightInPoints(11);
        cellStyle.setFont(font);
        c0.setCellStyle(cellStyle);
        c0.setCellType(1);
        c0.setCellValue(str[j]);
      }
    }
    OutputStream out = new FileOutputStream(outPath);
    wb.write(out);
    out.flush();
    out.close();
    File outFile = new File(outPath);
    uploadFile(ecn, outPath);
    if (outFile.exists())
      outFile.delete();
  }

  public static void setStandardDetailTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, TitleBean tb)
  {
    setCellTableNameAndCellStyle(0, 0, sheet, wb, tb.getStandard());

    setStateValueAndCellStyle(0, 8, sheet, wb, tb.getState());

    setTopMapNoAndCellStyle(2, 8, sheet, wb, tb.getCode());

    setDateAndCellStyle(1, 10, sheet, wb, tb.getDate());

    setCellValueAndCellStyle(5, 11, sheet, wb, "共 " + pageSum + " 页");

    setCellValueAndCellStyle(6, 11, sheet, wb, "第 " + page + " 页");
  }

  private static void createPartDetailReport(WTChangeOrder2 ecn, WTPart part)
    throws WTException, IOException, PropertyVetoException
  {
    TitleBean tb = TitleInfo.getTitleInfo(part);
    List infoList = PartDetailReport.getPartInfo(part);
    createMultiPartDetailExcel(ecn, infoList, tb);
  }

  public static void createMultiPartDetailExcel(WTChangeOrder2 ecn, List<String[]> list, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    int group = list.size() / 30;
    if (list.size() % 30 != 0) {
      group++;
    }

    for (int i = 0; i < group; i++) {
      List subList = new ArrayList();
      if (i == group - 1) {
        subList = list.subList(i * 30, list.size());
        createPartDetailExcel(ecn, subList, i, group, tb);
      } else {
        subList = list.subList(i * 30, 30 * (i + 1));
        createPartDetailExcel(ecn, subList, i, group, tb);
      }
    }
  }

  public static void createPartDetailExcel(WTChangeOrder2 ecn, List<String[]> emportlist, int page, int pageSum, TitleBean tb)
    throws IOException, WTException, PropertyVetoException
  {
    String wtHome = WTProperties.getLocalProperties()
      .getProperty("wt.home");
    XSSFSheet sheet = null;
    String tempPath = wtHome + File.separator + "codebase" + File.separator + 
      "ext" + File.separator + "tasv" + File.separator + "document" + 
      File.separator + "conf" + File.separator;
    String inPath = tempPath + "FZMXB.xlsx";
    String outPath = wtHome + File.separator + "temp" + File.separator + 
      "createReport" + File.separator + "FZMXB" + "_" + 
      String.valueOf(page + 1) + ".xlsx";
    String sheetName = "分组明细表_" + String.valueOf(page + 1);
    FileInputStream fis = new FileInputStream(inPath);
    XSSFWorkbook wb = new XSSFWorkbook(fis);
    sheet = wb.getSheetAt(0);
    wb.setSheetName(0, sheetName);

    setPartDetailTile(sheet, wb, page + 1, pageSum, tb);

    for (int i = 0; i < emportlist.size(); i++) {
      XSSFRow row1 = sheet.getRow(i + 11);
      String[] str = (String[])emportlist.get(i);
      for (int j = 0; j < str.length; j++) {
        XSSFCell c0 = row1.getCell(j);
        XSSFCellStyle cellStyle = c0.getCellStyle();
        XSSFFont font = cellStyle.getFont();
        font.setFontHeightInPoints(11);
        cellStyle.setFont(font);
        c0.setCellStyle(cellStyle);
        c0.setCellType(1);
        c0.setCellValue(str[j]);
      }
    }
    OutputStream out = new FileOutputStream(outPath);
    wb.write(out);
    out.flush();
    out.close();
    File outFile = new File(outPath);
    uploadFile(ecn, outPath);
  }

  public static void setPartDetailTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, TitleBean tb)
  {
    setCellTableNameAndCellStyle(0, 0, sheet, wb, tb.getName());

    setStateValueAndCellStyle(0, 8, sheet, wb, tb.getState());

    setTopMapNoAndCellStyle(2, 8, sheet, wb, tb.getCode());

    setDateAndCellStyle(1, 10, sheet, wb, tb.getDate());

    setCellValueAndCellStyle(5, 11, sheet, wb, "共 " + pageSum + " 页");

    setCellValueAndCellStyle(6, 11, sheet, wb, "第 " + page + " 页");
  }

  public static void uploadExcel(ContentHolder doc, String filePath)
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
        String contentName = appData.getFileName();
        System.out.println("文档主内容名---contentName--->>>" + contentName);
        if ((filePath == null) || ("".equals(filePath)))
          continue;
        String fileFullName = filePath.substring(filePath
          .lastIndexOf(File.separator) + 1);
        String fileName = fileFullName.substring(0, 
          fileFullName.lastIndexOf("."));
        System.out
          .println("可视化PDF文件全名---fileName--->>>" + fileName);
        if ((!fileFullName.equalsIgnoreCase(contentName)) || 
          (filePath.indexOf(".xlsx") == -1)) continue;
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

  public static void downloadFile(InputStream in, String path)
    throws FileNotFoundException
  {
    System.out.println("***********************8");
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

  public static ContentHolder removeAttachment(ContentHolder doc)
    throws WTException, PropertyVetoException
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
        ContentServerHelper.service.deleteContent(ch2, appData);
      }
    } catch (WTPropertyVetoException wtpve) {
      wtpve.printStackTrace();
    }

    return doc;
  }

  public static void uploadFile(ContentHolder doc, String filePath) throws WTException, PropertyVetoException, IOException
  {
    boolean flagAccess = SessionServerHelper.manager
      .setAccessEnforced(false);
    System.out.println("--------filePath---***********----" + filePath);
    try {
      doc = removeAttachment(doc, filePath);
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      ApplicationData appData2 = ApplicationData.newApplicationData(ch2);
      appData2.setRole(ContentRoleType.SECONDARY);
      ContentServerHelper.service.updateContent(ch2, appData2, filePath);
      System.out.println("上传成功**********************");
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      SessionServerHelper.manager.setAccessEnforced(flagAccess);
    }
  }

  public static void setExcelTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, SignBean sb, TitleBean tb)
  {
    setCellTableNameAndCellStyle(0, 0, sheet, wb, tb.getName());

    setStateValueAndCellStyle(0, 8, sheet, wb, tb.getState());

    setTopMapNoAndCellStyle(2, 10, sheet, wb, tb.getCode());

    setDateAndCellStyle(1, 10, sheet, wb, tb.getDate());

    setCellValueAndCellStyle(5, 11, sheet, wb, "共 " + pageSum + " 页");

    setCellValueAndCellStyle(6, 11, sheet, wb, "第 " + page + " 页");
  }

  public static void setSignTile(XSSFSheet sheet, XSSFWorkbook wb, int page, int pageSum, SignBean sb, TitleBean tb)
  {
    setTitleCellValue(6, 2, sheet, wb, sb.getBianzhi());

    setTitleCellValue(7, 2, sheet, wb, sb.getJiaodui());

    setTitleCellValue(8, 2, sheet, wb, sb.getShenhe());

    setTitleCellValue(6, 4, sheet, wb, sb.getBiaozhunhua());

    setTitleCellValue(7, 4, sheet, wb, sb.getPizhun());

    setDateValueAndCellStyle(8, 4, sheet, wb, sb.getDate());
  }

  public static void setTitleCellValue(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }
    System.out.println("设置签名信息value:________________" + value);
    cell.setCellValue(value);
  }

  public static void setCellValueAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }

    XSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(1);
    cellStyle.setBorderLeft(1);
    cellStyle.setBorderTop(1);
    cellStyle.setBorderRight(1);

    cellStyle.setAlignment(2);

    cellStyle.setWrapText(true);

    XSSFFont font = wb.getFontAt((short)a);
    font.setBold(true);
    font.setFontHeightInPoints(11);

    cellStyle.setFont(font);

    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void setCellTableNameAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }
    XSSFCellStyle cellStyle = wb.createCellStyle();

    XSSFFont font = wb.createFont();
    font.setFontHeightInPoints(16);
    font.setBoldweight(700);

    cellStyle.setFont(font);
    cellStyle.setAlignment(2);
    cellStyle.setWrapText(true);
    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void setTopMapNoAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }

    XSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(1);
    cellStyle.setBorderLeft(1);
    cellStyle.setBorderTop(1);
    cellStyle.setBorderRight(1);

    cellStyle.setAlignment(2);

    cellStyle.setWrapText(true);

    XSSFFont font = wb.getFontAt((short)a);
    font.setBold(true);
    font.setFontHeightInPoints(20);

    cellStyle.setFont(font);
    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void setDateAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }

    XSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(1);
    cellStyle.setBorderLeft(1);
    cellStyle.setBorderTop(1);
    cellStyle.setBorderRight(1);

    cellStyle.setAlignment(2);

    cellStyle.setWrapText(true);

    XSSFFont font = wb.getFontAt((short)a);
    font.setBold(true);
    font.setFontHeightInPoints(12);

    cellStyle.setFont(font);
    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void setStateValueAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }

    XSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(1);
    cellStyle.setBorderLeft(1);
    cellStyle.setBorderTop(1);
    cellStyle.setBorderRight(1);

    cellStyle.setAlignment(1);

    cellStyle.setWrapText(true);

    XSSFFont font = wb.getFontAt((short)a);
    font.setBold(true);
    font.setFontHeightInPoints(9);

    cellStyle.setFont(font);

    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void setDateValueAndCellStyle(int a, int b, XSSFSheet sheet, XSSFWorkbook wb, String value)
  {
    XSSFRow row = sheet.getRow(a);
    if (row == null) {
      row = sheet.createRow(a);
    }
    XSSFCell cell = row.getCell((short)b);
    if (cell == null) {
      cell = row.createCell((short)b);
      cell.setCellType(1);
    }

    XSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(1);
    cellStyle.setBorderLeft(1);
    cellStyle.setBorderTop(1);
    cellStyle.setBorderRight(1);

    cellStyle.setAlignment(2);

    cellStyle.setWrapText(true);

    XSSFFont font = wb.getFontAt((short)a);
    font.setBold(true);
    font.setFontHeightInPoints(9);

    cellStyle.setFont(font);

    cell.setCellStyle(cellStyle);
    cell.setCellValue(value);
  }

  public static void excuteSignExcel(WTObject obj, ObjectReference self, boolean wholeDetail, boolean partDetail, boolean standardDetail, boolean outsourceDetail)
    throws Exception
  {
    if ((obj instanceof WTChangeOrder2)) {
      WTChangeOrder2 ecn = (WTChangeOrder2)obj;
      signReport(ecn, self, wholeDetail, partDetail, standardDetail, outsourceDetail);
    }
  }

  public static void signReport(WTChangeOrder2 ecn, ObjectReference self, boolean wholeDetail, boolean partDetail, boolean standardDetail, boolean outsourceDetail)
    throws Exception
  {
    SignBean sb = SignInfo.getSignInfo(self);
    System.out.println("1111" + sb.getBianzhi());
    System.out.println("1111" + sb.getBiaozhunhua());
    System.out.println("1111" + sb.getDate());
    System.out.println("1111" + sb.getJiaodui());
    System.out.println("1111" + sb.getPizhun());
    System.out.println("1111" + sb.getShenhe());

    System.out.println("1111" + sb.getShenhe());

    System.out.println("wholeDetail******************" + wholeDetail);
    System.out.println("partDetail******************" + partDetail);
    System.out.println("standardDetail******************" + standardDetail);
    System.out.println("outsourceDetail******************" + outsourceDetail);

    if (wholeDetail) {
      signWholeDedail(ecn, sb);
      System.out.println("        wholeDetail ************    " + 
        wholeDetail);
    }
    if (partDetail) {
      signPartDedail(ecn, sb);
      System.out.println("        partDetail     " + partDetail);
    }
    if (standardDetail) {
      signStandardDedail(ecn, sb);
      System.out.println("        standardDetail     " + standardDetail);
    }
    if (outsourceDetail) {
      signOutsourceDetail(ecn, sb);
      System.out.println("        standardDetail     " + standardDetail);
    }
  }

  private static void signOutsourceDetail(WTChangeOrder2 ecn, SignBean sb)
    throws IOException, WTException, PropertyVetoException
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    String temp = wtHome + File.separator + "temp" + File.separator + "reportCache" + File.separator;

    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { WTChangeOrder2.class };
      Object[] argValues = { ecn };
      try
      {
        RemoteMethodServer.getDefault().invoke("signOutsourceDetail", 
          SignExcel.class.getName(), null, argTypes, 
          argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    else {
      QueryResult qrs = ContentHelper.service.getContentsByRole(ecn, 
        ContentRoleType.SECONDARY);
      while (qrs.hasMoreElements()) {
        ApplicationData applicationdata = (ApplicationData)qrs.nextElement();
        InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
        String filename = applicationdata.getFileName();
        System.out.println("filename**********signPartDedail******" + filename);
        System.out.println("+signPartDedail+++filename++++" + filename);
        if ((filename.endsWith(".xlsx")) && (filename.indexOf("WGJMXB") != -1)) {
          String filepath = temp + filename;
          System.out.println("++++signPartDedail**************++++" + filepath);
          downloadFile(in, filepath);

          XSSFSheet sheet = null;

          String outPath = temp + filepath.substring(filepath.lastIndexOf(File.separator) + 1);

          String fullName = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
          String fileName = fullName.substring(0, fullName.lastIndexOf("."));

          System.out.println("进入签名*********signWholeDetailExcel*********");

          FileInputStream fis = new FileInputStream(filepath);
          XSSFWorkbook wb = new XSSFWorkbook(fis);
          sheet = wb.getSheetAt(0);
          wb.setSheetName(0, fileName);

          setTitleCellValue(6, 2, sheet, wb, sb.getBianzhi());

          setTitleCellValue(7, 2, sheet, wb, sb.getJiaodui());

          setTitleCellValue(8, 2, sheet, wb, sb.getShenhe());

          setTitleCellValue(6, 4, sheet, wb, sb.getBiaozhunhua());

          setTitleCellValue(7, 4, sheet, wb, sb.getPizhun());

          setDateValueAndCellStyle(8, 4, sheet, wb, sb.getDate());

          OutputStream out = new FileOutputStream(outPath);
          wb.write(out);
          out.flush();
          out.close();
          File outFile = new File(outPath);

          uploadExcelFile(ecn, outPath);
        }
      }
    }
  }

  public static void signWholeDedail(WTChangeOrder2 ecn, SignBean sb)
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    String temp = wtHome + File.separator + "temp" + File.separator + "reportCache" + File.separator;

    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { WTChangeOrder2.class };
      Object[] argValues = { ecn };
      try
      {
        RemoteMethodServer.getDefault().invoke("signWholeDedail", 
          SignExcel.class.getName(), null, argTypes, 
          argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    else {
      QueryResult qrs = ContentHelper.service.getContentsByRole(ecn, 
        ContentRoleType.SECONDARY);
      while (qrs.hasMoreElements()) {
        ApplicationData applicationdata = (ApplicationData)qrs.nextElement();
        InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
        String filename = applicationdata.getFileName();
        System.out.println("filename*****************" + filename);
        System.out.println("++++filename++++" + filename);
        if ((filename.endsWith(".xlsx")) && (filename.indexOf("ZCMXB") != -1)) {
          String filepath = temp + filename;
          System.out.println("++++filepath**************++++" + filepath);
          downloadFile(in, filepath);

          XSSFSheet sheet = null;

          String outPath = temp + filepath.substring(filepath.lastIndexOf(File.separator) + 1);

          String fullName = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
          String fileName = fullName.substring(0, fullName.lastIndexOf("."));

          System.out.println("进入签名*********signWholeDetailExcel*********");

          FileInputStream fis = new FileInputStream(filepath);
          XSSFWorkbook wb = new XSSFWorkbook(fis);
          sheet = wb.getSheetAt(0);
          wb.setSheetName(0, fileName);

          setTitleCellValue(6, 2, sheet, wb, sb.getBianzhi());

          setTitleCellValue(7, 2, sheet, wb, sb.getJiaodui());

          setTitleCellValue(8, 2, sheet, wb, sb.getShenhe());

          setTitleCellValue(6, 4, sheet, wb, sb.getBiaozhunhua());

          setTitleCellValue(7, 4, sheet, wb, sb.getPizhun());

          setDateValueAndCellStyle(8, 4, sheet, wb, sb.getDate());

          OutputStream out = new FileOutputStream(outPath);
          wb.write(out);
          out.flush();
          out.close();
          File outFile = new File(outPath);

          uploadExcelFile(ecn, outPath);
        }
      }
    }
  }

  public static void uploadExcelFile(ContentHolder doc, String filePath)
    throws WTException, PropertyVetoException, IOException
  {
    boolean flagAccess = SessionServerHelper.manager
      .setAccessEnforced(false);
    System.out.println("--------filePath---***********----" + filePath);
    try {
      doc = removeAttachment(doc, filePath);
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      ApplicationData appData2 = ApplicationData.newApplicationData(ch2);
      appData2.setRole(ContentRoleType.SECONDARY);
      ContentServerHelper.service.updateContent(ch2, appData2, filePath);
      System.out.println("上传成功**********************");
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      SessionServerHelper.manager.setAccessEnforced(flagAccess);
    }
  }

  public static ContentHolder removeExcelAttachment(ContentHolder doc, String filePath) throws WTException, PropertyVetoException
  {
    try
    {
      ContentHolder ch2 = ContentHelper.service
        .getContents(doc);
      Vector apps = ContentHelper.getApplicationData(ch2);
      for (Enumeration e = apps.elements(); e.hasMoreElements(); ) {
        ApplicationData appData = (ApplicationData)e.nextElement();
        String contentName = appData.getFileName();
        System.out.println("文档主内容名---contentName--->>>" + contentName);
        if ((filePath == null) || ("".equals(filePath)))
          continue;
        String fileFullName = filePath.substring(filePath
          .lastIndexOf(File.separator) + 1);
        String fileName = fileFullName.substring(0, 
          fileFullName.lastIndexOf("."));
        System.out
          .println("可视化PDF文件全名---fileName--->>>" + fileName);
        if ((!fileFullName.equalsIgnoreCase(contentName)) || 
          (filePath.indexOf(".xlsx") == -1)) continue;
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

  public static void signPartDedail(WTChangeOrder2 ecn, SignBean sb)
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    String temp = wtHome + File.separator + "temp" + File.separator + "reportCache" + File.separator;

    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { WTChangeOrder2.class };
      Object[] argValues = { ecn };
      try
      {
        RemoteMethodServer.getDefault().invoke("signPartDedail", 
          SignExcel.class.getName(), null, argTypes, 
          argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    else {
      QueryResult qrs = ContentHelper.service.getContentsByRole(ecn, 
        ContentRoleType.SECONDARY);
      while (qrs.hasMoreElements()) {
        ApplicationData applicationdata = (ApplicationData)qrs.nextElement();
        InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
        String filename = applicationdata.getFileName();
        System.out.println("filename**********signPartDedail******" + filename);
        System.out.println("+signPartDedail+++filename++++" + filename);
        if ((filename.endsWith(".xlsx")) && (filename.indexOf("FZMXB") != -1)) {
          String filepath = temp + filename;
          System.out.println("++++signPartDedail**************++++" + filepath);
          downloadFile(in, filepath);

          XSSFSheet sheet = null;

          String outPath = temp + filepath.substring(filepath.lastIndexOf(File.separator) + 1);

          String fullName = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
          String fileName = fullName.substring(0, fullName.lastIndexOf("."));

          System.out.println("进入签名*********signWholeDetailExcel*********");

          FileInputStream fis = new FileInputStream(filepath);
          XSSFWorkbook wb = new XSSFWorkbook(fis);
          sheet = wb.getSheetAt(0);
          wb.setSheetName(0, fileName);

          setTitleCellValue(6, 2, sheet, wb, sb.getBianzhi());

          setTitleCellValue(7, 2, sheet, wb, sb.getJiaodui());

          setTitleCellValue(8, 2, sheet, wb, sb.getShenhe());

          setTitleCellValue(6, 4, sheet, wb, sb.getBiaozhunhua());

          setTitleCellValue(7, 4, sheet, wb, sb.getPizhun());

          setDateValueAndCellStyle(8, 4, sheet, wb, sb.getDate());

          OutputStream out = new FileOutputStream(outPath);
          wb.write(out);
          out.flush();
          out.close();
          File outFile = new File(outPath);

          uploadExcelFile(ecn, outPath);
        }
      }
    }
  }

  public static void signStandardDedail(WTChangeOrder2 ecn, SignBean sb)
    throws Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    String temp = wtHome + File.separator + "temp" + File.separator + "reportCache" + File.separator;

    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { WTChangeOrder2.class };
      Object[] argValues = { ecn };
      try
      {
        RemoteMethodServer.getDefault().invoke("signStandardDedail", 
          SignExcel.class.getName(), null, argTypes, 
          argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    else {
      QueryResult qrs = ContentHelper.service.getContentsByRole(ecn, 
        ContentRoleType.SECONDARY);
      while (qrs.hasMoreElements()) {
        ApplicationData applicationdata = (ApplicationData)qrs.nextElement();
        InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
        String filename = applicationdata.getFileName();
        System.out.println("filename*****************" + filename);
        System.out.println("++++filename++++" + filename);
        if ((filename.endsWith(".xlsx")) && (filename.indexOf("BZJMXB") != -1)) {
          String filepath = temp + filename;
          System.out.println("++++filepath**************++++" + filepath);
          downloadFile(in, filepath);

          XSSFSheet sheet = null;

          String outPath = temp + filepath.substring(filepath.lastIndexOf(File.separator) + 1);

          String fullName = filepath.substring(filepath.lastIndexOf(File.separator) + 1);
          String fileName = fullName.substring(0, fullName.lastIndexOf("."));

          System.out.println("进入签名*********signWholeDetailExcel*********");

          FileInputStream fis = new FileInputStream(filepath);
          XSSFWorkbook wb = new XSSFWorkbook(fis);
          sheet = wb.getSheetAt(0);
          wb.setSheetName(0, fileName);

          setTitleCellValue(6, 2, sheet, wb, sb.getBianzhi());

          setTitleCellValue(7, 2, sheet, wb, sb.getJiaodui());

          setTitleCellValue(8, 2, sheet, wb, sb.getShenhe());

          setTitleCellValue(6, 4, sheet, wb, sb.getBiaozhunhua());

          setTitleCellValue(7, 4, sheet, wb, sb.getPizhun());

          setDateValueAndCellStyle(8, 4, sheet, wb, sb.getDate());

          OutputStream out = new FileOutputStream(outPath);
          wb.write(out);
          out.flush();
          out.close();
          File outFile = new File(outPath);

          uploadExcelFile(ecn, outPath);
        }
      }
    }
  }
}