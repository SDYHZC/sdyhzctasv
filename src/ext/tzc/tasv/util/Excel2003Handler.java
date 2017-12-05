package ext.tzc.tasv.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Excel2003Handler
  implements CSCExcelHandler
{
  private HSSFWorkbook workbook = null;

  private File excelFile = null;

  private HSSFSheet currentSheet = null;

  public Excel2003Handler()
  {
  }

  public Excel2003Handler(String filePathName)
    throws IOException
  {
    POIFSFileSystem poiFileSystem = null;
    this.excelFile = new File(filePathName);

    if (this.excelFile.exists()) {
      poiFileSystem = new POIFSFileSystem(new FileInputStream(this.excelFile));
    }
    if (poiFileSystem != null) {
      this.workbook = new HSSFWorkbook(poiFileSystem);
      this.currentSheet = this.workbook.getSheetAt(0);
    }
  }

  public Excel2003Handler(File file)
    throws IOException
  {
    POIFSFileSystem poiFSFileSystem = null;
    this.excelFile = file;

    if (this.excelFile.exists()) {
      poiFSFileSystem = new POIFSFileSystem(new FileInputStream(this.excelFile));
    }
    if (poiFSFileSystem != null) {
      this.workbook = new HSSFWorkbook(poiFSFileSystem);
      this.currentSheet = this.workbook.getSheetAt(0);
    }
  }

  public Excel2003Handler(InputStream inputStream)
    throws IOException
  {
    if (inputStream != null) {
      POIFSFileSystem poiFISFileSystem = new POIFSFileSystem(inputStream);
      this.workbook = new HSSFWorkbook(poiFISFileSystem);
      this.currentSheet = this.workbook.getSheetAt(0);
    }
  }

  public boolean exists()
  {
    return this.workbook != null;
  }

  public String getParent()
  {
    return this.excelFile.getParent();
  }

  public String getFileName()
  {
    return this.excelFile.getName();
  }

  public boolean createNewFile()
    throws IOException
  {
    this.workbook = new HSSFWorkbook();

    if (this.excelFile != null)
    {
      boolean dirResult = this.excelFile.getParentFile().mkdirs();

      if ((dirResult) || (this.excelFile.getParentFile().exists())) {
        FileOutputStream fileOut = new FileOutputStream(getParent() + File.separator + getFileName());
        this.workbook.write(fileOut);
        fileOut.close();
      }
      else {
        return false;
      }
    }

    return true;
  }

  public boolean createNewFile(File newfile)
    throws IOException
  {
    this.excelFile = newfile;
    return createNewFile();
  }

  public boolean createNewFile(String fileName)
    throws IOException
  {
    this.excelFile = new File(fileName);
    return createNewFile();
  }

  public boolean createNewSheet(String sheetName)
    throws IOException
  {
    if (this.workbook == null) {
      return false;
    }
    HSSFSheet sheet = this.workbook.createSheet(sheetName);

    this.currentSheet = sheet;

    return sheet != null;
  }

  public void switchCurrentSheet(String sheetName)
  {
    if (this.workbook == null) {
      return;
    }
    HSSFSheet sheet = this.workbook.getSheet(sheetName);

    if (sheet != null)
      this.currentSheet = sheet;
  }

  public void switchCurrentSheet(int sheetId)
  {
    if (this.workbook == null) {
      return;
    }
    HSSFSheet sheet = this.workbook.getSheetAt(sheetId);

    if (sheet != null)
      this.currentSheet = sheet;
  }

  public int getSheetRowCount()
  {
    return this.currentSheet.getPhysicalNumberOfRows();
  }

  public boolean isExistSheet(String sheetName)
  {
    if (this.workbook == null) {
      return false;
    }
    HSSFSheet sheet = this.workbook.getSheet(sheetName);

    return sheet != null;
  }

  public boolean setStringValue(int row, int col, String value)
  {
    if (this.workbook == null) {
      return false;
    }
    HSSFRow hssfRow = this.currentSheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = this.currentSheet.createRow(row);
    }
    HSSFCell cell = hssfRow.createCell((short)col);

    cell.setCellType(1);
    cell.setCellValue(value);

    return true;
  }

  public boolean setNumericValue(int row, int col, double value)
  {
    if (this.workbook == null) {
      return false;
    }

    HSSFRow hssfRow = this.currentSheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = this.currentSheet.createRow(row);
    }

    HSSFCell cell = hssfRow.createCell((short)col);

    cell.setCellType(0);
    cell.setCellValue(value);

    return true;
  }

  public boolean setDateValue(int row, int col, Date value, String fomat)
  {
    if (this.workbook == null) {
      return false;
    }

    HSSFRow hssfRow = this.currentSheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = this.currentSheet.createRow(row);
    }

    HSSFCell cell = hssfRow.createCell((short)col);

    cell.setCellType(1);

    SimpleDateFormat dateFormat = new SimpleDateFormat(fomat);
    cell.setCellValue(dateFormat.format(value));

    return true;
  }

  public boolean setBooleanValue(int row, int col, boolean value)
  {
    if (this.workbook == null) {
      return false;
    }

    HSSFRow hssfRow = this.currentSheet.getRow(row);
    if (hssfRow == null) {
      hssfRow = this.currentSheet.createRow(row);
    }

    HSSFCell cell = hssfRow.createCell((short)col);

    cell.setCellType(4);
    cell.setCellValue(value);

    return true;
  }

  public boolean mergeCells(int rowFrom, int colFrom, int rowTo, int colTo)
  {
    if (this.workbook == null) {
      return false;
    }

    this.currentSheet.addMergedRegion(new Region(rowFrom, (short)colFrom, rowTo, (short)colTo));

    return true;
  }

  public String getValue(int row, int col)
  {
    if (this.workbook == null) {
      return "";
    }
    String value = "";

    HSSFRow hssfrow = this.currentSheet.getRow(row);
    if (hssfrow == null) {
      return "";
    }

    HSSFCell cell = hssfrow.getCell((short)col);
    if (cell == null) {
      return "";
    }

    int type = cell.getCellType();

    if (type == 1)
    {
      value = cell.getStringCellValue();
    } else if (HSSFDateUtil.isCellDateFormatted(cell))
    {
      double data = cell.getNumericCellValue();
      if (data == 0.0D) {
        value = "";
      } else {
        Date date = HSSFDateUtil.getJavaDate(data);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        value = dateFormat.format(date);
      }
    } else if (type == 0)
    {
      double dvalue = cell.getNumericCellValue();
      value = String.valueOf(dvalue);
    } else if (type == 4)
    {
      value = cell.getBooleanCellValue()+"";
    } else if (type == 3) {
      value = "";
    } else {
      value = cell.getStringCellValue();
    }

    if (value == null) {
      return "";
    }

    return value.trim();
  }

  public String getStringValue(int row, int col)
  {
    if (this.workbook == null) {
      return "";
    }
    String value = "";

    HSSFRow hssfrow = this.currentSheet.getRow(row);
    if (hssfrow == null) {
      return "";
    }

    HSSFCell cell = hssfrow.getCell((short)col);
    if (cell == null) {
      return "";
    }

    int type = cell.getCellType();
    if (type == 1)
    {
      value = cell.getStringCellValue();
    } else if (type == 0)
    {
      double dvalue = cell.getNumericCellValue();
      value = String.valueOf(dvalue);
      if (value.equals("0.0"))
        value = "0";
    }
    else {
      value = cell.getStringCellValue();
    }
    if (value == null) {
      return "";
    }
    return value.trim();
  }

  public boolean saveChanges()
    throws IOException
  {
    if ((this.workbook == null) || (this.excelFile == null)) {
      return false;
    }

    FileOutputStream fileOut = new FileOutputStream(getParent() + File.separator + getFileName());
    this.workbook.write(fileOut);
    fileOut.close();

    return true;
  }

  public void downloadExcel(HttpServletResponse response)
    throws IOException
  {
    OutputStream outstream = response.getOutputStream();
    this.workbook.write(outstream);
    outstream.flush();
    outstream.close();

    response.setStatus(200);
    response.flushBuffer();
  }
}