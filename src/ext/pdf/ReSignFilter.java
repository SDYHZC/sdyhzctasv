package ext.pdf;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;
import org.apache.log4j.Logger;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.WTReference;
import wt.log4j.LogR;

public class ReSignFilter extends DefaultSimpleValidationFilter
{
  private static final String CLASSNAME = ReSignFilter.class.getName();
  private static final Logger logger = LogR.getLogger(CLASSNAME);

  public UIValidationStatus preValidateAction(UIValidationKey validationKey, UIValidationCriteria validationCriteria)
  {
    UIValidationStatus status = UIValidationStatus.HIDDEN;
    String action = validationKey.getComponentID();

    logger.debug("action >>>>>>>>>>>>>>>>> " + action);

    if ((!validationCriteria.isOrgAdmin()) && (!validationCriteria.isSiteAdmin())) {
      logger.debug("user >>>>>>>>>>>>>>>>>  普通用户  >>>>>>>");
      return UIValidationStatus.HIDDEN;
    }
    Object obj = validationCriteria.getContextObject().getObject();
    logger.debug("obj >>>>>>>>>>>>>>>>> " + obj);
    if (action.equalsIgnoreCase("reSignPDF"))
    {
      if ((obj instanceof WTDocument))
        status = UIValidationStatus.ENABLED;
      else if ((obj instanceof EPMDocument))
      {
        if (!ReSignHelper.isDWG((EPMDocument)obj))
          status = UIValidationStatus.ENABLED;
      }
    }
    else if (action.equalsIgnoreCase("reSignPDF1"))
    {
    	if ((obj instanceof WTDocument))
            status = UIValidationStatus.ENABLED;
    }
    else if (action.equalsIgnoreCase("reSignDwg"))
    {
      if (((obj instanceof EPMDocument)) && 
        (ReSignHelper.isDWG((EPMDocument)obj))) {
        status = UIValidationStatus.ENABLED;
      }
    }

    return status;
  }
}