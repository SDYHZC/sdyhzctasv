package ext.hbt.addStandardizationGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityException;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkflowHelper;

public class addStandardizationGroup {

	private static final Logger logger = LogR
			.getLogger(addStandardizationGroup.class.getName());

	/**
	 * 通过组名获得组下所有用户
	 * 
	 * @param groupName
	 *            组名
	 * @param users
	 *            用户列表
	 * @return 用户集合
	 * @throws WTException
	 *             wte
	 */
	private static WTArrayList getUsersByGroupName(String groupName) throws WTException {
		WTGroup group = getGroupByName(groupName);
		WTArrayList users=new WTArrayList();
		System.out.println("=====get Group========" + group);
		if (group != null) {
			Enumeration emm = OrganizationServicesHelper.manager.members(group,
					true);
			System.out.println("=====get Group emm========" + emm);
			while (emm.hasMoreElements()) {
				Persistable persistable = (Persistable) emm.nextElement();
				System.out.println("=====get Group persistable========" + persistable);
				if (persistable instanceof WTPrincipal) {
					WTPrincipal wtp = (WTPrincipal) persistable;
					if (wtp instanceof WTUser) {
						WTUser user = (WTUser) wtp;
						System.out.println("=====get Group user========" + user);
							users.add(user);
							System.out.println("=====get Group users========" + users);
					}
				}
			}
		} else {
			System.out.println(">>>>>>>>>>没有找到系统组： " + groupName);
		}
		return users;
	}

	/**
	 * 根据组名获取组对象
	 * 
	 * @param name
	 *            ：组名称
	 * @return
	 * @throws WTException
	 */
	public static WTGroup getGroupByName(String name) throws WTException {
		WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
		WTGroup group = getGroupByName(
				OrganizationServicesHelper.manager.getOrganization(user), name);
		if (group == null)
			group = getGroupByName(null, name);
		return group;
	}

	/**
	 * 根据组名称以及组织获取组
	 * 
	 * @param org
	 *            ：组织
	 * @param name
	 *            ：组名称
	 * @return
	 * @throws WTException
	 */
	public static WTGroup getGroupByName(WTOrganization org, String name)
			throws WTException {

		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(WTGroup.class);
		// 根据组名称查询
		SearchCondition sc = new SearchCondition(WTGroup.class, "name",
				SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc);
		// 根据组的类型过滤
		SearchCondition sc2 = new SearchCondition(WTGroup.class, "internal",
				SearchCondition.IS_FALSE);
		qs.appendAnd();
		qs.appendSearchCondition(sc2);
		if (org != null) {
			QuerySpec qsx = new QuerySpec(OrgContainer.class);
			;
			SearchCondition scx = new SearchCondition(OrgContainer.class,
					OrgContainer.NAME, SearchCondition.EQUAL, org.getName());
			qsx.appendSearchCondition(scx);
			QueryResult qrx = PersistenceHelper.manager.find(qsx);
			WTContainer orgContainer = (WTContainer) qrx.nextElement();

			ReferenceFactory rf = new ReferenceFactory();
			ObjectIdentifier objId = ObjectIdentifier.newObjectIdentifier(rf
					.getReferenceString(orgContainer));
			SearchCondition sc3 = new SearchCondition(WTGroup.class,
					"containerReference.key", SearchCondition.EQUAL, objId);
			qs.appendAnd();
			qs.appendSearchCondition(sc3);
		}
		qr = PersistenceHelper.manager.find(qs);

		WTGroup group = null;
		if (qr.hasMoreElements())
			group = (WTGroup) qr.nextElement();

		return group;
	}

	/**
	 * 根据组名获取组内所有用户
	 * 
	 * @param name
	 *            ：组名称
	 * @return
	 * @throws WTException
	 */
	/*
	 * public static WTArrayList getUsersByGroupName(String name) throws
	 * WTException { WTArrayList arraylist=new WTArrayList(); WTUser user =
	 * (WTUser) SessionHelper.manager.getPrincipal(); WTGroup group =
	 * getGroupByNameAndOrg
	 * (OrganizationServicesHelper.manager.getOrganization(user), name);
	 * System.out.println("=======group=========="+group); if (group == null){
	 * group = getGroupByNameAndOrg(null, name); arraylist=null; }else{
	 * Enumeration members = group.members(); while (members != null &&
	 * members.hasMoreElements()) { WTPrincipal wtp = ((WTPrincipal)
	 * members.nextElement()); arraylist.add(wtp);
	 * System.out.println("=======arraylist=========="+arraylist); } } return
	 * arraylist; }
	 */

	/**
	 * 流程开始时强制添加标准化组到流程角色“收件人”
	 * 
	 * @param publishtoS
	 * @param publishtoC
	 * @param publishtoD
	 * @throws WTException
	 * @throws IOException
	 */

	public static void addStandardizationGroup(WTObject pbo,
			ObjectReference self) throws WTException, IOException {
		boolean bool = SessionServerHelper.manager.isAccessEnforced();
		try {
			SessionServerHelper.manager.setAccessEnforced(false);
			WfProcess process = WorkflowUtil.getProcess(self);
			Team team = (Team) process.getTeamId().getObject();
			String rolename = "RECIPIENT";
			Role role = Role.toRole(rolename);
			System.out.println("=========role========" + role);
			ArrayList oldRoles = new ArrayList();
			Enumeration participants = team.getPrincipalTarget(role);
			while ((participants != null) && participants.hasMoreElements()) {
				System.out.println("=========get user========" + participants);
				Persistable persistable = ((WTPrincipalReference) participants
						.nextElement()).getObject();
				System.out
						.println("=========persistable========" + persistable);
				if (persistable instanceof WTPrincipal) {
					WTPrincipal wtp = (WTPrincipal) persistable;
					if (wtp instanceof WTUser) {
						WTUser user = (WTUser) wtp;
						System.out
								.println("    performSetWfaugment   old user==="
										+ user.getFullName());
						oldRoles.add(user);
					}
				}
			}
			
			boolean haschange = false;
			WTArrayList userForAdd = new WTArrayList();
			userForAdd=getUsersByGroupName("技术标准化部_接收组");
			System.out.println("=========userForAdd========" + userForAdd);
			for (int i = 0; i < userForAdd.size(); ++i) {
				Persistable persistable = (Persistable) userForAdd.getPersistable(i);
				if (persistable instanceof WTUser) {
					WTUser user = (WTUser) persistable;
					// 用户如果已经存在，则不进行添加
					if (!oldRoles.contains(user)) {
						team.addPrincipal(role, user);
						haschange = true;
					}
				}
			}

			WTArrayList userForAdd1=new WTArrayList();;
			userForAdd1=getUsersByGroupName("备份_接收组");
			System.out.println("=========userForAdd1========" + userForAdd1);
			for (int  j= 0; j < userForAdd.size(); ++j) {
				Persistable persistable = (Persistable) userForAdd1.getPersistable(j);
				if (persistable instanceof WTUser) {
					WTUser user = (WTUser) persistable;
					// 用户如果已经存在，则不进行添加
					if (!oldRoles.contains(user)) {
						team.addPrincipal(role, user);
						haschange = true;
					}
				}
			}
			
			
			if (haschange) {
				updateTeam(team, (LifeCycleManaged) pbo);
			}
		} finally {
			SessionServerHelper.manager.setAccessEnforced(bool);
		}
	}

	/**
	 * 获得流程的某个活动节点的WfAssignedActivity对象
	 * 
	 * @param wfprocess
	 *            流程对象
	 * @param activity
	 *            活动名
	 * @return WfAssignedActivity对象
	 */
	private static WfAssignedActivity getWfAssignedActivity(
			WfProcess wfprocess, String activity) {
		if (wfprocess == null || activity == null
				|| activity.trim().length() == 0) {
			return null;
		}
		try {
			QueryResult qr = wfprocess.getContainerNodes();
			while (qr.hasMoreElements()) {
				Object obj = qr.nextElement();
				if (obj instanceof WfAssignedActivity) {
					WfAssignedActivity wfaa = (WfAssignedActivity) obj;
					if (activity.equals(wfaa.getName().trim())) {
						return wfaa;
					}
				}
			}
		} catch (WTException e) {
			e.getMessage();
		}
		return null;
	}

	// 更新团队
	public static void updateTeam(Team paramTeam,
			LifeCycleManaged paramLifeCycleManaged) throws WTException {
		paramTeam = (Team) PersistenceHelper.manager.refresh(paramTeam);
		paramTeam = (Team) PersistenceHelper.manager.save(paramTeam);
		TeamReference localTeamReference;
		if (paramLifeCycleManaged != null) {
			localTeamReference = TeamReference.newTeamReference(paramTeam);
			TeamHelper.service.augmentRoles(paramLifeCycleManaged,
					localTeamReference);
		} else {
			localTeamReference = TeamReference.newTeamReference(paramTeam);
			WorkflowHelper.service.doDynamicUpdate(localTeamReference);
		}
	}

	/**
	 * 根据组名称以及组织获取组
	 * 
	 * @param org
	 *            ：组织
	 * @param name
	 *            ：组名称
	 * @return
	 * @throws WTException
	 */
	public static WTGroup getGroupByNameAndOrg(WTOrganization org, String name)
			throws WTException {
		WTGroup group = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(WTGroup.class);
		// 根据组名称查询
		SearchCondition sc = new SearchCondition(WTGroup.class, "name",
				SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc);
		// 根据组的类型过滤
		SearchCondition sc2 = new SearchCondition(WTGroup.class, "internal",
				SearchCondition.IS_FALSE);
		qs.appendAnd();
		qs.appendSearchCondition(sc2);
		if (org != null) {
			QuerySpec qsx = new QuerySpec(OrgContainer.class);
			SearchCondition scx = new SearchCondition(OrgContainer.class,
					OrgContainer.NAME, SearchCondition.EQUAL, org.getName());
			qsx.appendSearchCondition(scx);
			QueryResult qrx = PersistenceHelper.manager.find(qsx);
			WTContainer orgContainer = (WTContainer) qrx.nextElement();
			ReferenceFactory rf = new ReferenceFactory();
			ObjectIdentifier objId = ObjectIdentifier.newObjectIdentifier(rf
					.getReferenceString(orgContainer));
			SearchCondition sc3 = new SearchCondition(WTGroup.class,
					"containerReference.key", SearchCondition.EQUAL, objId);
			qs.appendAnd();
			qs.appendSearchCondition(sc3);
		}
		qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			group = (WTGroup) qr.nextElement();

		return group;
	}
}
