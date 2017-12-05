package ext.tzc.tasv.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

public abstract interface CSCExcelHandler
{
  public abstract boolean exists();

  public abstract String getParent();

  public abstract String getFileName();

  public abstract boolean createNewFile()
    throws IOException;

  public abstract boolean createNewFile(File paramFile)
    throws IOException;

  public abstract boolean createNewFile(String paramString)
    throws IOException;

  public abstract boolean createNewSheet(String paramString)
    throws IOException;

  public abstract void switchCurrentSheet(String paramString);

  public abstract void switchCurrentSheet(int paramInt);

  public abstract int getSheetRowCount();

  public abstract boolean isExistSheet(String paramString);

  public abstract boolean setStringValue(int paramInt1, int paramInt2, String paramString);

  public abstract boolean setNumericValue(int paramInt1, int paramInt2, double paramDouble);

  public abstract boolean setDateValue(int paramInt1, int paramInt2, Date paramDate, String paramString);

  public abstract boolean setBooleanValue(int paramInt1, int paramInt2, boolean paramBoolean);

  public abstract boolean mergeCells(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract String getValue(int paramInt1, int paramInt2);

  public abstract String getStringValue(int paramInt1, int paramInt2);

  public abstract boolean saveChanges()
    throws IOException;

  public abstract void downloadExcel(HttpServletResponse paramHttpServletResponse)
    throws IOException;
}