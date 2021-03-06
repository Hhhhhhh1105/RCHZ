package com.zju.rchz.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.sin.android.sinlibs.utils.MD5Utils;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.model.LoginRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;

public class LoginActivity extends BaseActivity {
	AutoCompleteTextView Atv_username=null;
	EditText et_password = null;
    private ArrayAdapter<String> arrayAdapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.login);

		findViewById(R.id.tv_forgot).setOnClickListener(this);
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_register).setOnClickListener(this);

		Atv_username = (AutoCompleteTextView) findViewById(R.id.Atv_username);
		et_password = (EditText) findViewById(R.id.et_password);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login: {
			String username = Atv_username.getText().toString().trim();
			String passwrod = et_password.getText().toString().trim();
			// if(et_username.getText().toString())
			if (username.length() == 0) {
				showToast(R.string.tip_username_empty);
				Atv_username.requestFocus();
			} else if (passwrod.length() == 0) {
				showToast(R.string.tip_password_empty);
				et_password.requestFocus();
			} else {


				loginWitdhUP(username, passwrod);
			}
			break;
		}
		case R.id.btn_register: {
			startActivityForResult(new Intent(this, RegisterActivity.class), Tags.CODE_REG);
			break;
		}
		case R.id.tv_forgot: {
			startActivity(new Intent(this, ForgotPasswordActivity.class));
			// startActivityForResult(intent, requestCode);
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	private void loginWitdhUP(final String username, String passwrod) {
		final String pwdmd5 = MD5Utils.calcMD5(passwrod);
		showOperating();
		getUser().uuid = null;
		getRequestContext().add("action_user_load", new Callback<LoginRes>() {
			@Override
			public void callback(LoginRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					showToast("登录成功");
					getUser().userName = username;
					getUser().uuid = o.data.uuid;
					getUser().authority = o.data.authority;
					getUser().status=o.data.status;
					getUser().riverSum = o.data.riverSum;
					getUser().lakeSum = o.data.lakeSum;
					getUser().ifOnJob = o.data.ifOnJob;
					getUser().districtId = o.data.districtId;
					getUser().realName = o.data.realName;
					getUser().statusCity=o.data.statusCity;
					getUser().isLeader=o.data.isLeader;
					getUser().pwdmd5 = pwdmd5;
					getUser().authorityForIntruction = o.data.authorityForIntruction;
					saveLocalData();
					SharedPreferences.Editor editor=getSharedPreferences("login_name",MODE_PRIVATE).edit();


					setResult(RESULT_OK);
					finish();
					if (getLocalService() != null) {
						saveLocalData();
						getLocalService().notifyableChanged();
					}
				}
			}
		}, LoginRes.class, ParamUtils.freeParam(null, "userName", username, "password", pwdmd5, "cid", getLocalService() != null ? getLocalService().getCid() : ""));
	}

	@Override
	public void finish() {
		super.finish(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Atv_username != null && !StrUtils.isNullOrEmpty(getUser().userName)) {
            Atv_username.setText(getUser().userName);
		}
		if (Atv_username != null) {
			if (Values.hhDEBUG) {
                String [] arr={"13370902388镇河","18606305918镇湖","13906710701镇河湖","17662702683河督","17662702851湖督","17662702861河湖督"};
                arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arr);
//				et_username.setText("13706811460");
               // Atv_username.setText("18857100011");
//				et_username.setText("13705813320");
				//et_password.setText("HZHZ123456");
                Atv_username.setAdapter(arrayAdapter);
                et_password.setText("123456");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Tags.CODE_REG && resultCode == RESULT_OK && getUser().isLogined()) {
			setResult(RESULT_OK);
			finish();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
//    String [] arr={"aa","aab","aac"};
//        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arr);
//        autotext.setAdapter(arrayAdapter);