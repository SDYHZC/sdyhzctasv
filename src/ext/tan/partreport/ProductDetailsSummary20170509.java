package ext.tan.partreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartUsageLink;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.config.ConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

public class ProductDetailsSummary20170509 implements RemoteAccess {

	private static String CLASSNAME = ProductDetailsSummary20170509.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	private static final String IBANAME_LINK_BZ = "BZ";
	private static final int PAGE_SIZE = 30;
	private static final int EACH_PAGE_SIZE = 38;
	private static final String NAME_FLEX = "#";
	private static final String STATE_FLEX = "_";
	private static final String PRODUCT_AFTER = "-JW1-13-1";
	private static final String VALUE_CODE = "CODE";
	private static final String VALUE_NAME = "NAME";
	private static final String VALUE_PAGE_COUNT = "PAGE_COUNT";
	private static final String numbercharList = "0123456789";
	
	
	public static XSSFWorkbook getAllInforOfPartStruct(WTPart toppart) throws WTException, IOException
	{
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		XSSFWorkbook wb=null;
		try
		{
			SessionServerHelper.manager.setAccessEnforced(false);
			loger.debug("  productpart ===" + toppart.getDisplayIdentifier());
			View viewObj = ViewHelper.service.getView(toppart.getViewName());
			
			 String state = toppart.getLifeCycleState().getDisplay();
	            if(state.equals("正在审阅"))
	            {
					throw new WTException(toppart.getDisplayIdentifier() + "当前出于正在审阅状态，不能进行部件导出！");
				}
			
			if(toppart.isEndItem())
			{
			WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
			WTPartStandardConfigSpec standardConfig = config.getStandard();
			standardConfig.setView(viewObj);
			List alldata = new ArrayList();
			//获取下一层的明细表
			List likattrList = new ArrayList();
			likattrList.add(IBANAME_LINK_BZ);
			
			QueryResult qr = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(toppart, config);
			while (qr.hasMoreElements())
			{
				Persistable apersistable[] = (Persistable[]) qr.nextElement();
				if (apersistable[1] instanceof WTPart)
				{
					WTPart subpart = (WTPart) apersistable[1];
					loger.debug("  subpart ===" + subpart.getDisplayIdentifier());
					String partname=subpart.getName();
					HashMap itemInfor=new HashMap();
			        String productnameafter=ReportUtil.getNameStr(partname,NAME_FLEX, false);
			        String productnamebefore=ReportUtil.getNameStr(partname,NAME_FLEX, true);
					itemInfor.put(VALUE_CODE,productnamebefore); //表示明细的物料名称#前面部分
					itemInfor.put(VALUE_NAME,productnameafter); //表示明细的物料名称#号后面部分
					HashMap allsubMap=new HashMap();
					getAllSubStructure( subpart, config, allsubMap); 
					int datacount= ReportUtil.getAllCount(allsubMap) ;
					//计算需要分多少页
					loger.debug("  child datacount ===" + datacount);
					int pageSize=1;
					if((datacount%PAGE_SIZE)==0)
						pageSize=datacount/PAGE_SIZE;
					else
						pageSize=datacount/PAGE_SIZE+1;
					loger.debug(" child pageSize ===" + pageSize);
					itemInfor.put(VALUE_PAGE_COUNT,pageSize); //对应的分组明细的页数
					
					WTPartUsageLink usageLink = (WTPartUsageLink) apersistable[0];
					HashMap linkIbaht = ReportUtil.getObjectValues(usageLink, likattrList);
					itemInfor.put(IBANAME_LINK_BZ,(String)linkIbaht.get(IBANAME_LINK_BZ));//BOM结构中的备注
					alldata.add(itemInfor);
				}
			}
			loger.debug("  alldata ===" + alldata);
			//按code进行排序
			List alldatanew=sortData(alldata);
			wb=writeToExcel(toppart,alldatanew);
			loger.debug(" return XSSFWorkbook ===" + wb);
			
		}
			else
			{
				throw new WTException("当前部件"+toppart.getDisplayIdentifier() + "不是成品，请选择成品进行报表导出！");
			}
		}
		catch (WTPropertyVetoException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new WTException(e);
		}
		 
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(bool);
		}
		return wb;
	}

	private static void getAllSubStructure(WTPart toppart, ConfigSpec configSpec, HashMap allstandardPartMap) throws WTException  
	{
		QueryResult qr = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(toppart, configSpec);
		if (qr == null || !qr.hasMoreElements() || qr.size() <= 0)
		{
			return;
		}
		while (qr.hasMoreElements())
		{
			Persistable apersistable[] = (Persistable[]) qr.nextElement();
			if (apersistable[1] instanceof WTPart)
			{
				WTPart subpart = (WTPart) apersistable[1];
				WTPartUsageLink usageLink = (WTPartUsageLink) apersistable[0];
				// 获取已经记录的父件link
				List parentlist = (List) allstandardPartMap.get(subpart);
				if (parentlist == null)
				{
					// 如果不存在，则直接记录父件跟自己的link（后面出报表的时候获取各自属性比较方便）
					parentlist = new ArrayList();
					parentlist.add(usageLink);
				}
				else
				{
					// 如果已经记录，则不再重复记录。如果没记录，则记录到list中
					if (!parentlist.contains(usageLink))
						parentlist.add(usageLink);
				}
				allstandardPartMap.put(subpart, parentlist);
				getAllSubStructure(subpart, configSpec, allstandardPartMap);
			}
		}
	}

	public static XSSFWorkbook writeToExcel(WTPart toppart,List datalist) throws WTException
	{

		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		try
		{
			// 获取模板
			String templatePath = ReportUtil.getTemplatePath() + File.separator + "ProductDetailsReport.xlsx";
			loger.debug("  templatePath ===" + templatePath);

			fis = new FileInputStream(templatePath);
			// 生成要导出的Excel
			wb = new XSSFWorkbook(fis);
			XSSFSheet sheetT = wb.getSheetAt(0);
			int datacount= datalist.size() ;
			loger.debug("  datacount ===" + datacount);

			//计算需要分多少页
			int pageSize=1;
			if((datacount%PAGE_SIZE)==0)
				pageSize=datacount/PAGE_SIZE;
			else
				pageSize=datacount/PAGE_SIZE+1;
			loger.debug("  pageSize ===" + pageSize);

			// 往模板sheet插入抬头的数据
			writeTitleToSheet(toppart, sheetT,pageSize,1,0);
			 
			int lastrowNo=pageSize*EACH_PAGE_SIZE;
			//按分页的情况
			for(int i=0;i<lastrowNo;i++)
			{
				if(i%EACH_PAGE_SIZE==0&&i!=0)
				{
					ExcelUtil.createAllPageRows(sheetT, i,EACH_PAGE_SIZE);
				   //合并表头单元格
					ExcelUtil.mergerTitleForReport2(sheetT,i);
					//合并要填写数据的单元格
					mergerDataForReport(sheetT, i);
					//拷贝第一页的信息
					ExcelUtil.copyTemplateInforToSheet(wb,sheetT,i,i/EACH_PAGE_SIZE+1,EACH_PAGE_SIZE,11);
				}
			}
			//先写sheet中要填写的结构信息
			writeDataToSheet(sheetT,datacount,pageSize, datalist);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new WTException(e);
		}
		return wb;
	}
	
	public static void writeTitleToSheet(WTPart toppart, XSSFSheet sheetT,int totalPageNo,int currentPageNo,int targetBeginRowNo) 
	{
        String partname=toppart.getName();
       // String productnamebefore=ReportUtil.getNameStr(partname,NAME_FLEX, true);
        String productnameafter=ReportUtil.getNameStr(partname,NAME_FLEX, false);
        String productnameafterfinal=ReportUtil.getStrBeforeChs(productnameafter);
        String state=toppart.getLifeCycleState().getDisplay();
        String stateType=ReportUtil.getNameStr(state,STATE_FLEX, false);
        String curretnPage="第  "+currentPageNo+" 页";
        String totalPage="共  "+totalPageNo+" 页";
        XSSFRow tempRow=sheetT.getRow(targetBeginRowNo); 
		XSSFCell tempcell=tempRow.getCell(5);
		tempcell.setCellValue(productnameafter);
		tempcell=tempRow.getCell(8);
		tempcell.setCellValue(productnameafterfinal+PRODUCT_AFTER);
		tempRow=sheetT.getRow(3); 
		tempcell=tempRow.getCell(8);
		if(state.equals("返工")){
			tempcell.setCellValue("S");
		}else{
		tempcell.setCellValue(stateType);
		}
		tempRow=sheetT.getRow(2); 
		tempcell=tempRow.getCell(11);
		tempcell.setCellValue(totalPage);
		tempRow=sheetT.getRow(3); 
		tempcell=tempRow.getCell(11);
		tempcell.setCellValue(curretnPage);
		loger.debug("  writeTitleToSheet5 ==========");
		loger.debug("               partname5 ===" + partname);
		loger.debug("               currentPageNo5 ===" + currentPageNo);
		loger.debug("               totalPageNo5 ===" + totalPageNo);
		loger.debug("               stateType5 ===" + stateType);
		loger.debug("               productnameafterfinal5 ===" + productnameafterfinal);
		loger.debug("               productnameafter5 ===" + productnameafter);

	}
	//填写结构的数据到表中间
	public static void writeDataToSheet(XSSFSheet sheetNew ,int allcount,int pageSize,List datalist)  
	{
		loger.debug("  writeDataToSheet    begin");
		int nextRowNo=8;//从第十行开始写数据
		int orderNo=1;
		XSSFRow row=null;
		for(int i=0;i<datalist.size();i++)
		{
			HashMap tempmap=(HashMap) datalist.get(i);
			//创建行和列
			//row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
			row=sheetNew.getRow(nextRowNo);
			loger.debug("  writeDataToSheet    nextRowNo==="+nextRowNo);
			String code=(String) tempmap.get(VALUE_CODE);//代号
			String nameAndType=(String) tempmap.get(VALUE_NAME);//名称
			Integer count=(Integer) tempmap.get(VALUE_PAGE_COUNT);//页数
			String bz=(String) tempmap.get(IBANAME_LINK_BZ);//备注
			row.getCell(0).setCellValue(orderNo);//第一列为序号
			row.getCell(2).setCellValue(code);//第3列为代号
			row.getCell(4).setCellValue(nameAndType);//第4列为名称 
			row.getCell(8).setCellValue(count);//第8列为页数
			row.getCell(9).setCellValue(bz);//第910列为备注
			orderNo=orderNo+1;
			nextRowNo=ExcelUtil.getNextRowNo(nextRowNo,PAGE_SIZE,8);
		}
		loger.debug("  writeDataToSheet    end");
	}
	
	public static void mergerDataForReport(XSSFSheet toSheet, int beginRowNo)
	{
		for(int i=beginRowNo+8;i<beginRowNo+EACH_PAGE_SIZE;i++)
		{
			CellRangeAddress region0=new CellRangeAddress(i,i,0,1); 
			CellRangeAddress region1=new CellRangeAddress(i,i,2,3); 
			CellRangeAddress region2=new CellRangeAddress(i,i,4,7); 
			CellRangeAddress region3=new CellRangeAddress(i,i,9,11); 
			
			toSheet.addMergedRegion(region0);
			toSheet.addMergedRegion(region1);
			toSheet.addMergedRegion(region2);
			toSheet.addMergedRegion(region3);

		}
	}
	
	public static String getNumberStr(String str)
	{
	   StringBuffer buf=new StringBuffer("");
	   int index=str.lastIndexOf("-");
	   String tempstr=str;
	   if(index!=-1)
		   tempstr=str.substring(index+1,str.length());
	   
	   for(int i=0;i<tempstr.length();i++)
	   {
		   String chrStr=String.valueOf(tempstr.charAt(i));
		   //不是数字型的，退出循环
		   if(numbercharList.indexOf(chrStr)==-1)
			   break;
		   buf.append(chrStr);
	   }
	   return buf.toString();
	}
	
	public static List sortData(List datalist)
	{
		TreeMap treemap = new TreeMap();
		List newdatalist = new ArrayList();
		for (int i = 0; i < datalist.size(); i++)
		{
			HashMap tempmap = (HashMap) datalist.get(i);
			String tempcode = (String) tempmap.get(VALUE_CODE);
			String keycode = getNumberStr(tempcode);
			treemap.put(Integer.valueOf(keycode), tempmap);

		}
		Iterator it = treemap.keySet().iterator();
		while (it.hasNext())
		{
			newdatalist.add(treemap.get(it.next()));
		}
		return newdatalist;
	}
}
