package ext.tan.partreport;

import java.util.Locale;
import java.util.Vector;

import wt.fc.Persistable;
import wt.fc.WTReference;
import wt.part.WTPart;
import wt.util.WTException;

import com.ptc.core.ui.validation.DefaultUIComponentValidator;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationResult;
import com.ptc.core.ui.validation.UIValidationResultSet;
import com.ptc.core.ui.validation.UIValidationStatus;

public class ReportValidator  extends DefaultUIComponentValidator {

	public UIValidationResultSet performFullPreValidation(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria, Locale locale) throws WTException {
		UIValidationStatus uivalidationstatus = UIValidationStatus.HIDDEN;
		UIValidationResultSet uivresultSet = new UIValidationResultSet();
		System.out.println("uivresultSet===="+uivresultSet);
		if (uivalidationcriteria != null && uivalidationcriteria.getContextObject() != null) {
			
			WTReference wtreference = uivalidationcriteria.getContextObject();
			Persistable persistable = wtreference.getObject();
			ReadXMLUtil xmlUtil = new ReadXMLUtil(ExportConstant.PATH);
			Vector <String> stateList = xmlUtil.getBusinessRuleFromXML(ExportConstant.VALIDATOR_STATE);
				
			if (persistable instanceof WTPart) {
				WTPart part = (WTPart) persistable;
				String state = part.getLifeCycleState().toString();		
				boolean isState = stateList.contains(state);
				if(isState){
					uivalidationstatus = uivalidationstatus.ENABLED;
				}			
			}
		}
		uivresultSet.addResult(new UIValidationResult(uivalidationkey, uivalidationstatus));
		return uivresultSet;
	}
	
}
