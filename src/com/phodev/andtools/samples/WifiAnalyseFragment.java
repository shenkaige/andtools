package com.phodev.andtools.samples;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.wifianalyse.WifiChannelView;
import com.phodev.andtools.wifianalyse.WifiDrawEntity;
import com.phodev.andtools.wifianalyse.WifiEntityProvider;
import com.phodev.andtools.wifianalyse.WifiEntityProvider.WifiEntityHolder;

@SimpleDesc(title="WifiAnalyse")
public class WifiAnalyseFragment extends InnerFragment implements WifiEntityHolder {
	private WifiChannelView view;
	private WifiEntityProvider p;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(p==null){
			p = new WifiEntityProvider(getActivity());
			p.setWifiEntityHolder(this);
		}
		p.start();
	}



	@Override
	public void onDetach() {
		super.onDetach();
		if(p!=null){
			p.stop();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null){
			view =  new WifiChannelView(getActivity());
		}
		return view;
	}

	@Override
	public void onWifiEntitysUpdate(List<ScanResult> results) {
		List<WifiDrawEntity> array = new ArrayList<WifiDrawEntity>();
		for (ScanResult r : results) {
			WifiDrawEntity en = new WifiDrawEntity(r);
			array.add(en);
		}
		view.refreshScanResult(array);
	}



}
