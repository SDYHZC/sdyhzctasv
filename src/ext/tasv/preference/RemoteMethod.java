package ext.tasv.preference;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;

public class RemoteMethod implements RemoteAccess
{
    public static Object invokeProcessor(Serializable processor, Object... params)
    {
        Object ret=null;
        if (RemoteMethodServer.ServerFlag)
        {
            //保證processor不為null
            if (processor == null)
                throw new NullPointerException("Processor can't be null");
            
            //取得processor中聲明的所有方法
            Method[] declaredMethods = processor.getClass().getDeclaredMethods();
            
            //如果沒有聲明任何方法，則報錯
            if (declaredMethods.length == 0)
                throw new IllegalArgumentException("Processor must have a public method named \"process\"!");
            
            //定義保存process方法的變量
            Method processMethod = null;
            int count = 0;
            //查找process方法
            for (int i = 0; i < declaredMethods.length; i++)
            {
                Method m = declaredMethods[i];
                if ("process".equals(m.getName()))
                {
                    count++;
                    processMethod = m;
                }
            }
            //如果沒找到，或找到超過1個，則報錯
            if (count != 1)
                throw new IllegalArgumentException("Processor must have one and only one public method named \"process\"!");
            try
            {
                //保證方法可被調用（子類、內部類、不可見方法等。。。）
                processMethod.setAccessible(true);
                // 調用process方法
                ret = processMethod.invoke(processor, params);
            } catch (IllegalArgumentException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Illegal Arguments:" + Arrays.asList(params)
                                           + " for process method",e);
            } catch (IllegalAccessException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Can't access process method",e);
            } catch (InvocationTargetException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Unexpected exception while invoking process method", e);
            }
        } else
        {
            ret= invoke("invokeProcessor", RemoteMethod.class.getName(), null, new Class[] {
                    Serializable.class, Object[].class }, new Object[] { processor, params });
        }
        return ret;
    }

    public static Object invoke(String methodName,
                                String className,
                                Object obj,
                                Class<?>[] paramTypes,
                                Object[] args)
    {
        try
        {
            return RemoteMethodServer.getDefault().invoke(methodName,className,obj,paramTypes,args);
        } catch (RemoteException e)
        {
            throw new RuntimeException("Can't invoke RemoteMethodServer Method", e);
        } catch (InvocationTargetException e)
        {
            throw new RuntimeException("Can't invoke RemoteMethodServer Method", e);
        }
    }

    /**
     * 調用服務器端的靜態方法
     * 
     * @param c
     * @param methodName
     * @param paramTypes
     * @param params
     * @return
     * @author 張旋
     */
    public static Object invokeStatic(Class<?> c,
                                      String methodName,
                                      Class<?>[] paramTypes,
                                      Object... params)
    {
        Object ret = null;
        if (RemoteMethodServer.ServerFlag)
        {
            Method method = null;
            // 保證要調用靜態方法的類不為空
            if (c == null)
            {
                throw new NullPointerException("Class can't be null");
            }
            try
            {
                // 取得要調用的方法
                method = c.getMethod(methodName, paramTypes);
            } catch (SecurityException e)
            {
                // 沒有權限取得，拋出異常
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e)
            {
                // 沒有此方法，拋出異常
                String message = c.getClass().getName() + " doesn't have a method called '"
                                 + methodName + "',paramTypes:" + Arrays.asList(paramTypes);
                throw new RuntimeException(message, e);
            }
            try
            {
                //保證方法可被調用（子類、內部類、不可見方法等。。。）
                method.setAccessible(true);
                // 調用需要的方法
                ret = method.invoke(null, params);
            } catch (IllegalArgumentException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Illegal Arguments:" + Arrays.asList(params)
                                           + " for method " + c.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (IllegalAccessException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Can't access method " + c.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (InvocationTargetException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Unexpected exception while invoking remote method "
                                           + c.getClass().getName() + "." + methodName
                                           + "',paramTypes:" + Arrays.asList(paramTypes), e);
            }
        } else
        {
            ret = invoke("invokeStatic", RemoteMethod.class.getName(), null, new Class[] {
                    Class.class, String.class, Class[].class, Object[].class }, new Object[] { c,
                    methodName, paramTypes, params });
        }
        return ret;
    }

    public static Object invokeObject(Serializable o,
                                      String methodName,
                                      Class<?>[] paramTypes,
                                      Object... params)
    {
        Object ret = null;
        if (RemoteMethodServer.ServerFlag)
        {
            Method method = null;
            // 保證要調用的物件不為空
            if (o == null)
            {
                throw new NullPointerException("Object can't be null");
            }
            // 取得要調用物件的類型
            Class<?> c = o.getClass();
            try
            {
                // 取得要調用的方法
                method = c.getMethod(methodName, paramTypes);
            } catch (SecurityException e)
            {
                // 沒有權限取得，拋出異常
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e)
            {
                // 沒有此方法，拋出異常
                String message = c.getClass().getName() + " doesn't have a method called '"
                                 + methodName + "',paramTypes:" + Arrays.asList(paramTypes);
                throw new RuntimeException(message, e);
            }
            try
            {
                //保證方法可被調用（子類、內部類、不可見方法等。。。）
                method.setAccessible(true);
                // 調用需要的方法
                ret = method.invoke(o, params);
            } catch (IllegalArgumentException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Illegal Arguments:" + Arrays.asList(params)
                                           + " for method " + c.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (IllegalAccessException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Can't access method " + c.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (InvocationTargetException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Unexpected exception while invoking remote method "
                                           + c.getClass().getName() + "." + methodName
                                           + "',paramTypes:" + Arrays.asList(paramTypes), e);
            }
        } else
        {
            ret = invoke("invokeObject",
                         RemoteMethod.class.getName(),
                         null,
                         new Class[] { Serializable.class, String.class, Class[].class,
                                 Object[].class },
                         new Object[] { o, methodName, paramTypes, params });
        }
        return ret;
    }

    public static Object invokeService(Class<?> c,
                                       String serviceFieldName,
                                       String methodName,
                                       Class<?>[] paramTypes,
                                       Object... params)
    {
        Object ret = null;
        if (RemoteMethodServer.ServerFlag)
        {
            Method method = null;
            // 保證參數不為空
            if (c == null)
            {
                throw new NullPointerException("Class can't be null");
            }
            if (serviceFieldName == null)
            {
                throw new NullPointerException("Class can't be null");
            }
            Object service;
            try
            {
                // 取得要調用的service物件
                Field field = c.getField(serviceFieldName);
                field.setAccessible(true);
                service = field.get(null);
            } catch (IllegalArgumentException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Illegal Arguments:" + Arrays.asList(params));
            } catch (SecurityException e)
            {
                // 沒有權限取得，拋出異常
                throw new RuntimeException(e);
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException("Illegal Access", e);
            } catch (NoSuchFieldException e)
            {
                throw new RuntimeException("No such field " + serviceFieldName, e);
            }
            // 取得要調用物件的類型
            Class<?> oc = service.getClass();
            try
            {
                // 取得要調用的方法
                method = oc.getMethod(methodName, paramTypes);
            } catch (SecurityException e)
            {
                // 沒有權限取得，拋出異常
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e)
            {
                // 沒有此方法，拋出異常
                String message = oc.getClass().getName() + " doesn't have a method called '"
                                 + methodName + "',paramTypes:" + Arrays.asList(paramTypes);
                throw new RuntimeException(message, e);
            }
            try
            {
                //保證方法可被調用（子類、內部類、不可見方法等。。。）
                method.setAccessible(true);
                // 調用需要的方法
                ret = method.invoke(service, params);
            } catch (IllegalArgumentException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Illegal Arguments:" + Arrays.asList(params)
                                           + " for method " + oc.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (IllegalAccessException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Can't access method " + oc.getClass().getName() + "."
                                           + methodName + "',paramTypes:"
                                           + Arrays.asList(paramTypes));
            } catch (InvocationTargetException e)
            {
                // 調用過程中出現問題，拋出異常
                throw new RuntimeException("Unexpected exception while invoking remote method "
                                           + oc.getClass().getName() + "." + methodName
                                           + "',paramTypes:" + Arrays.asList(paramTypes), e);
            }
        } else
        {
            ret = invoke("invokeService",
                         RemoteMethod.class.getName(),
                         null,
                         new Class[] { Class.class, String.class, String.class, Class[].class,
                                 Object[].class },
                         new Object[] { c, serviceFieldName, methodName, paramTypes, params });
        }
        return ret;
    }
}
