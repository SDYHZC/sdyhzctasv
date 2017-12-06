package ext.yhzc.report;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import wt.auth.SimpleAuthenticator;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.method.MethodContext;
import wt.method.MethodServerException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.SyncedWithCADStatus;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.pom.Transaction;
import wt.pom.UnsupportedPDSException;
import wt.pom.WTConnection;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import ext.util.IBAUtility;

public class ModifySystemObject implements RemoteAccess{
	
	 public static void main(String[] args) {
	        RemoteMethodServer server = RemoteMethodServer.getDefault();
	        server.setUserName("wcadmin");
	        server.setPassword("wcadmin");
	        Class[] classes = { String.class};
	        Object[] objs = { args[0]};
	        try {
	            server.invoke("modifyPartNum", ModifySystemObject.class.getName(), null, classes, objs);
	        } catch (RemoteException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        }
	    }
	
	/**、
	 * 修改部件编号，保持原编号，插入数据库
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void modifyPartNum(String fileName) throws WTPropertyVetoException, WTException, IOException {
		WTProperties props = WTProperties.getLocalProperties();
		// TemplateFile是模板文件
		String readFile = props.getProperty("wt.home") + File.separatorChar + "codebase"
				+ File.separatorChar + "temp" + File.separatorChar + fileName;
		//读取数据
		XSSFWorkbook wb = ExportUtil.readFile(readFile);
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row = null;
		XSSFCell cell = null;
		String newNum = "";
		String oldNum = "";
		String masterOid = "";
		
		WTPartMaster m = null;
		WTPartMasterIdentity it = null;
		String BOMReplaceBy = "";
		WTPart replace = null;
		WTPart parent = null;
		WTConnection conn = null;
		PreparedStatement ps = null;
		String sql = "";
		int sum = -1;
		ResultSet result = null;
		WTPart p = null;
		HashMap map = new HashMap();
		if(sheet != null){
			System.out.println("lastrowNum:"+sheet.getLastRowNum());
			for(int i=6;i<=sheet.getLastRowNum();i++){
				row = sheet.getRow(i);
				newNum = row.getCell(0).getStringCellValue();
				oldNum = row.getCell(2).getStringCellValue();
				masterOid = row.getCell(22).getStringCellValue();
				System.out.println("新旧编码："+newNum+"+"+oldNum);
				p = (WTPart) getLatestPersistableByNumber(oldNum, WTPart.class);
				if(p != null){
					//设置属性和修改编码
					System.out.println(p.getNumber()+"进来了，");
					m = (WTPartMaster) p.getMaster();
					if(m != null){
						it = (WTPartMasterIdentity) m.getIdentificationObject();
						it.setNumber(newNum);
						IdentityHelper.service.changeIdentity(m, it);
					}
//					m = (WTPartMaster) PersistenceHelper.manager.refresh(m);
					//将修改保存至数据库
					Transaction ta = new Transaction();
					try {
						ta.start();
						conn = (WTConnection) getMethodContext().getConnection();
						sql = "insert into TAB_ERPNUMBER(ERPNumber,WTPartNumber,MasterOID) values('"+oldNum+"','"+newNum+"','"+masterOid+"')";
						System.out.println("sql:"+sql);
						ps = conn.prepareStatement(sql);
						result = ps.executeQuery();
						ta.commit();
					} catch (Exception e) {
						System.out.println("插入数据报错，number:"+newNum);
						ta.rollback();
						e.printStackTrace();
					}finally{
						try {
							if(ps != null){
								ps.close();
							}
							if (result != null) {
								result.close();
							}  
					       } catch (Exception e) {
					           e.printStackTrace();
					       }
					}
				}else{
					System.out.println("修改编码失败，此编码"+oldNum+"么有找到part对象");
				}
				p = (WTPart) getLatestPersistableByNumber(newNum, WTPart.class);
				if(p != null){
					System.out.println("new part num:"+p.getNumber());
					Properties properties=new Properties();
					properties.setProperty("ERPNumber", oldNum);
					try {
						IBAHelper.updateOrCreateIBAValues(p, properties);
					} catch (Exception e) {
						System.out.println(newNum+"部件ERPnumber属性设置失败");
						e.printStackTrace();
					}
				}else{
					System.out.println("保存编码属性失败，此编码"+newNum+"么有找到newpart对象");
				}
				//BOMReplaceBy栏位处理
				for(int j=6;j<=sheet.getLastRowNum();j++){
					row = sheet.getRow(j);
					newNum = row.getCell(0).getStringCellValue();
					cell = row.getCell(23);
					if(cell != null){
						BOMReplaceBy = cell.getStringCellValue();
						System.out.println("替代件："+newNum+"<---"+BOMReplaceBy);
						if(!"".equals(BOMReplaceBy) || BOMReplaceBy != null){
							replace = (WTPart) getLatestPersistableByNumber(BOMReplaceBy, WTPart.class);
							WTPart pt = null;
							pt = (WTPart) getLatestPersistableByNumber(newNum, WTPart.class);
							m = (WTPartMaster) pt.getMaster();
							if(replace != null && pt != null){
								QueryResult qr = WTPartHelper.service.getUsedByWTParts(m);
								while(qr.hasMoreElements()){
									parent = (WTPart) qr.nextElement();
									System.out.println("test parent num:"+parent.getNumber());
									if(!map.containsKey(parent.getNumber())){
										map.put(parent.getNumber(), "1");
										Folder folder = FolderHelper.service.getFolder(parent);
										if(!WorkInProgressHelper.isCheckedOut(parent)){
											CheckoutLink li = WorkInProgressHelper.service.checkout(parent, folder, null);
											parent = (WTPart) WorkInProgressHelper.service.checkin(li.getWorkingCopy(), "");
											parent = (WTPart) PersistenceHelper.manager.refresh(parent);
											System.out.println("保存后新版本："+parent.getIterationIdentifier().getValue());
										}else{
											System.out.println("该父件:"+parent.getNumber()+"是检出状态,无法直接使用替换功能！");
										}
									}
									QueryResult qrLink = PersistenceHelper.manager.find(WTPartUsageLink.class, parent, WTPartUsageLink.USED_BY_ROLE, m);
									if(qrLink.hasMoreElements()){
										WTPartUsageLink link = (WTPartUsageLink) qrLink.nextElement();
										//由EPM驱动的不做处理
										if(!link.getCadSynchronized().equals(SyncedWithCADStatus.YES)){
											link.setRoleBObject(replace.getMaster());
											PersistenceServerHelper.manager.update(link);
										}else{
											System.out.println(parent.getNumber()+"下的"+m.getNumber()+"是由EPM驱动的，不需要替换");
										}
									}
								}
							}else{
								System.out.println("有对象为空；替代件："+replace+"+被替代件："+pt);
							}
						}
					}
				}	
			}
		}
	}
	
	
	/**
     * 根据对象的number找到最新版本的对象
     * @author gongke
     * @param number    要查询的对象的编号
     * @param thisClass class对象
     * @return 由number标识的最新版本对象
     */
    public static Persistable getLatestPersistableByNumber(String number,Class thisClass) {
        Persistable persistable = null;
        try {
            int[] index = {0};
            QuerySpec qs = new QuerySpec(thisClass);
            String attribute = (String) thisClass.getField("NUMBER").get(
                    thisClass);
            qs.appendWhere(new SearchCondition(thisClass, attribute, SearchCondition.LIKE, number), index);
            QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
            LatestConfigSpec configSpec = new LatestConfigSpec();
            qr = configSpec.process(qr);
            if (qr != null && qr.hasMoreElements()) {
                persistable = (Persistable) qr.nextElement();
            }
        } catch (QueryException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return persistable;
    }
    
    public static MethodContext getMethodContext()
	        throws UnsupportedPDSException, UnknownHostException {
	    MethodContext methodcontext = null;
	    try {
	        methodcontext = MethodContext.getContext();
	    } catch (MethodServerException methodserverexception) {
	        RemoteMethodServer.ServerFlag = true;
	        InetAddress inetaddress = InetAddress.getLocalHost();
	        String s = inetaddress.getHostName();
	        if (s == null) {
	            s = inetaddress.getHostAddress();
	        }
	        SimpleAuthenticator simpleauthenticator = new SimpleAuthenticator();
	        methodcontext = new MethodContext(s, simpleauthenticator);
	        methodcontext.setThread(Thread.currentThread());
	    }
	    return methodcontext;
	}
}
