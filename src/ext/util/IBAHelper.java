package ext.util;

import java.io.PrintStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import wt.fc.ObjectIdentifier;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerNodeView;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.AttributeOrgNodeView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.definition.service.IBADefinitionService;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.StringValue;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueDBServiceInterface;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.IBAValueService;
import wt.iba.value.service.LoadValue;
import wt.method.RemoteMethodServer;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.services.ManagerService;
import wt.services.ManagerServiceFactory;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class IBAHelper
  implements Serializable
{
  private static final String CLASSNAME = IBAHelper.class.getName();
  private static Locale LOCALE = Locale.CHINA;

  public static Hashtable getAllIBAValues(WTObject obj)
    throws WTException
  {
    Hashtable hashtable = new Hashtable();
    try
    {
      if ((obj instanceof IBAHolder)) {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer dac = getContainer(ibaholder);

        if (dac != null) {
          AbstractValueView[] avv = (AbstractValueView[])null;
          avv = dac.getAttributeValues();

          for (int j = 0; j < avv.length; j++) {
            String thisIBAName = avv[j].getDefinition().getName();
            String thisIBAValue = IBAValueUtility.getLocalizedIBAValueDisplayString(
              avv[j], LOCALE);
            String thisIBAClass = avv[j].getDefinition()
              .getAttributeDefinitionClassName();
            if (thisIBAClass.equals("wt.iba.definition.FloatDefinition")) {
              float value = (float)((FloatValueDefaultView)avv[j]).getValue();
              hashtable.put(thisIBAName, new Float(value));
            }
            else if (thisIBAClass.equals("wt.iba.definition.IntegerDefinition")) {
              long value = ((IntegerValueDefaultView)avv[j]).getValue();
              hashtable.put(thisIBAName, String.valueOf(value));
            }
            else if (thisIBAClass.equals("wt.iba.definition.StringDefinition")) {
              String value = ((StringValueDefaultView)avv[j]).getValue();
              hashtable.put(thisIBAName, value);
            }
          }
        }
      }
    }
    catch (RemoteException rexp) {
      System.out.println(" ** !!!!! ** ERROR Getting IBAHelper.getAllIBAValues");
      rexp.printStackTrace();
    }

    return hashtable;
  }

  public static String getIBAStringValue(WTObject obj, String ibaName)
    throws WTException
  {
    String value = null;
    String ibaClass = "wt.iba.definition.StringDefinition";
    try
    {
      if ((obj instanceof IBAHolder)) {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
        if (defaultattributecontainer != null) {
          AbstractValueView avv = getIBAValueView(defaultattributecontainer, ibaName, ibaClass);
          if (avv != null)
            value = ((StringValueDefaultView)avv).getValue();
        }
      }
    }
    catch (RemoteException rexp)
    {
      rexp.printStackTrace();
    }

    return value;
  }

  public static String getStringIBAValueOfObject(IBAHolder p, String ibaName)
  {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "getStringIBAValueOfObject";
      Class[] types = { IBAHolder.class, String.class };
      Object[] vals = { p, ibaName };
      try {
        return (String)RemoteMethodServer.getDefault().invoke(method, CLASSNAME, null, types, vals);
      }
      catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    boolean access = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      AttributeDefDefaultView addv = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(ibaName);
      if (addv == null)
      {
        return null;
      }
      long ibaDefId = addv.getObjectID().getId();
      long prjObid = PersistenceHelper.getObjectIdentifier((Persistable)p).getId();
      QuerySpec qs = new QuerySpec(StringValue.class);
      qs.appendWhere(new SearchCondition(StringValue.class, "definitionReference.key.id", 
        "=", ibaDefId), new int[1]);
      qs.appendAnd();
      qs.appendWhere(new SearchCondition(StringValue.class, "theIBAHolderReference.key.id", 
        "=", prjObid), new int[1]);
      qs.setAdvancedQueryEnabled(true);
      QueryResult qr = PersistenceHelper.manager.find(qs);
      if (qr.hasMoreElements()) {
        return ((StringValue)qr.nextElement()).getValue();
      }
      return null;
    }
    catch (Exception e) {
      e.printStackTrace();
    } finally {
      SessionServerHelper.manager.setAccessEnforced(access);
    }
    return null;
  }

  public static IBAHolder setObjectIBAValueNoCheckout(IBAHolder ibaholder, String attrName, String attrValue) {
    if (!RemoteMethodServer.ServerFlag) {
      String method = "setObjectIBAValueNoCheckout";
      Class[] types = { IBAHolder.class, String.class, String.class };
      Object[] vals = { ibaholder, attrName, attrValue };
      IBAHolder rtn = ibaholder;
      try {
        rtn = (IBAHolder)RemoteMethodServer.getDefault().invoke(method, CLASSNAME, null, types, vals);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return rtn;
    }
    boolean checkFlag = SessionServerHelper.manager.setAccessEnforced(false);
    try {
      AbstractAttributeDefinizerView aadv = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(attrName);
      if (aadv != null)
      {
        Object obj = null;
        if ((aadv instanceof StringDefView)) {
          obj = new StringValueDefaultView((StringDefView)aadv, String.valueOf(attrValue));
        }

        if (obj != null) {
          ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, "CSM", null, null);
          DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)ibaholder.getAttributeContainer();
          if (defaultattributecontainer == null) {
            defaultattributecontainer = new DefaultAttributeContainer();
            defaultattributecontainer.addAttributeValue((AbstractValueView)obj);
            ibaholder.setAttributeContainer(defaultattributecontainer);
          } else {
            AbstractValueView[] oldavv = defaultattributecontainer.getAttributeValues((AttributeDefDefaultView)aadv);
            for (int k = 0; k < oldavv.length; k++)
              defaultattributecontainer.deleteAttributeValue(oldavv[k]);
            defaultattributecontainer.addAttributeValue((AbstractValueView)obj);
            IBAValueDBServiceInterface dbService = (IBAValueDBServiceInterface)ManagerServiceFactory.getDefault().getManager(IBAValueDBService.class);
            defaultattributecontainer = (DefaultAttributeContainer)dbService.updateAttributeContainer(ibaholder, defaultattributecontainer.getConstraintParameter(), null, null);
          }
          ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, "CSM", null, null);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      SessionServerHelper.manager.setAccessEnforced(checkFlag);
    }
    return ibaholder;
  }

  public static String setIBAStringValue(WTObject obj, String ibaName, String newvalue)
    throws WTException
  {
    String value = null;
    String ibaClass = "wt.iba.definition.StringDefinition";
    try {
      if ((obj instanceof IBAHolder))
      {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
        if (defaultattributecontainer != null)
        {
          StringValueDefaultView avv = (StringValueDefaultView)getIBAValueView(defaultattributecontainer, ibaName, ibaClass);
          if (avv != null)
          {
            avv.setValue(newvalue);
            defaultattributecontainer.updateAttributeValue(avv);
            ibaholder.setAttributeContainer(defaultattributecontainer);
            LoadValue.applySoftAttributes(ibaholder);
          }
        }
      }
    }
    catch (Exception rexp)
    {
      rexp.printStackTrace();
    }

    return value;
  }

  public static String setIBAStringValue(WTObject obj, String ibaName, String path, String newvalue)
    throws WTException
  {
    String value = null;
    String ibaClass = "wt.iba.definition.StringDefinition";
    try {
      if ((obj instanceof IBAHolder))
      {
        IBAHolder ibaholder = (IBAHolder)obj;
        DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
        if (defaultattributecontainer != null)
        {
          StringValueDefaultView avv = (StringValueDefaultView)getIBAValueView(defaultattributecontainer, ibaName, ibaClass);
          if (avv != null)
          {
            avv.setValue(newvalue);
            defaultattributecontainer.updateAttributeValue(avv);
          }
          else
          {
            StringDefView adv = getStringDefViewByPath(path);
            avv = new StringValueDefaultView(adv);
            avv.setValue(newvalue);
            defaultattributecontainer.addAttributeValue(avv);
          }

          ibaholder.setAttributeContainer(defaultattributecontainer);
          LoadValue.applySoftAttributes(ibaholder);
        }
      }
    }
    catch (Exception rexp) {
      rexp.printStackTrace();
    }

    return value;
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
    AbstractValueView[] aabstractvalueview = (AbstractValueView[])null;
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

  public static StringDefView getStringDefViewByPath(String path)
    throws Exception
  {
    StringTokenizer stringtokenizer = new StringTokenizer(path, "/");

    String node1 = stringtokenizer.nextToken();
    String node2 = stringtokenizer.nextToken();
    String ibaName = stringtokenizer.nextToken();

    AttributeOrgNodeView nodeview1 = getAttributeOrganizer(node1);
    AbstractAttributeDefinizerNodeView nodeview2 = getAttributeChildren(nodeview1, node2);
    AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeChildren(nodeview2, ibaName);
    AttributeDefDefaultView adv = IBADefinitionHelper.service.getAttributeDefDefaultView((AttributeDefNodeView)ibaDefNode);

    if ((adv instanceof StringDefView)) {
      return (StringDefView)adv;
    }
    return null;
  }

  private static AttributeOrgNodeView getAttributeOrganizer(String s)
  {
    AttributeOrgNodeView[] aattributeorgnodeview = (AttributeOrgNodeView[])null;
    try
    {
      aattributeorgnodeview = IBADefinitionHelper.service.getAttributeOrganizerRoots();
      for (int i = 0; i < aattributeorgnodeview.length; i++)
      {
        if ((aattributeorgnodeview[i] != null) && 
          (aattributeorgnodeview[i].getName().equalsIgnoreCase(s))) return aattributeorgnodeview[i];
      }
    }
    catch (RemoteException remoteexception)
    {
      remoteexception.printStackTrace();
    }
    catch (WTException wte)
    {
      wte.printStackTrace();
    }

    return null;
  }

  private static AbstractAttributeDefinizerNodeView getAttributeChildren(AbstractAttributeDefinizerNodeView ibaDefNode, String s)
  {
    AbstractAttributeDefinizerNodeView[] aattributeorgnodeview = (AbstractAttributeDefinizerNodeView[])null;
    try
    {
      aattributeorgnodeview = IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
      for (int i = 0; i < aattributeorgnodeview.length; i++)
      {
        if ((aattributeorgnodeview[i] != null) && 
          (aattributeorgnodeview[i].getName().equalsIgnoreCase(s))) return aattributeorgnodeview[i];
      }
    }
    catch (RemoteException remoteexception)
    {
      remoteexception.printStackTrace();
    }
    catch (WTException wte)
    {
      wte.printStackTrace();
    }

    return null;
  }

  private static AttributeDefDefaultView getDefaultViewObject(Object obj)
  {
    AttributeDefDefaultView attributedefdefaultview = null;
    try
    {
      if ((obj instanceof AttributeDefNodeView))
        attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultView((AttributeDefNodeView)obj);
    }
    catch (RemoteException remoteexception)
    {
      remoteexception.printStackTrace();
    }
    catch (WTException wte)
    {
      wte.printStackTrace();
    }

    return attributedefdefaultview;
  }
}