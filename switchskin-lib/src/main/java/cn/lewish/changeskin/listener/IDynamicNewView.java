package cn.lewish.changeskin.listener;

import android.view.View;

import java.util.List;

import cn.lewish.changeskin.entity.DynamicAttr;


/**
 * 动态添加View
 */
public interface IDynamicNewView {
	void dynamicAddView(View view, List<DynamicAttr> pDAttrs);
}
