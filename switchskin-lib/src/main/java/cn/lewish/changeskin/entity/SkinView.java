package cn.lewish.changeskin.entity;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.lewish.changeskin.utils.ListUtils;


/**
 * SkinView
 */
public class SkinView {
	
	public View view;
	
	public List<SkinAttr> attrs;
	
	public SkinView(){
		attrs = new ArrayList<SkinAttr>();
	}
	
	public void apply(){
		if(ListUtils.isEmpty(attrs)){
			return;
		}
		for(SkinAttr at : attrs){
			at.apply(view);
		}
	}
	
	public void clean(){
		if(ListUtils.isEmpty(attrs)){
			return;
		}
		for(SkinAttr at : attrs){
			at = null;
		}
	}

	@Override
	public String toString() {
		return "SkinView [view=" + view.getClass().getSimpleName() + ", attrs=" + attrs + "]";
	}
}
