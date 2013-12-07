package com.phodev.andtools.html.pagedriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;

import com.phodev.andtools.R;
import com.phodev.andtools.common.CommonParam;

/**
 * 负责HTML的生成
 * 
 * @author skg
 */
public class HtmlCreator {
	public final String HTML_IMG_TEMPLET;
	public final String HTML_TEXT_TEMPLET;
	//
	static final String IMG_DEG_SRC = //
	CommonParam.PAGE_DRIVER_TEMPLATE_DIR + "default_img.png";
	//
	static final String IMG_LOGDING_SRC = //
	CommonParam.PAGE_DRIVER_TEMPLATE_DIR + "loading.gif";

	HtmlCreator(Context context) {
		Resources r = context.getResources();
		HTML_IMG_TEMPLET = loadTemplet(r, R.raw.page_driver_html_img_template);
		HTML_TEXT_TEMPLET = loadTemplet(r, R.raw.page_driver_html_text_template);
		if (HTML_IMG_TEMPLET == null || HTML_TEXT_TEMPLET == null) {
			throw new RuntimeException("page driver html templet init failed");
		}
	}

	private String loadTemplet(Resources r, int rawResId) {
		String tempetStr = null;
		InputStream is = r.openRawResource(rawResId);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader bufferdReader = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		try {
			String lineStr = null;
			while ((lineStr = bufferdReader.readLine()) != null) {
				sb.append(lineStr);
			}
			tempetStr = sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			//
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				isr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				bufferdReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tempetStr;
	}

	public String createHtmlBodyContent(WebPageContent ar) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> strs = ar.getRawContentList();
		int size = (strs == null ? 0 : strs.size());
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				if (ar.isImageType(i)) {
					// 先使用默认图片
					long pId = ar.getPageId();
					sb.append(createHtmlImage(pId + i, IMG_LOGDING_SRC, pId, i,
							IMG_DEG_SRC));
				} else {
					sb.append(createHtmlText(strs.get(i)));
				}
			}
		}
		return sb.toString();
	}

	private String createHtmlImage(long imgTagId, String defImgPath,
			long pageId, int position, String onErrorImgPath) {
		return String.format(HTML_IMG_TEMPLET, imgTagId, defImgPath, pageId,
				position, onErrorImgPath, position);
	}

	private String createHtmlText(String text) {
		return String.format(HTML_TEXT_TEMPLET, text);
	}
}
