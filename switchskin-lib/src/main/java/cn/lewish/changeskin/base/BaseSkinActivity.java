package cn.lewish.changeskin.base;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.lewish.changeskin.entity.AttrFactory;
import cn.lewish.changeskin.entity.DynamicAttr;
import cn.lewish.changeskin.entity.SkinAttr;
import cn.lewish.changeskin.entity.SkinView;
import cn.lewish.changeskin.listener.IDynamicNewView;
import cn.lewish.changeskin.listener.ISkinUpdate;
import cn.lewish.changeskin.loader.SkinManager;
import cn.lewish.changeskin.utils.ListUtils;


/**
 * Created by sundong on 2017/1/9 9:50.
 */

public class BaseSkinActivity extends AppCompatActivity implements LayoutInflaterFactory,ISkinUpdate, IDynamicNewView {

    private List<SkinView> mSkinViews = new ArrayList<SkinView>();
    private boolean isResponseOnSkinChanging = true;

    static final Class<?>[] sConstructorSignature = new Class[]{
            Context.class, AttributeSet.class};
    private static final Map<String, Constructor<? extends View>> sConstructorMap
            = new ArrayMap<>();
    private final Object[] mConstructorArgs = new Object[2];
    private static Method sCreateViewMethod;
    static final Class<?>[] sCreateViewSignature = new Class[]{View.class, String.class, Context.class, AttributeSet.class};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        LayoutInflaterCompat.setFactory(layoutInflater, this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SkinManager.getInstance().attach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SkinManager.getInstance().detach(this);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        LayoutInflater layoutInflater = getLayoutInflater();
        AppCompatDelegate delegate = getDelegate();
        View view = null;
        try {
            //public View createView
            // (View parent, final String name, @NonNull Context context, @NonNull AttributeSet attrs)
            if (sCreateViewMethod == null) {
                Method methodOnCreateView = delegate.getClass().getMethod("createView", sCreateViewSignature);
                sCreateViewMethod = methodOnCreateView;
            }
            Object object = sCreateViewMethod.invoke(delegate, parent, name, context, attrs);
            view = (View) object;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        List<SkinAttr> skinAttrList = getSkinAttrs(context,attrs);
        if (skinAttrList.isEmpty()) {
            return view;//没有要换肤的属性，直接返回View
        }
        if (view == null) {//兼容包中没有该View，则根据反射重新创建
            view = createViewFromTag(context, name, attrs);
        }
        injectSkin(view, skinAttrList);
        return view;
    }

    private void injectSkin(View view, List<SkinAttr> skinAttrList) {
        if (!ListUtils.isEmpty(skinAttrList)) {
            SkinView skinView = new SkinView();
            skinView.view = view;
            skinView.attrs = skinAttrList;
            mSkinViews.add(skinView);
            if (SkinManager.getInstance().isExternalSkin()) {
                skinView.apply();
            }
        }
    }

    private View createViewFromTag(Context context, String name, AttributeSet attrs) {
        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;
            if (-1 == name.indexOf('.')) {
                String prefix = "android.widget.";
                if (TextUtils.equals(name, "View")) {
                    prefix = "android.view.";
                }
                return createView(context, name, prefix);
            } else {
                return createView(context, name, null);
            }
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        } finally {
            // Don't retain references on context.
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
    }

    private View createView(Context context, String name, String prefix)
            throws ClassNotFoundException, InflateException {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        try {
            if (constructor == null) {
                // Class not found in the cache, see if it's real, and try to add it
                Class<? extends View> clazz = context.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            // We do not want to catch these, lets return null and let the actual LayoutInflater
            // try
            return null;
        }
    }
    /**
     * 遍历AttributeSet，得到要换肤的List<SkinAttr>
     */
    private List<SkinAttr> getSkinAttrs(Context context, AttributeSet attrs){
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            if (!AttrFactory.isSupportedAttr(attrName)) {
                continue;
            }
            if (attrValue.startsWith("@")) {
                try {
                    int id = Integer.parseInt(attrValue.substring(1));
                    String entryName = context.getResources().getResourceEntryName(id);
                    String typeName = context.getResources().getResourceTypeName(id);
                    SkinAttr mSkinAttr = AttrFactory.get(attrName, id, entryName, typeName);
                    if (mSkinAttr != null) {
                        viewAttrs.add(mSkinAttr);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return viewAttrs;
    }

    public void applySkin(){
        if(ListUtils.isEmpty(mSkinViews)){
            return;
        }
        for(SkinView si : mSkinViews){
            if(si.view == null){
                continue;
            }
            si.apply();
        }
    }

    /**
     * 动态添加SkinView
     * @param context
     * @param view
     * @param attrName
     * @param attrValueResId
     */
    public void dynamicAddSkinEnableView(Context context, View view, String attrName, int attrValueResId){
        int id = attrValueResId;
        String entryName = context.getResources().getResourceEntryName(id);
        String typeName = context.getResources().getResourceTypeName(id);
        SkinAttr mSkinAttr = AttrFactory.get(attrName, id, entryName, typeName);
        SkinView skinView = new SkinView();
        skinView.view = view;
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        viewAttrs.add(mSkinAttr);
        skinView.attrs = viewAttrs;
        mSkinViews.add(skinView);
    }

    /**
     * 动态添加SkinView
     * @param context
     * @param view
     * @param pDAttrs
     */
    public void dynamicAddSkinEnableView(Context context, View view, List<DynamicAttr> pDAttrs){
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        SkinView skinView = new SkinView();
        skinView.view = view;
        for(DynamicAttr dAttr : pDAttrs){
            int id = dAttr.refResId;
            String entryName = context.getResources().getResourceEntryName(id);
            String typeName = context.getResources().getResourceTypeName(id);
            SkinAttr mSkinAttr = AttrFactory.get(dAttr.attrName, id, entryName, typeName);
            viewAttrs.add(mSkinAttr);
        }
        skinView.attrs = viewAttrs;
        mSkinViews.add(skinView);
    }

    protected void dynamicAddSkinEnableView(View view, String attrName, int attrValueResId){
        int id = attrValueResId;
        String entryName = getResources().getResourceEntryName(id);
        String typeName =  getResources().getResourceTypeName(id);
        SkinAttr mSkinAttr = AttrFactory.get(attrName, id, entryName, typeName);
        SkinView skinView = new SkinView();
        skinView.view = view;
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        viewAttrs.add(mSkinAttr);
        skinView.attrs = viewAttrs;
        mSkinViews.add(skinView);
    }

    protected void dynamicAddSkinEnableView(View view, List<DynamicAttr> pDAttrs){
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        SkinView skinView = new SkinView();
        skinView.view = view;
        for(DynamicAttr dAttr : pDAttrs){
            int id = dAttr.refResId;
            String entryName = getResources().getResourceEntryName(id);
            String typeName =  getResources().getResourceTypeName(id);
            SkinAttr mSkinAttr = AttrFactory.get(dAttr.attrName, id, entryName, typeName);
            viewAttrs.add(mSkinAttr);
        }
        skinView.attrs = viewAttrs;
        mSkinViews.add(skinView);
    }

    @Override
    public void dynamicAddView(View view, List<DynamicAttr> pDAttrs) {
        List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
        SkinView skinView = new SkinView();
        skinView.view = view;
        for(DynamicAttr dAttr : pDAttrs){
            int id = dAttr.refResId;
            String entryName = getResources().getResourceEntryName(id);
            String typeName =  getResources().getResourceTypeName(id);
            SkinAttr mSkinAttr = AttrFactory.get(dAttr.attrName, id, entryName, typeName);
            viewAttrs.add(mSkinAttr);
        }
        skinView.attrs = viewAttrs;
        mSkinViews.add(skinView);
    }

    public void clean(){
        if(ListUtils.isEmpty(mSkinViews)){
            return;
        }
        for(SkinView si : mSkinViews){
            if(si.view == null){
                continue;
            }
            si.clean();
        }
    }

    final protected void enableResponseOnSkinChanging(boolean enable){
        isResponseOnSkinChanging = enable;
    }

    @Override
    public void onThemeUpdate() {
        if(!isResponseOnSkinChanging) return;
        applySkin();
    }

}
