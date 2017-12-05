package ext.pdf;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import ext.hbt.signature.SignatureDataUtil;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeRecord2;
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
import wt.doc.WTDocument;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedHashMap;
import wt.maturity.MaturityHelper;
import wt.maturity.MaturityService;
import wt.maturity.PromotionNotice;
import wt.maturity.PromotionTarget;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.representation.RepresentationService;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfEngineService;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;

public class ReSignHelper
  implements RemoteAccess
{
  private static String path;

  static
  {
    try
    {
      WTProperties wtproperties = WTProperties.getLocalProperties();
      String wthome = wtproperties.getProperty("wt.home");
      path = wthome + "/temp/";
    }
    catch (Throwable localThrowable)
    {
    }
  }

  public static FormResult reSignPDF(NmCommandBean cb)
    throws Exception
  {
    FormResult formResult = new FormResult();
    Persistable persistable = (Persistable)cb.getPrimaryOid().getRefObject();
    List pboList = new ArrayList();

    String msg = checkSignData(pboList, persistable);
    if (msg.trim().length() != 0) {
      throw new Exception(msg);
    }

    WfProcess process = getLatestAssociatedProcess(pboList);
    if (process == null) {
      throw new Exception("未关联流程，不能进行重新签名！");
    }

    sign(persistable, process);
    FeedbackMessage feedBackMsg = new FeedbackMessage(FeedbackType.SUCCESS, 
      cb.getLocale(), "", null, new String[] { "重签成功" });
    formResult.addFeedbackMessage(feedBackMsg);
    formResult.setStatus(FormProcessingStatus.SUCCESS);
    return formResult;
  }

  public static WfProcess getLatestAssociatedProcess(List<Persistable> pboList)
    throws WTException
  {
    WfProcess process = null;
    for (Persistable pbo : pboList) {
      QueryResult qr = getAssociatedProcesses(pbo, null);
      if (qr != null) {
        while (qr.hasMoreElements()) {
          WfProcess tempPrcess = (WfProcess)qr.nextElement();
          if (process == null) {
            process = tempPrcess;
          }
          else if (tempPrcess.getEndTime().after(process.getEndTime())) {
            process = tempPrcess;
          }
        }
      }
    }

    return process;
  }

  public static String checkSignData(List<Persistable> pboList, Persistable persistable)
    throws Exception
  {
    StringBuffer msg = new StringBuffer();

    if (WorkInProgressHelper.isCheckedOut((Workable)persistable)) {
      msg.append("对象被检出，不能进行重新签名 \n");
    }

    if (!isDWG(persistable)) {
      boolean flag = haveRepresentation((Representable)persistable);
      if (!flag) {
        msg.append("对象不存在表示法文件，不能进行重新签名\n");
      }
    }

    String errorMsg = hasOpenRunningProcess(pboList, persistable);
    if (errorMsg.trim().length() != 0) {
      msg.append(errorMsg + "，不能进行重新签名\n");
    }
    return msg.toString();
  }

  public static boolean haveRepresentation(Representable representable)
    throws Exception
  {
    Representation representation = RepresentationHelper.service.getDefaultRepresentation(representable);
    if (representation != null) {
      representation = (Representation)ContentHelper.service.getContents(representation);
      Vector vector = ContentHelper.getContentList(representation);
      if (vector.size() > 0) {
        return true;
      }
    }
    return false;
  }

  public static String hasOpenRunningProcess(List<Persistable> pboList, Persistable persistable)
    throws WTException
  {
    WfProcess process = null;
    pboList.add(persistable);
    QueryResult qr = getAssociatedProcesses(persistable, WfState.OPEN_RUNNING);
    if (qr.size() > 0) {
      process = (WfProcess)qr.nextElement();
      if(persistable instanceof WTDocument){
    	  if(!"APPROVED".equalsIgnoreCase(((WTDocument) persistable).getLifeCycleState().toString())){
    		  return "关联了未结束流程：" + process.getIdentity();
    	  }
      }else  return "关联了未结束流程：" + process.getIdentity();
    }

    QueryResult ecns = ChangeHelper2.service.getUniqueImplementedChangeOrders((Changeable2)persistable);
    while (ecns.hasMoreElements()) {
      WTChangeOrder2 ecn = (WTChangeOrder2)ecns.nextElement();
      QueryResult qrr = ChangeHelper2.service.getChangeActivities(ecn);
      while(qrr.hasMoreElements()){
    	  WTChangeActivity2 eca = (WTChangeActivity2) qrr.nextElement();
    	  if(!eca.getName().contains(ecn.getNumber())) continue;
    	  QueryResult q = getAssociatedProcesses(eca,WfState.OPEN_RUNNING);
    	  if((q != null)&&q.hasMoreElements()){
    		  process = (WfProcess) q.nextElement();
    		  return "关联了未结束流程：" + process.getIdentity();
    	  }
    	  QueryResult p = ChangeHelper2.service.getChangeablesAfter(eca);
    	  while(p.hasMoreElements()){
    		  Persistable per = (Persistable) p.nextElement();
    		  if(per instanceof EPMDocument && persistable instanceof EPMDocument){
    			  if(((EPMDocument)per).getNumber().equals(((EPMDocument)persistable).getNumber())){
    				  pboList.add(eca);
    				  break;
    			  }
    		  }else if(per instanceof WTDocument && persistable instanceof WTDocument){
    			  if(((WTDocument)per).getNumber().equals(((WTDocument)persistable).getNumber())){
    				  pboList.add(eca);
    				  break;
    			  }
    		  }else if(per instanceof WTPart && persistable instanceof WTPart){
    			  if(((WTPart)per).getNumber().equals(((WTPart)persistable).getNumber())){
    				  pboList.add(eca);
    				  break;
    			  }
    		  }
    	  }
      }
      qr = getAssociatedProcesses(ecn, WfState.OPEN_RUNNING);
      if ((qr != null) && (qr.size() > 0)) {
    	  String state = ecn.getLifeCycleState().toString();
    	  if(!"Resolved".equalsIgnoreCase(state) && !"Executing".equalsIgnoreCase(state) && !"Execution".equalsIgnoreCase(state)){
    		  process = (WfProcess)qr.nextElement();
    		  return "关联了未结束流程：" + process.getIdentity();
    	  }
      }
      pboList.add(ecn);
    }

    for (PromotionNotice pn : getPromotionNotice(persistable)) {
      qr = getAssociatedProcesses(pn, WfState.OPEN_RUNNING);
      if ((qr != null) && (qr.size() > 0)) {
        process = (WfProcess)qr.nextElement();
        return "关联了未结束流程：" + process.getIdentity();
      }
      pboList.add(pn);
    }

    return "";
  }

  public static List<PromotionNotice> getPromotionNotice(Persistable persistable)
    throws WTException
  {
    List returnList = new ArrayList();
    if (persistable != null) {
      WTCollection wtc = new WTArrayList();
      wtc.add(persistable);
      WTKeyedHashMap wtkeymap = MaturityHelper.service.getTargetPromotionNotices(wtc);
      WTArrayList resultArrayList = (WTArrayList)wtkeymap.get(persistable);
      if (resultArrayList != null) {
        for (int i = 0; i < resultArrayList.size(); i++) {
          ObjectReference objRef = (ObjectReference)resultArrayList.get(i);
          if (objRef != null) {
            PromotionTarget pt = (PromotionTarget)objRef.getObject();
            if (pt != null) {
              PromotionNotice pn = pt.getPromotionNotice();
              returnList.add(pn);
            }
          }
        }
      }
    }
    return returnList;
  }

  public static QueryResult getAssociatedProcesses(Persistable persistable, WfState wfState)
    throws WTException
  {
    return WfEngineHelper.service.getAssociatedProcesses(persistable, wfState, null);
  }

  public static boolean isUG(EPMDocument epm)
  {
    boolean isUG = false;
    String epmapp = epm.getAuthoringApplication().toString();
    if (epmapp.equalsIgnoreCase("UG")) {
      isUG = true;
    }
    return isUG;
  }

  public static boolean isDWG(Persistable persistable)
  {
    boolean isDwg = false;
    if ((persistable instanceof EPMDocument)) {
      isDwg = isDWG((EPMDocument)persistable);
    }
    return isDwg;
  }

  public static boolean isDWG(EPMDocument epm)
  {
    boolean isDWG = false;
    if (epm.getAuthoringApplication().toString().equalsIgnoreCase("ACAD")) {
      isDWG = true;
    }
    return isDWG;
  }

  public static void sign(Persistable persistable, WfProcess process)
    throws WTException
  {
    WTObject pbo = (WTObject)process.getBusinessObjectReference(new ReferenceFactory()).getObject();
    if ((persistable instanceof EPMDocument)) {
      EPMDocument epm = (EPMDocument)persistable;
      if (isUG(epm))
        signUG(epm, process, pbo);
      else
        signEPM(epm, process, pbo);
    }
    else if ((persistable instanceof WTDocument)) {
      WTDocument doc = (WTDocument)persistable;
      String type = ext.tzc.tasv.doc.fileprint.SignUtil.getTypeByObject(doc);
      if ("tasvdoc12".equals(type)) {
        ObjectReference self = ObjectReference.newObjectReference(process);
        ext.tzc.tasv.doc.fileprint.SignUtil.execute(doc, self);
      } else {
        signDoc(doc, process, pbo);
      }
    }
  }

  public static void signUG(EPMDocument epm, WfProcess process, WTObject pbo)
    throws WTException
  {
    HashMap cooCacheMap = new HashMap();
    HashMap routesInfoMap = SignatureDataUtil.getRoutesInfo(process);
    ext.hbt.signature.SignUtil.signObject(epm, routesInfoMap, cooCacheMap);
  }

  public static void signDoc(WTDocument doc, WfProcess process, WTObject pbo)
    throws WTException
  {
    Vector vector = new Vector();
    vector.add(doc);

    Vector timeVec = new Vector();
    ObjectReference self = ObjectReference.newObjectReference(process);
    String typeName = getSoftType(doc);
    if (typeName.contains("com.tasv.tasvdoc05")) {
      SignSJRWS.signSJRWS(vector, self, timeVec, pbo);
    } else if (typeName.contains("tasvdoc01"))
    {
      signTZDAfterApprove(doc, self, timeVec, pbo, true);

      signTZDAfterApprove(doc, self, timeVec, pbo, false);
    } else if (typeName.contains("tasvdoc03"))
    {
      signTZDAfterApprove(doc, self, timeVec, pbo, true);

      if ((pbo instanceof WTDocument))
        signTZDAfterApprove(doc, self, timeVec, pbo, false);
      else
        signTZDAfterReceive(doc, self, timeVec, pbo, false);
    }
    else if ((typeName.endsWith(".bzjmxb")) || 
      (typeName.endsWith(".wgjmxb")) || 
      (typeName.endsWith(".zcmxb")) || 
      (typeName.endsWith(".fzmxb"))) {
      SignBaobiao.changePdfRevise(doc, self);
    }
    else
    {
      SignPDF_lg.signPdf(vector, self, timeVec);
    }
  }

  public static void signEPM(EPMDocument epm, WfProcess process, WTObject pbo)
    throws WTException
  {
    Vector vector = new Vector();
    vector.add(epm);

    Vector timeVec = new Vector();
    ObjectReference self = ObjectReference.newObjectReference(process);
    SignPDF_lg.signPdf(vector, self, timeVec);
  }

  public static String getSoftType(WTObject obj)
    throws WTException
  {
    TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
    return type.getTypename();
  }

  public static synchronized void signTZDAfterApprove(WTDocument wtdoc, ObjectReference self, Vector<String> timeVec, WTObject pbo, boolean afterApprove)
  {
    try
    {
      String absoluteFileName = "";
      if (wtdoc != null) {
        absoluteFileName = downloadpdf(wtdoc, afterApprove);
        if (absoluteFileName != null) {
          Hashtable revise = SignJSTZD.getReviews(self);
          String typename = getSoftType(wtdoc);
          if ((typename == null) || (typename.length() < 1)) {
            return;
          }
          Hashtable reset = new Hashtable();
          if (typename.contains("tasvdoc03"))
          {
            setTZDPrivateFields(new SignGGTZD(), wtdoc);
            reset = SignGGTZD.resetWF(revise);
            if (afterApprove) {
              reset.remove("收件人");
              reset.remove("更改单发放");
            }
            SignGGTZD.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo);
          } else if (typename.contains("tasvdoc01"))
          {
            setTZDPrivateFields(new SignJSTZD(), wtdoc);
            reset = SignJSTZD.resetWF(revise);
            if (afterApprove) {
              reset.remove("通知单接收");
              reset.remove("更改单发放");
            }
            SignJSTZD.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo);
          }
          docUtil.uploadpdf(wtdoc, absoluteFileName);
          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        } else {
          ContentHolder ch = ContentHelper.service
            .getContents(wtdoc);
          Vector attachmentList = 
            ContentHelper.getApplicationData(ch);
          for (int i = 0; i < attachmentList.size(); i++) {
            ApplicationData appDataPDF = (ApplicationData)attachmentList.get(i);
            String fileName = appDataPDF.getFileName();
            System.out.println("============aaa============" + fileName + "---------");
            if (fileName.toLowerCase().endsWith(".pdf")) {
              int pdf = fileName.lastIndexOf("-");
              if (pdf < 0) {
                pdf = fileName.lastIndexOf(".");
              }
              absoluteFileName = path + fileName;
              ContentServerHelper.service.writeContentStream(appDataPDF, absoluteFileName);
              Hashtable revise = SignJSTZD.getReviews(self);
              String typename = getSoftType(wtdoc);
              if ((typename == null) || (typename.length() < 1)) {
                return;
              }
              System.out.println("========================" + typename);
              Hashtable reset = new Hashtable();
              if (typename.contains("tasvdoc03"))
              {
                setTZDPrivateFields(new SignGGTZD(), wtdoc);
                reset = SignGGTZD.resetWF(revise);
                if (afterApprove) {
                  reset.remove("收件人");
                  reset.remove("更改单发放");
                }
                SignGGTZD.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo);
              } else if (typename.contains("tasvdoc01"))
              {
                setTZDPrivateFields(new SignJSTZD(), wtdoc);
                reset = SignJSTZD.resetWF(revise);
                if (afterApprove) {
                  reset.remove("通知单接收");
                  reset.remove("更改单发放");
                }
                SignJSTZD.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo);
              }
              appDataPDF.setFileName(fileName);
              ContentServerHelper.service.updateContent(ch, appDataPDF, absoluteFileName);
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

  public static synchronized void signTZDAfterReceive(WTDocument wtdoc, ObjectReference self, Vector<String> timeVec, WTObject pbo, boolean afterApprove)
  {
    WfProcess process = (WfProcess)self.getObject();
    try {
      String absoluteFileName = "";
      if (wtdoc != null) {
        absoluteFileName = downloadpdf(wtdoc, afterApprove);
        if (absoluteFileName != null)
        {
          setTZDPrivateFields(new SignECN2(), wtdoc);
          Hashtable revise = SignECN2.getReviews(self);
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
          reset = SignECN2.resetWF(revise);
          SignECN2.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo, process);
          docUtil.uploadpdf(wtdoc, absoluteFileName);
          File pdfFile = new File(absoluteFileName);
          pdfFile.delete();
        }
        else {
          setTZDPrivateFields(new SignECN2(), wtdoc);
          ContentHolder ch = ContentHelper.service.getContents(wtdoc);
          Vector attachmentList = ContentHelper.getApplicationData(ch);
          for (int i = 0; i < attachmentList.size(); i++) {
            ApplicationData appDataPDF = (ApplicationData)attachmentList.get(i);
            String fileName = appDataPDF.getFileName();
            System.out.println("============aaa============" + 
              fileName + "---------");
            if (fileName.toLowerCase().endsWith(".pdf")) {
              int pdf = fileName.lastIndexOf("-");
              if (pdf < 0) {
                pdf = fileName.lastIndexOf(".");
              }
              absoluteFileName = path + fileName;
              ContentServerHelper.service.writeContentStream(appDataPDF, absoluteFileName);
              Hashtable revise = SignECN2.getReviews(self);
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
              reset = SignECN2.resetWF(revise);
              SignECN2.writeText(reset, typename, absoluteFileName, timeVec, wtdoc, pbo, process);
              ContentServerHelper.service.updateContent(ch, appDataPDF, absoluteFileName);
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

  public static void setTZDPrivateFields(Object obj, WTDocument doc)
    throws Exception
  {
    setPrivateFieldsValue(obj, "docTime", doc.getCreateTimestamp()+"");
    setPrivateFieldsValue(obj, "bianhao", doc.getNumber());
  }

  public static void setPrivateFieldsValue(Object obj, String fieldName, String fieldValue)
    throws Exception
  {
    Class classType = obj.getClass();

    Field docTimeField = classType.getDeclaredField(fieldName);

    docTimeField.setAccessible(true);

    docTimeField.set(obj, fieldValue);
  }

  public static String downloadpdf(Representable doc, boolean afterApprove)
    throws WTException, PropertyVetoException, IOException
  {
    Representation representation = RepresentationHelper.service
      .getDefaultRepresentation(doc);
    if (representation != null) {
      representation = (Representation)ContentHelper.service.getContents(representation);
      Vector vector1 = ContentHelper.getContentList(representation);
      for (int l = 0; l < vector1.size(); l++) {
        ContentItem contentitem = (ContentItem)vector1.elementAt(l);
        if ((contentitem instanceof ApplicationData)) {
          ApplicationData applicationdata = (ApplicationData)contentitem;
          InputStream in = ContentServerHelper.service
            .findContentStream(applicationdata);
          String filename = applicationdata.getFileName();
          filename = SignJSTZD.unescape(filename);
          if (afterApprove) {
            filename = "批准_temp_" + filename;
          }

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
}