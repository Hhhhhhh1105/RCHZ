package com.zju.rchz.activity;

import android.os.Bundle;

import com.zju.rchz.R;

public class ViewItActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getIntent().getIntExtra("id", 0));
		initHead(R.drawable.ic_head_back, R.drawable.ic_head_share);
	}
}
