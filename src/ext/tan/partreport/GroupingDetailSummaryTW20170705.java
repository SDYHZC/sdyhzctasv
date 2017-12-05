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
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import ext.util.IBAUtility;

import wt.enterprise.RevisionControlled;
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
import wt.pom.PersistenceException;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc.config.ConfigHelper;
import wt.vc.config.ConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;

public class GroupingDetailSummaryTW20170705 implements RemoteAccess {

	private static String CLASSNAME = GroupingDetailSummaryTW20170705.class.getName();
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
			LinkedHashMap allFirstPartMap = new LinkedHashMap();
			LinkedHashMap strMap = new LinkedHashMap();
			LinkedHashMap strMap1 = new LinkedHashMap();
			LinkedHashMap strMap2 = new LinkedHashMap();
			LinkedHashMap strMap3 = new LinkedHashMap();
			HashMap sumMap = new HashMap();
			LinkedHashMap allstandardPartMap = new LinkedHashMap();
			LinkedHashMap alloutsourcingPartMap = new LinkedHashMap();
			LinkedHashMap allmakingPartMap = new LinkedHashMap();
			LinkedHashMap allBorrowPartMap = new LinkedHashMap();
			List k1Lst = new ArrayList();
			
            if(state.equals("正在审阅"))
            {
				throw new WTException(toppart.getDisplayIdentifier() + "当前出于正在审阅状态，不能进行部件导出！");
			}
            
			if (toppart.getName().indexOf("MX") <= 0)
			{
				throw new WTException(toppart.getDisplayIdentifier() + "不属于三维分组明细表，请重新选择部件进行导出！");
			}
			else
			{

				// 取明细表部件的第一层子件作为toppart
				ConfigSpec configSpec = ConfigHelper.service.getConfigSpecFor(toppart);
				QueryResult qr = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(toppart, configSpec);
				if (qr == null || !qr.hasMoreElements() || qr.size() <= 0)
				{
					throw new WTException(toppart.getDisplayIdentifier() + "不属于三维分组明细，无子件，请检查数据！");
				}
				else
				{
					while (qr.hasMoreElements())
					{
						Persistable apersistable[] = (Persistable[]) qr.nextElement();
						if (apersistable[1] instanceof WTPart)
						{
							WTPart subpart = (WTPart) apersistable[1];
							ConfigSpec conspec = ConfigHelper.service.getConfigSpecFor(subpart);
							QueryResult query = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(subpart, conspec);
							if (query == null || !query.hasMoreElements() || query.size() <= 0)
							{
								throw new WTException(subpart.getDisplayIdentifier() + "不是分组总成，请检查数据！");
							}
							WTPartUsageLink useLink = (WTPartUsageLink) apersistable[0];
							// 获取当前的link数量，递归跟上层数量惊喜相乘
							double amount = useLink.getQuantity().getAmount();
							
							sumMap.put(subpart.getNumber(), Double.valueOf(amount));
							
							String topPartName = subpart.getName();
							int topIndex = topPartName.indexOf(NAME_FLEX);
							String topCode = topPartName.substring(0, topIndex);// 代号
							String keyStr = topCode;
							if(topCode!=null && !"".equals(topCode)){
								int inx= topCode.indexOf("-");
								if(inx>-1){
									keyStr = topCode.substring(inx+1,topCode.length());
									List l = new ArrayList();
									for(int a = 0 ; a < k1Lst.size(); a++){
										String k1Str = (String) k1Lst.get(a);
										if(k1Str.contains(keyStr)){
											l.add(k1Str);
										}
									}
									if(l.size()>0){
										int sz = l.size()+1;
										keyStr  = keyStr + sz+"";
									}
								}
							}
							k1Lst.add(keyStr);
							strMap.put(keyStr, subpart);
							detailpart = (WTPart) apersistable[1];
						}
					}
					
				}
			}

			View viewObj = ViewHelper.service.getView(toppart.getViewName());
			WTPartConfigSpec config = WTPartHelper.service.findWTPartConfigSpec();
			WTPartStandardConfigSpec standardConfig = config.getStandard();
			standardConfig.setView(viewObj);
			List attrList = new ArrayList();
			attrList.add(IBA_PART_TYPE);

			
			
			strMap=sortMapByKey(strMap);
			Iterator keyset = strMap.keySet().iterator();
			while (keyset.hasNext())
			{
				String tag= keyset.next()+"";
				WTPart subpart = (WTPart) strMap.get(tag);
				List parentlist = new  ArrayList();
				allmakingPartMap.put(subpart, parentlist);
				double sum = (Double) sumMap.get(subpart.getNumber());
				int flag = 1;
				getPartSubStructure(subpart, config, allstandardPartMap, alloutsourcingPartMap, allmakingPartMap,allBorrowPartMap, sumMap, attrList, sum,flag);
			}
			LinkedHashMap linkmap = new LinkedHashMap();
			LinkedHashMap allBorrowPartMap1 = new LinkedHashMap();
			LinkedHashMap allstandardPartMap1 = new LinkedHashMap();
			LinkedHashMap alloutsourcingPartMap1 = new LinkedHashMap();
			LinkedHashMap allmakingPartMap1 = new LinkedHashMap();
			LinkedHashMap makingPartMap = new LinkedHashMap();

			Iterator mKey = allmakingPartMap.keySet().iterator();
			LinkedHashMap numMap = new LinkedHashMap();
			List lst = new ArrayList();
			while(mKey.hasNext()){
				WTPart pt = (WTPart) mKey.next();
				System.out.println("zyj--ts--pt.name:"+pt.getName());
				List parentList = (List) allmakingPartMap.get(pt);
				makingPartMap.put(pt, parentList);
				for(int i = 0 ; i < parentList.size();i++){
					WTPartUsageLink uLink = (WTPartUsageLink) parentList.get(i);
					IBAUtility ibaUtil = new IBAUtility(uLink);
					String bz  = ibaUtil.getIBAValue("BZ");
					if(!"".equals(bz) && bz!=null && !"null".equals(bz)){
						if((bz.contains("与") && bz.contains("同图")) || (bz.contains("与") && bz.contains("对称"))){
							if(bz.contains("与") && bz.contains("同图")){
								String strNum = bz.substring(1,bz.indexOf("同图"));
								numMap.put(pt,strNum);
								lst.add(pt);
							}else if(bz.contains("与") && bz.contains("对称")){
								String strNum = bz.substring(1,bz.indexOf("对称"));
								numMap.put(pt,strNum);
								lst.add(pt);
							}
						}else{
							continue;
						}
					}
				}
			}
			for(int k = 0 ; k < lst.size(); k++){
				WTPart pt = (WTPart) lst.get(k);
				allmakingPartMap.remove(pt);
			}
			Iterator mKey1 = allmakingPartMap.keySet().iterator();
			while(mKey1.hasNext()){
				WTPart pt = (WTPart) mKey1.next();
				Iterator numKey = numMap.keySet().iterator();
				allmakingPartMap1.put(pt, makingPartMap.get(pt));
				while(numKey.hasNext()){
					WTPart yPt = (WTPart) numKey.next();
					String numStr  = (String) numMap.get(yPt);
					String topPartName = pt.getName();
					int topIndex = topPartName.indexOf(NAME_FLEX);
					String topCode = "";
					if(topIndex<0){
						topCode = topPartName;
					}else{
						topCode = topPartName.substring(0, topIndex);// 代号
					}
					if(numStr.equals(topCode)){
						System.out.println("zyj--ts--uslink:"+makingPartMap.get(yPt));
						allmakingPartMap1.put(yPt, makingPartMap.get(yPt));
					}
				}
			}
			
			
			
			linkmap.putAll(allmakingPartMap1);
			
			Iterator keyB = allBorrowPartMap.keySet().iterator();
			while(keyB.hasNext()){
				WTPart pt = (WTPart) keyB.next();
				String nm = pt.getName();
				strMap3.put(nm, pt);
			}
			
			if(strMap3.size()>0){
			strMap3=sortMapByKey(strMap3);
			Iterator k1 = strMap3.keySet().iterator();
			while(k1.hasNext()){
				String nm = (String) k1.next();
				WTPart pt = (WTPart) strMap3.get(nm);
				List parentlist = new ArrayList();
				parentlist = (List) allBorrowPartMap.get(pt);
				allBorrowPartMap1.put(pt, parentlist);
			}
			linkmap.putAll(allBorrowPartMap1);
			}
			
			
			
			Iterator key = alloutsourcingPartMap.keySet().iterator();
			while(key.hasNext()){
				WTPart pt = (WTPart) key.next();
				String nm = pt.getName();
				strMap1.put(nm, pt);
			}
			
			if(strMap1.size()>0){
			strMap1=sortMapByKey(strMap1);
			Iterator k1 = strMap1.keySet().iterator();
			while(k1.hasNext()){
				String nm = (String) k1.next();
				WTPart pt = (WTPart) strMap1.get(nm);
				List parentlist = new ArrayList();
				parentlist = (List) alloutsourcingPartMap.get(pt);
				alloutsourcingPartMap1.put(pt, parentlist);
			}
			linkmap.putAll(alloutsourcingPartMap1);
			}
			
			Iterator key1 = allstandardPartMap.keySet().iterator();
			while(key1.hasNext()){
				WTPart pt = (WTPart) key1.next();
				String nm = pt.getName();
				strMap2.put(nm, pt);
			}
			
			if(strMap2.size()>0){
				strMap2=sortMapByKey(strMap2);
				Iterator k2 = strMap2.keySet().iterator();
				while(k2.hasNext()){
					String nm = (String) k2.next();
					WTPart pt = (WTPart) strMap2.get(nm);
					List parentlist = new ArrayList();
					parentlist = (List) allstandardPartMap.get(pt);
					allstandardPartMap1.put(pt, parentlist);
				}
				linkmap.putAll(allstandardPartMap1);
			}
			
			loger.debug("  sumMap ===" + sumMap);
			loger.debug("  allstandardPartMap ===" + allstandardPartMap);
			wb = writeToExcel(toppart, linkmap, sumMap,rootpart, detailpart);
			loger.debug("  XSSFWorkbook ===" + wb);

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

	private static void getPartSubStructure(WTPart toppart, ConfigSpec configSpec, LinkedHashMap hmap1, LinkedHashMap hmap2, LinkedHashMap hmap3,LinkedHashMap hmap4, HashMap sumMap, List attrList, double currentSum,int flag)
			throws WTException
	{
		flag =flag+1;
		System.out.println("zyj--ts--flag:"+flag);
		QueryResult qr = WTPartHelper.service.getUsesWTPartsWithAllOccurrences(toppart, configSpec);
		if (qr == null || !qr.hasMoreElements() || qr.size() <= 0)
		{
			return;
		}
		LinkedHashMap objMap = new LinkedHashMap();
		LinkedHashMap strMap = new LinkedHashMap();
		List k1Lst = new ArrayList();
		while (qr.hasMoreElements()){
			Persistable apersistable[] = (Persistable[]) qr.nextElement();			
			if (apersistable[1] instanceof WTPart){
				WTPart chidPart = (WTPart)apersistable[1];
				WTPartUsageLink usageLink = (WTPartUsageLink) apersistable[0];
				objMap.put(chidPart, usageLink);
				String topPartName = chidPart.getName();
				int topIndex = topPartName.indexOf(NAME_FLEX);
				String topCode = "";
				if(topIndex<0){
					topCode = topPartName;
				}else{
					topCode = topPartName.substring(0, topIndex);// 代号
				}
				String keyStr = topCode;
				if(topCode!=null && !"".equals(topCode)){
					int inx= topCode.indexOf("-");
					if(inx>-1){
						keyStr = topCode.substring(inx+1,topCode.length());
						List l = new ArrayList();
						for(int a = 0 ; a < k1Lst.size(); a++){
							String k1Str = (String) k1Lst.get(a);
							if(k1Str.contains(keyStr)){
								l.add(k1Str);
							}
						}
						if(l.size()>0){
							int sz = l.size()+1;
							keyStr  = keyStr + sz+"";
						}
					}
				}
				k1Lst.add(keyStr);
				if(strMap.containsKey(keyStr)){
					keyStr = keyStr +"1";
				}
				
				strMap.put(keyStr, chidPart);
			}
		}
		System.out.println("zyj--ts--strMap.size:"+strMap.size());
		strMap=sortMapByKey(strMap);
		Iterator keyset = strMap.keySet().iterator();
		while (keyset.hasNext())
		{
			String tag= keyset.next()+"";
			WTPart subpart = (WTPart) strMap.get(tag);
			System.out.println("zyj--ts --subPart:"+subpart.getName());
			WTPartUsageLink usageLink = (WTPartUsageLink)objMap.get(subpart) ;
			// 获取当前的link数量，递归跟上层数量惊喜相乘
			double amount = usageLink.getQuantity().getAmount();
			double newsum = currentSum * amount;
			HashMap subIbaht = ReportUtil.getObjectValues(subpart, attrList);
			// 过滤出标准件
			String partType = (String) subIbaht.get(IBA_PART_TYPE);
			IBAUtility ibaUtil = new IBAUtility(usageLink);
			String bz  = ibaUtil.getIBAValue("BZ");
			boolean strFlag = false;
			Vector<WTPart> parentsVec = new Vector<WTPart>();
			Vector<WTPart> vec = getParentParts(subpart,parentsVec);
			for(int k = 0 ; k < vec.size(); k++){
				WTPart pt = vec.get(k);
				if(hmap4.containsKey(pt)){
					strFlag = true;
					break;
				}
			}
			System.out.println("zyj--ts--strFlag:"+strFlag);
			if (partType != null && partType.equals(STANDARD_PART))
			{
				System.out.println("zyj--ts--partL:"+subpart.getName()+" ==STANDARD_PART");
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
			else if (partType != null && partType.equals(BUY_PART))
			{
				System.out.println("zyj--ts--partL:"+subpart.getName()+" ==BUY_PART");
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
				hmap2.put(subpart, parentlist);
			}
			else 
			{	
				
				if(strFlag || (!"".equals(bz) && bz!=null && "借".equals(bz) && flag==2)){
					System.out.println("zyj--ts--partL:"+subpart.getName()+" ==else jie");
					// 获取已经记录的父件link
					List parentlist = (List) hmap4.get(subpart);
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
					hmap4.put(subpart, parentlist);
				}else{
					System.out.println("zyj--ts--partL:"+subpart.getName()+" ==else");
					// 获取已经记录的父件link
					List parentlist = (List) hmap3.get(subpart);
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
					hmap3.put(subpart, parentlist);
				}
			}
				// 统计总数量
				String subpartnumber = subpart.getNumber();
				Double oldSum = (Double) sumMap.get(subpartnumber);
				System.out.println("zyj--ts--oldSum:"+oldSum+"==subpart name："+subpart.getName());

				// 如果原来没有记录数量 ，则获取当前层级的数量作为总数量
				double allsum = 0;
				if (oldSum == null)
					allsum = newsum;
				// 如果原来有记录数量 ，则获取当前层级的数量*原来的数量 作为总数量
				else
					allsum = newsum + oldSum.doubleValue();
				System.out.println("zyj--subpart name:"+subpart.getName()+"===allsum:"+allsum);
				sumMap.put(subpartnumber, Double.valueOf(allsum));
				
				getPartSubStructure(subpart, configSpec, hmap1, hmap2, hmap3,hmap4, sumMap, attrList, newsum,flag);
			}	
		}

	public static XSSFWorkbook writeToExcel(WTPart toppart, LinkedHashMap allstandardPartMap, HashMap sumMap,WTPart rootpart, WTPart detailpart) throws WTException
	{

		FileInputStream fis = null;
		XSSFWorkbook wb = null;
		try
		{
			// 获取模板
			String templatePath = ReportUtil.getTemplatePath() + File.separator + "GroupingDetailReport.xlsx";
			loger.debug("  templatePath ===" + templatePath);

			fis = new FileInputStream(templatePath);
			// 生成要导出的Excel
			wb = new XSSFWorkbook(fis);
			XSSFSheet sheetT = wb.getSheetAt(0);
			int datacount = ReportUtil.getAllCount(allstandardPartMap)-1;
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
			writeDataToSheet(sheetT, datacount, pageSize, allstandardPartMap, sumMap,rootpart,toppart);
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

		loger.debug("  writeTitleToSheet ==========");
		loger.debug("               partname ===" + partname);
		loger.debug("               currentPageNo ===" + currentPageNo);
		loger.debug("               totalPageNo ===" + totalPageNo);
		loger.debug("               stateType ===" + stateType);
		loger.debug("               productnameafterfinal ===" + productnameafter);
		loger.debug("               productnameafter ===" + productnameafter);

	}

	// 填写结构的数据到表中间
	public static void writeDataToSheet(XSSFSheet sheetNew, int allcount, int pageSize, HashMap allstandardPartMap, HashMap sumMap,WTPart rootpart,WTPart toppart) throws WTException
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
				loger.debug("  writeDataToSheet    subpart===" + subpart.getDisplayIdentifier());
				// 创建行和列
				// row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
				row = sheetNew.getRow(nextRowNo);
				loger.debug(" writeDataToSheet    nextRowNo===" + nextRowNo);
				row.getCell(0).setCellValue(orderNo);// 第一列为序号
				row.getCell(2).setCellValue(subpartname);// 第3列为代号
				row.getCell(3).setCellValue("");// 第4列为名称及规格
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
					if(parentsize>0){
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
				}
				// 无归属父件,则直接进入下一行
				else
				{
					orderNo = orderNo + 1;
					nextRowNo = ExcelUtil.getNextRowNo(nextRowNo, PAGE_SIZE, 9);
				}
			}else{
			loger.debug("  writeDataToSheet    subpart===" + subpart.getDisplayIdentifier());
			// 创建行和列
			// row=ExcelUtil.createRow(sheetNew, nextRowNo, 11);
			row = sheetNew.getRow(nextRowNo);
			loger.debug(" writeDataToSheet    nextRowNo===" + nextRowNo);
			if(row!=null){
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
			System.out.println("zyj--ts--parentList:"+parentList);
			if (parentList != null)
			{
				int parentsize = parentList.size();
				if(parentsize>0){
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
				}else{
					row = sheetNew.getRow(nextRowNo);
					row.getCell(0).setCellValue(orderNo);// 第一列为序号
					if (code.indexOf("-") > 0)
					{
						int inx = code.indexOf("-");
						String gQ = code.substring(0,inx);//杠前面的字符串
						String gH = code.substring(inx+1, code.length());// 杠后面的字符串
						String fZp = "";
						String fZpStr = "";
						if(gH.startsWith("28")){
							if(gH.startsWith("2800")){
								fZp = gH.replaceAll(gH.substring(0,7),"0001");
								fZpStr = gQ +"-"+fZp;
							}else{
								fZp = gH.replaceAll(gH.substring(0,7),"2800001");
								fZpStr = gQ +"-"+fZp;
							}
						}else{
							fZp = gH.replaceAll(gH.substring(0,7),"0001");
							fZpStr = "TA"+gQ+"-"+fZp;
						}
						row.getCell(6).setCellValue(fZpStr);// 第6列为父件物料名称#前面
						row.getCell(7).setCellValue( (Double) sumMap.get(subpart.getNumber()));// 第7列为BOM结构中数量
						row.getCell(9).setCellValue("个");// 第9列为BOM结构中的单位
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
		LinkedHashMap sortMap=new LinkedHashMap();
		Object[] key =  hmap.keySet().toArray();    
		Arrays.sort(key); 
		for(int i=0;i<key.length;i++){
			sortMap.put(key[i], hmap.get(key[i]));
		}
		return sortMap;
	}
	/**
	 * 通過master取得最新版本版序
	 * 
	 * @function 通過Mastered來取得最新的版本版序對象
	 * @param master
	 * @return RevisionControlled
	 * @throws PersistenceException
	 * @throws WTException
	 */
	public static RevisionControlled getLatestObject(Mastered master) throws PersistenceException, WTException
	{
		if (master != null)
		{
			QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
			return getLatestObject(queryResult);
		}
		return null;
	}

	/**
	 * function 通過QueryResult對象,取得Object的最新版本版序對象.
	 * 
	 * @param queryresult
	 * @return
	 * @throws WTException
	 */
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
	/**
	 * 通過子料,取得所有父料(共用件的問題)
	 * 
	 * @param wtpart
	 *            -子料
	 * @return
	 * @throws WTException
	 */
	public static Vector<WTPart> getParentParts(WTPart wtpart, Vector<WTPart> vector) throws WTException
	{
		if (wtpart != null)
		{

			QueryResult qr1 = StructHelper.service.navigateUsedBy(wtpart.getMaster());
			while (qr1.hasMoreElements())
			{
				Object obj = qr1.nextElement();
				if (obj instanceof WTPart)
				{
					WTPart part = (WTPart) obj;
					part = (WTPart) getLatestObject(part.getMaster());
					if (!vector.contains(part))
					{
						vector.add(part);
					}
					getParentParts(part, vector);
				}
			}

		}
		return vector;
	}
}
