package ext.tasv.preference;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wt.fc.EnumeratedType;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectToObjectLink;
import wt.fc.PagingQueryResult;
import wt.fc.PagingSessionHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.folder.Folder;
import wt.iba.value.FloatValue;
import wt.iba.value.IntegerValue;
import wt.iba.value.ReferenceValue;
import wt.iba.value.StringValue;
import wt.iba.value.TimestampValue;
import wt.pds.StatementSpec;
import wt.query.ArrayExpression;
import wt.query.AttributeRange;
import wt.query.ClassAttribute;
import wt.query.ColumnExpression;
import wt.query.ConstantExpression;
import wt.query.OrderBy;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.RangeExpression;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.query.TableExpression;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.Mastered;
import wt.vc.OneOffVersioned;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.ManagedBaseline;
import wt.vc.config.BaselineConfigSpec;
import wt.vc.config.IteratedFolderedConfigSpec;
import wt.vc.wip.Workable;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;

public final class Select < T > implements Cloneable, Serializable
{
    private static final long    serialVersionUID   = 5746619941945455497L;
    private Class<T>             _class;
    protected QuerySpec            _spec;
    private Map<String, Integer> _ibaMap            = new HashMap<String, Integer>();
    protected long                 pagingSessionId;
    // [start]屬性
    // [start]Table Indexes
    public final static int      SELECT_TABLE_INDEX = 0;
    // [end]
    // [start]查詢
    public static final boolean  CASE_SENSITIVE     = true;
    public static final boolean  CASE_INSENSITIVE   = false;

    // [end]
    // [end]

    private static class SelectUtil
    {
        @SuppressWarnings("all")
        public static void appendAnd(QuerySpec spec)
        {
            String where = null;
            try
            {
                if (!spec.isAdvancedQuery())
                {
                    where = spec.getWhere();
                } else
                {
                    Serializable processor = new Serializable()
                    {
                        public String process(QuerySpec spec) throws Exception
                        {
                            String where = spec.getWhere();
                            return where;
                        }
                    };
                    where = (String) RemoteMethod.invokeProcessor(processor, spec);
                }
            } catch (QueryException e)
            {
                throw new RuntimeException("Can't get where clause", e);
            } catch (WTException e)
            {
                throw new RuntimeException("Can't get where clause", e);
            }
            if (!cannotAppendOperator(where))
            {
                spec.appendAnd();
            }
        }

        /**
         * 為RevisionControlled的物件（如WTPart，WTDocument）添加必須是每個版次的最新版序的條件。
         * 
         * 如：某WTPart有A.1,A.2,A.3,B.1,B.2五個版本，則使用本條件後，只返回A.3和B.2物件。
         * 
         * @return Select對象本身
         * @author 張旋
         */
        public static void onlyLatestIteration(QuerySpec spec, int tableIndex)
        {
            try
            {
                // 判斷要限定的表是否是具有版序
                if (Iterated.class.isAssignableFrom(spec.getClassAt(tableIndex)))
                {
                    SelectUtil.appendAnd(spec);
                    spec.appendWhere(new SearchCondition(new ClassAttribute(spec.getClassAt(tableIndex),
                                                                            "iterationInfo.latest"),
                                                         SearchCondition.IS_TRUE),
                                     new int[] { tableIndex });
                }
            } catch (QueryException e)
            {
                throw new RuntimeException("Can't append onlyLatestIteration condition", e);
            }
        }

        /**
         * 取得某個類某個指定字段的值
         * 
         * @param c 要取的字段值的類
         * @param fieldName 要取得值的字段名
         * @return 字段的值（String類型，因為本方法僅作內部用途）
         * @author 張旋
         */
        public static String getClassField(Class<?> c, String fieldName)
        {
            try
            {
                Field field = c.getField(fieldName);
                return (String) field.get(null);
            } catch (SecurityException e)
            {
                e.printStackTrace();
            } catch (NoSuchFieldException e)
            {
                e.printStackTrace();
            } catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 取得類的實例（無受查異常拋出）
         * 
         * @param className
         * @return
         * @author 張旋
         */
        @SuppressWarnings("unchecked")
        public static Class<Persistable> getClass(String className)
        {
            try
            {
                return (Class<Persistable>) Class.forName(className);
            } catch (ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("all")
        public static PagingQueryResult query(Select<?> select, int fromIndex, int fetchSize)
        {
            Serializable processor = new Serializable()
            {
                public PagingQueryResult process(int fromIndex, int fetchSize, Select<?> select) throws Exception
                {
                    PagingQueryResult result = null;
                    if (select.pagingSessionId == 0)
                    {
                        select._spec.setAdvancedQueryEnabled(true);
                        result = PagingSessionHelper.openPagingSession(fromIndex,
                                                                       fetchSize,
                                                                       select._spec);
                        select.pagingSessionId = result.getSessionId();
                    } else
                    {
                        result = PagingSessionHelper.fetchPagingSession(fromIndex,
                                                                        fetchSize,
                                                                        select.pagingSessionId);
                    }
                    return result;
                }
            };
            return (PagingQueryResult) RemoteMethod.invokeProcessor(processor,
                                                                    fromIndex,
                                                                    fetchSize,
                                                                    select);
        }

        /**
         * 核心查詢方法，通過QuerySpec查詢物件
         * 
         * @param spec 要查找的條件
         * @return 查找到的結果
         * @author 張旋
         */
        public static QueryResult query(final StatementSpec spec)
        {
            QueryResult result = null;
            try
            {
                if(spec.isAdvancedQuery())
                    result = SelectUtil.advancedQuery(spec);
                else
                    result=PersistenceHelper.manager.find(spec);
                return result;
            } catch (Exception e)
            {
                return new QueryResult();
            }
        }

        /**
         * 判斷是否可以繼續添加and、or等分隔符，自動添加and方法用。
         * 
         * @param where 條件字符串
         * @return true為不能繼續添加分隔符，false為可以繼續添加分隔符
         * @author 張旋
         */
        public static boolean cannotAppendOperator(String where)
        {
            if (where == null)
                return true;
            String[] ends = { "(", " OR ", " AND ", " NOT " };
            for (String end : ends)
            {
                if (where.endsWith(end))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * 添加查詢類（無受查異常拋出）
         * 
         * @param spec
         * @param objClass
         * @return
         * @author 張旋
         */
        public static int appendClass(QuerySpec spec, Class<? extends Persistable> objClass)
        {
            try
            {
                return spec.appendClassList(objClass, false);
            } catch (QueryException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * 將QueryResult轉換為List<T>
         * 
         * @param <T> 要返回的物件類型
         * @param result 要轉換的結果
         * @return 轉換後的結果
         * @author 張旋
         */
        @SuppressWarnings("unchecked")
        public static < T > List<T> toList(QueryResult result)
        {
            List<T> list = new ArrayList<T>();
            while (result.hasMoreElements())
            {
                Object obj = result.nextElement();
                if (obj instanceof Object[])
                {
                    obj = ((Object[]) obj)[0];
                }
                list.add((T) obj);
            }
            return list;
        }

        @SuppressWarnings("all")
        public static QueryResult advancedQuery(StatementSpec spec)
        {
            Serializable processor = new Serializable()
            {
                public QueryResult process(StatementSpec spec) throws Exception
                {
                    spec.setAdvancedQueryEnabled(true);
                    return PersistenceHelper.manager.find(spec);
                }
            };
            return (QueryResult) RemoteMethod.invokeProcessor(processor, spec);
        }
    }

    // [start]構造函數
    private Select(Class<T> objClass)
    {
        _class = objClass;
        try
        {
            _spec = new QuerySpec();
            _spec.appendClassList(_class, true);
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Select(QuerySpec spec)
    {
        if (spec == null)
            throw new RuntimeException("QuerySpec can't be null");
        _spec = spec;
        _class = _spec.getPrimaryClass();
    }

    // [end]
    // [start]表連接函數
    // [start]添加新表
    /**
     * 添加連接表
     * 
     * @param objClass 要連接的對象的類，對應的tableIndex（物件表索引）為上一次添加from時的index加1
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> andFrom(Class<? extends Persistable> objClass)
    {
        SelectUtil.appendClass(_spec, objClass);
        return this;
    }

    // [end]
    // [start]Reference Join
    /**
     * 連接兩張表
     * 
     * 如：查詢QBook產品下的所有編號以CD開頭的零件
     * 
     * Select.from(WTPart.class).andFrom(PDMLinkProduct.class)
     *           .join(0,WTPart.CONTAINER_REFERENCE,1)
     *           .where(0,WTPart.NUMBER,SearchCondition.LIKE,"CD%")
     *           .where(1,PDMLinkProduct.NAME,SearchCondition.EQUAL,"QBook")
     *           .list();
     * 
     * @param tableIndex1 要連接的表1索引
     * @param referenceField 表1的外鍵引用字段
     * @param tableIndex2 要連接的表2索引
     * @return
     * @author 張旋
     */
    public Select<T> join(int tableIndex1, String referenceField, int tableIndex2)
    {
        openParen();
        where(tableIndex1,
              referenceField + ".key.id",
              SearchCondition.EQUAL,
              tableIndex2,
              "thePersistInfo.theObjectIdentifier.id");
        and();
        where(tableIndex1,
              referenceField + ".key.classname",
              SearchCondition.EQUAL,
              tableIndex2,
              "thePersistInfo.theObjectIdentifier.classname");
        closeParen();
        return this;
    }
    
    public Select<T> joinByLink(int roleATableIndex,int linkTableIndex,int roleBTableIndex)
    {
        join(linkTableIndex,ObjectToObjectLink.ROLE_AOBJECT_REF,roleATableIndex);
        join(linkTableIndex, ObjectToObjectLink.ROLE_BOBJECT_REF, roleBTableIndex);
        return this;
    }

    // [end]
    // [start]通用 Join
    /**
     * 連接兩張表
     * 
     * 查找QBook產品下的零件，這些零件的創建者必須和QBook產品的創建者是同一人
     * 
     * 同：where(int tableIndx1,String field1,String op,int tableIndex2,String field2)方法
     * 
     * 如：List<WTPart> list = Select.from(WTPart.class).andFrom(PDMLinkProduct.class)
     *       .join(0, WTPart.CONTAINER_REFERENCE, 1)
     *       .join(0, "iterationInfo.creator.key.id", "=",1,"containerInfo.creatorRef.key.id") //具體匹配字段名請使用inforeport -x wt.part.WTPart這樣的命令在WT_HOME下的temp目錄中生成報告，並在報告中查找
     *       .where(1, PDMLinkProduct.NAME,"=","QBook")
     *       .list();
     * 
     * @param tableIndex1
     * @param field1
     * @param op
     * @param tableIndex2
     * @param field2
     * @return
     * @author 張旋
     */
    public Select<T> join(int tableIndex1, String field1, String op, int tableIndex2, String field2)
    {
        return where(tableIndex1, field1, op, tableIndex2, field2);
    }
    
    /**
     * 連接兩個以OID字符串關聯的表
     * 
     * 注：oid規則須為OR的OID，但不能包含"OR:"字符串。即，必須為wt.part.WTPart:19934這種格式
     * 
     * 取值方法為part.getPersistInfo().getObjectIdentifier().toString()
     * 或PersistenceHelper.getObjectIdentifier(part).toString()
     * 
     * @param oidForeignKeyTableIndex　具有保存著對方物件oid字符串字段的物件表索引　
     * @param oidField1 保存著對方物件oid字符串的字段名
     * @param oidPrimaryKeyTableIndex　要被連接的、以連接oid作為主鍵的物件表索引
     * @return
     * @author 張旋
     */
    public Select<T> joinByOidField(int oidForeignKeyTableIndex,String oidField1,int oidPrimaryKeyTableIndex)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(
                new SearchCondition(
                    new ClassAttribute(_spec.getClassAt(oidForeignKeyTableIndex),oidField1),
                    "=",
                    getOidColumn(oidPrimaryKeyTableIndex)
                ),
                new int[] { oidForeignKeyTableIndex,oidPrimaryKeyTableIndex,oidPrimaryKeyTableIndex }
            );
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 取得某個表的oid字段
     * 
     * @param tableIndex
     * @return
     * @throws QueryException
     * @author 張旋
     */
    private SQLFunction getOidColumn(int tableIndex) throws QueryException
    {
        return SQLFunction.newSQLFunction(
            SQLFunction.CONCAT,
            new ColumnExpression[]{
                new ClassAttribute(_spec.getClassAt(tableIndex),"thePersistInfo.theObjectIdentifier.classname"),
                ConstantExpression.newExpression(":"),
                new ClassAttribute(_spec.getClassAt(tableIndex),"thePersistInfo.theObjectIdentifier.id")
            }
        );
    }
    
    

    // [end]
    // [start]IBA Join
    private int joinIBA(int tableIndex, String type, String fieldName)
    {
        final String valPackage = "wt.iba.value";
        final String defPackage = "wt.iba.definition";
        String cacheKey = type + "_" + fieldName;
        Integer cachedIndex = _ibaMap.get(cacheKey);
        Class<Persistable> valClass = SelectUtil.getClass(valPackage + "." + type + "Value");
        Class<Persistable> defClass = SelectUtil.getClass(defPackage + "." + type + "Definition");
        if (cachedIndex == null)
        {
            int valIndex = SelectUtil.appendClass(_spec, valClass);
            int defIndex = SelectUtil.appendClass(_spec, defClass);
            join(valIndex, SelectUtil.getClassField(valClass, "DEFINITION_REFERENCE"), defIndex);
            join(valIndex, SelectUtil.getClassField(valClass, "IBAHOLDER_REFERENCE"), tableIndex);
            where(defIndex, SelectUtil.getClassField(defClass, "NAME"), SearchCondition.EQUAL, fieldName);
            _ibaMap.put(cacheKey, valIndex);
            return valIndex;
        } else
        {
            return cachedIndex;
        }
    }

    private int joinStringIBA(int tableIndex, String fieldName)
    {
        return joinIBA(tableIndex, "String", fieldName);
    }

    private int joinFloatIBA(int tableIndex, String fieldName)
    {
        return joinIBA(tableIndex, "Float", fieldName);
    }

    private int joinIntegerIBA(int tableIndex, String fieldName)
    {
        return joinIBA(tableIndex, "Integer", fieldName);
    }

    private int joinTimestampIBA(int tableIndex, String fieldName)
    {
        return joinIBA(tableIndex, "Timestamp", fieldName);
    }

    private int joinReferenceIBA(int tableIndex, String fieldName)
    {
        return joinIBA(tableIndex, "Reference", fieldName);
    }

    // [end]
    // [end]
    // [start]where方法
    // [start] Java值類型及封裝類的搜索支持
    
    /**
     * 簡易where條件，添加某表物件的Name屬性為某指定值的條件
     * 
     * @param tableIndex
     * @param name
     * @return
     * @author 張旋
     */
    public Select<T> whereNameIs(int tableIndex,String name)
    {
        appendAnd();
        try
        {
            return where(tableIndex,SelectUtil.getClassField(_spec.getClassAt(tableIndex), "NAME"),"=",name);
        } catch (QueryException e)
        {
            throw new RuntimeException("Failed to append whereNameIs condition by tableIndex:"+tableIndex+",name:"+name,e);
        }
    }
    
    /**
     * 簡易where條件，添加某表物件的Number屬性為某指定值的條件
     * 
     * @param tableIndex
     * @param number
     * @return
     * @author 張旋
     */
    public Select<T> whereNumberIs(int tableIndex,String number)
    {
        appendAnd();
        try
        {
            return where(tableIndex,SelectUtil.getClassField(_spec.getClassAt(tableIndex), "NUMBER"),"=",number);
        } catch (QueryException e)
        {
            throw new RuntimeException("Failed to append whereNumberIs condition by tableIndex:"+tableIndex+",number:"+number,e);
        }
    }
    
    /**
     * 簡易where條件，添加某表物件的Oid屬性為某指定值的條件
     * 
     * @param tableIndex
     * @param oid
     * @return
     * @author 張旋
     */
    public Select<T> whereOidIs(int tableIndex,String oid)
    {
        appendAnd();
        try
        {
            return where(new int[]{tableIndex,tableIndex},new SearchCondition(getOidColumn(tableIndex),"=",new ConstantExpression((Object)oid)));
        } catch (QueryException e)
        {
            throw new RuntimeException("Failed to append whereOidIs condition by tableIndex:"+tableIndex+",oid:"+oid,e);
        }
    }
    
    
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param bigDecimal 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, BigDecimal bigDecimal)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  bigDecimal), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param byteValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, byte byteValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  byteValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param byteValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Byte byteValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  byteValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param charValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, char charValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  charValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param characterValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Character characterValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  characterValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param doubleValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, double doubleValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  doubleValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param doubleValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Double doubleValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  doubleValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param floatValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, float floatValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  (double) floatValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param floatValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Float floatValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  floatValue.doubleValue()),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param integerValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, int intValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex), field, op, intValue),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param integerValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Integer integerValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  integerValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param longValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, long longValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  longValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param longValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Long longValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  longValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param shortValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, short shortValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  shortValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param shortValue 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Short shortValue)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  shortValue), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param date 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, java.util.Date date)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  new java.sql.Date(date.getTime())),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param timeStamp 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, java.sql.Date date)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex), field, op, date),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param timeStamp 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Timestamp timeStamp)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  timeStamp), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start]EnumeratedType搜索
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param enumeratedType 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, EnumeratedType enumeratedType)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  enumeratedType), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start]ObjectIdentifier搜索
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param objectIdentifier 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex,
                           String field,
                           String op,
                           ObjectIdentifier objectIdentifier)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  objectIdentifier), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表為某個確定的實例的搜索條件
     * 
     * @param tableIndex 表索引
     * @param persistable 要判斷的物件
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereis(int tableIndex, Persistable persistable)
    {
        return where(tableIndex,
                     "thePersistInfo.theObjectIdentifier",
                     SearchCondition.EQUAL,
                     persistable.getPersistInfo().getObjectIdentifier());
    }

    /**
     * 添加某個表的某個外鍵引用為某個確定的實例的搜索條件
     * 
     * @param tableIndex 表索引
     * @param referenceKey 表的外鍵引用
     * @param persistable 要判斷的物件
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereis(int tableIndex, String referenceKey, Persistable referenceObject)
    {
        return where(tableIndex,
                     referenceKey + ".key",
                     SearchCondition.EQUAL,
                     referenceObject.getPersistInfo().getObjectIdentifier());
    }

    // [end]
    // [start]字符串搜索
    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param value 要限制的值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, String value)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex), field, op, value),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 添加某個表對應物件的屬性，與某個值比較結果的限制條件
     * 
     * @param tableIndex 要限制的表索引
     * @param field 要限制的字段
     * @param op 判斷符號，如SearchCondition.EQUAL,SearchCondition.GREATER_THAN
     * @param value 要限制的值
     * @param caseSensitive true為大小寫敏感，false為大小寫不敏感
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex,
                           String field,
                           String op,
                           String value,
                           boolean caseSensitive)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                  field,
                                                  op,
                                                  value,
                                                  caseSensitive), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start] In列表搜索
    /**
     * 對tableIndex對應的表（物件類型）中，field屬性，進行操作符為op，值在valueArray中的搜索條件限制(In)
     * 
     * @param tableIndex
     * @param field
     * @param op 一般設為SearchCondition.IN
     * @param valueArray 要搜索的值列表，如:new String[]{"PM0001","PM0002"}或new
     *            Integer[]{new Integer(1),new Integer(5)}
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, Object[] valueArray)
    {
        return where(tableIndex, field, op, new ArrayExpression(valueArray));
    }

    /**
     * 對tableIndex對應的表（物件類型）中，field屬性，進行操作符為op，字符串值在valueArray中的搜索條件限制(In)
     * 
     * @param tableIndex
     * @param field
     * @param op 一般設為SearchCondition.IN
     * @param valueArray 要搜索的值列表，如:new String[]{"PM0001","pm0002"}
     * @param caseSensitive 是否大小寫敏感
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex,
                           String field,
                           String op,
                           String[] valueArray,
                           boolean caseSensitive)
    {
        try
        {
            appendAnd();
            if (SearchCondition.IN.equals(op))
                _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                      field,
                                                      valueArray,
                                                      caseSensitive), new int[] { tableIndex });
            else if (SearchCondition.NOT_IN.equals(op))
                _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),
                                                      field,
                                                      valueArray,
                                                      caseSensitive,
                                                      false), new int[] { tableIndex });
            else
                return where(tableIndex, field, op, valueArray);
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 對tableIndex對應的表（物件類型）中，field屬性，進行操作符為op，值在valueArray中的搜索條件限制(In)
     * 
     * @param tableIndex
     * @param field
     * @param op 一般設為SearchCondition.IN
     * @param valueArray 要搜索的值列表，如:new String[]{"PM0001","PM0002"}或new
     *            Integer[]{new Integer(1),new Integer(5)}
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, long[] valueArray)
    {
        return where(tableIndex, field, op, new ArrayExpression(valueArray));
    }

    /**
     * 對tableIndex對應的表（物件類型）中，field屬性，進行操作符為op，值在arrayExpression中的搜索條件限制(In)
     * 
     * @param tableIndex
     * @param field
     * @param op 一般設為SearchCondition.IN
     * @param valueArray 要搜索的值列表，如:new String[]{"PM0001","PM0002"}或new
     *            Integer[]{new Integer(1),new Integer(5)}
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, ArrayExpression arrayExpression)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(new ClassAttribute(_spec.getClassAt(tableIndex),
                                                                     field), op, arrayExpression),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start] Between搜索
    /**
     * 對tableIndex對應的表（物件類型）中，field屬性，進行操作符為op，值在range之間的搜索條件限制(Between)
     * 
     * @param tableIndex
     * @param field
     * @param op 一般為SearchCondition.BETWEEN
     * @param range
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String op, AttributeRange range)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(new ClassAttribute(_spec.getClassAt(tableIndex),
                                                                     field),
                                                  op,
                                                  new RangeExpression(range)),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start] Boolean/Is Null搜索
    /**
     * 判斷tableIndex的表中field字段是否為true或false，或是否為空
     * 
     * @param tableIndex 要判斷的表索引
     * @param field 字段名
     * @param isWhat 可以傳入"TRUE","FALSE","IS NULL","IS NOT NULL"四個值
     *            分別對應SearchCondtion
     *            .IS_TRUE,SearchCondition.IS_FALSE,SearchCondition
     *            .IS_NULL,SearchCondition.NOT_NULL
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex, String field, String isWhat)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex), field, isWhat),
                              new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start] SoftType
    /**
     * 按客制類型搜索
     * 
     * @param tableIndex
     * @param softType
     * @return
     * @author 張旋
     */
    public Select<T> whereSoftType(int tableIndex, String softType)
    {
        List<WTTypeDefinition> list = Select.from(WTTypeDefinition.class)
                                            .andFrom(WTTypeDefinitionMaster.class)
                                            .join(0, WTTypeDefinition.MASTER_REFERENCE, 1)
                                            .where(1,
                                                   WTTypeDefinitionMaster.INT_HID,
                                                   SearchCondition.EQUAL,
                                                   softType)
                                            .list();
        if (list.size() > 0)
        {
            openParen();
            boolean isFirst = true;
            for (WTTypeDefinition typedef : list)
            {
                if (!isFirst)
                    or();
                isFirst = false;
                whereis(tableIndex, "typeDefinitionReference", typedef);
            }
            closeParen();
            return this;
        } else
        {
            throw new RuntimeException("Can't find WTTypeDefnitionMaster by softtype '" + softType
                                       + "'");
        }
    }

    // [end]
    // [start] 單表通用搜索
    /**
     * 對tableIndex對應的表（物件類型），進行searchCondition的搜索條件限制
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param searchCondition
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> where(int tableIndex, SearchCondition searchCondition)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(searchCondition, new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start] 雙表關聯搜索
    /**
     * 連接兩張表
     * 
     * 查找QBook產品下的零件，這些零件的創建者必須和QBook產品的創建者是同一人
     * 
     * 同：where(int tableIndx1,String field1,String op,int tableIndex2,String field2)方法
     * 
     * 如：List<WTPart> list = Select.from(WTPart.class).andFrom(PDMLinkProduct.class)
     *       .join(0, WTPart.CONTAINER_REFERENCE, 1)
     *       .join(0, "iterationInfo.creator.key.id", "=",1,"containerInfo.creatorRef.key.id") //具體匹配字段名請使用inforeport -x wt.part.WTPart這樣的命令在WT_HOME下的temp目錄中生成報告，並在報告中查找
     *       .where(1, PDMLinkProduct.NAME,"=","QBook")
     *       .list();
     * 
     * @param tableIndex1
     * @param field1
     * @param op
     * @param tableIndex2
     * @param field2
     * @return
     * @author 張旋
     */
    public Select<T> where(int tableIndex1, String field1, String op, int tableIndex2, String field2)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(new ClassAttribute(_spec.getClassAt(tableIndex1),
                                                                     field1),
                                                  op,
                                                  new ClassAttribute(_spec.getClassAt(tableIndex2),
                                                                     field2)), new int[] {
                    tableIndex1, tableIndex2 });
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    public Select<T> where(int[] tableIndexes, SearchCondition condition)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(condition, tableIndexes);
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 對tableIndex對應的表（物件類型），進行field為null的條件限制
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field 要限制為null的字段
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIsNull(int tableIndex,String field)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),field,true), new int[] {
                    tableIndex});
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }
    
    /**
     * 對tableIndex對應的表（物件類型），進行field不為null的條件限制
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field 要限制不為null的字段
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIsNotNull(int tableIndex,String field)
    {
        try
        {
            appendAnd();
            _spec.appendWhere(new SearchCondition(_spec.getClassAt(tableIndex),field,false), new int[] {
                    tableIndex});
        } catch (QueryException e)
        {
            throw new RuntimeException(e);
        }
        return this;
    }

    // [end]
    // [end]
    // [start]邏輯運算符部份
    /**
     * 添加“非”的條件
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> not()
    {
        _spec.appendNot();
        return this;
    }

    /**
     * 添加“和”的條件
     * 
     * 注意：使用where系列語句時，會自動添加and()，所以可以省略
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> and()
    {
        _spec.appendAnd();
        return this;
    }

    /**
     * 添加“或”的條件
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> or()
    {
        _spec.appendOr();
        return this;
    }

    /**
     * 為where子句添加左括號
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> openParen()
    {
        appendAnd();
        _spec.appendOpenParen();
        return this;
    }

    /**
     * 為where子句添加右括號
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> closeParen()
    {
        _spec.appendCloseParen();
        return this;
    }

    // [end]
    // [start]IBA屬性條件部份
    /**
     * 添加按軟屬性查詢的查詢條件
     * 
     * @param tableIndex 要查詢的條件
     * @param field
     * @param op
     * @param value
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex, String field, String op, String value)
    {
        return whereIBA(tableIndex, field, op, value, true);
    }

    public Select<T> whereIBA(int tableIndex,
                              String field,
                              String op,
                              String value,
                              boolean caseSensitive)
    {
        openParen();
        where(joinStringIBA(tableIndex, field), StringValue.VALUE2, op, value, caseSensitive);
        closeParen();
        return this;
    }

    public Select<T> whereIBA(int tableIndex, String field, String op, Float floatValue)
    {
        return whereIBA(tableIndex, field, op, floatValue.floatValue());
    }

    public Select<T> whereIBA(int tableIndex, String field, String op, float floatValue)
    {
        openParen();
        where(joinFloatIBA(tableIndex, field), FloatValue.VALUE, op, floatValue);
        closeParen();
        return this;
    }

    public Select<T> whereIBA(int tableIndex, String field, String op, Integer integerValue)
    {
        return whereIBA(tableIndex, field, op, integerValue.intValue());
    }

    public Select<T> whereIBA(int tableIndex, String field, String op, int intValue)
    {
        openParen();
        where(joinIntegerIBA(tableIndex, field), IntegerValue.VALUE, op, intValue);
        closeParen();
        return this;
    }

    /**
     * 添加某個表日期IBA屬性與某個時間進行比較的條件
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field IBA屬性名
     * @param op 操作符，可為SearchCodition.EQUAL，SearchCondition.GREATER_THAN等等
     * @param timestampValue 要限制的時間值
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex, String field, String op, Timestamp timestampValue)
    {
        openParen();
        where(joinTimestampIBA(tableIndex, field), TimestampValue.VALUE, op, timestampValue);
        closeParen();
        return this;
    }

    /**
     * 添加某個表IBA屬性的值為物個物件的條件
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field IBA屬性名
     * @param op 操作符，一般為SearchCondition.EQUAL
     * @param objectIdentifier 要限制的物件OID
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex,
                              String field,
                              String op,
                              ObjectIdentifier objectIdentifier)
    {
        openParen();
        where(joinReferenceIBA(tableIndex, field), ReferenceValue.IBAREFERENCEABLE_REFERENCE
                                                   + ".key", op, objectIdentifier);
        closeParen();
        return this;
    }

    /**
     * 添加某個表IBA屬性的值在一個範圍中的條件
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field IBA屬性名
     * @param op 操作符，一般為SearchCondition.BETWEEN
     * @param range 要限制的範圍，如：new AttributeRange(1.2,3.0)
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex, String field, String op, AttributeRange range)
    {
        openParen();
        Object start = range.getStart();
        Object end = range.getEnd();
        if (start == null || end == null)
            throw new RuntimeException("Bad Attribute range:" + (start == null ? "Start" : "End")
                                       + " can't be null!");
        if (start instanceof Integer)
        {
            where(joinIntegerIBA(tableIndex, field), IntegerValue.VALUE, op, range);
        } else if (start instanceof Float)
        {
            where(joinFloatIBA(tableIndex, field), FloatValue.VALUE, op, range);
        } else if (start instanceof Long)
        {
            where(joinIntegerIBA(tableIndex, field), IntegerValue.VALUE, op, range);
        } else if (start instanceof String)
        {
            where(joinStringIBA(tableIndex, field), StringValue.VALUE2, op, range);
        } else if (start instanceof Timestamp)
        {
            where(joinTimestampIBA(tableIndex, field), TimestampValue.VALUE, op, range);
        } else
            throw new RuntimeException("Range Type not supported!");
        closeParen();
        return this;
    }

    /**
     * 添加某個表IBA屬性的值在一個列表中的條件
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field IBA屬性名
     * @param op 操作符，一般為SearchCondition.IN
     * @param valueList 要限制的值列表
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex, String field, String op, Object[] valueList)
    {
        openParen();
        Object element = valueList[0];
        if (element instanceof Integer)
        {
            where(joinIntegerIBA(tableIndex, field), IntegerValue.VALUE, op, valueList);
        } else if (element instanceof Float)
        {
            where(joinFloatIBA(tableIndex, field), FloatValue.VALUE, op, valueList);
        } else if (element instanceof Long)
        {
            where(joinIntegerIBA(tableIndex, field), IntegerValue.VALUE, op, valueList);
        } else if (element instanceof String)
        {
            where(joinStringIBA(tableIndex, field), StringValue.VALUE2, op, valueList);
        } else if (element instanceof Timestamp)
        {
            where(joinTimestampIBA(tableIndex, field), TimestampValue.VALUE, op, valueList);
        } else
            throw new RuntimeException("Value Type not supported!");
        closeParen();
        return this;
    }

    /**
     * 添加某個表字符串類型IBA屬性的值在一個列表中的條件，可設置不區分大小寫
     * 
     * @param tableIndex 要限制條件的物件表索引
     * @param field IBA屬性名
     * @param op 操作符，一般為SearchCondition.IN
     * @param valueList 要判斷的值列表
     * @param caseSensitive 是否區分大小寫
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> whereIBA(int tableIndex,
                              String field,
                              String op,
                              String[] valueList,
                              boolean caseSensitive)
    {
        openParen();
        where(joinStringIBA(tableIndex, field), StringValue.VALUE2, op, valueList, caseSensitive);
        closeParen();
        return this;
    }

    /**
     * 返回滿足條件記錄的條數，但不返回結果
     * 
     * 
     * @return 記錄條數
     * @author 張旋
     */
    @SuppressWarnings("all")
    public int size()
    {
        try
        {
            if (this.pagingSessionId == 0)
            {
                Serializable o =new Serializable()
                {
                    public PagingQueryResult process(int from,int fetchSize,StatementSpec spec) throws WTException
                    {
                        spec.setAdvancedQueryEnabled(true);
                        return PagingSessionHelper.openPagingSession(from, fetchSize, spec);
                    }
                };
                PagingQueryResult result = (PagingQueryResult) RemoteMethod.invokeProcessor(o, 1,1,spec());
                this.pagingSessionId=result.getSessionId();
                int size= result.getTotalSize();
                closePagingSession();
                return size;
                
            } else
            {
                return PagingSessionHelper.getTotalCount(this.pagingSessionId);
            }
        } catch (WTException e)
        {
            throw new WTRuntimeException(e);
        }
    }
    
    /**
     * 取得分頁後的總頁數
     * 
     * @param pagesize 每頁大小
     * @return　按pagesize分頁後的總頁數
     * @author 張旋
     */
    public int pages(int pagesize)
    {
        return (int) Math.ceil(1.0*size()/pagesize);
    }
    
    // [end]
    // [start]返回結果類方法（不再返回Select對象）
    /**
     * 返回第一個滿足條件的物件
     * 
     * @return
     * @author 張旋
     */
    public T first()
    {
        List<T> list = list(0,1);
        closePagingSession();
        return list.size() == 0 ? null : list.get(0);
    }

    /**
     * 返回所有滿足條件的物件
     * 
     * @return　所有滿足條件的物件列表
     * @author 張旋
     */
    public List<T> list()
    {
        List<T> list = SelectUtil.<T> toList(query());
        return list;
    }
    
    /**
     * 從第fromIndex顆零件開始查找，查找fetchSize顆零件
     * 
     * 注意：使用本方法返回集合，並不再需要本Select對象的結果之後，必須使用本對象的closePagingSession()方法關閉分頁會話
     * 
     * @param fromIndex
     * @param fetchSize
     * @return 結果List
     * @author 張旋
     */
    public List<T> list(int fromIndex, int fetchSize)
    {
        return SelectUtil.toList(SelectUtil.query(this,fromIndex,fetchSize));
    }
    
    /**
     * 從第fromIndex顆零件開始查找，查找fetchSize顆零件
     * 
     * 注意：使用本方法返回集合，並不再需要本Select對象的結果之後，必須使用本對象的closePagingSession()方法關閉分頁會話
     * 
     * @param fromIndex
     * @param fetchSize
     * @return 結果QueryResult
     * @author 張旋
     */
    public QueryResult query(int fromIndex, int fetchSize)
    {
        return SelectUtil.query(this,fromIndex,fetchSize);
    }
    
    /**
     * 取得每頁的數據
     * 
     * 注意：使用本方法返回集合，並不再需要本Select對象的結果之後，必須使用本對象的closePagingSession()方法關閉分頁會話
     * 
     * @param pagesize 每頁大小
     * @param pageIndex 從0開始，0表示第1頁。
     * @return 結果List
     * @author 張旋
     */
    public List<T> listpage(int pagesize,int pageIndex)
    {
        return list(pageIndex*pagesize,pagesize);
    }
    
    /**
     * 取得每頁的數據
     * 
     * 注意：使用本方法返回集合，並不再需要本Select對象的結果之後，必須使用本對象的closePagingSession()方法關閉分頁會話
     * 
     * @param pagesize 每頁大小
     * @param pageIndex 從0開始，0表示第1頁。
     * @return 結果QueryResult
     * @author 張旋
     */
    public QueryResult querypage(int pagesize,int pageIndex)
    {
        return querypage(pageIndex*pagesize,pagesize);
    }

    /**
     * 關閉分頁會話。
     * 
     * 分頁會話是為了加速分頁數據讀取用的。使用完畢後需調用本方法關閉。
     * 
     * 使用哪些方法後，會打開分頁會話呢？列表如下：
     * 
     * size()方法
     * list(int fromIndex,int fetchSize)方法
     * listpage(int pagesize,int pageIndex)方法
     * pages(int pagesize)方法
     * 
     * 使用上述方法，並確定不再調用以上方法後，需要調用本方法關閉分頁會話。
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> closePagingSession()
    {
        try
        {
            PagingSessionHelper.closePagingSession(this.pagingSessionId);
        } catch (WTException e)
        {
            throw new WTRuntimeException(e);
        } finally
        {
            // 如發生異常，則下次取分頁信息時使用新的PagingSession
            this.pagingSessionId = 0;
        }
        return this;
    }

    /**
     * 返回構造好條件搜索回的QueryResult，不做轉換為java List的處理。
     * 
     * 無特殊需要的話，不建議使用本方法。建議使用list()方法替換。
     * 
     * @return
     * @author 張旋
     */
    public QueryResult query()
    {
        return SelectUtil.query(_spec);
    }

    /**
     * 返回構造好條件對應的QuerySpec()
     * 
     * @return
     * @author 張旋
     */
    public QuerySpec spec()
    {
        return _spec;
    }

    // [end]
    // [start]only系列方法（ConfigSpec、過濾器）
    /**
     * 為要搜索的物件添加必須是最新版本的搜索條件
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> onlyLatest()
    {
        onlyLatest(0);
        return this;
    }

    /**
     * 為from子句指定索引位置的物件添加必須是最新版本的搜索條件
     * 
     * 規則(同Windchill物件詳細信息頁面中“轉到最新版”頁面版本)：
     * 1.只保留最新大版本
     * 2.只保留最新小版本
     * 3.去除所有OneOffVersion（單次使用版本）
     * 
     * 如：某WTPart有A.1,A.2,A.3,B.1,B.2,B-1.1(OneOffVersion),B-1.2(OneOffVersion)幾個版本，則使用本條件後，只返回B.2物件。
     * 
     * @param tableIndex
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> onlyLatest(int tableIndex)
    {
        try
        {
            onlyLatestBranch(tableIndex);
            onlyLatestIteration(tableIndex);
            onlyNotOneOffVersion(tableIndex);
            onlyNotWorkingCopy(tableIndex);
            
        } catch (Exception e)
        {
            throw new WTRuntimeException(e);
        }
        return this;
    }
    
    public Select<T> onlyOneOffVersion(int tableIndex)
    {
        if(testClass(tableIndex, OneOffVersioned.class))
        {
            whereIsNotNull(tableIndex, "oneOffVersionInfo.identifier.oneOffVersionId");
        }
        return this;
    }
    
    private boolean testClass(int tableIndex,Class<?> superClass)
    {
        try
        {
            return superClass.isAssignableFrom(_spec.getClassAt(tableIndex));
        } catch (QueryException e)
        {
            return false;
        }
    }
    
    public Select<T> onlyNotOneOffVersion(int tableIndex)
    {
        if(testClass(tableIndex, OneOffVersioned.class))
        {
            whereIsNull(tableIndex, "oneOffVersionInfo.identifier.oneOffVersionId");
        }
        return this;
    }
    
    public Select<T> onlyNotWorkingCopy(int tableIndex)
    {
        if(testClass(tableIndex, Workable.class))
        {
            where(tableIndex, "checkoutInfo.state","<>","wrk");
        }
        return this;
    }
    
    private void onlyLatestBranch(int tableIndex)
    {
//        select count(*) from wtpart t where t.versionsortida2versioninfo =
//        (
//               select max(p.versionsortida2versioninfo) from wtpart p where t.ida3masterreference=p.ida3masterreference
//        )
        appendAnd();
        String prefix="BR"+Long.toHexString(new Date().getTime());
        QuerySpec subSpec=null;
        try
        {
            subSpec = new QuerySpec();
            subSpec.getFromClause().setAliasPrefix(prefix);
            subSpec.appendClassList(_spec.getClassAt(tableIndex), false);
            subSpec.appendSelect(
                        SQLFunction.newSQLFunction(
                            SQLFunction.MAXIMUM,
                            new ClassAttribute(
                                _spec.getClassAt(tableIndex),
                                "versionInfo.identifier.versionSortId"
                            )
                        ),
                        false
            );
            
            //下面一句防止MAX语句应用到WTPart的子表 （重要！否则会报错）
            subSpec.getFromClause().getTableExpressionAt(0).setDescendantsIncluded(false);
            
            String[] alias=new String[2];
            TableExpression[] expressiones=new TableExpression[2];
            
            alias[0]=_spec.getFromClause().getAliasAt(tableIndex);
            expressiones[0]=_spec.getFromClause().getTableExpressionAt(tableIndex);
            alias[1]=subSpec.getFromClause().getAliasAt(0);
            expressiones[1]=subSpec.getFromClause().getTableExpressionAt(0);
            subSpec.appendWhere(
                    new SearchCondition(
                        _spec.getClassAt(tableIndex),
                        "masterReference.key.id",
                        _spec.getClassAt(tableIndex),
                        "masterReference.key.id"
                    ),
                    expressiones,
                    alias
            );
            
            appendAnd();
            SubSelectExpression subSelectExpression = new SubSelectExpression(subSpec);
            
            SearchCondition searchCondition = new SearchCondition(
                      subSelectExpression,"="
                      ,new ClassAttribute(_spec.getClassAt(tableIndex),"versionInfo.identifier.versionSortId")
            );
            _spec.appendWhere(searchCondition,new int[]{tableIndex});
            
        } catch (QueryException e)
        {
            throw new RuntimeException("Can't create branch QuerySpec",e);
        } catch (WTPropertyVetoException e)
        {
            throw new RuntimeException("Prefix invalid:"+prefix,e);
        }
           
    }

    /**
     * 為RevisionControlled的物件（如WTPart，WTDocument）添加必須是每個版次的最新版序的條件。
     * 
     * 如：某WTPart有A.1,A.2,A.3,B.1,B.2五個版本，則使用本條件後，只返回A.3和B.2物件。
     * 
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> onlyLatestIteration(int tableIndex)
    {
        appendAnd();
        SelectUtil.onlyLatestIteration(_spec,tableIndex);
        return this;
    }
    
//    /**
//     * 為RevisionControlled的物件（如WTPart，WTDocument）添加必須是每個版次的最新版序的條件。
//     * 
//     * 如：某WTPart有A.1,A.2,A.3,B.1,B.2五個版本，則使用本條件後，只返回A.3和B.2物件。
//     * 
//     * @return Select對象本身
//     * @author 張旋
//     */
//    public Select<T> onlyLatestBranch(int tableIndex)
//    {
//        appendAnd();
//        SelectUtil.onlyLatestVersion(_spec, tableIndex);
//        return this;
//    }
    
//    /**
//     * 為OneOffVersioned的物件（如WTPart，WTDocument）添加必須是最新版次的條件。
//     * 
//     * 如：某WTPart有A.A.1,A.2,A.3,B.1,B.2五個版本，則使用本條件後，只返回A.3和B.2物件。
//     * 
//     * @return Select對象本身
//     * @author 張旋
//     */
//    public Select<T> onlyLatestOneOffVersion(int tableIndex)
//    {
//        appendAnd();
//        SelectUtil.onlyLatestOneOffVersion(_spec, tableIndex);
//        return this;
//    }
//    
//    public Select<T> onlyNotExistsOneOffVersion(int tableIndex)
//    {
//        appendAnd();
//        SelectUtil.notExistsOneOffVersion(_spec, tableIndex);
//        return this;
//    }



    /**
     * 為要搜索的物件添加必須在某資料夾下的搜索條件
     * 
     * @param folder
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> onlyInFolder(Folder folder)
    {
        try
        {
            IteratedFolderedConfigSpec.newIteratedFolderedConfigSpec(folder)
                                      .appendSearchCriteria(_spec);
        } catch (Exception e)
        {
            throw new WTRuntimeException(e);
        }
        return this;
    }

    /**
     * 為要搜索的條件添加必須在某個Baseline中的條件
     * 
     * @param baseline
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> onlyInBaseline(ManagedBaseline baseline)
    {
        try
        {
            BaselineConfigSpec.newBaselineConfigSpec(baseline).appendSearchCriteria(_spec);
        } catch (Exception e)
        {
            throw new WTRuntimeException(e);
        }
        return this;
    }

    /**
     * 增加排序條件，可多次添加，按多個字段排序
     * 
     * @param tableIndex　要排序的物件的表索引
     * @param key　要排序的物件的屬性
     * @param desc　true為遞減順序，false為遞增順序
     * @return Select對象本身
     * @author 張旋
     */
    public Select<T> orderby(int tableIndex, String key, boolean desc)
    {
        try
        {
            _spec.appendOrderBy(new OrderBy(new ClassAttribute(_spec.getClassAt(tableIndex), key),
                                            desc), new int[] { tableIndex });
        } catch (QueryException e)
        {
            throw new WTRuntimeException(e);
        }
        return this;
    }

    // [end]
    // [start]工具類方法
    /**
     * 為QuerySpec的Where從句自動添加and
     * 
     * @author 張旋
     */
    private void appendAnd()
    {
        SelectUtil.appendAnd(_spec);
    }

    // [end]
    // [start]公用靜態方法
    /**
     * 主要工廠方法，傳入要查找的物件類型以構造Select對象
     * 
     * @param objClass 要查詢的物件類型，對應的tableIndex（物件表索引）為0
     * @return 構造好的Select對象
     * @author 張旋
     */
    public static < T > Select<T> from(Class<T> objClass)
    {
        return new Select<T>(objClass);
    }

    /**
     * 從一個指定的QuerySpec構造Select對象
     * 
     * @param spec 要繼續構造的QuerySpec對象
     * @return 構造好的Select對象
     * @author 張旋
     */
    public static < T > Select<T> from(QuerySpec spec)
    {
        return new Select<T>(spec);
    }

    /**
     * 根據oid直接查找物件
     * 
     * @param oid
     * @return Select對象本身
     * @author 張旋
     */
    public static Persistable byOid(String oid)
    {
        wt.fc.Persistable persistable = null;
        if (oid != null)
        {
            ReferenceFactory referencefactory = new ReferenceFactory();
            WTReference wtreference = null;
            try
            {
                wtreference = referencefactory.getReference(oid);
            } catch (Exception e)
            {
                throw new WTRuntimeException(e);
            }
            persistable = wtreference != null ? wtreference.getObject() : null;
        }
        return persistable;
    }

    // [end]
    // [start]非查詢非靜態方法
    public Object clone()
    {
        Select<T> s = new Select<T>((QuerySpec) _spec.clone());
        s.pagingSessionId = this.pagingSessionId;
        s._ibaMap = this._ibaMap;
        return s;
    }

    /**
     * 返回一個本實例的復本，對本實例的變更不會影響到復本。
     * 
     * @return
     * @author 張旋
     */
    @SuppressWarnings("unchecked")
    public Select<T> copy()
    {
        return (Select<T>) clone();
    }

    public String toString()
    {
        return (String) RemoteMethod.invokeObject(_spec, "toString", new Class[]{});
    }

    /**
     * 取得某個物件表在from子句中的位置
     * 
     * @param objClass
     * @return
     * @author 張旋
     */
    public int getTableIndex(Class<? extends Persistable> objClass)
    {
        return _spec.getFromClause().getPosition(objClass);
    }
    // [end]
    // [start]分組
    // 待增加分組功能，返回List（每一個元素是一個List）或Map
    // [end]
    
    public static Iterated latest(Iterated iterated)
    {
        try
        {
            return VersionControlHelper.getLatestIteration(iterated, false);
        } catch (Exception e)
        {
            throw new RuntimeException("Can't get latest object for "+iterated,e);
        }
    }
    
    public static Iterated latest(Mastered mastered)
    {
        QueryResult allIterationsOf=null;
        try
        {
            allIterationsOf = VersionControlHelper.service.allIterationsOf(mastered);
            if(allIterationsOf.hasMoreElements())
            {
                return (Iterated) allIterationsOf.nextElement(); //Windchill API中講到，第1個元素就是最新元素
            }
            else
            {
                return null;
            }
        } catch (Exception e)
        {
            throw new RuntimeException("Can't get Latest Object for Mastered :"+mastered,e);
        }

    }
    

    

}
