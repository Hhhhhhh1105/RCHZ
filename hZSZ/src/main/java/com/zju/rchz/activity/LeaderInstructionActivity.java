package com.zju.rchz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.exutils.ImgUtils;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.model.District;
import com.zju.rchz.model.LeaderIntructionDataRes;
import com.zju.rchz.model.River;
import com.zju.rchz.model.RiverDataRes;
import com.zju.rchz.model.RiverListRes;
import com.zju.rchz.model.RiverQuickSearchRes;
import com.zju.rchz.model.RiverSearchDataRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ObjUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.ResUtils;
import com.zju.rchz.utils.StrUtils;
import com.zju.rchz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZJTLM4600l on 2018/5/10.
 * 领导新建批示
 */

public class LeaderInstructionActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, TextView.OnEditorActionListener {
    //district的包装函数
    class DistrictWarper {
        public District district;
        public boolean checked;

        public DistrictWarper(District district) {
            super();
            this.district = district;
            this.checked = false;
        }
    }

    private List<LeaderInstructionActivity.DistrictWarper> dwItems = new ArrayList<LeaderInstructionActivity.DistrictWarper>();
    private SimpleListAdapter dwAdapter = null; //adapter需要在后面

    private List<River> rivers = new ArrayList<River>();
    private SimpleListAdapter riversAdapter = null;

    private ListViewWarp listViewWarp = null;
    //是否是书记
    boolean isSecretary;
    //是否是市长
    boolean isMayor;
    //是否是分管市长
    boolean isOtherMayor;
    //是否是镇街总河长
    boolean isBossChief;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchriver);
//        setTitle(R.string.leaderinstruction);
        initHead(R.drawable.ic_head_back, 0);

        //是否是书记
        isSecretary = getUser().isLogined() && getUser().isSecretary();
        //是否是市长
        isMayor = getUser().isLogined() && getUser().isMayor();
        //是否是分管市长
        isOtherMayor = getUser().isLogined() && getUser().isOtherMayor();
        //是否是镇街总河长
        isBossChief = getUser().isLogined() && getUser().isBossChief();


        final View.OnClickListener oneRiverProblemsList = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LeaderInstructionActivity.this, OneRiverNumOfProblemListActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
                intent.putExtra("isOneRiver",true);
                startActivity(intent);
            }
        };

        findViewById(R.id.ll_keyword).setVisibility(View.GONE);

        //河道列表的适配器
        riversAdapter = new SimpleListAdapter(this, rivers, new SimpleViewInitor() {

            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(context, R.layout.item_river_leaderintruction, null);
                }
                River river = (River) data;
                ((TextView) convertView.findViewById(R.id.tv_name)).setText(river.riverName);   //河流名字
                ((TextView) convertView.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLittleLevel(river.riverLevel)); //河流区县等级
                String img = StrUtils.getImgUrl(river.getImgUrl());
                ImgUtils.loadImage(LeaderInstructionActivity.this, ((ImageView) convertView.findViewById(R.id.iv_picture)), img); //河流的图片

                if(river.numOfProblem == 0){
                    ((TextView)convertView.findViewById(R.id.tv_num_leaderintructions)).setText(0+"");
                    ((TextView)convertView.findViewById(R.id.tv_num_leaderintructions)).setTextColor(context.getResources().getColor(R.color.green));
                }else {
                    ((TextView)convertView.findViewById(R.id.tv_num_leaderintructions)).setText(river.numOfProblem+"");
                    ((TextView)convertView.findViewById(R.id.tv_num_leaderintructions)).setTextColor(context.getResources().getColor(R.color.red));
                }

                //当不指定区划时，显示河道所在区
                if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
                    ((TextView) (convertView.findViewById(R.id.tv_distname))).setText(river.districtName);
                    (convertView.findViewById(R.id.tv_distname)).setVisibility(View.VISIBLE);
                } else {
                    (convertView.findViewById(R.id.tv_distname)).setVisibility(View.GONE);
                }

                //一般都是goRiver
                convertView.setOnClickListener(oneRiverProblemsList);
                convertView.setTag(river);
                return convertView;
            }
        });

        //滑动河道列表时的操作函数
        listViewWarp = new ListViewWarp(this, riversAdapter, new ListViewWarp.WarpHandler() {

            @Override
            public boolean onRefresh() {
                //批示列表的个性化显示
                if(isSecretary || isMayor || isOtherMayor){
                    return startSearch(true);
                }else if (isBossChief) {
                    return startBossChiefSearch(true);
                }else {
                    return startCityChiefSearch(true);
                }
            }

            @Override
            public boolean onLoadMore() {
//                return startSearch(false);
                //批示列表的个性化显示
                if(isSecretary || isMayor || isOtherMayor){
                    return startSearch(false);
                }else if (isBossChief) {
                    return startBossChiefSearch(true);
                }else {
                    return startCityChiefSearch(true);
                }
            }
        });

        ((LinearLayout) findViewById(R.id.ll_contain)).addView(listViewWarp.getRootView());

        //区划的适配器
        dwAdapter = new SimpleListAdapter(LeaderInstructionActivity.this, dwItems, new SimpleViewInitor() {

            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                if (convertView == null) {
                    convertView = LinearLayout.inflate(context, R.layout.item_keyword, null);
                }
                LeaderInstructionActivity.DistrictWarper dw = (LeaderInstructionActivity.DistrictWarper) data;

                ((CheckBox) convertView).setText(dw.district.districtName); //view是一个checkBox

                ((CheckBox) convertView).setChecked(dw.checked); //默认都是false
                ((CheckBox) convertView).setTag(dw);
                ((CheckBox) convertView).setOnCheckedChangeListener(LeaderInstructionActivity.this);
                return convertView;
            }
        });

        //区划布局
        GridView gl = (GridView) findViewById(R.id.gv_areas);
        gl.setAdapter(dwAdapter);

        // add in 1.2
        if (getIntent().getIntExtra(Tags.TAG_CODE, 0) == Tags.CODE_SELECTRIVER) {
            setTitle(getIntent().getStringExtra(Tags.TAG_TITLE));
            isSelectRiver = true;
        }

        if (getIntent().getIntExtra(Tags.TAG_CODE, 0) == Tags.CODE_ALLRIVER) {
            setTitle(getIntent().getStringExtra(Tags.TAG_TITLE));
            isAllRiver = true;
        }

        //批示列表的个性化显示
        if(isSecretary || isMayor || isOtherMayor){
            setTitle(R.string.leaderinstruction);
            findViewById(R.id.gv_areas).setVisibility(View.VISIBLE);

            getRequestContext().add("riverquicksearch_data_get", new Callback<RiverQuickSearchRes>() {
                @Override
                public void callback(RiverQuickSearchRes o) {
                    if (o != null && o.isSuccess()) {
                        //第一行第一列的“不限”
                        dwItems.clear();
                        District ds = new District();
                        ds.districtId = 0;
                        ds.districtName = getString(R.string.unlimeited);
                        curDw = new LeaderInstructionActivity.DistrictWarper(ds);
                        dwItems.add(curDw);

                        for (District d : o.data.districtLists) {
                            LeaderInstructionActivity.DistrictWarper dw = new LeaderInstructionActivity.DistrictWarper(d); //id + name
                            dwItems.add(dw);
					/*	 if (curDw == null && d.districtName.contains("上城")) {
						 curDw = dw;
						 curDw.checked = true;
						 }*/
                            //如果有区划，则显示自身区划。如果无区划，则显示上城区
                            if (getUser().getDistrictId() == d.districtId) {
                                curDw = dw;
                                curDw.checked = true;
                            } else if(getUser().getDistrictId() == 0) {
                                curDw = new LeaderInstructionActivity.DistrictWarper(o.data.districtLists[0]);
                                curDw.checked = true;
                            }
                        }
                        if (curDw != null)
                            startSearch(true);
                        // curDw = dwItems.get(0);
                        // curDw.checked = true;
                        dwAdapter.notifyDataSetInvalidated();
                    }
                }
            }, RiverQuickSearchRes.class, ParamUtils.freeParam(null));
        }else if (isBossChief) {
            setTitle(R.string.bossChiefInstruction);
            findViewById(R.id.gv_areas).setVisibility(View.GONE);
            startBossChiefSearch(true);
        }else {
            setTitle(R.string.cityChiefInstruction);
            findViewById(R.id.gv_areas).setVisibility(View.GONE);
            startCityChiefSearch(true);
        }
    }

    private boolean isSelectRiver = false;
    private boolean isAllRiver = false; //查看所有河道

    private LeaderInstructionActivity.DistrictWarper curDw = null;

    private final int DefaultPageSize = 10;

    /**
     * 分页时需要传入的参数
     * @param refresh
     * @return
     */
    protected JSONObject getPageParam(boolean refresh) {
        return refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (rivers.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
    }

    /**
     * 搜索函数
     * @param refresh
     * @return
     */
    private boolean startSearch(final boolean refresh) {
        JSONObject p = null;
        if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
            //若没有选择区划
            p = ParamUtils.freeParam(getPageParam(refresh), "searchDistrictId", 0);
        } else {
            //若选择了区划，则增加区划id
            p = ParamUtils.freeParam(getPageParam(refresh), "searchDistrictId", curDw.district.districtId);
        }
        //展示进度条
        showOperating();

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("Get_RiverAndNumOfProblem", new Callback<LeaderIntructionDataRes>() {
            @Override
            public void callback(LeaderIntructionDataRes o) {
                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess() && o.data != null && o.data.riverAndNumOfProblems != null) {
                    if (refresh)
                        rivers.clear();
                    for (River r : o.data.riverAndNumOfProblems) {
                        rivers.add(r);
                    }

                    riversAdapter.notifyDataSetChanged();
                }
                hideOperating();
                if (rivers.size() == 0) {
                    showToast(R.string.no_searched_river);
                }
                if ((o != null && o.data != null && o.data.riverAndNumOfProblems != null && o.data.pageInfo != null) && (rivers.size() >= o.data.pageInfo.totalCounts || o.data.riverAndNumOfProblems.length == 0)) {
                    //没有更多河道了
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, LeaderIntructionDataRes.class, p);
        return true;
    }

    //市级河长的所有河道以及问题的搜寻
    private boolean startCityChiefSearch(final boolean refresh) {
        JSONObject p = null;

        p = ParamUtils.freeParam(null);
//        p = ParamUtils.freeParam(null,"UUID",getUser().getUuid());
        //展示进度条
        showOperating();

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("Get_CityChiefRiverAndProblem", new Callback<LeaderIntructionDataRes>() {
            @Override
            public void callback(LeaderIntructionDataRes o) {
                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess() && o.data != null && o.data.riverAndNumOfProblems != null) {
                    if (refresh)
                        rivers.clear();
                    for (River r : o.data.riverAndNumOfProblems) {
                        rivers.add(r);
                    }

                    riversAdapter.notifyDataSetChanged();
                }
                hideOperating();
                if (rivers.size() == 0) {
                    showToast(R.string.no_searched_river);
                }
                if ((o != null && o.data != null && o.data.riverAndNumOfProblems != null && o.data.pageInfo != null) && (rivers.size() >= o.data.pageInfo.totalCounts || o.data.riverAndNumOfProblems.length == 0)) {
                    //没有更多河道了
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, LeaderIntructionDataRes.class, p);
        return true;
    }

    //镇街总河长所河道及问题
    private boolean startBossChiefSearch(final boolean refresh) {
        JSONObject p = null;
        if(getUser().districtId!=0){
            p = ParamUtils.freeParam(getPageParam(refresh), "searchDistrictId", getUser().districtId);
        }else {
            p = ParamUtils.freeParam(getPageParam(refresh), "searchDistrictId", 100);
            showToast("没有区划信息。");
        }

        //展示进度条
        showOperating();

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("Get_RiverAndNumOfProblem", new Callback<LeaderIntructionDataRes>() {
            @Override
            public void callback(LeaderIntructionDataRes o) {
                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess() && o.data != null && o.data.riverAndNumOfProblems != null) {
                    if (refresh)
                        rivers.clear();
                    for (River r : o.data.riverAndNumOfProblems) {
                        rivers.add(r);
                    }

                    riversAdapter.notifyDataSetChanged();
                }
                hideOperating();
                if (rivers.size() == 0) {
                    showToast(R.string.no_searched_river);
                }
                if ((o != null && o.data != null && o.data.riverAndNumOfProblems != null && o.data.pageInfo != null) && (rivers.size() >= o.data.pageInfo.totalCounts || o.data.riverAndNumOfProblems.length == 0)) {
                    //没有更多河道了
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, LeaderIntructionDataRes.class, p);
        return true;
    }

    @SuppressWarnings("unused")
    private void startSearchOld(final boolean refresh) {
        JSONObject p = null;
        if (curDw == null || curDw.district == null || curDw.district.districtId == 0) {
            p = ParamUtils.freeParam(null);
        } else {
            p = ParamUtils.freeParam(null, "searchDistrictId", curDw.district.districtId);
        }
        if(isMayor||isOtherMayor||isSecretary){
            showOperating();
            getRequestContext().add("riversearch_data_get", new Callback<RiverListRes>() {
                @Override
                public void callback(RiverListRes o) {
                    if (o != null && o.isSuccess()) {
                        if (refresh)
                            rivers.clear();
                        for (River r : o.data) {
                            rivers.add(r);
                        }

                        riversAdapter.notifyDataSetChanged();
                    }
                    hideOperating();
                    if (rivers.size() == 0) {
                        showToast(R.string.no_searched_river);
                    }
                }
            }, RiverListRes.class, p);
        }

    }

    /**
     * 区划选择
     * @param arg0
     * @param arg1
     */
    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if (arg1) {
            curDw = (LeaderInstructionActivity.DistrictWarper) arg0.getTag();
            for (LeaderInstructionActivity.DistrictWarper d : dwItems) {
                d.checked = false;
            }
            curDw.checked = true;
            dwAdapter.notifyDataSetChanged();

            //选择区划区划之后则开始搜索
            startSearch(true);
        }
        // ((EditText) findViewById(R.id.et_keyword)).requestFocus();
        //当选择区划之后则通过InputMethodManager隐藏键盘
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
        }
    }

    /**
     * 在EditText输入后，不点击Button进行请求，而是直接点击软键盘上的"回车"，那么也应该能够正常响应请求
     * @param arg0
     * @param actId 搜索键
     * @param arg2
     * @return
     */
    @Override
    public boolean onEditorAction(TextView arg0, int actId, KeyEvent arg2) {
        if (actId == EditorInfo.IME_ACTION_SEARCH) {
            startSearch(true);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (riversAdapter != null) {
            riversAdapter.notifyDataSetInvalidated();
            riversAdapter.notifyDataSetChanged();
        }
    }

}
