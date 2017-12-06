package ext.yhzc.report;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.SyncedWithCADStatus;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartUsageLink;
import wt.pom.Transaction;
import wt.pom.WTConnection;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;

public class ModifyEPMObject implements RemoteAccess {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
        server.setUserName("wcadmin");
        server.setPassword("wcadmin");
        Class[] classes = { String.class};
        Object[] objs = { args[0]};
        try {
            server.invoke("modifyEPMNum", ModifySystemObject.class.getName(), null, classes, objs);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
	}
	
	/**、
	 * 修改EPM编号
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void modifyEPMNum(String fileName) throws WTPropertyVetoException, WTException, IOException {
		WTProperties props = WTProperties.getLocalProperties();
		// TemplateFile是模板文件
		String readFile = props.getProperty("wt.home") + File.separatorChar + "codebase"
				+ File.separatorChar + "temp" + File.separatorChar + fileName;
		//读取数据
		XSSFWorkbook wb = ExportUtil.readFile(readFile);
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row = null;
		String newNum = "";
		String oldNum = "";
		EPMDocumentMaster m = null;
		EPMDocumentMasterIdentity it = null;
		EPMDocument epm = null;
		if(sheet != null){
			System.out.println("lastrowNum:"+sheet.getLastRowNum());
			for(int i=6;i<=sheet.getLastRowNum();i++){
				row = sheet.getRow(i);
				newNum = row.getCell(0).getStringCellValue();
				oldNum = row.getCell(2).getStringCellValue();
				System.out.println("新旧编码："+newNum+"+"+oldNum);
				epm = (EPMDocument) ModifySystemObject.getLatestPersistableByNumber(oldNum,EPMDocument.class);
				if(epm != null){
					//设置属性和修改编码
					System.out.println(epm.getNumber()+"进来了，");
					m = (EPMDocumentMaster) epm.getMaster();
					if(m != null){
						it = (EPMDocumentMasterIdentity) m.getIdentificationObject();
						it.setNumber(newNum);
						IdentityHelper.service.changeIdentity(m, it);
					}
				}else{
					System.out.println("修改编码失败，此编码"+oldNum+"没有找到epm对象");
				}
			}
		}
	}

}
