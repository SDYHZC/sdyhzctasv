package ext.hbt.addStandardizationGroup;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.Locale;
import java.util.Calendar;
import java.sql.Timestamp;

import wt.clients.util.WTPrincipalUtil;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.ReferenceFactory;
import wt.inf.team.ContainerTeamHelper;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.org.electronicIdentity.ElectronicallySignable;
import wt.org.electronicIdentity.SignElectronicSignature;
import wt.pom.Transaction;
import wt.project.Role;
import wt.projmgmt.execution.ProjectActivity;
import wt.query.ClassAttribute;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.team.WTRoleHolder2;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.workflow.definer.WfAssignedActivityTemplate;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;
import wt.session.SessionServerHelper;
import wt.vc.wip.Workable;
import wt.folder.CabinetBased;
import wt.clients.vc.CheckInOutTaskLogic;
import wt.folder.FolderHelper;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.engine.WfState;
import wt.query.OrderBy;
import wt.session.SessionContext;
import wt.part.WTPart;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;

/**
 * <b>Program Name:</b> WorkflowUtil.java <br>
 * <b>Description:</b> ���������ó����
 * 
 * <br>
 * <br>
 * <b>Revision History</b> <br>
 * <b>Rev:</b> 1.0 - 2006/03/06, Zita Zhang <br>
 * <b>Comment:</b> Initial release.
 * 
 * @author Zita Zhang
 * @version 1.0
 */

public class WorkflowUtil implements Serializable, RemoteAccess {
	
	public static boolean SERVER = true;
	static Class array$Ljava$lang$String; /* synthetic field */
	public static String tempDir;
	private static boolean VERBOSE = false;
	public static int ai[] = {0};	
	static {
		WTProperties props = null;
		try {
			props = WTProperties.getLocalProperties();
			tempDir = props.getProperty("wt.temp");
			VERBOSE = props.getProperty("ext.com.workflow.verbose", false);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	static Class classx$(String s) {
		try {
			return Class.forName(s);
		} catch (ClassNotFoundException classnotfoundexception) {
			throw new NoClassDefFoundError(classnotfoundexception.getMessage());
		}
	}
	
	/**
	 * ��ö������ؽ��
	 * 
	 * @param obj
	 *            ��Ҫ��ȡ��ؽ�̵Ķ��󣬸ö������ΪWfActivity��WorkItem�ȣ�Ҳ����ΪWfProcess
	 * @return ��ؽ�� <br>
	 * <br>
	 *         <b>Revision History</b> <br>
	 *         <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *         <b>Comment:</b> Initial release.
	 */
	public static WfProcess getProcess(Object obj) {
		if (obj == null)
			return null;
		
		try {
			Persistable persistable = null;
			
			if (obj instanceof ObjectIdentifier)
				persistable = PersistenceHelper.manager
				.refresh((ObjectIdentifier) obj);
			else if (obj instanceof ObjectReference)
				persistable = ((ObjectReference) obj).getObject();
			
			if (obj instanceof Persistable)
				persistable = (Persistable) obj;
			
			if (persistable instanceof WorkItem)
				persistable = ((WorkItem) persistable).getSource().getObject();
			
			if (persistable instanceof WfActivity)
				persistable = ((WfActivity) persistable).getParentProcess();
			
			if (persistable instanceof WfConnector)
				persistable = ((WfConnector) persistable).getParentProcessRef()
				.getObject();
			
			if (persistable instanceof WfBlock)
				persistable = ((WfBlock) persistable).getParentProcess();
			
			if (persistable instanceof WfProcess)
				return (WfProcess) persistable;
			else
				return null;
		} catch (Exception e) {
			if (VERBOSE) {
				System.out.println("WorkflowUtil.getProcess : error");
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	/**
	 * �ж�ĳһ��ɫ�Ĳ������Ƿ�Ϊ��
	 * 
	 * @param processObj
	 *            ����������
	 * @param roleName
	 *            ��Ҫ�жϵĽ�ɫ
	 * @return �����գ������棻���򷵻ؼ�
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static boolean checkRoleNotEmpty(Object processObj, String roleName)
		throws WTException {
		boolean notEmpty = false;
		WfProcess wfprocess = getProcess(processObj);
		WTRoleHolder2 roleHolder = null;
		if (wfprocess != null) {
			roleHolder = (WTRoleHolder2) (Team) ((TeamReference) ((TeamManaged) wfprocess)
				.getTeamId()).getObject();
			if (roleHolder != null) {
				Role role = Role.toRole(roleName);
				Enumeration principalRefs = roleHolder.getPrincipalTarget(role);
				while (principalRefs != null && principalRefs.hasMoreElements()) {
					WTPrincipalReference principalRef = (WTPrincipalReference) principalRefs
						.nextElement();
					if (principalRef == null || principalRef.isDisabled())
						continue;
					notEmpty = true;
					break;
				}
			}
		}
		return notEmpty;
	}
	
	/**
	 * �жϵ�ǰ�û��Ƿ�Ϊĳһ��ɫ������
	 * 
	 * @param processObj
	 *            ����������
	 * @param rolename
	 *            ָ����ɫ
	 * @return �����ǣ�����false�����򷵻�true <br>
	 * <br>
	 *         <b>Revision History</b> <br>
	 *         <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *         <b>Comment:</b> Initial release.
	 */
	public static boolean isParticipant(Object processObj, String rolename) {
		if ((processObj == null) || (rolename == null)
			|| (rolename.length() <= 0))
			return false;
		try {
			WTPrincipal principal = SessionHelper.manager.getPrincipal();
			WfProcess wfprocess = null;
			wfprocess = getProcess(processObj);
			if (wfprocess == null)
				throw new WTException(
				"	failed to get the process by process obj "
				+ processObj);
			Enumeration participants = getRoleEnumeration(wfprocess, rolename);
			while (participants.hasMoreElements()) {
				WTPrincipalReference principalRef = (WTPrincipalReference) participants
					.nextElement();
				if (principalRef == null || principalRef.isDisabled())
					continue;
				WTPrincipal participant = principalRef.getPrincipal();
				if (participant.getName().equals(principal.getName()))
					return true;
				if (participant instanceof WTGroup) {
					if (((WTGroup) participant).isMember(principal))
						return true;
				}
			}
		} catch (WTException e) {
			if (VERBOSE) {
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}
	
	/**
	 * �õ��ض���ɫ�����в�������Ϣ
	 * 
	 * @param processObj
	 *            ����������
	 * @param roleName
	 *            ��Ҫ��ȡ��������Ϣ�Ľ�ɫ
	 * @return ����һ��vector�����ɫ�����в����ߣ��û����û��飩��Ԫ��ΪWTPrincipal����
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static Vector getPrincipalsFromRole(Object processObj,
		String roleName) throws WTException {
		Vector PrincipalsV = new Vector();
		if ((processObj == null) || (roleName == null)
			|| (roleName.length() <= 0))
			return PrincipalsV;
		
		try {
			WfProcess process = getProcess(processObj);
			if (process == null)
				throw new WTException(
				"	failed to get the process by process obj "
				+ processObj);
			Enumeration participants = getRoleEnumeration(process, roleName);
			while (participants.hasMoreElements()) {
				WTPrincipalReference principalRef = (WTPrincipalReference) participants
					.nextElement();
				if (principalRef == null || principalRef.isDisabled())
					continue;
				WTPrincipal participant = principalRef.getPrincipal();
				PrincipalsV.addElement(participant);
			}
		} catch (WTException e) {
			if (VERBOSE) {
				e.printStackTrace();
			}
			return null;
		}
		return PrincipalsV;
	}
	
	/**
	 * ���һ�������ߵ����̵�ĳһָ����ɫ��
	 * 
	 * @param processObj
	 *            ����������
	 * @param rolename
	 *            ָ����ɫ
	 * @param prin
	 *            ����ӵĳе��ߣ��û����û��飩
	 * @return ����ӳɹ�������true�����򷵻�false <br>
	 * <br>
	 *         <b>Revision History</b> <br>
	 *         <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *         <b>Comment:</b> Initial release.
	 */
	public static boolean setProcessRoleHolder(Object processObj,
		String rolename, WTPrincipal prin) {
		boolean flag = true;
		WfProcess process = getProcess(processObj);
		if (process == null)
			return false;
		
		try {
			Enumeration enum1 = getRoleEnumeration(process, rolename);
			while (enum1 != null && enum1.hasMoreElements()) {
				WTPrincipal wtp = ((WTPrincipalReference) enum1.nextElement())
					.getPrincipal();
				if (wtp.equals(prin))
					return false;
			}
			wt.project.Role role = (wt.project.Role) (wt.project.Role
				.toRole(rolename));
			wt.team.Team team = (wt.team.Team) ((wt.team.TeamReference) ((wt.team.TeamManaged) process)
				.getTeamId()).getObject();
			if (!hasExistPrincipal(team, role, prin))
				team.addPrincipal(role, prin);
		} catch (Exception e) {
			if (VERBOSE) {
				System.out.println("WorkflowUtil.setProcessRoleHolder : error");
				e.printStackTrace();
			}
			flag = false;
		} finally {
		}
		return flag;
	}
	
	/**
	 * ��Ӷ�������ߵ����̵�ĳһָ����ɫ��
	 * 
	 * @param processObj
	 *            ����������
	 * @param roleName
	 *            ָ����ɫ
	 * @param newPrincipals
	 *            ����ӵĳе��ߣ��û����û��飩��һ��WTPrincipalReference���͵�ö������
	 * @return �ɹ���Ӳ����ߺ��WTRoleHolder2
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static WTRoleHolder2 addPrincipalsToRole(Object processObj,
		String roleName, Enumeration newPrincipals) throws WTException {
		WTRoleHolder2 roleHolder = null;
		WfProcess wfprocess = getProcess(processObj);
		Team team = (Team) ((TeamReference) ((TeamManaged) wfprocess)
			.getTeamId()).getObject();
		team = (Team) PersistenceHelper.manager.refresh(team);
		roleHolder = addRolePrincipals(team, roleName, newPrincipals);
		return roleHolder;
	}
	
	/**
	 * @param teammanaged
	 * @param rolename
	 * @return �Ŷӽ�ɫ�е��û�����
	 * @throws WTException
	 */
	public static Enumeration getRoleEnumeration(
		wt.team.TeamManaged teammanaged, String rolename)
		throws WTException {
		if (teammanaged == null || rolename == null || rolename.length() == 0)
			return null;
		Team team = (Team) ((TeamReference) teammanaged.getTeamId())
			.getObject();
		if (team == null)
			return null;
		Role role = (Role) (Role.toRole(rolename));
		return team.getPrincipalTarget(role);
	}
	
	/**
	 * �����̵�ĳһָ����ɫ���Ƴ��ض��û����û���
	 * 
	 * @param processObj
	 *            ���������
	 * @param rolename
	 *            ָ����ɫ
	 * @param prin
	 *            ���Ƴ�ĳе��ߣ��û����û��飩
	 * @return ���Ƴ�ɹ�������true�����򷵻�false <br>
	 * <br>
	 *         <b>Revision History</b> <br>
	 *         <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *         <b>Rev:</b> 1.1 - 2009/1/20, chunfeng.zhu <br>
	 *         <b>Comment:</b> revise release.
	 */
	public static boolean deleteProcessRoleHolder(Object processObj,
		String rolename, WTPrincipal prin) {
		if (prin instanceof WTUser) {
			boolean flag = true;
			WfProcess process = getProcess(processObj);
			if (process == null)
				return false;
			
			try {
				Role role = (Role) (Role.toRole(rolename));
				Team team = (Team) ((TeamReference) ((TeamManaged) process)
					.getTeamId()).getObject();
				team.deletePrincipalTarget(role, prin);
			} catch (Exception e) {
				if (VERBOSE) {
					System.out
						.println("GreeWorkFlowUtil.deleteProcessRoleHolder : error");
					e.printStackTrace();
				}
				flag = false;
			}
			
			return flag;
		} else if (prin instanceof WTGroup) {
			WTGroup group = (WTGroup) prin;
			try {
				Enumeration enumeration = group.members();
				while (enumeration.hasMoreElements()) {
					WTPrincipal principal = (WTPrincipal) enumeration
						.nextElement();
					boolean flag = deleteProcessRoleHolder(processObj,
						rolename, principal);
					if (!flag) {
						return false;
					}
				}
			} catch (WTException e) {
				if (VERBOSE) {
					e.printStackTrace();
				}
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ���ָ����ɫ�Ĳ�����
	 * 
	 * @param processObj
	 *            ����������
	 * @param rolename
	 *            ָ����ɫ
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void clearProcessRole(Object processObj, String rolename)
		throws WTException {
		WfProcess process = getProcess(processObj);
		Enumeration enum1 = getRoleEnumeration(process, rolename);
		while (enum1 != null && enum1.hasMoreElements()) {
			WTPrincipal wtp = ((WTPrincipalReference) enum1.nextElement())
				.getPrincipal();
			deleteProcessRoleHolder(process, rolename, wtp);
		}
	}
	
	/**
	 * ���ƽ�ɫ������
	 * 
	 * @param processObj
	 *            ����������
	 * @param sourceRole
	 *            ��ɫԴ
	 * @param destRole
	 *            Ŀ���ɫ
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void copyParticipants(Object processObj, String sourceRole,
		String destRole) throws WTException {
		WfProcess wfprocess = getProcess(processObj);
		
		Team team = (Team) ((TeamReference) ((TeamManaged) wfprocess)
			.getTeamId()).getObject();
		team = (Team) PersistenceHelper.manager.refresh(team);
		Enumeration participants = getRoleEnumeration(team, sourceRole);
		if ((participants == null) || (!participants.hasMoreElements()))
			return;
		team = (Team) addRolePrincipals(team, destRole, participants);
		team = (Team) PersistenceHelper.manager.refresh(team);
	}
	
	/**
	 * @param team
	 * @param rolename
	 * @return �Ŷӽ�ɫ�е��û�����
	 * @throws WTException
	 */
	public static Enumeration getRoleEnumeration(wt.team.Team team,
		String rolename) throws WTException {
		if (rolename == null || rolename.length() == 0)
			return null;
		Role role = (Role) (Role.toRole(rolename));
		return team.getPrincipalTarget(role);
	}
	
	/**
	 * ���ָ����ɫ�Ĳ������Ƿ��Ѿ�����
	 * 
	 * @param roleHolder
	 * @param role
	 * @param principal
	 * @return �����ڷ���true�����򷵻�false
	 * @throws WTException
	 */
	public static boolean hasExistPrincipal(WTRoleHolder2 roleHolder,
		Role role, WTPrincipal principal) throws WTException {
		boolean hasExist = false;
		
		Enumeration enums = roleHolder.getPrincipalTarget(role);
		while (enums != null && enums.hasMoreElements()) {
			WTPrincipal aPrincipal = ((WTPrincipalReference) enums
				.nextElement()).getPrincipal();
			if (aPrincipal.toString().equals(principal.toString())) {
				hasExist = true;
				break;
			}
		}
		return hasExist;
	}
	
	/**
	 * @param roleHolder
	 * @param roleName
	 * @param newPrincipals
	 *            new principals References
	 * @return ������û�����ɫ�Ľ�ɫ������
	 * @throws WTException
	 */
	public static WTRoleHolder2 addRolePrincipals(WTRoleHolder2 roleHolder,
		String roleName, Enumeration newPrincipals) throws WTException {
		if (roleHolder == null || roleName == null || roleName.length() == 0
			|| newPrincipals == null || (!newPrincipals.hasMoreElements()))
			return roleHolder;
		Role role = Role.toRole(roleName);
		Enumeration enums = roleHolder.getPrincipalTarget(role);
		// remove the repeated principals
		Vector newPrincipalV = new Vector();
		while (newPrincipals.hasMoreElements()) {
			newPrincipalV.addElement(newPrincipals.nextElement());
		}
		while (enums != null && enums.hasMoreElements()) {
			WTPrincipalReference principalRef = (WTPrincipalReference) enums
				.nextElement();
			if (principalRef == null)
				continue;
			Object obj = principalRef.getObject();
			if (obj == null)
				continue;
			newPrincipalV.remove(principalRef);
		}
		
		for (int i = 0; i < newPrincipalV.size(); i++) {
			WTPrincipalReference principalRef = (WTPrincipalReference) newPrincipalV
				.elementAt(i);
			roleHolder.addPrincipal(role, (WTPrincipal) principalRef
				.getObject());
		}
		return roleHolder;
	}
	
	/**
	 * ���ö�����������״̬
	 * 
	 * @param obj
	 *            ��Ҫ������������״̬�Ķ���
	 * @param stateStr
	 *            �����õ���������״̬�������ڸ���������״̬��������
	 * @return �����õĶ���
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/07, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static LifeCycleManaged setLifeCycleState(LifeCycleManaged obj,
		String stateStr) throws WTException {
		State state = State.toState(stateStr);
		if (state.equals(obj.getLifeCycleState()))
			return obj;
		
		// �����ڸ���������״̬��������
		LifeCycleTemplate lifecycleTemplate = (LifeCycleTemplate) obj
			.getLifeCycleTemplate().getObject();
		if (LifeCycleHelper.service.isState(lifecycleTemplate, State
			.toState(stateStr)))
			obj = LifeCycleHelper.service.setLifeCycleState(obj, state);
		else
			System.out.println("\thasn't state: " + state.getDisplay()
			+ ", skipped.");
		
		return obj;
	}
	
	/**
	 * ��������ǩ��
	 * 
	 * @param pbo
	 *            ������PBO
	 * @param strRole
	 *            ��ɻ��ɫ
	 * @param comment
	 *            ����
	 * @param wfactivity
	 *            ���̻
	 * @param router
	 *            ��ѡ·��ѡ��
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/06, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void createElectronicSignature(Persistable pbo,
		String strRole, String comment, WfActivity wfactivity, String router)
		throws WTException, WTPropertyVetoException {
		Transaction transaction = null;
		try {
			transaction = new Transaction();
			transaction.start();
			String instructions = ((WfAssignedActivityTemplate) wfactivity
				.getTemplateReference().getObject()).getName();
			WTUser user = (WTUser) SessionHelper.manager
				.getPrincipalReference().getObject();
			WTPrincipal curUser = SessionHelper.manager.getPrincipal();
			SessionHelper.manager.setAdministrator();
			SignElectronicSignature.setObjectsElectronicSignature(user,
				(ElectronicallySignable) pbo, comment, instructions,
				strRole, router);
			SessionHelper.manager.setPrincipal(curUser.getName());
			transaction.commit();
			transaction = null;
		} finally {
			if (transaction != null)
				transaction.rollback();
		}
	}
	
	/**
	 * ���õ���ǩ��
	 * 
	 * @param currentUser
	 *            �û�����������
	 * @param object
	 *            ������PBO
	 * @param comment
	 *            ���
	 * @param activityInstructions
	 *            �˵��
	 * @param roleName
	 *            ��ɫ
	 * @param tallyResult
	 *            ͶƱ��Ϣ
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * 
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/03/08, Leon Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void setElectronicSignature(WTUser currentUser,
		Object object, String comment, String activityInstructions,
		String roleName, String tallyResult) throws WTException,
	WTPropertyVetoException {
		ElectronicallySignable electronicallysignable = (ElectronicallySignable) object;
		SignElectronicSignature.setObjectsElectronicSignature(currentUser,
			electronicallysignable, comment, activityInstructions,
			roleName, tallyResult);
	}
	
	/**
	 * �õ��û��Ĺ����������б�
	 * 
	 * @param user
	 *            �û�
	 * @param includeCompleted
	 *            �Ƿ��������ɵ�������Ϊtrue���������ɵ�����
	 * @return Vector �����������б?Ԫ��ΪWfActivity����
	 * @throws WTException
	 * 
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/09/26, Peter Duan <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static Vector getWorkItemsByUser(WTUser user,
		boolean includeCompleted) throws WTException {
		Vector wfctivitys = new Vector();
		if (user != null) {
			QueryResult workitems = WorkflowHelper.service.getWorkItems(user);
			while (workitems.hasMoreElements()) {
				WorkItem workitem = (WorkItem) workitems.nextElement();
				if (!workitem.isComplete()) {
					WfActivity source = (WfActivity) workitem.getSource()
						.getObject();
					wfctivitys.add(source);
				} else {
					WfActivity source = (WfActivity) workitem.getSource()
						.getObject();
					if (includeCompleted)
						wfctivitys.add(source);
				}
			}
		}
		return wfctivitys;
	}
	
	/**
	 * �õ��û�����Ŀ��б�
	 * 
	 * @param user
	 *            �û�
	 * @param includeCompleted
	 *            �Ƿ��������ɵĻ����Ϊtrue���������ɵĻ
	 * @return Vector ��Ŀ��б?Ԫ��ΪProjectActivity����
	 * @throws QueryException
	 * @throws WTException
	 * 
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2006/09/26, Peter Duan <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static Vector getProjectActivityByUser(WTUser user,
		boolean includeCompleted) throws QueryException, WTException {
		Vector projectActivitys = new Vector();
		if (user != null) {
			QuerySpec queryspec = new QuerySpec(ProjectActivity.class);
			SearchCondition sc = new SearchCondition(ProjectActivity.class,
				"ownership.owner.key", "LIKE", PersistenceHelper
				.getObjectIdentifier(user));
			queryspec.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(queryspec);
			while (qr.hasMoreElements()) {
				ProjectActivity projectActivity = (ProjectActivity) qr
					.nextElement();
				if (projectActivity.getPercentComplete() != 100)
					projectActivitys.add(projectActivity);
				else if (includeCompleted)
					projectActivitys.add(projectActivity);
			}
		}
		return projectActivitys;
	}
	
	/**
	 * ��ݽ�̣���ȡ��Ӧ״̬�Ļ�б� ����completed Ϊtrue����ȡ��������ɵĻ��Ϊfalse����ȡ����δ��ɵĻ��
	 * 
	 * @param process
	 *            ���̶���
	 * @param completed
	 *            Ϊtrue����ȡ��������ɵĻ��Ϊfalse����ȡ����δ��ɵĻ��
	 * @return ��Vector����Ԫ��ΪWorkItem����
	 * @throws WTException
	 * 
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2007/08/27, Winnie Ying <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static Vector getWorkItems(WfProcess process, boolean completed)
		throws WTException {
		QueryResult workItems;
		QuerySpec qs1 = new QuerySpec();
		qs1.setAdvancedQueryEnabled(true);
		int waaIndex = qs1.appendClassList(
			wt.workflow.work.WfAssignedActivity.class, false);
		qs1.appendSelectAttribute("thePersistInfo.theObjectIdentifier.id",
			waaIndex, false);
		qs1.appendWhere(new SearchCondition(
			wt.workflow.work.WfAssignedActivity.class,
			"parentProcessRef.key", "=", getOid(process)));
		SubSelectExpression sse1 = new SubSelectExpression(qs1);
		QuerySpec qs2 = new QuerySpec(wt.workflow.work.WorkItem.class);
		qs2.setAdvancedQueryEnabled(true);
		qs2.appendWhere(new SearchCondition(wt.workflow.work.WorkItem.class,
			"completedBy", !completed), 0);
		ClassAttribute qs2_ca1 = new ClassAttribute(
			wt.workflow.work.WorkItem.class, "source.key.id");
		qs2.appendAnd();
		SearchCondition qs2_sc2 = new SearchCondition(qs2_ca1, "IN", sse1);
		qs2.appendWhere(qs2_sc2, 0);
		workItems = PersistenceServerHelper.manager.query(qs2);
		return workItems.getObjectVectorIfc().getVector();
	}
	
	/**
	 * ��ݶ����ȡ��ObjectIdentifier���Ͷ���
	 * 
	 * @param object
	 *            ����
	 * @return ObjectIdentifier ���Ͷ���
	 * 
	 * <br>
	 * <br>
	 *         <b>Revision History</b> <br>
	 *         <b>Rev:</b> 1.0 - 2007/08/27, Winnie Ying <br>
	 *         <b>Comment:</b> Initial release.
	 */
	public static ObjectIdentifier getOid(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof ObjectReference) {
			return (ObjectIdentifier) ((ObjectReference) object).getKey();
		} else {
			return PersistenceHelper.getObjectIdentifier((Persistable) object);
		}
	}
	
	/**
	 * ���� <br>
	 * <br>
	 * <b>Revision History</b> <br>
	 * <b>Rev:</b> 1.0 - 2007/08/27, Winnie Ying <br>
	 * <b>Comment:</b> Initial release.
	 */
	private void doMyOperations(String args[]) throws Exception {
		System.out.println("aaaa");
		String oid = args[0];
		ReferenceFactory referencefactory = new ReferenceFactory();
		WfProcess process = (WfProcess) referencefactory.getReference(oid)
			.getObject();
		System.out.println("process = " + process.getName());
		Vector completedWI = getWorkItems(process, true);
		WorkItem wi = null;
		WfActivity wfa = null;
		for (int i = 0; i < completedWI.size(); i++) {
			wi = (WorkItem) completedWI.elementAt(i);
			wfa = (WfActivity) wi.getSource().getObject();
			System.out.println("wfa's name = " + wfa.getName());
			WTPrincipalReference prinRef = wi.getOwnership().getOwner();
			WTPrincipal prin = (WTPrincipal) prinRef.getObject();
			String userName = "";
			if (prin instanceof WTUser) {
				userName = WTPrincipalUtil.getFullName((WTUser) prin);
				System.out.println("userName 0 = " + userName);
				userName = WTPrincipalUtil.getName((WTUser) prin);
				System.out.println("userName 1 = " + userName);
				userName = ((WTUser) prin).getName();
				System.out.println("userName 2 = " + userName);
			} else if (prin instanceof WTGroup) {
				userName = ContainerTeamHelper.getDisplayName((WTGroup) prin);
				System.out.println("userName 3 = " + userName);
				userName = ((WTGroup) prin).getName();
				System.out.println("userName 4 = " + userName);
			}
			System.out.println("userName = " + userName);
			
			java.util.Date date = new java.util.Date();
			date = wi.getPersistInfo().getCreateStamp();
			System.out.println("date = " + date);
			String logTime = WTStandardDateFormat.format(date);
			System.out.println("logTime = " + logTime);
			
			date = wi.getPersistInfo().getModifyStamp();
			System.out.println("date 2 = " + date);
			logTime = WTStandardDateFormat.format(date);
			System.out.println("logTime 2 = " + logTime);
			
			date = wi.getPersistInfo().getUpdateStamp();
			System.out.println("date 3 = " + date);
			logTime = WTStandardDateFormat.format(date);
			System.out.println("logTime 3 = " + logTime);
		}
	}
	
	/**
	 * �ж�Vector�����е����Ƿ��ڼ��״̬��Vector��֧�ֵĶ�������ΪWTObject
	 * 
	 * @param reviewPartsList
	 *            ��Ҫ�ж��Ƿ��ڼ��״̬�Ķ��󼯺�
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void isObjectCheckedOut(Vector reviewPartsList)
		throws WTException
	{
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Vector checkedVector=new Vector();
		try
		{
			int partsListSize = reviewPartsList.size();
			for(int i = 0; i < partsListSize; i++)
			{
				WTObject wto = (WTObject)reviewPartsList.elementAt(i);
				if(VERBOSE)
				{
					System.out.println("\u5224\u65AD\u5BF9\u8C61\u2018" + wto.getDisplayIdentity() + "\u2019\u662F\u5426\u88AB\u68C0\u51FA\u3002");
				}
				if(isObjectCheckedOut(wto))
				{
					checkedVector.add(wto);
				}
			}
			if(checkedVector.size()>0)
			{
				StringBuffer buffer=new StringBuffer();
				for (Iterator iterator = checkedVector.iterator(); iterator
						.hasNext();) {
					WTObject wto = (WTObject) iterator.next();
					buffer.append(wto.getDisplayIdentity()).append("\n");
				}
				throw new WTException("\u5BF9\u8C61\u2018" + buffer.toString() + "\u2019\u6CA1\u6709\u68C0\u5165\u3002");
			}
		}
		catch(WTException e)
		{
			throw e;
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	
	/**
	 * �ж�ĳһ�ض������Ƿ��ڼ��״̬
	 * 
	 * @param wto
	 *            ��Ҫ�ж��Ƿ��ڼ��״̬�Ķ���
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static boolean isObjectCheckedOut(WTObject wto)
	{
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try
		{
			if((wto instanceof Workable) && (wto instanceof CabinetBased) && (CheckInOutTaskLogic.isCheckedOut((Workable)wto) || FolderHelper.inPersonalCabinet((CabinetBased)wto)))
				return true;
			else
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();		
		}
		finally
		{
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return false;
	}			
	
	/**
	 *������Ҫ�жϳ�Ա�Ƿ�Ϊ�յĽ�ɫ�ַ����У�Ŀǰ֧��10�������ʵ��δ�ﵽ10��������á�false, null�������
	 *�ú���Ӧ��verifyIfRoleSet������ʹ��
	 * @param activity1 �1�Ƿ�Ҫ��
	 * @param roleHey1 �1�����߽�ɫKEYֵ
	 * @param activity2 �2�Ƿ�Ҫ��
	 * @param roleHey2 �2�����߽�ɫKEYֵ
	 * @param activity3 �3�Ƿ�Ҫ��
	 * @param roleHey3 �3�����߽�ɫKEYֵ
	 * @param activity4 �4�Ƿ�Ҫ��
	 * @param roleHey4 �4�����߽�ɫKEYֵ
	 * @param activity5 �5�Ƿ�Ҫ��
	 * @param roleHey5 �5�����߽�ɫKEYֵ
	 * @param activity6 �6�Ƿ�Ҫ��
	 * @param roleHey6 �6�����߽�ɫKEYֵ
	 * @param activity7 �7�Ƿ�Ҫ��
	 * @param roleHey7 �7�����߽�ɫKEYֵ
	 * @param activity8 �8�Ƿ�Ҫ��
	 * @param roleHey8 �8�����߽�ɫKEYֵ
	 * @param activity9 �9�Ƿ�Ҫ��
	 * @param roleHey9 �9�����߽�ɫKEYֵ
	 * @param activity10 �10�Ƿ�Ҫ��
	 * @param roleHey10 �10�����߽�ɫKEYֵ
	 * @return ����Ҫ�߻�����߽�ɫKEYֵ+;;;qqq �ļ���
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static String getRoleStr(boolean activity1, String roleHey1, boolean activity2, String roleHey2, boolean activity3, String roleHey3, boolean activity4, String roleHey4, 
		boolean activity5, String roleHey5, boolean activity6, String roleHey6, boolean activity7, String roleHey7, boolean activity8, 
		String roleHey8, boolean activity9, String roleHey9, boolean activity10, String roleHey10)
	{
		String roleStr = "";
		if(activity1)
		{
			roleStr = roleStr + ";;;qqq" + roleHey1;
		}
		if(activity2)
		{
			roleStr = roleStr + ";;;qqq" + roleHey2;
		}
		if(activity3)
		{
			roleStr = roleStr + ";;;qqq" + roleHey3;
		}
		if(activity4)
		{
			roleStr = roleStr + ";;;qqq" + roleHey4;
		}
		if(activity5)
		{
			roleStr = roleStr + ";;;qqq" + roleHey5;
		}
		if(activity6)
		{
			roleStr = roleStr + ";;;qqq" + roleHey6;
		}
		if(activity7)
		{
			roleStr = roleStr + ";;;qqq" + roleHey7;
		}
		if(activity8)
		{
			roleStr = roleStr + ";;;qqq" + roleHey8;
		}
		if(activity9)
		{
			roleStr = roleStr + ";;;qqq" + roleHey9;
		}
		if(activity10)
		{
			roleStr = roleStr + ";;;qqq" + roleHey10;
		}
		return roleStr;
	}		
	
	/**
	 * �ж�ָ����ɫ������ĳ�Ա�Ƿ�Ϊ��
	 * 
	 * @param processRef
	 *          ���ʵ���?��self����  
	 * @param roles
	 *          �ɺ���getRoleStr�������Ҫ�жϲ����ߵĽ�ɫ�����ַ�
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void verifyIfRoleSet(ObjectReference processRef, String roles)
		throws WTException
	{
		Role myRole = null;
		if(roles.indexOf(";;;qqq") > -1)
		{
			String roles1[] = roles.split(";;;qqq");
			for(int i = 0; i < roles1.length; i++)
			{
				String role = roles1[i];
				if(role != null && role.length() > 0)
				{
					myRole = Role.toRole(role);
					boolean blHasRoleSet = hasRoleSet(processRef, role);
					if(!blHasRoleSet)
					{
						throw new WTException("\u89D2\u8272\u2018" + myRole.getDisplay() + "\u2019\u6CA1\u6709\u53C2\u4E0E\u8005\uFF0C\u8BF7\u6307\u5B9A\u53C2\u4E0E\u8005\u3002");
					}
				}
			}
			
		} else
		{
			String role = roles;
			if(role != null && role.length() > 0)
			{
				myRole = Role.toRole(role);
				boolean blHasRoleSet = hasRoleSet(processRef, role);
				if(blHasRoleSet)
				{
					return;
				} else
				{
					throw new WTException("\u89D2\u8272\u2018" + myRole.getDisplay() + "\u2019\u6CA1\u6709\u53C2\u4E0E\u8005\uFF0C\u8BF7\u6307\u5B9A\u53C2\u4E0E\u8005\u3002");
				}
			}
		}
	}	
	
	//�жϽ�ɫ��ĳ�Ա�Ƿ�Ϊ��	
	public static boolean hasRoleSet(ObjectReference processRef, String role)
	{
		WfAssignedActivity mySelf = null;
		WfProcess wfp = null;
		try
		{
			mySelf = (WfAssignedActivity)processRef.getObject();
			wfp = mySelf.getParentProcess();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Enumeration enumPrin = null;
		Team team = null;
		try
		{
			team = (Team)wfp.getTeamId().getObject();
			Role myRole = null;
			myRole = Role.toRole(role);
			enumPrin = team.getPrincipalTarget(myRole);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return enumPrin.hasMoreElements();
	}
	
	/**
	 * �������ö���״̬;�������ǡ�RELEASE��״̬�Ͳ�����ΪtoState
	 * 
	 * @param lcmList
	 *          ��Ҫ����״̬�Ķ��󼯺ϣ�֧��LifeCycleManaged���͵����ж���
	 * @param toState
	 *          Ŀ��״̬��Ϊ״̬��KEYֵ
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void setLifeCycleManagedState(Vector lcmList, String toState)
		throws WTException {
		WTPrincipal administrator = null;
		WTPrincipal previous = null;
		
		int lcmListSize = lcmList.size();
		
		if(VERBOSE) System.out.println("WorkflowUtil.setLifeCycleManagedState�� ���� "+ lcmListSize + "�� ����״̬Ϊ " + toState);
		//�л�ϵͳ����Ա
		WTUser currentuser = (WTUser)SessionHelper.manager.getPrincipal();
		WTUser admin = (WTUser)SessionHelper.manager.setAdministrator();
		try {
			for(int i = 0; i < lcmListSize; i++) {
				LifeCycleManaged lcm = (LifeCycleManaged)lcmList.elementAt(i);
				String currentlcmState = lcm.getLifeCycleState().toString();
				
				if(!currentlcmState.equals("RELEASED")) {
					if(VERBOSE)
						System.out.println("���ö���" + ((WTObject)lcm).getDisplayIdentifier() + "����״̬. ��ǰ״̬��" + currentlcmState);
					try{
						lcm = LifeCycleHelper.service.setLifeCycleState(lcm, State.toState(toState));
						
						if(VERBOSE)
							System.out.println("����Ϊ " + lcm.getLifeCycleState().toString() );
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		} finally {
			//���û��л�����
			WTUser user = (WTUser)SessionHelper.manager.setPrincipal(currentuser.getAuthenticationName());
			
		}
	}		
	
	/**
	 * �������ʱ��������һ�����ʵ����ͬ������ģ�塢��������ͬ�����Ŷ�����ʵ��
	 * ���У�PROMOTER��SUBMITTER��ɫ�������и���
	 * 
	 * @param currentProcessObj
	 *          �����ʵ��
	 * @param processTemplateName
	 *          ������ģ�����
	 * @param searchRange
	 *          ������ʱ�䷶Χ������ֵΪ30����ֻ����30���ڵķ��Ҫ��Ľ��ʵ��
	 * @throws WTException,IOException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void initializeRoleHolderByProcessCreator(Object currentProcessObj,String processTemplateName,int searchRange)throws WTException,IOException
	{
		if(VERBOSE) System.out.println(">>>>>>>>>>.initializeRoleHolderByProcessCreator()");
		
		WfProcess currentProcess= WorkflowUtil.getProcess(currentProcessObj);
		WTPrincipal curCreator=(WTPrincipal)((WTPrincipalReference)currentProcess.getCreator()).getObject();
		
		Team curTeam = (Team)((TeamReference)((TeamManaged)currentProcess).getTeamId()).getObject();
		curTeam= (Team)PersistenceHelper.manager.refresh(curTeam);
		Vector curRoles = curTeam.getRoles();
		if(VERBOSE) System.out.println("    the roles of currentProcess=" + curRoles);
		if((curRoles==null) || (curRoles.size()<=0))
			return;
		//remove the system roles
		for(int i=0; i< curRoles.size(); i++)
		{
			Object role = curRoles.elementAt(i);
			if(role.toString().equals("PROMOTER") || role.toString().equals("SUBMITTER"))
			{
				curRoles.removeElementAt(i);
				i--;
			}
		}
		
		if((curRoles==null) || (curRoles.size()<=0))
		{
			if(VERBOSE) System.out.println("	remained roles=null, exit!!!");
			return;
		}
		//clear the role's participates
		for(int i=0; i< curRoles.size(); i++)
		{
			Object role = curRoles.elementAt(i);
			java.util.Enumeration enums = curTeam.getPrincipalTarget(wt.project.Role.toRole(role.toString()));
			if(VERBOSE) System.out.println("clear curRole is: "  + role);
			while(enums.hasMoreElements())
			{
				wt.org.WTPrincipalReference princ = (wt.org.WTPrincipalReference)(enums.nextElement());
				if(VERBOSE) System.out.println("delete cur Team principal is: "  + ((WTPrincipal)(princ.getObject())).getName());
				curTeam.deletePrincipalTarget(wt.project.Role.toRole(role.toString()), (wt.org.WTPrincipal)princ.getObject());
			}
			
		}
		WTProperties wtproperties = WTProperties.getLocalProperties();
		
		if(VERBOSE) System.out.println("Process searchRange="+searchRange);
		
		boolean needUpdate=false;
		ReferenceFactory rf= new ReferenceFactory();
		String curProcessOid= rf.getReferenceString(currentProcess);
		QueryResult enuHistoryProcess=getRecentProcess(processTemplateName,curCreator,searchRange);
		
		while(enuHistoryProcess.hasMoreElements())
		{
			WfProcess historyProcess= (WfProcess)enuHistoryProcess.nextElement();
			if(VERBOSE) System.out.println("get history recently process="+ historyProcess);
			if( curProcessOid.equals(rf.getReferenceString(historyProcess)))
				continue;
			if(historyProcess instanceof wt.team.TeamManaged)
			{				
				TeamReference tf =(TeamReference)((TeamManaged)historyProcess).getTeamId();
				if (tf ==null)
					continue;
				
				Team historyTeam =null;
				try
				{
					historyTeam = (Team)(tf.getObject());	
				}
				catch (wt.util.WTRuntimeException e)
				{
					System.out.println("error ="+e.toString());
					continue;
				}												
				
				if (historyTeam ==null)
					continue;
				
				Vector historyRoles =historyTeam.getRoles();
				if(VERBOSE) System.out.println("    historyProcess(" + historyProcess.getIdentity() + ") team roles=" + historyRoles);
				for(int i=0; i<curRoles.size(); i++)
				{
					String curRoleName=curRoles.elementAt(i).toString();
					Object curRole = curRoles.elementAt(i);
					java.util.Enumeration enums = historyTeam.getPrincipalTarget(wt.project.Role.toRole(curRole.toString()));
					while(enums.hasMoreElements())
					{
						wt.org.WTPrincipalReference princ = (wt.org.WTPrincipalReference)(enums.nextElement());
						if(VERBOSE) System.out.println("add "+((WTPrincipal)princ.getObject()).getName()+" to curTeam " + curTeam.getName());
						curTeam.addPrincipal(wt.project.Role.toRole(curRole.toString()), (WTPrincipal)princ.getObject());
						needUpdate = true;
					}
					
				}
				if(VERBOSE) System.out.println("    needUpdate="+needUpdate );
				if(needUpdate)
					break;
			}
		}
		if(VERBOSE) System.out.println("<<<<<<<<<<<.initializeRoleHolderByProcessCreator()");
	}
	
	/**
	 * �������ģ����ơ������ߡ�����ʱ�䡢����״̬����ִ�У������������������������
	 * 
	 * @param processTemplateName
	 *          ������ģ�����
	 * @param creator
	 *          ������
	 * @param timeRange
	 *          ������ʱ�䷶Χ����λΪ��
	 * @throws WTException,IOException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static QueryResult getRecentProcess(String processTemplateName,WTPrincipal creator, int timeRange) throws WTException
	{		
		if(VERBOSE) System.out.println("    >>>getRecentProcess()--processs templat name=" + processTemplateName);
		
		if(creator==null)
		{
			creator= SessionHelper.manager.getPrincipal();
		}
		Locale locale= SessionHelper.manager.getLocale();
		if(VERBOSE) System.out.println("    creator=" + creator.getName());
		Calendar calendar= Calendar.getInstance(locale);
		calendar.add(Calendar.DATE, -(timeRange));
		Timestamp timeStamp= new Timestamp(calendar.getTime().getTime());
		
		QuerySpec querysearch=null;
		QueryResult queryresult=null;
		boolean hasCondition=false;
		querysearch= new QuerySpec(WfProcess.class);
		if(processTemplateName!=null && processTemplateName.length()>0)
		{
			SearchCondition sc5= new SearchCondition(WfProcess.class, "name", SearchCondition.LIKE, "%"+processTemplateName+"%");
			querysearch.appendWhere(sc5);
			hasCondition=true;
		}
		
		SearchCondition sc6= new SearchCondition(WfProcess.class, "creator.key", SearchCondition.LIKE, PersistenceHelper.getObjectIdentifier(creator));
		if(hasCondition)
		{
			querysearch.appendAnd();
		}
		querysearch.appendSearchCondition(sc6);
		
		SearchCondition sc7 = new SearchCondition(WfProcess.class, "thePersistInfo.createStamp", SearchCondition.GREATER_THAN, timeStamp);
		querysearch.appendAnd();
		querysearch.appendSearchCondition(sc7);
		
		SearchCondition sc8 = new SearchCondition(WfProcess.class, "state", SearchCondition.LIKE, WfState.CLOSED_COMPLETED_EXECUTED);
		querysearch.appendAnd();
		querysearch.appendSearchCondition(sc8);
		
		OrderBy orderby = new OrderBy(new ClassAttribute(WfProcess.class, "thePersistInfo.createStamp"), true);	//trueΪ����falseΪ����
		querysearch.appendOrderBy(orderby, ai);
		
		queryresult= PersistenceHelper.manager.find(querysearch);		
		return queryresult;
	}	
	
	/**
	 *���Ʊ����ʵ����Ŷӣ���PBO���Ŷӣ�����ǩ�㲿������ǩ�ĵ�/CAD�ĵ���
	 * ���У�PROMOTER��SUBMITTER��ɫ�������и���
	 * 
	 * @param processObj
	 *          �����ʵ��self
	 * @param batchReviewParts
	 *          ��ǩ�㲿������
	 * @param batchReviewDocs
	 *          ��ǩ�ĵ�/CAD�ĵ�����
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void copyRolesToBatchObjects(Object processObj, Vector batchReviewParts, Vector batchReviewDocs)throws WTException
	{
		if(VERBOSE) System.out.println("----->.copyRolesToBatchObjects()");
		WTPrincipal administrator = SessionHelper.manager.getAdministrator ();
		WTPrincipal previous = SessionContext.setEffectivePrincipal ( administrator );
		
		//�����㲿��
		if (batchReviewParts !=null && batchReviewParts.size()!=0)
		{
			Enumeration enuBatchReviewPart = batchReviewParts.elements();
			while(enuBatchReviewPart.hasMoreElements())
			{
				WTPart batchPart=(WTPart)enuBatchReviewPart.nextElement();
				copyRoles(processObj,batchPart);
			}
		}
		
		//�����ĵ�
		if (batchReviewDocs!=null && batchReviewDocs.size()!=0)
		{
			Enumeration enuBatchReviewCad = batchReviewDocs.elements();
			while(enuBatchReviewCad.hasMoreElements())
			{
				WTObject each = (WTObject)enuBatchReviewCad.nextElement();
				if(each instanceof WTDocument)
					copyRoles(processObj,(WTDocument)each);
				else if(each instanceof EPMDocument)
					copyRoles(processObj,(EPMDocument)each);
			}
		}
		
		SessionContext.setEffectivePrincipal(previous);
	}	
	
	/**
	 *���Ʊ����ʵ����Ŷӣ���PBO���Ŷӣ���ĳһ���ض�������
	 * ���У�PROMOTER��SUBMITTER��ɫ�������и���
	 * 
	 * @param processObj
	 *          �����ʵ��self
	 * @param destObj
	 *          ��Ҫ�����Ŷӵ�Ŀ����󣬿����ǲ���/�ĵ�/CAD�ĵ�
	 * @throws WTException
	 * <br>
	 * <br>
	 *             <b>Revision History</b> <br>
	 *             <b>Rev:</b> 1.0 - 2009/02/05, Zita Zhang <br>
	 *             <b>Comment:</b> Initial release.
	 */
	public static void copyRoles(Object processObj,TeamManaged destObj)throws WTException
	{
		if(VERBOSE) System.out.println("--->.copyRoles()");
		
		TeamReference teamref =(TeamReference)destObj.getTeamId();
		if (teamref ==null)
			return;
		
		Team destTeam = (Team)(teamref.getObject());
		if (destTeam==null)
			return;
		
		destTeam= (Team)PersistenceHelper.manager.refresh(destTeam);
		Vector destRoles = destTeam.getRoles();
		if(VERBOSE) System.out.println("    the roles of destObj "+destObj+" team=" + destRoles);
		if((destRoles==null) || (destRoles.size()<=0))
			return;
		
		WfProcess process= WorkflowUtil.getProcess(processObj);
		boolean updated=false;
		if(VERBOSE) System.out.println("get process="+ process);
		if(process instanceof wt.team.TeamManaged)
		{
			Team processTeam = (Team)((TeamReference)((TeamManaged)process).getTeamId()).getObject();
			Vector processRoles =processTeam.getRoles();
			if(VERBOSE) System.out.println("    process(" + process.getIdentity() + ") team roles=" + processRoles);
			
			for(int i=0; i<processRoles.size(); i++)
			{
				String srcRoleName=processRoles.elementAt(i).toString();
				wt.project.Role role = wt.project.Role.toRole(srcRoleName);
				if(srcRoleName.equals("PROMOTER") || srcRoleName.equals("SUBMITTER"))
				{
					continue;
				}
				
				//����Ŷ��в�����
				Enumeration enums = destTeam.getPrincipalTarget(role);
				while (enums != null && enums.hasMoreElements())
				{
					WTPrincipal principal = ((WTPrincipalReference)enums.nextElement()).getPrincipal();
					destTeam.deletePrincipalTarget(role, principal);
					destTeam=(Team)PersistenceHelper.manager.refresh(destTeam);
				}				
				
				Enumeration participants= WorkflowUtil.getRoleEnumeration(processTeam, srcRoleName);
				if((participants!=null) && participants.hasMoreElements())
				{
					updated=true;
					destTeam= (Team)WorkflowUtil.addRolePrincipals(destTeam, srcRoleName, participants);
				}
			}
			if(VERBOSE) System.out.println("  destObj "+destObj+" team updated="+updated );
		}
	}		
	
	public static void main(String[] args) throws Exception,
	WTPropertyVetoException {
		System.out.println("Begin...");
		try {
			WorkflowUtil app = new WorkflowUtil();
			if (SERVER) {
				System.out.println("SERVER");
				Class aclass[] = { array$Ljava$lang$String != null ? array$Ljava$lang$String
						: (array$Ljava$lang$String = classx$("[Ljava.lang.String;")) };
				Object aobj[] = { args };
				RemoteMethodServer.getDefault().invoke("doMyOperations", null,
					app, aclass, aobj);
			} else {
				System.out.println("Not SERVER.");
				app.doMyOperations(args);
			}
		} catch (Exception exception) {
			System.out.println(exception);
			
		} finally {
			System.out.println("End.");
			System.out.flush();
			System.exit(0);
		}
		System.exit(0);
	}
}
