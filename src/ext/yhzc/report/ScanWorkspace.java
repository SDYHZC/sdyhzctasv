package ext.yhzc.report;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import com.ptc.core.security.slcc.SLCCConstants.object_status;

import wt.epm.EPMDocument;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTSet;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class ScanWorkspace implements RemoteAccess{

	/**
	 * @param args
	 */
	public static void main(String[] args){
		RemoteMethodServer server = RemoteMethodServer.getDefault();
        server.setUserName("wcadmin");
        server.setPassword("wcadmin");
        Class[] classes = { String.class};
        Object[] objs = { args[0]};
        try {
            server.invoke("selectFunction", ModifySystemObject.class.getName(), null, classes, objs);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 
	 * @param sel //暂时有三个个选项，ws:扫描并输出工作区的部件和图档，pt:检查并输出检出的部件，epm:检查并输出检出的epm
	 */
	public static void selectFunction(String sel){
		if("ws".equals(sel)){
			checkWorkspace();
		}else if("pt".equals(sel)){
			checkPartIfCheckout();
		}else if("epm".equals(sel)){
			checkEPMIfCheckout();
		}
	}
	
	/**
	 * 扫描工作区，输出其中的部件和EPM文档对象
	 */
	public static void checkWorkspace(){
		System.out.println("into check workspace---------");
		try {
			QuerySpec qs = null;
			QueryResult qr = null;
			System.out.println("check workspace========part start=======");
			qs = new QuerySpec(EPMWorkspace.class);
			qr = PersistenceHelper.manager.find(qs);
			EPMWorkspace ws = null;
			WTSet set = null;
			System.out.println("check workspace:===ws==="+qr.size());
			while(qr.hasMoreElements()){
				ws = (EPMWorkspace) qr.nextElement();
				set = EPMWorkspaceHelper.manager.getObjectsInWorkspace(ws, WTPart.class);
				if(set.size()>0){
					for(Object o:set){
						if(o instanceof WTPart){
							WTPart p = (WTPart) o;
							System.out.println(ws.getPrincipalReference().getFullName()+"的工作区：【"+ws.getName()+"】有未清空的部件【"+p.getNumber()+"】");
						}
					}
				}
			}
			System.out.println("check workspace========part end=======");
			System.out.println("check workspace========epm start=======");
			qr = null;
			qr = PersistenceHelper.manager.find(qs);
			System.out.println("check workspace:===ws==="+qr.size());
			while(qr.hasMoreElements()){
				ws = (EPMWorkspace) qr.nextElement();
				set = null;
				set = EPMWorkspaceHelper.manager.getObjectsInWorkspace(ws, EPMDocument.class);
				if(set.size()>0){
					for(Object o:set){
						if(o instanceof EPMDocument){
							EPMDocument epm = (EPMDocument) o;
							System.out.println(ws.getPrincipalReference().getFullName()+"的工作区：【"+ws.getName()+"】有未清空的图档【"+epm.getNumber()+"】");
						}
					}
				}
			}
			System.out.println("check workspace========epm  end=======");
		} catch (QueryException e) {
			System.out.println("query check workspace exception");
			e.printStackTrace();
		} catch (WTException e) {
			System.out.println("check workspace exception");
			e.printStackTrace();
		}
	}
	
	public static void checkPartIfCheckout(){
		System.out.println("into check part if check out---------");
		try {
			QuerySpec qs = new QuerySpec(WTPart.class);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
			qr = configSpec.process(qr);
			System.out.println("检查part是否检出的数量："+qr.size());
			WTPart p = null;
			while(qr.hasMoreElements()){
				p = (WTPart) qr.nextElement();
				if(WorkInProgressHelper.isCheckedOut(p)){
					System.out.println("user:"+WorkInProgressHelper.service.workingCopyOf(p).getModifierFullName()+"-有未检入的部件【"+p.getNumber()+"】");
				}
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	public static void checkEPMIfCheckout() {
		System.out.println("into check EPM if check out---------");
		try {
			QuerySpec qs = new QuerySpec(EPMDocument.class);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec configSpec = new LatestConfigSpec();
			qr = configSpec.process(qr);
			System.out.println("检查epm是否检出的数量："+qr.size());
			EPMDocument epm = null;
			while(qr.hasMoreElements()){
				epm = (EPMDocument) qr.nextElement();
				if(WorkInProgressHelper.isCheckedOut(epm)){
					System.out.println("user:"+WorkInProgressHelper.service.workingCopyOf(epm).getModifierFullName()+"-有未检入的图档【"+epm.getNumber()+"】");
				}
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
	}
}
