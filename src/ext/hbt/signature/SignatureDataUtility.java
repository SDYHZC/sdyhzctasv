package ext.hbt.signature;

import wt.fc.ReferenceFactory;
import wt.util.WTException;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.PushButton;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;

import ext.hbt.reviewobjecttype.model.TypeItem;
import ext.hbt.signature.model.CoordinateItem;

public class SignatureDataUtility extends AbstractDataUtility {

	public Object getDataValue(String s, Object obj, ModelContext modelcontext) throws WTException
	{
		if (s.equals("hbt_sign_objTypeName"))
		{
			if (obj instanceof TypeItem)
			{
				TypeItem item = (TypeItem) obj;
				String disp = item.getTypeName();
				PushButton button = new PushButton(disp);
				ReferenceFactory referencefactory = new ReferenceFactory();
				String oid = referencefactory.getReferenceString(item);
				button.setId("sign_objTypeName_" + oid);
				button.setName("sign_objTypeName_" + oid);
				button.addJsAction("onClick", "reloadCoordinateItemTable('" + oid + "')");
				button.setValue(disp);
				return button;
			}
			else if (obj instanceof CoordinateItem)
			{
				CoordinateItem item = (CoordinateItem) obj;
				TypeItem typeitem = (TypeItem) item.getTargetTypeRef().getObject();;
				TextDisplayComponent textdisplaycomponent = new TextDisplayComponent("");
				textdisplaycomponent.setValue(typeitem.getTypeName());
				return textdisplaycomponent;
			}
		}
		
		return TextDisplayComponent.NBSP;
	}
}
