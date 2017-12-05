package ext.tzc.tasv.printApplicationFrom.workflow;

import ext.tzc.tasv.constants.ObjectConstant;
import ext.tzc.tasv.printApplicationFrom.util.PrintApplicationFromHelp;
import ext.tzc.tasv.util.CSCExcelHandler;
import ext.tzc.tasv.util.ExcelHandlerFactory;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Vector;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.ContentService;
import wt.content.ContentServiceSvr;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildRule;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.series.MultilevelSeries;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTRuntimeException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;
import wt.vc.VersionIdentifier;

public class PrintApplicationFromWFHelp
{
  public static String setDownLoadNoPDFURL(WTObject obj, String downLoadURL)
    throws WTException
  {
    if ((obj instanceof WTDocument)) {
      WTDocument doc = (WTDocument)obj;
      ReferenceFactory rf = new ReferenceFactory();
      String oid = rf.getReferenceString(doc);
      downLoadURL = "<a href='/Windchill/app/netmarkets/jsp/ext/tzc/tasv/printApplicationFrom/downLoadNoPDFInfo.jsp?oid=" + oid + "' target='_blank' >" + "�������" + "</a>";
    }
    return downLoadURL;
  }

  public static String downLoadNoPDFExcel(String oid)
  {
    String filePath = "";
    try {
      ReferenceFactory rf = new ReferenceFactory();
      Persistable obj = rf.getReference(oid).getObject();
      if ((obj instanceof WTDocument)) {
        WTDocument doc = (WTDocument)obj;
        ArrayList<String> numbers = readFile(doc);
        String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
        filePath = wtHome + File.separator + "temp" + File.separator + doc.getNumber() + PrintApplicationFromHelp.getCurrentTime("yyyyMMdd") + ".xls";
        String templatePath = wtHome + File.separator + ObjectConstant.PRINT_APPLICATION_FROM_TEMPLATE;
        PrintApplicationFromHelp.copy(templatePath, filePath);
        FileOutputStream out = new FileOutputStream(filePath);
        FileInputStream in = new FileInputStream(templatePath);

        HSSFWorkbook wb = new HSSFWorkbook(in);
        HSSFSheet sheet0 = wb.getSheetAt(0);
        HSSFCellStyle leftCellStyle = PrintApplicationFromHelp.getLeftCellstyle(wb);
        HSSFCellStyle centreCellStyle = PrintApplicationFromHelp.getCentreCellstyle(wb);
        int i = 0;
        for (String number : numbers) {
          Persistable persistable = getObjectByNumber(WTPartMaster.class, number);
          if (persistable != null) {
            WTPartMaster master = (WTPartMaster)persistable;
            WTPart part = (WTPart)getLatestObject(master);
            boolean flag = isNoPDF(part).booleanValue();
            if (!flag) {
              i++;
              String sequence = String.valueOf(i);
              String name = part.getName();
              String representationable = "��";
              String version = part.getIterationDisplayIdentifier().toString();
              String state = part.getState().getState().getFullDisplay();
              String amount = "1";
              String mark = "";
              PrintApplicationFromHelp.setCellValue(sheet0, leftCellStyle, centreCellStyle, i + 1, sequence, number, name, representationable, version, state, amount, mark, null);
            }
          } else {
            persistable = getObjectByNumber(EPMDocumentMaster.class, number);
            if (persistable != null) {
              EPMDocumentMaster master = (EPMDocumentMaster)persistable;
              EPMDocument epm = (EPMDocument)getLatestObject(master);
              boolean flag = isNoPDF(epm).booleanValue();
              if (!flag) {
                i++;
                String sequence = String.valueOf(i);
                String name = epm.getName();
                String representationable = "��";
                String version = epm.getIterationDisplayIdentifier().toString();
                String state = epm.getState().getState().getFullDisplay();
                String amount = "1";
                String mark = "";
                PrintApplicationFromHelp.setCellValue(sheet0, leftCellStyle, centreCellStyle, i + 1, sequence, number, name, representationable, version, state, amount, mark, null);
              }
            }
          }
        }
        wb.write(out);
        out.close();
        in.close();
      }
    }
    catch (WTRuntimeException e) {
      e.printStackTrace();
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return filePath;
  }

  public static ArrayList<String> readFile(WTDocument doc)
  {
    boolean bool = SessionServerHelper.manager.isAccessEnforced();
    SessionServerHelper.manager.setAccessEnforced(false);
    ArrayList lists = null;
    try
    {
      doc = (WTDocument)ContentHelper.service.getContents(doc);
      QueryResult qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
      while (qr.hasMoreElements()) {
        ApplicationData oAppData = (ApplicationData)qr.nextElement();
        String fileName = oAppData.getFileName();
        InputStream inputStream = ContentServerHelper.service.findContentStream(oAppData);
        lists = readExcel(inputStream, fileName);
      }
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (PropertyVetoException e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(bool);
    }
    return lists;
  }

  public static void copy(FileInputStream input, FileOutputStream output) {
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

  public static ArrayList<String> readExcel(InputStream inputStream, String fileName) {
    ArrayList listRead = new ArrayList();
    try
    {
      CSCExcelHandler excel = ExcelHandlerFactory.getExcelHandler(inputStream, fileName);
      int rowNum = excel.getSheetRowCount();
      for (int i = 2; i < rowNum; i++) {
        String str = String.valueOf(excel.getValue(i, 1));
        if (str.indexOf("E") != -1) {
          BigDecimal bd = new BigDecimal(str);
          str = bd.toPlainString();
        }
        listRead.add(str);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return listRead;
  }

  public static void updateWTDocumentPrimaryContent(WTObject obj)
  {
    if ((obj instanceof WTDocument)) {
      WTDocument doc = (WTDocument)obj;
      System.out.println("----");
      File file = readPrimaryFile(doc);
      if (file != null) {
        PrintApplicationFromHelp.addPrimaryContent(doc, file);
        file.delete();
      }
    }
  }

  public static File readPrimaryFile(WTDocument doc)
  {
    boolean bool = SessionServerHelper.manager.isAccessEnforced();
    SessionServerHelper.manager.setAccessEnforced(false);
    File file = null;
    try
    {
      doc = (WTDocument)ContentHelper.service.getContents(doc);
      QueryResult qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
      while (qr.hasMoreElements()) {
        ApplicationData oAppData = (ApplicationData)qr.nextElement();
        String fileName = oAppData.getFileName();
        InputStream inputStream = ContentServerHelper.service.findContentStream(oAppData);
        InputStream inputStream01 = ContentServerHelper.service.findContentStream(oAppData);
        ArrayList<String[]> lists = readPrimaryExcel(inputStream, fileName);

        String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
        String filePath = wtHome + File.separator + "temp" + File.separator + fileName;
        FileOutputStream out = new FileOutputStream(filePath);
        copy((FileInputStream)inputStream, out);
        HSSFWorkbook wb = new HSSFWorkbook((FileInputStream)inputStream01);
        HSSFSheet sheet0 = wb.getSheetAt(0);
        HSSFCellStyle leftCellStyle = PrintApplicationFromHelp.getLeftCellstyle(wb);
        HSSFCellStyle centreCellStyle = PrintApplicationFromHelp.getCentreCellstyle(wb);
        int i = 0;
        for (String[] str : lists) {
          i++;
          PrintApplicationFromHelp.setCellValue(sheet0, leftCellStyle, centreCellStyle, i + 1, str[0], str[1], str[2], str[3], str[4], str[5], str[6], str[6], null);
        }
        wb.write(out);
        out.close();
        inputStream.close();
        file = new File(filePath);
      }
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    catch (PropertyVetoException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(bool);
    }

    return file;
  }

  public static void updateCellValue(HSSFSheet sheet, HSSFCellStyle leftCellStyle, int rowNum, String number)
  {
    HSSFRow row = sheet.createRow(rowNum);
    HSSFCell cell = row.createCell(1);
    cell.setCellValue(number);
    cell.setCellStyle(leftCellStyle);
    cell.setCellType(1);
  }

  public static ArrayList<String[]> readPrimaryExcel(InputStream inputStream, String fileName) {
    ArrayList listRead = new ArrayList();
    try
    {
      CSCExcelHandler excel = ExcelHandlerFactory.getExcelHandler(inputStream, fileName);
      int rowNum = excel.getSheetRowCount();
      for (int i = 2; i < rowNum; i++) {
        String[] str = new String[8];
        str[0] = excel.getValue(i, 0);
        String number = String.valueOf(excel.getValue(i, 1));
        if (number.indexOf("E") != -1) {
          BigDecimal bd = new BigDecimal(number);
          number = bd.toPlainString();
        }
        str[1] = number;
        str[2] = excel.getValue(i, 2);
        str[3] = excel.getValue(i, 3);
        str[4] = excel.getValue(i, 4);
        str[5] = excel.getValue(i, 5);
        str[6] = excel.getValue(i, 6);
        str[7] = excel.getValue(i, 7);
        listRead.add(str);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return listRead;
  }

  public static WTPart getObjectByNumber(String number) throws Exception
  {
    WTPart wtpart = null;
    if ((number != null) && (number.length() > 0))
    {
      WTPartMaster master = (WTPartMaster)getObjectByNumber(WTPartMaster.class, number);
      if (master != null)
        wtpart = (WTPart)getLatestObject(master);
    }
    return wtpart;
  }

  public static RevisionControlled getLatestObject(QueryResult queryresult) throws WTException {
    RevisionControlled rc = null;
    if (queryresult != null)
    {
      while (queryresult.hasMoreElements())
      {
        RevisionControlled obj = (RevisionControlled)queryresult.nextElement();
        if ((rc != null) && (!obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries())))
          continue;
        rc = obj;
      }

      if (rc != null)
      {
        return (RevisionControlled)VersionControlHelper.getLatestIteration(rc, false);
      }

      return rc;
    }

    return rc;
  }

  public static RevisionControlled getLatestObject(Mastered master) throws PersistenceException, WTException {
    if (master != null)
    {
      QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
      return getLatestObject(queryResult);
    }
    return null;
  }

  public static Persistable getObjectByNumber(Class objclass, String objectNumber) throws Exception
  {
    Persistable persistable = null;
    QuerySpec criteria = new QuerySpec(objclass);
    String numberField = getQueryColumnName(objclass, "NUMBER");
    criteria.appendWhere(new SearchCondition(objclass, numberField, "=", objectNumber, true), 
      new int[1]);
    QueryResult results = PersistenceHelper.manager.find(criteria);
    if (results.hasMoreElements())
      persistable = (Persistable)results.nextElement();
    return persistable;
  }

  public static String getQueryColumnName(Class objclass, String fieldName) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
  {
    return (String)objclass.getField(fieldName).get(null);
  }

  public static Boolean isNoPDF(WTObject obj)
    throws WTException, PropertyVetoException
  {
    boolean flag = false;
    if ((obj instanceof WTPart)) {
      WTPart part = (WTPart)obj;
      QueryResult qr = PersistenceHelper.manager.navigate(part, "buildSource", EPMBuildRule.class, true);
      while (qr.hasMoreElements()) {
        EPMDocument epm3d = (EPMDocument)qr.nextElement();
        ContentHolder ch = ContentHelper.service.getContents(epm3d);
        Vector attachmentList = 
          ContentHelper.getApplicationData(ch);
        if (epm3d.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
          if (attachmentList.size() > 0)
            flag = true;
        }
        else {
          EPMDocument epm2d = PrintApplicationFromHelp.getDrawing(epm3d, null);
          Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm2d);
          if ((representation != null) || (attachmentList.size() > 0))
            flag = true;
        }
      }
    }
    else if ((obj instanceof EPMDocument)) {
      EPMDocument epm = (EPMDocument)obj;
      ContentHolder ch = ContentHelper.service.getContents(epm);
      Vector attachmentList = ContentHelper.getApplicationData(ch);
      if (epm.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
        if (attachmentList.size() > 0)
          flag = true;
      }
      else {
        Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm);
        if ((representation != null) || (attachmentList.size() > 0)) {
          flag = true;
        }
      }
    }
    return Boolean.valueOf(flag);
  }
}