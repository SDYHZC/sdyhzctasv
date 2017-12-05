package ext.yhzc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import ext.util.IBAUtility;
import ext.yhzc.tools.PublicLogger;

public class EPMNumberSplit {
	public static PublicLogger Logger = new PublicLogger();
	private static Map<String, String> type_resource_map = new HashMap<String, String>();
	private static String outFilePath;
	private static DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");  
	static{
	        Logger.setStrAbsolutePath("d:\\log.log");
	        Logger.setBPrint(true);
	        Logger.setBlog(true);
	        outFilePath = "d:\\temp";
	        type_resource_map.put("1", "EPM导出");
	        type_resource_map.put("2", "EPM");
	}
	public static void main(String[] args) {
		do{
	        Logger.println("ExportMain:", "******************************************************************");
	        Logger.println("ExportMain:", "*   输入序号选择数据导出的类型：");
	        Logger.println("ExportMain:", "*   1、EPM导出");
	        Logger.println("ExportMain:", "******************************************************************");
	        Scanner scanner;
	        String select = "";
	        while(!select.equals("1")){
	            System.out.print("请输入：");
	            scanner = new Scanner(System.in);
	            select = scanner.next();
	            if(!select.equals("1")){
	                Logger.println("ExportMain:", "输入值‘"+select+"’错误，请输入1~6选项");
	                continue;
	            }
	        }
	        while(true){   
	            System.out.print("请输入输出路径：");
	            scanner = new Scanner(System.in);
	            outFilePath = scanner.next();
	            File downloadFile=new File(outFilePath);	
	            if(!downloadFile.exists()){	
	                Logger.println("ExportMain:", "输入路径错误!");
	                continue;
	            }else{
	            	break;
	            }
	        }
            outFilePath = outFilePath +File.separator+ "EPMDocument"+sdf.format(new Date());
            downloadFile=new File(outFilePath);	
			HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(downloadFile));
			HSSFSheet hs = wb.getSheetAt(0);
			wb.setSheetName(0, "epmdoc");
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
	        }
	        Logger.println("ExportMain:", "您选择的导出:"+type_resource_map.get(select));
	        Logger.println("ExportMain:", "******************************************************************");
		}while(true);
	}
	
	/**
     * 根据对象的number找到最新版本的对象
     *
     * @param number    要查询的对象的编号
     * @param thisClass class对象
     * @return 由number标识的最新版本对象
     */
    public static List<Persistable> getAllLatestObject(Class thisClass) {
        List<Persistable> list = new ArrayList<Persistable>();
        Persistable persistable = null;
        try {
            int[] index = {0};
            QuerySpec qs = new QuerySpec(thisClass);

            QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

            LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);

            while (qr != null && qr.hasMoreElements()) {
                persistable = (Persistable) qr.nextElement();
                list.add(persistable);
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public static String getDateString(Timestamp timestamp){
        String return_String = "";
        
        try {  
            Calendar cal=Calendar.getInstance();
            cal.setTime(timestamp);
            //cal.add(Calendar.HOUR, -8);
            return_String = sdf.format(cal.getTime());  
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return return_String;
    }
}
