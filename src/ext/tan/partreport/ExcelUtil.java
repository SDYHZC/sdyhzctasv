package ext.tan.partreport;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.log4j.LogR;


public final class ExcelUtil {
 
	private static String CLASSNAME = ExcelUtil.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	public static XSSFRow createRow(XSSFSheet sheet, int rowNo, int cellcount)
	{
		// 创建行
		XSSFRow row = sheet.createRow(rowNo);
		// 创建列
		for (int i = 0; i < cellcount; i++)
		{
			row.createCell(i);
		}
		CellRangeAddress region =new CellRangeAddress(rowNo,rowNo,3,4);
		sheet.addMergedRegion(region);
		return row;
	}

	/**
	 * Sheet复制
	 * 
	 * @param fromSheet
	 * @param toSheet
	 * @param copyValueFlag
	 *            为true代表复制内容
	 */

	public static void copySheet(XSSFWorkbook wb, XSSFSheet fromSheet, XSSFSheet toSheet, boolean copyValueFlag)
	{
		// 合并区域处理
		mergerRegion(fromSheet, toSheet);
		for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext();)
		{
			XSSFRow tmpRow = (XSSFRow) rowIt.next();
			XSSFRow newRow = toSheet.createRow(tmpRow.getRowNum());
			// 行复制
			copyRow(wb, tmpRow, newRow, copyValueFlag);
		}
	}

	/**
	 * 复制原有sheet的合并单元格到新创建的sheet
	 * 
	 * @param sheetCreat
	 *            新创建sheet
	 * @param sheet
	 *            原有的sheet
	 */
	public static void mergerRegion(XSSFSheet fromSheet, XSSFSheet toSheet)
	{
		int sheetMergerCount = fromSheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergerCount; i++)
		{
			CellRangeAddress mergedRegionAt = (CellRangeAddress) fromSheet.getMergedRegion(i);
			toSheet.addMergedRegion(mergedRegionAt);
		}
	}

	/**
	 * 行复制功能
	 * 
	 * @param fromRow
	 * @param toRow
	 */

	public static void copyRow(XSSFWorkbook wb, XSSFRow fromRow, XSSFRow toRow, boolean copyValueFlag)
	{
		toRow.setHeight(fromRow.getHeight());
		for (Iterator cellIt = fromRow.cellIterator(); cellIt.hasNext();)
		{
			XSSFCell tmpCell = (XSSFCell) cellIt.next();
			XSSFCell newCell = toRow.createCell(tmpCell.getColumnIndex());
			// System.out.println("1121...");
			copyCell(wb, tmpCell, newCell, copyValueFlag);
		}
	}

	/**
	 * 复制单元格
	 * 
	 * @param srcCell
	 * @param distCell
	 * @param copyValueFlag
	 *            true则连同cell的内容一起复制
	 */
	public static void copyCell(XSSFWorkbook wb, XSSFCell srcCell, XSSFCell distCell, boolean copyValueFlag)
	{
		XSSFCellStyle newstyle = wb.createCellStyle();
		if (srcCell.getCellStyle() != null)
		{
			newstyle.cloneStyleFrom(srcCell.getCellStyle());
			// copyCellStyle(srcCell.getCellStyle(), newstyle);
		}
		// 样式
		distCell.setCellStyle(newstyle);
		// 评论
		if (srcCell.getCellComment() != null)
		{
			distCell.setCellComment(srcCell.getCellComment());
		}
		// 不同数据类型处理
		int srcCellType = srcCell.getCellType();
		distCell.setCellType(srcCellType);
		if (copyValueFlag)
		{
			if (srcCellType == XSSFCell.CELL_TYPE_NUMERIC)
			{
				if (HSSFDateUtil.isCellDateFormatted(srcCell))
				{
					distCell.setCellValue(srcCell.getDateCellValue());
				}
				else
				{
					distCell.setCellValue(srcCell.getNumericCellValue());
				}
			}
			else if (srcCellType == XSSFCell.CELL_TYPE_STRING)
			{
				distCell.setCellValue(srcCell.getRichStringCellValue());
			}
			else if (srcCellType == XSSFCell.CELL_TYPE_BLANK)
			{
				// nothing21
			}
			else if (srcCellType == XSSFCell.CELL_TYPE_BOOLEAN)
			{
				distCell.setCellValue(srcCell.getBooleanCellValue());
			}
			else if (srcCellType == XSSFCell.CELL_TYPE_ERROR)
			{
				distCell.setCellErrorValue(srcCell.getErrorCellValue());
			}
			else if (srcCellType == XSSFCell.CELL_TYPE_FORMULA)
			{
				distCell.setCellFormula(srcCell.getCellFormula());
			}
			else
			{ // nothing29
			}
		}
	}

	/**
	 * 复制一个单元格样式到目的单元格样式
	 * 
	 * @param fromStyle
	 * @param toStyle
	 */
	public static void copyCellStyle(XSSFCellStyle fromStyle, XSSFCellStyle toStyle)
	{
		toStyle.setAlignment(fromStyle.getAlignment());
		// 边框和边框颜色
		toStyle.setBorderBottom(fromStyle.getBorderBottom());
		toStyle.setBorderLeft(fromStyle.getBorderLeft());
		toStyle.setBorderRight(fromStyle.getBorderRight());
		toStyle.setBorderTop(fromStyle.getBorderTop());
		toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
		toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
		toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
		toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());

		// 背景和前景
		toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
		toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());

		toStyle.setDataFormat(fromStyle.getDataFormat());
		toStyle.setFillPattern(fromStyle.getFillPattern());
		toStyle.setFont(fromStyle.getFont());
		toStyle.setHidden(fromStyle.getHidden());
		toStyle.setIndention(fromStyle.getIndention());// 首行缩进
		toStyle.setLocked(fromStyle.getLocked());
		toStyle.setRotation(fromStyle.getRotation());// 旋转
		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
		toStyle.setWrapText(fromStyle.getWrapText());
	}
	//合并表头的单元格（标准件汇总表）
	public static void mergerTitleForReport(XSSFSheet toSheet, int beginRowNo)
	{
		CellRangeAddress region1=new CellRangeAddress(32+beginRowNo,35+beginRowNo,6,7);//成品名称
		CellRangeAddress region2=new CellRangeAddress(32+beginRowNo,33+beginRowNo,8,11);//成品名称#后面、汉字之前部分-JW1-13-1
		CellRangeAddress region3=new CellRangeAddress(34+beginRowNo,34+beginRowNo,8,10);//图 样 标 记
		CellRangeAddress region4=new CellRangeAddress(36+beginRowNo,36+beginRowNo,1,2);//编制
		CellRangeAddress region5=new CellRangeAddress(37+beginRowNo,37+beginRowNo,1,2);//校对
		CellRangeAddress region6=new CellRangeAddress(38+beginRowNo,38+beginRowNo,1,2);//审核
		CellRangeAddress region7=new CellRangeAddress(36+beginRowNo,38+beginRowNo,6,7);//标准件汇总表
		CellRangeAddress region8=new CellRangeAddress(36+beginRowNo,38+beginRowNo,8,11);//泰安航天特种车有限公司
		CellRangeAddress region9=new CellRangeAddress(0+beginRowNo,1+beginRowNo,1,1);//序号
		CellRangeAddress region10=new CellRangeAddress(0+beginRowNo,1+beginRowNo,2,2);//幅画
		CellRangeAddress region11=new CellRangeAddress(0+beginRowNo,1+beginRowNo,3,3);//代号
		CellRangeAddress region12=new CellRangeAddress(0+beginRowNo,1+beginRowNo,4,5);//名 称 及 规 格
		CellRangeAddress region13=new CellRangeAddress(0+beginRowNo,1+beginRowNo,6,6);//材料
		CellRangeAddress region14=new CellRangeAddress(0+beginRowNo,0+beginRowNo,7,8);//所 属 装 配
		CellRangeAddress region15=new CellRangeAddress(0+beginRowNo,1+beginRowNo,9,9);//总数量
		CellRangeAddress region16=new CellRangeAddress(0+beginRowNo,1+beginRowNo,10,10);//单位
		CellRangeAddress region17=new CellRangeAddress(0+beginRowNo,1+beginRowNo,11,11);//备  注
		toSheet.addMergedRegion(region1);
		toSheet.addMergedRegion(region2);
		toSheet.addMergedRegion(region3);
		toSheet.addMergedRegion(region4);
		toSheet.addMergedRegion(region5);
		toSheet.addMergedRegion(region6);
		toSheet.addMergedRegion(region7);
		toSheet.addMergedRegion(region8);
		toSheet.addMergedRegion(region9);
		toSheet.addMergedRegion(region10);
		toSheet.addMergedRegion(region11);
		toSheet.addMergedRegion(region12);
		toSheet.addMergedRegion(region13);
		toSheet.addMergedRegion(region14);
		toSheet.addMergedRegion(region15);
		toSheet.addMergedRegion(region16);
		toSheet.addMergedRegion(region17);		
	}
	
	//合并表头的单元格（整 车 明 细 表）
	public static void mergerTitleForReport2(XSSFSheet toSheet, int beginRowNo)
	{
		CellRangeAddress region1=new CellRangeAddress(32+beginRowNo,35+beginRowNo,6,7);//成品名称
		CellRangeAddress region2=new CellRangeAddress(32+beginRowNo,33+beginRowNo,8,11);//成品名称#后面、汉字之前部分-JW1-13-1
		CellRangeAddress region3=new CellRangeAddress(34+beginRowNo,34+beginRowNo,8,10);//图 样 标 记
		CellRangeAddress region4=new CellRangeAddress(36+beginRowNo,36+beginRowNo,1,2);//编制
		CellRangeAddress region5=new CellRangeAddress(37+beginRowNo,37+beginRowNo,1,2);//校对
		CellRangeAddress region6=new CellRangeAddress(38+beginRowNo,38+beginRowNo,1,2);//审核
		CellRangeAddress region7=new CellRangeAddress(36+beginRowNo,38+beginRowNo,6,7);//标准件汇总表
		CellRangeAddress region8=new CellRangeAddress(36+beginRowNo,38+beginRowNo,8,11);//泰安航天特种车有限公司
		CellRangeAddress region9=new CellRangeAddress(0+beginRowNo,1+beginRowNo,1,2);//序号
		CellRangeAddress region10=new CellRangeAddress(0+beginRowNo,1+beginRowNo,3,4);//代号
		CellRangeAddress region11=new CellRangeAddress(0+beginRowNo,1+beginRowNo,5,7);//分 组（组）明 细 表 名 称
		CellRangeAddress region12=new CellRangeAddress(0+beginRowNo,1+beginRowNo,8,9);//页数
		CellRangeAddress region13=new CellRangeAddress(0+beginRowNo,1+beginRowNo,10,11);//备  注
		toSheet.addMergedRegion(region1);
		toSheet.addMergedRegion(region2);
		toSheet.addMergedRegion(region3);
		toSheet.addMergedRegion(region4);
		toSheet.addMergedRegion(region5);
		toSheet.addMergedRegion(region6);
		toSheet.addMergedRegion(region7);
		toSheet.addMergedRegion(region8);
		toSheet.addMergedRegion(region9);
		toSheet.addMergedRegion(region10);
		toSheet.addMergedRegion(region11);
		toSheet.addMergedRegion(region12);
		toSheet.addMergedRegion(region13);
	}
	//创建每一页的空白行
	public static void createAllPageRows(XSSFSheet toSheet, int beginRowNo,int eachpagesize)
	{
		XSSFRow tmpRow =null;
		for(int i=beginRowNo;i<beginRowNo+eachpagesize;i++)
		{
			  tmpRow = (XSSFRow) toSheet.createRow(i);
			  for(int k=1;k<12;k++)
				  tmpRow.createCell(k);
		}
	}
	
	public static void  copyTemplateInforToSheet(XSSFWorkbook wb,XSSFSheet fromsheet,int targetRow,int pageNo,int eachpagesize,int culNo)
	{
		loger.debug("  copyTemplateInforToSheet    targetRow==="+targetRow);
		loger.debug("  copyTemplateInforToSheet    pageNo==="+pageNo);
		loger.debug("  copyTemplateInforToSheet    eachpagesize==="+eachpagesize);

		//拷贝标题栏
		for(int i=0;i<eachpagesize;i++)
		{
			XSSFRow tmpRow = (XSSFRow) fromsheet.getRow(i);
			XSSFRow newRow =  (XSSFRow) fromsheet.getRow(i+targetRow);
			// 行复制
			ExcelUtil.copyRow(wb, tmpRow, newRow, true);
		}
		XSSFRow pagerow=fromsheet.getRow(targetRow+34); 
		XSSFCell pagecell=pagerow.getCell(culNo);
		pagecell.setCellValue("第  "+pageNo+" 页");
	}
	public static void  copyTemplateInforToSheet1(XSSFWorkbook wb,XSSFSheet fromsheet,int targetRow,int pageNo,int eachpagesize,int culNo)
	{
		loger.debug("  copyTemplateInforToSheet    targetRow==="+targetRow);
		loger.debug("  copyTemplateInforToSheet    pageNo==="+pageNo);
		loger.debug("  copyTemplateInforToSheet    eachpagesize==="+eachpagesize);

		//拷贝标题栏
		for(int i=0;i<eachpagesize;i++)
		{
			XSSFRow tmpRow = (XSSFRow) fromsheet.getRow(i);
			XSSFRow newRow =  (XSSFRow) fromsheet.getRow(i+targetRow);
			// 行复制
			ExcelUtil.copyRow(wb, tmpRow, newRow, true);
		}
		XSSFRow pagerow=fromsheet.getRow(targetRow+35); 
		XSSFCell pagecell=pagerow.getCell(culNo);
		pagecell.setCellValue("第  "+pageNo+" 页");
	}
	public static int getNextRowNo(int currentRow,int pagesize,int flexcount)  
	{
		//默认为下一行
		int nextRowNo = currentRow+1;
		//每页的总行数
		int eachpagesize=pagesize+flexcount;
		//能够整除，代表刚好是每页的最后一行，则下行的行数为当前行的数跳过间隔的行数
		if(nextRowNo%eachpagesize==0)
		{
			nextRowNo=currentRow+flexcount+1;
		}
		return nextRowNo;
	}
}