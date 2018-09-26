package com.zju.rchz.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.mapapi.SDKInitializer;
import com.sin.android.update.ToolBox;
import com.tencent.open.utils.Global;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.fragment.BaseFragment;
import com.zju.rchz.fragment.LakeFragment;
import com.zju.rchz.fragment.MainFragment;
import com.zju.rchz.fragment.MeFragment;
import com.zju.rchz.fragment.NewsFragment;
import com.zju.rchz.fragment.PublicityFragment;
import com.zju.rchz.fragment.RiverFragment;
import com.zju.rchz.fragment.SectionFragment;
import com.zju.rchz.fragment.TabRankingFragment;
import com.zju.rchz.model.IndexData;
import com.zju.rchz.model.IndexDataRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.receiver.PushReceiver;
import com.zju.rchz.utils.ImgUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;

public class MainActivity extends BaseActivity implements OnCheckedChangeListener {

	private BaseFragment sectionFragment = null;
	private BaseFragment newsFragment = null; //新闻动态
	private BaseFragment mainFragment = null; //首页
	private BaseFragment riverFragment = null; //河道信息
	private BaseFragment rankingFragment = null;
	private BaseFragment publicityFragment = null; //投诉公示
	private BaseFragment meFragment = null; //个人中心
	private BaseFragment lakeFragment=null;

	public static boolean islakeFr=false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext()); //百度地图的初始化方法

		// E5:0B:1F:E1:72:C3:C6:EA:49:5B:B0:FF:F7:59:0E:BA:04:C2:57:24;com.zju.hzsz
		// E5:0B:1F:E1:72:C3:C6:EA:49:5B:B0:FF:F7:59:0E:BA:04:C2:57:24;com.zju.hzsz

		// r: 486bc747f888533fb56cb754b804adf1

		//

		setContentView(R.layout.activity_main);

		initHead(R.drawable.ic_head_plus, R.drawable.ic_head_share);

		((RadioGroup) findViewById(R.id.rg_main)).setOnCheckedChangeListener(this);

		sectionFragment = new SectionFragment();
		newsFragment = new NewsFragment();
		mainFragment = new MainFragment();
		riverFragment = new RiverFragment();
		lakeFragment=new LakeFragment();

		// rankingFragment = new RankingFragment();
		rankingFragment = new TabRankingFragment();
		publicityFragment = new PublicityFragment();
		meFragment = new MeFragment();

		replaceFragment(mainFragment);

		new ToolBox(this).checkUpdate(!Values.RELEASE ? "rchz" : "rchz", Values.Ver);
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		Log.i(getTag(), "rd: " + rdid);
		switch (rdid) {
		case R.id.rd_shouye:
			replaceFragment(mainFragment);
			break;
		case R.id.rd_duanmian:
			replaceFragment(sectionFragment);
			break;
		case R.id.rd_xinwen:
			replaceFragment(newsFragment);
			break;
		case R.id.rd_hedao:

			replaceFragment(riverFragment);
			break;
		case R.id.rd_panhang:
			replaceFragment(meFragment);
			break;
		case R.id.rd_publicity:
			replaceFragment(publicityFragment);
			break;
		default:
			break;
		}
	}

	public BaseFragment curFragment = null;

	private void replaceFragment(BaseFragment newFragment) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (curFragment != null)
			curFragment.whenVisibilityChanged(false);
		if (!newFragment.isAdded()) {
			if (curFragment == null) {
				transaction.replace(R.id.container, newFragment).commit();
			} else {
				transaction.replace(R.id.container, newFragment).commit();
//				transaction.hide(curFragment).add(R.id.container, newFragment).commit();
			}
		} else {
			if (curFragment != null)
				transaction.hide(curFragment);
			transaction.show(newFragment);
			transaction.commit();
		}

		curFragment = newFragment;
		curFragment.whenVisibilityChanged(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (curFragment != null)
			curFragment.whenVisibilityChanged(true);

		if (PushReceiver.getPayload() != null) {
			PushReceiver.clearPayLoad();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Tags.CODE_LOGIN && requestCode == RESULT_OK) {
			startActivity(new Intent(this, MyCenterActivity.class), false);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}

		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		checkChiefNotify();
	}

	public void startShare() {
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
//		UMWXHandler wxCircleHandler = new UMWXHandler(this, Values.getWXAppID(), Values.getWXAppSecret());
		UMWXHandler wxCircleHandler = new UMWXHandler(this, "wxd0c565c230eb7625", "341433d103a191072579fa7bb4eec4a4");
		wxCircleHandler.addToSocialSDK();

//		wxCircleHandler = new UMWXHandler(this, Values.getWXAppID(), Values.getWXAppSecret());
		wxCircleHandler = new UMWXHandler(this, "wxd0c565c230eb7625", "341433d103a191072579fa7bb4eec4a4");
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, "1104784285", "4N9UUNraoZvCbUJJ");
		qqSsoHandler.addToSocialSDK();
		//
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, "1104784285", "4N9UUNraoZvCbUJJ");
		qZoneSsoHandler.addToSocialSDK();

		// mController.getConfig().setSsoHandler(new SinaSsoHandler());
		// mController.getConfig().setSsoHandler(new TencentWBSsoHandler());

		// mController.getConfig().removePlatform(SHARE_MEDIA.QZONE,
		// SHARE_MEDIA.TENCENT, SHARE_MEDIA.QQ, SHARE_MEDIA.SINA);

		mController.getConfig().removePlatform(SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT);

		String title = getString(R.string.app_name);
		String shareurl = "http://123.206.204.153:8080/RongChengHeZhang/homepage/home.htm";
		String content = "荣成智慧河道APP：以河长制，促进河长治！";// + shareurl;

		// 设置分享内容
		mController.setShareContent(content);

		mController.setAppWebSite(shareurl);

		mController.setAppWebSite(SHARE_MEDIA.WEIXIN, shareurl);
		mController.setAppWebSite(SHARE_MEDIA.WEIXIN_CIRCLE, shareurl);
		mController.setAppWebSite(SHARE_MEDIA.SINA, shareurl);
		mController.setAppWebSite(SHARE_MEDIA.QQ, shareurl);
		mController.setAppWebSite(SHARE_MEDIA.QZONE, shareurl);

		UMImage img = new UMImage(this, R.drawable.ic_launcher);
		CircleShareContent circleMedia = new CircleShareContent();
		circleMedia.setTitle(content);
		circleMedia.setTargetUrl(shareurl);
		circleMedia.setShareContent(content);
		circleMedia.setShareMedia(img);

		mController.setShareMedia(circleMedia);

		WeiXinShareContent weiXinShareContent = new WeiXinShareContent();
		// weiXinShareContent.setTitle(title);
		weiXinShareContent.setTargetUrl(shareurl);
		weiXinShareContent.setShareContent(content);
		weiXinShareContent.setShareMedia(img);

		mController.setShareMedia(weiXinShareContent);

		QQShareContent qqShareContent = new QQShareContent();
		qqShareContent.setTargetUrl(shareurl);
		qqShareContent.setShareContent(content);
		qqShareContent.setShareImage(img);
		mController.setShareMedia(qqShareContent);

		QZoneShareContent qZoneShareContent = new QZoneShareContent();
		qZoneShareContent.setTargetUrl(shareurl);
		qZoneShareContent.setShareContent(content);
		qZoneShareContent.setShareImage(img);
		mController.setShareMedia(qZoneShareContent);

		mController.openShare(this, false);
	}

	// public void startCheckNotify() {
	// checkChiefNotify();
	// }
}
