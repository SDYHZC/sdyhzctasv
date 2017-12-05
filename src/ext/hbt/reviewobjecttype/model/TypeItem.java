package ext.hbt.reviewobjecttype.model;

import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.util.WTException;

import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;

@GenAsPersistable(
	superClass = WTObject.class, extendable = true, 
	properties = 
	{
		@GeneratedProperty(name = "itemType", type = String.class),//类型
		@GeneratedProperty(name = "fullTypeName", type = String.class), 
		@GeneratedProperty(name = "typecomment", type = String.class),  
		@GeneratedProperty(name = "parentTypeItem", type = ObjectReference.class),  
		@GeneratedProperty(name = "typeName", type = String.class), 
		@GeneratedProperty(name = "objType1", type = String.class), 
		@GeneratedProperty(name = "objType2", type = String.class), 
		@GeneratedProperty(name = "objType3", type = String.class),
		@GeneratedProperty(name = "objType4", type = String.class),
		@GeneratedProperty(name = "objType5", type = String.class),
	})

public class TypeItem extends _TypeItem
{
	    static final long serialVersionUID = 1;
		public static TypeItem newTypeItem() throws WTException 
		{
			final TypeItem instance = new TypeItem();
			instance.initialize();
			return instance;
		}
}

