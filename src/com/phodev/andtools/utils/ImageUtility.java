package com.phodev.andtools.utils;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;

/**
 * 图片工具类
 * 
 * @author skg
 * 
 */
public class ImageUtility {
	public static Bitmap getCachedImage(Context context, String url,
			int targetWidthPx, boolean manualScale) {
		AQuery aq = new AQuery(context);
		if (manualScale)
			targetWidthPx = (targetWidthPx * 3) / 5;
		return aq.getCachedImage(url, targetWidthPx);
	}

	public static boolean loadImage(ImageView img, String url,
			int targetWidthPx, int waitingFaceResId, boolean manualScale) {

		if (manualScale)
			targetWidthPx = (targetWidthPx * 3) / 5;
		if (img == null)
			return false;
		if (url != null && url.length() > 0) {// URL可用
			AQuery aq = new AQuery(img);
			Bitmap bt = aq.getCachedImage(url, targetWidthPx);
			if (bt == null) {// 没有已经缓存的图片，先显示默认图片
				Bitmap presetBt = null;
				if (waitingFaceResId != 0) {
					presetBt = getMemoryCahceBitmap(img.getContext(),
							waitingFaceResId);
				}
				aq.image(url, true, true, targetWidthPx, 0, presetBt,
						AQuery.FADE_IN);
			} else {// 存在缓存图片，直接使用缓存图片,不需要先显示等待或者默认图片
				img.setImageBitmap(bt);
			}
		} else {
			fillOrClearImageView(img, waitingFaceResId);
		}
		return true;
	}

	/**
	 * 不管URL,ImageView是否是有效的,CallBack都会确保被调用
	 * 
	 * @param img
	 * @param url
	 * @param targetWidthPx
	 * @param waitingFaceResId
	 * @param callback
	 */
	public static void loadImage(ImageView img, String url, int targetWidthPx,
			int waitingFaceResId, BitmapAjaxCallback callback,
			boolean manualScale) {
		if (manualScale)
			targetWidthPx = (targetWidthPx * 3) / 5;
		AQuery aq = new AQuery(img);
		aq.image(url, true, true, targetWidthPx, waitingFaceResId, callback);
	}

	/**
	 * 添加一个等待的ProgressBar交给AQuery处理
	 * 
	 * @param img
	 * @param url
	 * @param targetWidthPx
	 * @param waitingFaceResId
	 * @param callback
	 * @param manualScale
	 * @param progress
	 */
	public static void loadImage(ImageView img, String url, int targetWidthPx,
			int waitingFaceResId, BitmapAjaxCallback callback,
			boolean manualScale, View progress) {
		if (manualScale)
			targetWidthPx = (targetWidthPx * 3) / 5;
		AQuery aq = new AQuery(img);
		aq = aq.progress(progress);
		aq.image(url, true, true, targetWidthPx, waitingFaceResId, callback);
	}

	public static void loadImage(ImageView img, String url, boolean memCache,
			boolean fileCache, int targetWidthPx, int waitingFaceResId,
			BitmapAjaxCallback callback, boolean manualScale) {
		if (manualScale)
			targetWidthPx = (targetWidthPx * 3) / 5;
		AQuery aq = new AQuery(img);
		aq.image(url, memCache, fileCache, targetWidthPx, waitingFaceResId,
				callback);
	}

	/**
	 * @see #cancelAqueryLoad(ImageView, boolean)
	 */
	public static void cancelAqueryLoad(ImageView img) {
		cancelAqueryLoad(img, false);
	}

	/**
	 * 将会清除View中key是AQuery.TAG_URL的Tag
	 * 
	 * @param img
	 * @param clearCurImgData
	 *            是否也把当前View的内容清除(只当View是ImageView的时候有效，clear之后图片将不显示任何东西)
	 */
	public static void cancelAqueryLoad(ImageView img, boolean clearCurImgData) {
		if (img != null) {
			cancelAqueryLoadRelationOnly(img);
			if (clearCurImgData) {
				img.setImageDrawable(null);
			}
		}
	}

	/**
	 * 取消和Aquery的加载关系，并使用指定的默认图片替换
	 * 
	 * @param img
	 * @param replaceResId
	 */
	public static void cancelAqueryLoad(ImageView img, int replaceResId) {
		if (img != null) {
			cancelAqueryLoadRelationOnly(img);
			fillOrClearImageView(img, replaceResId);
		}
	}

	/**
	 * 清除跟Aquery的关系，并不改变view任何特性
	 * 
	 * @param view
	 */
	public static void cancelAqueryLoadRelationOnly(View view) {
		if (view != null) {
			// 这个清除是否真的有效要根据AQuery的实现而定，之所以用下面的方法来清除AQuery加载关联关系，
			// 是因为我们看过源码里的处理逻辑，但是并不代表适合所有AQuery版本
			view.setTag(AQuery.TAG_URL, null);
		}
	}

	private static void fillOrClearImageView(ImageView img, int resId) {
		if (img != null) {
			if (resId == 0) {// 如果没有可以显示的图片，要清空ImageView的现有效果
				img.setImageBitmap(null);
			} else {
				// 如果URL可用，并且缓存没有数据的时候才用等待的图标
				img.setImageResource(resId);
			}
		}
	}

	private static SparseArray<SoftReference<Bitmap>> bitmaps = new SparseArray<SoftReference<Bitmap>>();

	/**
	 * 如果存在则直接返回，否则会尝试跟聚Resource Id读取，并添加到内存，所以要谨慎使用，避免不必要的缓存
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	private static Bitmap getMemoryCahceBitmap(Context context, int resId) {
		SoftReference<Bitmap> refer = bitmaps.get(resId);
		if (refer == null || refer.get() == null) {
			Bitmap bt = null;
			bt = BitmapFactory.decodeResource(context.getResources(), resId);
			refer = new SoftReference<Bitmap>(bt);
			bitmaps.put(resId, refer);
		}
		return refer.get();
	}

}
