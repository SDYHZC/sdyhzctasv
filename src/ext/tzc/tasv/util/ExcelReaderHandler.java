package ext.tzc.tasv.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import wt.log4j.LogR;

public class ExcelReaderHandler
{
  private static final Logger LOGGER = LogR.getLogger(ExcelReaderHandler.class.getName());

  private Workbook wb = null;

  public ExcelReaderHandler(String path) {
    LOGGER.debug("new ExcelReader : path= " + path);
    try
    {
      InputStream is = new FileInputStream(path);
      this.wb = WorkbookFactory.create(is);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public ExcelReaderHandler(InputStream is) {
    LOGGER.debug("new ExcelReader : InputStream= " + is);
    try {
      this.wb = WorkbookFactory.create(is);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getCellValue(int sheetIndex, int rowIndex, int colIndex)
  {
    String cellValue = "";
    Cell cell = getCell(sheetIndex, rowIndex, colIndex);
    cellValue = getCellData(cell);
    return cellValue;
  }

  public String getCellValue(String sheetName, int rowIndex, int colIndex)
  {
    String cellValue = "";
    int sheetIndex = getSheetNumByName(sheetName);
    Cell cell = getCell(sheetIndex, rowIndex, colIndex);
    cellValue = getCellData(cell);
    return cellValue;
  }

  public int getSheetNum()
  {
    int sheetNum = 0;
    sheetNum = this.wb.getNumberOfSheets();
    return sheetNum;
  }

  public int getSheetNumByName(String sheetName)
  {
    Sheet sheet = this.wb.getSheet(sheetName);
    int sheetIndex = this.wb.getSheetIndex(sheet);
    return sheetIndex;
  }

  public String getSheetName()
  {
    return this.wb.getSheetName(0);
  }

  public int getLastRowIndex(int sheetIndex)
  {
    Sheet sheet = this.wb.getSheetAt(sheetIndex);
    return sheet.getLastRowNum();
  }

  public int getLastColumnIndex(int sheetIndex)
  {
    Sheet sheet = this.wb.getSheetAt(sheetIndex);
    Row row = sheet.getRow(0);
    if ((row != null) && (row.getLastCellNum() > 0)) {
      return row.getLastCellNum();
    }
    return 0;
  }

  public int getLastRowIndex(String sheetName)
  {
    Sheet sheet = this.wb.getSheet(sheetName);
    return sheet.getLastRowNum();
  }

  public int getLastColumnIndex(String sheetName)
  {
    Sheet sheet = this.wb.getSheet(sheetName);
    Row row = sheet.getRow(0);
    if ((row != null) && (row.getLastCellNum() > 0)) {
      return row.getLastCellNum();
    }
    return 0;
  }

  private Cell getCell(int sheetIndex, int rowIndex, int colIndex)
  {
    Cell cell = null;
    if (sheetIndex > getSheetNum())
      return cell;
    if (rowIndex > getLastRowIndex(sheetIndex))
      return cell;
    if (colIndex > getLastColumnIndex(sheetIndex)) {
      return cell;
    }
    Sheet sheet = this.wb.getSheetAt(sheetIndex);
    Row row = sheet.getRow(rowIndex);
    if (row == null) {
      return null;
    }
    cell = row.getCell(colIndex, Row.CREATE_NULL_AS_BLANK);
    return cell;
  }

  private String getCellData(Cell cell)
  {
    String str = "";
    if (cell == null) {
      return str;
    }
    switch (cell.getCellType()) {
    case 3:
      break;
    case 4:
      str = Boolean.toString(cell.getBooleanCellValue());
      break;
    case 0:
      if (DateUtil.isCellDateFormatted(cell)) {
        str = String.valueOf(cell.getDateCellValue());
      } else {
        cell.setCellType(1);
        String temp = cell.getStringCellValue();
        if (temp.indexOf(".") > -1)
          str = String.valueOf(new Double(temp)).trim();
        else {
          str = temp.trim();
        }
      }
      break;
    case 1:
      str = cell.getStringCellValue().trim();
      break;
    case 5:
      break;
    case 2:
      cell.setCellType(1);
      str = cell.getStringCellValue();
      if (str == null) break;
      str = str.replaceAll("#N/A", "").trim();

      break;
    }

    return str;
  }
}