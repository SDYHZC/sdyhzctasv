package ext.tan.partreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
import wt.part.Quantity;
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

public class StandardPartSummary20170710 implements RemoteAccess {

	private static String CLASSNAME = StandardPartSummary20170710.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	private static final String IBA_MATERIAL = "CMAT";
	private static final String IBA_PART_TYPE = "LJLB";
	private static final String IBANAME_LINK_BZ = "BZ";
	private static final String STANDARD_PART = "标准件";
	private static final int PAGE_SIZE = 30;
	private static final int EACH_PAGE_SIZE = 39;
	private static final String NAME_FLEX = "#";
	private static final String STATE_FLEX = "_";
	private static final String PRODUCT_AFTER = "-JW1-13-1";
	private static final String IBA_DRAWING_NO = "TUFU";
	
	public static XSSFWorkbook getAllInforOfPartStruct(WTPart toppart) throws WTException
	{
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		XSSFWorkbook wb=null;
		try
		{
			SessionServerHelper.manager.setAccessEnforced(false);
			loger.debug("  toppart ===" + toppart.getDisplayIdentifier());
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
			List attrList = new ArrayList();
			attrList.add(IBA_PART_TYPE);
			HashMap allstandardPartMap = new HashMap();
			HashMap sumMap = new HashMap();
			double sum = 1;
			getPartSubStructure(toppart, config, allstandardPartMap, sumMap, attrList, sum);
			loger.debug("  sumMap ===" + sumMap);
			loger.debug("  allstandardPartMap ===" + allstandardPartMap);
			wb=writeToExcel(toppart,allstandardPartMap,sumMap);
			loger.debug("  XSSFWorkbook ===" + wb);
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

	private static void getPartSubStructure(WTPart toppart, ConfigSpec configSpec, HashMap allstandardPartMap, HashMap sumMap, List attrList, double currentSum) throws WTException  
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
				
			//	System.out.println("测试第一个"+subpart.getName());
				
				WTPartUsageLink usageLink = (WTPartUsageLink) apersistable[0];
			//	System.out.println("测试第一个"+apersistable[0]);
				// 获取当前的link数量，递归跟上层数量惊喜相乘
				double amount = usageLink.getQuantity().getAmount();
				double newsum = currentSum * amount;
				HashMap subIbaht = ReportUtil.getObjectValues(subpart, attrList);
				// 过滤出标准件
				String partType = (String) subIbaht.get(IBA_PART_TYPE);
				if (partType != null && partType.equals(STANDARD_PART))
				{
					loger.debug("  subpart ===" + subpart.getDisplayIdentifier() + " is " + STANDARD_PART + " and parent part is " + toppart.getDisplayIdentifier());
					// 获取已经记录的父件link
					List parentlist = (List) allstandardPartMap.get(subpart);
					System.out.println(" 旧父件parentlist ===" + parentlist);
					loger.debug(" old parentlist ===" + parentlist);
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
					loger.debug(" new parentlist ===" + parentlist);
					allstandardPartMap.put(subpart, parentlist);
					// 统计总数量
					String subpartnumber = subpart.getNumber();
					Double oldSum = (Double) sumMap.get(subpartnumber);
					loger.debug("  parentCount ===" + newsum);
					loger.debug("  oldSum ===" + oldSum);

					// 如果原来没有记录数量 ，则获取当前层级的数量作为总数量
					double allsum = 0;
					if (oldSum == null)
						allsum = newsum;
					// 如果原来有记录数量 ，则获取当前层级的数量*原来的数量 作为总数量
					else
						allsum = newsum + oldSum.doubleValue();
					loger.debug("  newAllSum ===" + allsum);
					sumMap.put(subpartnumber, Double.valueOf(allsum));
				}
				getPartSubStructure(subpart, configSpec, allstandardPartMap, sumMap, attrList, newsum);
			}
		}
	}

	public static XSSFWorkbook writeToExcel(WTPart toppart,HashMap allstandardPartMap, HashMap sumMap) throws WTException
	{

		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		try
		{
			// 获取模板
			String templatePath = ReportUtil.getTemplatePath() + File.separator + "StandardPartReport.xlsx";
			loger.debug("  templatePath ===" + templatePath);

			fis = new FileInputStream(templatePath);
			// 生成要导出的Excel
			wb = new XSSFWorkbook(fis);
			XSSFSheet sheetT = wb.getSheetAt(0);
			int datacount= ReportUtil.getAllCount(allstandardPartMap) ;
			loger.debug("  datacount ===" + datacount);

			//计算需要分多少页
			int pageSize=1;
			if((datacount%PAGE_SIZE)==0)
				pageSize=datacount/PAGE_SIZE;
			else
				pageSize=datacount/PAGE_SIZE+1;
			loger.debug("  pageSize ===" + pageSize);

			// 往模板sheet插入抬头的数据
			writeTitleToSheet(toppart, sheetT,pageSize,1,32);
			 
			int lastrowNo=pageSize*EACH_PAGE_SIZE;
			//按分页的情况
			for(int i=0;i<lastrowNo;i++)
			{
				if(i%EACH_PAGE_SIZE==0&&i!=0)
				{
					ExcelUtil.createAllPageRows(sheetT, i,EACH_PAGE_SIZE);
				   //合并表头单元格
					ExcelUtil.mergerTitleForReport(sheetT,i);
					//合并要填写数据的单元格
					mergerDataForReport(sheetT, i);
					//拷贝第一页的信息
					ExcelUtil.copyTemplateInforToSheet1(wb,sheetT,i,i/EACH_PAGE_SIZE+1,EACH_PAGE_SIZE,10);
				}
			}
			//先写sheet中要填写的结构信息
			writeDataToSheet(sheetT,datacount,pageSize,allstandardPartMap,sumMap);
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
		tempcell=tempRow.getCell(7);
		tempcell.setCellValue(productnameafterfinal+PRODUCT_AFTER);
		tempRow=sheetT.getRow(35); 
		tempcell=tempRow.getCell(7);
		if(state.equals("返工")){
			tempcell.setCellValue("S");
		}else{
		tempcell.setCellValue(stateType);
		}
		tempRow=sheetT.getRow(34); 
		tempcell=tempRow.getCell(10);
		tempcell.setCellValue(totalPage);
		tempRow=sheetT.getRow(35); 
		tempcell=tempRow.getCell(10);
		tempcell.setCellValue(curretnPage);
		loger.debug("  writeTitleToSheet ==========");
		loger.debug("               partname ===" + partname);
		loger.debug("               currentPageNo ===" + currentPageNo);
		loger.debug("               totalPageNo ===" + totalPageNo);
		loger.debug("               stateType ===" + stateType);
		loger.debug("               productnameafterfinal ===" + productnameafterfinal);
		loger.debug("               productnameafter ===" + productnameafter);

	}
	//填写结构的数据到表中间
	public static void writeDataToSheet(XSSFSheet sheetNew ,int allcount,int pageSize,HashMap allstandardPartMap,HashMap sumMap) throws WTException  
	{
		List attrList = new ArrayList();
		attrList.add(IBA_MATERIAL);
		attrList.add(IBA_DRAWING_NO);
		List likattrList = new ArrayList();
		likattrList.add(IBANAME_LINK_BZ);
		loger.debug("  writeDataToSheet    begin");

		int nextRowNo=2;//从第十行开始写数据
		int orderNo=1;
		Iterator keyset=allstandardPartMap.keySet().iterator();
		WTPart subpart=null;	
		List parentList=null;
		XSSFRow row=null;
		WTPartUsageLink usageLink =null;
		WTPart parentpart =null;
		String subpartname=null;
		String parenypartname=null;
		while(keyset.hasNext())
		{
			subpart=(WTPart) keyset.next();
			subpartname=subpart.getName();
			if(subpartname.indexOf("#")<=0)
			{
				throw new WTException(subpartname + "-----名称没有用#隔开，请检查数据！");
			}
			loger.debug("  writeDataToSheet    subpart==="+subpart.getDisplayIdentifier());
			//创建行和列
			//row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
			row=sheetNew.getRow(nextRowNo);
			loger.debug("  writeDataToSheet    nextRowNo==="+nextRowNo);
			row.getCell(0).setCellValue(orderNo);//第一列为序号
			
			
			
			
			int index=subpartname.indexOf(NAME_FLEX);
			String code=subpartname.substring(0,index);//代号
			
			row.getCell(2).setCellValue(code);//第3列为代号
			String nameAndType=subpartname.substring(index+1,subpartname.length());//名称及规格
			row.getCell(3).setCellValue(nameAndType);//第4列为名称及规格
			HashMap subIbaht = ReportUtil.getObjectValues(subpart, attrList);
			String material = (String) subIbaht.get(IBA_MATERIAL);//材料
			String drawing = (String) subIbaht.get(IBA_DRAWING_NO);// 幅面
			System.out.println(drawing);
			if (drawing == null)
				drawing = "";
			row.getCell(1).setCellValue(drawing);// 幅面
			
			row.getCell(5).setCellValue(material);//第5列为材料
			Double tempsum=(Double)sumMap.get(subpart.getNumber());
			if(tempsum==null)
				row.getCell(8).setCellValue(0);//第8列为总数量
			else
			    row.getCell(8).setCellValue(tempsum.doubleValue());//第8列为总数量
			parentList=(List) allstandardPartMap.get(subpart);
			loger.debug("  writeDataToSheet    parentList==="+parentList);

			//有归属父件
			if(parentList!=null)
			{
				int parentsize=parentList.size();
				for(int i=0;i<parentsize;i++)
				{
					usageLink = (WTPartUsageLink) parentList.get(i);
					loger.debug("  writeDataToSheet  parent  nextRowNo==="+nextRowNo);
					if(i!=0)
					{
						//row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
						row=sheetNew.getRow(nextRowNo);
						row.getCell(0).setCellValue(orderNo);//第一列为序号
					}
					HashMap linkIbaht = ReportUtil.getObjectValues(usageLink, likattrList);
					Quantity qt=usageLink.getQuantity();
					parentpart=(WTPart) usageLink.getRoleAObject();
					parenypartname=parentpart.getName();
					if(parenypartname.indexOf("#")<=0)
					{
						throw new WTException(parenypartname + "-----名称没有用#隔开，请检查数据！");
					}
					int indexp=parenypartname.indexOf(NAME_FLEX);
					row.getCell(6).setCellValue(parenypartname.substring(0,indexp));//第6列为父件物料名称#前面
					row.getCell(7).setCellValue(qt.getAmount());//第7列为BOM结构中数量
					row.getCell(9).setCellValue(qt.getUnit().getDisplay().replaceAll("每", ""));//第9列为BOM结构中的单位
					row.getCell(10).setCellValue((String)linkIbaht.get(IBANAME_LINK_BZ));//第10列为BOM结构中的备注
				/*	String Remark=(String) linkIbaht.get(IBANAME_LINK_BZ);			
					if(Remark!=null&&Remark.equals("借")){
						Remark= "";
						row.getCell(1).setCellValue(Remark);
					}	*/
					
					if(orderNo%PAGE_SIZE==0){
						System.out.println("zyj--ts==nextRowNo+9");
						nextRowNo = nextRowNo + 10;
					}else{
						System.out.println("zyj--ts==nextRowNo:"+nextRowNo);
						nextRowNo = nextRowNo + 1;
					}
					orderNo=orderNo+1;
				}
			}
			//无归属父件,则直接进入下一行
			else
			{
				if(orderNo%PAGE_SIZE==0){
					System.out.println("zyj--ts==nextRowNo+9");
					nextRowNo = nextRowNo + 10;
				}else{
					System.out.println("zyj--ts==nextRowNo:"+nextRowNo);
					nextRowNo = nextRowNo + 1;
				}
				orderNo=orderNo+1;
			}
		}
		loger.debug("  writeDataToSheet    end");

	}
	
	public static void mergerDataForReport(XSSFSheet toSheet, int beginRowNo)
	{
		for(int i=beginRowNo+9;i<beginRowNo+EACH_PAGE_SIZE;i++)
		{
			CellRangeAddress region0=new CellRangeAddress(i,i,3,4); 
			toSheet.addMergedRegion(region0);
		}
	}
	
	
	
	
}
