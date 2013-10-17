package com.phodev.andtools.drag;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class CacheManager<K> {
	private CacheBounds<K> mCacheBounds;
	private HashMap<K, Releasable> cacheList = new HashMap<K, Releasable>();

	public void setCacheBounds(CacheBounds<K> bounds) {
		mCacheBounds = bounds;
	}

	public void checkCacheaVailability() {
		if (cacheList.isEmpty()) {
			return;
		}
		Iterator<Entry<K, Releasable>> inter = cacheList.entrySet().iterator();
		while (inter.hasNext()) {
			Entry<K, Releasable> en = inter.next();
			if (!isNeedCache(en.getKey())) {
				Releasable rel = en.getValue();
				if (!rel.release()) {
					rel.onForceDiscard();
				}
			}

		}
		cacheList.clear();
	}

	public void releaseAll() {
		if (cacheList.isEmpty()) {
			return;
		}
		Iterator<Entry<K, Releasable>> inter = cacheList.entrySet().iterator();
		while (inter.hasNext()) {
			Entry<K, Releasable> en = inter.next();
			Releasable rel = en.getValue();
			if (!rel.release()) {
				rel.onForceDiscard();
			}
		}
		cacheList.clear();
	}

	public void addManage(K key, Releasable rel) {
		if (isNeedCache(key)) {
			cacheList.put(key, rel);
		} else {
			cacheList.remove(key);
			if (rel != null) {
				if (!rel.release()) {
					rel.onForceDiscard();
				}
			}
		}
	}

	public void removeManage(K key, Releasable rel) {
		cacheList.remove(key);
	}

	public boolean isNeedCache(K key) {
		if (mCacheBounds == null) {
			return false;
		}
		return mCacheBounds.isNeedCache(key);
	}

	public interface Releasable {
		public boolean release();

		public void onForceDiscard();
	}

	public interface CacheBounds<K> {
		public boolean isNeedCache(K key);
	}
}
