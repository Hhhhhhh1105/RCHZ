package com.zju.rchz.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.adapter.SimplePagerAdapter;
import com.zju.rchz.fragment.river.BaseRiverPagerItem;
import com.zju.rchz.fragment.river.RiverInfoItem;
import com.zju.rchz.fragment.river.RiverPolicyItem;
import com.zju.rchz.fragment.river.RiverQualityItem;
import com.zju.rchz.model.River;
import com.zju.rchz.utils.ArrUtils;
import com.zju.rchz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 河道Activity<br/>
 * 传入参数 Tags.TAG_RIVER: River对象
 * 
 * @author Robin
 * 
 */
public class RiverActivity extends BaseActivity implements OnPageChangeListener, OnCheckedChangeListener {
	private River river = null;
	private List<PagerItem> pagerItems = null;
	private SimplePagerAdapter adapter = null;
	private static final String TAG = "hhh11";

	// private int[] rdids = new int[] { R.id.rb_river_dzgsp,
	// R.id.rb_river_hdsz, R.id.rb_river_yhyc, R.id.rb_river_hdfw };
	//rdids：电子公示牌-基本信息，河道水质，一河一策，信息公开；
	//取消投诉统计
//	private int[] rdids = new int[] { R.id.rb_river_dzgsp, R.id.rb_river_hdsz, R.id.rb_river_yhyc, R.id.rb_river_tsxx };
	private int[] rdids ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_river);

		// initHead(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
		initHead(R.drawable.ic_head_back, 0);

		river = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), River.class);
		if (river != null) {


			setTitle(StrUtils.renderText(this, R.string.fmt_riverinfo, river.riverName));
			pagerItems = new ArrayList<PagerItem>();
			//不知道为什么riverLevel是0
			if((river.riverLevel==3)||(river.riverLevel==0)){
				Log.d(TAG, "Msg "+river.riverLevel);
				pagerItems.clear();
				pagerItems.add(new RiverInfoItem(river, this, null));

				pagerItems.add(new RiverPolicyItem(river, this));
                rdids = new int[] { R.id.rb_river_dzgsp, R.id.rb_river_yhyc};
                findViewById(R.id.rb_river_hdsz).setVisibility(View.GONE);

			}
			else {
				Log.d(TAG, "Msg "+river.riverLevel);
				pagerItems.clear();
				pagerItems.add(new RiverInfoItem(river, this, null));
                pagerItems.add(new RiverQualityItem(river, this));
				pagerItems.add(new RiverPolicyItem(river, this));
                rdids = new int[] { R.id.rb_river_dzgsp, R.id.rb_river_hdsz, R.id.rb_river_yhyc};
                findViewById(R.id.rb_river_hdsz).setVisibility(View.VISIBLE);
			}
//			pagerItems.add(new RiverInfoPubItem(river, this));//DH

			((ViewPager) findViewById(R.id.vp_river_tab)).setOnPageChangeListener(this);
			adapter = new SimplePagerAdapter(pagerItems);
			((ViewPager) findViewById(R.id.vp_river_tab)).setAdapter(adapter);

			((RadioGroup) findViewById(R.id.rg_river_showwith)).setOnCheckedChangeListener(this);

			findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					BaseRiverPagerItem brpi = ((BaseRiverPagerItem) pagerItems.get(((ViewPager) findViewById(R.id.vp_river_tab)).getCurrentItem()));
					brpi.loadData();
				}
			});

			if (pagerItems.size() != rdids.length)
				showToast("Tab和Pager数目不一样");
		} else {
			showToast("没有传入河道参数");
			finish();
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int ix) {
		((RadioButton) findViewById(rdids[ix])).setChecked(true);
	}

	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		int ix = ArrUtils.indexOf(rdids, rdid);
		if (ix >= 0) {
			pagerItems.get(ix).getView();
			((BaseRiverPagerItem) pagerItems.get(ix)).readyView();
			((ViewPager) findViewById(R.id.vp_river_tab)).setCurrentItem(ix);
		}
	}
}
