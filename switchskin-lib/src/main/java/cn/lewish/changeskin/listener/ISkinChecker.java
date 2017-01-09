package cn.lewish.changeskin.listener;

import android.content.Context;

/**
 * 检查Skin是否合法
 */
public interface ISkinChecker {
	
	/**
	 * Check whether the skin is exits or legal
	 * @param context
	 * @param path
	 * @return 
	 */
	boolean isSkinPackageLegality(Context context, String path);
}
