package ext.pdf;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.*;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.*;
import wt.iba.value.litevalue.*;
import wt.iba.value.service.*;
import wt.method.RemoteMethodServer;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
/**
 * 此类中方法直接测试得不到很好的效果，在另外的方法调用后，再远程测试
 * 目前能够设置成功的方法只有 setIBAStringValue(WTObject obj, String ibaName,String newvalue)
 * 获取属性的都有效
 * @author ymj
 *
 */

public class IBAUtil {


	private static Locale LOCALE = Locale.CHINA;
	public Hashtable ibaMap;
	public DefaultAttributeContainer defaultContainer;

	private IBAUtil() {
		ibaMap = new Hashtable();
	}

	/**
	 * 
	 * @param ibaholder
	 */
	public IBAUtil(IBAHolder ibaholder) {
		initializeIBAPart(ibaholder);
		System.out.println("调用构造11111111111111");
	}

	/**
	 * 获取全部属性
	 * 
	 * @author ymj
	 * @param ibaholder
	 */
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer();
		Enumeration enumeration = ibaMap.keys();
		try {
			while (enumeration.hasMoreElements()) {
				String s = (String) enumeration.nextElement();
				System.out.println("s__________+" + s);
				AbstractValueView abstractvalueview = (AbstractValueView) ((Object[]) ibaMap
						.get(s))[1];
				System.out.println("abstractvalueview_________________:"
						+ abstractvalueview);
				stringbuffer.append(s
						+ " - "
						+ IBAValueUtility.getLocalizedIBAValueDisplayString(
								abstractvalueview,
								SessionHelper.manager.getLocale()));
				stringbuffer.append('\n');
				System.out.println("stringbuffer.toString()"
						+ stringbuffer.toString());

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return stringbuffer.toString();
	}

	/**
	 * 根据内部名称，获取属性本地化的值
	 * 
	 * @param s
	 * @return
	 */
	public String getIBAValue(String s) {
		try {
			return getIBAValue(s, SessionHelper.manager.getLocale());
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}

	/**
	 * 根据内部名称和本地化的对象，获取属性本地化的值（被getIBAValue(String s)调用）
	 * 
	 * @author ymj
	 * @param s
	 * @param locale
	 * @return
	 */
	public String getIBAValue(String s, Locale locale) {
		// hike add -->
		AbstractValueView abstractvalueview = null;
		if (((Object[]) ibaMap.get(s)) != null
				&& ((Object[]) ibaMap.get(s)).length > 0) {
//			根据映射关系获取Object对象
			System.out.println("ibaMap.get(s).toString()______________"
					+ ibaMap.get(s).toString());
			abstractvalueview = (AbstractValueView) ((Object[]) ibaMap.get(s))[1];
			System.out.println("abstractvalueview:*****************888"
					+ abstractvalueview.toString());
		} else {

			return null;
		}
		// hike add <--
		try {
			return IBAValueUtility.getLocalizedIBAValueDisplayString(
					abstractvalueview, locale);
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}
	
	
 
	

	/**
	 * 根据属性名，获取其布尔值
	 * 
	 * @param attrName
	 * @return
	 */
	public boolean getIBABooleanValue(String attrName) {
		boolean value = false;
		if (ibaMap.get(attrName) == null) {
			return value;
		}

		AbstractValueView abstractvalueview = (AbstractValueView) ((Object[]) ibaMap
				.get(attrName))[1];
		String thisIBAClass = (abstractvalueview.getDefinition())
				.getAttributeDefinitionClassName();
		if (thisIBAClass.equals("wt.iba.definition.BooleanDefinition")) {
			value = (boolean) ((BooleanValueDefaultView) abstractvalueview)
					.isValue();
		}
		return value;
	}

	private void initializeIBAPart(IBAHolder ibaholder) {
		ibaMap = new Hashtable();
		try {
			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, SessionHelper.manager.getLocale(), null);
			// 获取属性容器
			defaultContainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			System.out.println("22222222222222"
					+ ibaholder.getAttributeContainer().toString());
			System.out.println("*************************");
			System.out.println("22222222222222"
					+ ibaholder.getAttributeContainer().toString());
			if (defaultContainer != null) {
				// 获取属性定义默认视图
				AttributeDefDefaultView aattributedefdefaultview[] = defaultContainer
						.getAttributeDefinitions();
				for (int i = 0; i < aattributedefdefaultview.length; i++) {
					// 根据属性容器和属性定义默认视图，得到抽象值视图
					AbstractValueView aabstractvalueview[] = defaultContainer
							.getAttributeValues(aattributedefdefaultview[i]);
					System.out
							.println("值示图：getLocalizedDisplayString():"
									+ aabstractvalueview[0]
											.getLocalizedDisplayString());
					System.out.println("值示图：getKey():"
							+ aabstractvalueview[0].getKey());
					System.out.println("值示图：getState():"
							+ aabstractvalueview[0].getState());
					System.out.println("值示图：getLocalizedDisplayString(LOCALE):"
							+ aabstractvalueview[0]
									.getLocalizedDisplayString(LOCALE));

					// 属性类型获取方式，先获取属性定义默认视图，在调用方法获取属性类型（即创建属相时下拉菜单的9中选项字符创、布尔值等）
					String ibaClass = (aabstractvalueview[0].getDefinition())
							.getAttributeDefinitionClassName();

					// 演示结果：
					// String ibaClass="wt.iba.definition.StringDefinition"
					System.out
							.println("属性的类型：ibaClass：————————————————————————"
									+ ibaClass);
					//

					System.out.println("AbstractValueView 视图名称:____________---"
							+ aattributedefdefaultview[i].getName());
					System.out
							.println("AbstractValueView 视图显示名称:____________---"
									+ aattributedefdefaultview[i]
											.getDisplayName());
					System.out
							.println("AbstractValueView 视图逻辑标志符:____________---"
									+ aattributedefdefaultview[i]
											.getLogicalIdentifier());
					System.out
							.println("AbstractValueView 视图本地化显示逻辑标志符:____________---"
									+ aattributedefdefaultview[i]
											.getLocalizedDisplayString());
					if (aabstractvalueview != null) {
						// 定义Object对象数字，接收属性定义默认视图[0]，抽象值视图[1]
						Object aobj[] = new Object[2];
						aobj[0] = aattributedefdefaultview[i];
						System.out.println("aobj[0]_________1_____________"
								+ aobj[0].toString());
						aobj[1] = aabstractvalueview[0];
						System.out.println("aobj[1]__________2____________"
								+ aobj[1].toString());
						// 定义map,将key设置为属性定义默认视图的名称，只设置为Object对象数字
						ibaMap.put(aattributedefdefaultview[i].getName(),
								((Object) (aobj)));
					}
				}

			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	

	/**
	 * 根据对象、属性逻辑标识符，属性值，设置字符串类型的值(只能设置字符串类型)
	 * @author ymj
	 * @param obj
	 * @param ibaName
	 * @param newvalue
	 * @return
	 * @throws WTException
	 */
	public static String setIBAStringValue(WTObject obj, String ibaName,
			String newvalue) throws WTException {
		String value = null;
		String ibaClass = "wt.iba.definition.StringDefinition";
//		wt.iba.definition.BooleanDefinition;
//		wt.iba.definition.IntegerDefinition;
//		wt.iba.definition.RatioDefinition;
//		wt.iba.definition.StringDefinition
//		wt.iba.definition.FloatDefinition
//		wt.iba.definition.TimestampDefinition
//		wt.iba.definition.URLDefinition
//		wt.iba.definition.ReferenceDefinition
//		wt.iba.definition.UnitDefinition
		
		
		try {
			if (obj instanceof IBAHolder) {
				IBAHolder ibaholder = (IBAHolder) obj;
				DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
				if (defaultattributecontainer != null) {
					System.out.println("	got the DefaultAttributeContainer!");
				} else {
					defaultattributecontainer = new DefaultAttributeContainer();
					ibaholder.setAttributeContainer(defaultattributecontainer);
				}
				StringValueDefaultView stringvaluedefaultview = (StringValueDefaultView) getIBAValueView(
						defaultattributecontainer, ibaName, ibaClass);
				if (stringvaluedefaultview != null) {
					stringvaluedefaultview.setValue(newvalue);
					defaultattributecontainer
							.updateAttributeValue(stringvaluedefaultview);
				} else {
					AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
					StringValueDefaultView stringvaluedefaultview1 = new StringValueDefaultView(
							(StringDefView) attributedefdefaultview, newvalue);
					defaultattributecontainer
							.addAttributeValue(stringvaluedefaultview1);
				}
				ibaholder.setAttributeContainer(defaultattributecontainer);
				LoadValue.applySoftAttributes(ibaholder);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return value;
	}

	

	/**
	 * 根据逻辑标志符（属性），得到属性定义默认视图
	 * 
	 * @author ymj
	 * @param s
	 * @return
	 */
	public static AttributeDefDefaultView getAttributeDefinition(String s) {
		AttributeDefDefaultView attributedefdefaultview = null;
		if (!RemoteMethodServer.ServerFlag) {
			Class[] argTypes = { String.class };
			Object[] argValues = { s };
			try {
				return (AttributeDefDefaultView) RemoteMethodServer
						.getDefault().invoke("getAttributeDefinition",
								IBAUtil.class.getName(), null, argTypes,
								argValues);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		try {
			// 根据逻辑标志符得到属性定义默认视图
			attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader
						.getAttributeDefinition(s);
				if (abstractattributedefinizerview != null)
					attributedefdefaultview = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) abstractattributedefinizerview);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return attributedefdefaultview;
	}

	/**
	 * 根据属性的数据类型，和属性定义默认视图，以及属性值，创建抽象值视图(创建属性时，属性类型有9中和代码中一致)
	 * 
	 * @author ymj
	 * @param abstractattributedefinizerview
	 * @param s
	 * @return
	 * @throws IBAValueException 
	 */
	private AbstractValueView internalCreateValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s) throws IBAValueException {
		AbstractValueView abstractvalueview = null;
		if (abstractattributedefinizerview instanceof FloatDefView){
			abstractvalueview = LoadValue.newFloatValue(
					abstractattributedefinizerview, s, null);
		}else if (abstractattributedefinizerview instanceof StringDefView){
			System.out.println("进入internalCreateValue方法，获取抽象值视图");
			abstractvalueview = LoadValue.newStringValue(abstractattributedefinizerview, s);
			System.out.println("进入internalCreateValue方法，获取抽象值视图"+abstractvalueview.getLocalizedDisplayString());
//		    abstractvalueview = new StringValueDefaultView((StringDefView) abstractattributedefinizerview,s);
//		***********************修改方式**************************	
		}else if (abstractattributedefinizerview instanceof IntegerDefView){
			abstractvalueview = LoadValue.newIntegerValue(
					abstractattributedefinizerview, s);	
		}else if (abstractattributedefinizerview instanceof RatioDefView){
			abstractvalueview = LoadValue.newRatioValue(
					abstractattributedefinizerview, s, null);	
		}else if (abstractattributedefinizerview instanceof TimestampDefView){
			abstractvalueview = LoadValue.newTimestampValue(
					abstractattributedefinizerview, s);			
		}else if (abstractattributedefinizerview instanceof BooleanDefView){
			
			abstractvalueview = LoadValue.newBooleanValue(
					abstractattributedefinizerview, s);
		}else if (abstractattributedefinizerview instanceof URLDefView){	
			abstractvalueview = LoadValue.newURLValue(
					abstractattributedefinizerview, s, null);
		}else if (abstractattributedefinizerview instanceof ReferenceDefView){
			abstractvalueview = LoadValue.newReferenceValue(
					abstractattributedefinizerview, "ClassificationNode", s);
		}else if (abstractattributedefinizerview instanceof UnitDefView){
			abstractvalueview = LoadValue.newUnitValue(
					abstractattributedefinizerview, s, null);
		}
		return abstractvalueview;
	}

	/**
	 * 根据对象，得到属相容器
	 * 
	 * @param ibaholder
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static DefaultAttributeContainer getContainer(IBAHolder ibaholder)
			throws WTException, RemoteException {
		ibaholder = IBAValueHelper.service
				.refreshAttributeContainerWithoutConstraints(ibaholder);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		return defaultattributecontainer;
	}

	/**
	 * 属性容器，属性内部名称（逻辑标志符），
	 * 
	 * @param dac
	 * @param ibaName
	 * @param ibaClass
	 * @return
	 * @throws WTException
	 */
	public static AbstractValueView getIBAValueView(
			DefaultAttributeContainer dac, String ibaName, String ibaClass)
			throws WTException {
		// 演示属性类型 9种类型 还是8种类型，创建属性时，确认
		// String ibaClass="wt.iba.definition.StringDefinition"
		// wt.iba.definition.BooleanDefinition
		// wt.iba.definition.FloatDefinition
		// wt.iba.definition.IntegerDefinition
		// wt.iba.definition.URLDefinition
		// wt.iba.definition.UnitDefinition

		AbstractValueView aabstractvalueview[] = null;
		AbstractValueView avv = null;
		aabstractvalueview = dac.getAttributeValues();
		for (int j = 0; j < aabstractvalueview.length; j++) {
			String thisIBAName = aabstractvalueview[j].getDefinition()
					.getName();
			String thisIBAValue = IBAValueUtility
					.getLocalizedIBAValueDisplayString(aabstractvalueview[j],
							LOCALE);

			String thisIBAClass = (aabstractvalueview[j].getDefinition())
					.getAttributeDefinitionClassName();
			// 抽象值视图得到属性定义默认视图
			// AttributeDefDefaultView
			// definition=(aabstractvalueview[j].getDefinition());
			// 属性定义默认视图
			// String ibaClass=definition.getAttributeDefinitionClassName();

			// 将属性由容器对象得到属性和属性的类型与方法传入的参数：属性和属性的类型比对，得到需要的抽象值视图
			if (thisIBAName.equals(ibaName) && thisIBAClass.equals(ibaClass)) {
				avv = aabstractvalueview[j];
				break;
			}
		}
		return avv;
	}

	/**
	 * 根据抽象值视图对象，克隆一个抽象值视图对象
	 * 
	 * @author ymj
	 * @param source
	 * @return
	 */
	private static AbstractValueView cloneIBAValueView(AbstractValueView source) {
		AbstractValueView dest = null;
		try {
			if (source instanceof UnitValueDefaultView) {
				dest = new UnitValueDefaultView(
						((UnitValueDefaultView) source).getUnitDefinition(),
						((UnitValueDefaultView) source).getValue(),
						((UnitValueDefaultView) source).getPrecision());
			} else if (source instanceof BooleanValueDefaultView) {
				dest = new BooleanValueDefaultView(
						((BooleanValueDefaultView) source)
								.getBooleanDefinition(),
						((BooleanValueDefaultView) source).isValue());
			} else if (source instanceof FloatValueDefaultView) {
				dest = new FloatValueDefaultView(
						((FloatValueDefaultView) source).getFloatDefinition(),
						((FloatValueDefaultView) source).getValue(),
						((FloatValueDefaultView) source).getPrecision());
			} else if (source instanceof IntegerValueDefaultView) {
				dest = new IntegerValueDefaultView(
						((IntegerValueDefaultView) source)
								.getIntegerDefinition(),
						((IntegerValueDefaultView) source).getValue());
			} else if (source instanceof RatioValueDefaultView) {
				dest = new RatioValueDefaultView(
						((RatioValueDefaultView) source).getRatioDefinition(),
						((RatioValueDefaultView) source).getValue(),
						((RatioValueDefaultView) source).getDenominator());
			}

			else if (source instanceof ReferenceValueDefaultView) {
				dest = new ReferenceValueDefaultView(
						((ReferenceValueDefaultView) source)
								.getReferenceDefinition(),
						((ReferenceValueDefaultView) source)
								.getLiteIBAReferenceable(),
						((ReferenceValueDefaultView) source).getObjectID(),
						((ReferenceValueDefaultView) source).getUpdateCount());
			}

			else if (source instanceof StringValueDefaultView) {
				dest = new StringValueDefaultView(
						((StringValueDefaultView) source).getStringDefinition(),
						((StringValueDefaultView) source).getValue());
			}

			else if (source instanceof TimestampValueDefaultView) {
				dest = new TimestampValueDefaultView(
						((TimestampValueDefaultView) source)
								.getTimestampDefinition(),
						((TimestampValueDefaultView) source).getValue());
			} else if (source instanceof URLValueDefaultView) {
				dest = new URLValueDefaultView(
						((URLValueDefaultView) source).getUrlDefinition(),
						((URLValueDefaultView) source).getValue(),
						((URLValueDefaultView) source).getDescription());
			}
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		}
		return dest;
	}


	/**
	 * 根据对象和属性、以及属性值，设置对象的整型类型属性的值
	 * 
	 * @param obj
	 * @param ibaName
	 * @param newvalue
	 * @throws WTException
	 */
	public static void setIBAIntegerValue(WTObject obj, String ibaName,
			long newvalue) throws WTException {
		// 定义整形的属性类型
		String ibaClass = "wt.iba.definition.IntegerDefinition";
		try {
			if (obj instanceof IBAHolder) {
				IBAHolder ibaholder = (IBAHolder) obj;
				// 根据传入参数对象，得到属性容器
				DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
				if (defaultattributecontainer != null) {
					System.out.println("	got the DefaultAttributeContainer!");
				} else {
					// 如果没有属性容器，创建
					defaultattributecontainer = new DefaultAttributeContainer();
					// 设置对象属性容器
					ibaholder.setAttributeContainer(defaultattributecontainer);
				}
				// 根据容器对象、属性、属性类型，得到抽象值视图，转化为整型值默认视图
				IntegerValueDefaultView integervaluedefaultview = (IntegerValueDefaultView) getIBAValueView(
						defaultattributecontainer, ibaName, ibaClass);
				if (integervaluedefaultview != null) {
					// 设置整型值默认视图的值
					integervaluedefaultview.setValue(newvalue);
					// 容器更新整型值默认视图
					defaultattributecontainer
							.updateAttributeValue(integervaluedefaultview);
				} else {
					// 如果整型值默认视图为空，根据属性，得到属性定义默认视图
					AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
					// 属性定义默认视图，转化为整型定义视图；根据整型定义视图和值，实例化整型值默认视图
					IntegerValueDefaultView integervaluedefaultview1 = new IntegerValueDefaultView(
							(IntegerDefView) attributedefdefaultview, newvalue);
					// 容器添加整型值默认视图
					defaultattributecontainer
							.addAttributeValue(integervaluedefaultview1);
				}
				ibaholder.setAttributeContainer(defaultattributecontainer);
				wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	
	
	
	
	 /**
     * 获取单个属性值                                              测试有效
     * @param persistable 对象
     * @param key 属性名称
     * @return
     * @throws WTException
     */
    public static Object getIBAValue(Persistable persistable, String key) throws WTException {
        Object value = null;
        PersistableAdapter obj = new PersistableAdapter(persistable,null,null,null);
        obj.load(key);
        value = obj.get(key);
        return value;
    }
    
    /**
     * 获取多个属性值                                                     测试有效
     * @param persistable 对象
     * @param ibaLogicals 属性名称数组
     * @return
     * @throws WTException
     */
    public static Hashtable getIBAValues(Persistable persistable, String[] names) throws WTException {
        Hashtable values = new Hashtable();
        PersistableAdapter obj = new PersistableAdapter(persistable,null,null,null);
        for(int i=0; i<names.length; i++) {
            String key = names[i];
            obj.load(key);
            Object value = obj.get(key);
            if(value==null){
                values.put(key, "");
            }else{
                values.put(key, value);
            }
        }       
        return values;
    }
    
    
    
    
    

    /**
     * 此方法可以设置软属性，需要检出，才可以操作
     * @param persistable
     * @param name
     * @param value
     * @throws WTException
     */
    public static void setIBAValueAfterCheckOut(Persistable persistable, String name, Object value) throws WTException{
    	@SuppressWarnings("deprecation")
		LWCNormalizedObject obj = new LWCNormalizedObject(persistable, null,Locale.CHINA, new UpdateOperationIdentifier());
    	obj.load(name);
    	/* Set value of IBAName  soft attribute  to IBAValue */
    	obj.set(name,value);
    	obj.apply();
    	PersistenceHelper.manager.modify(persistable);
    }
    

    
    /**
     * 设置单个属性值                                                  (需要将对象检出操作，可以使用)                                                
     * @param persistable 对象
     * @param key 属性名称
     * @param value 目标值
     * @throws WTException
     */
    public static void setIBAValue(Persistable persistable, String name, Object value) throws WTException {
        PersistableAdapter obj = new PersistableAdapter(persistable,null,null,null);
        obj.load(name);
        obj.set(name, value);
        obj.apply(); 
//        PersistenceServerHelper.manager.update(persistable);    测试无效 
        PersistenceHelper.manager.modify(persistable);            //测试有效   
    }
    
    
    
    /**
     * 设置多个属性值                                                      修改后测试有效，必须对象检出时，才可以修改对象的IBA属性                                                         
     * @param persistable 对象
     * @param ht 属性名称和目标值的Hashtable
     * @throws WTException
     */
    public static void setIBAValues(Persistable persistable, Hashtable ht) throws WTException {
        PersistableAdapter obj = new PersistableAdapter(persistable,null,null,null);
        Enumeration names = ht.keys();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = ht.get(name);
            obj.load(name);
            obj.set(name, value);
            obj.apply();
        }
//        PersistenceServerHelper.manager.update(persistable);    //测试无效 
        PersistenceHelper.manager.modify(persistable);    //测试有效
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//   ******************************************** 以下方法待验证***************************
    
    

	/**
	 * 传入对象，删减属性
	 * 
	 * @param ibaHolder
	 * @return
	 * @throws WTException
	 */
	public static IBAHolder pruneIBA(IBAHolder ibaHolder) throws WTException {
		DefaultAttributeContainer ibaContainer = null;
		Vector prunedIBAs = new Vector();
		try {
			ibaContainer = getContainer(ibaHolder);
		} catch (RemoteException rme) {
			rme.printStackTrace();
			throw new WTException(rme);
		}
		if (ibaContainer == null) {
			System.out.println("	don't get iba container of the source!");
			return ibaHolder;
		}

		// copy values
		AbstractValueView valueViews[] = null;
		AbstractValueView avv = null;
		valueViews = ibaContainer.getAttributeValues();
		for (int j = 0; j < valueViews.length; j++) {
			String thisIBAName = valueViews[j].getDefinition().getName();
			String thisIBAValue = IBAValueUtility
					.getLocalizedIBAValueDisplayString(valueViews[j], LOCALE);
			String thisIBAClass = (valueViews[j].getDefinition())
					.getAttributeDefinitionClassName();
			if (!prunedIBAs.contains(thisIBAName)) {
				prunedIBAs.addElement(thisIBAName);
			} else {

				ibaContainer.deleteAttributeValue(valueViews[j]);
			}

		}
		//
		ibaHolder.setAttributeContainer(ibaContainer);
		ibaHolder = wt.iba.value.service.LoadValue
				.applySoftAttributes(ibaHolder);
		return ibaHolder;
	}
    
    
    
    /**
	 * 设置属性值
	 * 
	 * @author ymj
	 * @param ibaholder
	 * @return
	 * @throws Exception
	 */
	public IBAHolder updateIBAHolder(IBAHolder ibaholder) throws Exception {
		// 刷新属性容器
		ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder,
				null, SessionHelper.manager.getLocale(), null);
		// 得到 属性容器
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		// 将map对象取出value值
		for (Enumeration enumeration = ibaMap.elements(); enumeration
				.hasMoreElements();)
			try {
				// object数组接收属性定义默认视图[0]，抽象值视图[1]
				Object aobj[] = (Object[]) enumeration.nextElement();
				AbstractValueView abstractvalueview = (AbstractValueView) aobj[1];
				AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
				if (abstractvalueview.getState() == 1) {
					// 属性容器，调用删除属性值方法，删除属性定义默认视图
					defaultattributecontainer
							.deleteAttributeValues(attributedefdefaultview);
					// 设置抽象值视图的状态 0 3 分别代表什么？
					abstractvalueview.setState(3);
					// 设置新的抽象值视图
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}

		ibaholder.setAttributeContainer(defaultattributecontainer);
		return ibaholder;
	}
	


	/**
	 * 根据原对象，复制IBA属性给目标对象
	 * @param source
	 * @param dest
	 * @return
	 * @throws WTException
	 */
	public static IBAHolder copyIBA(IBAHolder source, IBAHolder dest)
			throws WTException {
		try {
			DefaultAttributeContainer ibaContainer1 = getContainer(source);
			DefaultAttributeContainer ibaContainer2 = null;
			if (ibaContainer1 == null) {
				System.out.println("	don't get iba container of the source!");
				return dest;
			} else {
				ibaContainer2 = getContainer(dest);
				if (ibaContainer2 == null) {
					ibaContainer2 = new DefaultAttributeContainer();
					dest.setAttributeContainer(ibaContainer2);
				}
			}

			// copy values
			AbstractValueView valueViews[] = null;
			valueViews = ibaContainer1.getAttributeValues();

			for (int j = 0; j < valueViews.length; j++) {
				String thisIBAName = valueViews[j].getDefinition().getName();
				String thisIBAValue = IBAValueUtility
						.getLocalizedIBAValueDisplayString(valueViews[j],
								LOCALE);
				String thisIBAClass = (valueViews[j].getDefinition())
						.getAttributeDefinitionClassName();

				AbstractValueView valueView = getIBAValueView(ibaContainer2,
						thisIBAName, thisIBAClass);
				if (valueView != null) {
					valueView = (AbstractValueView) valueViews[j].clone();
					ibaContainer2.updateAttributeValue(valueView);
				} else {
					// valueView= (AbstractValueView)(valueViews[j].clone());
					valueView = cloneIBAValueView(valueViews[j]);
					ibaContainer2.addAttributeValue(valueView);
				}
			}
			//
			dest.setAttributeContainer(ibaContainer2);
			dest = wt.iba.value.service.LoadValue.applySoftAttributes(dest);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return dest;
	}
	
	
	
	/*

	*//**
	 * 设置属性的值(此方法利用定义默认视图和抽象值示图 的映射关系设置属性，但测试不能更改属性，只能获取，不能使用)
	 * 
	 * @param logicId
	 * @param value
	 * @throws WTPropertyVetoException
	 * @throws WTException 
	 * @throws RemoteException 
	 *//*
	public void setIBAValue(IBAHolder ibaholder,String logicId, String value)
			throws WTPropertyVetoException, RemoteException, WTException {
		AbstractValueView abstractvalueview = null;
		AttributeDefDefaultView attributedefdefaultview = null;
		DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
		
		
		Object aobj[] = (Object[]) ibaMap.get(logicId);
		if (aobj != null) {
			abstractvalueview = (AbstractValueView) aobj[1];
			attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
		}
		if (abstractvalueview == null)
			// 抽象视图值为空时，根据逻辑标识符（属性），得到属性定义默认视图
			attributedefdefaultview = getAttributeDefinition(logicId);
		
		
		if (attributedefdefaultview == null) {
			System.out.println("definition is null ...");
			return;
		}
		// 根据属性定义默认视图和属性值，创建抽象值示图
		abstractvalueview = internalCreateValue(attributedefdefaultview, value);
		defaultattributecontainer.updateAttributeValue(abstractvalueview);
		ibaholder.setAttributeContainer(defaultattributecontainer);
		LoadValue.applySoftAttributes(ibaholder);
		

		if (abstractvalueview == null) {
			System.out.println("after creation, iba value is null ..");
			return;
		} else {
			System.out.println("抽象值示图不为空***********************************");
//			abstractvalueview.setState(1);
			Object aobj1[] = new Object[2];
			aobj1[0] = attributedefdefaultview;
			aobj1[1] = abstractvalueview;
			ibaMap.put(attributedefdefaultview.getName(), ((Object) (aobj1)));
			System.out.println("属性定义默认视图和抽象值示图 的映射关系***********************************");
			return;
		}
	}
	
	*/
    
    
    /**
     * 设置属性方法，无法实现设置   ，无效
     * @param logicId
     * @param value
     * @throws WTPropertyVetoException
     * @throws RemoteException
     * @throws WTException
     */
	public void setIBAValue(String logicId, String value)
			throws WTPropertyVetoException, RemoteException, WTException {
		AbstractValueView abstractvalueview = null;
		AttributeDefDefaultView attributedefdefaultview = null;
		Object aobj[] = (Object[]) ibaMap.get(logicId);
		if (aobj != null) {
			abstractvalueview = (AbstractValueView) aobj[1];
			attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
		}
		if (abstractvalueview == null)
			// 抽象视图值为空时，根据逻辑标识符（属性），得到属性定义默认视图
			attributedefdefaultview = getAttributeDefinition(logicId);
		
		
		if (attributedefdefaultview == null) {
			System.out.println("definition is null ...");
			return;
		}
		// 根据属性定义默认视图和属性值，创建抽象值示图
		abstractvalueview = internalCreateValue(attributedefdefaultview, value);
		if (abstractvalueview == null) {
			System.out.println("after creation, iba value is null ..");
			return;
		} else {
			System.out.println("抽象值示图不为空***********************************");
//			abstractvalueview.setState(1);
			Object aobj1[] = new Object[2];
			aobj1[0] = attributedefdefaultview;
			aobj1[1] = abstractvalueview;
			ibaMap.put(attributedefdefaultview.getName(), ((Object) (aobj1)));
			System.out.println("属性定义默认视图和抽象值示图 的映射关系***********************************");
			return;
		}
	}
    
     
 

	

}
