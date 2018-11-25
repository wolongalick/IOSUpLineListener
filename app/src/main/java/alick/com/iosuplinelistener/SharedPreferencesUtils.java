package alick.com.iosuplinelistener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import java.util.HashSet;
import java.util.Set;


/**
 * @author : Ellison.Sun
 * @ClassName : SharedPreferencesUtils
 * @Description : 共享数据发布者
 * @date : 2015-9-2 上午10:14:51
 */
public class SharedPreferencesUtils {

    private static SharedPreferencesUtils instance;
    private static SharedPreferences mPreferences = null;

    private final String PREFS_NAME = "cuixingwang";

    public class Shared {
        String key;
        Object val;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getVal() {
            return val;
        }

        public void setVal(Object val) {
            this.val = val;
        }

        Shared(String key, Object val) {
            this.key = key;
            this.val = val;
        }
    }

    private SharedPreferencesUtils(Context context) {
        if (mPreferences != null) {
            return;
        }
        mPreferences = (context.getApplicationContext()).getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    static public SharedPreferencesUtils getInstance(Context context) {
        if (null == instance) {
            instance = new SharedPreferencesUtils(context);
        }
        return instance;
    }

    /**
     * @param key
     * @param value
     * @param tag
     */
    @SuppressLint("NewApi")
    public void put(String key, Object value, String tag) {
        Editor editor = mPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Long) value);
        } else if(value instanceof Set){
            editor.putStringSet(key, (Set<String>) value);
        }

//		if (null != tag && !tag.isEmpty())
//			getEventBus().post(new Shared(key, value));

        editor.commit();
    }

    /**
     * @param
     * @return
     * @desc (清空缓存中的数据)
     * @user sunyao
     * @createtime 2016/12/5 - 18:03
     */
    public void clearAll() {
        Editor editor = mPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * @param
     * @return
     * @desc (根据指定的key来删除缓存)
     * @user sunyao
     * @createtime 2016/12/5 - 18:04
     */
    public void clearByTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        Editor editor = mPreferences.edit();
        editor.remove(tag);
        editor.commit();
    }

    public void put(String key, Object value) {
        this.put(key, value, null);
    }

    public String getString(String key, String def) {
        return mPreferences.getString(key, def);
    }

    public String getString(String key) {
        return mPreferences.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public int getInteger(String key) {
        return mPreferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return mPreferences.getLong(key, 0L);
    }

    public float getFloat(String key) {
        return mPreferences.getFloat(key, 0f);
    }

    public Set<String> getSet(String key){
        return mPreferences.getStringSet(key,null);
    }

    public void putString2Set(String key, String value){
        Set<String> stringSet = mPreferences.getStringSet(key, null);
        if(stringSet==null){
            stringSet=new HashSet<>();
        }
        stringSet.add(value);
    }


}