package ext.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.httpgw.URLFactory;
import wt.inf.library.WTLibrary;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteAccess;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.ManagedBaseline;
import wt.vc.views.ViewHelper;
import ext.workflow.MXBReportUtil;
import ext.tasv.preference.Select;
import ext.util.IBAUtility;



public class DownloadECN
  implements RemoteAccess
{
  private static WTProperties properties;
  private static String WT_HOME = null;
  public static WTPartConfigSpec wtPartStandardConfigSpec;
  public static String path;
  public static String wthome;
  static {
    try {
      Properties props = WTProperties.getLocalProperties();
      WT_HOME = props.getProperty("wt.home");
      WTProperties wtproperties = WTProperties.getLocalProperties();
	  wthome = wtproperties.getProperty("wt.home");
	  path = wthome + File.separator + "codebase/temp" + File.separator;
	  wtPartStandardConfigSpec = WTPartConfigSpec.newWTPartConfigSpec(WTPartStandardConfigSpec
			.newWTPartStandardConfigSpec(ViewHelper.service.getView("Design"), null));
    } catch (Throwable throwable) {
    	throw new ExceptionInInitializerError(throwable);
    }
  }
  public static URL download(WTObject pbo) throws Exception {
    String reportName = "";
	String strbt="";
	//增加PBO是ECA的情况
	if(pbo instanceof WTChangeActivity2){
		WTChangeActivity2 eca = (WTChangeActivity2) pbo;
		QueryResult qr = ChangeHelper2.service.getChangeOrder(eca);
		if(qr.hasMoreElements()){
			WTChangeOrder2 o = (WTChangeOrder2) qr.nextElement();
			pbo = o;
		}
	}
    if (pbo instanceof WTChangeOrder2) {
    	WTChangeOrder2 ecn = (WTChangeOrder2)pbo;  
    	strbt = ecn.getNumber();
        SimpleDateFormat fmDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String tmpStr = fmDate.format(new Date());
		tmpStr=tmpStr.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
        reportName = strbt+tmpStr+".xlsx";
      //从模板生成新的EXCEL
    	String outFilePath = MXBReportUtil.copyExcelMode("ECN.xlsx",reportName);
    	//写入数据
    	XSSFWorkbook wb = MXBReportUtil.readFile(outFilePath);
    	XSSFSheet s = wb.getSheetAt(0);
    	XSSFCellStyle cellStyle = null;
    	cellStyle = setCellStyle(wb);
		XSSFRow r = s.getRow(1);
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		String dt = date.format(new Date());
		QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
	    while (qr.hasMoreElements())
        {
	      Persistable pobj = (Persistable)qr.nextElement();
          if (pobj instanceof WTPart) {
        	  WTPart pt = (WTPart)pobj;
        	  String num = pt.getNumber();
        	  LinkedHashMap map = new LinkedHashMap();
        	  List<WTPart> ptList= Select.from(WTPart.class).andFrom(WTLibrary.class)
	                    .join(0,WTPart.CONTAINER_REFERENCE,1)
	                    .where(0,WTPart.NUMBER,SearchCondition.EQUAL,pt.getNumber())
	                    .where(1,WTLibrary.NAME,SearchCondition.EQUAL,"非优选标准件库")
	                    .onlyLatest()
	                    .list();
			  System.out.println("zyj--ts--ptList.size:"+ptList.size());
      	      if(ptList!=null && ptList.size()>0){
      		    for(int i = 0 ; i < ptList.size() ; i++){
      			  WTPart part  = ptList.get(i);
      			  double ct = 1;
      			  map.put(part, ct);
      		    }
      	      }
    		  getAllPart(pt,map);
    		  Iterator keyset = map.keySet().iterator();
    		  int i = 0;
	  		  while (keyset.hasNext())
  			  { 
	  			WTPart inpart= (WTPart) keyset.next();
	  			double count =   (Double) map.get(inpart);
	  			int rNum = s.getLastRowNum()+1;
				System.out.println("zyj--rNum:"+rNum);
  				if(i==0){
  					XSSFRow rowi = s.createRow(rNum);
  					XSSFCell bhC = rowi.createCell(0);
  					bhC.setCellStyle(cellStyle);
  					bhC.setCellValue(pt.getNumber());
  					XSSFCell mcC = rowi.createCell(1);
  					mcC.setCellStyle(cellStyle);
  					mcC.setCellValue(pt.getName());
  					XSSFCell yxbhC = rowi.createCell(2);
  					yxbhC.setCellStyle(cellStyle);
  					yxbhC.setCellValue(inpart.getNumber());
  					XSSFCell yxmcC = rowi.createCell(3);
  					yxmcC.setCellStyle(cellStyle);
  					yxmcC.setCellValue(inpart.getName());
  					XSSFCell yxslC = rowi.createCell(4);
  					yxslC.setCellStyle(cellStyle);
  					yxslC.setCellValue(count);
  				}else{
  					XSSFRow rowi = s.createRow(rNum);
  					XSSFCell bhC = rowi.createCell(0);
  					bhC.setCellStyle(cellStyle);
  					bhC.setCellValue("");
  					XSSFCell mcC = rowi.createCell(1);
  					mcC.setCellStyle(cellStyle);
  					mcC.setCellValue("");
  					XSSFCell yxbhC = rowi.createCell(2);
  					yxbhC.setCellStyle(cellStyle);
  					yxbhC.setCellValue(inpart.getNumber());
  					XSSFCell yxmcC = rowi.createCell(3);
  					yxmcC.setCellStyle(cellStyle);
  					yxmcC.setCellValue(inpart.getName());
  					XSSFCell yxslC = rowi.createCell(4);
  					yxslC.setCellStyle(cellStyle);
  					yxslC.setCellValue(count);
  					
  				}
  			  }
          }
        }
		FileOutputStream out = new FileOutputStream(outFilePath);
		wb.write(out);
		out.close();
    }
    String outZipFile = "temp"+File.separatorChar+reportName;
    System.out.println("zyj--test--outZipFile--"+outZipFile);
    URLFactory urlfactory = new URLFactory();
    String strUrl = urlfactory.getHREF(outZipFile);//ext.wl.
    URL url = new URL(strUrl);
    return url;
  }
	public static XSSFWorkbook readFile(String filename) throws IOException
	{
		return new XSSFWorkbook(new FileInputStream(filename));
	}
	 public static RevisionControlled getLatestObject(Mastered master) throws PersistenceException, WTException
		{
			if (master != null)
			{
				QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
				return getLatestObject(queryResult);
			}
			return null;
		}
		public static RevisionControlled getLatestObject(QueryResult queryresult) throws WTException
		{
			RevisionControlled rc = null;
			if (queryresult != null)
			{
				while (queryresult.hasMoreElements())
				{
					RevisionControlled obj = ((RevisionControlled) queryresult.nextElement());
					if (rc == null || obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))
					{
						rc = obj;
					}
				}
				if (rc != null)
				{
					return (RevisionControlled) wt.vc.VersionControlHelper.getLatestIteration(rc, false);
				} else
				{
					return rc;
				}
			}
			return rc;
		}
		public static void getAllPart(WTPart topPart,LinkedHashMap map) throws WTException
		{
			QueryResult subNodes = WTPartHelper.service.getUsesWTParts(topPart, wtPartStandardConfigSpec);
			while (subNodes.hasMoreElements())
			{
				Persistable aSubNodePair[] = (Persistable[]) subNodes.nextElement();
				if (aSubNodePair[1] instanceof WTPart) 
				{
					WTPart childpart = (WTPart) aSubNodePair[1];
					//物料编码
					String partNum = childpart.getNumber();
					
					WTPartUsageLink usagelink = (WTPartUsageLink) aSubNodePair[0];
					Quantity localQuantity = usagelink.getQuantity();
					//数量
					double count= localQuantity.getAmount();
					List<WTPart> ptList= Select.from(WTPart.class).andFrom(WTLibrary.class)
		                    .join(0,WTPart.CONTAINER_REFERENCE,1)
		                    .where(0,WTPart.NUMBER,SearchCondition.EQUAL,childpart.getNumber())
		                    .where(1,WTLibrary.NAME,SearchCondition.EQUAL,"非优选标准件库")
		                    .onlyLatest()
		                    .list();
					System.out.println("zyj--ts--childpartList.size1:"+ptList.size());
	        	    if(ptList!=null && ptList.size()>0){
	        		  for(int i = 0 ; i < ptList.size() ; i++){
	        			  WTPart part  = ptList.get(i);
	        			  map.put(part, count);
	        		  }
	        	     }
					getAllPart(childpart,map);
				}
				else if (aSubNodePair[1] instanceof WTPartMaster) 
				{
					continue;
				}
			}
		}
		public static XSSFCellStyle setCellStyle(XSSFWorkbook wb) {
			XSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
			return cellStyle;
		}
}