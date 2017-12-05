package ext.hbt.reviewobjecttype.model;

@SuppressWarnings({"cast", "deprecation", "unchecked"})
public abstract class _TypeItem extends wt.fc.WTObject implements java.io.Externalizable {
   static final long serialVersionUID = 1;

   static final java.lang.String RESOURCE = "ext.hbt.reviewobjecttype.model.modelResource";
   static final java.lang.String CLASSNAME = TypeItem.class.getName();

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String ITEM_TYPE = "itemType";
   static int ITEM_TYPE_UPPER_LIMIT = -1;
   java.lang.String itemType;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getItemType() {
      return itemType;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setItemType(java.lang.String itemType) throws wt.util.WTPropertyVetoException {
      itemTypeValidate(itemType);
      this.itemType = itemType;
   }
   void itemTypeValidate(java.lang.String itemType) throws wt.util.WTPropertyVetoException {
      if (ITEM_TYPE_UPPER_LIMIT < 1) {
         try { ITEM_TYPE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("itemType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ITEM_TYPE_UPPER_LIMIT = 200; }
      }
      if (itemType != null && !wt.fc.PersistenceHelper.checkStoredLength(itemType.toString(), ITEM_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "itemType"), java.lang.String.valueOf(java.lang.Math.min(ITEM_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "itemType", this.itemType, itemType));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String FULL_TYPE_NAME = "fullTypeName";
   static int FULL_TYPE_NAME_UPPER_LIMIT = -1;
   java.lang.String fullTypeName;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getFullTypeName() {
      return fullTypeName;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setFullTypeName(java.lang.String fullTypeName) throws wt.util.WTPropertyVetoException {
      fullTypeNameValidate(fullTypeName);
      this.fullTypeName = fullTypeName;
   }
   void fullTypeNameValidate(java.lang.String fullTypeName) throws wt.util.WTPropertyVetoException {
      if (FULL_TYPE_NAME_UPPER_LIMIT < 1) {
         try { FULL_TYPE_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("fullTypeName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { FULL_TYPE_NAME_UPPER_LIMIT = 200; }
      }
      if (fullTypeName != null && !wt.fc.PersistenceHelper.checkStoredLength(fullTypeName.toString(), FULL_TYPE_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "fullTypeName"), java.lang.String.valueOf(java.lang.Math.min(FULL_TYPE_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "fullTypeName", this.fullTypeName, fullTypeName));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String TYPECOMMENT = "typecomment";
   static int TYPECOMMENT_UPPER_LIMIT = -1;
   java.lang.String typecomment;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getTypecomment() {
      return typecomment;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setTypecomment(java.lang.String typecomment) throws wt.util.WTPropertyVetoException {
      typecommentValidate(typecomment);
      this.typecomment = typecomment;
   }
   void typecommentValidate(java.lang.String typecomment) throws wt.util.WTPropertyVetoException {
      if (TYPECOMMENT_UPPER_LIMIT < 1) {
         try { TYPECOMMENT_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("typecomment").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { TYPECOMMENT_UPPER_LIMIT = 200; }
      }
      if (typecomment != null && !wt.fc.PersistenceHelper.checkStoredLength(typecomment.toString(), TYPECOMMENT_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "typecomment"), java.lang.String.valueOf(java.lang.Math.min(TYPECOMMENT_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "typecomment", this.typecomment, typecomment));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String PARENT_TYPE_ITEM = "parentTypeItem";
   wt.fc.ObjectReference parentTypeItem;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public wt.fc.ObjectReference getParentTypeItem() {
      return parentTypeItem;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setParentTypeItem(wt.fc.ObjectReference parentTypeItem) throws wt.util.WTPropertyVetoException {
      parentTypeItemValidate(parentTypeItem);
      this.parentTypeItem = parentTypeItem;
   }
   void parentTypeItemValidate(wt.fc.ObjectReference parentTypeItem) throws wt.util.WTPropertyVetoException {
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String TYPE_NAME = "typeName";
   static int TYPE_NAME_UPPER_LIMIT = -1;
   java.lang.String typeName;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getTypeName() {
      return typeName;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setTypeName(java.lang.String typeName) throws wt.util.WTPropertyVetoException {
      typeNameValidate(typeName);
      this.typeName = typeName;
   }
   void typeNameValidate(java.lang.String typeName) throws wt.util.WTPropertyVetoException {
      if (TYPE_NAME_UPPER_LIMIT < 1) {
         try { TYPE_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("typeName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { TYPE_NAME_UPPER_LIMIT = 200; }
      }
      if (typeName != null && !wt.fc.PersistenceHelper.checkStoredLength(typeName.toString(), TYPE_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "typeName"), java.lang.String.valueOf(java.lang.Math.min(TYPE_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "typeName", this.typeName, typeName));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String OBJ_TYPE1 = "objType1";
   static int OBJ_TYPE1_UPPER_LIMIT = -1;
   java.lang.String objType1;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getObjType1() {
      return objType1;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setObjType1(java.lang.String objType1) throws wt.util.WTPropertyVetoException {
      objType1Validate(objType1);
      this.objType1 = objType1;
   }
   void objType1Validate(java.lang.String objType1) throws wt.util.WTPropertyVetoException {
      if (OBJ_TYPE1_UPPER_LIMIT < 1) {
         try { OBJ_TYPE1_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("objType1").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { OBJ_TYPE1_UPPER_LIMIT = 200; }
      }
      if (objType1 != null && !wt.fc.PersistenceHelper.checkStoredLength(objType1.toString(), OBJ_TYPE1_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "objType1"), java.lang.String.valueOf(java.lang.Math.min(OBJ_TYPE1_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "objType1", this.objType1, objType1));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String OBJ_TYPE2 = "objType2";
   static int OBJ_TYPE2_UPPER_LIMIT = -1;
   java.lang.String objType2;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getObjType2() {
      return objType2;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setObjType2(java.lang.String objType2) throws wt.util.WTPropertyVetoException {
      objType2Validate(objType2);
      this.objType2 = objType2;
   }
   void objType2Validate(java.lang.String objType2) throws wt.util.WTPropertyVetoException {
      if (OBJ_TYPE2_UPPER_LIMIT < 1) {
         try { OBJ_TYPE2_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("objType2").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { OBJ_TYPE2_UPPER_LIMIT = 200; }
      }
      if (objType2 != null && !wt.fc.PersistenceHelper.checkStoredLength(objType2.toString(), OBJ_TYPE2_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "objType2"), java.lang.String.valueOf(java.lang.Math.min(OBJ_TYPE2_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "objType2", this.objType2, objType2));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String OBJ_TYPE3 = "objType3";
   static int OBJ_TYPE3_UPPER_LIMIT = -1;
   java.lang.String objType3;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getObjType3() {
      return objType3;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setObjType3(java.lang.String objType3) throws wt.util.WTPropertyVetoException {
      objType3Validate(objType3);
      this.objType3 = objType3;
   }
   void objType3Validate(java.lang.String objType3) throws wt.util.WTPropertyVetoException {
      if (OBJ_TYPE3_UPPER_LIMIT < 1) {
         try { OBJ_TYPE3_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("objType3").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { OBJ_TYPE3_UPPER_LIMIT = 200; }
      }
      if (objType3 != null && !wt.fc.PersistenceHelper.checkStoredLength(objType3.toString(), OBJ_TYPE3_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "objType3"), java.lang.String.valueOf(java.lang.Math.min(OBJ_TYPE3_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "objType3", this.objType3, objType3));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String OBJ_TYPE4 = "objType4";
   static int OBJ_TYPE4_UPPER_LIMIT = -1;
   java.lang.String objType4;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getObjType4() {
      return objType4;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setObjType4(java.lang.String objType4) throws wt.util.WTPropertyVetoException {
      objType4Validate(objType4);
      this.objType4 = objType4;
   }
   void objType4Validate(java.lang.String objType4) throws wt.util.WTPropertyVetoException {
      if (OBJ_TYPE4_UPPER_LIMIT < 1) {
         try { OBJ_TYPE4_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("objType4").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { OBJ_TYPE4_UPPER_LIMIT = 200; }
      }
      if (objType4 != null && !wt.fc.PersistenceHelper.checkStoredLength(objType4.toString(), OBJ_TYPE4_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "objType4"), java.lang.String.valueOf(java.lang.Math.min(OBJ_TYPE4_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "objType4", this.objType4, objType4));
   }

   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public static final java.lang.String OBJ_TYPE5 = "objType5";
   static int OBJ_TYPE5_UPPER_LIMIT = -1;
   java.lang.String objType5;
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public java.lang.String getObjType5() {
      return objType5;
   }
   /**
    * @see ext.hbt.reviewobjecttype.model.TypeItem
    */
   public void setObjType5(java.lang.String objType5) throws wt.util.WTPropertyVetoException {
      objType5Validate(objType5);
      this.objType5 = objType5;
   }
   void objType5Validate(java.lang.String objType5) throws wt.util.WTPropertyVetoException {
      if (OBJ_TYPE5_UPPER_LIMIT < 1) {
         try { OBJ_TYPE5_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("objType5").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { OBJ_TYPE5_UPPER_LIMIT = 200; }
      }
      if (objType5 != null && !wt.fc.PersistenceHelper.checkStoredLength(objType5.toString(), OBJ_TYPE5_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "objType5"), java.lang.String.valueOf(java.lang.Math.min(OBJ_TYPE5_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "objType5", this.objType5, objType5));
   }

   public java.lang.String getConceptualClassname() {
      return CLASSNAME;
   }

   public wt.introspection.ClassInfo getClassInfo() throws wt.introspection.WTIntrospectionException {
      return wt.introspection.WTIntrospector.getClassInfo(getConceptualClassname());
   }

   public java.lang.String getType() {
      try { return getClassInfo().getDisplayName(); }
      catch (wt.introspection.WTIntrospectionException wte) { return wt.util.WTStringUtilities.tail(getConceptualClassname(), '.'); }
   }

   public static final long EXTERNALIZATION_VERSION_UID = 5923786058342872678L;

   public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
      output.writeLong( EXTERNALIZATION_VERSION_UID );

      super.writeExternal( output );

      output.writeObject( fullTypeName );
      output.writeObject( itemType );
      output.writeObject( objType1 );
      output.writeObject( objType2 );
      output.writeObject( objType3 );
      output.writeObject( objType4 );
      output.writeObject( objType5 );
      output.writeObject( parentTypeItem );
      output.writeObject( typeName );
      output.writeObject( typecomment );
   }

   protected void super_writeExternal_TypeItem(java.io.ObjectOutput output) throws java.io.IOException {
      super.writeExternal(output);
   }

   public void readExternal(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      long readSerialVersionUID = input.readLong();
      readVersion( (ext.hbt.reviewobjecttype.model.TypeItem) this, input, readSerialVersionUID, false, false );
   }
   protected void super_readExternal_TypeItem(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      super.readExternal(input);
   }

   public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.writeExternal( output );

      output.setString( "fullTypeName", fullTypeName );
      output.setString( "itemType", itemType );
      output.setString( "objType1", objType1 );
      output.setString( "objType2", objType2 );
      output.setString( "objType3", objType3 );
      output.setString( "objType4", objType4 );
      output.setString( "objType5", objType5 );
      output.writeObject( "parentTypeItem", parentTypeItem, wt.fc.ObjectReference.class, true );
      output.setString( "typeName", typeName );
      output.setString( "typecomment", typecomment );
   }

   public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.readExternal( input );

      fullTypeName = input.getString( "fullTypeName" );
      itemType = input.getString( "itemType" );
      objType1 = input.getString( "objType1" );
      objType2 = input.getString( "objType2" );
      objType3 = input.getString( "objType3" );
      objType4 = input.getString( "objType4" );
      objType5 = input.getString( "objType5" );
      parentTypeItem = (wt.fc.ObjectReference) input.readObject( "parentTypeItem", parentTypeItem, wt.fc.ObjectReference.class, true );
      typeName = input.getString( "typeName" );
      typecomment = input.getString( "typecomment" );
   }

   boolean readVersion5923786058342872678L( java.io.ObjectInput input, long readSerialVersionUID, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      if ( !superDone )
         super.readExternal( input );

      fullTypeName = (java.lang.String) input.readObject();
      itemType = (java.lang.String) input.readObject();
      objType1 = (java.lang.String) input.readObject();
      objType2 = (java.lang.String) input.readObject();
      objType3 = (java.lang.String) input.readObject();
      objType4 = (java.lang.String) input.readObject();
      objType5 = (java.lang.String) input.readObject();
      parentTypeItem = (wt.fc.ObjectReference) input.readObject();
      typeName = (java.lang.String) input.readObject();
      typecomment = (java.lang.String) input.readObject();
      return true;
   }

   protected boolean readVersion( TypeItem thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      boolean success = true;

      if ( readSerialVersionUID == EXTERNALIZATION_VERSION_UID )
         return readVersion5923786058342872678L( input, readSerialVersionUID, superDone );
      else
         success = readOldVersion( input, readSerialVersionUID, passThrough, superDone );

      if (input instanceof wt.pds.PDSObjectInput)
         wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

      return success;
   }
   protected boolean super_readVersion_TypeItem( _TypeItem thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
   }

   boolean readOldVersion( java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID="+readSerialVersionUID+" local class externalizationVersionUID="+EXTERNALIZATION_VERSION_UID);
   }
}
