package ext.hbt.signature.model;

@SuppressWarnings({"cast", "deprecation", "unchecked"})
public abstract class _CoordinateItem extends wt.fc.WTObject implements java.io.Externalizable {
   static final long serialVersionUID = 1;

   static final java.lang.String RESOURCE = "ext.hbt.signature.model.modelResource";
   static final java.lang.String CLASSNAME = CoordinateItem.class.getName();

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String TARGET_TYPE_REF = "targetTypeRef";
   wt.fc.ObjectReference targetTypeRef;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public wt.fc.ObjectReference getTargetTypeRef() {
      return targetTypeRef;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setTargetTypeRef(wt.fc.ObjectReference targetTypeRef) throws wt.util.WTPropertyVetoException {
      targetTypeRefValidate(targetTypeRef);
      this.targetTypeRef = targetTypeRef;
   }
   void targetTypeRefValidate(wt.fc.ObjectReference targetTypeRef) throws wt.util.WTPropertyVetoException {
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ATTR_NAME = "attrName";
   static int ATTR_NAME_UPPER_LIMIT = -1;
   java.lang.String attrName;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getAttrName() {
      return attrName;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setAttrName(java.lang.String attrName) throws wt.util.WTPropertyVetoException {
      attrNameValidate(attrName);
      this.attrName = attrName;
   }
   void attrNameValidate(java.lang.String attrName) throws wt.util.WTPropertyVetoException {
      if (ATTR_NAME_UPPER_LIMIT < 1) {
         try { ATTR_NAME_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("attrName").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ATTR_NAME_UPPER_LIMIT = 200; }
      }
      if (attrName != null && !wt.fc.PersistenceHelper.checkStoredLength(attrName.toString(), ATTR_NAME_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "attrName"), java.lang.String.valueOf(java.lang.Math.min(ATTR_NAME_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "attrName", this.attrName, attrName));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ATTR_TYPE = "attrType";
   static int ATTR_TYPE_UPPER_LIMIT = -1;
   java.lang.String attrType;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getAttrType() {
      return attrType;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setAttrType(java.lang.String attrType) throws wt.util.WTPropertyVetoException {
      attrTypeValidate(attrType);
      this.attrType = attrType;
   }
   void attrTypeValidate(java.lang.String attrType) throws wt.util.WTPropertyVetoException {
      if (ATTR_TYPE_UPPER_LIMIT < 1) {
         try { ATTR_TYPE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("attrType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ATTR_TYPE_UPPER_LIMIT = 200; }
      }
      if (attrType != null && !wt.fc.PersistenceHelper.checkStoredLength(attrType.toString(), ATTR_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "attrType"), java.lang.String.valueOf(java.lang.Math.min(ATTR_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "attrType", this.attrType, attrType));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String X_LOCATION = "xLocation";
   static int X_LOCATION_UPPER_LIMIT = -1;
   java.lang.String xLocation;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getXLocation() {
      return xLocation;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setXLocation(java.lang.String xLocation) throws wt.util.WTPropertyVetoException {
      xLocationValidate(xLocation);
      this.xLocation = xLocation;
   }
   void xLocationValidate(java.lang.String xLocation) throws wt.util.WTPropertyVetoException {
      if (X_LOCATION_UPPER_LIMIT < 1) {
         try { X_LOCATION_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("xLocation").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { X_LOCATION_UPPER_LIMIT = 200; }
      }
      if (xLocation != null && !wt.fc.PersistenceHelper.checkStoredLength(xLocation.toString(), X_LOCATION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "xLocation"), java.lang.String.valueOf(java.lang.Math.min(X_LOCATION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "xLocation", this.xLocation, xLocation));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String Y_LOCATION = "yLocation";
   static int Y_LOCATION_UPPER_LIMIT = -1;
   java.lang.String yLocation;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getYLocation() {
      return yLocation;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setYLocation(java.lang.String yLocation) throws wt.util.WTPropertyVetoException {
      yLocationValidate(yLocation);
      this.yLocation = yLocation;
   }
   void yLocationValidate(java.lang.String yLocation) throws wt.util.WTPropertyVetoException {
      if (Y_LOCATION_UPPER_LIMIT < 1) {
         try { Y_LOCATION_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("yLocation").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { Y_LOCATION_UPPER_LIMIT = 200; }
      }
      if (yLocation != null && !wt.fc.PersistenceHelper.checkStoredLength(yLocation.toString(), Y_LOCATION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "yLocation"), java.lang.String.valueOf(java.lang.Math.min(Y_LOCATION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "yLocation", this.yLocation, yLocation));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String FONT_SIZE = "fontSize";
   static int FONT_SIZE_UPPER_LIMIT = -1;
   java.lang.String fontSize;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getFontSize() {
      return fontSize;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setFontSize(java.lang.String fontSize) throws wt.util.WTPropertyVetoException {
      fontSizeValidate(fontSize);
      this.fontSize = fontSize;
   }
   void fontSizeValidate(java.lang.String fontSize) throws wt.util.WTPropertyVetoException {
      if (FONT_SIZE_UPPER_LIMIT < 1) {
         try { FONT_SIZE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("fontSize").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { FONT_SIZE_UPPER_LIMIT = 200; }
      }
      if (fontSize != null && !wt.fc.PersistenceHelper.checkStoredLength(fontSize.toString(), FONT_SIZE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "fontSize"), java.lang.String.valueOf(java.lang.Math.min(FONT_SIZE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "fontSize", this.fontSize, fontSize));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String FONT_TYPE = "fontType";
   static int FONT_TYPE_UPPER_LIMIT = -1;
   java.lang.String fontType;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getFontType() {
      return fontType;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setFontType(java.lang.String fontType) throws wt.util.WTPropertyVetoException {
      fontTypeValidate(fontType);
      this.fontType = fontType;
   }
   void fontTypeValidate(java.lang.String fontType) throws wt.util.WTPropertyVetoException {
      if (FONT_TYPE_UPPER_LIMIT < 1) {
         try { FONT_TYPE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("fontType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { FONT_TYPE_UPPER_LIMIT = 200; }
      }
      if (fontType != null && !wt.fc.PersistenceHelper.checkStoredLength(fontType.toString(), FONT_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "fontType"), java.lang.String.valueOf(java.lang.Math.min(FONT_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "fontType", this.fontType, fontType));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String CONTENT_TYPE = "contentType";
   static int CONTENT_TYPE_UPPER_LIMIT = -1;
   java.lang.String contentType;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getContentType() {
      return contentType;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setContentType(java.lang.String contentType) throws wt.util.WTPropertyVetoException {
      contentTypeValidate(contentType);
      this.contentType = contentType;
   }
   void contentTypeValidate(java.lang.String contentType) throws wt.util.WTPropertyVetoException {
      if (CONTENT_TYPE_UPPER_LIMIT < 1) {
         try { CONTENT_TYPE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("contentType").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { CONTENT_TYPE_UPPER_LIMIT = 200; }
      }
      if (contentType != null && !wt.fc.PersistenceHelper.checkStoredLength(contentType.toString(), CONTENT_TYPE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "contentType"), java.lang.String.valueOf(java.lang.Math.min(CONTENT_TYPE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "contentType", this.contentType, contentType));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String SIGN_RANGE = "signRange";
   static int SIGN_RANGE_UPPER_LIMIT = -1;
   java.lang.String signRange;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getSignRange() {
      return signRange;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setSignRange(java.lang.String signRange) throws wt.util.WTPropertyVetoException {
      signRangeValidate(signRange);
      this.signRange = signRange;
   }
   void signRangeValidate(java.lang.String signRange) throws wt.util.WTPropertyVetoException {
      if (SIGN_RANGE_UPPER_LIMIT < 1) {
         try { SIGN_RANGE_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("signRange").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { SIGN_RANGE_UPPER_LIMIT = 200; }
      }
      if (signRange != null && !wt.fc.PersistenceHelper.checkStoredLength(signRange.toString(), SIGN_RANGE_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "signRange"), java.lang.String.valueOf(java.lang.Math.min(SIGN_RANGE_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "signRange", this.signRange, signRange));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ROTATION = "rotation";
   static int ROTATION_UPPER_LIMIT = -1;
   java.lang.String rotation;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getRotation() {
      return rotation;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setRotation(java.lang.String rotation) throws wt.util.WTPropertyVetoException {
      rotationValidate(rotation);
      this.rotation = rotation;
   }
   void rotationValidate(java.lang.String rotation) throws wt.util.WTPropertyVetoException {
      if (ROTATION_UPPER_LIMIT < 1) {
         try { ROTATION_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("rotation").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ROTATION_UPPER_LIMIT = 200; }
      }
      if (rotation != null && !wt.fc.PersistenceHelper.checkStoredLength(rotation.toString(), ROTATION_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "rotation"), java.lang.String.valueOf(java.lang.Math.min(ROTATION_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "rotation", this.rotation, rotation));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String IMAGE_WIDTH = "imageWidth";
   static int IMAGE_WIDTH_UPPER_LIMIT = -1;
   java.lang.String imageWidth;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getImageWidth() {
      return imageWidth;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setImageWidth(java.lang.String imageWidth) throws wt.util.WTPropertyVetoException {
      imageWidthValidate(imageWidth);
      this.imageWidth = imageWidth;
   }
   void imageWidthValidate(java.lang.String imageWidth) throws wt.util.WTPropertyVetoException {
      if (IMAGE_WIDTH_UPPER_LIMIT < 1) {
         try { IMAGE_WIDTH_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("imageWidth").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { IMAGE_WIDTH_UPPER_LIMIT = 200; }
      }
      if (imageWidth != null && !wt.fc.PersistenceHelper.checkStoredLength(imageWidth.toString(), IMAGE_WIDTH_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "imageWidth"), java.lang.String.valueOf(java.lang.Math.min(IMAGE_WIDTH_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "imageWidth", this.imageWidth, imageWidth));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String IMAGE_HEIGHT = "imageHeight";
   static int IMAGE_HEIGHT_UPPER_LIMIT = -1;
   java.lang.String imageHeight;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getImageHeight() {
      return imageHeight;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setImageHeight(java.lang.String imageHeight) throws wt.util.WTPropertyVetoException {
      imageHeightValidate(imageHeight);
      this.imageHeight = imageHeight;
   }
   void imageHeightValidate(java.lang.String imageHeight) throws wt.util.WTPropertyVetoException {
      if (IMAGE_HEIGHT_UPPER_LIMIT < 1) {
         try { IMAGE_HEIGHT_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("imageHeight").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { IMAGE_HEIGHT_UPPER_LIMIT = 200; }
      }
      if (imageHeight != null && !wt.fc.PersistenceHelper.checkStoredLength(imageHeight.toString(), IMAGE_HEIGHT_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "imageHeight"), java.lang.String.valueOf(java.lang.Math.min(IMAGE_HEIGHT_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "imageHeight", this.imageHeight, imageHeight));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ATTR1 = "attr1";
   static int ATTR1_UPPER_LIMIT = -1;
   java.lang.String attr1;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getAttr1() {
      return attr1;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setAttr1(java.lang.String attr1) throws wt.util.WTPropertyVetoException {
      attr1Validate(attr1);
      this.attr1 = attr1;
   }
   void attr1Validate(java.lang.String attr1) throws wt.util.WTPropertyVetoException {
      if (ATTR1_UPPER_LIMIT < 1) {
         try { ATTR1_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("attr1").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ATTR1_UPPER_LIMIT = 200; }
      }
      if (attr1 != null && !wt.fc.PersistenceHelper.checkStoredLength(attr1.toString(), ATTR1_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "attr1"), java.lang.String.valueOf(java.lang.Math.min(ATTR1_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "attr1", this.attr1, attr1));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ATTR2 = "attr2";
   static int ATTR2_UPPER_LIMIT = -1;
   java.lang.String attr2;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getAttr2() {
      return attr2;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setAttr2(java.lang.String attr2) throws wt.util.WTPropertyVetoException {
      attr2Validate(attr2);
      this.attr2 = attr2;
   }
   void attr2Validate(java.lang.String attr2) throws wt.util.WTPropertyVetoException {
      if (ATTR2_UPPER_LIMIT < 1) {
         try { ATTR2_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("attr2").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ATTR2_UPPER_LIMIT = 200; }
      }
      if (attr2 != null && !wt.fc.PersistenceHelper.checkStoredLength(attr2.toString(), ATTR2_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "attr2"), java.lang.String.valueOf(java.lang.Math.min(ATTR2_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "attr2", this.attr2, attr2));
   }

   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public static final java.lang.String ATTR3 = "attr3";
   static int ATTR3_UPPER_LIMIT = -1;
   java.lang.String attr3;
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public java.lang.String getAttr3() {
      return attr3;
   }
   /**
    * @see ext.hbt.signature.model.CoordinateItem
    */
   public void setAttr3(java.lang.String attr3) throws wt.util.WTPropertyVetoException {
      attr3Validate(attr3);
      this.attr3 = attr3;
   }
   void attr3Validate(java.lang.String attr3) throws wt.util.WTPropertyVetoException {
      if (ATTR3_UPPER_LIMIT < 1) {
         try { ATTR3_UPPER_LIMIT = (java.lang.Integer) wt.introspection.WTIntrospector.getClassInfo(CLASSNAME).getPropertyDescriptor("attr3").getValue(wt.introspection.WTIntrospector.UPPER_LIMIT); }
         catch (wt.introspection.WTIntrospectionException e) { ATTR3_UPPER_LIMIT = 200; }
      }
      if (attr3 != null && !wt.fc.PersistenceHelper.checkStoredLength(attr3.toString(), ATTR3_UPPER_LIMIT, true))
         throw new wt.util.WTPropertyVetoException("wt.introspection.introspectionResource", wt.introspection.introspectionResource.UPPER_LIMIT,
               new java.lang.Object[] { new wt.introspection.PropertyDisplayName(CLASSNAME, "attr3"), java.lang.String.valueOf(java.lang.Math.min(ATTR3_UPPER_LIMIT, wt.fc.PersistenceHelper.DB_MAX_SQL_STRING_SIZE/wt.fc.PersistenceHelper.DB_MAX_BYTES_PER_CHAR)) },
               new java.beans.PropertyChangeEvent(this, "attr3", this.attr3, attr3));
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

   public static final long EXTERNALIZATION_VERSION_UID = -2337842623316854626L;

   public void writeExternal(java.io.ObjectOutput output) throws java.io.IOException {
      output.writeLong( EXTERNALIZATION_VERSION_UID );

      super.writeExternal( output );

      output.writeObject( attr1 );
      output.writeObject( attr2 );
      output.writeObject( attr3 );
      output.writeObject( attrName );
      output.writeObject( attrType );
      output.writeObject( contentType );
      output.writeObject( fontSize );
      output.writeObject( fontType );
      output.writeObject( imageHeight );
      output.writeObject( imageWidth );
      output.writeObject( rotation );
      output.writeObject( signRange );
      output.writeObject( targetTypeRef );
      output.writeObject( xLocation );
      output.writeObject( yLocation );
   }

   protected void super_writeExternal_CoordinateItem(java.io.ObjectOutput output) throws java.io.IOException {
      super.writeExternal(output);
   }

   public void readExternal(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      long readSerialVersionUID = input.readLong();
      readVersion( (ext.hbt.signature.model.CoordinateItem) this, input, readSerialVersionUID, false, false );
   }
   protected void super_readExternal_CoordinateItem(java.io.ObjectInput input) throws java.io.IOException, java.lang.ClassNotFoundException {
      super.readExternal(input);
   }

   public void writeExternal(wt.pds.PersistentStoreIfc output) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.writeExternal( output );

      output.setString( "attr1", attr1 );
      output.setString( "attr2", attr2 );
      output.setString( "attr3", attr3 );
      output.setString( "attrName", attrName );
      output.setString( "attrType", attrType );
      output.setString( "contentType", contentType );
      output.setString( "fontSize", fontSize );
      output.setString( "fontType", fontType );
      output.setString( "imageHeight", imageHeight );
      output.setString( "imageWidth", imageWidth );
      output.setString( "rotation", rotation );
      output.setString( "signRange", signRange );
      output.writeObject( "targetTypeRef", targetTypeRef, wt.fc.ObjectReference.class, true );
      output.setString( "xLocation", xLocation );
      output.setString( "yLocation", yLocation );
   }

   public void readExternal(wt.pds.PersistentRetrieveIfc input) throws java.sql.SQLException, wt.pom.DatastoreException {
      super.readExternal( input );

      attr1 = input.getString( "attr1" );
      attr2 = input.getString( "attr2" );
      attr3 = input.getString( "attr3" );
      attrName = input.getString( "attrName" );
      attrType = input.getString( "attrType" );
      contentType = input.getString( "contentType" );
      fontSize = input.getString( "fontSize" );
      fontType = input.getString( "fontType" );
      imageHeight = input.getString( "imageHeight" );
      imageWidth = input.getString( "imageWidth" );
      rotation = input.getString( "rotation" );
      signRange = input.getString( "signRange" );
      targetTypeRef = (wt.fc.ObjectReference) input.readObject( "targetTypeRef", targetTypeRef, wt.fc.ObjectReference.class, true );
      xLocation = input.getString( "xLocation" );
      yLocation = input.getString( "yLocation" );
   }

   boolean readVersion_2337842623316854626L( java.io.ObjectInput input, long readSerialVersionUID, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      if ( !superDone )
         super.readExternal( input );

      attr1 = (java.lang.String) input.readObject();
      attr2 = (java.lang.String) input.readObject();
      attr3 = (java.lang.String) input.readObject();
      attrName = (java.lang.String) input.readObject();
      attrType = (java.lang.String) input.readObject();
      contentType = (java.lang.String) input.readObject();
      fontSize = (java.lang.String) input.readObject();
      fontType = (java.lang.String) input.readObject();
      imageHeight = (java.lang.String) input.readObject();
      imageWidth = (java.lang.String) input.readObject();
      rotation = (java.lang.String) input.readObject();
      signRange = (java.lang.String) input.readObject();
      targetTypeRef = (wt.fc.ObjectReference) input.readObject();
      xLocation = (java.lang.String) input.readObject();
      yLocation = (java.lang.String) input.readObject();
      return true;
   }

   protected boolean readVersion( CoordinateItem thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      boolean success = true;

      if ( readSerialVersionUID == EXTERNALIZATION_VERSION_UID )
         return readVersion_2337842623316854626L( input, readSerialVersionUID, superDone );
      else
         success = readOldVersion( input, readSerialVersionUID, passThrough, superDone );

      if (input instanceof wt.pds.PDSObjectInput)
         wt.fc.EvolvableHelper.requestRewriteOfEvolvedBlobbedObject();

      return success;
   }
   protected boolean super_readVersion_CoordinateItem( _CoordinateItem thisObject, java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      return super.readVersion(thisObject, input, readSerialVersionUID, passThrough, superDone);
   }

   boolean readOldVersion( java.io.ObjectInput input, long readSerialVersionUID, boolean passThrough, boolean superDone ) throws java.io.IOException, java.lang.ClassNotFoundException {
      throw new java.io.InvalidClassException(CLASSNAME, "Local class not compatible: stream classdesc externalizationVersionUID="+readSerialVersionUID+" local class externalizationVersionUID="+EXTERNALIZATION_VERSION_UID);
   }
}
