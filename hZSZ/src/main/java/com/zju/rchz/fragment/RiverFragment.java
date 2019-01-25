package com.zju.rchz.fragment;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.activity.MainActivity;
import com.zju.rchz.activity.SearchRiverActivity;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.adapter.SimplePagerAdapter;
import com.zju.rchz.fragment.river.RiverInfoItem;
import com.zju.rchz.fragment.river.RiverInfoPubItem;
import com.zju.rchz.fragment.river.RiverPolicyItem;
import com.zju.rchz.fragment.river.RiverPositionItem;
import com.zju.rchz.fragment.river.RiverQualityItem;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.River;
import com.zju.rchz.model.RiverListRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.StrUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */

public class RiverFragment extends BaseFragment implements OnCheckedChangeListener, OnPageChangeListener {
	BaseFragment curFragment = null;

	private void replaceFragment(BaseFragment newFragment) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (curFragment != null)
			curFragment.whenVisibilityChanged(false);
		if (!newFragment.isAdded()) {
			if (curFragment == null) {
				transaction.replace(R.id.container, newFragment).commit();
			} else {
				transaction.hide(curFragment).add(R.id.container, newFragment).commit();
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
	private BaseFragment lakeFragment = null; //跳转至湖



	enum ShowRiverType {
		DZGSP, YHYC, HDSZ, HDFW, TSXX
	}

	public static void showToastCenter(Context context, String toastStr) {
		Toast toast = Toast.makeText(context.getApplicationContext(), toastStr, Toast.LENGTH_SHORT);
		int tvToastId = Resources.getSystem().getIdentifier("message", "id", "android");
		TextView tvToast = ((TextView) toast.getView().findViewById(tvToastId));
		if(tvToast != null){
			tvToast.setGravity(Gravity.CENTER);
		}
		toast.show();
	}
	class RiverPagerItem extends PagerItem {
		private River river;
		private RiverInfoItem infoItem = null;
		private RiverQualityItem qualityItem = null;
		private RiverPolicyItem policyItem = null;
		private RiverPositionItem positionItem = null;
		// private RiverCompItem compItem = null;
		private RiverInfoPubItem infoPubItem = null;

		private ShowRiverType showRiverType = ShowRiverType.DZGSP;
		private LinearLayout layout = null;

		public RiverPagerItem(River river) {
			super();
			this.river = river;
		}

		/*
		* 更新河道信息下的各个子页面
		* */
		public void refreshChildView() {
			layout.removeAllViews();
			switch (showRiverType) {
			case DZGSP:
				if (infoItem == null)
					infoItem = new RiverInfoItem(river, getBaseActivity(), new BaseActivity.BooleanCallback() {
						@Override
						public void callback(boolean b) {
							pagerItems.clear();
							for (River r : rivers) {
								pagerItems.add(new RiverPagerItem(r));
							}
							adapter.notifyDataSetChanged();
							refreshTitle();
						}
					});
				layout.addView(infoItem.getView());
				break;
			case YHYC:
				if (policyItem == null)
					policyItem = new RiverPolicyItem(river, getBaseActivity());
				layout.addView(policyItem.getView());
				break;
			case HDSZ:
				if (qualityItem == null)
					qualityItem = new RiverQualityItem(river, getBaseActivity());
				layout.addView(qualityItem.getView());
				break;
			case HDFW:
				if (positionItem == null)
					positionItem = new RiverPositionItem(river, getBaseActivity());
				layout.addView(positionItem.getView());
				break;
			case TSXX:
				// if (compItem == null)
				// compItem = new RiverCompItem(river, getBaseActivity());
				// layout.addView(compItem.getView());

				if (infoPubItem == null)
					infoPubItem = new RiverInfoPubItem(river, getBaseActivity());
				layout.addView(infoPubItem.getView());
				break;
			}
			if (rivers==null||rivers.size()==0){
//				Toast toast=new Toast(MainActivity.getCurActivity());
//				toast.makeText(MainActivity.getCurActivity(),"暂无收藏的河道信息。\n请点击右上角搜索河道并收藏！",Toast.LENGTH_LONG).show();
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				showToastCenter(MainActivity.getCurActivity(),"暂无收藏的河道信息。\n请点击右上角搜索河道并收藏！");
			}
		}

		/*
		* 这个函数其实是多余的 并无实际 作用
		* */

		/*
		* 这个函数并不是多余的 items.get(Position)得到的是RiverPagerItem对象
		* */
		@Override
		public View getView() {
			if (layout == null) {
				layout = new LinearLayout(getBaseActivity());
				refreshChildView();
			}
			return layout;
		}

		public void switchShow(ShowRiverType show) {
			if (this.showRiverType != show) {
				this.showRiverType = show;
				refreshChildView();
			}
		}

		public ShowRiverType getShowRiverType() {
			return showRiverType;
		}
	}

	private List<River> rivers = null;
	private List<River> aa = null;
	private River[] riversArr=null;

	private List<PagerItem> pagerItems = new ArrayList<PagerItem>();
	private SimplePagerAdapter adapter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			//当rootView为空时创建View
			rootView = inflater.inflate(R.layout.fragment_river, container, false);
			// rivers.addAll(getBaseActivity().getUser().getCollections());
			//rivers = new List<River>();
//			rivers = getBaseActivity().getUser().getCollections();
//			rivers = getBaseActivity().getUser().getRiverSumList();

			rivers = Arrays.asList(getBaseActivity().getUser().riverSum);

			//顶栏左边无图片，右边为search图片
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
			//为RadioGroup绑定监听器
			((RadioGroup) rootView.findViewById(R.id.rg_river_showwith)).setOnCheckedChangeListener(this);
			//为ViewPager绑定监听器
			((ViewPager) rootView.findViewById(R.id.vp_rivers)).setOnPageChangeListener(this);
			//ViewPager的适配器
			adapter = new SimplePagerAdapter(pagerItems) {
				@Override
				public int getItemPosition(Object object) {
					return POSITION_NONE;
				}

				@Override
				public void destroyItem(ViewGroup container, int position, Object object) {
					try {
						//需将object对象强转成View
						container.removeView((View) object);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			((ViewPager) rootView.findViewById(R.id.vp_rivers)).setAdapter(adapter);

			rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					lakeFragment = new LakeFragment();
					MainActivity.islakeFr=true;
					replaceFragment(lakeFragment);
				}
			});

			if (rivers.size() == 0) {
//				showToastCenter(MainActivity.getCurActivity(),"3333333333333");
                showOperating();
                getRequestContext().add("Get_RandomRiver_List", new Callback<RiverListRes>() {
                    @Override
                    public void callback(RiverListRes o) {
                        if (o != null && o.isSuccess()) {

                            // 更新

                            rivers = Arrays.asList(o.data);

                        }

                        hideOperating();
                        pagerItems.clear();
                        for (River r : rivers) {

                            pagerItems.add(new RiverPagerItem(r));
                        }

                        adapter.notifyDataSetChanged();
                        refreshTitle();
                    }
                }, RiverListRes.class, null);


			} else {
				pagerItems.clear();
				for (River r : rivers) {
					pagerItems.add(new RiverPagerItem(r));
				}

				adapter.notifyDataSetChanged();
				refreshTitle();
			}
		}
		if (rivers==null||rivers.size()==0){
			showOperating();
			getRequestContext().add("Get_RandomRiver_List", new Callback<RiverListRes>() {
				@Override
				public void callback(RiverListRes o) {
					if (o != null && o.isSuccess()) {

						// 更新
						rivers = Arrays.asList(o.data);
					}

					hideOperating();
					pagerItems.clear();
					for (River r : rivers) {

						pagerItems.add(new RiverPagerItem(r));
					}

					adapter.notifyDataSetChanged();
					refreshTitle();
				}
			}, RiverListRes.class, null);

		}
		return rootView;
	}

	private void getRivers() {
		showOperating();
		getRequestContext().add("river_list_get", new Callback<RiverListRes>() {

			@Override
			public void callback(RiverListRes o) {
				if (o != null && o.isSuccess()) {
					// 更新
					for (River r : o.data) {
						if (!rivers.contains(r))
							rivers.add(r);
					}

					pagerItems.clear();
					for (River r : rivers) {
						pagerItems.add(new RiverPagerItem(r));
					}
					adapter.notifyDataSetChanged();

					((ViewPager) rootView.findViewById(R.id.vp_rivers)).setCurrentItem(0);
					if (rivers.size() > 0)
						onPageSelected(0);
					refreshTitle();
				}
				hideOperating();
			}
		}, RiverListRes.class, null);

		if (rivers==null||rivers.size()==0){
//			Toast.makeText(MainActivity.getCurActivity(),"暂无收藏的河道信息。\n" +
//					"请点击右上角搜索河道并收藏",Toast.LENGTH_SHORT).show();
//			showToastCenter(MainActivity.getCurActivity(),"???????????????");
		}
	}
	private List<River> getRandRivers(){
		 aa=null;
        showOperating();
        getRequestContext().add("Get_RandomRiver_List", new Callback<RiverListRes>() {
            @Override
            public void callback(RiverListRes o) {
                if (o != null && o.isSuccess()) {
					// 更新
					aa = Arrays.asList(o.data);
				}
                hideOperating();
            }
        }, RiverListRes.class, null);
        return aa;
    }

	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		if (rivers==null||rivers.size()==0){
			 showOperating();
                getRequestContext().add("Get_RandomRiver_List", new Callback<RiverListRes>() {
                    @Override
                    public void callback(RiverListRes o) {
                        if (o != null && o.isSuccess()) {

                            // 更新

                            rivers = Arrays.asList(o.data);

                        }

                        hideOperating();
                        pagerItems.clear();
                        for (River r : rivers) {

                            pagerItems.add(new RiverPagerItem(r));
                        }

                        adapter.notifyDataSetChanged();
                        refreshTitle();
                    }
                }, RiverListRes.class, null);
		}
		if (pagerItems.size() == 0)
			return;

		RiverPagerItem pagerItem = (RiverPagerItem) pagerItems.get(((ViewPager) rootView.findViewById(R.id.vp_rivers)).getCurrentItem());


		switch (rdid) {
		case R.id.rb_river_dzgsp:
			pagerItem.switchShow(ShowRiverType.DZGSP);
			break;
		case R.id.rb_river_yhyc:
			pagerItem.switchShow(ShowRiverType.YHYC);
			break;
		case R.id.rb_river_hdsz:
			pagerItem.switchShow(ShowRiverType.HDSZ);
			break;
		case R.id.rb_river_hdfw:
			pagerItem.switchShow(ShowRiverType.HDFW);
			break;
		case R.id.rb_river_tsxx:
			pagerItem.switchShow(ShowRiverType.TSXX);
			break;
		default:
			break;
		}
	}

	/*
	* 若发生页面滑动时，直接使用河流的基本信息显示类型
	* */
	@Override
	public void onPageSelected(int ix) {
		getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, rivers.get(ix).riverName));
		((RadioButton) rootView.findViewById(R.id.rb_river_dzgsp)).setChecked(true);

		if (ix < pagerItems.size()) {
			RiverPagerItem pagerItem = (RiverPagerItem) pagerItems.get(ix);
			if (pagerItem.getShowRiverType() != ShowRiverType.DZGSP) {
				pagerItem.switchShow(ShowRiverType.DZGSP);
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	private void refreshTitle() {
		if (rootView.findViewById(R.id.vp_rivers) != null) {
			int ix = ((ViewPager) rootView.findViewById(R.id.vp_rivers)).getCurrentItem();
			if (rivers != null && ix >= 0 && ix < rivers.size()) {
				getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, rivers.get(ix).riverName));
			}

		}
	}

	public void whenVisibilityChanged(boolean isVisibleToUser) {
		if (isVisibleToUser && adapter != null && rivers.size() != pagerItems.size()) {

			refreshTitle();

			if (rivers.size() == 0) {
				showOperating();
				getRequestContext().add("Get_RandomRiver_List", new Callback<RiverListRes>() {
					@Override
					public void callback(RiverListRes o) {
						if (o != null && o.isSuccess()) {

							// 更新

							rivers = Arrays.asList(o.data);

						}

						hideOperating();
						pagerItems.clear();
						for (River r : rivers) {

							pagerItems.add(new RiverPagerItem(r));
						}

						adapter.notifyDataSetChanged();
						refreshTitle();
					}
				}, RiverListRes.class, null);
			} else {
				// 更新
				pagerItems.clear();
				for (River r : rivers) {
					pagerItems.add(new RiverPagerItem(r));
				}
				adapter.notifyDataSetChanged();
			}
		}
	}
}
