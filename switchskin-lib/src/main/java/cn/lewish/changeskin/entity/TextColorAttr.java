package cn.lewish.changeskin.entity;

import android.view.View;
import android.widget.TextView;

import cn.lewish.changeskin.loader.SkinManager;

public class TextColorAttr extends SkinAttr {

	@Override
	public void apply(View view) {
		if(view instanceof TextView){
			TextView tv = (TextView)view;
			if(RES_TYPE_NAME_COLOR.equals(attrValueTypeName)){
				cn.lewish.changeskin.utils.L.e("attr1", "TextColorAttr");
				tv.setTextColor(SkinManager.getInstance().convertToColorStateList(attrValueRefId));
			}
		}
	}
}
