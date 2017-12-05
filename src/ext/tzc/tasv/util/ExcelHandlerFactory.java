package ext.tzc.tasv.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang.StringUtils;

public class ExcelHandlerFactory
{
  public static CSCExcelHandler getExcelHandler(String filePathName)
    throws Exception
  {
    if (filePathName == null) {
      return null;
    }

    return getExcelHandlerByInputStreamOrFilePath(null, filePathName);
  }

  public static CSCExcelHandler getExcelHandler(InputStream is, String fileName) throws Exception {
    if ((is == null) || (StringUtils.isBlank(fileName))) {
      return null;
    }

    return getExcelHandlerByInputStreamOrFilePath(is, fileName);
  }

  private static CSCExcelHandler getExcelHandlerByInputStreamOrFilePath(InputStream is, String fileNameOrFilePath) throws Exception {
    CSCExcelHandler handler = null;
    if (fileNameOrFilePath.toLowerCase().endsWith(".xls"))
      handler = get2003ExcelHandlerByInputStreamOrFilePath(is, fileNameOrFilePath);
    else if (fileNameOrFilePath.toLowerCase().endsWith(".xlsx"))
      handler = get2007ExcelHandlerByInputStreamOrFilePath(is, fileNameOrFilePath);
    else {
      try {
        handler = get2003ExcelHandlerByInputStreamOrFilePath(is, fileNameOrFilePath);
      } catch (Exception e) {
        try {
          handler = get2007ExcelHandlerByInputStreamOrFilePath(is, fileNameOrFilePath);
        } catch (Exception ee) {
          throw ee;
        }
      }
    }
    return handler;
  }

  private static CSCExcelHandler get2003ExcelHandlerByInputStreamOrFilePath(InputStream is, String fileNameOrFilePath)
    throws IOException
  {
    CSCExcelHandler handler;
    if (is != null)
      handler = get2003ExcelHandler(is);
    else
      handler = get2003ExcelHandler(fileNameOrFilePath);
    return handler;
  }

  private static CSCExcelHandler get2007ExcelHandlerByInputStreamOrFilePath(InputStream is, String fileNameOrFilePath)
    throws IOException
  {
    CSCExcelHandler handler;
    if (is != null)
      handler = get2007ExcelHandler(is);
    else
      handler = get2007ExcelHandler(fileNameOrFilePath);
    return handler;
  }

  public static CSCExcelHandler getExcelHandler(File file) throws Exception {
    if (file == null) {
      return null;
    }
    return getExcelHandler(file.getAbsolutePath());
  }

  public static CSCExcelHandler get2003ExcelHandler(String filePathName) throws IOException {
    return new Excel2003Handler(filePathName);
  }

  public static CSCExcelHandler get2003ExcelHandler(File file) throws IOException {
    return new Excel2003Handler(file);
  }

  public static CSCExcelHandler get2003ExcelHandler(InputStream is) throws IOException {
    return new Excel2003Handler(is);
  }

  public static CSCExcelHandler get2007ExcelHandler(String filePathName) throws IOException {
    return new Excel2007Handler(filePathName);
  }

  public static CSCExcelHandler get2007ExcelHandler(File file) throws IOException {
    return new Excel2007Handler(file);
  }

  public static CSCExcelHandler get2003ExcelHandler() throws IOException {
    return new Excel2003Handler();
  }

  public static CSCExcelHandler get2007ExcelHandler() throws IOException {
    return new Excel2007Handler();
  }

  public static CSCExcelHandler get2007ExcelHandler(InputStream is) throws IOException {
    return new Excel2007Handler(is);
  }
}