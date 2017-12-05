package ext.tasv.pdfexport.validator;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.tasv.pdfexport.util.ExportConstant;
import ext.tasv.pdfexport.util.OrgUtil;
import ext.tasv.pdfexport.util.ReadXMLUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import wt.change2.WTChangeOrder2;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.State;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.project._Role;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.util.LocalizableMessage;
import wt.util.WTException;
import wt.vc.views.ViewReference;
import org.apache.log4j.Logger;
import wt.log4j.LogR;

public class ExportPDFValidator extends DefaultSimpleValidationFilter
{
  public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria)
  {
    UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
    WTReference wtreference = uivalidationcriteria.getContextObject();
	WTChangeOrder2 wtchange = (WTChangeOrder2) wtreference.getObject();

	WTContainerRef containerRef = uivalidationcriteria.getParentContainer();
	@SuppressWarnings("deprecation")
	WTContainer container = containerRef.getContainer();

	ReadXMLUtil xmlUtil = new ReadXMLUtil(ExportConstant.PATH);

	Vector<String> groupList = null;
	try {
		groupList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_GROUP);
	} catch (WTException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	Vector<String> roleList = null;
	try {
		roleList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_ROLE);
	} catch (WTException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	Vector<String> stateList = null;
	try {
		stateList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_STATE);
	} catch (WTException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	//Vector <String> viewList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_VIEW);

	// 当前状态是否匹配XML中的状态
	/*String state = wtchange.getLifeCycleState().toString();
//	Logger.debug("current state = "+state);
	boolean isState = stateList.contains(state);*/

	// 获取当前用户
	WTUser curUser = null;
	try {
		curUser = (WTUser) SessionHelper.manager.getPrincipal();
	} catch (WTException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
//	logger.debug("current User = "+curUser);
	WTPrincipalReference curUserRef = null;
	try {
		curUserRef = WTPrincipalReference.newWTPrincipalReference(curUser);
	} catch (WTException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

	// 当前用户是否在XML配置的角色中
	boolean isMember = false;
	int role_size = roleList.size();

	for (int i = 0; i < role_size; i++) {
		String roleStr = roleList.get(i);

		//logger.debug("roleStr = "+roleStr);
		Role role = _Role.toRole(roleStr);
		ArrayList<?> userList = null;
		try {
			userList = OrgUtil.getAllPrincipalsForTarget(container , role);
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (userList != null && userList.contains(curUserRef)) {
			isMember = true;
			break;
		}
	}
//	logger.debug("role Over isMember = "+isMember);
	if (!isMember) {
		// 当前用户是否属于XML配置的组
		int group_size = groupList.size();
		if (roleList.size()==0&&group_size == 0)
			isMember = true;
		for (int j = 0; j < group_size; j++) {
			String groupStr = groupList.get(j);

		//	logger.debug("groupStr = "+groupStr);
			isMember = OrgUtil.isMemberOfGroup(groupStr , curUser);
			if (isMember)
				break;
		}
	}

	System.out.println("ext.generic.exportPDF.validator.ExportPDFValidator---isState="+"isMember="+isMember);
	if (isMember)
		uivalidationstatus = UIValidationStatus.ENABLED;
    return uivalidationstatus;
  }

}