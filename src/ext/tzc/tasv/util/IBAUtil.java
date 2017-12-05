package ext.tzc.tasv.util;

import java.io.PrintStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import wt.fc.WTObject;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.ReferenceDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.service.IBADefinitionService;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.BooleanValueDefaultView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.RatioValueDefaultView;
import wt.iba.value.litevalue.ReferenceValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.litevalue.TimestampValueDefaultView;
import wt.iba.value.litevalue.URLValueDefaultView;
import wt.iba.value.litevalue.UnitValueDefaultView;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.IBAValueService;
import wt.iba.value.service.LoadValue;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class IBAUtil
{
  private static Locale LOCALE = Locale.CHINA;
  public Hashtable ibaContainer;
  public DefaultAttributeContainer defaultContainer;

  private IBAUtil()
  {
    this.ibaContainer = new Hashtable();
  }

  public IBAUtil(IBAHolder ibaholder)
  {
    initializeIBAPart(ibaholder);
  }

  public String toString()
  {
    StringBuffer stringbuffer = new StringBuffer();
    Enumeration enumeration = this.ibaContainer.keys();
    try
    {
      while (enumeration.hasMoreElements())
      {
        String s = (String)enumeration.nextElement();
        AbstractValueView abstractvalueview = (AbstractValueView)((Object[])this.ibaContainer.get(s))[1];
        stringbuffer.append(s + " - " + IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview, SessionHelper.manager.getLocale()));
        stringbuffer.append('\n');
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
    return stringbuffer.toString();
  }

  public String getIBAValue(String s)
  {
    try
    {
      return getIBAValue(s, SessionHelper.manager.getLocale());
    }
    catch (WTException wte)
    {
      wte.printStackTrace();
    }
    return null;
  }

  public String getIBAValue(String s, Locale locale)
  {
    AbstractValueView abstractvalueview = null;
    if (((Object[])this.ibaContainer.get(s) != null) && (((Object[])this.ibaContainer.get(s)).length > 0))
      abstractvalueview = (AbstractValueView)((Object[])this.ibaContainer.get(s))[1];
    else {
      return null;
    }
    try
    {
      return IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview, locale);
    }
    catch (WTException wte)
    {
      wte.printStackTrace();
    }
    return null;
  }

  public boolean getIBABooleanValue(String attrName)
  {
    boolean value = false;
    if (this.ibaContainer.get(attrName) == null)
    {
      return value;
    }

    AbstractValueView abstractvalueview = (AbstractValueView)((Object[])this.ibaContainer.get(attrName))[1];
    String thisIBAClass = abstractvalueview.getDefinition().getAttributeDefinitionClassName();
    if (thisIBAClass.equals("wt.iba.definition.BooleanDefinition"))
    {
      value = ((BooleanValueDefaultView)abstractvalueview).isValue();
    }
    return value;
  }

  private void initializeIBAPart(IBAHolder ibaholder)
  {
    this.ibaContainer = new Hashtable();
    try
    {
      ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
      this.defaultContainer = ((DefaultAttributeContainer)ibaholder.getAttributeContainer());
      if (this.defaultContainer != null)
      {
        AttributeDefDefaultView[] aattributedefdefaultview = this.defaultContainer.getAttributeDefinitions();
        for (int i = 0; i < aattributedefdefaultview.length; i++)
        {
          AbstractValueView[] aabstractvalueview = this.defaultContainer.getAttributeValues(aattributedefdefaultview[i]);
          if (aabstractvalueview == null)
            continue;
          Object[] aobj = new Object[2];
          aobj[0] = aattributedefdefaultview[i];
          aobj[1] = aabstractvalueview[0];
          this.ibaContainer.put(aattributedefdefaultview[i].getName(), aobj);
        }

      }

    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
  }

  public IBAHolder updateIBAHolder(IBAHolder ibaholder)
    throws Exception
  {
    ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, SessionHelper.manager.getLocale(), null);
    DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)ibaholder.getAttributeContainer();
    for (Enumeration enumeration = this.ibaContainer.elements(); enumeration.hasMoreElements(); ) {
      try
      {
        Object[] aobj = (Object[])enumeration.nextElement();
        AbstractValueView abstractvalueview = (AbstractValueView)aobj[1];
        AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView)aobj[0];
        if (abstractvalueview.getState() != 1)
          continue;
        defaultattributecontainer.deleteAttributeValues(attributedefdefaultview);
        abstractvalueview.setState(3);
        defaultattributecontainer.addAttributeValue(abstractvalueview);
      }
      catch (Exception exception)
      {
        exception.printStackTrace();
      }
    }
    ibaholder.setAttributeContainer(defaultattributecontainer);
    return ibaholder;
  }

  public void setIBAValue(String s, String s1)
    throws WTPropertyVetoException
  {
    AbstractValueView abstractvalueview = null;
    AttributeDefDefaultView attributedefdefaultview = null;
    Object[] aobj = (Object[])this.ibaContainer.get(s);
    if (aobj != null)
    {
      abstractvalueview = (AbstractValueView)aobj[1];
      attributedefdefaultview = (AttributeDefDefaultView)aobj[0];
    }
    if (abstractvalueview == null)
      attributedefdefaultview = getAttributeDefinition(s);
    if (attributedefdefaultview == null)
    {
      System.out.println("definition is null ...");
      return;
    }
    abstractvalueview = internalCreateValue(attributedefdefaultview, s1);
    if (abstractvalueview == null)
    {
      System.out.println("after creation, iba value is null ..");
      return;
    }

    abstractvalueview.setState(1);
    Object[] aobj1 = new Object[2];
    aobj1[0] = attributedefdefaultview;
    aobj1[1] = abstractvalueview;
    this.ibaContainer.put(attributedefdefaultview.getName(), aobj1);
  }

  public static String setIBAStringValue(WTObject obj, String ibaName, String newvalue)
    throws WTException
  {
    String value = null;
    String ibaClass = "wt.iba.definition.StringDefinition";
    try
    {
      if ((obj instanceof IBAHolder))
      {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
        if (defaultattributecontainer != null)
        {
          System.out.println("\tgot the DefaultAttributeContainer!");
        }
        else
        {
          defaultattributecontainer = new DefaultAttributeContainer();
          ibaholder.setAttributeContainer(defaultattributecontainer);
        }
        StringValueDefaultView stringvaluedefaultview = (StringValueDefaultView)getIBAValueView(defaultattributecontainer, ibaName, ibaClass);
        if (stringvaluedefaultview != null)
        {
          stringvaluedefaultview.setValue(newvalue);
          defaultattributecontainer.updateAttributeValue(stringvaluedefaultview);
        }
        else
        {
          AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
          StringValueDefaultView stringvaluedefaultview1 = new StringValueDefaultView((StringDefView)attributedefdefaultview, newvalue);
          defaultattributecontainer.addAttributeValue(stringvaluedefaultview1);
        }
        ibaholder.setAttributeContainer(defaultattributecontainer);
        LoadValue.applySoftAttributes(ibaholder);
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
    return value;
  }

  public static IBAHolder copyIBA(IBAHolder source, IBAHolder dest)
    throws WTException
  {
    try
    {
      DefaultAttributeContainer ibaContainer1 = getContainer(source);
      DefaultAttributeContainer ibaContainer2 = null;
      if (ibaContainer1 == null)
      {
        System.out.println("\tdon't get iba container of the source!");
        return dest;
      }

      ibaContainer2 = getContainer(dest);
      if (ibaContainer2 == null)
      {
        ibaContainer2 = new DefaultAttributeContainer();
        dest.setAttributeContainer(ibaContainer2);
      }

      AbstractValueView[] valueViews = null;
      valueViews = ibaContainer1.getAttributeValues();

      for (int j = 0; j < valueViews.length; j++)
      {
        String thisIBAName = valueViews[j].getDefinition().getName();
        String thisIBAValue = IBAValueUtility.getLocalizedIBAValueDisplayString(valueViews[j], LOCALE);
        String thisIBAClass = valueViews[j].getDefinition().getAttributeDefinitionClassName();

        AbstractValueView valueView = getIBAValueView(ibaContainer2, thisIBAName, thisIBAClass);
        if (valueView != null)
        {
          valueView = (AbstractValueView)valueViews[j].clone();
          ibaContainer2.updateAttributeValue(valueView);
        }
        else
        {
          valueView = cloneIBAValueView(valueViews[j]);
          ibaContainer2.addAttributeValue(valueView);
        }
      }

      dest.setAttributeContainer(ibaContainer2);
      dest = LoadValue.applySoftAttributes(dest);
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
    return dest;
  }

  public static AttributeDefDefaultView getAttributeDefinition(String s)
  {
    AttributeDefDefaultView attributedefdefaultview = null;
    try
    {
      attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(s);
      if (attributedefdefaultview == null)
      {
        AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader.getAttributeDefinition(s);
        if (abstractattributedefinizerview != null)
          attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultView((AttributeDefNodeView)abstractattributedefinizerview);
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
    return attributedefdefaultview;
  }

  private AbstractValueView internalCreateValue(AbstractAttributeDefinizerView abstractattributedefinizerview, String s)
  {
    AbstractValueView abstractvalueview = null;
    if ((abstractattributedefinizerview instanceof FloatDefView)) {
      abstractvalueview = LoadValue.newFloatValue(abstractattributedefinizerview, s, null);
    }
    else if ((abstractattributedefinizerview instanceof StringDefView)) {
      abstractvalueview = LoadValue.newStringValue(abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof IntegerDefView)) {
      abstractvalueview = LoadValue.newIntegerValue(abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof RatioDefView)) {
      abstractvalueview = LoadValue.newRatioValue(abstractattributedefinizerview, s, null);
    }
    else if ((abstractattributedefinizerview instanceof TimestampDefView)) {
      abstractvalueview = LoadValue.newTimestampValue(abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof BooleanDefView)) {
      abstractvalueview = LoadValue.newBooleanValue(abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof URLDefView)) {
      abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, s, null);
    }
    else if ((abstractattributedefinizerview instanceof ReferenceDefView)) {
      abstractvalueview = LoadValue.newReferenceValue(abstractattributedefinizerview, "ClassificationNode", s);
    }
    else if ((abstractattributedefinizerview instanceof UnitDefView))
      abstractvalueview = LoadValue.newUnitValue(abstractattributedefinizerview, s, null);
    return abstractvalueview;
  }

  public static DefaultAttributeContainer getContainer(IBAHolder ibaholder)
    throws WTException, RemoteException
  {
    ibaholder = IBAValueHelper.service.refreshAttributeContainerWithoutConstraints(ibaholder);
    DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)ibaholder.getAttributeContainer();
    return defaultattributecontainer;
  }

  public static AbstractValueView getIBAValueView(DefaultAttributeContainer dac, String ibaName, String ibaClass)
    throws WTException
  {
    AbstractValueView[] aabstractvalueview = null;
    AbstractValueView avv = null;
    aabstractvalueview = dac.getAttributeValues();
    for (int j = 0; j < aabstractvalueview.length; j++) {
      String thisIBAName = aabstractvalueview[j].getDefinition().getName();
      String thisIBAValue = IBAValueUtility.getLocalizedIBAValueDisplayString(aabstractvalueview[j], LOCALE);
      String thisIBAClass = aabstractvalueview[j].getDefinition().getAttributeDefinitionClassName();
      if ((thisIBAName.equals(ibaName)) && (thisIBAClass.equals(ibaClass))) {
        avv = aabstractvalueview[j];
        break;
      }
    }
    return avv;
  }

  private static AbstractValueView cloneIBAValueView(AbstractValueView source)
  {
    AbstractValueView dest = null;
    try
    {
      if ((source instanceof UnitValueDefaultView))
      {
        dest = new UnitValueDefaultView(((UnitValueDefaultView)source).getUnitDefinition(), 
          ((UnitValueDefaultView)source).getValue(), 
          ((UnitValueDefaultView)source).getPrecision());
      }
      else if ((source instanceof BooleanValueDefaultView))
      {
        dest = new BooleanValueDefaultView(((BooleanValueDefaultView)source).getBooleanDefinition(), ((BooleanValueDefaultView)source).isValue());
      }
      else if ((source instanceof FloatValueDefaultView))
      {
        dest = new FloatValueDefaultView(((FloatValueDefaultView)source).getFloatDefinition(), 
          ((FloatValueDefaultView)source).getValue(), 
          ((FloatValueDefaultView)source).getPrecision());
      }
      else if ((source instanceof IntegerValueDefaultView))
      {
        dest = new IntegerValueDefaultView(((IntegerValueDefaultView)source).getIntegerDefinition(), ((IntegerValueDefaultView)source).getValue());
      }
      else if ((source instanceof RatioValueDefaultView))
      {
        dest = new RatioValueDefaultView(((RatioValueDefaultView)source).getRatioDefinition(), 
          ((RatioValueDefaultView)source).getValue(), 
          ((RatioValueDefaultView)source).getDenominator());
      }
      else if ((source instanceof ReferenceValueDefaultView))
      {
        dest = new ReferenceValueDefaultView(((ReferenceValueDefaultView)source).getReferenceDefinition(), 
          ((ReferenceValueDefaultView)source).getLiteIBAReferenceable(), 
          ((ReferenceValueDefaultView)source).getObjectID(), 
          ((ReferenceValueDefaultView)source).getUpdateCount());
      }
      else if ((source instanceof StringValueDefaultView))
      {
        dest = new StringValueDefaultView(((StringValueDefaultView)source).getStringDefinition(), ((StringValueDefaultView)source).getValue());
      }
      else if ((source instanceof TimestampValueDefaultView))
      {
        dest = new TimestampValueDefaultView(((TimestampValueDefaultView)source).getTimestampDefinition(), 
          ((TimestampValueDefaultView)source).getValue());
      }
      else if ((source instanceof URLValueDefaultView))
      {
        dest = new URLValueDefaultView(((URLValueDefaultView)source).getUrlDefinition(), 
          ((URLValueDefaultView)source).getValue(), 
          ((URLValueDefaultView)source).getDescription());
      }
    }
    catch (IBAValueException ibave)
    {
      ibave.printStackTrace();
    }
    return dest;
  }

  public static IBAHolder pruneIBA(IBAHolder ibaHolder) throws WTException
  {
    DefaultAttributeContainer ibaContainer = null;
    Vector prunedIBAs = new Vector();
    try
    {
      ibaContainer = getContainer(ibaHolder);
    }
    catch (RemoteException rme)
    {
      rme.printStackTrace();
      throw new WTException(rme);
    }
    if (ibaContainer == null)
    {
      System.out.println("\tdon't get iba container of the source!");
      return ibaHolder;
    }

    AbstractValueView[] valueViews = null;
    AbstractValueView avv = null;
    valueViews = ibaContainer.getAttributeValues();
    for (int j = 0; j < valueViews.length; j++)
    {
      String thisIBAName = valueViews[j].getDefinition().getName();
      String thisIBAValue = IBAValueUtility.getLocalizedIBAValueDisplayString(valueViews[j], LOCALE);
      String thisIBAClass = valueViews[j].getDefinition().getAttributeDefinitionClassName();
      if (!prunedIBAs.contains(thisIBAName))
      {
        prunedIBAs.addElement(thisIBAName);
      }
      else
      {
        ibaContainer.deleteAttributeValue(valueViews[j]);
      }

    }

    ibaHolder.setAttributeContainer(ibaContainer);
    ibaHolder = LoadValue.applySoftAttributes(ibaHolder);
    return ibaHolder;
  }

  public static void setIBAIntegerValue(WTObject obj, String ibaName, long newvalue)
    throws WTException
  {
    String ibaClass = "wt.iba.definition.IntegerDefinition";
    try
    {
      if ((obj instanceof IBAHolder))
      {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
        if (defaultattributecontainer != null)
        {
          System.out.println("\tgot the DefaultAttributeContainer!");
        }
        else
        {
          defaultattributecontainer = new DefaultAttributeContainer();
          ibaholder.setAttributeContainer(defaultattributecontainer);
        }
        IntegerValueDefaultView integervaluedefaultview = (IntegerValueDefaultView)getIBAValueView(defaultattributecontainer, ibaName, ibaClass);
        if (integervaluedefaultview != null)
        {
          integervaluedefaultview.setValue(newvalue);
          defaultattributecontainer.updateAttributeValue(integervaluedefaultview);
        }
        else
        {
          AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
          IntegerValueDefaultView integervaluedefaultview1 = new IntegerValueDefaultView((IntegerDefView)attributedefdefaultview, newvalue);
          defaultattributecontainer.addAttributeValue(integervaluedefaultview1);
        }
        ibaholder.setAttributeContainer(defaultattributecontainer);
        LoadValue.applySoftAttributes(ibaholder);
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
  }
}