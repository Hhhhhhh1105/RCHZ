package com.zju.rchz.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.activity.AboutActivity;
import com.zju.rchz.activity.CompListActivity;
import com.zju.rchz.activity.DuChaDanActivity;
import com.zju.rchz.activity.DuChaListAcitivity;
import com.zju.rchz.activity.DubanTopersonCoordListActivity;
import com.zju.rchz.activity.LakeDubanTopersonListActivity;
import com.zju.rchz.activity.LakeProblemReportActivity;
import com.zju.rchz.activity.LoginActivity;
import com.zju.rchz.activity.MyCenterActivity;
import com.zju.rchz.activity.MyCollectActivity;
import com.zju.rchz.activity.ProblemReportActivity;
import com.zju.rchz.activity.ProblemReportListActivity;
import com.zju.rchz.activity.RiverActivity;
import com.zju.rchz.activity.SettingActivity;
import com.zju.rchz.clz.RootViewWarp;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.CheckNotifyRes;
import com.zju.rchz.model.River;
import com.zju.rchz.net.Callback;
import com.zju.rchz.npc.activity.NpcLegalListActivity;
import com.zju.rchz.npc.activity.NpcMyjobActivity;
import com.zju.rchz.npc.activity.NpcRankActivity;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;

import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;

/**
 * 个人中心页面
 * Created by Wangli on 2017/1/18.
 */

public class MeFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = "MeFragment";

    //任何人登录时显示我的投诉、我的建议、注销登录
    private int[] showWhenLogined = { R.id.tv_logout, R.id.rl_complaint, R.id.rl_suggestion,R.id.rl_setting};
    //三级河长、河管员登陆时
    private int[] showWhenChiefLogined = {R.id.rl_chief_complaint, R.id.rl_chief_record, R.id.rl_chief_suggestion };
    //三级湖长登录时显示巡湖记录、处理投诉、处理建议
    private int[] showWhenLakechiefLogined ={R.id.rl_chief_complaint,R.id.rl_lakechief_record,R.id.rl_chief_suggestion};

    private int[] showWhenCityChiefLogined = { R.id.rl_chief_record };

//    private int[] showWhenChiefLogined = { R.id.rl_chief_sign, R.id.rl_chief_mail, R.id.rl_chief_complaint, R.id.rl_chief_duban, R.id.rl_chief_record, R.id.rl_chief_suggestion };
    private int[] showWhenNpcLogined = { R.id.rl_npc_myriver};

    private String userIdentity = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //加载布局
        if (rootView == null){
            rootView = inflater.inflate(R.layout.activity_me, container, false);
            //设置actionBar布局
            RootViewWarp warp = getRootViewWarp();
            warp.setHeadImage(0, 0);
            warp.setHeadTitle(R.string.mycenter);
//            Toast.makeText(getActivity(),"11"+getBaseActivity().getUser().statusCity+"22",Toast.LENGTH_LONG).show();

            rootView.findViewById(R.id.tv_complaint).setOnClickListener(this);
            rootView.findViewById(R.id.tv_suggestion).setOnClickListener(this);
            rootView.findViewById(R.id.tv_collection).setOnClickListener(this);
            rootView.findViewById(R.id.tv_setting).setOnClickListener(this);
            rootView.findViewById(R.id.tv_about).setOnClickListener(this);
            rootView.findViewById(R.id.tv_logout).setOnClickListener(this);
            rootView.findViewById(R.id.iv_logo).setOnClickListener(this);
            rootView.findViewById(R.id.tv_problem_report).setOnClickListener(this);//河道问题上报
            rootView.findViewById(R.id.tv_lakeproblem_report).setOnClickListener(this);//湖泊问题上报
            rootView.findViewById(R.id.tv_problem_report_list).setOnClickListener(this);//河道业务处置
            rootView.findViewById(R.id.tv_lakeproblem_report_list).setOnClickListener(this);//湖泊业务处置
            rootView.findViewById(R.id.tv_chief_complaint).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_duban).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_duban).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_dubanToperson).setOnClickListener(this);//我的督办单.，镇街河长
            rootView.findViewById(R.id.tv_citychief_dubanToperson).setOnClickListener(this);//我的督办单.，市级河长
            rootView.findViewById(R.id.tv_leaderDuban_list).setOnClickListener(this);//领导督办
            rootView.findViewById(R.id.tv_leaderintruction_list).setOnClickListener(this);//领导新建批示
            rootView.findViewById(R.id.tv_history_leaderintruction_list).setOnClickListener(this);//领导历史批示
            rootView.findViewById(R.id.tv_chief_rivermanage).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_notepad).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_inspect).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_record).setOnClickListener(this);
            rootView.findViewById(R.id.tv_lakechief_record).setOnClickListener(this);//巡湖记录
            rootView.findViewById(R.id.tv_chief_suggestion).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_sign).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_mail).setOnClickListener(this);
            rootView.findViewById(R.id.tv_chief_npcsug).setOnClickListener(this);  //代表监督
            rootView.findViewById(R.id.tv_ducha).setOnClickListener(this);//督察
            rootView.findViewById(R.id.tv_ducha_list).setOnClickListener(this);
            rootView.findViewById(R.id.tv_npc_legal).setOnClickListener(this);  //规范法规
            rootView.findViewById(R.id.tv_npc_myriver).setOnClickListener(this);  //我的河道
            rootView.findViewById(R.id.tv_npc_myjob).setOnClickListener(this);  //我的履职
            rootView.findViewById(R.id.tv_npc_comment).setOnClickListener(this);  //履职评价

        }

//        if ( getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isNpc()) {
//            changeNpcLogo();
//            ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("其他投诉");
//            ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("其他建议");
//        } else {
//            ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("我的投诉");
//            ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("我的建议");
//        }

        return rootView;
    }

    private void checkNotify() {
        getRequestContext().add("Get_Notify", new Callback<CheckNotifyRes>() {
            @Override
            public void callback(CheckNotifyRes o) {
                if (o != null && o.isSuccess()) {
                    // o.data.sumUndealComp = 10;
                    // o.data.sumUndealAdv = 5;
                    getBaseActivity().nofityChecked(o.data);

                    //投诉信息提醒
                    if (o.data.sumUndealComp > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setText(o.data.sumUndealComp + "个未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
                    }

                    //代表投诉提醒
                    if (o.data.sumUnDealDeputyComp > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setText(o.data.sumUnDealDeputyComp + "个未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandle_npccomplaint_count)).setVisibility(View.GONE);
                    }
                    //督办单信息提醒
                    if (o.data.sumUndealDubanToperson>0){
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setText(o.data.sumUndealDubanToperson + "个未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.VISIBLE);
                    }else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.GONE);
                    }

                    if (o.data.sumUndealAdv > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setText(o.data.sumUndealAdv + "个未处理");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
                    }

                    if (o.data.sumUnReadMail > 0) {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setText(o.data.sumUnReadMail + "条未读消息");
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView) rootView.findViewById(R.id.tv_chief_unreadmail_count)).setVisibility(View.GONE);
                    }
                }
            }
        }, CheckNotifyRes.class, new JSONObject());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshView();

        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlecomplaint_count)).setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.tv_chief_unhandlesuggestion_count)).setVisibility(View.GONE);
        ((TextView) rootView.findViewById(R.id.tv_chief_unhandleDubanToperson_count)).setVisibility(View.GONE);
        //镇级河湖长可以 查看投诉
        if (getBaseActivity().getUser().isLogined() && (getBaseActivity().getUser().isChief()||getBaseActivity().getUser().isLakeChief())) {
            checkNotify();
        }
    }

    private void refreshView() {
        //判断是否已登录
        boolean logined = getBaseActivity().getUser().isLogined();
        boolean ischief = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isChief();
        //判断是否是村级河长 8
        boolean isVillageChief = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isVillageChief();
        //判断是否是河管员 7
        boolean isCleaner = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isCleaner();
        //判断是否是督察员协管员 6
        boolean isCoordinator = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isCoordinator();
        //市级督察员13
        boolean isDucha=getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isDucha();

        //判断是否是市级或区级河长 区级9 市级10
        boolean isDistrictChief = logined && getBaseActivity().getUser().isDistrictChief();
        boolean isCityChief = logined && getBaseActivity().getUser().isCityChief();
        boolean isNpc = logined && getBaseActivity().getUser().isNpc();
        //市级河长联系人
        boolean isCityLinkMan =logined && getBaseActivity().getUser().isCityLinkMan();
        //是否是领导
        boolean isLeader=getBaseActivity().getUser().isLogined()&&getBaseActivity().getUser().isLeader();

        //是否是书记
        boolean isSecretary = logined && getBaseActivity().getUser().isSecretary();
        //是否是市长
        boolean isMayor = logined && getBaseActivity().getUser().isMayor();
        //是否是分管市长
        boolean isOtherMayor = logined && getBaseActivity().getUser().isOtherMayor();
        //是否是镇街总河长
        boolean isBossChief = logined && getBaseActivity().getUser().isBossChief();
        //管辖河道小bug - 1.4.30
        //是否湖长

        //3个湖长等级标志位------------------------------------

        //是否镇级湖长 2
        boolean isLakechief =getBaseActivity().getUser().isLogined()&&getBaseActivity().getUser().isLakeChief();
        //村级湖长 8
        boolean isVillageLakeChief=getBaseActivity().getUser().isLogined()&&getBaseActivity().getUser().isVillageLakeChief();
        //市级湖长 10
        boolean isCityLakeChief=getBaseActivity().getUser().isLogined()&&getBaseActivity().getUser().isCityLakeChief();
        //胡泊督察员 6
        boolean isLakeCoordinator=getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isLakeCoordinator();
        //湖管员7
        boolean isLakeCleaner = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isLakeCleaner();
        //市级湖长联系人  10
        boolean isLakeCityLinkMan = getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isLakeCityLinkMan();

        //所有人登录时都可以看到 我的建议，我的投诉，注销登录

        getRootViewWarp().setHeadTitle("个人中心");
        //更改底端tab栏为“个人中心”
        ((RadioButton) getBaseActivity().findViewById(R.id.rd_panhang)).setText("个人中心");
        rootView.findViewById(R.id.rl_chief_rivermanage).setVisibility(View.GONE);
        for (int id : showWhenLogined) {
            View v = rootView.findViewById(id);
            if (v != null)
                v.setVisibility(logined ? View.VISIBLE : View.GONE);
        }
        if (!logined){
            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_chief_dubanToperson).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_citychief_dubanToperson).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_problem_report).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_chief_sign).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_problem_report).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_citychief_dubanToperson).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_problem_report_list).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_ducha).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_ducha_list).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.GONE);

            rootView.findViewById(R.id.rl_lakeproblem_report).setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_lakeproblem_report_list).setVisibility(View.GONE);

        }

        //镇级河长显示巡河记录，处理投诉，处理建议，我的督办单
        if(ischief){
            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);

            rootView.findViewById(R.id.rl_chief_dubanToperson).setVisibility(View.VISIBLE);
        }
        //村级河长显示巡河记录
        if(isVillageChief){
            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.VISIBLE);
        }
        //市级河长显示巡河记录，我的督办单，河道问题上报
        if(isCityChief){
            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_citychief_dubanToperson).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_problem_report).setVisibility(View.VISIBLE);

        }
        //河管员显示巡河记录，河长签到
        if(isCleaner){
//            rootView.findViewById(R.id.rl_chief_sign).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_chief_sign)).setText("河管员签到");
            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_chief_record)).setText("河管员巡河");//都叫巡河

        }
        //联络人显示河道问题上报，我的督办单
        if (isCityLinkMan){
            rootView.findViewById(R.id.rl_problem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_citychief_dubanToperson).setVisibility(View.VISIBLE);
        }
        //河督察员显示巡河记录，河道问题上报，河道业务处置
        if (isCoordinator){
            Log.d(TAG, "refreshView: 河督河督河督");


            rootView.findViewById(R.id.rl_chief_record).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_problem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_problem_report_list).setVisibility(View.VISIBLE);
        }
        //市级督察员显示新建督查，我的督查单
        if (isDucha){
            rootView.findViewById(R.id.rl_ducha).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_ducha_list).setVisibility(View.VISIBLE);
        }
        //人大
        if (isNpc){
            changeNpcLogo();
        }
        //镇级湖长显示巡湖记录，处理投诉，处理建议，(湖泊问题上报)，湖泊业务处置
        if (isLakechief){
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);
            //rootView.findViewById(R.id.rl_lakeproblem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report_list).setVisibility(View.VISIBLE);
            ((TextView)rootView.findViewById(R.id.tv_lakeproblem_report_list)).setText("查看湖泊督办单");
        }
        //村级湖长显示巡湖记录，处理投诉，处理建议
        if (isVillageLakeChief){
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);
        }
        //市级湖长显示巡湖记录，湖泊问题上报，湖泊督办单
        if (isCityLakeChief){
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report_list).setVisibility(View.VISIBLE);
            ((TextView)rootView.findViewById(R.id.tv_lakeproblem_report_list)).setText("查看湖泊督办单");
        }
        //湖泊督察员湖泊问题上报，湖泊业务处置,巡湖记录
        if (isLakeCoordinator){
            Log.d(TAG, "refreshView: 湖督湖督湖督0000");
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.VISIBLE);

            rootView.findViewById(R.id.rl_lakeproblem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report_list).setVisibility(View.VISIBLE);
            ( (TextView)rootView.findViewById(R.id.tv_lakeproblem_report_list)).setText("湖泊业务处置");
//            ((TextView)rootView.findViewById(R.id.tv_lakechief_record)).setText("督察员巡湖");
        }
        //湖管员签到、巡湖，投诉建议
        if (isLakeCleaner){
//            rootView.findViewById(R.id.rl_lakechief_sign).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_lakechief_sign)).setText("湖管员签到");
            rootView.findViewById(R.id.rl_lakechief_record).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_lakechief_record)).setText("湖管员巡湖");
        }
        if (isLakeCityLinkMan){

//            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_lakeproblem_report_list).setVisibility(View.VISIBLE);
            ((TextView)rootView.findViewById(R.id.tv_lakeproblem_report_list)).setText("查看湖泊督办单");

        }

//        for(int id:showWhenLakechiefLogined){
//            View v = rootView.findViewById(id);
//            if(v!=null){
//                v.setVisibility((isLakechief||isCityLakeChief||isVillageLakeChief)?View.VISIBLE:View.GONE);
//            }
//        }


//        for (int id : showWhenChiefLogined) {
//            View v =  rootView.findViewById(id);
//            if (v != null)
//                v.setVisibility((ischief||isVillageChief||isCityChief||isCleaner)? View.VISIBLE : View.GONE);
//        }
//        if(isCityChief||isVillageChief||isCleaner){
//            View v1 =  rootView.findViewById(R.id.rl_chief_complaint);
//            v1.setVisibility(View.GONE);
//            View v2 =  rootView.findViewById(R.id.rl_chief_suggestion);
//            v2.setVisibility(View.GONE);
//        }
//        if(isLakechief){
//            rootView.findViewById(R.id.rl_chief_complaint).setVisibility(View.VISIBLE);
//            rootView.findViewById(R.id.rl_chief_suggestion).setVisibility(View.VISIBLE);
//        }
//        //协管员
//        if(isCoordinator){
//            View v1=rootView.findViewById(R.id.rl_problem_report);
//            v1.setVisibility(View.VISIBLE);
//            View v2=rootView.findViewById(R.id.rl_problem_report_list);
//            v2.setVisibility(View.VISIBLE);
//            View v3=rootView.findViewById(R.id.rl_chief_record);
//            v3.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_problem_report);
//            v1.setVisibility(View.GONE);
//            View v2=rootView.findViewById(R.id.rl_problem_report_list);
//            v2.setVisibility(View.GONE);
//        }
//        if(isLakeCoordinator||isLakechief||isCityLakeChief){
//            View v1=rootView.findViewById(R.id.rl_lakeproblem_report);
//            v1.setVisibility(View.VISIBLE);
//            View v2=rootView.findViewById(R.id.rl_lakeproblem_report_list);
//            v2.setVisibility(View.VISIBLE);
////            View v3=rootView.findViewById(R.id.rl_lakechief_record);
////            v3.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_lakeproblem_report);
//            v1.setVisibility(View.GONE);
//            View v2=rootView.findViewById(R.id.rl_lakeproblem_report_list);
//            v2.setVisibility(View.GONE);
//        }
//        //河管员签到
//        if(isCleaner){
//            View v1=rootView.findViewById(R.id.rl_chief_sign);
//            v1.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_chief_sign);
//            v1.setVisibility(View.GONE);
//        }
//        //督察员
//        if(isDucha){
//            View v1=rootView.findViewById(R.id.rl_ducha);
//            v1.setVisibility(View.VISIBLE);
//            View v2=rootView.findViewById(R.id.rl_ducha_list);
//            v2.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_ducha);
//            v1.setVisibility(View.GONE);
//            View v2=rootView.findViewById(R.id.rl_ducha_list);
//            v2.setVisibility(View.GONE);
//        }
//        //我的督办单，镇街河长
//        if (ischief){
//            View v1=rootView.findViewById(R.id.rl_chief_dubanToperson);
//            v1.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_chief_dubanToperson);
//            v1.setVisibility(View.GONE);
//        }
//        //我的督办单，市级河长
//        if (isCityChief ||isCityLinkMan){
//            View v1=rootView.findViewById(R.id.rl_citychief_dubanToperson);
//            v1.setVisibility(View.VISIBLE);
//            View v3=rootView.findViewById(R.id.rl_problem_report);
//            v3.setVisibility(View.VISIBLE);
//        }else {
//            View v1=rootView.findViewById(R.id.rl_citychief_dubanToperson);
//            v1.setVisibility(View.GONE);
//        }
        //批示
        if(isSecretary || isMayor || isOtherMayor){
            View v1=rootView.findViewById(R.id.rl_leaderintruction_list);
            v1.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_history_leaderintruction_list).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_leaderintruction_list)).setText("领导批示");
        }else if(isCityChief){
            View v1=rootView.findViewById(R.id.rl_leaderintruction_list);
            v1.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_history_leaderintruction_list).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_leaderintruction_list)).setText("市级河长批示");
//			Toast.makeText(this,"if",Toast.LENGTH_LONG).show();
        } else if (isBossChief) {
            View v1=rootView.findViewById(R.id.rl_leaderintruction_list);
            v1.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.rl_history_leaderintruction_list).setVisibility(View.VISIBLE);
//            ((TextView)rootView.findViewById(R.id.tv_leaderintruction_list)).setText("镇街总河长批示");
//			Toast.makeText(this,"if",Toast.LENGTH_LONG).show();
        }else {
            View v1=rootView.findViewById(R.id.rl_leaderintruction_list);
            v1.setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_history_leaderintruction_list).setVisibility(View.GONE);
        }

//        //如果是人大代表账号
//        if (isNpc){
//            changeNpcLogo();
//
//        }
//        if (isCleaner) {
//
//            ((TextView)rootView.findViewById(R.id.tv_chief_record)).setText("河管员巡河");
//            ((TextView)rootView.findViewById(R.id.tv_chief_sign)).setText("河管员签到");
//
//        } else if(isCoordinator||isLakeCoordinator){
//            ((TextView)rootView.findViewById(R.id.tv_chief_record)).setText("督察员巡河");
//            ((TextView)rootView.findViewById(R.id.tv_lakechief_record)).setText("督察员巡湖");
//        } else {
//            ((TextView)rootView.findViewById(R.id.tv_chief_record)).setText("巡河记录");
//            ((TextView)rootView.findViewById(R.id.tv_lakechief_record)).setText("巡湖记录");
//            getRootViewWarp().setHeadTitle("个人中心");
//            //更改底端tab栏为“个人中心”
//            ((RadioButton) getBaseActivity().findViewById(R.id.rd_panhang)).setText("个人中心");
//
//            //dh
//            ((TextView)rootView.findViewById(R.id.tv_chief_record)).setText("巡河记录");
//            ((TextView)rootView.findViewById(R.id.tv_chief_sign)).setText("河长签到");
//
//        }

        ((TextView) rootView.findViewById(R.id.tv_name)).setText(getBaseActivity().getUser().getDisplayName());
        //用户身份
        if(isSecretary || isMayor || isOtherMayor){
            userIdentity = "领导";
        }else if (isBossChief){
            userIdentity = "镇街总河长";
        }else{
            if(getBaseActivity().getUser().getStatus() == 2){//河湖
                if(isVillageChief ){
                    userIdentity = "村级河/湖长";
                }else if (ischief){
                    userIdentity = "镇街河/湖长";
                }else if(isCityChief){
                    userIdentity = "市级河/湖长";
                }else if(isCityLinkMan){
                    userIdentity = "市级河/湖长联系人";
                }else if(isCleaner){
                    userIdentity = "河/湖管员";
                }else if (isCoordinator){
                    userIdentity = "督察员";
                }else if(logined){
                    userIdentity = "公众用户";
                }else{
                    userIdentity = " ";
                }
            }else if(getBaseActivity().getUser().getStatus() == 1){//湖泊
                if(isVillageLakeChief ){
                    userIdentity = "村级湖长";
                }else if (isLakechief){
                    userIdentity = "镇街湖长";
                }else if(isCityLakeChief){
                    userIdentity = "市级湖长";
                }else if(isLakeCityLinkMan){
                    userIdentity = "市级湖长联系人";
                }else if(isLakeCleaner){
                    userIdentity = "湖管员";
                }else if (isLakeCoordinator){
                    userIdentity = "督察员";
                }else if(logined){
                    userIdentity = "公众用户";
                }else{
                    userIdentity = " ";
                }
            }else{//河道
                if(isVillageChief){
                    userIdentity = "村级河湖长";
                }else if (ischief){
                    userIdentity = "镇街河长";
                }else if(isCityChief){
                    userIdentity = "市级河长";
                }else if(isCityLinkMan){
                    userIdentity = "市级河长联系人";
                }else if(isCleaner){
                    userIdentity = "河管员";
                }else if (isCoordinator){
                    userIdentity = "督察员";
                }else if(logined){
                    userIdentity = "公众用户";
                }else{
                    userIdentity = " ";
                }
            }
        }
//		((TextView) findViewById(R.id.tv_info)).setText(getUser().getDisplayRiver());
        ((TextView) rootView.findViewById(R.id.tv_info)).setText(userIdentity);
//        ((TextView) rootView.findViewById(R.id.tv_info)).setText(getBaseActivity().getUser().getDisplayRiver());
//        ((TextView) rootView.findViewById(R.id.tv_info)).setText("3");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chief_sign: {
                startActivity(new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefSignActivity.class));
                break;
            }
            case R.id.tv_chief_mail: {
                startActivity(new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefMailListActivity.class));
                break;
            }
            case R.id.tv_chief_complaint: {
                startActivity(new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefCompListActivity.class));
                break;
            }
            case R.id.tv_chief_suggestion: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefCompListActivity.class);
                intent.putExtra(Tags.TAG_ABOOLEAN, false);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_inspect:{
                Intent intent = new Intent( getBaseActivity(),com.zju.rchz.chief.activity.ChiefInspectActivity.class);
                startActivity(intent);
                break;
            }
            //巡河记录
            case R.id.tv_chief_record: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefRecordListActivity.class);
                startActivity(intent);
                break;
            }
            //巡湖记录
            case R.id.tv_lakechief_record: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.lakechief.activity.LakeChiefRecordListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_rivermanage: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefRivermanageActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_npcsug: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefNpcSupActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_duban: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefDubanListActivity.class);
                startActivity(intent);
                break;
            }
            //我的督办单,镇街河长
            case R.id.tv_chief_dubanToperson: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.activity.DubanTopersonListActivity.class);
                intent.putExtra("isLeaderDuban",0);
                startActivity(intent);
                break;
            }
            //我的督办单，市级河长
            case R.id.tv_citychief_dubanToperson: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.activity.DubanTopersonCitychiefListActivity.class);
                getBaseActivity().getUser().isLeaderDuban = 0;
                intent.putExtra("isLeaderDuban",0);
                startActivity(intent);
                break;
            }
            //领导督办
            case R.id.tv_leaderDuban_list: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.activity.DubanTopersonLeaderListActivity.class);
                getBaseActivity().getUser().isLeaderDuban = 1;
                intent.putExtra("isLeaderDuban",1);
                startActivity(intent);
                break;
            }
            //领导新建批示
            case R.id.tv_leaderintruction_list: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.activity.LeaderInstructionActivity.class);
                startActivity(intent);
                break;
            }
            //领导历史批示
            case R.id.tv_history_leaderintruction_list: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.activity.InstrcutionListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.tv_chief_notepad: {
                Intent intent = new Intent(getBaseActivity(), com.zju.rchz.chief.activity.ChiefNotepadActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_npc_legal: {
                startActivity(new Intent(getBaseActivity(), NpcLegalListActivity.class));
                break;
            }
            case R.id.tv_npc_myriver: {
                River river = new River();
                river.riverId = getBaseActivity().getUser().getMyRiverId();
                river.riverName = getBaseActivity().getUser().riverSum[0].riverName;
                Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(river));
                startActivity(intent);
                break;
            }
            case R.id.tv_npc_myjob: {
                Intent intent = new Intent(getBaseActivity(), NpcMyjobActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_npc_comment: {
                Intent intent = new Intent(getBaseActivity(), NpcRankActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.tv_complaint: {
                startActivity(new Intent(getBaseActivity(), CompListActivity.class));
                break;
            }
            //河道问题上报
            case R.id.tv_problem_report: {
                Intent intent = new Intent(getBaseActivity(), ProblemReportActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("eventFlag", "0");//（0是外面的问题上报，1是巡河过程中的问题上报）
                intent.putExtras(bundle);
                startActivity(intent);
//                startActivity(new Intent(getBaseActivity(), ProblemReportActivity.class));
                break;
            }
            //湖泊问题上报
            case R.id.tv_lakeproblem_report:{
                Intent intent = new Intent(getBaseActivity(), LakeProblemReportActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("eventFlag", "0");//（0是外面的问题上报，1是巡河过程中的问题上报）
                intent.putExtras(bundle);
                startActivity(intent);
//                startActivity(new Intent(getBaseActivity(), ProblemReportActivity.class));
                break;
            }
            //河管员督办单
            case R.id.tv_problem_report_list: {
                Intent intent = new Intent(getBaseActivity(), DubanTopersonCoordListActivity.class);
                intent.putExtra("isLeaderDuban",0);
                startActivity(intent);
//                startActivity(new Intent(getBaseActivity(), DubanTopersonCoordListActivity.class));
                break;
            }


            //湖泊业务处置
            case R.id.tv_lakeproblem_report_list: {
                Intent intent = new Intent(getBaseActivity(), LakeDubanTopersonListActivity.class);
                //intent.putExtra("isLeaderDuban",0);
                startActivity(intent);
//                startActivity(new Intent(getBaseActivity(), DubanTopersonCoordListActivity.class));
                break;
            }

            //督察
            case R.id.tv_ducha: {
                startActivity(new Intent(getBaseActivity(), DuChaDanActivity.class));
                break;
            }
            case R.id.tv_ducha_list: {
                startActivity(new Intent(getBaseActivity(), DuChaListAcitivity.class));
                break;
            }
            case R.id.tv_suggestion: {
                Intent intent = new Intent(getBaseActivity(), CompListActivity.class);
                intent.putExtra(Tags.TAG_ABOOLEAN, false);
                startActivity(intent);
                break;
            }
            case R.id.tv_collection: {
                startActivity(new Intent(getBaseActivity(), MyCollectActivity.class));
                break;
            }
            case R.id.tv_about: {
                startActivity(new Intent(getBaseActivity(), AboutActivity.class));
                break;
            }
            case R.id.tv_setting: {
                startActivity(new Intent(getBaseActivity(), SettingActivity.class));
                break;
            }
            case R.id.iv_logo: {
                if (!getBaseActivity().getUser().isLogined()) {


                    Intent intent = new Intent(getBaseActivity(), LoginActivity.class);
                    startActivityForResult(intent, Tags.CODE_LOGIN);
                }
                break;
            }
            case R.id.tv_logout:
                getBaseActivity().createMessageDialog("提示", "确定要注销登录吗?", "确定", "取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        logout();
                    }
                }, null, null).show();
                break;
            default:
                getBaseActivity().onClick(v);
                break;
        }
    }

    private void logout() {
        showOperating();

        //更换logo
        rootView.findViewById(R.id.iv_logo).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tv_name).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tv_info).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.shape_radius).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.npc_logo).setVisibility(View.INVISIBLE);

        //更换"我的投诉"、"我的建议"
        ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("我的投诉");
        ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("我的建议");

        getRequestContext().add("User_Logout_Action", new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                // if (o != null && o.isSuccess()) {
                getBaseActivity().getUser().clearInfo();
                refreshView();
                hideOperating();
                // }
            }
        }, BaseRes.class, ParamUtils.freeParam(null, "cid", getBaseActivity().getLocalService() != null ? getBaseActivity().getLocalService().getCid() : ""));
    }

    private void changeNpcLogo() {
        //更换logo
        rootView.findViewById(R.id.iv_logo).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.tv_name).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.tv_info).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.shape_radius).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.npc_logo).setVisibility(View.VISIBLE);
        //更换"其他投诉"、"其他建议"
        ((TextView) rootView.findViewById(R.id.tv_complaint)).setText("其他投诉");
        ((TextView) rootView.findViewById(R.id.tv_suggestion)).setText("其他建议");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && getBaseActivity().getUser().isLogined() && getBaseActivity().getUser().isNpc()) {
            changeNpcLogo();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
