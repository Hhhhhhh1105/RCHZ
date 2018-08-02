package com.zju.rchz.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.activity.LakeSugOrComtActivity;
import com.zju.rchz.activity.MainActivity;
import com.zju.rchz.activity.SugOrComtActivity;
import com.zju.rchz.model.BottomAnimDialog;


/**
 * 投诉公示
 * 
 * @author Robin
 * 
 */
public class PublicityFragment extends BaseFragment implements OnCheckedChangeListener {

	PublicityListFragment listFragment = new PublicityListFragment();
	PublicityMapFragment mapFragment = new PublicityMapFragment();
	TabRankingFragment rankingFragment = new TabRankingFragment();

	boolean isMainPage = false;

	//DH



//	private ViewPager view_pager;
//	private LinearLayout ll_dotGroup;
//	private TextView newsTitle;
//	private int imgResIds[]=new int[]{R.drawable.pictureShowA,R.drawable.pictureShowB,R.drawable.pictureShowC,R.drawable.pictureShowD,R.drawable.pictureShowE,
//			R.drawable.pictureShowF,R.drawable.pictureShowH,R.drawable.pictureShowI};
//	private int curIndex = 0;
//	PicsAdapter picsAdapter;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_publicity, container, false);
//			setViewPager();

			isMainPage = (getBaseActivity() instanceof MainActivity);

			ImageView myImage=(ImageView)rootView.findViewById(R.id.iv_head_left);
			myImage.setVisibility(View.GONE);

			TextView myHeadText=rootView.findViewById(R.id.tv_head_title);
			myHeadText.setText("投诉建议");



//			((RadioGroup) rootView.findViewById(R.id.rg_headtab)).setOnCheckedChangeListener(this);
			//设置右上方的图标
			//getRootViewWarp().setHeadImage(R.drawable.ic_head_search, R.drawable.ic_head_refresh);
//			replaceFragment(listFragment);
//			rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {4
//					if (islist && !isRank) {
//						listFragment.onHeadRefresh();
//					} else if(!islist && !isRank){
//						mapFragment.onHeadRefresh();
//					}else if (isRank){
//						rankingFragment.showAreaPop();
//					}
//
//				}
//			});

			//一键投诉按钮的设置MainActivity.getCurActivity().getUser().isLogined()
			rootView.findViewById(R.id.btn_comp).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (MainActivity.getCurActivity().checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行投诉。")){
						final BottomAnimDialog dialog = new BottomAnimDialog(getBaseActivity(), "投诉河道", "投诉湖泊", "取消");
						dialog.setClickListener(new BottomAnimDialog.BottonAnimDialogListener() {
							@Override
							public void onItem1Listener() {
								// TO_DO
								Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
								intent.putExtra(Tags.TAG_ABOOLEAN, true);
								startActivity(intent);
								dialog.dismiss();
							}

							@Override
							public void onItem2Listener() {
								// TO_DO
								Intent intent = new Intent(getBaseActivity(), LakeSugOrComtActivity.class);
								intent.putExtra(Tags.TAG_ABOOLEAN, true);
								startActivity(intent);
								dialog.dismiss();
							}

							@Override
							public void onItem3Listener() {
								dialog.dismiss();
							}
						});

						dialog.show();
//						Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
//						intent.putExtra(Tags.TAG_ABOOLEAN, true);
//						startActivity(intent);
					}
				}
			});

			//一键建议按钮的设置
			rootView.findViewById(R.id.btn_sug).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (MainActivity.getCurActivity().checkUserAndLogin("请到个人中心进行注册或登录，使用个人账号登录后再进行建议。")){
//						Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
//						intent.putExtra(Tags.TAG_ABOOLEAN, false);
//						startActivity(intent);
						final BottomAnimDialog dialog = new BottomAnimDialog(getBaseActivity(), "建议河道", "建议湖泊", "取消");
						dialog.setClickListener(new BottomAnimDialog.BottonAnimDialogListener() {
							@Override
							public void onItem1Listener() {
								// TO_DO
								Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
								intent.putExtra(Tags.TAG_ABOOLEAN, false);
								startActivity(intent);
								dialog.dismiss();
							}

							@Override
							public void onItem2Listener() {
								// TO_DO
								Intent intent = new Intent(getBaseActivity(), LakeSugOrComtActivity.class);
								intent.putExtra(Tags.TAG_ABOOLEAN, false);
								startActivity(intent);
								dialog.dismiss();
							}

							@Override
							public void onItem3Listener() {
								dialog.dismiss();
							}
						});

						dialog.show();

					}
				}
			});


//			if (!isMainPage) {
//				getRootViewWarp().setHeadImage(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
//				// ((TextView)
//				// rootView.findViewById(R.id.tv_head_title)).setText("所有断面");
//
//			}
//			getRootViewWarp().setHeadTitle("投诉公示");
//
//			rootView.findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					if (!isMainPage) {
//						getBaseActivity().finish();
//					}else {
//						//跳转至搜索界面
//						startActivity(new Intent(getBaseActivity(), SearchCompActivity.class));
//					}
//				}
//			});
		}
		return rootView;
	}

//	private void setViewPager() {
//		view_pager  = (ViewPager) rootView.findViewById(R.id.view_pager);
////		ll_dotGroup = (LinearLayout) rootView.findViewById(R.id.dotgroup);
//		picsAdapter = new PicsAdapter();// 创建适配器  
//		picsAdapter.setData(imgResIds);
//		view_pager.setAdapter(picsAdapter);// 设置适配器  
//		view_pager.setOnPageChangeListener(new MyPageChangeListener()); //设置页面切换监听器  
//		initPoints(imgResIds.length); // 初始化图片小圆点  
//		startAutoScroll();// 开启自动播放  
//		}
//
//	// 初始化图片轮播的小圆点和目录  
//	private void initPoints(int count) {
//		for (int i = 0; i < count; i++) {
//			ImageView iv = new ImageView(MainActivity.getCurActivity());
//			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
//			params.setMargins(0,0,20,0);
//			iv.setLayoutParams(params);
//			iv.setImageResource(R.drawable.dot1);
//			ll_dotGroup.addView(iv);
//			}
//		   ((ImageView)ll_dotGroup.getChildAt(curIndex)).setImageResource(R.drawable.dot2);
//
//	}
//
//	//自动播放
//	private void startAutoScroll() {
//		ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();// 每隔4秒钟切换一张图片  
//		scheduledExecutorService.scheduleWithFixedDelay(new ViewPagerTask(), 5, 4, TimeUnit.SECONDS);
//	}
//
//	//切换图片任务
//	private class ViewPagerTask implements Runnable {
//		@Override
//	public void run() {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				int count = picsAdapter.getCount();
//				view_pager.setCurrentItem((curIndex + 1) % count);
//				}
//			});
//		}
//	}
//
//	// 定义ViewPager控件页面切换监听器  
//	class MyPageChangeListener implements OnPageChangeListener {
//		@Override
//		public void onPageScrolled(int position, float positionOffset,int positionOffsetPixels) {
//		}
//
//		@Override
//		public void onPageSelected(int position) {
//			ImageView imageView1 = (ImageView)ll_dotGroup.getChildAt(position);
//			ImageView imageView2 = (ImageView)ll_dotGroup.getChildAt(curIndex);
//			if (imageView1 != null) {
//				imageView1.setImageResource(R.drawable.dot2);
//			}
//			if (imageView2 != null){
//				imageView2.setImageResource(R.drawable.dot1);
//			}
//			curIndex=position;
//		}
//
//		boolean b =false;//为例实现末尾后返回到第一张 
//
//		@Override
//		public void onPageScrollStateChanged(int state){
//
//		}
//	}
//
//	class PicsAdapter extends PagerAdapter{
//		private List<ImageView> views=new ArrayList<ImageView>();
//
//		@Override
//		public int getCount() {
//			if (views==null){
//				return 0;
//			}
//			return views.size();
//		}
//
//		public  void setData(int[] imgResIds){
//			for (int i=0;i<imgResIds.length;i++){
//				ImageView iv=new ImageView(MainActivity.getCurActivity());
//				ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//				iv.setLayoutParams(params);
//				iv.setScaleType(ImageView.ScaleType.FIT_XY);
//
//				iv.setImageResource(imgResIds[i]);
//				views.add(iv);
//			}
//		}
//		public  Object getItem(int position){
//			if (position<getCount())
//				return views.get(position);
//			return null;
//		}
//
//		@Override
//		public boolean isViewFromObject(View agr0,Object agr1){
//			return agr0==agr1;
//		}
//
//		@Override
//		public void destroyItem(View container, int position, Object object){
//			if ((position<views.size()))
//				((ViewPager)container).removeView(views.get(position));
//		}
//
//		@Override
//		 public int getItemPosition(Object object){
//			return  views.indexOf(object);
//		}
//		@Override
//		public Object instantiateItem(View container, int position){
//			if (position<views.size()){
//				final ImageView imageView=views.get(position);
//				((ViewPager)container).addView(imageView);
//				return views.get(position);
//			}
//			return null;
//		}
//	}

	boolean islist = false;
	boolean isRank = false;

	//取id为add是因为后续增加了个tab，但自己太懒了，不想改原来right的逻辑
	@Override
	public void onCheckedChanged(RadioGroup rg, int rdid) {
		switch (rdid) {
		case R.id.rb_head_left:
			replaceFragment(listFragment);
			getRootViewWarp().setHeadImage(R.drawable.ic_head_search, R.drawable.ic_head_refresh);//若为最新投诉，左上角设置为搜索图标
			break;
		case R.id.rb_head_right:
			replaceFragment(mapFragment);
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
			break;
		case R.id.rb_head_add:
			replaceFragment(rankingFragment);
			getRootViewWarp().setHeadImage(0, R.drawable.ic_head_order);
			break;
		default:
			break;
		}
	}

	Fragment curFragment = null;

	private void replaceFragment(Fragment newFragment) {
		islist = newFragment == listFragment;//islist为true时，listFragment加载数据
		isRank = newFragment == rankingFragment;
		//开启fragment事务
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (!newFragment.isAdded()) {
			if (curFragment == null) {
				//使用当前Fragment的布局替代id为fl_section_container的控件
				transaction.replace(R.id.fl_section_container, newFragment).commit();
			} else {
				transaction.hide(curFragment).add(R.id.fl_section_container, newFragment).commit();
			}
		} else {
			if (curFragment != null)
				transaction.hide(curFragment);
			transaction.show(newFragment);
			//提交事务
			transaction.commit();
		}
		curFragment = newFragment;
	}
}
