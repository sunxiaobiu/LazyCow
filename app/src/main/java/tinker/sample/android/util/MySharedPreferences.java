package tinker.sample.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public interface MySharedPreferences {
    class SharedPreferencesUtil {
        private static final String FILE_NAME = "Config";
        private static SharedPreferences mPreferences;
        private static SharedPreferences.Editor mEditor;
        private static SharedPreferencesUtil mSharedPreferencesUtil;

        @SuppressLint("CommitPrefEdits")
        SharedPreferencesUtil(Context context) {
            mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            mEditor = mPreferences.edit();
        }

        public static SharedPreferencesUtil getInstance(Context context) {
            if (mSharedPreferencesUtil == null) {
                mSharedPreferencesUtil = new SharedPreferencesUtil(context);
            }
            return mSharedPreferencesUtil;
        }

        /**
         * 保存数据到文件
         */
        public void saveData(String key, Object data) {
            String type = data.getClass().getSimpleName();

            if ("Integer".equals(type)) {
                mEditor.putInt(key, (Integer) data);
            } else if ("Boolean".equals(type)) {
                mEditor.putBoolean(key, (Boolean) data);
            } else if ("String".equals(type)) {
                mEditor.putString(key, (String) data);
            } else if ("Float".equals(type)) {
                mEditor.putFloat(key, (Float) data);
            } else if ("Long".equals(type)) {
                mEditor.putLong(key, (Long) data);
            }

            SharedPreferencesCompat.apply(mEditor);
        }

        /**
         * 从文件中读取数据
         */
        public Object getData(String key, Object defValue) {

            String type = defValue.getClass().getSimpleName();

            //defValue为为默认值，如果当前获取不到数据就返回它
            if ("Integer".equals(type)) {
                return mPreferences.getInt(key, (Integer) defValue);
            } else if ("Boolean".equals(type)) {
                return mPreferences.getBoolean(key, (Boolean) defValue);
            } else if ("String".equals(type)) {
                return mPreferences.getString(key, (String) defValue);
            } else if ("Float".equals(type)) {
                return mPreferences.getFloat(key, (Float) defValue);
            } else if ("Long".equals(type)) {
                return mPreferences.getLong(key, (Long) defValue);
            }

            return null;
        }

        /**
         * 清除所有数据
         */
        public void clear() {
            mEditor.clear();
            SharedPreferencesCompat.apply(mEditor);
        }

        /**
         * 移除某个key值已经对应的值
         */
        public void remove(String key) {
            mEditor.remove(key);
            SharedPreferencesCompat.apply(mEditor);
        }

        /**
         * 查询某个key是否已经存在
         */
        public boolean contains(String key) {
            return mPreferences.contains(key);
        }

        /**
         * 返回所有的键值对
         */
        public Map<String, ?> getAll() {
            return mPreferences.getAll();
        }
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException ignored) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException ignored) {
            } catch (IllegalAccessException ignored) {
            } catch (InvocationTargetException ignored) {
            }
            editor.commit();
        }
    }
}
