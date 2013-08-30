package com.phodev.andtools.common;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 控制EditText输入长度
 * 
 * @author skg
 * 
 */
public class InputLengthControler {
	private EditText mEditText;
	private TextView mHintTextView;
	private final String hint_remain_count = "您还可以输入%s字";
	private final String hint_max_length_msg = "最多只能输入%s字";
	private int MAX_LENGTH = 140;

	public void config(EditText inputBox, int maxLength, TextView lengthHintView) {
		MAX_LENGTH = maxLength;
		mEditText = inputBox;
		mHintTextView = lengthHintView;
		mEditText.addTextChangedListener(watcher);
		updateLengthHint(MAX_LENGTH);
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if (mEditText.getText().length() > MAX_LENGTH) {
				String str = mEditText.getText().toString();
				if (str != null && str.length() > 10) {
					str = str.substring(0, MAX_LENGTH);
					mEditText.setText(str);
					mEditText.setSelection(str.length());
				}
				toast(String.format(hint_max_length_msg, MAX_LENGTH));
			}
			int enableCount = MAX_LENGTH;
			Editable curContent = mEditText.getText();
			if (curContent != null && curContent.length() > 0) {
				enableCount = MAX_LENGTH - curContent.length();
			}
			updateLengthHint(enableCount);
		}
	};

	private void updateLengthHint(int enableCount) {
		if (enableCount < 0) {
			enableCount = 0;
		} else if (enableCount > MAX_LENGTH) {
			enableCount = MAX_LENGTH;
		}
		mHintTextView.setText(String.format(hint_remain_count, enableCount));
	}

	private void toast(String msg) {
		Context context;
		if (msg != null && (context = getContext()) != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private Context getContext() {
		if (mEditText != null) {
			return mEditText.getContext();
		}
		return null;
	}

}
