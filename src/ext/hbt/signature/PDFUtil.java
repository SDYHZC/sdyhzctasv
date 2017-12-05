package ext.hbt.signature;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import ext.hbt.signature.model.CoordinateItem;
import ext.hbt.reviewobjecttype.ObjectTypeConstant;
 
import wt.epm.EPMDocument;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.util.WTException;
import wt.util.WTProperties;


public class PDFUtil {
	private static final Logger logger = LogR.getLogger(SignUtil.class.getName());
	public static String IMAGE_PATH;
	public static WTProperties wtProperties;


	static
	{
		try
		{
			wtProperties = WTProperties.getLocalProperties();
			IMAGE_PATH= wtProperties.getProperty("hbt.signature.imagePath");
			if(IMAGE_PATH==null||IMAGE_PATH.trim().length()==0)
				IMAGE_PATH= wtProperties.getProperty("wt.codebase.location") + File.separator + "ext" + File.separator+ "hbt" + File.separator+ "signature" + File.separator+"printimage";
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	public static BaseFont createBaseFont() throws DocumentException, IOException
	{
		BaseFont bf = BaseFont.createFont(wtProperties.getProperty("wt.home") + File.separator
				+ "codebase" + File.separator + "ext" + File.separator
				+ "tasv" + File.separator + "document" + File.separator
				+ "conf" + File.separator + "simsun.ttc,1",
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		return bf;
	}
	/**
	 * 根据PDF页面大小，判断图幅类型 @
	 */
	public static String getDrawingSize(float height,float width)
	{
		String a0 = "A0";
		String a1 = "A1";
		String a2 = "A2";
		String a3 = "A3";
		String a4s = "A4S";
		String a4h = "A4H";
		String a0X2 = "A0X2";
		String a0X3 = "A0X3";
		String a1X3 = "A1X3";
		String a1X4 = "A1X4";
		String a2X3 = "A2X3";
		String a2X4 = "A2X4";
		String a2X5 = "A2X5";
		String a3X3 = "A3X3";
		String a3X4 = "A3X4";
		String a3X5 = "A3X5";
		String a3X6 = "A3X6";
		String a3X7 = "A3X7";
		String a4X3 = "A4X3";
		String a4X4 = "A4X4";
		String a4X5 = "A4X5";
		String a4X6 = "A4X6";
		String a4X7 = "A4X7";
		String a4X8 = "A4X8";
		String a4X9 = "A4X9";

		String k = Float.toString(width);
		String g = Float.toString(height);
		
		String type="";
		if (width > 3368 && width < 3400 && height > 2378 && height < 2408)
			type= a0;
		// 2393,1690
		else if (width > 2378 && width < 2428 && height > 1675 && height < 1705)
			type= a1;
		// 1690,1198
		else if (width > 1675 && width < 1725 && height > 1183 && height < 1213)
			type= a2;
		else if (width > 1180 && width < 1220 && height > 832 && height < 852)
			type= a3;
		else if (582.0 < width && width < 620.0 && height > 832.0 && height < 852.0)
			type= a4s;
		else if (832.0 < width && width < 862.0 && height > 582 && height < 612)
			type= a4h;
		else if (4750.06 < width && width < 4780.06 && height > 3350.87 && height < 3379.87)
			type= a0X2;
		else if (7130.09 < width && width < 7170.09 && height > 3350.87 && height < 3379.87)
			type= a0X3;
		else if (5030.89 < width && width < 5080.89 && height > 2370.03 && height < 2399.03)
			type= a1X3;
		else if (6710.74 < width && width < 6759.74 && height > 2370.03 && height < 2399.03)
			type= a1X4;
		else if (3550.63 < width && width < 3600.63 && height > 1671.02 && height < 1699.02)
			type= a2X3;
		else if (4750.06 < width && width < 4799.06 && height > 1671.02 && height < 1699.02)
			type= a2X4;
		else if (5930.66 < width && width < 5979.66 && height > 1671.02 && height < 1699.02)
			type= a2X5;
		else if (2511.53 < width && width < 2559.53 && height > 1170.6 && height < 1199.6)
			type= a3X3;
		else if (3350.87 < width && width < 3399.87 && height > 1170.6 && height < 1199.6)
			type= a3X4;
		else if (4190.38 < width && width < 4239.38 && height > 1170.6 && height < 1199.6)
			type= a3X5;
		else if (5030.89 < width && width < 5079.89 && height > 1170.6 && height < 1199.6)
			type= a3X6;
		else if (5870.4 < width && width < 5910.4 && height > 1170.6 && height < 1199.6)
			type= a3X7;
		else if (1770.9 < width && width < 1810.9 && height > 825.51 && height < 859.51)
			type= a4X3;
		else if (2365.03 < width && width < 2410.03 && height > 825.51 && height < 859.51)
			type= a4X4;
		else if (2960.33 < width && width < 2999.99 && height > 825.51 && height < 859.51)
			type= a4X5;
		else if (3550.63 < width && width < 3599.63 && height > 825.51 && height < 859.51)
			type= a4X6;
		else if (4150.93 < width && width < 4199.93 && height > 825.51 && height < 859.51)
			type= a4X7;
		else if (4750.06 < width && width < 4799.06 && height > 825.51 && height < 859.51)
			type= a4X8;
		else if (5340.36 < width && width < 5389.36 && height > 825.51 && height < 859.51)
			type= a4X9;
		logger.debug("  getDrawingSize ==="+type);
		return type;
	}
	/**
	 * 根据PDF页面大小，判断图幅类型 @
	 */
	public static String getFinalObjType(PdfReader reader,int pageNo,String topType,WTObject obj)
	{
		logger.debug("PDF Page Numbers of :" + pageNo);
		Rectangle rect = reader.getPageSize(pageNo);
		float height = rect.getHeight();
		float width = rect.getWidth();
		logger.debug("PDF Page Numbers of :" + pageNo + "  Height:" + height + "  Width:" + width );

		boolean isepm=obj instanceof EPMDocument;
		String finalCooType=topType;
		if(isepm)
		{
			String drawingtype=PDFUtil.getDrawingSize(height,width);
			finalCooType=topType+ObjectTypeConstant.TYPE_SPLIT_IN+drawingtype;
		}
		else
		{
			//文档某页是横向的情况
			int pagerotate = reader.getPageRotation(pageNo);
			logger.debug("PDF Page Numbers of :" + pageNo + "  pagerotate:" + pagerotate );
			boolean isHorizontal=false;
			if (width > height || pagerotate == 270 || pagerotate == 90)
			{
			    isHorizontal = true;
			}
			logger.debug("PDF Page isHorizontal :" + isHorizontal);
			if(isHorizontal)
				finalCooType=topType+ObjectTypeConstant.TYPE_SPLIT_IN+ObjectTypeConstant.DOC_Horizontal_Type;
		}
		logger.debug("finalCooType :" + finalCooType);

		return finalCooType;
	}

	public static void writeToPage(PdfStamper stamp, HashMap cooMap, HashMap inforHm, int pageNo, boolean islastpage) throws WTException
	{
		logger.debug("   writeToPage to page=" + pageNo);
		PdfContentByte over = stamp.getOverContent(pageNo);
		// 循环操作每个有配置坐标的信息
		Iterator iter = cooMap.keySet().iterator();
		
		String contentType = null;
		String signRange = null;
		while (iter.hasNext())
		{
			String attrName = (String) iter.next();
			logger.debug("   writeToPage attr name===" + attrName);
			CoordinateItem coorditem = (CoordinateItem) cooMap.get(attrName);
			String value = (String) inforHm.get(attrName);
			logger.debug("   writeToPage attr value===" + value);
			if (value == null || value.trim().length() == 0)
				continue;
			signRange = coorditem.getSignRange();
			logger.debug("    *******signRange===" + signRange);
			if (!((signRange.equals("-1") && islastpage) || signRange.equals("0") || String.valueOf(pageNo).equals(signRange)))
				continue;
			// 签字页
			contentType = coorditem.getContentType();
			logger.debug("    ******contentType===" + signRange);
			if ("TEXT".equals(contentType))
				wriTtext(over, value, coorditem);
			else if ("IMAGE".equals(contentType))
				wriImage(over, value, coorditem);
		}
	}

	public static void wriTtext(PdfContentByte over, String value, CoordinateItem coorditem) throws WTException
	{
		try
		{
			Float fontSizeTemp = Float.valueOf(coorditem.getFontSize());
			Float xlocationTemp = Float.valueOf(coorditem.getXLocation());
			Float ylocationTemp = Float.valueOf(coorditem.getYLocation());
			Float rotationTemp = Float.valueOf(coorditem.getRotation());
			//BaseFont basefont = BaseFont.createFont(coorditem.getFontType(), "UniGB-UCS2-H", false);
			BaseFont basefont =createBaseFont();
			logger.debug("   wriTtext       fontSizeTemp===" + fontSizeTemp);
			logger.debug("   wriTtext       xlocationTemp===" + xlocationTemp);
			logger.debug("   wriTtext       ylocationTemp===" + ylocationTemp);
			logger.debug("   wriTtext       rotationTemp===" + rotationTemp);
			logger.debug("   wriTtext       fontType===" + coorditem.getFontType());
			String valuesplit[] = value.split(SignatureConstant.PRINT_VALUE_SPLIT);
			for (int i = 0; i < valuesplit.length; i++)
			{
				String tempvalue = valuesplit[i];
				if (tempvalue != null && tempvalue.trim().length() > 0)
				{
					over.beginText();
					over.setFontAndSize(basefont, fontSizeTemp);
					if (i == 0)
					{
						Float xlocationfn = xlocationTemp;
						Float ylocationfn = ylocationTemp;
						over.showTextAligned(0, tempvalue, xlocationfn, ylocationfn, rotationTemp);
						logger.debug("   wriTtext  tempvalue="+tempvalue+" xlocationfn="+xlocationfn+" ylocationfn="+ylocationfn);
					}
					else
					{
						Float xlocationfn = xlocationTemp;
						Float ylocationfn = Float.valueOf((float) (ylocationTemp - SignatureConstant.LINE_HEIGHT * i));
						over.showTextAligned(0, tempvalue, xlocationfn, ylocationfn, rotationTemp);
						logger.debug("   wriTtext  tempvalue="+tempvalue+" xlocationfn="+xlocationfn+" ylocationfn="+ylocationfn);
					}
					over.endText();
				}
			}
		}
		catch (DocumentException e)
		{
			throw new WTException(e);
		}
		catch (IOException e)
		{
			throw new WTException(e);
		}
	}
	public static void wriImage(PdfContentByte over, String value, CoordinateItem coorditem) throws WTException
	{
		try
		{
			Float fontSizeTemp = Float.valueOf(coorditem.getFontSize());
			Float xlocationTemp = Float.valueOf(coorditem.getXLocation());
			Float ylocationTemp = Float.valueOf(coorditem.getYLocation());
			Float heightTemp = null;
			Float weightTemp = null;
			String height=coorditem.getImageHeight();
			if(height!=null&&height.trim().length()>0)
				heightTemp = Float.valueOf(height);
			String weight=coorditem.getImageWidth();
			if(weight!=null&&weight.trim().length()>0)
				weightTemp = Float.valueOf(weight);
  			Float rotationTemp = Float.valueOf(coorditem.getRotation());
  			BaseFont basefont =createBaseFont();
  			String valuesplit[] = value.split(SignatureConstant.PRINT_VALUE_SPLIT);
			boolean imagescrol=(heightTemp!= null&&weightTemp != null);
			HashMap filemap=new HashMap();
			File filePath = new File(IMAGE_PATH);
			if (filePath.exists() && filePath.isDirectory())
			{
				File[] files = filePath.listFiles();
				for (File file : files)
				{
					String fileName = file.getName();
					logger.debug("fileName = " + fileName);
					int index = fileName.lastIndexOf(".");
					if (index != -1)
						fileName = fileName.substring(0, index);
					filemap.put(fileName, file.getAbsolutePath());
				}
			}
			for (int i = 0; i < valuesplit.length; i++)
			{
				String tempvalue = valuesplit[i];
				if (tempvalue != null && tempvalue.trim().length() > 0)
				{
			       String filepath=(String) filemap.get(tempvalue);
			       Image img = null;
			       if(filepath!=null)
					  img = Image.getInstance(Toolkit.getDefaultToolkit().createImage(filepath), null);
			       //打印图片
			       if(img!=null)
			       {
			    	   if(imagescrol)
							img.scaleToFit(weightTemp, heightTemp);
			    	   if (i == 0)
						   img.setAbsolutePosition(xlocationTemp, ylocationTemp);
						else
						{
							 if(imagescrol)
								 img.setAbsolutePosition(xlocationTemp, (ylocationTemp-heightTemp)*i);
							 else
							 {
								 img.setAbsolutePosition(xlocationTemp, (ylocationTemp-img.getHeight())*i);
							 }
						}
						over.addImage(img);
						 
			       }
			       //打印文字
			       else
			       {
			    	   over.beginText();
						over.setFontAndSize(basefont, fontSizeTemp);
						if (i == 0)
						{
							Float xlocationfn = xlocationTemp;
							Float ylocationfn = ylocationTemp;
							over.showTextAligned(0, tempvalue, xlocationfn, ylocationfn, rotationTemp);
						}
						else
						{
							Float xlocationfn = xlocationTemp;
							Float ylocationfn = Float.valueOf((float) (ylocationTemp - SignatureConstant.LINE_HEIGHT * i));
							over.showTextAligned(0, tempvalue, xlocationfn, ylocationfn, rotationTemp);
						}
						over.endText();
			       }
				}
			}
		}
		catch (DocumentException e)
		{
			throw new WTException(e);
		}
		catch (IOException e)
		{
			throw new WTException(e);
		}
	}
}
