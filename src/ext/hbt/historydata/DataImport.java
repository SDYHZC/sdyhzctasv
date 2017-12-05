package ext.hbt.historydata;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import wt.log4j.LogR;
import wt.util.WTException;

/**
 * 判断excel模板，读取数据
 * @author Administrator
 *
 */
public class DataImport {

	private static String CLASSNAME = DataImport.class.getName();
	private static final Logger loger = LogR.getLogger(CLASSNAME);
	public static List startDataImport(Workbook workbook) throws WTException{
		List list = new ArrayList();
		Sheet sheet = workbook.getSheetAt(0);
		Row row0 = sheet.getRow(0);
		int lastrownum = sheet.getLastRowNum();
		loger.debug("最后一行为"+lastrownum);
		Row rowlast = sheet.getRow(lastrownum);
		
		if((row0 != null)&&(rowlast != null)){
			if(getCellValue(row0.getCell(0)).equals("编号")){
				if(sheet !=null){
				int rowNumber = sheet.getLastRowNum();
					for(int i=1;i<rowNumber+1;i++){	
						Row row = sheet.getRow(i);
						if(row !=null){
							String number = "";																		
							Cell cellnumber = row.getCell(0);																				
							if(cellnumber !=null){
								number = getCellValue(cellnumber);				
							}				
							ExcelData exceldata = new ExcelData(number);
							list.add(exceldata);
						}
					}
				}	
		}		else {
			throw new WTException("不是标准的数据导入模板，请重新选择文件！");
		}	
	}
		return list;
}	
	
	/**
	 * 转换excel单元格的值
	 * @param cell 获取到的单元格的值
	 * @return value 转换为字符串的单元格的值
	 * @throws WTException 异常
	 */
	public static String getCellValue(Cell cell) throws WTException {
		 String value = "";
		 try {
				if (null == cell) {
					return "";
				}
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					int b  = (int) cell.getNumericCellValue();
					double a =cell.getNumericCellValue();
					if(a == b){
						value = Integer.toString(b) ;
					}else{
						value = Double.toString(a) ;
					}
					break;
				case Cell.CELL_TYPE_STRING:
					value = cell.getStringCellValue();
					break;
				case Cell.CELL_TYPE_BLANK:
					value = "";
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					value = Boolean.toString(cell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_FORMULA:
					try {
						value = String.valueOf((int) cell.getNumericCellValue());
					} catch (Exception e) {
						value = cell.getStringCellValue();
					}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				throw new WTException(e.getMessage());
			}

			return value;
	 }
}
