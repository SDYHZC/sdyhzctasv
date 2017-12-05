package ext.pdf;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.log4j.LogR;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTUser;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTStandardDateFormat;
import wt.vc.VersionControlHelper;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfRequesterActivity;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WfAssignment;
import wt.workflow.work.WfBallot;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;

public class SignBaobiao {
	static float hqspace = 5.5F;

	static float cc = 2.834381F;

	static float cy = 2.835017F;
	private static String path;
	private static String wthome;
 
	private static final Logger logger = LogR.getLogger(SignBaobiao.class.getName());

	static
	{

		try
		{
			WTProperties wtproperties = WTProperties.getLocalProperties();
			wthome = wtproperties.getProperty("wt.home");
			path = wthome + "/temp/";
		}
		catch (Throwable throwable)
		{
			throw new ExceptionInInitializerError(throwable);
		}
	}

	public static void signPBO(WTObject pbo, ObjectReference self) throws   WTException
	{
		Vector vector = new Vector();
	    if ((pbo instanceof WTChangeOrder2))
		{
			WTChangeOrder2 wtc = (WTChangeOrder2) pbo;
			logger.debug("  signPBO  WTChangeOrder2=="+wtc.getDisplayIdentifier());
			if ((wtc != null) && ((wtc instanceof WTChangeOrder2)))
			{
				Vector changeAfter = new Vector();
				QueryResult qrActivities = ChangeHelper2.service.getChangeActivities(wtc);
				while (qrActivities.hasMoreElements())
				{
					Object objActivities = qrActivities.nextElement();
					if ((objActivities instanceof WTChangeActivity2))
					{
						QueryResult qrAfter = ChangeHelper2.service.getChangeablesAfter((WTChangeActivity2) objActivities);
						while (qrAfter.hasMoreElements())
						{
							WTObject objAfter = (WTObject) qrAfter.nextElement();
							if ((objAfter instanceof WTDocument))
							{
								vector.add(objAfter);
							}
						}
					}
				}
			}

		}
		signPdf(vector, self );
	}
	public static void signPdf(Vector<WTObject> vector, ObjectReference self ) throws WTException
	{
		if (vector != null)
		{
			int size = vector.size();
			for (int i = 0; i < size; i++)
			{
				WTObject object = (WTObject) vector.get(i);
				if ((object instanceof WTDocument))
				{
					WTDocument wtDocument = (WTDocument) object;
					logger.debug("  signPBO  wtDocument=="+wtDocument.getDisplayIdentifier());
					String typename = getSoftType(wtDocument);
					logger.debug("  signPBO  typename=="+typename);
					if ((typename != null) && (typename.length() >0))
					{
						if (typename.endsWith(".bzjmxb") || typename.endsWith(".wgjmxb") || typename.endsWith(".zcmxb") || typename.endsWith(".fzmxb"))
							changePdfRevise(wtDocument, self);
					}					
				}
			}
		}
	}

	public static void changePdfRevise(WTDocument wtdoc, ObjectReference self) throws WTException
	{
		try
		{
			if (wtdoc != null)
			{
				logger.debug("  changePdfRevise ===" + wtdoc.getDisplayIdentifier());
				String version = VersionControlHelper.getVersionIdentifier(wtdoc).getValue();
				// 下载可视化中的pdf
				ApplicationData pdfapp   = getdownloadpdf(wtdoc);
				logger.debug("  changePdfRevise getdownloadpdf  ===" + pdfapp);
				if (pdfapp == null)
					throw new WTException("  Representation PDF is null!");
				Hashtable revise = getReviews(self);
				Hashtable reset = new Hashtable();
				reset = resetWF(revise);
				logger.debug("  changePdfRevise getReviews  ===" + reset);
				File printfile=writeText(reset, pdfapp, wtdoc);
				logger.debug("  changePdfRevise printfile  ===" + printfile);

				if(printfile!=null)
				{
					String fullpath=printfile.getAbsolutePath();
					logger.debug("  changePdfRevise fullpath  ===" + fullpath);
					logger.debug("  changePdfRevise uploadpdf ");
					docUtil.uploadpdf(wtdoc, fullpath);
					logger.debug("  changePdfRevise uploadpdf final ");
 					printfile.delete();
					logger.debug("  changePdfRevise delete  "+fullpath);
				}
			}
		}
		catch (Exception e)
		{
			throw new WTException(e);
		}
	}
	public static File writeText(Hashtable htrevise, ApplicationData pdfapp,WTDocument wtdoc) throws WTException
	{
		File tempFile =null;
		try
		{
			String filetype = getSoftType(wtdoc);
			Hashtable hts = InitXml();
			String writeType = null;
			logger.debug(" writeText filetype==="+ filetype);
			if(filetype.endsWith(".bzjmxb")||filetype.endsWith(".wgjmxb")||filetype.endsWith(".zcmxb")||filetype.endsWith(".fzmxb"))
			{
 				writeType=filetype.substring(filetype.lastIndexOf(".")+1,filetype.length());
			}
			logger.debug(" writeText writeType==="+ writeType);
			if (writeType == null)
				return tempFile;
			Hashtable htf = (Hashtable) hts.get(writeType);
		    InputStream is = ContentServerHelper.service.findContentStream(pdfapp);
			PdfReader reader = new PdfReader(is);
			int pageNumber = reader.getNumberOfPages();
			String filename = pdfapp.getFileName();
	        filename = unescape(filename);
			//String version=VersionControlHelper.getVersionIdentifier(wtdoc).getValue();
			//String pdfpath=  path +version+"_"+filename;
	        String pdfpath=  path  +filename;
			tempFile= new File(pdfpath);
			logger.debug(" writeText pdfpath==="+ pdfpath);
			PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(tempFile));
			logger.debug(" writeText  getNumberOfPages " + pageNumber);
			BaseFont bf = BaseFont.createFont(wthome + "/codebase/ext/pdf/simsun.ttc,1", "Identity-H", true);
			for (int pageNo = 0; pageNo < pageNumber; pageNo++)
			{
				PdfContentByte cb = stamp.getOverContent(pageNo+1);
				cb.setFontAndSize(bf, 12.0F);
				Hashtable htp = (Hashtable) htf.get(String.valueOf(pageNo+1));
				if (htp != null)
				  writeText(cb, bf, htp, htrevise , filetype);
				else 
				{
					Hashtable htpOthers = (Hashtable) htf.get("others");
					if (htpOthers != null)
						writeText(cb, bf, htpOthers, htrevise , filetype);
				}
			}
			stamp.close();
		}
		catch (Exception e)
		{
			if(tempFile!=null&&tempFile.exists())
			{
				logger.debug(" writeText Exception And delete  "+tempFile.getAbsolutePath());
				tempFile.delete();
			}
			throw new WTException(e);
		}
		return tempFile;
	}

	private static void writeText(PdfContentByte cb, BaseFont bf, Hashtable ht, Hashtable htrevise, String filetype) 
	{
		Enumeration keys = ht.keys();
 		while (keys.hasMoreElements())
		{

			String key = (String) keys.nextElement();
 			logger.debug("  config keys:" + key);
			Hashtable section = (Hashtable) ht.get(key);

			float x = Float.parseFloat((String) section.get("x"));
			float y = Float.parseFloat((String) section.get("y"));
			float datex = Float.parseFloat((String) section.get("datex"));
			float datey = Float.parseFloat((String) section.get("datey"));
			float fontsize = Float.parseFloat((String) section.get("fontsize"));
			float rotation = Float.parseFloat((String) section.get("rotation"));
 			if ((htrevise.get(key) != null) && (!"".equals(htrevise.get(key))))
			{
				String usertime = (String) htrevise.get(key);
				logger.debug("usertime:" + usertime + "=============");
				usertime = usertime.replaceAll(";;;", " ").replaceAll("&&&", " ");
				int num1 = usertime.indexOf(" ");
				String name = usertime.substring(0, num1);
 				String time = usertime.substring(usertime.length() - 10, usertime.length());
				if ((datex != 0.0F) && (datey != 0.0F))
				{
					time = time.replaceAll("-", "");
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(0, name, x * cc, y * cy, rotation);
					cb.showTextAligned(0, time, datex * cc, datey * cy, rotation);
					cb.endText();
					logger.debug("   连签================");
					logger.debug("      name=="+name+"  x="+x * cc+"  y="+y * cy);
					logger.debug("      time=="+time+"  x="+datex * cc+"  y="+datey * cy);
				}
				else
				{
					cb.beginText();
					cb.setFontAndSize(bf, fontsize);
					cb.showTextAligned(0, name, x * cc, y * cy, rotation);
					cb.endText();
					logger.debug("   非连签================");
					logger.debug("      name=="+name+"  x="+x * cc+"  y="+y * cy);
				}
			}
		}
	}
	
	private static Hashtable InitXml() throws Exception
	{
		Hashtable ht = new Hashtable();
		Hashtable ht1 = new Hashtable();
		Hashtable ht2 = new Hashtable();
		Hashtable ht3 = new Hashtable();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder bulider = factory.newDocumentBuilder();
		org.w3c.dom.Document doc = bulider.parse(wthome + "/codebase/ext/pdf/signpdf.xml");
		NodeList nl = doc.getElementsByTagName("type");

		for (int i = 0; i < nl.getLength(); i++)
		{
			Element nodeType = (Element) nl.item(i);
			String name1 = nodeType.getAttribute("name");

			NodeList n2 = nodeType.getElementsByTagName("page");
			ht1 = new Hashtable();
			for (int j = 0; j < n2.getLength(); j++)
			{
				Element nodePage = (Element) n2.item(j);
				String name2 = nodePage.getAttribute("pageno");
				NodeList n3 = nodePage.getElementsByTagName("section");
				ht2 = new Hashtable();
				for (int k = 0; k < n3.getLength(); k++)
				{
					ht3 = new Hashtable();
					Element nodeSection = (Element) n3.item(k);
					String name3 = nodeSection.getAttribute("name");
					String x = nodeSection.getElementsByTagName("x").item(0).getFirstChild().getNodeValue().trim();
					String y = nodeSection.getElementsByTagName("y").item(0).getFirstChild().getNodeValue().trim();
					String kuan = nodeSection.getElementsByTagName("chang").item(0).getFirstChild().getNodeValue().trim();
					String chang = nodeSection.getElementsByTagName("kuan").item(0).getFirstChild().getNodeValue().trim();
					String datex = nodeSection.getElementsByTagName("datex").item(0).getFirstChild().getNodeValue().trim();
					String datey = nodeSection.getElementsByTagName("datey").item(0).getFirstChild().getNodeValue().trim();
					String yearx = nodeSection.getElementsByTagName("yearx").item(0).getFirstChild().getNodeValue().trim();
					String yeary = nodeSection.getElementsByTagName("yeary").item(0).getFirstChild().getNodeValue().trim();
					String monthx = nodeSection.getElementsByTagName("monthx").item(0).getFirstChild().getNodeValue().trim();
					String monthy = nodeSection.getElementsByTagName("monthy").item(0).getFirstChild().getNodeValue().trim();
					String dayx = nodeSection.getElementsByTagName("dayx").item(0).getFirstChild().getNodeValue().trim();
					String dayy = nodeSection.getElementsByTagName("dayy").item(0).getFirstChild().getNodeValue().trim();
					String fontsize = nodeSection.getElementsByTagName("fontsize").item(0).getFirstChild().getNodeValue().trim();
					String rotation = nodeSection.getElementsByTagName("rotation").item(0).getFirstChild().getNodeValue().trim();

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
				ht1.put(name2, ht2);
			}
			ht.put(name1, ht1);
		}
		return ht;
	}




	public static Hashtable getReviews(ObjectReference selfRef) throws Exception
	{
		Hashtable revise = new Hashtable();
		Enumeration enu1 = null;
		Enumeration enumeration = null;
	 
			WfProcess self = (WfProcess) selfRef.getObject();
			enu1 = WfEngineHelper.service.getProcessSteps(self, null);
			Vector v = new Vector();
			while (enu1.hasMoreElements())
			{
				Object obj = enu1.nextElement();
				if ((obj instanceof WfAssignedActivity))
				{
					v.addElement(obj);
				}
				else if ((obj instanceof WfRequesterActivity))
				{
					WfContainer con = ((WfRequesterActivity) obj).getPerformer();
					Enumeration enu2 = null;
					enu2 = WfEngineHelper.service.getProcessSteps(con, null);
					while (enu2.hasMoreElements())
					{
						Object obj2 = enu2.nextElement();
						if ((obj2 instanceof WfAssignedActivity))
						{
							v.addElement(obj2);
						}
					}
				}
			}
			enumeration = v.elements();

			while (enumeration.hasMoreElements())
			{
				WfAssignedActivity wfactivity = (WfAssignedActivity) enumeration.nextElement();

				if ((wfactivity != null) && ((wfactivity instanceof WfAssignedActivity)))
				{
					if (wfactivity.getState().toString().equalsIgnoreCase("CLOSED_COMPLETED_EXECUTED"))
					{
						revise.put(wfactivity.getName(), getPrincipalName(wfactivity));
					}
				}
			}
		 
		return revise;
	}

	private static String getPrincipalName(WfActivity wfa) throws Exception
	{
		String field = "";
		String str = "";
		String strfull = "";
		Enumeration en1 = null;
		Enumeration en2 = null;
		en1 = ((WfAssignedActivity) wfa).getAssignments();
		String time;
		if ((wfa.getStartTime() != null) && (wfa.getEndTime() != null))
			time = WTStandardDateFormat.format(wfa.getEndTime(), "yyyy-MM-dd");
		else
		{
			time = "";
		}

		for (int i = 0; (en1 != null) && (en1.hasMoreElements()); i++)
		{
			WfAssignment wfassignment = (WfAssignment) en1.nextElement();
			en2 = wfassignment.checkBallotStatus().elements();
			for (int j = 0; (en2 != null) && (en2.hasMoreElements()); j++)
			{
				WfBallot wfballot = (WfBallot) en2.nextElement();

				WTPrincipal wtp = wfballot.getVoter().getPrincipal();

				if ((wtp instanceof WTUser))
				{
					str = ((WTUser) wtp).getName().toString();

					strfull = ((WTUser) wtp).getFullName().toString();
					if (strfull.indexOf(",") > 0)
					{
						strfull = strfull.replaceFirst(",", "");
					}
					strfull = strfull.replaceAll(" ", "");
				}
				else if ((wtp instanceof WTGroup))
				{
					str = ((WTGroup) wtp).getName().toString();
				}
				if ((str != null) && (str.length() > 0))
				{
					if (field == "")
						field = strfull + ";;;" + time;
					else
						field = field + "&&&" + str + ";;;" + time;
				}
			}
		}
		return field;
	}

	public static ApplicationData getdownloadpdf(Representable doc ) throws WTException
	{
		ApplicationData app = null;
		try
		{
			Representation representation = RepresentationHelper.service.getDefaultRepresentation(doc);
			logger.debug("   representation===:" + representation);
			if (representation == null)
				return app;
			representation = (Representation) ContentHelper.service.getContents(representation);
			logger.debug("   getContents===" + representation);
			Vector vector1 = ContentHelper.getContentList(representation);
			logger.debug("   getContentList===" + vector1);
			for (int l = 0; l < vector1.size(); l++)
			{
				ContentItem contentitem = (ContentItem) vector1.elementAt(l);
				if ((contentitem instanceof ApplicationData))
				{
					ApplicationData applicationdata = (ApplicationData) contentitem;
					logger.debug("   applicationdata===" + applicationdata);
					InputStream in = ContentServerHelper.service.findContentStream(applicationdata);
					String filename = applicationdata.getFileName();
					logger.debug("   applicationdata filename===" + filename);
					if (filename.endsWith(".pdf"))
					{
						return applicationdata;
					}
				}
			}
		}
		catch(PropertyVetoException e)
		{
			throw new WTException(e);
		}
		 
		return app;
	}

	public static String unescape(String s)
	{
		StringBuffer sbuf = new StringBuffer();
		int l = s.length();
		int ch = -1;
		int sumb = 0;
		int i = 0;
		for (int more = -1; i < l; i++)
		{
			int b;
			switch (ch = s.charAt(i))
			{
			case '%':
				ch = s.charAt(++i);
				int hb = (Character.isDigit((char) ch) ? ch - 48 : '\n' + Character.toLowerCase((char) ch) - 97) & 0xF;
				ch = s.charAt(++i);
				int lb = (Character.isDigit((char) ch) ? ch - 48 : '\n' + Character.toLowerCase((char) ch) - 97) & 0xF;
				b = hb << 4 | lb;
				break;
			case '+':
				b = 32;
				break;
			default:
				b = ch;
			}
			if ((b & 0xC0) == 128)
			{
				sumb = sumb << 6 | b & 0x3F;
				more--;
				if (more == 0)
					sbuf.append((char) sumb);
			}
			else if ((b & 0x80) == 0)
			{
				sbuf.append((char) b);
			}
			else if ((b & 0xE0) == 192)
			{
				sumb = b & 0x1F;
				more = 1;
			}
			else if ((b & 0xF0) == 224)
			{
				sumb = b & 0xF;
				more = 2;
			}
			else if ((b & 0xF8) == 240)
			{
				sumb = b & 0x7;
				more = 3;
			}
			else if ((b & 0xFC) == 248)
			{
				sumb = b & 0x3;
				more = 4;
			}
			else
			{
				sumb = b & 0x1;
				more = 5;
			}
		}
		return sbuf.toString();
	}
	public static String getSoftType(WTObject obj) throws WTException
	{
		String typename = "";
		TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
		typename = type.getTypename();
		return typename;
	}

	public static Hashtable resetWF(Hashtable ht)
	{
		Enumeration keys = ht.keys();
		Hashtable resetTable = new Hashtable();

		while (keys.hasMoreElements())
		{
			String key = (String) keys.nextElement();
			String str = (String) ht.get(key);
			String ss = null;
			if ((key.equals("编制")) || (key.equals("提交审签")) || (key.equals("提交升级")))
				ss = "设计";
			else
			{
				ss = key;
			}
			resetTable.put(ss, str);
		}
		return resetTable;
	}
 
	 
}
