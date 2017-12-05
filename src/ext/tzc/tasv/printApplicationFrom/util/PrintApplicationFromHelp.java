package ext.tzc.tasv.printApplicationFrom.util;

import com.ptc.core.foundation.type.server.impl.TypeHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.PersistenceException;
import ext.tzc.tasv.constants.ObjectConstant;
import ext.tzc.tasv.util.StringUtil;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
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
import wt.content.FormatContentHolder;
import wt.doc.DocumentMaster;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMStructureHelper;
import wt.epm.structure.EPMStructureService;
import wt.fc.InvalidRoleException;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.PersistenceManagerSvr;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.httpgw.URLFactory;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.StandardIBAValueService;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.State;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartService;
import wt.pom.Transaction;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.rule.init.InitRuleEvalService;
import wt.rule.init.InitRuleHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTRuntimeException;
import wt.util.WTStandardDateFormat;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlService;

public class PrintApplicationFromHelp
{
  public static void getChildPart(WTPart part, ArrayList<Object> printList, ArrayList<Object> viewList, String tagName, boolean stateFlag, boolean flag)
  {
    try
    {
      QueryResult qr = WTPartHelper.service.getUsesWTParts(part, 
        WTPartHelper.service.findWTPartConfigSpec());
      while (qr.hasMoreElements()) {
        Object[] obj = (Object[])qr.nextElement();
        WTPart childpart = (WTPart)obj[1];
        String childtagName = childpart.getName();
        childtagName = childtagName.substring(0, childtagName.indexOf("-") + 1);
        String childState = childpart.getState().getState().toString();
        if (flag) {
          if (StringUtil.isNotEmpty(tagName)) {
            if (tagName.equals(childtagName)) {
              if (stateFlag) {
                if (childState.startsWith("RELEASED")) {
                  printList.add(childpart);
                  getChildPart(childpart, printList, viewList, tagName, stateFlag, true);
                } else {
                  viewList.add(childpart);
                  getChildPart(childpart, printList, viewList, null, stateFlag, false);
                }
              } else {
                printList.add(childpart);
                getChildPart(childpart, printList, viewList, tagName, stateFlag, true);
              }
            } else {
              viewList.add(childpart);
              getChildPart(childpart, printList, viewList, null, stateFlag, false);
            }
          } else {
            viewList.add(childpart);
            getChildPart(childpart, printList, viewList, null, stateFlag, false);
          }
        } else {
          viewList.add(childpart);
          getChildPart(childpart, printList, viewList, null, stateFlag, false);
        }
      }
    } catch (WTException e) {
      e.printStackTrace();
    }
  }

  public static String getObjUrl(WTObject wtObject)
  {
    try {
      URLFactory urlfactory = new URLFactory();
      String strUrl = urlfactory
        .getHREF("app/#ptc1/tcomp/infoPage") + 
        "?oid=OR%3A" + 
        wtObject;
      return strUrl;
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static WTDocument createPrintApplicationFrom(WTReference containerRefer, String namePrefix)
    throws IOException, Exception
  {
    TypeIdentifier typeidentifier = TypeHelper.getTypeIdentifier("wt.doc.WTDocument|com.ptc.ReferenceDocument|com.tasv.tasvdoc02");
    WTDocument doc = (WTDocument)TypeHelper.newInstance(typeidentifier);
    String name = "打印申请单_" + namePrefix + "_" + getCurrentTime("yyyyMMdd");
    doc.setName(name);
    Folder folder = getInitFolder(doc, (WTContainerRef)containerRefer);
    if (folder != null) {
      FolderHelper.assignLocation(doc, folder);
    }
    if (!PersistenceHelper.isPersistent(doc)) {
      PersistenceHelper.manager.save(doc);
    } else {
      StandardIBAValueService.theIBAValueDBService.updateAttributeContainer(doc, null, null, null);
      PersistenceServerHelper.manager.update(doc);
    }
    return doc;
  }

  public static Folder getInitFolder(Object obj, WTContainerRef wtContainerRef) throws WTException {
    Folder folder = (Folder)InitRuleHelper.evaluator.getValue("folder.id", obj, wtContainerRef);
    return folder;
  }

  public static void addPrimaryContent(ContentHolder holder, File file)
  {
    if (!RemoteMethodServer.ServerFlag) {
      Class[] argTypes = { ContentHolder.class, File.class };
      Object[] argValues = { holder, file };
      try {
        RemoteMethodServer.getDefault().invoke("addContentToDoc1", 
          PrintApplicationFromHelp.class.getName(), null, argTypes, 
          argValues);
      } catch (RemoteException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else {
      Transaction trx = new Transaction();
      try {
        trx.start();
        ContentHolder content = holder;
        content = ContentHelper.service.getContents(content);

        QueryResult qr1 = ContentHelper.service.getContentsByRole(
          content, ContentRoleType.PRIMARY);
        while (qr1.hasMoreElements()) {
          ApplicationData oAppData = (ApplicationData)qr1
            .nextElement();
          String strFileName = oAppData.getFileName();
          if (strFileName != null) {
            ContentServerHelper.service.deleteContent(content, 
              oAppData);
          }
        }

        ApplicationData data = ApplicationData.newApplicationData(holder);
        Vector vData = ContentHelper.getApplicationData(content);
        data.setRole(ContentRoleType.PRIMARY);
        data.setFileName(file.getName());

        FileInputStream in = new FileInputStream(file);
        data = ContentServerHelper.service.updateContent(content, data, 
          in);
        ContentServerHelper.service
          .updateHolderFormat((FormatContentHolder)content);
        PersistenceServerHelper.manager.update(data);
        trx.commit();
      } catch (PersistenceException e) {
        e.printStackTrace();
      } catch (WTRuntimeException e) {
        e.printStackTrace();
      } catch (WTException e) {
        e.printStackTrace();
      } catch (PropertyVetoException e) {
        e.printStackTrace();
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (trx != null)
          trx.rollback();
      }
    }
  }

  public static EPMDocument getDrawing(EPMDocument doc, Vector vec) throws WTException, InvalidRoleException
  {
    QueryResult qr1 = new QueryResult();
    try
    {
      qr1 = EPMStructureHelper.service.navigateReferencedBy(
        (DocumentMaster)doc.getMaster(), null, false);
    }
    catch (WTException e) {
      e.printStackTrace();
    }
    if (qr1 != null)
    {
      while (qr1.hasMoreElements()) {
        Object link = qr1.nextElement();
        if ((link instanceof EPMReferenceLink)) {
          EPMReferenceLink lin_r = (EPMReferenceLink)link;
          Object otherside = lin_r.getObject("referencedBy");
          if ((!(otherside instanceof EPMDocument)) || 
            (lin_r.getDepType() != 4)) continue;
          EPMDocument oo = (EPMDocument)otherside;
          oo = (EPMDocument)getLatestObject(oo.getMaster());
          return oo;
        }

      }

    }

    return null;
  }

  public static RevisionControlled getLatestObject(Mastered master) throws WTException {
    QueryResult queryResult = VersionControlHelper.service
      .allVersionsOf(master);
    return (RevisionControlled)queryResult.nextElement();
  }

  public static HSSFCellStyle getLeftCellstyle(HSSFWorkbook wb)
  {
    HSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderTop((short)1);
    cellStyle.setBorderBottom((short)1);
    cellStyle.setBorderLeft((short)1);
    cellStyle.setBorderRight((short)1);
    cellStyle.setAlignment((short)1);
    cellStyle.setWrapText(true);
    return cellStyle;
  }
  public static HSSFCellStyle getCentreCellstyle(HSSFWorkbook wb) {
    HSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setBorderTop((short)1);
    cellStyle.setBorderBottom((short)1);
    cellStyle.setBorderLeft((short)1);
    cellStyle.setBorderRight((short)1);
    cellStyle.setAlignment((short)2);
    cellStyle.setWrapText(true);
    return cellStyle;
  }

  public static File createExcel(String docNumber, ArrayList<Object> printList, ArrayList<Object> viewList, String fname, String tagName, boolean flag) throws IOException, Exception
  {
    String wtHome = WTProperties.getLocalProperties().getProperty("wt.home");
    String filePath = wtHome + File.separator + "temp" + File.separator + docNumber + "_打印申请单_" + fname + ".xls";
    String templatePath = wtHome + File.separator + ObjectConstant.PRINT_APPLICATION_FROM_TEMPLATE;
    copy(templatePath, filePath);
    FileOutputStream out = new FileOutputStream(filePath);
    FileInputStream in = new FileInputStream(templatePath);
    Set printtSet = new HashSet();
    Set viewSet = new HashSet();
    printtSet.addAll(printList);
    viewSet.addAll(viewList);

    HSSFWorkbook wb = new HSSFWorkbook(in);
    HSSFSheet sheet0 = wb.getSheetAt(0);
    HSSFSheet sheet1 = wb.getSheetAt(1);
    HSSFCellStyle leftCellStyle = getLeftCellstyle(wb);
    HSSFCellStyle centreCellStyle = getCentreCellstyle(wb);
    int i = 0;
    for (Iterator localIterator = printtSet.iterator(); localIterator.hasNext(); ) { Object wtObject = localIterator.next();
      i++;
      if ((wtObject instanceof WTPart)) {
        WTPart part = (WTPart)wtObject;
        String sequence = String.valueOf(i);
        String number = part.getNumber();
        String name = part.getName();
        String representationable = "无";
        QueryResult qr = PersistenceHelper.manager.navigate(part, "buildSource", EPMBuildRule.class, true);
        while (qr.hasMoreElements()) {
          EPMDocument epm3d = (EPMDocument)qr.nextElement();
          ContentHolder ch = ContentHelper.service
            .getContents(epm3d);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          if (epm3d.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
            if (attachmentList.size() > 0)
              representationable = "有";
          }
          else {
            EPMDocument epm2d = getDrawing(epm3d, null);
            if (epm2d != null) {
              Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm2d);
              if ((representation != null) || (attachmentList.size() > 0)) {
                representationable = "有";
              }
            }
          }
        }
        String version = part.getIterationDisplayIdentifier().toString();
        String state = part.getState().getState().getFullDisplay();
        String amount = "1";
        String mark = "";
        setCellValue(sheet0, leftCellStyle, centreCellStyle, i + 1, sequence, number, name, representationable, version, state, amount, mark, null);
      } else if ((wtObject instanceof EPMDocument)) {
        EPMDocument epm = (EPMDocument)wtObject;
        String sequence = String.valueOf(i);
        String number = epm.getNumber();
        String name = epm.getName();
        String representationable = "无";
        ContentHolder ch = ContentHelper.service.getContents(epm);
        Vector attachmentList = ContentHelper.getApplicationData(ch);
        if (epm.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
          if (attachmentList.size() > 0)
            representationable = "有";
        }
        else {
          Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm);
          if ((representation != null) || (attachmentList.size() > 0)) {
            representationable = "有";
          }
        }
        String version = epm.getIterationDisplayIdentifier().toString();
        String state = epm.getState().getState().getFullDisplay();
        String amount = "1";
        String mark = "";
        setCellValue(sheet0, leftCellStyle, centreCellStyle, i + 1, sequence, number, name, representationable, version, state, amount, mark, null);
      }
    }
    int h = 0;
    ArrayList viewValidList = new ArrayList();
    ArrayList list = new ArrayList();
    while(viewSet.iterator().hasNext()) { Object wtObject = viewSet.iterator().next();
      if ((wtObject instanceof WTPart)) {
        WTPart part = (WTPart)wtObject;
        String name = part.getName();
        String childTagName = name.substring(0, name.indexOf("-") + 1);
        if (tagName.equals(childTagName))
          viewValidList.add(part);
        else {
          list.add(part);
        }
      }
    }
    while(viewValidList.iterator().hasNext()) { Object wtObject = viewValidList.iterator().next();
      h++;
      if ((wtObject instanceof WTPart)) {
        WTPart part = (WTPart)wtObject;
        String name = part.getName();
        String sequence = String.valueOf(h);
        String number = part.getNumber();
        String representationable = "无";
        QueryResult qr = PersistenceHelper.manager.navigate(part, "buildSource", EPMBuildRule.class, true);
        while (qr.hasMoreElements()) {
          EPMDocument epm3d = (EPMDocument)qr.nextElement();
          ContentHolder ch = ContentHelper.service
            .getContents(epm3d);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          if (epm3d.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
            if (attachmentList.size() > 0)
              representationable = "有";
          }
          else {
            EPMDocument epm2d = getDrawing(epm3d, null);
            if (epm2d != null) {
              Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm2d);
              if ((representation != null) || (attachmentList.size() > 0))
                representationable = "有";
            }
          }
        }
        String version = part.getIterationDisplayIdentifier().toString();
        String state = part.getState().getState().getFullDisplay();
        String amount = "1";
        String mark = "";

        setCellValue(sheet1, leftCellStyle, centreCellStyle, h + 1, sequence, number, name, representationable, version, state, amount, mark, null);
      }
    }
    while(list.iterator().hasNext()) { 
    	Object wtObject = list.iterator().next();
      h++;
      if ((wtObject instanceof WTPart)) {
        WTPart part = (WTPart)wtObject;
        String name = part.getName();
        String sequence = String.valueOf(h);
        String number = part.getNumber();
        String representationable = "无";
        QueryResult qr = PersistenceHelper.manager.navigate(part, "buildSource", EPMBuildRule.class, true);
        while (qr.hasMoreElements()) {
          EPMDocument epm3d = (EPMDocument)qr.nextElement();
          ContentHolder ch = ContentHelper.service
            .getContents(epm3d);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          if (epm3d.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
            if (attachmentList.size() > 0)
              representationable = "有";
          }
          else {
            EPMDocument epm2d = getDrawing(epm3d, null);
            if (epm2d != null) {
              Representation representation = RepresentationHelper.service.getDefaultRepresentation(epm2d);
              if ((representation != null) || (attachmentList.size() > 0))
                representationable = "有";
            }
          }
        }
        String version = part.getIterationDisplayIdentifier().toString();
        String state = part.getState().getState().getFullDisplay();
        String amount = "1";
        String mark = "";

        setCellValue(sheet1, leftCellStyle, centreCellStyle, h + 1, sequence, number, name, representationable, version, state, amount, mark, null);
      }

    }

    wb.write(out);
    out.close();
    in.close();
    File file = new File(filePath);
    return file;
  }

  public static void setCellValue(HSSFSheet sheet, HSSFCellStyle leftCellStyle, HSSFCellStyle centreCellStyle, int rowNum, String sequence, String number, String name, String representationable, String version, String state, String amount, String mark, String reason)
  {
    HSSFRow row = sheet.createRow(rowNum);
    HSSFCell cell = row.createCell(0);
    cell.setCellValue(sequence);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    cell = row.createCell(1);
    cell.setCellValue(number);
    cell.setCellStyle(leftCellStyle);
    cell.setCellType(1);

    cell = row.createCell(2);
    cell.setCellValue(name);
    cell.setCellStyle(leftCellStyle);
    cell.setCellType(1);

    cell = row.createCell(3);
    cell.setCellValue(representationable);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    cell = row.createCell(4);
    cell.setCellValue(version);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    cell = row.createCell(5);
    cell.setCellValue(state);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    cell = row.createCell(6);
    cell.setCellValue(amount);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    cell = row.createCell(7);
    cell.setCellValue(mark);
    cell.setCellStyle(centreCellStyle);
    cell.setCellType(1);

    if (StringUtil.isNotEmpty(reason)) {
      cell = row.createCell(8);
      cell.setCellValue(reason);
      cell.setCellStyle(centreCellStyle);
      cell.setCellType(1);
    }
  }

  public static void copy(String sourceFile, String targetFile) {
    FileInputStream input = null;
    FileOutputStream output = null;
    try {
      input = new FileInputStream(sourceFile);
      output = new FileOutputStream(targetFile);
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

  public static String getCurrentTime(String DATE_FORMAT)
  {
    Locale locale = WTContext.getContext().getLocale();
    Calendar calendar = Calendar.getInstance(WTContext.getContext()
      .getTimeZone(), locale);
    Date datDate = calendar.getTime();
    return WTStandardDateFormat.format(datDate, DATE_FORMAT, locale, 
      calendar.getTimeZone());
  }
}