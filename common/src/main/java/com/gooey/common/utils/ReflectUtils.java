package com.gooey.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtils {
    public static Object getField(Class<?> clazz, Object obj, String fieldName) {
        return getField(false, clazz, obj, fieldName);
    }

    /**
     * 仅给反射自动化测试用
     */
    public static Object getField(boolean throwException, Class<?> clazz, Object obj, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object getField(String className, Object obj, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getFieldByName(String className, Object obj, String fieldName) {
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldByName(String className, String fieldName, Object obj, Object value) {
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setField(Class<?> clazz, String fieldName, Object obj, Object value) {
        setField(false, clazz, fieldName, obj, value);
    }

    /**
     * 仅给反射自动化测试用
     */
    public static void setField(boolean throwException, Class<?> clazz, String fieldName, Object obj, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    public static Object invokeMethod(Class<?> clazz, String methodName, Class<?>[] signature, Object receiver, Object... args) {
        return invokeMethod(false, clazz, methodName, signature, receiver, args);
    }

    public static boolean invokeMethodSuccessOrNot(Class<?> clazz, String methodName, Class<?>[] signature, Object receiver, Object... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, signature);
            method.setAccessible(true);
            method.invoke(receiver, args);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 仅给反射自动化测试用
     */
    public static Object invokeMethod(boolean throwException, Class<?> clazz, String methodName, Class<?>[] signature, Object receiver, Object... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, signature);
            method.setAccessible(true);
            return method.invoke(receiver, args);
        } catch (Exception e) {
            if (throwException) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Object invokeMethod(Method method, Object receiver, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(receiver, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(String className, String methodName, Class<?>[] signature) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.getDeclaredMethod(methodName, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] signature) {
        try {
            return clazz.getDeclaredMethod(methodName, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }
        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }
        throw new NoSuchMethodException("Method " + name + " with parameters " + Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }
}
