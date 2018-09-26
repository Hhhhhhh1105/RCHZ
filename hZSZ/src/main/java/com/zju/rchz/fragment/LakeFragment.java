package com.zju.rchz.fragment;


import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.activity.MainActivity;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.adapter.SimplePagerAdapter;
import com.zju.rchz.fragment.lake.LakeInfoItem;
import com.zju.rchz.fragment.lake.LakePolicyItem;
import com.zju.rchz.fragment.lake.LakeQualityItem;
import com.zju.rchz.fragment.river.RiverInfoItem;
import com.zju.rchz.fragment.river.RiverInfoPubItem;
import com.zju.rchz.fragment.river.RiverPolicyItem;
import com.zju.rchz.fragment.river.RiverPositionItem;
import com.zju.rchz.fragment.river.RiverQualityItem;
import com.zju.rchz.model.Lake;

import com.zju.rchz.model.LakeListRes11;
import com.zju.rchz.model.Lake;
import com.zju.rchz.model.RiverListRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.StrUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.zju.rchz.fragment.LakeFragment.ShowLakeType.DZGSP;


public class LakeFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {
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
    private BaseFragment riverFragment = null; //切换到河


    enum ShowLakeType {
        DZGSP, YHYC, HDSZ
//        , HDFW, TSXX
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
    class LakePagerItem extends PagerItem {
        private Lake lake;
        private LakeInfoItem infoItem = null;
        private LakeQualityItem qualityItem = null;
        private LakePolicyItem policyItem = null;
//        private LakePositionItem positionItem = null;
        // private RiverCompItem compItem = null;
//        private LakeInfoPubItem infoPubItem = null;

        private LakeFragment.ShowLakeType showLakeType = DZGSP;
        private LinearLayout layout = null;

        public LakePagerItem(Lake  lake) {
            super();
            this.lake = lake;
        }

        /*
         * 更新河道信息下的各个子页面
         * */
        public void refreshChildView() {
            layout.removeAllViews();
            switch (showLakeType) {
                case DZGSP:
                    if (infoItem == null)
                        infoItem = new LakeInfoItem(lake, getBaseActivity(), new BaseActivity.BooleanCallback() {
                            @Override
                            public void callback(boolean b) {
                                pagerItems.clear();
                                for (Lake r : lakes) {
                                    pagerItems.add(new LakeFragment.LakePagerItem(r));
                                }
                                adapter.notifyDataSetChanged();
                                refreshTitle();
                            }
                        });
                    layout.addView(infoItem.getView());
                    break;
                case YHYC:
                    if (policyItem == null)
                        policyItem = new LakePolicyItem(lake, getBaseActivity());
                    layout.addView(policyItem.getView());
                    break;
                case HDSZ:
                    if (qualityItem == null)
                        qualityItem = new LakeQualityItem(lake, getBaseActivity());
                    layout.addView(qualityItem.getView());
                    break;
//                case HDFW:
//                    if (positionItem == null)
//                        positionItem = new RiverPositionItem(river, getBaseActivity());
//                    layout.addView(positionItem.getView());
//                    break;
//                case TSXX:
//                    // if (compItem == null)
//                    // compItem = new RiverCompItem(river, getBaseActivity());
//                    // layout.addView(compItem.getView());
//
//                    if (infoPubItem == null)
//                        infoPubItem = new RiverInfoPubItem(river, getBaseActivity());
//                    layout.addView(infoPubItem.getView());
//                    break;
            }
            if (lakes==null||lakes.size()==0){
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

        public void switchShow(LakeFragment.ShowLakeType show) {
            if (this.showLakeType != show) {
                this.showLakeType = show;
                refreshChildView();
            }
        }

        public LakeFragment.ShowLakeType getShowLakeType() {
            return showLakeType;
        }
    }

    private List<Lake> lakes = null;
//    private List<Lake> aa = null;
//    private Lake[] riversArr=null;

    private List<PagerItem> pagerItems = new ArrayList<PagerItem>();
    private SimplePagerAdapter adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            //当rootView为空时创建View
            rootView = inflater.inflate(R.layout.fragment_river, container, false);
            // lakes.addAll(getBaseActivity().getUser().getCollections());
            //lakes = new List<Lake>();
//			lakes = getBaseActivity().getUser().getCollections();
//			lakes = getBaseActivity().getUser().getRiverSumList();


            lakes = Arrays.asList(getBaseActivity().getUser().lakeSum);






            //顶栏左边无图片，右边为search图片
            getRootViewWarp().setHeadImage(0, R.drawable.ic_head_refresh);
            //为RadioGroup绑定监听器
            ((RadioGroup) rootView.findViewById(R.id.rg_river_showwith)).setOnCheckedChangeListener(this);
            //为ViewPager绑定监听器
            ((ViewPager) rootView.findViewById(R.id.vp_rivers)).setOnPageChangeListener(this);
            ((RadioButton)rootView.findViewById(R.id.rb_river_hdsz)).setText("湖泊水质");
            ((RadioButton)rootView.findViewById(R.id.rb_river_yhyc)).setText("一湖一策");
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
                    riverFragment = new RiverFragment();
                    MainActivity.islakeFr=false;
                    replaceFragment(riverFragment);
                }
            });

            if (lakes.size() == 0) {
//				showToastCenter(MainActivity.getCurActivity(),"3333333333333");
                showOperating();
                getRequestContext().add("Get_RandomLake_List", new Callback<LakeListRes11>() {
                    @Override
                    public void callback(LakeListRes11 o) {
                        if (o != null && o.isSuccess()) {

                            // 更新

                            lakes = Arrays.asList(o.data);

                        }

                        hideOperating();
                        pagerItems.clear();
                        for (Lake r : lakes) {

                            pagerItems.add(new LakeFragment.LakePagerItem(r));
                        }

                        adapter.notifyDataSetChanged();
                        refreshTitle();
                    }
                }, LakeListRes11.class, null);


            } else {
                pagerItems.clear();
                for (Lake r : lakes) {
                    pagerItems.add(new LakeFragment.LakePagerItem(r));
                }

                adapter.notifyDataSetChanged();
                refreshTitle();
            }
        }
        if (lakes==null||lakes.size()==0){
            showOperating();
            getRequestContext().add("Get_RandomLake_List", new Callback<LakeListRes11>() {
                @Override
                public void callback(LakeListRes11 o) {
                    if (o != null && o.isSuccess()) {

                        // 更新

                        lakes = Arrays.asList(o.data);

                    }

                    hideOperating();
                    pagerItems.clear();
                    for (Lake r : lakes) {

                        pagerItems.add(new LakeFragment.LakePagerItem(r));
                    }

                    adapter.notifyDataSetChanged();
                    refreshTitle();
                }
            }, LakeListRes11.class, null);

        }
        return rootView;
    }




    @Override
    public void onCheckedChanged(RadioGroup rg, int rdid) {
        if (lakes==null||lakes.size()==0){
            showOperating();
            getRequestContext().add("Get_RandomLake_List", new Callback<LakeListRes11>() {
                @Override
                public void callback(LakeListRes11 o) {
                    if (o != null && o.isSuccess()) {

                        // 更新

                        lakes = Arrays.asList(o.data);

                    }

                    hideOperating();
                    pagerItems.clear();
                    for (Lake r : lakes) {

                        pagerItems.add(new LakeFragment.LakePagerItem(r));
                    }

                    adapter.notifyDataSetChanged();
                    refreshTitle();
                }
            }, LakeListRes11.class, null);
        }
        if (pagerItems.size() == 0)
            return;

        LakeFragment.LakePagerItem pagerItem = (LakeFragment.LakePagerItem) pagerItems.get(((ViewPager) rootView.findViewById(R.id.vp_rivers)).getCurrentItem());


        switch (rdid) {
            case R.id.rb_river_dzgsp:
                pagerItem.switchShow(LakeFragment.ShowLakeType.DZGSP);
                break;
            case R.id.rb_river_yhyc:
                pagerItem.switchShow(LakeFragment.ShowLakeType.YHYC);
                break;
            case R.id.rb_river_hdsz:
                pagerItem.switchShow(LakeFragment.ShowLakeType.HDSZ);
                break;
//            case R.id.rb_river_hdfw:
//                pagerItem.switchShow(RiverFragment.ShowRiverType.HDFW);
//                break;
//            case R.id.rb_river_tsxx:
//                pagerItem.switchShow(RiverFragment.ShowRiverType.TSXX);
//                break;
            default:
                break;
        }
    }

    /*
     * 若发生页面滑动时，直接使用河流的基本信息显示类型
     * */
    @Override
    public void onPageSelected(int ix) {
        getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, lakes.get(ix).lakeName));
        ((RadioButton) rootView.findViewById(R.id.rb_river_dzgsp)).setChecked(true);

        if (ix < pagerItems.size()) {
            LakeFragment.LakePagerItem pagerItem = (LakeFragment.LakePagerItem) pagerItems.get(ix);
            if (pagerItem.getShowLakeType() != LakeFragment.ShowLakeType.DZGSP) {
                pagerItem.switchShow(LakeFragment.ShowLakeType.DZGSP);
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
            if (lakes != null && ix >= 0 && ix < lakes.size()) {
                getRootViewWarp().setHeadTitle(StrUtils.renderText(getBaseActivity(), R.string.fmt_riverinfo, lakes.get(ix).lakeName));
            }

        }
    }

    public void whenVisibilityChanged(boolean isVisibleToUser) {
        if (isVisibleToUser && adapter != null && lakes.size() != pagerItems.size()) {

            refreshTitle();

            if (lakes.size() == 0) {
                showOperating();
                getRequestContext().add("Get_RandomLake_List", new Callback<LakeListRes11>() {
                    @Override
                    public void callback(LakeListRes11 o) {
                        if (o != null && o.isSuccess()) {

                            // 更新

                            lakes = Arrays.asList(o.data);

                        }

                        hideOperating();
                        pagerItems.clear();
                        for (Lake r : lakes) {

                            pagerItems.add(new LakeFragment.LakePagerItem(r));
                        }

                        adapter.notifyDataSetChanged();
                        refreshTitle();
                    }
                }, LakeListRes11.class, null);
            } else {
                // 更新
                pagerItems.clear();
                for (Lake r : lakes) {
                    pagerItems.add(new LakeFragment.LakePagerItem(r));
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}
