package com.phodev.andtools.html.pagedriver;

import java.util.ArrayList;

/**
 * WebPage Data
 * 
 * @author sky
 */
public class WebPageContent {
	private ArrayList<String> content = new ArrayList<String>();
	private ArrayList<Integer> imagesIndex = new ArrayList<Integer>();
	private ArrayList<Integer> textsIndex = new ArrayList<Integer>();
	private long id;

	public long getPageId() {
		return id;
	}

	public void setPageId(long id) {
		this.id = id;
	}

	/**
	 * 文章的所有内容列表，改内虽然都是String，但是有可能是Image的URl， 也有可能是Text字符串，可以根据position判断具体的类型
	 * 
	 * @see #isImageType(int)
	 * @see #isTextType(int)
	 */
	public ArrayList<String> getRawContentList() {
		return content;
	}

	/**
	 * 标识哪个位置是Image
	 * 
	 * @return
	 */
	public ArrayList<Integer> getImagesIndex() {
		return imagesIndex;
	}

	/**
	 * 标识哪个位置是Texts
	 * 
	 * @return
	 */
	public ArrayList<Integer> getTextsIndex() {
		return textsIndex;
	}

	/**
	 * 添加图片(可以是多个)
	 * 
	 * @param url
	 */
	public void addImage(String url) {
		if (url != null) {
			int nextIndex = content.size();
			if (content.add(url)) {
				imagesIndex.add(nextIndex);
			}
		}
	}

	/**
	 * 添加正文(可以是多个)
	 * 
	 * @param text
	 */
	public void addTextContent(String text) {
		if (text != null) {
			int nextIndex = content.size();
			if (content.add(text)) {
				textsIndex.add(nextIndex);
			}
		}
	}

	/**
	 * 获取Texts的总共数量
	 * 
	 * @return
	 */
	public int getTextCount() {
		return textsIndex.size();
	}

	/**
	 * 获取图片的总共数量
	 * 
	 * @return
	 */
	public int getImageCount() {
		return imagesIndex.size();
	}

	/**
	 * 判断是否是文本，如果不是文本就是Image的URL
	 * 
	 * @return
	 */
	public boolean isTextType(int position) {
		return !imagesIndex.contains(position);
	}

	/**
	 * 文章内容分为两种，一种是Text，一种是Image的URL
	 * 
	 * @return
	 */
	public boolean isImageType(int position) {
		return imagesIndex.contains(position);
	}

	/**
	 * 根据全局index获取内容，有可能是Text，也有可能是URL
	 * 
	 * @see #imagesIndex
	 * @see #textsIndex
	 * @param globalIndex
	 * @return
	 */
	public String getContentByPosition(int globalIndex) {
		if (globalIndex >= 0 && content != null && globalIndex < content.size()) {
			return content.get(globalIndex);
		} else {
			return null;
		}
	}
}