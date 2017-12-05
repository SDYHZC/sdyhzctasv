package ext.hbt;

import java.util.Locale;
import wt.org.WTPrincipal;
import wt.session.SessionHelper;
import wt.util.WTException;
import com.ptc.core.ui.validation.DefaultUIComponentValidator;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationResult;
import com.ptc.core.ui.validation.UIValidationResultSet;
import com.ptc.core.ui.validation.UIValidationStatus;

public class SignatureValidator extends DefaultUIComponentValidator {

	@SuppressWarnings("deprecation")
	public UIValidationResultSet performFullPreValidation(
			UIValidationKey uivalidationkey,
			UIValidationCriteria uivalidationcriteria, Locale locale)
			throws WTException {
		UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
		UIValidationResultSet uivresultSet = new UIValidationResultSet();
		System.out.println("uivresultSet====" + uivresultSet);
	//	if (uivalidationcriteria != null
		//		&& uivalidationcriteria.getContextObject() != null) {

			try {

				WTPrincipal wtPrincipal = SessionHelper.manager.getPrincipal();
				if (wtPrincipal != null) {
					String wtPrincipalName = wtPrincipal.getName();
					System.out.println("当前用户为"+wtPrincipalName);
					if (wtPrincipalName.equalsIgnoreCase("Administrator")
							|| wtPrincipalName.equalsIgnoreCase("orgadmin")) {
						uivalidationstatus = UIValidationStatus.ENABLED;
					}
				}
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
		uivresultSet.addResult(new UIValidationResult(uivalidationkey,
				uivalidationstatus));
		return uivresultSet;
	}

}
