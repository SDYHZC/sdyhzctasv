
package ext.tan.partreport;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.project.Role;
import wt.project._Role;
import wt.session.SessionHelper;
import wt.util.WTException;

import com.ptc.core.ui.validation.DefaultUIComponentValidator;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationResult;
import com.ptc.core.ui.validation.UIValidationResultSet;
import com.ptc.core.ui.validation.UIValidationStatus;

import ext.tan.partreport.ExportConstant;
import ext.tan.partreport.ReadXMLUtil;
import org.apache.log4j.Logger;
import wt.log4j.LogR;

/**
 * 报表入口检验的Validator
 * 
 * @author Administrator
 * 
 */
public class ExportValidator extends DefaultUIComponentValidator {

	private String CLASSNAME = ExportValidator.class.getName();
	private Logger logger = LogR.getLogger(CLASSNAME);

	@Override
	public UIValidationResultSet performFullPreValidation(UIValidationKey validationKey , UIValidationCriteria validationCriteria , Locale locale) throws WTException {

		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;

		WTReference wtreference = validationCriteria.getContextObject();
		WTPart wtpart = (WTPart) wtreference.getObject();

		WTContainerRef containerRef = validationCriteria.getParentContainer();
		WTContainer container = containerRef.getContainer();

		ReadXMLUtil xmlUtil = new ReadXMLUtil(ExportConstant.PATH);

		Vector <String> groupList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_GROUP);
		Vector <String> roleList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_ROLE);
		Vector <String> stateList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_STATE);
		Vector <String> viewList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_VIEW);

		// 当前状态是否匹配XML中的状态
		String state = wtpart.getLifeCycleState().toString();
		System.out.println("当前部件的状态为 = "+state);
		logger.debug("current state = "+state);
		boolean isState = stateList.contains(state);

		// 当前状态是否匹配XML中的视图
		String viewStr = wtpart.getViewName();
		logger.debug("current viewStr = "+viewStr);
		boolean isView = viewList.contains(viewStr);

		// 获取当前用户
		WTUser curUser = (WTUser) SessionHelper.manager.getPrincipal();
		logger.debug("current User = "+curUser);
		WTPrincipalReference curUserRef = WTPrincipalReference.newWTPrincipalReference(curUser);

		// 当前用户是否在XML配置的角色中
		boolean isMember = false;
		int role_size = roleList.size();

		for (int i = 0; i < role_size; i++) {
			String roleStr = roleList.get(i);

			logger.debug("roleStr = "+roleStr);
			Role role = _Role.toRole(roleStr);
			ArrayList <?> userList = OrgUtil.getAllPrincipalsForTarget(container , role);
			if (userList != null && userList.contains(curUserRef)) {
				isMember = true;
				break;
			}
		}
		logger.debug("role Over isMember = "+isMember);
		if (!isMember) {
			// 当前用户是否属于XML配置的组
			int group_size = groupList.size();
			if (roleList.size()==0&&group_size == 0)
				isMember = true;
			for (int j = 0; j < group_size; j++) {
				String groupStr = groupList.get(j);
               
				logger.debug("groupStr = "+groupStr);
				System.out.println("用户所属组为 = "+groupStr);
				isMember = OrgUtil.isMemberOfGroup(groupStr , curUser);
				if (isMember)
					break;
			}
		}

		logger.debug("ext.generic.exportPDF.validator.ExportPDFValidator--------------isState="+isState+"isView="+isView+"isMember="+isMember);
		if (isState)
			
			uivalidationstatus = UIValidationStatus.ENABLED;

		UIValidationResultSet resultSet = new UIValidationResultSet();
		resultSet.addResult(UIValidationResult.newInstance(validationKey , uivalidationstatus));

		return resultSet;
	}

}
