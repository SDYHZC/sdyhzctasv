package ext.yhzc.report;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.config.LatestConfigSpec;
import ext.util.IBAUtility;

public class ExportSystemObject {
	/**
	 * 导出系统中所有部件
	 * @param nb
	 * @return
	 */
	public static String exportAllPart(){
//		FormResult fr = new FormResult(FormProcessingStatus.SUCCESS);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		String tmpStr = sdf.format(new Date());
		String outFile = "wtpart-"+tmpStr+".xlsx";
		try {
			WTProperties pro = WTProperties.getLocalProperties();
			//从模板生成新的EXCEL
			String outFilePath = ExportUtil.copyExcelMode("exportParts.xlsx",outFile);
			//写入数据
			XSSFWorkbook wb = ExportUtil.readFile(outFilePath);
			XSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "wtpart");
			//查询并写入数据
			//start 查询
			QuerySpec qs = new QuerySpec(WTPart.class);
			qs.appendOrderBy(new OrderBy(new ClassAttribute(WTPart.class, WTPart.NAME), false),new int[]{0});
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);
			System.out.println("part size:"+qr.size());
			int v_row = 0;
			String name = "";
			while(qr.hasMoreElements()){
				WTPart pt = (WTPart) qr.nextElement();
				XSSFRow row = hs.getRow(v_row+6);
				if(row == null){
					row = hs.createRow(v_row+6);
				}
				name = pt.getName();
//				System.out.println("name:"+name);
				if(name.indexOf("#") ==-1 || "".equals(name) || name ==null){
					row.createCell(0).setCellValue(name);
				}else{
					String n = name.substring(0,name.indexOf("#"));
					if("".equals(n) || n ==null || "null".equals(n)){
						row.createCell(0).setCellValue("");
					}else
					row.createCell(0).setCellValue(n.trim());
				}
				row.createCell(1).setCellValue(pt.getType());
				row.createCell(2).setCellValue(pt.getNumber());
				row.createCell(3).setCellValue(pt.getName());
				row.createCell(4).setCellValue(pt.isEndItem());
				row.createCell(5).setCellValue(pt.getDefaultTraceCode().toString());
				row.createCell(6).setCellValue(pt.getGenericType().toString());
				row.createCell(7).setCellValue(pt.getPartType().toString());
				row.createCell(8).setCellValue(pt.getLocation());
				row.createCell(9).setCellValue(pt.getVersionIdentifier().getValue()+pt.getIterationIdentifier().getValue());
				row.createCell(10).setCellValue(pt.getViewName());
				row.createCell(11).setCellValue(pt.getLifeCycleState().toString());
				row.createCell(12).setCellValue(pt.getSource().toString());
				row.createCell(13).setCellValue(pt.getDefaultUnit().getDisplay());
				row.createCell(14).setCellValue(pt.isCollapsible());
				//IBA属性
				IBAUtility iba = new IBAUtility(pt);
				row.createCell(15).setCellValue(iba.getIBAValue("CMAT"));
				row.createCell(16).setCellValue(iba.getIBAValue("CSPE"));
				row.createCell(17).setCellValue(iba.getIBAValue("CGRA"));
				row.createCell(18).setCellValue(iba.getIBAValue("LJLB"));
				row.createCell(19).setCellValue(iba.getIBAValue("BZ"));
				row.createCell(20).setCellValue(iba.getIBAValue("TUFU"));
				row.createCell(21).setCellValue(iba.getIBAValue("CMASS"));
				row.createCell(22).setCellValue(new ReferenceFactory().getReferenceString(pt.getMaster()));
				v_row++;
			}
			
			FileOutputStream out = new FileOutputStream(outFilePath);
			wb.write(out);
			out.close();
			
//			downloadFile.deleteOnExit();
		/*	if (downloadFile != null) {
	            String url = NmObjectHelper.constructOutputURL(downloadFile, downloadFile.getName()).toExternalForm();
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("exportListUtilities.finishServerExport('"
	                    + url + "')"));
	        }else {
	            fr.setStatus(FormProcessingStatus.FAILURE);
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("top.finishServerSideExport();"));
	        }*/
		} catch (IOException e) {
			System.out.println("find IOexception");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("find WTexception");
			e.printStackTrace();
		}
		return outFile;
	}
	
	/**
	 * 导出系统中所有部件
	 * @param nb
	 * @return
	 */
	public static String exportAllEPM(){
//		FormResult fr = new FormResult(FormProcessingStatus.SUCCESS);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		String tmpStr = sdf.format(new Date());
		String outFile = "EPMDocument-"+tmpStr+".xlsx";
		try {
			WTProperties pro = WTProperties.getLocalProperties();
			//从模板生成新的EXCEL
			String outFilePath = ExportUtil.copyExcelMode("exportParts.xlsx",outFile);
			//写入数据
			XSSFWorkbook wb = ExportUtil.readFile(outFilePath);
			XSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "EPMDocument");
			//查询并写入数据
			//start 查询
			QuerySpec qs = new QuerySpec(EPMDocument.class);
			qs.appendOrderBy(new OrderBy(new ClassAttribute(EPMDocument.class, EPMDocument.NAME), false),new int[]{0});
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);
			System.out.println("EPM size:"+qr.size());
			int v_row = 0;
			String name = "";
			while(qr.hasMoreElements()){
				EPMDocument epm = (EPMDocument) qr.nextElement();
				XSSFRow row = hs.getRow(v_row+6);
				if(row == null){
					row = hs.createRow(v_row+6);
				}
				name = epm.getName();
//				System.out.println("name:"+name);
				if(name.indexOf("#") ==-1 || "".equals(name) || name ==null){
					row.createCell(0).setCellValue(name);
				}else{
					String n = name.substring(0,name.indexOf("#"));
					if("".equals(n) || n ==null || "null".equals(n)){
						row.createCell(0).setCellValue("");
					}else
					row.createCell(0).setCellValue(n.trim());
				}
				if(epm.getNumber().toUpperCase().contains("_SKEL")){
					row.getCell(0).setCellValue("");
				}
				row.createCell(1).setCellValue(epm.getType());
				row.createCell(2).setCellValue(epm.getNumber());
				row.createCell(3).setCellValue(epm.getName());
				row.createCell(4).setCellValue(epm.isCollapsible());
//				row.createCell(5).setCellValue(epm.getDefaultTraceCode().toString());
				row.createCell(6).setCellValue(epm.getGenericType().toString());
//				row.createCell(7).setCellValue(epm.getPartType().toString());
				row.createCell(8).setCellValue(epm.getLocation());
				row.createCell(9).setCellValue(epm.getVersionIdentifier().getValue()+epm.getIterationIdentifier().getValue());
//				row.createCell(10).setCellValue(epm.getViewName())
				row.createCell(11).setCellValue(epm.getLifeCycleState().toString());
//				row.createCell(12).setCellValue(epm.getSource().toString());
				row.createCell(13).setCellValue(epm.getDefaultUnit().getDisplay());
				row.createCell(14).setCellValue(epm.isCollapsible());
				//IBA属性
				IBAUtility iba = new IBAUtility(epm);
				row.createCell(15).setCellValue(iba.getIBAValue("CMAT"));
				row.createCell(16).setCellValue(iba.getIBAValue("CSPE"));
				row.createCell(17).setCellValue(iba.getIBAValue("CGRA"));
				row.createCell(18).setCellValue(iba.getIBAValue("LJLB"));
				row.createCell(19).setCellValue(iba.getIBAValue("BZ"));
				row.createCell(20).setCellValue(iba.getIBAValue("TUFU"));
				row.createCell(21).setCellValue(iba.getIBAValue("CMASS"));
				row.createCell(22).setCellValue(new ReferenceFactory().getReferenceString(epm.getMaster()));
				v_row++;
			}
			
			FileOutputStream out = new FileOutputStream(outFilePath);
			wb.write(out);
			out.close();
			
//			downloadFile.deleteOnExit();
		/*	if (downloadFile != null) {
	            String url = NmObjectHelper.constructOutputURL(downloadFile, downloadFile.getName()).toExternalForm();
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("exportListUtilities.finishServerExport('"
	                    + url + "')"));
	        }else {
	            fr.setStatus(FormProcessingStatus.FAILURE);
	            fr.setNextAction(FormResultAction.JAVASCRIPT);
	            fr.setJavascript(HTMLEncoder.encodeForJavascript("top.finishServerSideExport();"));
	        }*/
		} catch (IOException e) {
			System.out.println("find IOexception");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("find WTexception");
			e.printStackTrace();
		}
		return outFile;
	}
	
	/**
	 * 导出系统中所有部件去检查
	 * @param nb
	 * @return
	 */
	public static String exportAllPartToCheck(){
//		FormResult fr = new FormResult(FormProcessingStatus.SUCCESS);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		String tmpStr = sdf.format(new Date());
		String outFile = "wtpartCheck-"+tmpStr+".xlsx";
		try {
			WTProperties pro = WTProperties.getLocalProperties();
			//从模板生成新的EXCEL
			String outFilePath = ExportUtil.copyExcelMode("exportPartsCheck.xlsx",outFile);
			//写入数据
			XSSFWorkbook wb = ExportUtil.readFile(outFilePath);
			XSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "wtpart");
			//查询并写入数据
			//start 查询
			QuerySpec qs = new QuerySpec(WTPart.class);
			qs.appendOrderBy(new OrderBy(new ClassAttribute(WTPart.class, WTPart.NAME), false),new int[]{0});
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);
			System.out.println("part size:"+qr.size());
			int v_row = 0;
			String name = "";
			String parentNum = "";
			WTPart parent = null;
			String version = "";
			while(qr.hasMoreElements()){
				WTPart pt = (WTPart) qr.nextElement();
				XSSFRow row = hs.getRow(v_row+6);
				if(row == null){
					row = hs.createRow(v_row+6);
				}
				name = pt.getName();
//				System.out.println("name:"+name);
				if(name.indexOf("#") ==-1 || "".equals(name) || name ==null){
					row.createCell(0).setCellValue(name);
				}else{
					String n = name.substring(0,name.indexOf("#"));
					if("".equals(n) || n ==null || "null".equals(n)){
						row.createCell(0).setCellValue("");
					}else
						row.createCell(0).setCellValue(n.trim());
				}
				row.createCell(1).setCellValue(pt.getType());
				row.createCell(2).setCellValue(pt.getNumber());
				row.createCell(3).setCellValue(pt.getName());
				row.createCell(4).setCellValue(pt.isEndItem());
				row.createCell(5).setCellValue(pt.getDefaultTraceCode().toString());
				row.createCell(6).setCellValue(pt.getGenericType().toString());
				row.createCell(7).setCellValue(pt.getPartType().toString());
				row.createCell(8).setCellValue(pt.getLocation());
				row.createCell(9).setCellValue(pt.getVersionIdentifier().getValue()+pt.getIterationIdentifier().getValue());
				row.createCell(10).setCellValue(pt.getViewName());
				row.createCell(11).setCellValue(pt.getLifeCycleState().toString());
				row.createCell(12).setCellValue(pt.getSource().toString());
				row.createCell(13).setCellValue(pt.getDefaultUnit().getDisplay());
				row.createCell(14).setCellValue(pt.isCollapsible());
				//IBA属性
				IBAUtility iba = new IBAUtility(pt);
				row.createCell(15).setCellValue(iba.getIBAValue("CMAT"));
				row.createCell(16).setCellValue(iba.getIBAValue("CSPE"));
				row.createCell(17).setCellValue(iba.getIBAValue("CGRA"));
				row.createCell(18).setCellValue(iba.getIBAValue("LJLB"));
				row.createCell(19).setCellValue(iba.getIBAValue("BZ"));
				row.createCell(20).setCellValue(iba.getIBAValue("TUFU"));
				row.createCell(21).setCellValue(iba.getIBAValue("CMASS"));
				QueryResult re = WTPartHelper.service.getUsedByWTParts((WTPartMaster) pt.getMaster());
				parentNum = "";
				version = "";
				while(re.hasMoreElements()){
					parent = (WTPart) qr.nextElement();
					version += parent.getVersionIdentifier().getValue()+parent.getIterationIdentifier().getValue();
					parentNum += parent.getNumber();
				}
				row.createCell(22).setCellValue(version);
				row.createCell(23).setCellValue(parentNum);
				v_row++;
			}
			
			FileOutputStream out = new FileOutputStream(outFilePath);
			wb.write(out);
			out.close();
		} catch (IOException e) {
			System.out.println("find IOexception in check");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("find WTexception in check");
			e.printStackTrace();
		}
		return outFile;
	}
	
	/**
	 * 导出系统中所有图档去检查
	 * @param nb
	 * @return
	 */
	public static String exportAllEPMToCheck(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmss");
		String tmpStr = sdf.format(new Date());
		String outFile = "epmCheck-"+tmpStr+".xlsx";
		try {
			WTProperties pro = WTProperties.getLocalProperties();
			//从模板生成新的EXCEL
			String outFilePath = ExportUtil.copyExcelMode("exportPartsCheck.xlsx",outFile);
			//写入数据
			XSSFWorkbook wb = ExportUtil.readFile(outFilePath);
			XSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "epm");
			//查询并写入数据
			//start 查询
			QuerySpec qs = new QuerySpec(EPMDocument.class);
			qs.appendOrderBy(new OrderBy(new ClassAttribute(EPMDocument.class, EPMDocument.NAME), false),new int[]{0});
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);
			System.out.println("epm size:"+qr.size());
			int v_row = 0;
			String name = "";
			while(qr.hasMoreElements()){
				EPMDocument epm = (EPMDocument) qr.nextElement();
				XSSFRow row = hs.getRow(v_row+6);
				if(row == null){
					row = hs.createRow(v_row+6);
				}
				name = epm.getName();
//				System.out.println("name:"+name);
				if(name.indexOf("#") ==-1 || "".equals(name) || name ==null){
					row.createCell(0).setCellValue(name);
				}else{
					String n = name.substring(0,name.indexOf("#"));
					if("".equals(n) || n ==null || "null".equals(n)){
						row.createCell(0).setCellValue("");
					}else
					row.createCell(0).setCellValue(n.trim());
				}
				if(epm.getNumber().toUpperCase().contains("_SKEL")){
					row.getCell(0).setCellValue("");
				}
				row.createCell(1).setCellValue(epm.getType());
				row.createCell(2).setCellValue(epm.getNumber());
				row.createCell(3).setCellValue(epm.getName());
//				row.createCell(4).setCellValue(epm.isEndItem());
//				row.createCell(5).setCellValue(epm.getDefaultTraceCode().toString());
				row.createCell(6).setCellValue(epm.getGenericType().toString());
//				row.createCell(7).setCellValue(epm.getPartType().toString());
				row.createCell(8).setCellValue(epm.getLocation());
				row.createCell(9).setCellValue(epm.getVersionIdentifier().getValue()+epm.getIterationIdentifier().getValue());
//				row.createCell(10).setCellValue(epm.getViewName());
				row.createCell(11).setCellValue(epm.getLifeCycleState().toString());
//				row.createCell(12).setCellValue(epm.getSource().toString());
				row.createCell(13).setCellValue(epm.getDefaultUnit().getDisplay());
				row.createCell(14).setCellValue(epm.isCollapsible());
				//IBA属性
				IBAUtility iba = new IBAUtility(epm);
				row.createCell(15).setCellValue(iba.getIBAValue("CMAT"));
				row.createCell(16).setCellValue(iba.getIBAValue("CSPE"));
				row.createCell(17).setCellValue(iba.getIBAValue("CGRA"));
				row.createCell(18).setCellValue(iba.getIBAValue("LJLB"));
				row.createCell(19).setCellValue(iba.getIBAValue("BZ"));
				row.createCell(20).setCellValue(iba.getIBAValue("TUFU"));
				row.createCell(21).setCellValue(iba.getIBAValue("CMASS"));
				row.createCell(22).setCellValue(epm.getVersionIdentifier().getValue()+epm.getIterationIdentifier().getValue());
//				row.createCell(23).setCellValue(parentNum);
				v_row++;
			}
			
			FileOutputStream out = new FileOutputStream(outFilePath);
			wb.write(out);
			out.close();
		} catch (IOException e) {
			System.out.println("find IOexception in check");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("find WTexception in check");
			e.printStackTrace();
		}
		return outFile;
	}
}
