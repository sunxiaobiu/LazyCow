package com.example.crowdtestinglibrary.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class DexUtils {

    public static ArrayList<String> findClassesStartWith(String prefix) {
        try {
            ArrayList<String> result = new ArrayList<>();
            ArrayList<DexFile> dexFiles = findAllDexFiles(Thread.currentThread().getContextClassLoader());
            for (DexFile dexFile : dexFiles) {
                Enumeration<String> classNames = dexFile.entries();
                while (classNames.hasMoreElements()) {
                    String className = classNames.nextElement();
                    if (className.startsWith(prefix)) {
                        result.add(className);
                    }
                }
            }
            return result;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ArrayList<String> findClassesEndWith(String endfix) {
        try {
            ArrayList<String> result = new ArrayList<>();
            ArrayList<DexFile> dexFiles = findAllDexFiles(Thread.currentThread().getContextClassLoader());
            for (DexFile dexFile : dexFiles) {
                Enumeration<String> classNames = dexFile.entries();
                while (classNames.hasMoreElements()) {
                    String className = classNames.nextElement();
                    if (className.endsWith(endfix)) {
                        result.add(className);
                    }
                }
            }
            return result;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ArrayList<String> findClassesStartEndWith(String prefix, String endfix) {
        try {
            ArrayList<String> result = new ArrayList<>();
            ArrayList<DexFile> dexFiles = findAllDexFiles(Thread.currentThread().getContextClassLoader());
            for (DexFile dexFile : dexFiles) {
                Enumeration<String> classNames = dexFile.entries();
                while (classNames.hasMoreElements()) {
                    String className = classNames.nextElement();
                    if (className.startsWith(prefix) && className.endsWith(endfix)) {
                        result.add(className);
                    }
                }
            }
            return result;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static ArrayList<DexFile> findAllDexFiles(ClassLoader classLoader) {
        ArrayList<DexFile> dexFiles = new ArrayList<>();
        try {
            Field pathListField = findField(classLoader, "pathList");
            Object pathList = pathListField.get(classLoader);
            Field dexElementsField = findField(pathList, "dexElements");
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);
            Field dexFileField = findField(dexElements[0], "dexFile");

            for (Object dexElement : dexElements) {
                Object dexFile = dexFileField.get(dexElement);
                dexFiles.add((DexFile) dexFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dexFiles;
    }

    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("=======================Field " + name + " not found in " + instance.getClass());
    }
}