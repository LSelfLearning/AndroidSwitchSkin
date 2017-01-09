package cn.lewish.changeskin.loader;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.lewish.changeskin.config.SkinConfig;
import cn.lewish.changeskin.entity.AttrFactory;
import cn.lewish.changeskin.entity.DynamicAttr;
import cn.lewish.changeskin.entity.SkinAttr;
import cn.lewish.changeskin.entity.SkinView;
import cn.lewish.changeskin.utils.L;
import cn.lewish.changeskin.utils.ListUtils;


/**
 * Supply {@link SkinInflaterFactory} to be called when inflating from a LayoutInflater.
 * 
 * <p>Use this to collect the {skin:enable="true|false"} views availabled in our XML layout files.
 * 
 * @author fengjun
 */
public class SkinInflaterFactory implements Factory {
	
	private static final boolean DEBUG = true;
	
	/**
	 * Store the view item that need skin changing in the activity
	 */
	private List<SkinView> mSkinViews = new ArrayList<SkinView>();
	
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// if this is NOT enable to be skined , simplly skip it 
		boolean isSkinEnable = attrs.getAttributeBooleanValue(SkinConfig.NAMESPACE, SkinConfig.ATTR_SKIN_ENABLE, false);
        if (!isSkinEnable){
        		return null;
        }
		
		View view = createView(context, name, attrs);
		
		if (view == null){
			return null;
		}
		
		parseSkinAttr(context, attrs, view);
		
		return view;
	}
	
	/**
     * Invoke low-level function for instantiating a view by name. This attempts to
     * instantiate a view class of the given <var>name</var> found in this
     * LayoutInflater's ClassLoader.
     * 
     * @param context 
     * @param name The full name of the class to be instantiated.
     * @param attrs The XML attributes supplied for this instance.
     * 
     * @return View The newly instantiated view, or null.
     */
	private View createView(Context context, String name, AttributeSet attrs) {
		View view = null;
		try {
			if (-1 == name.indexOf('.')){
				if ("View".equals(name)) {
					view = LayoutInflater.from(context).createView(name, "android.view.", attrs);
				} 
				if (view == null) {
					view = LayoutInflater.from(context).createView(name, "android.widget.", attrs);
				} 
				if (view == null) {
					view = LayoutInflater.from(context).createView(name, "android.webkit.", attrs);
				} 
			}else {
	            view = LayoutInflater.from(context).createView(name, null, attrs);
	        }

			L.i("about to create " + name);

		} catch (Exception e) { 
			L.e("error while create 【" + name + "】 : " + e.getMessage());
			view = null;
		}
		return view;
	}

	/**
	 * Collect skin able tag such as background , textColor and so on
	 * @param context
	 * @param attrs
	 * @param view
	 */
	private void parseSkinAttr(Context context, AttributeSet attrs, View view) {
		List<SkinAttr> viewAttrs = new ArrayList<SkinAttr>();
		
		for (int i = 0; i < attrs.getAttributeCount(); i++){
			String attrName = attrs.getAttributeName(i);
			String attrValue = attrs.getAttributeValue(i);
			
			if(!AttrFactory.isSupportedAttr(attrName)){
				continue;
			}
			
		    if(attrValue.startsWith("@")){
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
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
		    }
		}
		
		if(!ListUtils.isEmpty(viewAttrs)){
			SkinView skinView = new SkinView();
			skinView.view = view;
			skinView.attrs = viewAttrs;

			mSkinViews.add(skinView);
			
			if(SkinManager.getInstance().isExternalSkin()){
				skinView.apply();
			}
		}
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
		addSkinView(skinView);
	}
	
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
		addSkinView(skinView);
	}
	
	public void addSkinView(SkinView item){
		mSkinViews.add(item);
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
}
