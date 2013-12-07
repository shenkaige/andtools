package com.phodev.andtools.html.pagedriver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.util.SparseIntArray;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.phodev.andtools.common.CommonParam;

/**
 * 控制WebView中图片统一从这里走,负责跟定义好的js交互
 * 
 * @author skg
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebPageDriver {
	public final static boolean DEBUG = CommonParam.DEBUG;
	private final static String TAG = "WebViewImageDriver";
	public final static String PAGE_TEMPLE_HTML_PATH = //
	CommonParam.PAGE_DRIVER_TEMPLATE_DIR + "page_detail.html";
	//
	public final static String JS_INTERFACE_NAME = "phodev_jsh";// js handler
	final static String JS_TAG = "javascript:";
	final static String JS_FILL_IMG = JS_TAG + "fillImage(\"%s\",\"%s\")";
	final static String JS_SET_TEXT_SIZE = JS_TAG + "setTextSize(\"%s\")";
	private HtmlCreator mHtmlCreator;
	private WebView mWebView;
	private String mPageHtmlContent;
	private WebPageContent mPageContent;
	private Handler handler;

	@SuppressLint("JavascriptInterface")
	public WebPageDriver(WebView webview) {
		if (webview == null) {
			throw new RuntimeException(TAG + " webview can not be null");
		}
		mHtmlCreator = new HtmlCreator(webview.getContext());
		mWebView = webview;
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(this, JS_INTERFACE_NAME);
		mWebView.setWebViewClient(mWebViewClient);
		WebSettings settings = mWebView.getSettings();
		//
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		settings.setDefaultTextEncodingName("utf-8");
		mWebView.setFocusable(false);
		//
		handler = new Handler(handlerCallback);
	}

	public void driverPager(WebPageContent pageData) {
		if (pageData == null) {
			// TODO 考虑清除当前的内容
			return;
		} else {
			mPageContent = pageData;
			mPageHtmlContent = mHtmlCreator.createHtmlBodyContent(pageData);
		}
		jsInvokeQuere.clear();
		errorImgPosition.clear();
		wattingTaskRequstIdRecord.clear();
		//
		mWebView.loadUrl(PAGE_TEMPLE_HTML_PATH);
	}

	/**
	 * 设置网页的字体大小
	 * 
	 * @param textSize
	 */
	public void setWebViewTextSize(String textSize) {
		invokeJsSetTextSize(textSize);
	}

	private WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
			// 开始加载图片
			if (mPageContent != null) {
				ArrayList<String> strs = mPageContent.getRawContentList();
				long pId = mPageContent.getPageId();
				int size = (strs == null ? 0 : strs.size());
				Context ctx = view.getContext();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						if (mPageContent.isImageType(i)) {
							long reqId = makeRequestId(pId, i);
							wattingTaskRequstIdRecord.add(reqId);
							loadImage(ctx, strs.get(i), reqId);
						}
					}
				}
			}
			checkJsInvokeQuere();
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (mWebDriverListener != null) {
				mWebDriverListener.onHrefClick(url);
			}
			// 阻止所有的内部跳转请求
			return true;
		}

	};

	private void loadImage(Context context, String url, long reqId) {
		AQuery aquery = new AQuery(mWebView.getContext());
		File file = null;
		if ((file = aquery.getCachedFile(url)) != null) {
			// TODO 检查文件完整性
			addJsInvokeToQeuer(reqId, file.getAbsolutePath());
			return;
		}
		ImageFileAjaxCallback ajaxCallback = new ImageFileAjaxCallback(reqId) {
			@Override
			public void callback(File cachedFile, String url,
					String fileCachedPath, AjaxStatus callback, long requestId) {
				Message msg = handler.obtainMessage();
				msg.what = msg_type_invoke_js_fill_img;
				Bundle data = msg.getData();
				data.putLong(key_request_id, requestId);
				data.putString(key_file_cach_path, fileCachedPath);
				handler.sendMessage(msg);
			}
		};
		aquery.ajax(url, File.class, ajaxCallback);
	}

	private PageDriverListener mWebDriverListener;

	public interface PageDriverListener {
		public void onImageClick(long pageId, int position);

		public void onHrefClick(String url);
	}

	public void setWebDriverListener(PageDriverListener l) {
		mWebDriverListener = l;
	}

	private final LinkedList<Object[]> jsInvokeQuere = new LinkedList<Object[]>();
	private boolean isWaitingJsInvoke = false;// 防止WebView刚刚启动加载，状态值并没有变成loading

	private void addJsInvokeToQeuer(long id, String path) {
		jsInvokeQuere.offer(new Object[] { id, path });
		checkJsInvokeQuere();
	}

	/**
	 * 当webviewFinisLoad的时候要检查是否有等待调用的js队列，如果有就調用
	 */
	private void checkJsInvokeQuere() {
		if (isWaitingJsInvoke) {
			if (DEBUG) {
				log("checkJsInvokeQuere and isWaitingJsInvoke:"
						+ isWaitingJsInvoke + " queue size:"
						+ jsInvokeQuere.size());
			}
			return;
		} else {
			tryInvokeNextJavaScript();
		}
	}

	/**
	 * 尝试从队列里读取JS Task并执行,如果队列是空则不执行任何操作
	 */
	private void tryInvokeNextJavaScript() {
		if (DEBUG) {
			log("poll--->tryInvokeNextJavaScript start");
		}
		String path = null;
		Long reqId = Long.MIN_VALUE;
		boolean isPollOk = false;
		while (!isPollOk && !jsInvokeQuere.isEmpty()) {
			try {
				Object[] objs = jsInvokeQuere.poll();
				if (objs != null && objs.length == 2) {
					reqId = (Long) objs[0];
					path = (String) objs[1];
				}
			} catch (Exception e) {
				// ignore
			}
			if (DEBUG) {
				log("poll-------> poll data path:" + path + " reqId:" + reqId
						+ " queue size:" + jsInvokeQuere.size());
			}
			if (path == null || reqId == Long.MIN_VALUE) {
				if (reqId != Long.MIN_VALUE) {
					invokeJsFillPic(reqId, HtmlCreator.IMG_DEG_SRC);
					onImgError((int) (reqId - mPageContent.getPageId()));
				}
				path = null;
				reqId = Long.MIN_VALUE;
				isPollOk = false;
			} else {
				isPollOk = true;
				invokeJsFillPic(reqId, path);
				break;
			}
		}
		if (DEBUG) {
			log("poll--->tryInvokeNextJavaScript end");
		}
	}

	private long makeRequestId(long pageId, int position) {
		return pageId + position;
	}

	private int requestIdToPosition(long requestId) {
		if (mPageContent == null) {
			return -1;
		}
		return (int) (requestId - mPageContent.getPageId());
	}

	/**
	 * js调用结束
	 * 
	 * @param view
	 * @param url
	 */
	public void onJsInvokeFinished() {
		isWaitingJsInvoke = false;
		tryInvokeNextJavaScript();
	}

	private void invokeJsFillPic(long imgId, String imgLocalPath) {
		isWaitingJsInvoke = true;
		String script = String.format(JS_FILL_IMG, imgId, imgLocalPath);
		mWebView.loadUrl(script);
		if (DEBUG) {
			log("start invoke:" + script);
		}
	}

	private void invokeJsSetTextSize(String textSize) {
		if (textSize == null || textSize.length() <= 0) {
			return;
		}
		isWaitingJsInvoke = true;
		String script = String.format(JS_SET_TEXT_SIZE, textSize);
		mWebView.loadUrl(script);
		if (DEBUG) {
			log("start invoke:" + script);
		}
	}

	Runnable onJsInvokeFinishedRunnable = new Runnable() {

		@Override
		public void run() {
			onJsInvokeFinished();
		}
	};

	private void showLoadingImg(long pageId, int posistion) {
		invokeJsFillPic(pageId + posistion, HtmlCreator.IMG_LOGDING_SRC);
	}

	// private int webTextSize = 20;// px
	// private int webTextColor = Color.BLACK;
	// private int webBgColor = Color.WHITE;

	// ----------------------------------------------------js invoke function
	/**
	 * px,或者其他css支持的尺寸.例如"10px","15px"
	 */
	@JavascriptInterface
	public String getTextSize() {
		// return webTextSize + "px";
		return "20";
	}

	/**
	 * #FFFFFF....
	 * 
	 * @return
	 */
	@JavascriptInterface
	public String getTextColor() {
		return "#000000";
	}

	/**
	 * #FFFFFF....
	 * 
	 * @return
	 */
	@JavascriptInterface
	public String getBackgroudColor() {
		return "#FFFFFF";
	}

	@JavascriptInterface
	public String getPageHtmlContent() {
		return mPageHtmlContent;
	}

	private final HashSet<Integer> errorImgPosition = new HashSet<Integer>();
	private final HashSet<Long> wattingTaskRequstIdRecord = new HashSet<Long>();

	@JavascriptInterface
	public void onImageClick(long pageId, int position) {
		Message msg = handler.obtainMessage(msg_type_item_click);
		Bundle data = msg.getData();
		data.putLong(key_page_id, pageId);
		data.putInt(key_img_position, position);
		handler.sendMessage(msg);
	}

	@JavascriptInterface
	public void onFillPicFinish(String imgId) {
		Message msg = handler.obtainMessage(msg_type_fill_img_success);
		Bundle data = msg.getData();
		data.putString(key_img_id, imgId);
		handler.sendMessage(msg);
	}

	/**
	 * 当HTML中的IMG标签的position加载图片失败的时候会触发onError事件，这时候时候我们会调用这个方法,来做相关的操作
	 * 
	 * @param position
	 */
	@JavascriptInterface
	public void onImgError(int position) {
		Message msg = handler.obtainMessage(msg_type_on_img_error);
		Bundle data = msg.getData();
		data.putInt(key_img_position, position);
		handler.sendMessage(msg);
	}

	private static final int msg_type_item_click = 1;
	private static final int msg_type_fill_img_success = 2;
	private static final int msg_type_on_img_error = 3;
	private static final int msg_type_invoke_js_fill_img = 4;
	//
	private static final String key_page_id = "k_page_id";
	private static final String key_img_id = "k_img_id";
	private static final String key_img_position = "k_img_position";
	private static final String key_request_id = "k_request_id";
	private static final String key_file_cach_path = "k_file_cache_path";
	//
	private Callback handlerCallback = new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = msg.peekData();
			if (data == null) {
				return true;
			}
			switch (msg.what) {
			case msg_type_item_click:
				doOnImageClick(data.getLong(key_page_id),
						data.getInt(key_img_position));
				break;
			case msg_type_fill_img_success:
				doOnFillPicFinish(data.getString(key_img_id));
				break;
			case msg_type_on_img_error:
				doOnImgError(data.getInt(key_img_position));
				break;
			case msg_type_invoke_js_fill_img:
				long req_id = data.getLong(key_request_id);
				addJsInvokeToQeuer(req_id, data.getString(key_file_cach_path));
			}
			return true;
		}
	};

	// ----------------JS_action_from_handler---------------
	public void doOnImageClick(long pageId, int position) {
		if (DEBUG) {
			log("pageId:" + pageId + "  position:" + position);
		}
		if (errorImgPosition.contains(position)) {
			// 判断图片是否加载失败了，如果失败了则重新加
			if (mPageContent != null && mPageContent.isImageType(position)) {
				// 显示加载中的GIF图片
				showLoadingImg(pageId, position);
				//
				String url = mPageContent.getContentByPosition(position);
				long reqId = mPageContent.getPageId() + position;
				loadImage(mWebView.getContext(), url, reqId);
				errorImgPosition.remove(position);
				if (DEBUG) {
					log("ignore pic click and start reload error pic...");
				}
			}
		} else {
			if (mWebDriverListener != null
					&& !wattingTaskRequstIdRecord.contains(makeRequestId(
							pageId, position))) {
				mWebDriverListener.onImageClick(pageId, position);
			}
		}
	}

	public void doOnFillPicFinish(String imgId) {
		if (DEBUG) {
			log("onFillPicFinish imgId:" + imgId);
		}
		try {
			wattingTaskRequstIdRecord.remove(Long.parseLong(imgId));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mWebView.post(onJsInvokeFinishedRunnable);
	}

	public void doOnImgError(int position) {
		if (DEBUG) {
			log("onImgError position:" + position);
		}
		errorImgPosition.add(position);
	}

	void log(String msg) {
		Log.d(TAG, "-->" + msg);
	}
}
