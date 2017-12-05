package ext.yhzc.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.query.ClassAttribute;
import wt.query.QuerySpec;
import wt.util.HTMLEncoder;
import wt.util.WTException;
import wt.util.WTProperties;

import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.forms.FormResultAction;
import com.ptc.netmarkets.model.NmObjectHelper;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.util.IBAUtility;

public class ExportSystemObject {
	/**
	 * 导出系统中所有部件
	 * @param nb
	 * @return
	 */
	public static FormResult exportAllPart(NmCommandBean nb){
		FormResult fr = new FormResult(FormProcessingStatus.SUCCESS);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		String tmpStr = sdf.format(new Date());
		String outFile = "wtpart-"+tmpStr+".xls";
		try {
			WTProperties pro = WTProperties.getLocalProperties();
			String outFilePath = pro.getProperty("wt.home") + File.separatorChar + "codebase"+ File.separatorChar + "temp" + File.separatorChar + outFile;
			//查询并写入数据
			File downloadFile=new File(outFilePath);	
			HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(downloadFile));
			HSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "wtpart");
			//表头
			HSSFRow r4 = hs.getRow(4);
			HSSFCell c15 = r4.getCell(15);
			c15.setCellValue("材料名称");
			HSSFCell c16 = r4.getCell(16);
			c16.setCellValue("材料规格");
			HSSFCell c17 = r4.getCell(17);
			c17.setCellValue("材料牌号");
			HSSFCell c18 = r4.getCell(18);
			c18.setCellValue("零件类别");
			HSSFCell c19 = r4.getCell(19);
			c19.setCellValue("备注");
			HSSFCell c20 = r4.getCell(20);
			c20.setCellValue("图幅");
			HSSFCell c21 = r4.getCell(21);
			c21.setCellValue("质量");
			
			HSSFRow r5 = hs.getRow(5);
			String[] tab = {"重复物料图号","Type","Number","Name","End Item","Trace Code","Generic Type","Assembly Mode","Location",
							"Revision","View","State","Source","Default Unit","Gathering Part","CMAT","CSPE","CGRA","LJLB","BZ",
							"TUFU","CMASS","MasterOID","BOMReplaceBy"};
			for(int i=0;i<24;i++){
				HSSFCell c = r5.getCell(i);
				c.setCellValue(tab[i]);
			}
			//start 查询
			QuerySpec qs = new QuerySpec(WTPart.class);
			qs.appendGroupBy(new ClassAttribute(WTPart.class, WTPart.NUMBER), new int[]{0}, false);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			System.out.println("part size:"+qr.size());
			while(qr.hasMoreElements()){
				int v_row = 0;
				WTPart pt = (WTPart) qr.nextElement();
				HSSFRow row = hs.getRow(v_row+6);
				row.getCell(0).setCellValue(pt.getName().substring(0, pt.getName().indexOf("#")));
				row.getCell(1).setCellValue(pt.getType());
				row.getCell(2).setCellValue(pt.getNumber());
				row.getCell(3).setCellValue(pt.getName());
				row.getCell(4).setCellValue(pt.isEndItem());
				row.getCell(5).setCellValue(pt.getDefaultTraceCode().getDisplay());
				row.getCell(6).setCellValue(pt.getGenericType().getStringValue());
				row.getCell(7).setCellValue(pt.getBusinessType());
				row.getCell(8).setCellValue(pt.getLocation());
				row.getCell(9).setCellValue(pt.getVersionIdentifier().getValue()+pt.getIterationIdentifier().getValue());
				row.getCell(10).setCellValue(pt.getViewName());
				row.getCell(11).setCellValue(pt.getLifeCycleState().toString());
				row.getCell(12).setCellValue(pt.getSource().toString());
				row.getCell(13).setCellValue(pt.getDefaultUnit().getDisplay());
				row.getCell(14).setCellValue(pt.getPartType().getDisplay());
				//IBA属性
				IBAUtility iba = new IBAUtility(pt);
				row.getCell(15).setCellValue(iba.getIBAValue("CMAT"));
				row.getCell(16).setCellValue(iba.getIBAValue("CSPE"));
				row.getCell(17).setCellValue(iba.getIBAValue("CGRA"));
				row.getCell(18).setCellValue(iba.getIBAValue("LJLB"));
				row.getCell(19).setCellValue(iba.getIBAValue("BZ"));
				row.getCell(20).setCellValue(iba.getIBAValue("TUFU"));
				row.getCell(21).setCellValue(iba.getIBAValue("CMASS"));
				row.getCell(22).setCellValue(new ReferenceFactory().getReferenceString(pt.getMaster()));
				v_row++;
			}
			
			FileOutputStream out = new FileOutputStream(downloadFile);
			wb.write(out);
			out.close();
			
			downloadFile.deleteOnExit();
			System.out.println("downloadFile file=="+downloadFile);
			if (downloadFile != null) {
	            String url = NmObjectHelper.constructOutputURL(downloadFile, downloadFile.getName()).toExternalForm();
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("exportListUtilities.finishServerExport('"
	                    + url + "')"));
	        }else {
	            fr.setStatus(FormProcessingStatus.FAILURE);
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("top.finishServerSideExport();"));
	        }
		} catch (IOException e) {
			System.out.println("find IOexception");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("find WTexception");
			e.printStackTrace();
		}
		return fr;
	}
}
