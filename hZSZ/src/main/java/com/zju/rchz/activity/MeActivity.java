package com.zju.rchz.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.CheckNotifyRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ParamUtils;

import org.json.JSONObject;

public class MeActivity extends BaseActivity {

	private int[] showWhenLogined = { R.id.tv_logout, R.id.rl_complaint, R.id.rl_suggestion };
//	private int[] showWhenChiefLogined = { R.id.rl_chief_sign, R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban, R.id.rl_chief_inspect, R.id.rl_chief_record, R.id.rl_chief_suggestion };
	private int[] showWhenChiefLogined = {R.id.rl_chief_complaint, R.id.rl_chief_record, R.id.rl_chief_suggestion  };
	private int[] showWhenCityChiefLogined = {R.id.rl_chief_record};
	private int[] showWhenCleanerLogined = { R.id.rl_chief_record};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me);
		initHead(R.drawable.ic_head_back, 0);
		setTitle(R.string.mycenter);

		findViewById(R.id.tv_problem_report).setOnClickListener(this);//问题上报
		findViewById(R.id.tv_problem_report_list).setOnClickListener(this);
		findViewById(R.id.tv_complaint).setOnClickListener(this);
		findViewById(R.id.tv_suggestion).setOnClickListener(this);
		findViewById(R.id.tv_collection).setOnClickListener(this);
		findViewById(R.id.tv_setting).setOnClickListener(this);
		findViewById(R.id.tv_about).setOnClickListener(this);
		findViewById(R.id.tv_logout).setOnClickListener(this);
		findViewById(R.id.iv_logo).setOnClickListener(this);
		findViewById(R.id.tv_ducha).setOnClickListener(this);//督察
		findViewById(R.id.tv_ducha_list).setOnClickListener(this);

		findViewById(R.id.tv_chief_complaint).setOnClickListener(this);
		findViewById(R.id.tv_chief_duban).setOnClickListener(this);
		findViewById(R.id.tv_chief_dubanToperson).setOnClickListener(this);//我的督办单,镇街河长
		findViewById(R.id.tv_citychief_dubanToperson).setOnClickListener(this);//我的督办单,市级河长
		findViewById(R.id.tv_leaderDuban_list).setOnClickListener(this);//领导督办
		findViewById(R.id.tv_leaderintruction_list).setOnClickListener(this);//领导督办rl_leaderintruction_list

		findViewById(R.id.tv_chief_inspect).setOnClickListener(this);
		findViewById(R.id.tv_chief_record).setOnClickListener(this);
		findViewById(R.id.tv_chief_suggestion).setOnClickListener(this);
		findViewById(R.id.tv_chief_sign).setOnClickListener(this);
		findViewById(R.id.tv_chief_mail).setOnClickListener(this);
		findViewById(R.id.tv_lakechief_record).setOnClickListener(this);
	}

	private void checkNotify() {
		getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
			@Override
			public void callback(CheckNotifyRes o) {
				if (o != null && o.isSuccess()) {
					// o.data.sumUndealComp = 10;
					// o.data.sumUndealAdv = 5;
					nofityChecked(o.data);

					//投诉信息提醒
					if (o.data.sumUndealComp > 0) {
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setText(o.data.sumUndealComp + "个投诉未处理");
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
					}

					if (o.data.sumUndealAdv > 0) {
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setText(o.data.sumUndealAdv + "个建议未处理");
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
					}

					//督办单信息提醒
					if (o.data.sumUndealDubanToperson>0){
						((TextView) findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setText(o.data.sumUndealDubanToperson + "个督办单未处理");
						((TextView) findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.VISIBLE);
					}else {
						((TextView) findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.GONE);
					}
					if (o.data.sumUnReadMail > 0) {
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setText(o.data.sumUnReadMail + "条未读消息");
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.VISIBLE);
					} else {
						((TextView) findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.GONE);
					}
				}
			}
		}, CheckNotifyRes.class, new JSONObject());
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshView();

		((TextView) findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
		((TextView) findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.GONE);
		if (getUser().isLogined() && getUser().isChief()) {
			checkNotify();
		}
	}

	private void refreshView() {
		boolean logined = getUser().isLogined();
		boolean ischief = getUser().isLogined() && getUser().isChief();
		//判断是否是村级河长 8
		boolean isVillageChief =getUser().isLogined() && getUser().isVillageChief();
		//判断是否是河管员 7
		boolean isCleaner =getUser().isLogined() && getUser().isCleaner();
		//判断是否是协管员 6
		boolean isCoordinator = getUser().isLogined() && getUser().isCoordinator();
		//判断是否是市级或区级河长 区级9 市级10
		boolean isDistrictChief = logined && getUser().isDistrictChief();
		boolean isCityChief = logined && getUser().isCityChief();
		//市级河长联系人
		boolean isCityLinkMan =logined && getUser().isCityLinkMan();
		//督察员13
		boolean isDucha=getUser().isLogined() && getUser().isDucha();
		//是否是领导
		boolean isLeader=getUser().isLogined()&&getUser().isLeader();

		//是否是书记
		boolean isSecretary = getUser().isLogined() && getUser().isSecretary();
		//是否是市长
		boolean isMayor = getUser().isLogined() && getUser().isMayor();
		//是否是分管市长
		boolean isOtherMayor = getUser().isLogined() && getUser().isOtherMayor();
		//是否是镇街总河长
		boolean isBossChief = getUser().isLogined() && getUser().isBossChief();

		for (int id : showWhenLogined) {
			View v = findViewById(id);
			if (v != null)
				v.setVisibility(logined ? View.VISIBLE : View.GONE);
		}
		for (int id : showWhenChiefLogined) {
			View v = findViewById(id);
			if (v != null)
				v.setVisibility((ischief||isVillageChief||isCityChief||isCleaner)? View.VISIBLE : View.GONE);
		}
		if(isCityChief||isVillageChief||isCleaner){
			View v1 = findViewById(R.id.rl_chief_complaint);
			v1.setVisibility(View.GONE);
			View v2 = findViewById(R.id.rl_chief_suggestion);
			v2.setVisibility(View.GONE);
		}

		//河管员签到
		if(isCleaner){
			View v1=findViewById(R.id.rl_chief_sign);
			v1.setVisibility(View.VISIBLE);

		}else {
			View v1=findViewById(R.id.rl_chief_sign);
			v1.setVisibility(View.GONE);
		}

		//协管员
		if(isCoordinator){
			View v1=findViewById(R.id.rl_problem_report);
			v1.setVisibility(View.VISIBLE);
			View v2=findViewById(R.id.rl_problem_report_list);
			v2.setVisibility(View.VISIBLE);
			View v3=findViewById(R.id.rl_chief_record);
			v3.setVisibility(View.VISIBLE);
		}else {
			View v1=findViewById(R.id.rl_problem_report);
			v1.setVisibility(View.GONE);
			View v2=findViewById(R.id.rl_problem_report_list);
			v2.setVisibility(View.GONE);
		}
		//督察员
		if(isDucha){
			View v1=findViewById(R.id.rl_ducha);
			v1.setVisibility(View.VISIBLE);
			View v2=findViewById(R.id.rl_ducha_list);
			v2.setVisibility(View.VISIBLE);

		}else {
			View v1=findViewById(R.id.rl_ducha);
			v1.setVisibility(View.GONE);
			View v2=findViewById(R.id.rl_ducha_list);
			v2.setVisibility(View.GONE);

		}
		//我的督办单，镇街河长
		if(ischief){
			View v1=findViewById(R.id.rl_chief_dubanToperson);
			v1.setVisibility(View.VISIBLE);
		}else {
			View v1=findViewById(R.id.rl_chief_dubanToperson);
			v1.setVisibility(View.GONE);
		}
		//我的督办单，问题上报，市级河长
		if(isCityChief || isCityLinkMan){
			View v1=findViewById(R.id.rl_citychief_dubanToperson);
			v1.setVisibility(View.VISIBLE);
			View v3=findViewById(R.id.rl_problem_report);
			v3.setVisibility(View.VISIBLE);
		}else {
			View v1=findViewById(R.id.rl_citychief_dubanToperson);
			v1.setVisibility(View.GONE);
		}
		//是否是领导isLeader
		if(isSecretary || isMayor || isOtherMayor){
			View v1=findViewById(R.id.rl_leaderintruction_list);
			v1.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tv_leaderintruction_list)).setText("领导批示");
//			Toast.makeText(this,"if",Toast.LENGTH_LONG).show();
		}else if(isCityChief){
			View v1=findViewById(R.id.rl_leaderintruction_list);
			v1.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tv_leaderintruction_list)).setText("市级河长批示");
//			Toast.makeText(this,"if",Toast.LENGTH_LONG).show();
		} else if (isBossChief) {
			View v1=findViewById(R.id.rl_leaderintruction_list);
			v1.setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.tv_leaderintruction_list)).setText("镇街总河长批示");
//			Toast.makeText(this,"if",Toast.LENGTH_LONG).show();
		}else {
			View v1=findViewById(R.id.rl_leaderintruction_list);
			v1.setVisibility(View.GONE);
//			Toast.makeText(this,"else",Toast.LENGTH_LONG).show();
		}

		((TextView) findViewById(R.id.tv_name)).setText(getUser().getDisplayName());
		((TextView) findViewById(R.id.tv_info)).setText(getUser().getDisplayRiver());
//		((TextView) findViewById(R.id.tv_info)).setText("1");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_chief_sign: {
			startActivity(new Intent(this, com.zju.rchz.chief.activity.ChiefSignActivity.class));
			break;
		}
		case R.id.tv_chief_mail: {
			startActivity(new Intent(this, com.zju.rchz.chief.activity.ChiefMailListActivity.class));
			break;
		}
		case R.id.tv_chief_complaint: {
			startActivity(new Intent(this, com.zju.rchz.chief.activity.ChiefCompListActivity.class));
			break;
		}
		//领导督办
			case R.id.tv_leaderDuban_list: {
				Intent intent = new Intent(this,
						com.zju.rchz.activity.DubanTopersonLeaderListActivity.class);
				getUser().isLeaderDuban = 1;
				intent.putExtra("isLeaderDuban",1);
				startActivity(intent);
				break;
			}
			//领导批示
			case R.id.tv_leaderintruction_list: {
				Intent intent = new Intent(this,
						com.zju.rchz.activity.LeaderInstructionActivity.class);
				startActivity(intent);
				break;
			}
		case R.id.tv_chief_suggestion: {
			Intent intent = new Intent(this, com.zju.rchz.chief.activity.ChiefCompListActivity.class);
			intent.putExtra(Tags.TAG_ABOOLEAN, false);
			startActivity(intent);

			break;
		}
		case R.id.tv_chief_inspect:{
			Intent intent = new Intent( this,com.zju.rchz.chief.activity.ChiefInspectActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.tv_chief_record: {
			Intent intent = new Intent(this, com.zju.rchz.chief.activity.ChiefRecordListActivity.class);
			startActivity(intent);
			break;
		}
		//巡湖记录
			case R.id.tv_lakechief_record: {
				Intent intent = new Intent(this, com.zju.rchz.lakechief.activity.LakeChiefRecordListActivity.class);
				startActivity(intent);
				break;
			}
		case R.id.tv_chief_duban: {
			Intent intent = new Intent(this, com.zju.rchz.chief.activity.ChiefDubanListActivity.class);
			startActivity(intent);
			break;
		}
		//我的督办单，镇街河长
			case R.id.tv_chief_dubanToperson: {
				Intent intent = new Intent(this, com.zju.rchz.activity.DubanTopersonListActivity.class);
				getUser().isLeaderDuban = 0;
				intent.putExtra("isLeaderDuban",0);
				startActivity(intent);
				break;
			}
			//我的督办单，市级河长
			case R.id.tv_citychief_dubanToperson: {
				Intent intent = new Intent(this, com.zju.rchz.activity.DubanTopersonCitychiefListActivity.class);
				getUser().isLeaderDuban = 0;
				intent.putExtra("isLeaderDuban",0);
				startActivity(intent);
				break;
			}
		case R.id.tv_complaint: {
			startActivity(new Intent(this, CompListActivity.class));
			break;
		}
		//河管员督办单列表
			case R.id.tv_problem_report_list: {
				Intent intent = new Intent(this, DubanTopersonCoordListActivity.class);
				getUser().isLeaderDuban = 0;
				intent.putExtra("isLeaderDuban",0);
				startActivity(intent);
				break;
			}
			case R.id.tv_problem_report: {
				Intent intent = new Intent(this, ProblemReportActivity.class);
				Bundle bundle=new Bundle();
				bundle.putString("eventFlag", "0");//督察员问题上报（0表示上报人是督察员）
				intent.putExtras(bundle);
				startActivity(intent);
//				startActivity(new Intent(this, ProblemReportActivity.class));
				break;
			}
			case R.id.tv_ducha_list: {
				startActivity(new Intent(this, DuChaListAcitivity.class));
				break;
			}
			case R.id.tv_ducha: {
				startActivity(new Intent(this, DuChaDanActivity.class));
				break;
			}

		case R.id.tv_suggestion: {
			Intent intent = new Intent(this, CompListActivity.class);
			intent.putExtra(Tags.TAG_ABOOLEAN, false);
			startActivity(intent);
			break;
		}
		case R.id.tv_collection: {
			startActivity(new Intent(this, MyCollectActivity.class));
			break;
		}
		case R.id.tv_about: {
			startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		case R.id.tv_setting: {
			startActivity(new Intent(this, SettingActivity.class));
			break;
		}
		case R.id.iv_logo: {
			if (!getUser().isLogined()) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivityForResult(intent, Tags.CODE_LOGIN);
			}
			break;
		}
		case R.id.tv_logout:
			createMessageDialog("提示", "确定要注销登录吗?", "确定", "取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					logout();
				}
			}, null, null).show();
			break;
		default:
			super.onClick(v);
			break;
		}
	}

	private void logout() {
		showOperating();
		getRequestContext().add("User_Logout_Action", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				// if (o != null && o.isSuccess()) {
				getUser().clearInfo();
				refreshView();
				hideOperating();
				// }
			}
		}, BaseRes.class, ParamUtils.freeParam(null, "cid", getLocalService() != null ? getLocalService().getCid() : ""));
	}

	@Override
	public void finish() {
		super.finish(false);
	}
}
