package ext.tan.partreport;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.federation.FederationUtilities;
import wt.federation.PrincipalManager.DirContext;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContainer;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.inf.team.ContainerTeamManagedInfo;
import wt.inf.team.ContainerTeamReference;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.ufid.FederatableServerHelper;
import wt.ufid.Repository;
import wt.ufid.StandardUfidSrvService;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTStringUtilities;

import com.infoengine.SAK.Task;
import com.infoengine.object.factory.Group;

public class OrgUtil implements Serializable, RemoteAccess {
	private static String CLASSNAME = "ext.com.org.OrgUtil";
	private static boolean VERBOSE = false;
	private static WTProperties wtProperties;
	static {
		try { 
			wtProperties = WTProperties.getLocalProperties();
			VERBOSE = wtProperties.getProperty("ext.com.org.verbose", false);
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * 获取用户直接所属用户组；
	 * 注意：该用户组是指组织下定义的用户组
	 * 系统中存在对应的API，但是在Windchill9.1系统中发现存在严重的效率问题，此方法修复了效率问题
	 * @param wtprincipal：参与者，可以是用户或者是组
	 * @return
	 * @throws Exception
	 */
	public static ArrayList immediateParentGroups(WTPrincipal wtprincipal) throws Exception {
		
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ArrayList) RemoteMethodServer.getDefault().invoke("immediateParentGroups", CLASSNAME, null, new Class[]
				        { WTPrincipal.class }, new Object[]
				        { wtprincipal });
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}		
		ArrayList list = new ArrayList<WTPrincipal>();		
			WTGroup wtgroup = null;
			if (wtprincipal.isDisabled())
				return list;
			String s = wtprincipal.getDn();
			if (wtprincipal instanceof WTUser) {
				WTOrganization wtorganization = OrganizationServicesHelper.manager.getOrganization(wtprincipal);
				if (wtorganization != null) {
					WTPrincipalReference wtprincipalreference = WTPrincipalReference.newWTPrincipalReference(wtorganization);
				} else {
					throw new WTException("没有找到" + wtprincipal.getName() + "的所属组织!");
				}
			}
			if (wtgroup == null || !wtgroup.isInternal()) {
				String as[] = _getAllServices();
				StandardUfidSrvService standardufidsrvservice = (StandardUfidSrvService) FederatableServerHelper.service;
				label0:
				for (int i = 0; i < as.length; i++) {
					Group group = queryParents(s, as[i]);
					if (group == null)
						continue;
					int j = group.getElementCount();
					//System.out.println("group.getElementCount()="+j);
					Repository repository = FederatableServerHelper.getRepository(DirContext.getDomain(as[i]));
					for (int k = 0; k < j; k++) {
						String s1 = (String) group.getAttributeValue(k, "object");
						//System.out.println("s1="+s1);
						WTPrincipal principal = OrganizationServicesHelper.manager.getPrincipalByDN(s1);
						if (principal instanceof WTGroup) {
							//System.out.println(principal.getName());
							list.add(principal);
						}
					}
				}
			}		
		
			 return list;		
		
	}
	/**
	 * 获取Ldap目录服务
	 * @return
	 * @throws Exception
	 */
	private static String[] _getAllServices() throws Exception {
		WTProperties wtproperties = WTProperties.getLocalProperties();
		String DIRECTORY_SERVICES = wtproperties.getProperty("wt.federation.org.directoryServices");
		ArrayList arraylist = new ArrayList(2);	
		
		if (DIRECTORY_SERVICES == null) {
			Enumeration enumeration = DirContext.getJNDIAdapterNames();
			do {
				if (!enumeration.hasMoreElements())
					break;
				String s = (String) enumeration.nextElement();
				arraylist.add(s);
			} while (true);
		} else {
			StringTokenizer stringtokenizer = new StringTokenizer(DIRECTORY_SERVICES, ",");
			do {
				if (!stringtokenizer.hasMoreTokens())
					break;
				String s1 = DirContext.getPrimaryName(stringtokenizer.nextToken().trim());
				if (s1 != null) {
					arraylist.add(s1);
				}
			} while (true);
		}		
			String as[] = (String[]) (String[]) arraylist.toArray(new String[arraylist.size()]);
			return as;
		
	}

	/**
	 * 根据给定的用户id获得用户
	 * @param  userid :用户名称
	 * @return 返回用户名称对应的用户，如果用户id无效，则返回空
	 * @throws WTException
	 **/
	public static WTUser getUserById(String userid) throws WTException {
		WTUser user = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(WTUser.class);
		if (userid.equalsIgnoreCase("wcadmin"))
			userid = "Administrator";
		SearchCondition sc = new SearchCondition(WTUser.class, "name", SearchCondition.LIKE, userid, false);
		qs.appendSearchCondition(sc);
		qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			user = (WTUser) qr.nextElement();
			break;
		}
		return user;
	}
	
	/**
	 * @param s userDn
	 * @param s1 LDAP目录服务
	 * @return Info Engine输出结果集
	 * @throws Exception
	 */
	private static Group queryParents(String s, String s1) throws Exception {
		String s2;
		String s3;
		int i;
		String s4;
		String s5;
		boolean flag;
		s2 = _getObjectClass(s1, wt.org.WTGroup.class);
		s3 = _getObjectClass(s1, wt.org.WTOrganization.class);
		i = 0;
		s4 = null;
		s5 = null;
		flag = false;
		if (!s2.equals("")) {
			i++;
			s4 = DirContext.getMapping(s1, "group.uniqueMember");
		}
		if (!s3.equals("")) {
			i++;
			s5 = DirContext.getMapping(s1, "org.uniqueMember");
		}
		if (i < 1)
			return null;
		StringBuffer stringbuffer;
		Task task;
		boolean flag1 = false;
		stringbuffer = new StringBuffer(128);
		String s6 = FederationUtilities.escapeFilter(s);
		s6 = WTStringUtilities.insertEscapeChars(s6, "*");
		if (i > 1) {
			flag = s4.equals(s5);
			if (flag) {
				stringbuffer.append("(&(");
				stringbuffer.append(s4);
				stringbuffer.append('=');
				stringbuffer.append(s6);
				stringbuffer.append(')');
			}
		}
		if (i > 1)
			stringbuffer.append("(|");
		if (!s2.equals("")) {
			String s7 = DirContext.getMapping(s1, "group.filter", null);
			if (!flag || s7 != null)
				stringbuffer.append("(&");
			stringbuffer.append("(objectClass=");
			stringbuffer.append(s2);
			stringbuffer.append(')');
			if (!flag) {
				stringbuffer.append('(');
				stringbuffer.append(s4);
				stringbuffer.append('=');
				stringbuffer.append(s6);
				stringbuffer.append(')');
			}
			if (s7 != null)
				if (s7.startsWith("(")) {
					stringbuffer.append(s7);
				} else {
					stringbuffer.append('(');
					stringbuffer.append(s7);
					stringbuffer.append(')');
				}
			if (!flag || s7 != null)
				stringbuffer.append(')');
		}
		if (!s3.equals("")) {
			String s8 = DirContext.getMapping(s1, "org.filter", null);
			if (!flag || s8 != null)
				stringbuffer.append("(&");
			stringbuffer.append("(objectClass=");
			stringbuffer.append(s3);
			stringbuffer.append(')');
			if (!flag) {
				stringbuffer.append('(');
				stringbuffer.append(s5);
				stringbuffer.append('=');
				stringbuffer.append(s6);
				stringbuffer.append(')');
			}
			if (s8 != null)
				if (s8.startsWith("(")) {
					stringbuffer.append(s8);
				} else {
					stringbuffer.append('(');
					stringbuffer.append(s8);
					stringbuffer.append(')');
				}
			if (!flag || s8 != null)
				stringbuffer.append(')');
		}
		if (i > 1)
			stringbuffer.append(')');
		if (flag)
			stringbuffer.append(')');

		//task = new Task(QUERY_PRINCIPALS_TASK);
		task = new Task("/wt/federation/QueryPrincipals.xml");
		try {
			flag1 = SessionServerHelper.manager.setAccessEnforced(false);
			//SessionHelper.manager.setAdministrator();
			//task.setUsername(SessionHelper.manager.getPrincipal().getName());
			task.setUsername("Administrator");		
			task.addParam("instance", s1);
			String s9 = DirContext.getJNDIAdapterSearchBase(s1);
			if (s9 != null)
				task.addParam("base", s9);
			task.addParam("searchFilter", stringbuffer.toString());
			task.addParam("scope", "subtree");
			task.addParam("attribute", "objectClass");
			task.addParam("group_out", "parents");
			task.invoke();
		} catch (Exception exception) {
			throw new WTException(exception);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag1);
			return task.getGroup("parents");
		}
	}

	private static String _getObjectClass(String s, Class class1) throws Exception {
		String s1;
		s1 = "";
		if (wt.org.WTUser.class.isAssignableFrom(class1))
			s1 = DirContext.getMapping(s, "user.objectClass", "inetOrgPerson");
		else if (wt.org.WTOrganization.class.isAssignableFrom(class1))
			s1 = DirContext.getMapping(s, "org.objectClass", "ptcOrganization");
		else if (wt.org.WTGroup.class.isAssignableFrom(class1))
			s1 = DirContext.getMapping(s, "group.objectClass", "groupOfUniqueNames");
		return s1;
	}

	/**
	 * 根据组名获取组对象
	 * @param name：组名称
	 * @return
	 * @throws WTException
	 */
	public static WTGroup getGroupByName(String name) throws WTException {
		WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
		WTGroup group = getGroupByName(OrganizationServicesHelper.manager.getOrganization(user), name);
		if (group == null)
			group = getGroupByName(null, name);
		return group;
	}

	/**
	 * 根据组名称以及组织获取组
	 * @param org：组织
	 * @param name：组名称
	 * @return
	 * @throws WTException
	 */
	public static WTGroup getGroupByName(WTOrganization org, String name) throws WTException {
		if (VERBOSE)
			System.out.println(">>>" + CLASSNAME + ".getGroupByName()---name=" + name);
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(WTGroup.class);
		//根据组名称查询
		SearchCondition sc = new SearchCondition(WTGroup.class, "name", SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc);
		//根据组的类型过滤
		SearchCondition sc2 = new SearchCondition(WTGroup.class, "internal", SearchCondition.IS_FALSE);
		qs.appendAnd();
		qs.appendSearchCondition(sc2);
		if (org != null) {
			QuerySpec qsx = new QuerySpec(OrgContainer.class);
			;
			SearchCondition scx = new SearchCondition(OrgContainer.class, OrgContainer.NAME, SearchCondition.EQUAL, org.getName());
			qsx.appendSearchCondition(scx);
			QueryResult qrx = PersistenceHelper.manager.find(qsx);
			WTContainer orgContainer = (WTContainer) qrx.nextElement();
			if (VERBOSE)
				System.out.println("    org container=" + orgContainer.getName());
			ReferenceFactory rf = new ReferenceFactory();
			ObjectIdentifier objId = ObjectIdentifier.newObjectIdentifier(rf.getReferenceString(orgContainer));
			SearchCondition sc3 = new SearchCondition(WTGroup.class, "containerReference.key", SearchCondition.EQUAL, objId);
			qs.appendAnd();
			qs.appendSearchCondition(sc3);
		}
		qr = PersistenceHelper.manager.find(qs);
		if (VERBOSE)
			System.out.println(">>>" + CLASSNAME + ".getGroupByName()---PersistenceHelper.manager.find count =" + qr.size());
		WTGroup group = null;
		if (qr.hasMoreElements())
			group = (WTGroup) qr.nextElement();

		if (VERBOSE && group != null)
			System.out.println("<<<" + CLASSNAME + ".getGroupByName()---group=" + group.getName());
		return group;
	}

	/**
	 * 判断指定用户是否属于指定用户组的成员;该用户组可能是用户所在组织下的用户组;如果用户不属于任何组织,则从站点下的用户组查询指定的用户组;
	 * @param groupName 用户组名
	 * @param user 用户
	 * @return 用户是否属于指定用户组
	 * @author Harry Cao
	 */
	public static boolean isMemberOfGroup(String groupName, WTUser user) {
		boolean flag = false;
		try {
			WTGroup group = getGroupByName(OrganizationServicesHelper.manager.getOrganization(user), groupName);
			if (group == null)
				group = getGroupByName(null, groupName);

			if (group != null)
				flag = group.isMember(user);
		} catch (WTException wte) {
			wte.printStackTrace();
			flag = false;
		}
		if (VERBOSE)
			System.out.println("<<<<" + CLASSNAME + ".isMemberOfGroup()--groupName=" + groupName + "; user=" + user.getFullName() + "; return=" + flag);
		return flag;
	}

	/**
	 * 获取容器团队中某个角色的成员，此方法支持有共享团队的容器
	 * @param container
	 * @param role
	 * @return
	 * @throws WTException
	 * @author ZhiChao Yuan
	 */
	public static ArrayList getAllPrincipalsForTarget(WTContainer container, Role role) throws WTException {
		if ((container instanceof ContainerTeamManaged) && role != null) {
			ContainerTeamManaged ctm = (ContainerTeamManaged) container;
			ContainerTeamManagedInfo containerteammanagedinfo = ctm.getContainerTeamManagedInfo();
			ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(ctm);
			if (containerteammanagedinfo.getSharedTeamId() != null && containerteammanagedinfo.getSharedTeamId().getKey() != null &&
			        containerteammanagedinfo.getSharedTeamId().getKey().getClassname() != null) {
				//有共享团队
				ArrayList localWtps = containerTeam.getAllPrincipalsForTarget(role);
				//System.out.println("localWtps:"+localWtps);
				ArrayList allWtps = localWtps;
				ContainerTeamReference containerTeamRef = ctm.getSharedTeamReference();
				if (containerTeamRef != null) {
					ContainerTeam sharedTeam = (ContainerTeam) containerTeamRef.getObject();
					if (sharedTeam != null) {
						ArrayList sharedWtps = sharedTeam.getAllPrincipalsForTarget(role);
						//System.out.println("sharedWtps:"+sharedWtps);
						for (int i = 0; i < sharedWtps.size(); i++) {
							WTPrincipalReference wtpRef = (WTPrincipalReference) sharedWtps.get(i);
							if (!allWtps.contains(wtpRef)) {
								allWtps.add(wtpRef);
							}
						}
					}
				}
				return allWtps;
			} else {
				//无共享团队
				return containerTeam.getAllPrincipalsForTarget(role);
			}
		} else {
			return null;
		}
	}

	/**
	 * 获取容器团队中所有的角色，此方法支持有共享团队的容器
	 * @param container
	 * @return
	 * @throws WTException
	 * @author ZhiChao Yuan
	 */
	public static Vector getRoles(WTContainer container) throws WTException {
		if ((container instanceof ContainerTeamManaged)) {
			ContainerTeamManaged ctm = (ContainerTeamManaged) container;
			ContainerTeamManagedInfo containerteammanagedinfo = ctm.getContainerTeamManagedInfo();
			ContainerTeam containerTeam = ContainerTeamHelper.service.getContainerTeam(ctm);
			if (containerteammanagedinfo.getSharedTeamId() != null && containerteammanagedinfo.getSharedTeamId().getKey() != null &&
			        containerteammanagedinfo.getSharedTeamId().getKey().getClassname() != null) {
				//有共享团队
				Vector localRoles = containerTeam.getRoles();
				//System.out.println("localRoles:"+localRoles);
				Vector allRoles = localRoles;
				ContainerTeamReference containerTeamRef = ctm.getSharedTeamReference();
				if (containerTeamRef != null) {
					ContainerTeam sharedTeam = (ContainerTeam) containerTeamRef.getObject();
					if (sharedTeam != null) {
						Vector sharedRoles = sharedTeam.getRoles();
						//System.out.println("sharedRoles:"+sharedRoles);
						for (int i = 0; i < sharedRoles.size(); i++) {
							Role role = (Role) sharedRoles.get(i);
							if (!allRoles.contains(role)) {
								allRoles.add(role);
							}
						}
					}
				}
				return allRoles;
			} else {
				//无共享团队
				return containerTeam.getRoles();
			}
		} else {
			return null;
		}
	}

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
	
	//	WTPrincipal  prin =SessionHelper.manager.getPrincipal();
		//try{
		
		WTPrincipal prin=getUserById("demo");
		if(prin==null){
			System.out.println(">>>为空");
		}else{
		  System.out.println("**************prin=" + prin.getOrganization());
			 ArrayList arry = immediateParentGroups(prin);
			    for (int i = 0; i < arry.size(); i++)
			      System.out.println(">>>>>>>>org=" + arry.get(i));
			  }		
		//}finally{
			//SessionHelper.manager.setPrincipal(princ.getName());
		//}
	}
		
	
}
