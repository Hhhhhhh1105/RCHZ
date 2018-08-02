package com.zju.rchz.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.adapter.SimplePagerAdapter;
import com.zju.rchz.chief.activity.DuabnTopersonDetailActivity;
import com.zju.rchz.clz.ViewWarp;
import com.zju.rchz.model.ChiefCompListRes;
import com.zju.rchz.model.DubanTopersonObject;
import com.zju.rchz.net.Callback;
import com.zju.rchz.net.Constants;
import com.zju.rchz.utils.ArrUtils;
import com.zju.rchz.utils.DipPxUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;
import com.zju.rchz.view.ListViewWarp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZJTLM4600l on 2018/7/31.
 */

public class LakeDubanTopersonListActivity extends BaseActivity implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener{
    public class CompListPager extends PagerItem implements ListViewWarp.WarpHandler {
        private View view = null;
        private int type = 0;
        private ListViewWarp listViewWarp = null;
        //        private List<ChiefComp> items = new ArrayList<ChiefComp>();
        private List<DubanTopersonObject> items=new ArrayList<DubanTopersonObject>();
        private SimpleListAdapter adapter = null;

        //处理投诉单按钮
        private View.OnClickListener btnClk = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof DubanTopersonObject) {
                    DubanTopersonObject comp = (DubanTopersonObject) v.getTag();
                    Intent intent = new Intent(LakeDubanTopersonListActivity.this,
                            DuabnTopersonDetailActivity.class);

                    intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
                    intent.putExtra(Tags.TAG_HANDLED, type != 0);    //0是未处理，1是已处理

                    startActivityForResult(intent, Tags.CODE_COMP);
                }
            }
        };

        private SimpleViewInitor initor = new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position,
                                 View convertView, ViewGroup parent, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(
                            LakeDubanTopersonListActivity.this,
                            R.layout.item_chief_complaint2, null);
                }
                ViewWarp warp = new ViewWarp(convertView,
                        LakeDubanTopersonListActivity.this);
                DubanTopersonObject comp = (DubanTopersonObject) data;
                warp.setText(R.id.tv_status,comp.getNewMark(TransformNewstate(comp.getNewState())));  //处理状态
                warp.setText(R.id.tv_sernum, comp.getSerNum());   //投诉单编号
                warp.setText(R.id.tv_title, comp.getTheme());   //投诉单标题
                warp.setText(R.id.tv_content, comp.getContent());  //投诉单内容
                if (comp.getDeadline()!=null&&comp.getDeadline()!=""){
                    warp.setText(R.id.tv_deal_deadline,"处理期限："+comp.getDeadline());  //处理期限
                }
                warp.setText(R.id.tv_time, comp.getTime() != null ? comp
                        .getTime().getYMDHM(LakeDubanTopersonListActivity.this) : "");    //投诉时间

                if (type == 0) {
                    ((Button) warp.getViewById(R.id.btn_handle))
                            .setText("处理督办单");         //0是未处理，1是已处理
                } else {
                    ((Button) warp.getViewById(R.id.btn_handle))
                            .setText("查看督办单");
                }
                ((Button) warp.getViewById(R.id.btn_handle)).setTag(comp);
                ((Button) warp.getViewById(R.id.btn_handle))
                        .setOnClickListener(btnClk);
                return convertView;
            }
        };

        //将返回的状态转变为汉语标志在数组中的位置，方便显示
        public int TransformNewstate(Integer new_state){
            int statePosition=1;
            switch (new_state.intValue()){
                case 0:
                    statePosition = 1;
                    break;
                case 20:
                    statePosition = 2;
                    break;
                case 21:
                    statePosition = 3;
                    break;
                case 30:
                    statePosition = 4;
                    break;
                case 40:
                    statePosition = 5;
                    break;
                case 41:
                    statePosition = 6;
                    break;
                case 50:
                    statePosition = 7;
                    break;
                case 60:
                    statePosition = 8;
                    break;
                default: break;
            }
            return statePosition;
        }
        //CompListPager类的构造函数
        public CompListPager(int type) {
            super();
            this.type = type;
        }

        @Override
        public View getView() {
            if (view == null) {
                view = LinearLayout.inflate(LakeDubanTopersonListActivity.this,
                        R.layout.confs_chief_complist, null);
                adapter = new SimpleListAdapter(LakeDubanTopersonListActivity.this,
                        items, initor);
                listViewWarp = new ListViewWarp(LakeDubanTopersonListActivity.this,
                        adapter, this);
                listViewWarp.getListView().setDivider(
                        new ColorDrawable(getResources().getColor(
                                R.color.bg_gray)));
                listViewWarp.getListView().setDividerHeight(
                        DipPxUtils.dip2px(
                                LakeDubanTopersonListActivity.this,
                                getResources().getDimension(
                                        R.dimen.padding_medium)));
                ((LinearLayout) view.findViewById(R.id.ll_main))
                        .addView(listViewWarp.getRootView());

                loadComps(true);
            }
            return view;
        }

        //下拉刷新
        @Override
        public boolean onRefresh() {
            loadComps(true);
            return true;
        }

        //上拉加载
        @Override
        public boolean onLoadMore() {
            loadComps(false);
            return true;
        }

        private void loadComps(final boolean refresh) {
            if (refresh)
                listViewWarp.setRefreshing(true);
            else
                listViewWarp.setLoadingMore(true);
            getRequestContext().add(
                    "Get_LakeDubanToperson_List",
                    new Callback<ChiefCompListRes>() {
                        @Override
                        public void callback(ChiefCompListRes o) {
                            listViewWarp.setRefreshing(false);
                            listViewWarp.setLoadingMore(false);

                            if (o != null && o.isSuccess()) {
                                if (refresh)
                                    items.clear();
                                for (DubanTopersonObject c : o.data.dubanTopersonObject) {
                                    items.add(c);
                                }
                                adapter.notifyDataSetChanged();

                                //无更多
                                if (items.size() >= o.data.pageInfo.totalCounts) {
                                    listViewWarp.setNoMore(true);
                                }
                            }
                        }
                    }, ChiefCompListRes.class, getPageParam(refresh));
        }

        private final int DefaultPageSize = Constants.DefaultPageSize;

        protected JSONObject getPageParam(boolean refresh) {
            JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1)
                    : ParamUtils.pageParam(DefaultPageSize, (items.size()
                    + DefaultPageSize - 1)
                    / DefaultPageSize + 1);
            try {
                j.put("ifDeal", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return j;
        }
    }

    private int[] rdids = new int[] { R.id.rb_chief_comp_unhandle,
            R.id.rb_chief_comp_handled };
    private List<PagerItem> pagerItems = null;
    private SimplePagerAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_complist);

        setTitle(R.string.dubandan);
        Button button_handling=(Button)findViewById(R.id.rb_chief_comp_unhandle);
        button_handling.setText("待查验");
        Button button_handled = (Button)findViewById(R.id.rb_chief_comp_handled);
        button_handled.setText("已处置");
        initHead(R.drawable.ic_head_back, 0);

        ((ViewPager) findViewById(R.id.vp_chief_comp_tab))
                .setOnPageChangeListener(this);
        ((RadioGroup) findViewById(R.id.rg_chief_comp))
                .setOnCheckedChangeListener(this);

        pagerItems = new ArrayList<PagerItem>();
        pagerItems.add(new CompListPager(0));
        pagerItems.add(new CompListPager(1));
        adapter = new SimplePagerAdapter(pagerItems);
        ((ViewPager) findViewById(R.id.vp_chief_comp_tab)).setAdapter(adapter);
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
        // ((CompListPager) pagerItems.get(((ViewPager)
        // findViewById(R.id.vp_chief_comp_tab)).getCurrentItem())).readyView();
    }

    @Override
    public void onCheckedChanged(RadioGroup rg, int rdid) {
        int ix = ArrUtils.indexOf(rdids, rdid);
        if (ix >= 0) {
            ((ViewPager) findViewById(R.id.vp_chief_comp_tab))
                    .setCurrentItem(ix);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            ((CompListPager) pagerItems
                    .get(((ViewPager) findViewById(R.id.vp_chief_comp_tab))
                            .getCurrentItem())).loadComps(true);
        }
    }

}
