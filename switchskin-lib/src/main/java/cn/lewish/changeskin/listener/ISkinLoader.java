package cn.lewish.changeskin.listener;

/**
 * 皮肤加载监听
 */
public interface ISkinLoader {
	void attach(ISkinUpdate observer);
	void detach(ISkinUpdate observer);
	void notifySkinUpdate();
//	void notifySkinDefault();
}
