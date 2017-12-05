package ext.tasv.docworkflow;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Locale;

import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.lifecycle.State;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.util.LocalizableMessage;
import wt.util.WTException;
import wt.vc.views.ViewReference;

public class DocWorkflowValidator extends DefaultSimpleValidationFilter
{
  public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey, UIValidationCriteria uivalidationcriteria)
  {
    UIValidationStatus uivalidationstatus = UIValidationStatus.DISABLED;
    WTReference wtreference = uivalidationcriteria.getContextObject();
    if (wtreference != null) {
      Persistable persistable = wtreference.getObject();
      try {
        if ((persistable != null) && ((persistable instanceof WTDocument))) {
          WTDocument doc = (WTDocument)persistable;
           String docType=getDocType(doc);
          if (docType.equals("技术更改通知单")||docType.equals("技术通知单")) {
              uivalidationstatus = UIValidationStatus.HIDDEN;
          }else{
        	  uivalidationstatus = UIValidationStatus.ENABLED;
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    return uivalidationstatus;
  }

  /**
	 * 获取文档类型
	 * @param doc
	 * @return
	 * @throws IOException
	 * @throws WTException
	 */
	
	
  public String getDocType(WTDocument doc) throws IOException, WTException
  {
      String docType = "";
      Locale locale = SessionHelper.manager.getLocale();
      docType = ClientTypedUtility.getLocalizedTypeName(doc, locale);
      return docType;
  }	
  
}