package ext.hbt.reviewobjecttype;

import wt.util.WTException;

import com.ptc.core.components.descriptor.ModelContext;
import com.ptc.core.components.factory.AbstractDataUtility;
import com.ptc.core.components.rendering.guicomponents.TextDisplayComponent;

import ext.hbt.reviewobjecttype.model.TypeItem;

public class ObjectTypeDataUtility extends AbstractDataUtility {

 

	public Object getDataValue(String s, Object obj, ModelContext modelcontext) throws WTException
	{
		if (obj instanceof TypeItem)
		{
			TypeItem item = (TypeItem) obj;
			if (s.equals("fullTypeName"))
			{
				TextDisplayComponent textdisplaycomponent = new TextDisplayComponent("");
				String value=item.getFullTypeName();
				value = value.replaceAll(ObjectTypeConstant.TYPE_SPLIT_IN, ObjectTypeConstant.TYPE_SPLIT_OUT);
				textdisplaycomponent.setValue(value);
				return textdisplaycomponent;
			}
		}
		return TextDisplayComponent.NBSP;
	}
}
