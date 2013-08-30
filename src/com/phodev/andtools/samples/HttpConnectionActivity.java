package com.phodev.andtools.samples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.phodev.andtools.R;
import com.phodev.andtools.conn.ConnectionHelper;
import com.phodev.andtools.conn.ConnectionHelper.RequestReceiver;

public class HttpConnectionActivity extends Activity implements OnClickListener {

	private EditText urlBox;
	private TextView resultTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_conn);
		findViewById(R.id.submit).setOnClickListener(this);
		findViewById(R.id.local_source_hint).setOnClickListener(this);
		urlBox = (EditText) findViewById(R.id.url_box);
		resultTv = (TextView) findViewById(R.id.request_result);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.submit:
			requestData();
			break;
		case R.id.local_source_hint:
			urlBox.setText("http://10.0.2.2:8080/");
			break;
		}
	}

	private void requestData() {
		resultTv.setText("loading...");
		String url = urlBox.getText().toString();
		ConnectionHelper conn = ConnectionHelper.obtainInstance();
		conn.httpGet(url, 0, rr);
	}

	private RequestReceiver rr = new RequestReceiver() {

		@Override
		public void onResult(int resultCode, int requestId, String rawResponses) {
			if (resultCode == RESULT_STATE_OK) {
				resultTv.setText(rawResponses);
			} else {
				resultTv.setText("error");
			}
		}
	};

}
