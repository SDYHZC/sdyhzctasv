package ext.util;

/** 
 * IBA Utility Class
 * @version：
 * @author：
 * @modify:
 */
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import wt.csm.businessentity.BusinessEntity;
import wt.csm.navigation.CSMClassificationNavigationException;
import wt.csm.navigation.litenavigation.ClassificationStructDefaultView;
import wt.csm.navigation.service.ClassificationHelper;
import wt.csm.navigation.service.ClassificationService;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceServerHelper;
import wt.iba.constraint.ConstraintGroup;
import wt.iba.constraint.IBAConstraintException;
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
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAContainerException;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractContextualValueDefaultView;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.ReferenceValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.lite.AbstractLiteObject;
import wt.session.SessionHelper;
import wt.units.service.QuantityOfMeasureDefaultView;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class IBAUtility {

	Hashtable ibaContainer;

	Hashtable ibaOrigContainer;

	final static String UNITS = "SI";

	boolean VERBOSE = false;

	// Can not be called directly by the end user
	public IBAUtility() {
		ibaContainer = new Hashtable();
	}

	// The only constrator can be called by the end user
	public IBAUtility(IBAHolder ibaHolder) throws WTException {
		super();
		try {
			initializeIBAValue(ibaHolder);

		} catch (Exception e) {
			throw new WTException(e);
		}
	}

	public Enumeration getAttributeDefinitions() {
		return ibaContainer.keys();
	}

	public void removeAllAttributes() throws WTException,
			WTPropertyVetoException {
		ibaContainer.clear();
	}

	public void removeAttribute(String name) throws WTException,
			WTPropertyVetoException {
		ibaContainer.remove(name);
	}

	/**
	 * return single IBA value
	 * 
	 * @param name
	 * @return
	 */
	public String getIBAValue(String name) {
		String value = null;
		try {
			if (ibaContainer.get(name) != null) {
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(name))[1];
				value = (IBAValueUtility.getLocalizedIBAValueDisplayString(
						theValue, SessionHelper.manager.getLocale()));
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * return multiple IBA values
	 * 
	 * @param name
	 * @return
	 */
	public Vector getIBAValues(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					vector.addElement(IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale()));
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	/**
	 * return ALL IBA values
	 * 
	 * @return Hashtable
	 */
	public Hashtable getAllIBAValues() {
		return ibaContainer;
	}

	/**
	 * return multiple IBA values & dependency relationship
	 * 
	 * @param name
	 * @return
	 */
	public Vector getIBAValuesWithDependency(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					String[] temp = new String[3];
					temp[0] = IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale());
					if ((theValue instanceof AbstractContextualValueDefaultView)
							&& ((AbstractContextualValueDefaultView) theValue)
									.getReferenceValueDefaultView() != null) {
						temp[1] = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView()
								.getReferenceDefinition().getName();
						temp[2] = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView()
								.getLocalizedDisplayString();
					} else {
						temp[1] = null;
						temp[2] = null;
					}
					vector.addElement(temp);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	public Vector getIBAValuesWithBusinessEntity(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					Object[] temp = new Object[2];
					temp[0] = IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale());
					if ((theValue instanceof AbstractContextualValueDefaultView)
							&& ((AbstractContextualValueDefaultView) theValue)
									.getReferenceValueDefaultView() != null) {
						ReferenceValueDefaultView referencevaluedefaultview = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView();
						ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
								.getLiteIBAReferenceable()).getObjectID();
						Persistable persistable = ObjectReference
								.newObjectReference(objectidentifier)
								.getObject();
						temp[1] = (BusinessEntity) persistable;
					} else {
						temp[1] = null;
					}
					vector.addElement(temp);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	public BusinessEntity getIBABusinessEntity(String name) {
		BusinessEntity value = null;
		try {
			if (ibaContainer.get(name) != null) {
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(name))[1];
				ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
				ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
						.getLiteIBAReferenceable()).getObjectID();
				Persistable persistable = ObjectReference.newObjectReference(
						objectidentifier).getObject();
				value = (BusinessEntity) persistable;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return value;
	}

	public Vector getIBABusinessEntities(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
					ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
							.getLiteIBAReferenceable()).getObjectID();
					Persistable persistable = ObjectReference
							.newObjectReference(objectidentifier).getObject();
					vector.addElement(persistable);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	private AbstractValueView getAbstractValueView(
			AttributeDefDefaultView theDef, String value) throws WTException,
			WTPropertyVetoException {
		String name = theDef.getName();
		String value2 = null;
		AbstractValueView ibaValue = null;

		if (theDef instanceof UnitDefView) {
			value = value + " " + getDisplayUnits((UnitDefView) theDef, UNITS);
			// System.out.println(value);
		} else if (theDef instanceof ReferenceDefView) {
			value2 = value;
			value = ((ReferenceDefView) theDef).getReferencedClassname();
		} else if (theDef instanceof FloatDefView) {
			// value = Utility.trimFloatString(value);
		}

		ibaValue = internalCreateValue(theDef, value, value2);
		if (ibaValue == null) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = " + value + " not found.");
			// return;
		}

		if (ibaValue instanceof ReferenceValueDefaultView) {
			if (VERBOSE)
				System.out.println("Before find original reference : " + name
						+ " has key=" + ibaValue.getKey());
			ibaValue = getOriginalReferenceValue(name, ibaValue);
			if (VERBOSE)
				System.out.println("After find original reference : " + name
						+ " has key=" + ibaValue.getKey());
		}
		ibaValue.setState(AbstractValueView.NEW_STATE);
		return ibaValue;
	}

	private AbstractValueView getOriginalReferenceValue(String name,
			AbstractValueView ibaValue) throws IBAValueException {
		Object[] objs = (Object[]) ibaOrigContainer.get(name);
		if (objs != null && (ibaValue instanceof ReferenceValueDefaultView)) {
			int businessvaluepos = 1;
			for (businessvaluepos = 1; businessvaluepos < objs.length; businessvaluepos++) {
				if (((AbstractValueView) objs[businessvaluepos])
						.compareTo(ibaValue) == 0) {
					ibaValue = (AbstractValueView) objs[businessvaluepos];
					break;
				}
			}
		}
		return ibaValue;
	}

	/*
	 * Henry Change this method from "private" to "public" at 2006/12/08 private
	 * AttributeDefDefaultView getDefDefaultView(String name) throws WTException {
	 */
	public AttributeDefDefaultView getDefDefaultView(String name)
			throws WTException {
		AttributeDefDefaultView theDef = null;
		Object[] obj = (Object[]) ibaContainer.get(name);
		if (obj != null) {
			theDef = (AttributeDefDefaultView) obj[0];
		} else {
			theDef = getAttributeDefinition(name);
		}
		if (theDef == null) {
			System.out.println("IBA name:" + name
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + name + " not existed.");
		}
		return theDef;
	}

	public void setIBAValue(String name, String value) throws WTException,
			WTPropertyVetoException {
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object theValue = getAbstractValueView(theDef, value);
		// System.out.println(name + " put
		// "+((AbstractValueView)theValue).getLocalizedDisplayString());
		Object[] temp = new Object[2];
		temp[0] = theDef;
		temp[1] = theValue;
		ibaContainer.put(name, temp);
	}

	/**
	 * Set the attribute with multiple values from the list
	 * 
	 * @param name
	 * @param values
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public void setIBAValues(String name, Vector values)
			throws WTPropertyVetoException, WTException {
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object[] temp = new Object[values.size() + 1];
		temp[0] = theDef;
		for (int i = 0; i < values.size(); i++) {
			String value = (String) values.get(i);
			Object theValue = getAbstractValueView(theDef, value);
			temp[i + 1] = theValue;
		}
		ibaContainer.put(name, temp);
	}

	public void addIBAValue(String name, String value) throws WTException,
			WTPropertyVetoException {
		Object[] obj = (Object[]) ibaContainer.get(name);
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object theValue = getAbstractValueView(theDef, value);

		Object[] temp;
		if (obj == null) {
			temp = new Object[2];
			temp[0] = theDef;
			temp[1] = theValue;
		} else {
			temp = new Object[obj.length + 1];
			int i;
			for (i = 0; i < obj.length; i++)
				temp[i] = obj[i];
			temp[i] = theValue;
		}

		ibaContainer.put(name, temp);
	}

	private AbstractValueView setDependency(AttributeDefDefaultView sourceDef,
			AbstractValueView sourceValue, AttributeDefDefaultView businessDef,
			AbstractValueView businessValue) throws WTPropertyVetoException,
			WTException {
		String sourcename = sourceDef.getName();
		String businessname = businessDef.getName();

		if (businessValue == null) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value doesn't exist in System Business Entity. Add IBA dependancy failed!!");
		}
		Object[] businessobj = (Object[]) ibaContainer.get(businessname);
		if (businessobj == null) {
			throw new WTException("Part IBA:" + businessname
					+ " Value is null. Add IBA dependancy failed!!");
		}
		int businessvaluepos = 1;
		for (businessvaluepos = 1; businessvaluepos < businessobj.length; businessvaluepos++) {
			if (((AbstractValueView) businessobj[businessvaluepos])
					.compareTo(businessValue) == 0) {
				businessValue = (AbstractValueView) businessobj[businessvaluepos];
				break;
			}
		}
		if (businessvaluepos == businessobj.length) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value:"
							+ businessValue.getLocalizedDisplayString()
							+ " is not existed in Part IBA values. Add IBA dependancy failed!!");
		}

		if (!(businessValue instanceof ReferenceValueDefaultView)) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value:"
							+ businessValue.getLocalizedDisplayString()
							+ " is not a ReferenceValueDefaultView. Add IBA dependancy failed!!");
		}
		((AbstractContextualValueDefaultView) sourceValue)
				.setReferenceValueDefaultView((ReferenceValueDefaultView) businessValue);
		if (VERBOSE)
			System.out.println("ref obj="
					+ ((AbstractContextualValueDefaultView) sourceValue)
							.getReferenceValueDefaultView()
							.getLocalizedDisplayString());
		if (VERBOSE)
			System.out.println("ref key="
					+ ((AbstractContextualValueDefaultView) sourceValue)
							.getReferenceValueDefaultView().getKey());
		if (VERBOSE)
			System.out.println("This IBA:" + sourcename + " value:"
					+ sourceValue.getLocalizedDisplayString()
					+ " add dependancy with Business Entity:" + businessname
					+ " value:" + businessValue.getLocalizedDisplayString()
					+ " successfully with state=" + sourceValue.getState()
					+ " !!");
		return sourceValue;
	}

	public void setIBAValue(String sourcename, String sourcevalue,
			String businessname, String businessvalue)
			throws IBAValueException, WTPropertyVetoException, WTException {

		AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
		AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
		AbstractValueView sourceValue = getAbstractValueView(sourceDef,
				sourcevalue);
		AbstractValueView businessValue = getAbstractValueView(businessDef,
				businessvalue);
		sourceValue = setDependency(sourceDef, sourceValue, businessDef,
				businessValue);
		Object[] temp = new Object[2];
		temp[0] = sourceDef;
		temp[1] = sourceValue;
		ibaContainer.put(sourcename, temp);
	}

	/**
	 * Add an IBA value with dependency relation
	 * 
	 * @param sourcename
	 * @param sourcevalue
	 * @param businessname
	 * @param businessvalue
	 * @throws IBAValueException
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */

	public void addIBAValue(String sourcename, String sourcevalue,
			String businessname, String businessvalue)
			throws IBAValueException, WTPropertyVetoException, WTException {
		AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
		AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
		AbstractValueView sourceValue = getAbstractValueView(sourceDef,
				sourcevalue);
		AbstractValueView businessValue = getAbstractValueView(businessDef,
				businessvalue);
		sourceValue = setDependency(sourceDef, sourceValue, businessDef,
				businessValue);

		Object[] obj = (Object[]) ibaContainer.get(sourcename);
		Object[] temp;
		if (obj == null) {
			temp = new Object[2];
			temp[0] = sourceDef;
			temp[1] = sourceValue;
		} else {
			temp = new Object[obj.length + 1];
			int i;
			for (i = 0; i < obj.length; i++)
				temp[i] = obj[i];
			temp[i] = sourceValue;
		}
		ibaContainer.put(sourcename, temp);
	}

	// initializePart() with this signature is designed to pre-populate values
	// from an existing IBA holder.
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	private void initializeIBAValue(IBAHolder ibaHolder) throws WTException,
			RemoteException {
		ibaContainer = new Hashtable();
		ibaOrigContainer = new Hashtable();
		if (ibaHolder.getAttributeContainer() == null)
			ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
					ibaHolder, null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer theContainer = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		if (theContainer != null) {
			AttributeDefDefaultView[] theAtts = theContainer
					.getAttributeDefinitions();
			for (int i = 0; i < theAtts.length; i++) {
				AbstractValueView[] theValues = theContainer
						.getAttributeValues(theAtts[i]);
				if (theValues != null) {
					// Add by Somesh
					Object[] temp = new Object[theValues.length + 1];
					temp[0] = theAtts[i];
					for (int j = 1; j <= theValues.length; j++) {
						temp[j] = theValues[j - 1];
					}
					// End Add by Somesh
					ibaContainer.put(theAtts[i].getName(), temp);
					ibaOrigContainer.put(theAtts[i].getName(), temp);
				}
			}
		}
		// System.out.println("initializeIBAValue : ibaContainer = " +
		// ibaContainer);
		// System.out.println("initializeIBAValue : ibaOrigContainer = " +
		// ibaOrigContainer);
	}

	private DefaultAttributeContainer suppressCSMConstraint(
			DefaultAttributeContainer theContainer, String s)
			throws WTException {
		// rjla 2000-11-17
		// If the part classification IBA is to be updated, we must prevent the
		// CSM constraint
		// that makes it immutable from being applied.
		// Note that suppressing this constraint here - as the container is
		// updated - does not
		// remove the need to suppress it again when the updated part is parted;
		// in other words
		// you still need RemoveCSMConstraint line.
		// AttributeDefDefaultView definitions[] =
		// theContainer.getAttributeDefinitions();
		ClassificationStructDefaultView defStructure = null;
		// defStructure =
		// ClassificationHelper.service.getClassificationStructDefaultView("wt.csm.businessentity.BusinessEntity");
		defStructure = getClassificationStructDefaultViewByName(s);
		if (defStructure != null) {
			// ReferenceDefView ref = defStructure.getReferenceDefView();
			Vector cgs = theContainer.getConstraintGroups();
			Vector newCgs = new Vector();
			// AttributeConstraint immutable = null;
			try {
				// if (VERBOSE)
				// System.out.println("cgs size="+cgs.size());
				for (int i = 0; i < cgs.size(); i++) {
					ConstraintGroup cg = (ConstraintGroup) cgs.elementAt(i);
					if (cg != null) {
						// System.out.println(cg.getConstraintGroupLabel());
						if (!cg
								.getConstraintGroupLabel()
								.equals(
										wt.csm.constraint.CSMConstraintFactory.CONSTRAINT_GROUP_LABEL)) {
							newCgs.addElement(cg);
						} else {
							// Enumeration enum = cg.getConstraints();
							ConstraintGroup newCg = new ConstraintGroup();
							newCg.setConstraintGroupLabel(cg
									.getConstraintGroupLabel());
							/*
							 * while (enum.hasMoreElements()){
							 * AttributeConstraint ac =
							 * (AttributeConstraint)enum.nextElement(); if
							 * ((ac.appliesToAttrDef(ref)) &&
							 * (ac.getValueConstraint() instanceof
							 * wt.iba.constraint.Immutable)){ immutable = ac; }
							 * else { newCg.addConstraint(ac); } }
							 */

							newCgs.addElement(newCg);
						}
					}
				}
				theContainer.setConstraintGroups(newCgs);
			} catch (wt.util.WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}
		// end of CSM constraint removal, rjla 2000-11-17
		return theContainer;
	}

	private DefaultAttributeContainer removeCSMConstraint(
			DefaultAttributeContainer attributecontainer) {
		Object obj = attributecontainer.getConstraintParameter();
		if (obj == null)
			obj = new String("CSM");
		else if (obj instanceof Vector) {
			((Vector) obj).addElement(new String("CSM"));
		} else {
			Vector vector1 = new Vector();
			vector1.addElement(obj);
			obj = vector1;
			((Vector) obj).addElement(new String("CSM"));
		}
		try {
			attributecontainer.setConstraintParameter(obj);
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			wtpropertyvetoexception.printStackTrace();

		}
		return attributecontainer;
	}

	/**
	 * Update the IBAHolder's attribute container from the hashtable
	 * 
	 * @param ibaHolder
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * @throws RemoteException
	 */
	public IBAHolder updateAttributeContainer(IBAHolder ibaHolder)
			throws WTException, WTPropertyVetoException, RemoteException {
		if (ibaHolder.getAttributeContainer() == null)
			ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
					ibaHolder, null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();

		defaultattributecontainer = suppressCSMConstraint(
				defaultattributecontainer, getIBAHolderClassName(ibaHolder));

		AttributeDefDefaultView[] theAtts = defaultattributecontainer
				.getAttributeDefinitions();
		// Delete existed iba if they aren't in the hashtable of this class
		for (int i = 0; i < theAtts.length; i++) {
			AttributeDefDefaultView theDef = theAtts[i];
			if (ibaContainer.get(theDef.getName()) == null) {
				createOrUpdateAttributeValuesInContainer(
						defaultattributecontainer, theDef, null);
			}
		}

		// Update the iba from the hashtable to the attribute container of the
		// iba holder
		/*
		 * Enumeration enum = ibaContainer.elements(); while (
		 * enum.hasMoreElements() ) { Object[] temp = (Object[])
		 * enum.nextElement(); AttributeDefDefaultView theDef =
		 * (AttributeDefDefaultView) temp[0]; if (theDef instanceof
		 * ReferenceDefView) { AbstractValueView abstractvalueviews[] = new
		 * AbstractValueView[temp.length-1]; for (int i=0; i<temp.length-1;
		 * i++) { abstractvalueviews[i] = (AbstractValueView)temp[i+1]; }
		 * createOrUpdateAttributeValuesInContainer(defaultattributecontainer,
		 * theDef, abstractvalueviews); } }
		 */

		Enumeration enum1 = ibaContainer.elements();
		while (enum1.hasMoreElements()) {
			Object[] temp = (Object[]) enum1.nextElement();
			AttributeDefDefaultView theDef = (AttributeDefDefaultView) temp[0];
			AbstractValueView abstractvalueviews[] = new AbstractValueView[temp.length - 1];
			for (int i = 0; i < temp.length - 1; i++) {
				abstractvalueviews[i] = (AbstractValueView) temp[i + 1];
			}
			createOrUpdateAttributeValuesInContainer(defaultattributecontainer,
					theDef, abstractvalueviews);
		}

		defaultattributecontainer = removeCSMConstraint(defaultattributecontainer);
		ibaHolder.setAttributeContainer(defaultattributecontainer);

		return ibaHolder;
	}

	/**
	 * Update without checkout/checkin
	 * 
	 * @param ibaholder
	 * @return
	 */
	public static boolean updateIBAHolder(IBAHolder ibaholder)
			throws WTException {
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		boolean flag = true;
		try {
			PersistenceServerHelper.manager.update((Persistable) ibaholder);
			AttributeContainer attributecontainer = ibaholder
					.getAttributeContainer();
			Object obj = ((DefaultAttributeContainer) attributecontainer)
					.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);
		} catch (WTException wtexception) {
			System.out.println("updateIBAHOlder: Couldn't update. "
					+ wtexception);
			flag = false;
			wtexception.printStackTrace();
			throw new WTException(wtexception);
		}
		return flag;
	}

	/**
	 * Referenced from method "createOrUpdateAttributeValueInContainer" of
	 * wt.iba.value.service.LoadValue.java -> modified to have multi-values
	 * support
	 * 
	 * @param defaultattributecontainer
	 * @param theDef
	 * @param abstractvalueviews
	 * @throws WTException
	 */
	private void createOrUpdateAttributeValuesInContainer(
			DefaultAttributeContainer defaultattributecontainer,
			AttributeDefDefaultView theDef,
			AbstractValueView[] abstractvalueviews) throws WTException,
			WTPropertyVetoException {
		if (defaultattributecontainer == null)
			throw new IBAContainerException(
					"wt.iba.value.service.LoadValue.createOrUpdateAttributeValueInContainer :  DefaultAttributeContainer passed in is null!");
		AbstractValueView abstractvalueviews0[] = defaultattributecontainer
				.getAttributeValues(theDef);
		try {
			if (abstractvalueviews0 == null || abstractvalueviews0.length == 0) {
				// Original valus is empty
				for (int j = 0; j < abstractvalueviews.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews[j];
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
					// System.out.println("IBAUtil:"+abstractvalueview.getLocalizedDisplayString()+"
					// in "+abstractvalueview.getDefinition().getName());
				}
			} else if (abstractvalueviews == null
					|| abstractvalueviews.length == 0) {
				// New value is empty, so delete all existed values
				for (int j = 0; j < abstractvalueviews0.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews0[j];
					defaultattributecontainer
							.deleteAttributeValue(abstractvalueview);
				}
			} else if (abstractvalueviews0.length <= abstractvalueviews.length) {

				// More new valuss than (or equal to) original values,
				// So update existed values and add new values
				for (int j = 0; j < abstractvalueviews0.length; j++) {
					abstractvalueviews0[j] = LoadValue.cloneAbstractValueView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					// abstractvalueviews0[j] = abstractvalueviews[j];
					abstractvalueviews0[j] = cloneReferenceValueDefaultView(
							abstractvalueviews[j], abstractvalueviews0[j]);

					defaultattributecontainer
							.updateAttributeValue(abstractvalueviews0[j]);
				}
				for (int j = abstractvalueviews0.length; j < abstractvalueviews.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews[j];
					// abstractvalueview.setState(AbstractValueView.CHANGED_STATE);
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
				}
			} else if (abstractvalueviews0.length > abstractvalueviews.length) {
				// Less new values than original values,
				// So delete some values
				for (int j = 0; j < abstractvalueviews.length; j++) {
					abstractvalueviews0[j] = LoadValue.cloneAbstractValueView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					abstractvalueviews0[j] = cloneReferenceValueDefaultView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					// abstractvalueviews0[j] = abstractvalueviews[j];
					defaultattributecontainer
							.updateAttributeValue(abstractvalueviews0[j]);
				}
				for (int j = abstractvalueviews.length; j < abstractvalueviews0.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews0[j];
					defaultattributecontainer
							.deleteAttributeValue(abstractvalueview);
				}
			}
		} catch (IBAConstraintException ibaconstraintexception) {
			ibaconstraintexception.printStackTrace();
		}
	}

	// For dependency used.
	AbstractValueView cloneReferenceValueDefaultView(
			AbstractValueView abstractvalueview,
			AbstractValueView abstractvalueview1) throws IBAValueException {
		if (abstractvalueview instanceof AbstractContextualValueDefaultView) {
			if (VERBOSE) {
				System.out.println(abstractvalueview1
						.getLocalizedDisplayString()
						+ ":" + abstractvalueview.getLocalizedDisplayString());
				if (((AbstractContextualValueDefaultView) abstractvalueview1)
						.getReferenceValueDefaultView() != null)
					System.out
							.println("Key before set="
									+ ((AbstractContextualValueDefaultView) abstractvalueview1)
											.getReferenceValueDefaultView()
											.getKey());
			}

			try {
				((AbstractContextualValueDefaultView) abstractvalueview1)
						.setReferenceValueDefaultView(((AbstractContextualValueDefaultView) abstractvalueview)
								.getReferenceValueDefaultView());
			} catch (WTPropertyVetoException wtpropertyvetoexception) {
				throw new IBAValueException(
						"can't get ReferenceValueDefaultView from the Part in the database");
			}
			if (VERBOSE) {
				if (((AbstractContextualValueDefaultView) abstractvalueview1)
						.getReferenceValueDefaultView() != null)
					System.out
							.println("Key after set="
									+ ((AbstractContextualValueDefaultView) abstractvalueview1)
											.getReferenceValueDefaultView()
											.getKey());
			}

		}
		return abstractvalueview1;
	}

	/**
	 * another "black-box": pass in a string, and get back an IBA value object.
	 * Copy from wt.iba.value.service.LoadValue.java -> please don't modify this
	 * method
	 * 
	 * @param abstractattributedefinizerview
	 * @param s
	 * @param s1
	 * @return
	 */
	private static AbstractValueView internalCreateValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s, String s1) {
		AbstractValueView abstractvalueview = null;
		if (abstractattributedefinizerview instanceof FloatDefView)
			abstractvalueview = LoadValue.newFloatValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof StringDefView)
			abstractvalueview = LoadValue.newStringValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof IntegerDefView)
			abstractvalueview = LoadValue.newIntegerValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof RatioDefView)
			abstractvalueview = LoadValue.newRatioValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof TimestampDefView)
			abstractvalueview = LoadValue.newTimestampValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof BooleanDefView)
			abstractvalueview = LoadValue.newBooleanValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof URLDefView)
			abstractvalueview = LoadValue.newURLValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof ReferenceDefView)
			abstractvalueview = LoadValue.newReferenceValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof UnitDefView)
			abstractvalueview = LoadValue.newUnitValue(
					abstractattributedefinizerview, s, s1);

		return abstractvalueview;
	}

	// //////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is a "black-box": pass in a string, like
	 * "Electrical/Resistance/ ResistanceRating" and get back a IBA definition
	 * object.
	 * 
	 * @param ibaPath
	 * @return
	 */
	public AttributeDefDefaultView getAttributeDefinition(String ibaPath) {

		AttributeDefDefaultView ibaDef = null;
		try {
			ibaDef = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(ibaPath);
			if (ibaDef == null) {
				AbstractAttributeDefinizerView ibaNodeView = DefinitionLoader
						.getAttributeDefinition(ibaPath);
				if (ibaNodeView != null)
					ibaDef = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) ibaNodeView);
			}
		} catch (Exception wte) {
			wte.printStackTrace();
		}

		return ibaDef;
	}

	public static String getDisplayUnits(UnitDefView unitdefview) {
		return getDisplayUnits(unitdefview, UNITS);
	}

	public static String getDisplayUnits(UnitDefView unitdefview, String s) {
		QuantityOfMeasureDefaultView quantityofmeasuredefaultview = unitdefview
				.getQuantityOfMeasureDefaultView();
		String s1 = quantityofmeasuredefaultview.getBaseUnit();
		if (s != null) {
			String s2 = unitdefview.getDisplayUnitString(s);
			if (s2 == null)
				s2 = quantityofmeasuredefaultview.getDisplayUnitString(s);
			if (s2 == null)
				s2 = quantityofmeasuredefaultview
						.getDefaultDisplayUnitString(s);
			if (s2 != null)
				s1 = s2;
		}
		if (s1 == null)
			return "";
		else
			return s1;
	}

	public static String getClassificationStructName(IBAHolder ibaHolder)
			throws IBAConstraintException {
		String s = getIBAHolderClassName(ibaHolder);
		ClassificationService classificationservice = ClassificationHelper.service;
		ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
		try {
			aclassificationstructdefaultview = classificationservice
					.getAllClassificationStructures();
		} catch (RemoteException remoteexception) {
			remoteexception.printStackTrace();
			throw new IBAConstraintException(remoteexception);
		} catch (CSMClassificationNavigationException csmclassificationnavigationexception) {
			csmclassificationnavigationexception.printStackTrace();
			throw new IBAConstraintException(
					csmclassificationnavigationexception);
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
			throw new IBAConstraintException(wtexception);
		}
		for (int i = 0; aclassificationstructdefaultview != null
				&& i < aclassificationstructdefaultview.length; i++)
			if (s.equals(aclassificationstructdefaultview[i]
					.getPrimaryClassName())) {
				return s;
			}

		try {
			for (Class class1 = Class.forName(s).getSuperclass(); !class1
					.getName().equals((wt.fc.WTObject.class).getName())
					&& !class1.getName().equals(
							(java.lang.Object.class).getName()); class1 = class1
					.getSuperclass()) {
				for (int j = 0; aclassificationstructdefaultview != null
						&& j < aclassificationstructdefaultview.length; j++)
					if (class1.getName().equals(
							aclassificationstructdefaultview[j]
									.getPrimaryClassName())) {
						return class1.getName();
					}
			}
		} catch (ClassNotFoundException classnotfoundexception) {
			classnotfoundexception.printStackTrace();
		}
		return null;
	}

	/**
	 * Please refer to the method "getIBAHolderClassName" of class
	 * "wt.csm.constraint.CSMConstraintFactory"
	 * 
	 * @param ibaholder
	 * @return
	 */
	private static String getIBAHolderClassName(IBAHolder ibaholder) {
		String s = null;
		if (ibaholder instanceof AbstractLiteObject)
			s = ((AbstractLiteObject) ibaholder).getHeavyObjectClassname();
		else
			s = ibaholder.getClass().getName();
		return s;
	}

	/**
	 * Please refer to the method "getClassificationStructDefaultViewByName" of
	 * class "wt.csm.constraint.CSMConstraintFactory"
	 * 
	 * @param s
	 * @return
	 * @throws IBAConstraintException
	 */
	private ClassificationStructDefaultView getClassificationStructDefaultViewByName(
			String s) throws IBAConstraintException {
		ClassificationService classificationservice = ClassificationHelper.service;
		ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
		try {
			aclassificationstructdefaultview = classificationservice
					.getAllClassificationStructures();
		} catch (RemoteException remoteexception) {
			remoteexception.printStackTrace();
			throw new IBAConstraintException(remoteexception);
		} catch (CSMClassificationNavigationException csmclassificationnavigationexception) {
			csmclassificationnavigationexception.printStackTrace();
			throw new IBAConstraintException(
					csmclassificationnavigationexception);
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
			throw new IBAConstraintException(wtexception);
		}
		for (int i = 0; aclassificationstructdefaultview != null
				&& i < aclassificationstructdefaultview.length; i++)
			if (s.equals(aclassificationstructdefaultview[i]
					.getPrimaryClassName())) {
				return aclassificationstructdefaultview[i];
			}

		try {
			for (Class class1 = Class.forName(s).getSuperclass(); !class1
					.getName().equals((wt.fc.WTObject.class).getName())
					&& !class1.getName().equals(
							(java.lang.Object.class).getName()); class1 = class1
					.getSuperclass()) {
				for (int j = 0; aclassificationstructdefaultview != null
						&& j < aclassificationstructdefaultview.length; j++)
					if (class1.getName().equals(
							aclassificationstructdefaultview[j]
									.getPrimaryClassName())) {
						return aclassificationstructdefaultview[j];
					}
			}
		} catch (ClassNotFoundException classnotfoundexception) {
			classnotfoundexception.printStackTrace();
		}
		return null;
	}
}