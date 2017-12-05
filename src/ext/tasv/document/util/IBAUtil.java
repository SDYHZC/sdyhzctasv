package ext.tasv.document.util;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
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
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.IBAValueService;
import wt.iba.value.service.LoadValue;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class IBAUtil
{
  Hashtable ibaContainer;

  private IBAUtil()
  {
    this.ibaContainer = new Hashtable();
  }

  public IBAUtil(IBAHolder ibaholder) {
    initializeIBAPart(ibaholder);
  }

  public String toString() {
    StringBuffer stringbuffer = new StringBuffer();
    Enumeration enumeration = this.ibaContainer.keys();
    try {
      while (enumeration.hasMoreElements()) {
        String s = (String)enumeration.nextElement();
        AbstractValueView abstractvalueview = (AbstractValueView)
          ((Object[])this.ibaContainer.get(s))[1];
        stringbuffer.append(s + " - " + 
          IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview, 
          SessionHelper.manager.getLocale()));
        stringbuffer.append('\n');
      }
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    return stringbuffer.toString();
  }

  public String getIBAValue(String s) {
    try {
      String value = getIBAValue(s, SessionHelper.manager.getLocale());
      System.out.println("123" + value + "456");
      if ((value != null) && (!value.toLowerCase().equals("null")) && (!value.toLowerCase().equals("default")) && (!value.equals("-")))
        return value;
    }
    catch (WTException wte) {
      wte.printStackTrace();
    }
    return "";
  }

  public String getIBAValue(String s, Locale locale) {
    try {
      AbstractValueView abstractvalueview = (AbstractValueView)
        ((Object[])this.ibaContainer.get(s))[1];
      return IBAValueUtility.getLocalizedIBAValueDisplayString(
        abstractvalueview, locale);
    }
    catch (NullPointerException npe)
    {
      return "";
    }
    catch (WTException wte) {
      wte.printStackTrace();
    }
    return "";
  }

  private void initializeIBAPart(IBAHolder ibaholder)
  {
    this.ibaContainer = new Hashtable();
    try {
      ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, 
        SessionHelper.manager.getLocale(), null);
      DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)
        ibaholder.getAttributeContainer();
      if (defaultattributecontainer != null) {
        AttributeDefDefaultView[] aattributedefdefaultview = 
          defaultattributecontainer.getAttributeDefinitions();
        for (int i = 0; i < aattributedefdefaultview.length; i++) {
          AbstractValueView[] aabstractvalueview = defaultattributecontainer
            .getAttributeValues(aattributedefdefaultview[i]);
          if (aabstractvalueview != null) {
            Object[] aobj = new Object[2];
            aobj[0] = aattributedefdefaultview[i];
            aobj[1] = aabstractvalueview[0];

            this.ibaContainer.put(aattributedefdefaultview[i].getName(), 
              aobj);
          }
        }
      }
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public IBAHolder updateIBAPart(IBAHolder ibaholder) throws Exception {
    ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null, 
      SessionHelper.manager.getLocale(), null);
    DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)
      ibaholder.getAttributeContainer();
    for (Enumeration enumeration = this.ibaContainer.elements(); 
      enumeration.hasMoreElements(); ) {
      try
      {
        Object[] aobj = (Object[])enumeration.nextElement();
        AbstractValueView abstractvalueview = (AbstractValueView)aobj[1];
        AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView)
          aobj[0];
        if (abstractvalueview.getState() == 1) {
          defaultattributecontainer.deleteAttributeValues(
            attributedefdefaultview);
          abstractvalueview.setState(3);
          defaultattributecontainer.addAttributeValue(abstractvalueview);
        }
      }
      catch (Exception exception) {
        exception.printStackTrace();
      }
    }

    ibaholder.setAttributeContainer(defaultattributecontainer);
    return ibaholder;
  }

  public void setIBAValue(String s, String s1) throws WTPropertyVetoException
  {
    AbstractValueView abstractvalueview = null;
    AttributeDefDefaultView attributedefdefaultview = null;
    Object[] aobj = (Object[])this.ibaContainer.get(s);
    if (aobj != null) {
      abstractvalueview = (AbstractValueView)aobj[1];
      attributedefdefaultview = (AttributeDefDefaultView)aobj[0];
    }
    if (abstractvalueview == null) {
      attributedefdefaultview = getAttributeDefinition(s);
    }
    if (attributedefdefaultview == null) {
      System.out.println("definition is null ...");
      return;
    }
    abstractvalueview = internalCreateValue(attributedefdefaultview, s1);
    if (abstractvalueview == null) {
      System.out.println("after creation, iba value is null ..");
      return;
    }

    abstractvalueview.setState(1);
    Object[] aobj1 = new Object[2];
    aobj1[0] = attributedefdefaultview;
    aobj1[1] = abstractvalueview;

    this.ibaContainer.put(attributedefdefaultview.getName(), aobj1);
  }

  private AttributeDefDefaultView getAttributeDefinition(String s)
  {
    AttributeDefDefaultView attributedefdefaultview = null;
    try {
      attributedefdefaultview = IBADefinitionHelper.service
        .getAttributeDefDefaultViewByPath(s);
      if (attributedefdefaultview == null) {
        AbstractAttributeDefinizerView abstractattributedefinizerview = 
          DefinitionLoader.getAttributeDefinition(s);
        if (abstractattributedefinizerview != null)
          attributedefdefaultview = IBADefinitionHelper.service
            .getAttributeDefDefaultView((AttributeDefNodeView)
            abstractattributedefinizerview);
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
      abstractvalueview = LoadValue.newFloatValue(
        abstractattributedefinizerview, s, null);
    }
    else if ((abstractattributedefinizerview instanceof StringDefView)) {
      abstractvalueview = LoadValue.newStringValue(
        abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof IntegerDefView)) {
      abstractvalueview = LoadValue.newIntegerValue(
        abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof RatioDefView)) {
      abstractvalueview = LoadValue.newRatioValue(
        abstractattributedefinizerview, s, null);
    }
    else if ((abstractattributedefinizerview instanceof TimestampDefView)) {
      abstractvalueview = LoadValue.newTimestampValue(
        abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof BooleanDefView)) {
      abstractvalueview = LoadValue.newBooleanValue(
        abstractattributedefinizerview, s);
    }
    else if ((abstractattributedefinizerview instanceof URLDefView)) {
      abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, 
        s, null);
    }
    else if ((abstractattributedefinizerview instanceof ReferenceDefView)) {
      abstractvalueview = LoadValue.newReferenceValue(
        abstractattributedefinizerview, "ClassificationNode", s);
    }
    else if ((abstractattributedefinizerview instanceof UnitDefView)) {
      abstractvalueview = LoadValue.newUnitValue(abstractattributedefinizerview, 
        s, null);
    }
    return abstractvalueview;
  }
}