package ext.pdf;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import wt.change2.ChangeHelper2;
import wt.change2.ChangeRecord2;
import wt.change2.ChangeableIfc;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.inf.team.ContainerTeamHelper;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.vc.baseline.BaselineHelper;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfRequesterActivity;
import wt.workflow.engine.WfState;
import wt.workflow.engine.WfVotingEventAudit;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignment;
import wt.workflow.work.WfBallot;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfWriter;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.netmarkets.workflow.NmWorkflowHelper;

import ext.util.IBAUtility;

/**
 * 
 * @author Efflux//
 * 
 */
////
public class SignECN{

	static float hqspace = 5.5f;

	static float cc = 595.22f / 210f;

	static float cy = 842f / 297f;

	private static String path;

	private static String wthome;

	private static Image jpeg;
	
	private static String docTime = "";
	
	private static String bianhao = "";
	
	private static Properties properties = new Properties();
	
	private static Map zuMap =new HashMap();
	static {
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			wthome = wtproperties.getProperty("wt.home");
			path = wthome + "/temp/";
			zuMap.put("质量部_接收组", "质量部_接收组");
			zuMap.put("生产计划部_接收组", "生产计划部_接收组");
			zuMap.put("军品科研计划部_接收组", "军品科研计划部_接收组");
			zuMap.put("外协部_接收组", "外协部_接收组");
			zuMap.put("物资管理部_接收组", "物资管理部_接收组");
			zuMap.put("军品设计部_接收组", "军品设计部_接收组");
			zuMap.put("民品设计部_接收组", "民品设计部_接收组");
			zuMap.put("零部件设计部_接收组", "零部件设计部_接收组");
			zuMap.put("工艺部_接收组", "工艺部_接收组");
			zuMap.put("试验中心_接收组", "试验中心_接收组");
			zuMap.put("研究院项目管理部_接收组", "研究院项目管理部_接收组");
			zuMap.put("信息中心_接收组", "信息中心_接收组");
			zuMap.put("矿用自卸车业务部_接收组", "矿用自卸车业务部_接收组");
			zuMap.put("油田车业务部_接收组", "油田车业务部_接收组");
			zuMap.put("特种车业务部_接收组", "特种车业务部_接收组");
			zuMap.put("专用车业务部_接收组", "专用车业务部_接收组");
			zuMap.put("国际市场部_接收组", "国际市场部_接收组");
			zuMap.put("售后服务部_接收组", "售后服务部_接收组");
			zuMap.put("总装一车间_接收组", "总装一车间_接收组");
			zuMap.put("总装二车间_接收组", "总装二车间_接收组");
			zuMap.put("车桥车间_接收组", "车桥车间_接收组");
			zuMap.put("车架车身车间_接收组", "车架车身车间_接收组");
			zuMap.put("涂漆车间_接收组", "涂漆车间_接收组");
			zuMap.put("热处理车间_接收组", "热处理车间_接收组");
			zuMap.put("采购部_接收组", "采购部_接收组");
			zuMap.put("精益制造部_接收组", "精益制造部_接收组");
			String propertiespath = "";
			WTProperties props = WTProperties.getLocalProperties();
			propertiespath=props.getProperty("wt.home") + File.separatorChar + "codebase" + File.separatorChar
			+ "netmarkets" + File.separatorChar + "jsp" + File.separatorChar 
			+ "ext" + File.separatorChar + "WTVariance"+File.separatorChar +"department.properties";
			System.out.println("zyj--test--propertiespath:"+propertiespath);
			properties.load(new FileInputStream(new File(propertiespath)));
			
		} catch (Throwable throwable) {
			
		}
	}


	private static Hashtable InitXml() throws Exception {

		java.util.Hashtable ht = new java.util.Hashtable();
		java.util.Hashtable ht1 = new java.util.Hashtable();
		java.util.Hashtable ht2 = new java.util.Hashtable();
		java.util.Hashtable ht3 = new java.util.Hashtable();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bulider = factory.newDocumentBuilder();
		Document doc = bulider.parse(wthome + "/codebase/ext/pdf/signpdf.xml");
		NodeList nl = doc.getElementsByTagName("type");

		for (int i = 0; i < nl.getLength(); i++) {
			org.w3c.dom.Element nodeType = (org.w3c.dom.Element) nl.item(i);
			String name1 = nodeType.getAttribute("name");

			NodeList n2 = nodeType.getElementsByTagName("page");
			ht1 = new java.util.Hashtable();
			for (int j = 0; j < n2.getLength(); j++) {
				org.w3c.dom.Element nodePage = (org.w3c.dom.Element) n2.item(j);
				String name2 = nodePage.getAttribute("pageno");
				NodeList n3 = nodePage.getElementsByTagName("section");
				ht2 = new java.util.Hashtable();
				for (int k = 0; k < n3.getLength(); k++) {
					ht3 = new java.util.Hashtable();
					org.w3c.dom.Element nodeSection = (org.w3c.dom.Element) n3
							.item(k);
					String name3 = nodeSection.getAttribute("name");
					String x = nodeSection.getElementsByTagName("x").item(0)
							.getFirstChild().getNodeValue().trim();
					String y = nodeSection.getElementsByTagName("y").item(0)
							.getFirstChild().getNodeValue().trim();
					String kuan = nodeSection.getElementsByTagName("chang")
							.item(0).getFirstChild().getNodeValue().trim();
					String chang = nodeSection.getElementsByTagName("kuan")
							.item(0).getFirstChild().getNodeValue().trim();
					String datex = nodeSection.getElementsByTagName("datex")
							.item(0).getFirstChild().getNodeValue().trim();
					String datey = nodeSection.getElementsByTagName("datey")
							.item(0).getFirstChild().getNodeValue().trim();
					String yearx = nodeSection.getElementsByTagName("yearx")
					.item(0).getFirstChild().getNodeValue().trim();
					String yeary = nodeSection.getElementsByTagName("yeary")
							.item(0).getFirstChild().getNodeValue().trim();
					String monthx = nodeSection.getElementsByTagName("monthx")
					.item(0).getFirstChild().getNodeValue().trim();
					String monthy = nodeSection.getElementsByTagName("monthy")
							.item(0).getFirstChild().getNodeValue().trim();
					String dayx = nodeSection.getElementsByTagName("dayx")
					.item(0).getFirstChild().getNodeValue().trim();
					String dayy = nodeSection.getElementsByTagName("dayy")
						.item(0).getFirstChild().getNodeValue().trim();
					String fontsize = nodeSection.getElementsByTagName(
							"fontsize").item(0).getFirstChild().getNodeValue()
							.trim();
					String rotation = nodeSection.getElementsByTagName(
							"rotation").item(0).getFirstChild().getNodeValue()
							.trim();

					ht3.put("x", x);
					ht3.put("y", y);
					ht3.put("chang", kuan);
					ht3.put("kuan", chang);
					ht3.put("datex", datex);
					ht3.put("datey", datey);
					ht3.put("yearx", yearx);
					ht3.put("yeary", yeary);
					ht3.put("monthx", monthx);
					ht3.put("monthy", monthy);
					ht3.put("dayx", dayx);
					ht3.put("dayy", dayy);
					ht3.put("fontsize", fontsize);
					ht3.put("rotation", rotation);
					ht2.put(name3, ht3);
				}
				ht1.put(name2, ht2);// page
			}
			ht.put(name1, ht1);// doctype
		}
		return ht;
	}

	private static String getNodeValue(org.w3c.dom.Element element,
			String childElementName) {
		String value = element.getElementsByTagName(childElementName).item(0)
				.getNodeValue();
		if (value != null)
			value = "";
		return value;
	}


	private static void writeText(PdfContentByte cb, BaseFont bf, Hashtable ht,
			Hashtable htrevise, Vector<String> timeVec,String filetype,WTObject pbo)
			throws Exception {
		// htrevise 系统中的
		java.util.Enumeration keys = ht.keys();

		while (keys.hasMoreElements()) {

			String key = (String) keys.nextElement();
			Hashtable section = (Hashtable) ht.get(key);

			float x = Float.parseFloat((String) section.get("x"));
			float y = Float.parseFloat((String) section.get("y"));
			float chang = Float.parseFloat((String) section.get("chang"));
			float kuan = Float.parseFloat((String) section.get("kuan"));
			float datex = Float.parseFloat((String) section.get("datex"));
			float datey = Float.parseFloat((String) section.get("datey"));
			float fontsize = Float.parseFloat((String) section.get("fontsize"));
			float rotation = Float.parseFloat((String) section.get("rotation"));
			
			float yearx=Float.parseFloat((String) section.get("yearx"));
			float yeary=Float.parseFloat((String) section.get("yeary"));
			float monthx=Float.parseFloat((String) section.get("monthx"));
			float monthy=Float.parseFloat((String) section.get("monthy"));
			float dayx=Float.parseFloat((String) section.get("dayx"));
			float dayy=Float.parseFloat((String) section.get("dayy"));
			if (htrevise.get(key) != null) {
				String usertime = (String) htrevise.get(key);
				usertime = usertime.replaceAll(";;;", " ").replaceAll("&&&",
						" ");
				int num1 = usertime.indexOf(" ");
				String name = usertime.substring(0, num1);
				String picturePath = null;
				String time = usertime.substring(usertime.length() - 10,
						usertime.length());
				try {
					WTProperties wtproperties = WTProperties
							.getLocalProperties();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//签宋体
				
				if(key.equalsIgnoreCase("更改单发放")) {
					WfProcess last_process = getProcess(pbo);
					Hashtable table =getReviews(last_process, properties);
					if(table != null){
			    		Enumeration em = table.keys();
			    		while(em.hasMoreElements()){
			    			String act_name = (String)em.nextElement();
			    			System.out.println("zyj--test--shoujian start!!");
			    			if(act_name.equals("更改单发放"))
			    			{
			    				Vector names = (Vector)table.get(act_name);
			    				for(int n=0;n<names.size();n++){
									String name_time = (String)names.get(n);
									System.out.println("zyj--test--name_time:"+name_time);
									String department = name_time.substring(name_time.lastIndexOf("_=_")+3,name_time.lastIndexOf("_==_"));
									String departName = name_time.substring(name_time.indexOf(";;") + 2,name_time.lastIndexOf("__"));
									String dapartDate = name_time.substring(name_time.lastIndexOf("__")+2,name_time.lastIndexOf("__")+12);
									System.out.println("zyj--test--department:"+department+"==departName:"+departName+"==dapartDate:"+dapartDate);
									if(!"".equals(department)){
										department = department.substring(0, department.indexOf("_接收组"));
									}
									if(n==0){
										cb.beginText();
										cb.setFontAndSize(bf, fontsize);
										cb.showTextAligned(Element.ALIGN_LEFT, department, x * cc, y * cy, rotation);
										cb.showTextAligned(Element.ALIGN_LEFT, departName, (Float.parseFloat((x+31.75)*cc+"")), y * cy, rotation);
										cb.showTextAligned(Element.ALIGN_LEFT, dapartDate, (Float.parseFloat((x+61.27)*cc+"")), y * cy, rotation);
										cb.endText();
									}else{
										cb.beginText();
										cb.setFontAndSize(bf, fontsize);
										cb.showTextAligned(Element.ALIGN_LEFT, department, x * cc, (Float.parseFloat((y-5.77*n)*cy+"")), rotation);
										cb.showTextAligned(Element.ALIGN_LEFT, departName, (Float.parseFloat((x+31.75)*cc+"")), (Float.parseFloat((y-5.77*n)*cy+"")), rotation);
										cb.showTextAligned(Element.ALIGN_LEFT, dapartDate, (Float.parseFloat((x+61.27)*cc+"")), (Float.parseFloat((y-5.77*n)*cy+"")), rotation);
										cb.endText();
									}
			    				}
			    			}
			    		}
			    	}
				}else if(yearx!=0 && yeary!=0 && monthx!=0 && monthy!=0 && dayx!=0 && dayy!=0){
					System.out.println("zyj--test--签年月日================");
					String year = time.split("-")[0];
					String month =time.split("-")[1];
					String day = time.split("-")[2];
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(Element.ALIGN_LEFT, name, x * cc, y * cy, rotation);
					cb.showTextAligned(Element.ALIGN_LEFT, year, yearx * cc, yeary * cy, rotation);
					cb.showTextAligned(Element.ALIGN_LEFT, month, monthx * cc, monthy * cy, rotation);
					cb.showTextAligned(Element.ALIGN_LEFT, day, dayx * cc, dayy * cy, rotation);
					cb.endText();
				}else if(datex!=0 && datey!=0 ){
					System.out.println("zyj--test--连签================");
					time=time.replaceAll("-", ".");
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(Element.ALIGN_LEFT, name, x * cc, y * cy, rotation);
					cb.showTextAligned(Element.ALIGN_LEFT, time, datex * cc, datey * cy, rotation);
					cb.endText();
				}else{
					System.out.println("============默认=============");
					time=time.replaceAll("-", "");
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(Element.ALIGN_LEFT, name, x * cc, y * cy, rotation);
					cb.showTextAligned(Element.ALIGN_LEFT, time, (Float.parseFloat((x+11.54)*cc+"")), y * cy, rotation);
					cb.endText();
				}
			}else{
				System.out.println("==========test==========");
				if(key.equalsIgnoreCase("TIME")) {
					docTime  = docTime.substring(0,10);
					docTime = docTime.replaceFirst("-", "年");
					docTime = docTime.replaceFirst("-", "月");
					docTime = docTime+"日";
					System.out.println("zyj--test--TIME=========="+docTime);
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(Element.ALIGN_LEFT, docTime, x * cc, y * cy, rotation);
					cb.endText();
				}else if(key.equalsIgnoreCase("BIANHAO")) {
					System.out.println("zyj--test--BIANHAO=========="+bianhao);
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(Element.ALIGN_LEFT, bianhao, x * cc, y * cy, rotation);
					cb.endText();
					
				}
			}
		}
	}


	public static String getType(String sourceType, WTDocument wtdoc) {
		String xihuafenlei = "---";
		if (wtdoc != null) {
			IBAUtil ibUtil = new IBAUtil(wtdoc);
			xihuafenlei = ibUtil.getIBAValue("WENJIANFENLEI");
		}
		String returnType = null;
		try {
			java.util.Properties prop = new Properties();
			String filename = wthome + "/codebase/ext/pdf/type.properties";
			FileInputStream fis = new FileInputStream(filename);
			prop.load(fis);
			Enumeration enump = prop.keys();
			while (enump.hasMoreElements())  {
				String t = (String) enump.nextElement();
				String t2 = new String(t.getBytes("ISO8859-1"), "GB2312");
				String t1 = (String) prop.getProperty(t);
				System.out.println("t.getBytes:"+t.getBytes("ISO8859-1"));
				System.out.println("t2:"+t2+"==============sourceType:"+sourceType);
				System.out.println("t2.indexOf(sourceType):"+t2.indexOf(sourceType)+"======sourceType.indexOf(t2):"+sourceType.indexOf(t2));
				if ((t2.indexOf(sourceType) != -1)
						|| sourceType.indexOf(t2) != -1) {
					returnType = t1;
					
					break;
				} else {
					System.out.println("xihuafenlei:"+xihuafenlei);
					if (xihuafenlei != null && xihuafenlei.length() > 0) {
						if ((t2.indexOf(xihuafenlei) != -1)
								|| xihuafenlei.indexOf(t2) != -1) {
							returnType = t1;
							break;
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			//ss TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnType;
	}

	public static void writeText(Hashtable htrevise, String filetype,
			String fileName, Vector<String> timeVec, WTDocument wtdoc,WTObject pbo) {
		try {
			String oldfileName=fileName;
			Hashtable hts = InitXml();
			String writeType = "";
			System.out.println("zyj--test--filetype:"+filetype+"==============");
			if(filetype.substring(filetype.length()-9).equalsIgnoreCase("tasvdoc03")){
				writeType = filetype.substring(filetype.length()-9);
			}
			
			System.out.println("zyj--test--writeType:"+writeType);
			if (writeType == null) {
				return;
			}
			Hashtable htf = (Hashtable) hts.get(writeType);

			com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(
					fileName);

			int pages = reader.getNumberOfPages();

			Rectangle psize = reader.getPageSize(1);
			float height = psize.getHeight();
			float width = psize.getWidth();
			Rectangle psize2 = reader.getPageSizeWithRotation(1);//新实际位置
			Rectangle rectPageSize = null;
			
			if(psize2.getWidth()!=width && psize2.getHeight()!=height)
			{
				rectPageSize = new Rectangle(height, width);// 定义页面大小
				System.out.println("111height-==>"+rectPageSize.getHeight()+"|||width-==>"+rectPageSize.getWidth());
                System.out.println(rectPageSize.getRotation());
                rectPageSize = rectPageSize.rotate();
                System.out.println("222height-==>"+rectPageSize.getHeight()+"|||width-==>"+rectPageSize.getWidth());
                System.out.println(rectPageSize.getRotation());
                rectPageSize = rectPageSize.rotate();
                System.out.println("333height-==>"+rectPageSize.getHeight()+"|||width-==>"+rectPageSize.getWidth());
                System.out.println(rectPageSize.getRotation());
                rectPageSize = rectPageSize.rotate();
                System.out.println("444height-==>"+rectPageSize.getHeight()+"|||width-==>"+rectPageSize.getWidth());
                System.out.println(rectPageSize.getRotation());
                
                
    			
                com.lowagie.text.Document documentnew = new com.lowagie.text.Document(rectPageSize);
                int ii = fileName.lastIndexOf("/");
    			fileName = path + "temp_" + fileName.substring(ii + 1);
    			System.out.println("zyj-test--path--"+path);
    			System.out.println("zyj-test--fileName--"+fileName);
    			PdfWriter writernew = PdfWriter.getInstance(documentnew, new FileOutputStream(fileName));
    			documentnew.open();
                
                PdfContentByte cbnew = writernew.getDirectContent();
                BaseFont bfnew = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", BaseFont.IDENTITY_H,
    					BaseFont.EMBEDDED);
                cbnew.setFontAndSize(bfnew, 12);
                Hashtable htpOthers = (Hashtable) htf.get("others");
                for (int i = 1; i <= pages; i++) 
    			{

    					PdfImportedPage pagei = writernew.getImportedPage(reader, i);
    					cbnew.addTemplate(pagei, 0, 0);
    					documentnew.newPage();
    			}
	    		documentnew.close();
	            writernew.close();
	            reader = new com.lowagie.text.pdf.PdfReader(fileName);
	    		pages = reader.getNumberOfPages();
	    		psize = reader.getPageSize(1);// 老的
	    		height = psize.getHeight();
	    		width = psize.getWidth();
	    		rectPageSize = new Rectangle(width, height);// 定义页面大小
    		}else{
    			rectPageSize = new Rectangle(width, height);// 定义页面大小
    		}
			com.lowagie.text.Document document = new com.lowagie.text.Document(
					rectPageSize);

			int ii = fileName.lastIndexOf("/");
			String outfilename = path + "temp_" +"new_"+ fileName.substring(ii + 1);
			System.out.println("===outfilename:"+outfilename+"==================");
			PdfWriter writer = PdfWriter.getInstance(document,
					new FileOutputStream(outfilename));
			document.open();

			PdfContentByte cb = writer.getDirectContent();

			BaseFont bf = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", BaseFont.IDENTITY_H,
					BaseFont.EMBEDDED);
			cb.setFontAndSize(bf, 12);
			Hashtable htpOthers = (Hashtable) htf.get("others");
			for (int i = 1; i <= pages; i++) {
				Hashtable htp = (Hashtable) htf.get(String.valueOf(i));
				System.out.println("zyj--test--htp:"+htp+"============");
				if (htp != null) {
					System.out.println("zyj--test--第一页");
					PdfImportedPage pagei = writer.getImportedPage(reader, i);
					cb.addTemplate(pagei, 0, 0);
					writeText(cb, bf, htp, htrevise, timeVec,filetype,pbo);
					document.newPage();
				}else if(htpOthers!=null){
					System.out.println("zyj--test--多页");
					PdfImportedPage pagei = writer.getImportedPage(reader, i);
					cb.addTemplate(pagei, 0, 0);
					writeText(cb, bf, htpOthers, htrevise, timeVec,filetype,pbo);
					document.newPage();
				}else{
					PdfImportedPage pagei = writer.getImportedPage(reader, i);
					cb.addTemplate(pagei, 0, 0);
					document.newPage();
				}
			}
			document.close();
			File pdfFileold=new File(oldfileName); 
			File pdfFile = new File(fileName);
			pdfFileold.delete();
			pdfFile.delete();

			File tempPdfFile = new File(outfilename);
			tempPdfFile.renameTo(pdfFileold);


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static String getSoftAtribute(WTObject obj) {
		String wenjianfenlei="";
		if (obj instanceof WTDocument) {
			WTDocument doc=(WTDocument)obj;
			try {
				IBAUtil ibUtil = new IBAUtil(doc);
				wenjianfenlei = ibUtil.getIBAValue("WENJIANFENLEI");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wenjianfenlei;
	}

	public static String tufu(String fileName) {
		String a0h = "A0H";
		String a1h = "A1H";
		String a2h = "A2H";
		String a3h = "A3H";
		String a4h = "A4H";
		String a4s = "A4S";
		String a02 = "A02";
		String a03 = "A03";
		String a13 = "A13";
		String a14 = "A14";
		String a23 = "A23";
		String a24 = "A24";
		String a25 = "A25";
		String a33 = "A33";
		String a34 = "A34";
		String a35 = "A35";
		String a36 = "A36";
		String a37 = "A37";
		String a43 = "A43";
		String a44 = "A44";
		String a45 = "A45";
		String a46 = "A46";
		String a47 = "A47";
		String a48 = "A48";
		String a49 = "A49";
		
		try {

			com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(
					fileName);
			Rectangle rect = reader.getPageSizeWithRotation(1);
			float width = rect.getWidth();
			float height = rect.getHeight();
		   String k=Float.toString(width);
		   String g=Float.toString(height);
		   System.out.println("tufu--width--"+Float.toString(width)+"tufu--height--"+Float.toString(height));
		    if (width > 3368 && width < 3398 && height > 2378 && height < 2408) {
				System.out.println("tufu: - -! A0H");
				return a0h;
			}
			// 2393,1690
			else if (width > 2378 && width < 2408 && height > 1675 && height < 1705) {
				System.out.println("tufu: - -! A1H");
				return a1h;

			}
			// 1690,1198
			else if (width > 1675 && width < 1705 && height > 1183 && height < 1213) {
				System.out.println("tufu: - -! A2H");
				return a2h;

			}else if(width > 1180 && width < 1200 && height > 832 && height < 852){
				System.out.println("tufu: - -! A3H");
				return a3h;
			} else if (582.0 < width && width < 612.0 && height > 832.0 && height < 852.0) {
				System.out.println("tufu: - -! A4s");
				return a4s;
			} else if (832.0 < width && width < 852.0 && height > 582 && height < 612) {
				System.out.println("tufu: - -! A4h");
				return a4h;
			} else if (4750.06 < width && width < 4770.06 && height > 3350.87 && height < 3379.87) {
				System.out.println("tufu: - -! A02");//4760.06,3364.87
				return a02;
			}else if (7130.09 < width && width < 7150.09 && height > 3350.87 && height < 3379.87) {
				System.out.println("tufu: - -! A03");//7140.09;3364.87
				return a03;
			}else if (5030.89 < width && width <5080.89 && height > 2370.03 && height < 2399.03) {
				System.out.println("tufu: - -! A13");   //5045.89;2380.03
				return a13;
			}else if (6710.74 < width && width < 6739.74 && height > 2370.03 && height < 2399.03) {
				System.out.println("tufu: - -! A14");//6729.74,2380.03
				return a14;
			}else if (3550.63 < width && width < 3580.63 && height > 1671.02 && height < 1699.02) {
				System.out.println("tufu: - -! A23");//3568.63;1681.02
				return a23;
			}else if (4750.06 < width && width < 4779.06 && height > 1671.02 && height < 1699.02) {
				System.out.println("tufu: - -! A24");//4760.06;1681.02
				return a24;
			}else if (5930.66 < width && width < 5959.66 &&  height > 1671.02 && height < 1699.02) {
				System.out.println("tufu: - -! A25");//5948.66;1681.02
				return a25;
			}else if (2511.53 < width && width < 2539.53 && height > 1170.6 && height < 1199.6) {
				System.out.println("tufu: - -! A33");//2521.53;1188.6
				return a33;
			}else if (3350.87 < width && width < 3379.87 && height > 1170.6 && height < 1199.6) {
				System.out.println("tufu: - -! A34");//3364.87;1188.6
				return a34;
			}else if (4190.38 < width && width < 4219.38 && height > 1170.6 && height < 1199.6) {
				System.out.println("tufu: - -! A35");//4205.38;1188.6
				return a35;
			}else if (5030.89 < width && width < 5059.89 && height > 1170.6 && height < 1199.6) {
				System.out.println("tufu: - -! A36");//5045.89;1188.6
				return a36;
			}else if (5870.4 < width && width < 5899.4 && height > 1170.6 && height < 1199.6) {
				System.out.println("tufu: - -! A37");//5886.4;1188.6
				return a37;
			}else if (1770.9 < width && width < 1799.9 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A43");//1782.9;840.51
				return a43;
			}else if (2365.03 < width && width < 2399.03 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A44");//2380.03;840.51
				return a44;
			}else if (2960.33 < width && width < 2989.33 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A45");//2974.33;840.51
				return a45;
			}else if (3550.63 < width && width < 3579.63 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A46");//3568.63;840.51
				return a46;
			}else if (4150.93 < width && width < 4179.93 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A47");//4162.93;840.51
				return a47;
			}else if (4750.06 < width && width < 4779.06 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A48");//4760.06;840.51
				return a48;
			}else if (5340.36 < width && width < 5369.36 && height > 825.51 && height < 859.51) {
				System.out.println("tufu: - -! A49");//5354.36;840.51
				return a49;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Hashtable getReviews(ObjectReference selfRef)
			throws Exception {
		Hashtable revise = new Hashtable();
		Enumeration enu1 = null;
		Enumeration enumeration = null;

		try {
			WfProcess self = (WfProcess) selfRef.getObject();
			enu1 = WfEngineHelper.service.getProcessSteps(self, null);
			Vector v = new Vector();
			while (enu1.hasMoreElements()) {
				Object obj = (Object) enu1.nextElement();
				if (obj instanceof WfAssignedActivity) {
					v.addElement(obj);
				} else if (obj instanceof WfRequesterActivity) {
					WfContainer con = ((WfRequesterActivity) obj)
							.getPerformer();
					Enumeration enu2 = null;
					enu2 = WfEngineHelper.service.getProcessSteps(con, null);
					while (enu2.hasMoreElements()) {
						Object obj2 = (Object) enu2.nextElement();
						if (obj2 instanceof WfAssignedActivity) {
							v.addElement(obj2);
						}
					}
				}
			}
			enumeration = v.elements();

			while (enumeration.hasMoreElements()) {
				WfAssignedActivity wfactivity = (WfAssignedActivity) enumeration
						.nextElement();
				// begin if(0)
				if (wfactivity != null
						&& wfactivity instanceof WfAssignedActivity
						&& (wfactivity.getState().toString())
								.equalsIgnoreCase("CLOSED_COMPLETED_EXECUTED")) {
					//
					revise.put(wfactivity.getName(),
							getPrincipalName(wfactivity));
				}
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		return revise;
	}

	/**
	 *  获取节点信息（ID + Time）
	 */

	private static String getPrincipalName(WfActivity wfa) throws Exception {
		String field = "";
		String str = "";
		String strfull = "";
		Enumeration en1 = null;
		Enumeration en2 = null;
		en1 = ((WfAssignedActivity) wfa).getAssignments();

		// 获得任务完成时间
		String time;
		if (wfa.getStartTime() != null && wfa.getEndTime() != null) {
			time = WTStandardDateFormat.format(wfa.getEndTime(), "yyyy-MM-dd");
		} else {
			time = "";
		}

		for (int i = 0; en1 != null && en1.hasMoreElements(); i++) {
			WfAssignment wfassignment = (WfAssignment) en1.nextElement();
			en2 = wfassignment.checkBallotStatus().elements();
			for (int j = 0; en2 != null && en2.hasMoreElements(); j++) {
				WfBallot wfballot = (WfBallot) en2.nextElement();
				// str = wfballot.getVoter().getPrincipal().getName();
				WTPrincipal wtp = wfballot.getVoter().getPrincipal();

				if (wtp instanceof WTUser){
					str = ((WTUser) wtp).getName().toString();// 得到用户ID
					// ///////////////得到用户全名
					strfull = ((WTUser) wtp).getFullName().toString();
					if (strfull.indexOf(",") > 0) {
						strfull = strfull.replaceFirst(",", "");
					}
					strfull = strfull.replaceAll(" ", "");// 去掉空格
					}
				else if (wtp instanceof WTGroup)
					str = ((WTGroup) wtp).getName().toString();
				if (str != null && str.length() > 0) {
					if (field == "")
						field = strfull + ";;;" + time;
					else
						field = field + "&&&" + str + ";;;" + time;
				}
			}
		}
		return field;
	}

	public static String downloadpdf2(Representable doc) throws WTException,
			PropertyVetoException, IOException {
		Representation representation = RepresentationHelper.service
				.getDefaultRepresentation(doc);//获得表示法
		if (representation != null) {
			wt.content.ContentHolder ch = wt.content.ContentHelper.service
					.getContents(doc);//得到文档所在的容器
			representation = (Representation) ContentHelper.service
					.getContents(representation); // 得到表示法的ContentHolder（内容持有者）
			Vector vector1 = ContentHelper.getContentList(representation);// 得到内容列表
			for (int l = 0; l < vector1.size(); l++) {
				ContentItem contentitem = (ContentItem) vector1.elementAt(l);
				if (contentitem instanceof ApplicationData) {
					ApplicationData applicationdata = (ApplicationData) contentitem; // 得到表示法对象的数据
					InputStream in = ContentServerHelper.service
							.findContentStream(applicationdata);
					String filename = applicationdata.getFileName();// 得到表示法对象的文件名
					filename=unescape(filename);
					if (filename.indexOf(".pdf") > 0) {
						String absoluteFileName = path + filename;
						docUtil.downloadFile(in, absoluteFileName);//下载文档
						return absoluteFileName;
					}
				}
			}
		}
		return null;
	}


	public static void changePdfRevise(WTDocument wtdoc, ObjectReference self,
			Vector<String> timeVec,WTObject pbo) {

		try {
			boolean repReady = true;
			String absoluteFileName="";
			if(wtdoc!=null){
				absoluteFileName = downloadpdf2(wtdoc);
				if (absoluteFileName != null) {
					WfProcess last_process = getProcess(wtdoc);
					Hashtable revise =getReviews(self);
					IBAUtil ibautil = new IBAUtil((wt.iba.value.IBAHolder) wtdoc);
					String typename = "";
					try {
						typename = getSoftType(wtdoc);
					} catch (WTException e) {
						return;
					}
					if (typename == null || typename.length() < 1) {
						return;
					}
					Hashtable reset = new Hashtable();
					reset = resetWF(revise);
					// typename = wtDeel(typename, absoluteFileName);
					writeText(reset, typename, absoluteFileName, timeVec, wtdoc,pbo);
					docUtil.uploadpdf(wtdoc, absoluteFileName);
					File pdfFile = new File(absoluteFileName);
					pdfFile.delete();
				} else {

					wt.content.ContentHolder ch = wt.content.ContentHelper.service
							.getContents(wtdoc);
					Vector attachmentList = wt.content.ContentHelper
							.getApplicationData(ch);
					for (int i = 0; i < attachmentList.size(); i++) {
						wt.content.ApplicationData appDataPDF = (wt.content.ApplicationData) attachmentList
								.get(i);
						String fileName = appDataPDF.getFileName();
						System.out.println("============aaa============"+fileName+"---------");
						if (fileName.toLowerCase().endsWith(".pdf")) {
							int pdf = fileName.lastIndexOf("-");
							if (pdf < 0) {
								pdf = fileName.lastIndexOf(".");
							}

							absoluteFileName = path + fileName;
							ContentServerHelper.service.writeContentStream(
									appDataPDF, absoluteFileName);
							WfProcess last_process = getProcess(wtdoc);
							Hashtable revise =getReviews(self);
							IBAUtil ibautil = new IBAUtil(
									(wt.iba.value.IBAHolder) wtdoc);
							String typename = "";
							try {
								typename = getSoftType(wtdoc);
							} catch (WTException e) {
								return;
							}
							if (typename == null || typename.length() < 1) {
								return;
							}
							System.out.println("========================"+typename);
							Hashtable reset = new Hashtable();
							reset = resetWF(revise);
//								typename = wtDeel(typename, absoluteFileName);
							writeText(reset, typename, absoluteFileName, timeVec,
									wtdoc,pbo);
							ContentServerHelper.service.updateContent(ch,
									appDataPDF, absoluteFileName);

							File pdfFile = new File(absoluteFileName);
							pdfFile.delete();
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Vector<String> setTime(Vector<String> timeVec) {
		SimpleDateFormat fmDate = new SimpleDateFormat("yyyy-mm-dd");
		String str = fmDate.format(new Date());
		timeVec.add(str);
		return timeVec;
	}

	public static String getSoftType(WTObject obj) throws WTException {// haha
		String typename = "";
		TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
		typename = type.getTypename();
		return typename;
	}

	private static void debug(String s) {
		StackTraceElement ste = new Throwable().getStackTrace()[2];
		String ss = ste.getFileName() + "." + ste.getLineNumber() + ": ";
		System.out.println(ss + s);
	}

	public static String wtDeel(String typename, String fileName) {
		String s = typename;
		String s1 = null;
		String s2 = null;
		String s3 = null;
		if (!s.equals("�㲿��ͼ(��(���ƹ����ͼ)") && !s.equals("����ԭ��ͼ")
				&& !s.equals("l��ʾ��ͼ(����l��ͼ)") && !s.equals("��jͼ")
				&& !s.equals("�������ͼ") && !s.equals("����ͼ")) {
			return s;
		} else {
			s1 = "autoCAD";
			s2 = tufu(fileName);
			s3 = s1 + "_" + s2;
			return s3;
		}
	}

	public static Hashtable resetWF(Hashtable ht) {
		java.util.Enumeration keys = ht.keys();
		Hashtable resetTable = new Hashtable();

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String str = (String) ht.get(key);
			String ss = null;
			if (key.equals("编制") || key.equals("提交审签")
					|| key.equals("提交升级")) {
				ss = "编制";
			} else {
				ss = key;
			}
			resetTable.put(ss, str);

		}

		return resetTable;
	}


	public static void signECN(Vector<WTObject> vector, ObjectReference self,
			Vector<String> timeVec,WTObject pbo) {
		if (vector != null) {
			int size = vector.size();
			for (int i = 0; i < size; i++) {
				WTObject object = vector.get(i);
				if (object instanceof WTDocument) {
					WTDocument wtDocument = (WTDocument) object;
					docTime = wtDocument.getCreateTimestamp()+"";
					bianhao = wtDocument.getNumber();
					System.out.println("zyj--test--docTIME:"+docTime+"===bianhao:"+bianhao);
					changePdfRevise(wtDocument, self, timeVec,pbo);
				} 
			}
		}
	}

	public static Vector getChangeAfters(WTObject pbo) throws Exception {
		Vector vec = new Vector();
		QueryResult qr = wt.change2.ChangeHelper2.service
				.getChangeActivities((wt.change2.ChangeOrderIfc) pbo);

		while (qr.hasMoreElements()) {
			Persistable persistable = (Persistable) qr.nextElement();
			if (persistable instanceof WTChangeActivity2) {
				WTChangeActivity2 caifc = (WTChangeActivity2) persistable;
				QueryResult qrObj = wt.change2.ChangeHelper2.service
						.getChangeablesAfter((wt.change2.ChangeActivityIfc) caifc);
				while (qrObj.hasMoreElements()) {
					Object obj = qrObj.nextElement();
					if (obj instanceof WTDocument) {
						WTDocument wt = (WTDocument) obj;
						System.out.println("����WTDoc����ӵ�����,������" + wt.toString());
						vec.add(wt);
					} else if (obj instanceof EPMDocument) {
						EPMDocument epm = (EPMDocument) obj;
						System.out.println("����EPMDoc����ӵ�����,������"
								+ epm.toString());
						vec.add(epm);
					}
				}
			}
		}
		return vec;
	}

	public static void signPBO(WTObject pbo, ObjectReference self)
			throws MaturityException, WTException {
		Vector vector = new Vector();
		Vector<String> timeVec = new Vector<String>();
		 if(pbo instanceof WTChangeOrder2){
			// 获得变更中的更改对象
			// vector.add(wte);
			// /获得受影响对象
			wt.change2.WTChangeOrder2 wtc = (wt.change2.WTChangeOrder2) pbo;
			if (wtc == null || !(wtc instanceof WTChangeOrder2)) {
			} else {
				Vector changeAfter = new Vector();

				QueryResult qrActivities = ChangeHelper2.service.getChangeActivities((WTChangeOrder2) wtc);

				// //
				while (qrActivities.hasMoreElements()) {
					Object objActivities = qrActivities.nextElement();

					if (objActivities instanceof WTChangeActivity2) {
						QueryResult qrAfter = ChangeHelper2.service
								.getChangeablesAfter((WTChangeActivity2) objActivities);

						while (qrAfter.hasMoreElements()) {

							WTObject objAfter = (WTObject) qrAfter.nextElement();
							// ////////过滤只获得wtdocument、epmdocument
							if (objAfter instanceof WTDocument) {
								vector.add(objAfter);
							}
							if (objAfter instanceof EPMDocument) {
								vector.add(objAfter);
							}
							if (objAfter instanceof WTPart) {
								vector.add(objAfter);
							}
						}
					}
				}

			}
		}
		signECN(vector, self, timeVec,pbo);
	}
	
	public static String getTime(){
		java.text.DateFormat df = java.text.DateFormat.getDateInstance(
				java.text.DateFormat.LONG, java.util.Locale.CHINA);
		
		java.util.Date date = new java.util.Date(System.currentTimeMillis());
	   ////
		java.text.DateFormat   formatter   =   new   SimpleDateFormat( "yyyy.MM.dd"); 
		String   s = formatter.format(date);

		String r=s.replace("-", ".");
		return r;
	}
	public static String downloadpdf(Representable doc) throws WTException,
	PropertyVetoException, IOException {
		  if (doc != null) { 
			  ContentHolder contentHolder = ContentHelper.service.getContents((ContentHolder) doc);
			  ContentItem contentitem =  ContentHelper.getPrimary((FormatContentHolder) contentHolder);

			  ApplicationData applicationdata = (ApplicationData)contentitem;
			  InputStream in = ContentServerHelper.service
			  	.findContentStream(applicationdata);
			  String filename = applicationdata.getFileName();
			  if (filename.indexOf(".xls") > 0||filename.indexOf(".xlsx") > 0||filename.indexOf(".XLS") > 0||filename.indexOf(".XLSX") > 0) {
				  
			      String absoluteFileName = path + filename;
			      downloadFile(in, absoluteFileName);
			      return absoluteFileName;
			  }
		  }
		  return null;
	 	}
	 public static void downloadFile(InputStream in, String path)
		throws FileNotFoundException {
	// String absoluteFileName = getTempPath() + fileName; //����˵������ַ+�ļ���
	FileOutputStream outputStream1 = new FileOutputStream(path);
	InputStream inputStream = in;
	byte[] buffer = new byte[1024];
	int len;
	try {
		while ((len = inputStream.read(buffer)) > 0) {
			outputStream1.write(buffer, 0, len);
		}
		
	} catch (IOException e) {
		e.printStackTrace(); // To change body of catch statement use
								// File | Settings | File Templates.
	}
	System.out.println("download is completed!");
	 }
		//判断文件是否处于检出状态,若有检出的,则返回 
	public static String CheckOut(WTObject obj) {
		String errorlog="";
		try {
			if (WorkInProgressHelper.isCheckedOut((Workable) obj)) {
				errorlog+="对象"+obj.getIdentity()+"处于检出状态,因此不能提交.";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorlog;
	}
	/**
	 * 更新升级请求内物件设置为升级对象
	 * @param pbo
	 * @param self
	 * @throws MaturityException
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static void updatePromotionTargets(WTObject pbo) throws MaturityException, WTException, WTPropertyVetoException
	{
		Transaction trx = new Transaction();
		boolean access = SessionServerHelper.manager.setAccessEnforced(false);
		try
		{
			
			trx.start();
			WTSet new_set = new WTHashSet();
			WTSet new_set2 = new WTHashSet();
			if(pbo instanceof PromotionNotice)
			{
				PromotionNotice pn = (PromotionNotice) pbo;
				MaturityBaseline baseline = pn.getConfiguration();
				wt.fc.QueryResult qr = wt.maturity.MaturityHelper.service.getPromotionTargets(pn);
				while (qr.hasMoreElements()) 
				{
					wt.fc.WTObject obj = (wt.fc.WTObject) qr.nextElement();
					new_set.add(obj);
				}
				wt.fc.QueryResult qr2 = wt.maturity.MaturityHelper.service.getBaselineItems(pn);
				while (qr2.hasMoreElements()) 
				{
					wt.fc.WTObject obj = (wt.fc.WTObject) qr2.nextElement();
					new_set2.add(obj);
				}
				MaturityHelper.service.deletePromotionTargets(pn, new_set);
				BaselineHelper.service.removeFromBaseline(new_set2, baseline);
				
				MaturityHelper.service.savePromotionTargets(pn, new_set2);
				BaselineHelper.service.addToBaseline(new_set2, baseline);
				
				
				new_set = null;
				trx.commit();
				trx = null;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			trx.rollback();
			trx = null;
		}
		finally {
			SessionServerHelper.manager.setAccessEnforced(access);
		}
	}
	//获取变更的小类
	public static String getType(WTObject obj) throws WTException, UnsupportedEncodingException {
		String softAttr="";
		if (obj instanceof wt.change2.WTChangeOrder2) {
			WTChangeOrder2 eco = (WTChangeOrder2)obj;
			IBAUtility ibUtil = new IBAUtility((wt.iba.value.IBAHolder) eco);
			softAttr = ibUtil.getIBAValue("GENGGAILEIXING");
		}
		return softAttr;
	}
	 //将%E4%BD%A0转换为汉字 
    public static String unescape(String s) {
        StringBuffer sbuf = new StringBuffer();
        int l = s.length();
        int ch = -1;
        int b, sumb = 0;
        for (int i = 0, more = -1; i < l; i++) {
            /* Get next byte b from URL segment s */
            switch (ch = s.charAt(i)) {
            case '%':
                ch = s.charAt(++i);
                int hb = (Character.isDigit((char) ch) ? ch - '0'
                        : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                ch = s.charAt(++i);
                int lb = (Character.isDigit((char) ch) ? ch - '0'
                        : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                b = (hb << 4) | lb;
                break;
            case '+':
                b = ' ';
                break;
            default:
                b = ch;
            }
            /* Decode byte b as UTF-8, sumb collects incomplete chars */
            if ((b & 0xc0) == 0x80) { // 10xxxxxx (continuation byte)   
                sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb   
                if (--more == 0)
                    sbuf.append((char) sumb); // Add char to sbuf   
            } else if ((b & 0x80) == 0x00) { // 0xxxxxxx (yields 7 bits)   
                sbuf.append((char) b); // Store in sbuf   
            } else if ((b & 0xe0) == 0xc0) { // 110xxxxx (yields 5 bits)   
                sumb = b & 0x1f;
                more = 1; // Expect 1 more byte   
            } else if ((b & 0xf0) == 0xe0) { // 1110xxxx (yields 4 bits)   
                sumb = b & 0x0f;
                more = 2; // Expect 2 more bytes   
            } else if ((b & 0xf8) == 0xf0) { // 11110xxx (yields 3 bits)   
                sumb = b & 0x07;
                more = 3; // Expect 3 more bytes   
            } else if ((b & 0xfc) == 0xf8) { // 111110xx (yields 2 bits)   
                sumb = b & 0x03;
                more = 4; // Expect 4 more bytes   
            } else /*if ((b & 0xfe) == 0xfc)*/{ // 1111110x (yields 1 bit)   
                sumb = b & 0x01;
                more = 5; // Expect 5 more bytes   
            }
            /* We don't test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString();
    }
    public static Map getUserGroup(Properties properties){
		 Map user_group = new HashMap();
		 try{
	     	Set grp = zuMap.keySet();;
	     	List<String> group_names = new ArrayList();
	     	group_names.addAll(grp);
		 	QuerySpec qs = new QuerySpec(WTGroup.class);
          SearchCondition sc = new SearchCondition(WTGroup.class, WTGroup.NAME,
                  SearchCondition.LIKE, "%接收组%");
          qs.appendWhere(sc);
          
          System.out.println("zyj--test--group.size:"+group_names.size());
          for(int i=0;i<group_names.size();i++){
	            sc = new SearchCondition(WTGroup.class, WTGroup.NAME,
	                    SearchCondition.EQUAL, group_names.get(i));
	            qs.appendOr();
	            qs.appendWhere(sc);
          }
          QueryResult qr = PersistenceHelper.manager.find(qs);
          
	        while(qr.hasMoreElements()){
	        	WTGroup wtgroup = (WTGroup)qr.nextElement();
	        	Enumeration em = OrganizationServicesHelper.manager.members(wtgroup);
	        	while(em.hasMoreElements()){
	        		Object user = em.nextElement();
	        		if(user instanceof WTUser){
	        			System.out.println("zyj==test==wtgroup.getName():"+wtgroup.getName());
	        			System.out.println("zyj==test==properties.get(wtgroup.getName()):"+zuMap.get(wtgroup.getName()));
	        			user_group.put(((WTUser)user).getAuthenticationName(),zuMap.get(wtgroup.getName()));
	        		}
	        	}
	        }
      }catch(Exception e){
      	e.printStackTrace();
      }
      return user_group;
}	
	// //获得流程节点信息
	public static Hashtable getReviews(WfProcess self, Properties properties) throws Exception {
		String str = new String();
		Hashtable revise = new Hashtable();
		Enumeration enu1 = null;
		Enumeration enumeration = null;		
		if(null == self){
			return revise;
		}
		else{
			try {
				Map user_group = getUserGroup(properties);
				QueryResult qr = NmWorkflowHelper.service.getVotingEventsForProcess(self);
		        while (qr.hasMoreElements()) {
			       WfVotingEventAudit wfvotingeventaudit = (WfVotingEventAudit) qr.nextElement();
			       String activityName = wfvotingeventaudit.getActivityName();
			
			       WTPrincipalReference wtprincipalreference = wfvotingeventaudit.getUserRef();
			       WTPrincipal wtprincipal = (WTPrincipal) wtprincipalreference.getObject();
			       String userName = null;
			       String name = null;
			       if (wtprincipal instanceof WTUser){
				     userName = ((WTUser) wtprincipal).getFullName();
				     name = ((WTUser) wtprincipal).getName();
				     
			       }else if (wtprincipal instanceof WTGroup){
				     userName = ContainerTeamHelper.getDisplayName((WTGroup) wtprincipal, null);
			}
			Timestamp timestamp = wfvotingeventaudit.getTimestamp();
			
			if(revise.get(activityName)!=null){
				Vector nameVector = (Vector)revise.get(activityName);
					nameVector.add(name + ";;" + userName.replaceAll(",","").replaceAll(" ", "")
							+"__"+timestamp+"_=_"+user_group.get(((WTUser)wtprincipal).getAuthenticationName())+"_==_"+" ");
					revise.put(activityName,nameVector);
			}else{
				Vector nameVector = new Vector();
				nameVector.add(name + ";;"+ userName.replaceAll(",","").trim().replaceAll(" ", "")
						+"__"+timestamp+"_=_"+user_group.get(((WTUser)wtprincipal).getAuthenticationName())+"_==_"+" ");
				revise.put(activityName,nameVector);
			}
		}
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
		return revise;
	}
	public static WfProcess getProcess(Persistable persistable) throws Exception{
    	Enumeration eu2 = WfEngineHelper.service.getAssociatedProcesses(persistable, WfState.OPEN_RUNNING);
    	Enumeration eu = WfEngineHelper.service.getAssociatedProcesses(persistable, WfState.CLOSED_COMPLETED);
    	boolean isApproved = false;
    	WfProcess last_process = null;
    	WfProcess process = null;
    	if(eu.hasMoreElements()){
    		process = (WfProcess)eu.nextElement();
    		return process;
    	}
    	if (persistable instanceof WTChangeOrder2) {
    		 WTChangeOrder2 ecn = (WTChangeOrder2) persistable;
    		 QueryResult qrAfter = ChangeHelper2.service.getChangeablesAfter(ecn,false);
             ChangeRecord2 cr2 = new ChangeRecord2();
             while(qrAfter != null && qrAfter.hasMoreElements()){
            	 Persistable p = (Persistable) qrAfter.nextElement();
                 if(p instanceof ChangeRecord2){
                	 cr2 = (ChangeRecord2)p;
                	 ChangeableIfc cb1 = cr2.getChangeable2();
                	 if(cb1 instanceof WTDocument){
                		 WTDocument doc = (WTDocument)cb1;
                		 if(doc.getLifeCycleState().toString().equals("APPROVED")){
                			 isApproved = true;
                			 break;
                		 }
                	 }
                 }
             }
    	    	if(eu2.hasMoreElements() && isApproved){
    	    	      process = (WfProcess)eu2.nextElement();
    	    	      return process;
    	    	}
    	}
		return process;
	}
}