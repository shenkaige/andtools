package com.phodev.andtools.samples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.html.pagedriver.WebPageContent;
import com.phodev.andtools.html.pagedriver.WebPageDriver;
import com.phodev.andtools.html.pagedriver.WebPageDriver.PageDriverListener;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.CommonUtils;

/**
 * 组织文字和图片用网页显示
 * 
 * @author sky
 * 
 */
@SimpleDesc(title = "WebPageDriver", desc = "组织文字和图片用网页显示")
public class WebPageDriverFragment extends InnerFragment {
	private WebView webview;
	private WebPageDriver webPageDriver;
	private WebPageContent content = new WebPageContent();
	{
		content.addTextContent("<h2><a href=\"http:www.baidu.com\">Test fill img and text use WebView</a></h2>");
		content.addTextContent("=======================");
		content.addImage("http://www.zetakey.com/img/Linux_Logo-200x200.jpg");
		content.addTextContent("<h2>test text</h2>hello hello");
		content.addImage("http://i-cdn.phonearena.com/images/article/28207-image/Linux-3.3-kernel-outed-Android-code-now-included-in-it.jpg");
		content.addImage("https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcQ5V__Gw4jItM2Z7_SSGCex1v3FRi5O56dv_SjEvWAVUhXFWRXo");
		content.addTextContent("-----------------------");
		content.addTextContent("=======================");
		content.addTextContent("=======================");
		content.addTextContent("=======================");
		content.addTextContent("-----------------------");
		content.addImage("https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRGh1lir7QPnv-Z-_j_mnh2qc_OOtvthi-PS4dq-zNoFxlxPipF");
		content.addTextContent("-----------------------");
		content.addTextContent("-----------------------");
		content.addTextContent("-----------------------");
		content.addImage("https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTFBf7aa4G6ez0t11RbvI-rlNT-Jz5JxLvarV9qQY_Xuy7ixB45");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		webview = new WebView(getActivity());
		LayoutParams params = new LayoutParams(-1, -1);
		webview.setLayoutParams(params);
		//
		webPageDriver = new WebPageDriver(webview);
		webPageDriver.setWebDriverListener(actionListener);
		webview.post(runnable);
		return webview;
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			webPageDriver.driverPager(content);
		}
	};

	private PageDriverListener actionListener = new PageDriverListener() {

		@Override
		public void onImageClick(long pageId, int position) {
			CommonUtils.toast(getActivity(), "img click#pageId:" + pageId
					+ ",position:" + position);
		}

		@Override
		public void onHrefClick(String url) {
			CommonUtils.toast(getActivity(), "onHrefClick#" + url);
		}
	};

}
