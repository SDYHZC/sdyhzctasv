package ext.tan.partreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ext.util.IBAUtility;

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
import wt.vc.config.ConfigHelper;
import wt.vc.config.ConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

public class GroupingDetailSpecialSummary implements RemoteAccess {

	private static String CLASSNAME = GroupingDetailSpecialSummary.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	private static final String IBA_MATERIAL = "CMAT";
	private static final String IBA_PART_TYPE = "LJLB";
	private static final String IBANAME_LINK_BZ = "BZ";
	private static final String STANDARD_PART = "标准件";
	private static final String BUY_PART = "外购件";
	private static final String MAKE_PART = "自制件";
	private static final int PAGE_SIZE = 30;
	private static final int EACH_PAGE_SIZE = 39;
	private static final String NAME_FLEX = "#";
	private static final String STATE_FLEX = "_";
	private static final String IBA_SPEC = "CSPE";
	private static final String IBA_MARK = "CGRA";
	private static final String IBA_DRAWING_NO = "TUFU";
	public static XSSFWorkbook getAllInforOfPartStruct(WTPart toppart,WTPart rootpart) throws WTException
	{
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		XSSFWorkbook wb = null;
		try
		{
			SessionServerHelper.manager.setAccessEnforced(false);
			loger.debug("  toppart ===" + toppart.getDisplayIdentifier());
			WTPartUsageLink usageLink = null;
            WTPart detailpart=toppart;
            String state = toppart.getLifeCycleState().getDisplay();
            if(state.equals("正在审阅"))
            {
				throw new WTException(toppart.getDisplayIdentifier() + "当前出于正在审阅状态，不能进行部件导出！");
			}
            
		/*	if (toppart.getName().indexOf("明细表") <= 0)
			{
				throw new WTException(toppart.getDisplayIdentifier() + "不属于明细表，请重新选择部件进行导出！");
			}*/
            
			else
			{

				// 取明细表部件的第一层子件作为toppart
				ConfigSpec configSpec = ConfigHelper.service.getConfigSpecFor(toppart);
				QueryResult qr = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(toppart, configSpec);
				if (qr == null || !qr.hasMoreElements() || qr.size() <= 0)
				{
					throw new WTException(toppart.getDisplayIdentifier() + "不属于总成明细，无子件，请检查数据！");
				}
				else
				{
					Persistable apersistable[] = (Persistable[]) qr.nextElement();
					if (apersistable[1] instanceof WTPart)
					{
						detailpart = (WTPart) apersistable[1];
					}
					usageLink = (WTPartUsageLink) apersistable[0];
				}
			}

			View viewObj = ViewHelper.service.getView(toppart.getViewName());
			WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
			LinkedHashMap singleMap = new LinkedHashMap();
			LinkedHashMap alltagMap = new LinkedHashMap();
			LinkedHashMap alltagTDMap = new LinkedHashMap();
			double sum = 1;
			HashMap sumMap = new HashMap();
			List topList = new ArrayList();
			
			singleMap.put(toppart, topList);
			getPartSubStructure(toppart, config, singleMap, alltagMap,sumMap,sum);
			String topPartName = toppart.getName();
			int index = topPartName.indexOf(NAME_FLEX);
			String code = topPartName.substring(0, index);// 代号
			int codeIndex = code.indexOf("-");
			String gBefor = code.substring(0,codeIndex);
			String gAfter = code.substring(codeIndex + 1, code.length());
			String ottf = gAfter.substring(0,4);
			alltagTDMap.put(ottf, singleMap);
			System.out.println("zyj--ts--singleMap:"+singleMap+" == size："+singleMap.size());
			Iterator keyset = alltagMap.keySet().iterator();
			while (keyset.hasNext())
			{
				String tag=(String) keyset.next();
				LinkedHashMap map = (LinkedHashMap) alltagMap.get(tag);
				alltagTDMap.put(tag, map);
			}
			wb = writeToExcel(toppart, singleMap, sumMap,rootpart, detailpart,alltagTDMap);
		}
		catch (WTException e)
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
//hmap1:标准件    hmap2：外购   hmap3：自制  currentSum：上级数量（父级数量）
	private static void getPartSubStructure(WTPart toppart, ConfigSpec configSpec, LinkedHashMap hmap1, LinkedHashMap hmap2, LinkedHashMap hmap4,HashMap sumMap, List attrList, double currentSum)
			throws WTException
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
				System.out.println("zyj--ts --subPart:"+subpart.getName());
				WTPartUsageLink usageLink = (WTPartUsageLink) apersistable[0];
				// 获取当前的link数量，递归跟上层数量惊喜相乘
				double amount = usageLink.getQuantity().getAmount();
				double newsum = currentSum * amount;
				HashMap subIbaht = ReportUtil.getObjectValues(subpart, attrList);
				// 过滤出标准件
				String partType = (String) subIbaht.get(IBA_PART_TYPE);
				IBAUtility ibaUtil = new IBAUtility(usageLink);
				String bz  = ibaUtil.getIBAValue("BZ");
				if (partType != null && (partType.equals(STANDARD_PART) || (partType.equals(BUY_PART) || partType.equals(MAKE_PART))))
				{
					if (partType.equals(STANDARD_PART))
					{//标准件
						System.out.println("zyj--ts--STANDARD_PART:"+subpart.getName());
						loger.debug("  subpart ===" + subpart.getDisplayIdentifier() + " is " + STANDARD_PART + " and parent part is " + toppart.getDisplayIdentifier());
						// 获取已经记录的父件link
						List parentlist = (List) hmap1.get(subpart);
						loger.debug(" old hmap1 parentlist ===" + parentlist);
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
						loger.debug(" new hmap1 parentlist ===" + parentlist);
						hmap1.put(subpart, parentlist);
					}
					
					else if (partType.equals(BUY_PART))
					{ //外购件
						System.out.println("zyj--ts--BUY_PART:"+subpart.getName());
						loger.debug("  subpart ===" + subpart.getDisplayIdentifier() + " is " + BUY_PART + " and parent part is " + toppart.getDisplayIdentifier());
						// 获取已经记录的父件link
						List parentlist = (List) hmap2.get(subpart);
						loger.debug(" old hmap2 parentlist ===" + parentlist);
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
						loger.debug(" new hmap2 parentlist ===" + parentlist);
						hmap2.put(subpart, parentlist);
					}
					else{
						// 获取已经记录的父件link
						System.out.println("zyj--ts--qita:"+subpart.getName());
						List parentlist = (List) hmap4.get(subpart);
						loger.debug(" old hmap3 parentlist ===" + parentlist);
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
						loger.debug(" new hmap3 parentlist ===" + parentlist);
						hmap4.put(subpart, parentlist);
					}
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
				}else{
					System.out.println("zyj--ts--jie 2:"+subpart.getName());
					if (bz!=null && !"".equals(bz) && bz.contains("借"))
					{ //借用件
						loger.debug("  subpart ===" + subpart.getDisplayIdentifier() + " is 借用件"  + " and parent part is " + toppart.getDisplayIdentifier());
						// 获取已经记录的父件link
						List parentlist = (List) hmap4.get(subpart);
						loger.debug(" old hmap5 parentlist ===" + parentlist);
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
						loger.debug(" new hmap2 parentlist ===" + parentlist);
						hmap4.put(subpart, parentlist);
					}
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
				getPartSubStructure(subpart, configSpec, hmap1,hmap2, hmap4, sumMap, attrList, newsum);
			}	
		}
	}
	/*  根据数据类型分sheet页
	 *  hmap1:图号-前的字符与总成不同，单个零件没有结构   
	 *  hmap2:根据图号-后面的前4位进行分组，分别组成不同的sheet页
	 *  currentSum：上级数量（父级数量）
	 *  Map类型的：key-前四位标识符     value-subpart
	 *  Map类型的map：key - subpart vallue - usagelink
	 */
		private static void getPartSubStructure(WTPart toppart, ConfigSpec configSpec, LinkedHashMap hmap1, LinkedHashMap hmap2,HashMap sumMap,double currentSum)
				throws WTException
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
					// 获取当前的link数量，递归跟上层数量惊喜相乘
					double amount = usageLink.getQuantity().getAmount();
					double newsum = currentSum * amount;
					String topPartName = toppart.getName();
					int topIndex = topPartName.indexOf(NAME_FLEX);
					String topCode = topPartName.substring(0, topIndex);// 代号
					int topcodeIndex = topCode.indexOf("-");
					String topgBefor = topCode.substring(0,topcodeIndex);
					String topgAfter = topCode.substring(topcodeIndex + 1, topCode.length());
					
					
					String subPartName = subpart.getName();
					int index = subPartName.indexOf(NAME_FLEX);
					String code = subPartName.substring(0, index);// 代号
					int codeIndex = code.indexOf("-");
					String gBefor = code.substring(0,codeIndex);
					String gAfter = code.substring(codeIndex + 1, code.length());
					QueryResult q = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(subpart, configSpec);
					String ottf = gAfter.substring(0,4);
					if(!gBefor.equals(topgBefor) || (q == null || !q.hasMoreElements() || q.size() <= 0)){
						if(!gBefor.equals(topgBefor) && (q == null || !q.hasMoreElements() || q.size() <= 0)){
							List parentlist = (List) hmap1.get(subpart);
							loger.debug(" old hmap1 parentlist ===" + parentlist);
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
							loger.debug(" new hmap1 parentlist ===" + parentlist);
							hmap1.put(subpart, parentlist);
						}else if(!gBefor.equals(topgBefor) && (q != null && q.size() >0)){
							
						}else if(gBefor.equals(topgBefor) && (q == null || !q.hasMoreElements() || q.size() <= 0)){
							List parentlist = (List) hmap1.get(subpart);
							loger.debug(" old hmap1 parentlist ===" + parentlist);
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
							loger.debug(" new hmap1 parentlist ===" + parentlist);
							hmap1.put(subpart, parentlist);
						}
						
					}else{
						System.out.println("zyj--ts--hmap2:"+hmap2);
						LinkedHashMap partMap = (LinkedHashMap) hmap2.get(ottf);
						List parentlist = null;
						loger.debug(" partlist ===" + partMap);
						if (partMap == null)
						{
							// 通过前4位标识取部件编号的map
							partMap = new LinkedHashMap();
							parentlist = new ArrayList();
							parentlist.add(usageLink);
							partMap.put(subpart, parentlist);
						}
						else
						{
							// 如果已经记录，则不再重复记录。如果没记录，则记录到map中
							if (!partMap.containsKey(subpart)){
								parentlist= new ArrayList();;
								parentlist.add(usageLink);
								partMap.put(subpart, parentlist);
							}
						}
						loger.debug(" new hmap2 partMap ===" + partMap);
						hmap2.put(ottf, partMap);
					}
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
			}
		}
	public static XSSFWorkbook writeToExcel(WTPart toppart, LinkedHashMap allstandardPartMap, HashMap sumMap,WTPart rootpart, WTPart detailpart,LinkedHashMap tagMap) throws WTException
	{

		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		try
		{
			// 获取模板
			String templatePath = ReportUtil.getTemplatePath() + File.separator + "GroupingDetailSpecReport.xlsx";
			loger.debug("  templatePath ===" + templatePath);

			fis = new FileInputStream(templatePath);
			// 生成要导出的Excel
			wb = new XSSFWorkbook(fis);
			
			Iterator keyset = tagMap.keySet().iterator();
			List stList = new ArrayList();
			String stName="";
			while (keyset.hasNext())
			{
			    //sheet的名称
				stName = (String) keyset.next();
				stList.add(stName);
			}
			XSSFSheet sheetT = null;
			System.out.println("zyj--ts--stList:"+stList.size());
			for(int n = 0;n<stList.size();n++){
				LinkedHashMap standardPartMap = new LinkedHashMap();
				//LinkedHashMap borrowPartMap = new LinkedHashMap();
				LinkedHashMap outsourcingPartMap = new LinkedHashMap();
				//LinkedHashMap makingPartMap = new LinkedHashMap();
				LinkedHashMap qtPartMap = new LinkedHashMap();
				if(n ==0){
					sheetT = wb.getSheetAt(0);
					Iterator key = allstandardPartMap.keySet().iterator();
					while(key.hasNext()){
						WTPart pt = (WTPart) key.next();
						System.out.println("zyj--ts--pt:"+pt.getName());
						IBAUtility iba = new IBAUtility(pt);
						String type = iba.getIBAValue(IBA_PART_TYPE);
						List ls = (List) allstandardPartMap.get(pt);
						WTPartUsageLink lk = null;
						IBAUtility ibaUtil =null;
						String bz = "";
						if(ls!=null && ls.size()>0){
							for(int k=0;k<ls.size();k++){
								lk = (WTPartUsageLink) ls.get(k);
							}
							if(lk!=null){
								ibaUtil  = new IBAUtility(lk);
							}
						}else{
							ibaUtil = null;
						}
						if(ibaUtil!=null){
							bz=ibaUtil.getIBAValue("BZ");
						}
						System.out.println("zyj--ts--bz:"+bz);
						if (type != null && (type.equals(STANDARD_PART))){
							standardPartMap.put(pt, allstandardPartMap.get(pt));
//						}else if (bz!=null && !"".equals(bz) && "借".equals(bz)){
//							borrowPartMap.put(pt, allstandardPartMap.get(pt));
						}else if (type != null && (type.equals(BUY_PART))){
							outsourcingPartMap.put(pt, allstandardPartMap.get(pt));
//						}else if ((type != null && (type.equals(MAKE_PART))) || pt.getNumber().equals(toppart.getNumber())){
//							System.out.println("zyj--ts--toppart come here "+pt.getName());
//							makingPartMap.put(pt, allstandardPartMap.get(pt));
						}else{
							qtPartMap.put(pt, allstandardPartMap.get(pt));
						}
//						WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
//						List attrList = new ArrayList();
//						attrList.add(IBA_PART_TYPE);
//						List uselist = (List) allstandardPartMap.get(pt);
//						WTPartUsageLink usageLink=null;
//						for(int m=0;m<uselist.size();m++){
//							usageLink = (WTPartUsageLink) uselist.get(m);
//						}
//						double sum = usageLink.getQuantity().getAmount();
						//getPartSubStructure(pt, config, standardPartMap,borrowPartMap, outsourcingPartMap, makingPartMap,qtPartMap, sumMap, attrList, sum);
					}	
				}else{
					sheetT = wb.createSheet(stList.get(n)+"MX");
					System.out.println("zyj--ts--sheetT:"+stList.get(n));
					ExcelUtil.copySheet(wb,wb.getSheetAt(0),sheetT,true);
					String shtName = (String) stList.get(n);
					LinkedHashMap partMap = (LinkedHashMap) tagMap.get(shtName);
					Iterator key = partMap.keySet().iterator();
					while(key.hasNext()){
						WTPart pt = (WTPart) key.next();
						System.out.println("zyj--ts--sheetT  PART:"+stList.get(n) +"  ==  "+pt.getName());
						IBAUtility iba = new IBAUtility(pt);
						String type = (String) iba.getIBAValue(IBA_PART_TYPE);
						List ls = (List) partMap.get(pt);
						WTPartUsageLink lk = null;
						IBAUtility ibaUtil =null;
						String bz = "";
						if(ls!=null && ls.size()>0){
							for(int k=0;k<ls.size();k++){
								lk = (WTPartUsageLink) ls.get(k);
							}
							if(lk!=null){
								ibaUtil  = new IBAUtility(lk);
							}
						}
						if(ibaUtil!=null){
							bz=ibaUtil.getIBAValue("BZ");
						}
						if (type != null && (type.equals(STANDARD_PART))){
							standardPartMap.put(pt, partMap.get(pt));
//						}else if (bz!=null && !"".equals(bz) && bz.contains("借")){
//							borrowPartMap.put(pt, partMap.get(pt));
						}else if (type != null && (type.equals(BUY_PART))){
							outsourcingPartMap.put(pt, partMap.get(pt));
//						}else if (type != null && (type.equals(MAKE_PART))){
//							makingPartMap.put(pt, partMap.get(pt));
						}else{
							qtPartMap.put(pt, partMap.get(pt));
						}
						WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
						List attrList = new ArrayList();
						attrList.add(IBA_PART_TYPE);
						List uselist = (List) partMap.get(pt);
						System.out.println("zyj--ts--uselist:"+uselist);
						WTPartUsageLink usageLink=null;
						if(uselist!=null){
							for(int m=0;m<uselist.size();m++){
								usageLink = (WTPartUsageLink) uselist.get(m);
							}
							double sum = usageLink.getQuantity().getAmount();
							System.out.println("zyj--st--come to xunhuanzijian");
							getPartSubStructure(pt, config, standardPartMap, outsourcingPartMap, qtPartMap,sumMap, attrList, sum);
						}
						
					}
				}
				LinkedHashMap linkmap = new LinkedHashMap();
				linkmap.putAll(qtPartMap);
				linkmap.putAll(outsourcingPartMap);
				linkmap.putAll(standardPartMap);
				
				
				int datacount = ReportUtil.getAllCount(linkmap)-1;
				loger.debug("  datacount ===" + datacount);

				// 计算需要分多少页
				int pageSize = 1;
				if ((datacount % PAGE_SIZE) == 0)
					pageSize = datacount / PAGE_SIZE;
				else
					pageSize = datacount / PAGE_SIZE + 1;
				loger.debug("  pageSize ===" + pageSize);

				// 往模板sheet插入抬头的数据
				writeTitleToSheet(toppart, sheetT, pageSize, 1, 0,detailpart);

				int lastrowNo = pageSize * EACH_PAGE_SIZE;
				// 按分页的情况
				for (int i = 0; i < lastrowNo; i++)
				{
					if (i % EACH_PAGE_SIZE == 0 && i != 0)
					{
						ExcelUtil.createAllPageRows(sheetT, i, EACH_PAGE_SIZE);
						// 合并表头单元格
						ExcelUtil.mergerTitleForReport(sheetT, i);
						// 合并要填写数据的单元格
						mergerDataForReport(sheetT, i);
						// 拷贝第一页的信息
						ExcelUtil.copyTemplateInforToSheet(wb, sheetT, i, i / EACH_PAGE_SIZE + 1, EACH_PAGE_SIZE, 10);
					}
				}
				// 先写sheet中要填写的结构信息
				writeDataToSheet(toppart,sheetT, datacount, pageSize, linkmap, sumMap,rootpart);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new WTException(e);
		}
		return wb;
	}

	public static void writeTitleToSheet(WTPart toppart, XSSFSheet sheetT, int totalPageNo, int currentPageNo, int targetBeginRowNo,WTPart detailpart)
	{
		String partname = detailpart.getName();
		// System.out.println("编号QQQ"+partname);
		// String productnamebefore=ReportUtil.getNameStr(partname,NAME_FLEX,
		// true);
		String productnameafter = ReportUtil.getNameStr(partname, NAME_FLEX, false);
		// String
		// productnameafterfinal=ReportUtil.getStrBeforeChs(productnameafter);
		// String productnameafter=partname.substring(0,partname.indexOf("#"));

		String state = toppart.getLifeCycleState().getDisplay();
		System.out.println("toppart0410"+toppart.getName());
		System.out.println("detailpart0410"+detailpart.getName());
		String stateType = ReportUtil.getNameStr(state, STATE_FLEX, false);
		String curretnPage = "第  " + currentPageNo + " 页";
		String totalPage = "共  " + totalPageNo + " 页";
		XSSFRow tempRow = sheetT.getRow(targetBeginRowNo);
		XSSFCell tempcell = tempRow.getCell(5);
		tempcell.setCellValue(productnameafter);
		productnameafter = ReportUtil.getNameStr(partname, NAME_FLEX, true);
		
  	    String productnamebefore=ReportUtil.getNameStr(toppart.getName(),NAME_FLEX,true);//yy
		tempcell = tempRow.getCell(7);
		
		tempcell.setCellValue(productnamebefore);
 
		tempRow = sheetT.getRow(3);
		tempcell = tempRow.getCell(7);
		tempcell.setCellValue("S");
		
		if(state.equals("返工")){
			tempcell.setCellValue("S");
		}else{
		tempcell.setCellValue(stateType);
		}
		
		tempRow = sheetT.getRow(2);
		tempcell = tempRow.getCell(10);
		tempcell.setCellValue(totalPage);
		tempRow = sheetT.getRow(3);
		tempcell = tempRow.getCell(10);
		tempcell.setCellValue(curretnPage);

	}

	// 填写结构的数据到表中间
	public static void writeDataToSheet(WTPart toppart,XSSFSheet sheetNew, int allcount, int pageSize, HashMap allstandardPartMap, HashMap sumMap,WTPart rootpart) throws WTException
	{
		List attrList = new ArrayList();
		attrList.add(IBA_MATERIAL);
		List likattrList = new ArrayList();
		likattrList.add(IBANAME_LINK_BZ);
		attrList.add(IBA_SPEC);
		attrList.add(IBA_MARK);
		attrList.add(IBA_DRAWING_NO);

		loger.debug("  writeDataToSheet    begin");

		int nextRowNo = 9;// 从第十行开始写数据
		int orderNo = 1;
		System.out.println("rootpart是"+rootpart.getName());
		Iterator keyset = allstandardPartMap.keySet().iterator();
//		System.out.println("zyj--ts--allstandardPartMap1.size:"+allstandardPartMap.size()+"===allstandardPartMap1:"+allstandardPartMap);
//		if(keyset.hasNext())
//		{
//			allstandardPartMap.remove((WTPart) keyset.next());
//		}
//		System.out.println("zyj--ts--allstandardPartMap2.size:"+allstandardPartMap.size()+"===allstandardPartMap2:"+allstandardPartMap);
		keyset = allstandardPartMap.keySet().iterator();
		WTPart subpart = null;
		
		List parentList = null;
		XSSFRow row = null;
		WTPartUsageLink usageLink = null;
		WTPart parentpart = null;
		String subpartname = null;
		String parenypartname = null;
		
		while (keyset.hasNext())
		{
			subpart = (WTPart) keyset.next();
			subpartname = subpart.getName();
			System.out.println("subpart是"+subpartname);
			if (subpartname.indexOf("#") <= 0)
			{
				throw new WTException(subpartname + "-----名称没有用#隔开，请检查数据！");
			}
			loger.debug("  writeDataToSheet    subpart===" + subpart.getDisplayIdentifier());
			// 创建行和列
			// row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
			row = sheetNew.getRow(nextRowNo);
			loger.debug(" writeDataToSheet    nextRowNo===" + nextRowNo);
			row.getCell(0).setCellValue(orderNo);// 第一列为序号
			int index = subpartname.indexOf(NAME_FLEX);
			String code = subpartname.substring(0, index);// 代号
			row.getCell(2).setCellValue(code);// 第3列为代号
			String nameAndType = subpartname.substring(index + 1, subpartname.length());// 名称及规格
			row.getCell(3).setCellValue(nameAndType);// 第4列为名称及规格
			HashMap subIbaht = ReportUtil.getObjectValues(subpart, attrList);

			String drawing = (String) subIbaht.get(IBA_DRAWING_NO);// 幅面
			System.out.println("幅面==="+drawing);
			String material = (String) subIbaht.get(IBA_MATERIAL);// 材料名称
			String spec = (String) subIbaht.get(IBA_SPEC);// 材料规格
			String materialmark = (String) subIbaht.get(IBA_MARK);// 材料牌号
			
			if (drawing == null)
				drawing = "";
			row.getCell(1).setCellValue(drawing);// 幅面

			if (material == null)
				material = "";
			if (spec == null)
				spec = "";
			if (materialmark == null)
				materialmark = "";
			if (material == "" && spec == "")
			{
				row.getCell(5).setCellValue(materialmark);// 第5列为材料、规格、牌号
			}
			else if (materialmark == "")
			{
				row.getCell(5).setCellValue(material + spec);// 第5列为材料、规格、牌号
			}
			else
			{
				row.getCell(5).setCellValue(material + spec + "\r\n" + materialmark);// 第5列为材料、规格、牌号
			}
			Double tempsum = (Double) sumMap.get(subpart.getNumber());
			if (tempsum == null)
				row.getCell(8).setCellValue(0);// 第8列为总数量
			else
				row.getCell(8).setCellValue(tempsum.doubleValue());// 第8列为总数量
			parentList = (List) allstandardPartMap.get(subpart);
			loger.debug("  writeDataToSheet    parentList===" + parentList);

			// 有归属父件
			if (parentList != null)
			{
				int parentsize = parentList.size();
				for (int i = 0; i < parentsize; i++)
				{
					usageLink = (WTPartUsageLink) parentList.get(i);
					loger.debug("  writeDataToSheet  parent  nextRowNo===" + nextRowNo);
					if (i != 0)
					{
						// row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
						row = sheetNew.getRow(nextRowNo);
						row.getCell(0).setCellValue(orderNo);// 第一列为序号
					}
					HashMap linkIbaht = ReportUtil.getObjectValues(usageLink, likattrList);
					Quantity qt = usageLink.getQuantity();
					parentpart = (WTPart) usageLink.getRoleAObject();
					//第一行为总成的信息，所属装配写的上层明细所归属的成品名称
				if(nextRowNo==9)
					{
						parenypartname = rootpart.getName();
					}
					else
					{   
				    	parenypartname = parentpart.getName();
					}
					if (parenypartname.indexOf("#") <= 0)
					{
						throw new WTException(parenypartname + "-----名称没有用#隔开，请检查数据！");
					}
					int indexp = parenypartname.indexOf(NAME_FLEX);
					row.getCell(6).setCellValue(parenypartname.substring(0, indexp));// 第6列为父件物料名称#前面
					row.getCell(7).setCellValue(qt.getAmount());// 第7列为BOM结构中数量
					row.getCell(9).setCellValue(qt.getUnit().getDisplay().replaceAll("每", ""));// 第9列为BOM结构中的单位
					row.getCell(10).setCellValue((String) linkIbaht.get(IBANAME_LINK_BZ));// 第10列为BOM结构中的备注
					String Remark=(String) linkIbaht.get(IBANAME_LINK_BZ);
			
					if(Remark!=null&&Remark.equals("借")){
						Remark= "";
						row.getCell(1).setCellValue(Remark);
					}
				
					nextRowNo = ExcelUtil.getNextRowNo(nextRowNo, PAGE_SIZE, 9);
					orderNo = orderNo + 1;
				}
			}
			// 无归属父件,则直接进入下一行
			else
			{
					orderNo = orderNo + 1;
					nextRowNo = ExcelUtil.getNextRowNo(nextRowNo, PAGE_SIZE, 9);
			}
		}
		loger.debug("  writeDataToSheet    end");
	//	sheetNew.removeRow(sheetNew.getRow(9));
	}
	

	public static void mergerDataForReport(XSSFSheet toSheet, int beginRowNo)
	{
		for (int i = beginRowNo + 9; i < beginRowNo + EACH_PAGE_SIZE; i++)
		{
			CellRangeAddress region0 = new CellRangeAddress(i, i, 3, 4);
			toSheet.addMergedRegion(region0);
		}
	}
	/**
	 * 使用 Map按key进行排序
	 * @param map
	 * @return
	 */
	public static LinkedHashMap sortMapByKey(LinkedHashMap hmap) {
		if (hmap == null || hmap.isEmpty()) {
			return null;
		}
		LinkedHashMap sortMap=null;
		Object[] key =  hmap.keySet().toArray();    
		Arrays.sort(key); 
		for(int i=0;i<key.length;i++){
			sortMap.put(key, hmap.get(key[i]));
		}
		return sortMap;
	}
}
