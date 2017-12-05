package ext.hbt.signature.model;

 
import wt.fc.ObjectReference;
import wt.fc.WTObject;
import wt.util.WTException;

import com.ptc.windchill.annotations.metadata.GenAsPersistable;
import com.ptc.windchill.annotations.metadata.GeneratedProperty;

@GenAsPersistable(superClass = WTObject.class, extendable = true, 
	properties = 
	{
	   @GeneratedProperty(name = "targetTypeRef", type =  ObjectReference.class),//对象的类型
	   @GeneratedProperty(name = "attrName", type = String.class), //签名类型名称（活动的名称或者属性的内部名称）
	   @GeneratedProperty(name = "attrType", type = String.class),//是活动（0）还是属性（1）的名称。
	   @GeneratedProperty(name = "xLocation", type = String.class), //X坐标
	   @GeneratedProperty(name = "yLocation", type = String.class), //Y坐标
	   @GeneratedProperty(name = "fontSize", type = String.class),//字体大小
	   @GeneratedProperty(name = "fontType", type = String.class),//字体类型
	   @GeneratedProperty(name = "contentType", type = String.class),//0：文字，1：图片 
	   @GeneratedProperty(name = "signRange", type= String.class),//打印的范围，第几页或者全部页面
	   @GeneratedProperty(name = "rotation", type = String.class),//打印旋转角度
	   @GeneratedProperty(name = "imageWidth", type= String.class),//图片宽度
	   @GeneratedProperty(name = "imageHeight", type = String.class),//图片高度
	   @GeneratedProperty(name = "attr1", type = String.class),
	   @GeneratedProperty(name = "attr2", type = String.class),
	   @GeneratedProperty(name = "attr3", type = String.class),
	})

public class CoordinateItem extends _CoordinateItem{

	    static final long serialVersionUID = 1;


		public static CoordinateItem newCoordinateItem() throws WTException 
		{
			final CoordinateItem instance = new CoordinateItem();
			instance.initialize();
			return instance;
		}
}

