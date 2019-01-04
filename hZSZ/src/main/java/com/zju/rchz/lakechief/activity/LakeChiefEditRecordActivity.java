package com.zju.rchz.lakechief.activity;

import com.zju.rchz.activity.BaseActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.sin.android.sinlibs.utils.InjectUtils;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.activity.LakeProblemReportActivity;
import com.zju.rchz.activity.PhotoViewActivity;
import com.zju.rchz.activity.ProblemReportActivity;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.DateTime;
import com.zju.rchz.model.Lake;
import com.zju.rchz.model.LakeRecord;
import com.zju.rchz.model.LakeRecordRes;
import com.zju.rchz.model.RiverRecordTemporaryJsonRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.service.LocalService;
import com.zju.rchz.utils.ImgUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.PatrolRecordUtils;
import com.zju.rchz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by ZJTLM4600l on 2018/7/13.
 */

public class LakeChiefEditRecordActivity extends BaseActivity {

    //巡检情况处的布局
    private LinearLayout ll_cboxes = null;
    //处理情况
    private EditText et_deal = null;
    //其他问题
    private EditText et_otherquestion = null;
    private Button btn_track = null;
    private Button btn_trackView = null;
    //LakeRecord:巡湖相关类
    private LakeRecord lakeRecord = null;
    private ViewRender viewRender = new ViewRender();
    private Location location = null;
    private Location imgLocation=null;

    private String picPath; //图片链接

    private boolean isReadOnly = false;

    private String latList;
    private String lngList;

    private String latlist_temp;//当前巡湖数据
    private String lnglist_temp;
    private String[] lat_array;
    private String[] lng_array;
    private String[] lat_temp_array;
    private String[] lng_temp_array;

    private String latList_host;//服务器回传的轨迹经纬度
    private String lngList_host;
    private int lakeRecordTempLakeId;
    private int lakeRecordTempPassTime;//服务器保存的之前的巡河时长（单位：秒）
    //巡河记录返回的信息
    private String patrolLength;//巡河长度
    private String patrolTime;//巡河时长

    private String imgLnglist="";
    private String imgLatlist="";

    //拍摄照片时的经纬度或者照片自带的经纬度
    private Double imgLngTemp;
    private Double imgLatTemp;

    private boolean hasImg = false;

    //判断是否大于5min
    private int startTimeHour;
    private int startTimeMin;
    private String startTime;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //巡湖线程改动
    private MyRunable myRunable_record = new MyRunable();
    private boolean isRunMyRunable = false;
    private boolean isNewLakeRecord = false;

    //确定是新加巡湖单还是编辑巡湖单（现在已经无法编辑）
    private boolean isAddNewLakeRecord = false;

    //记录当前的年月日
    String year="2015";
    String month="10";
    String day="10";

    private String eventFlag = "0";//上报人员身份。0代表督察员，1代表湖长。

    //计数，如果长时间没有新的点加入要删除的轨迹，认为之前的取点有误
    private int countNoJoin = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplication());
        setContentView(R.layout.activity_chief_editrecord);

        initHead(R.drawable.ic_head_back, 0);
        SDKInitializer.initialize(this);
        InjectUtils.injectViews(this, R.id.class);

//		if(ischief||isVillageChief||isDistrictChief||isCityChief){
        findViewById(R.id.action_event_report).setVisibility(View.VISIBLE);
        eventFlag = "1";
//		}else {
//			findViewById(R.id.action_event_report).setVisibility(View.GONE);
//		}
//		if (location != null) {
//			latlist_temp = "" + location.getLatitude();
//			latlist_temp = "" + location.getLongitude();
//		}
        //认为不是第一次上传巡湖轨迹
        isNewLakeRecord = false;

        findViewById(R.id.btn_selriver).setOnClickListener(this);//选择湖泊按钮
        findViewById(R.id.btn_submit).setOnClickListener(this);//保存按钮
        findViewById(R.id.btn_cancel).setOnClickListener(this);//取消按钮


        btn_track = (Button) findViewById(R.id.btn_track);//查看轨迹按钮-巡湖界面
        btn_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lakeRecord.lakeId==0){
                    selectLake();
                }else{
                    Intent intent = new Intent(LakeChiefEditRecordActivity.this, LakeChiefInspectActivity.class);
//					intent.putExtra("latlist_temp", OptimizePoints(latlist_temp,lnglist_temp)[0]);
//					intent.putExtra("lnglist_temp", OptimizePoints(latlist_temp,lnglist_temp)[1]);
                    intent.putExtra("latlist_temp", latlist_temp);//将当前的巡湖数据传至巡湖地图界面
                    intent.putExtra("lnglist_temp", lnglist_temp);
                    intent.putExtra("isAddNewLakeRecord",isAddNewLakeRecord);
                    intent.putExtra("passTime",lakeRecordTempPassTime);
                    intent.putExtra("lakeId", (int)lakeRecord.lakeId);
                    startActivityForResult(intent, Tags.CODE_LATLNG);
                }
            }
        });
        btn_trackView = (Button) findViewById(R.id.btn_trackView);//查看轨迹按钮-查看巡湖单界面
        btn_trackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LakeChiefEditRecordActivity.this, LakeChiefTrackViewActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("latList", latList);
                bundle.putString("lngList", lngList);
                bundle.putString("imgLnglist",imgLnglist);
                bundle.putString("imgLatlist",imgLatlist);
                bundle.putString("patrolLength",patrolLength);
                bundle.putString("patrolTime",patrolTime);
                bundle.putString("picPath",picPath);
                intent.putExtra("lakeId", lakeRecord.lakeId);
                bundle.putDouble("longitude",longitude);
                bundle.putDouble("latitude",latitude);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //初始化当前的年月日
        Calendar calendar=Calendar.getInstance();  //获取当前时间，作为图标的名字
        year=calendar.get(Calendar.YEAR)+"";
        month=calendar.get(Calendar.MONTH)+1+"";
        day=calendar.get(Calendar.DAY_OF_MONTH)+"";

        lakeRecord = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RECORD), LakeRecord.class);

        isReadOnly = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);
        latList_host = getIntent().getExtras().getString("latList_host");
        lngList_host = getIntent().getExtras().getString("lngList_host");
        lakeRecordTempLakeId = getIntent().getExtras().getInt("lakeId");
        lakeRecordTempPassTime = getIntent().getExtras().getInt("passTime",0);

        submitUuidParam = new JSONObject();
        submitTemporaryParam = new JSONObject();
        submitSetLakeRecordIsCorrectParam = new JSONObject();
        try{
            submitUuidParam.put("UUID",getUser().getUuid());
            submitTemporaryParam.put("UUID",getUser().getUuid());
            submitSetLakeRecordIsCorrectParam.put("UUID",getUser().getUuid());
            submitSetLakeRecordIsCorrectParam.put("year",year);
            submitSetLakeRecordIsCorrectParam.put("month",month);
            submitSetLakeRecordIsCorrectParam.put("day",day);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (lakeRecord == null){//从新建中进入
//			showToastCenter(ChiefEditRecordActivity.getCurActivity(),"除拍照等操作外\n请在【查看轨迹】界面方便记录轨迹");//提示

            //确定是新加巡湖单还是编辑巡湖单（现在已经无法编辑）
            isAddNewLakeRecord = true;

            //在新建巡湖单的时候启动线程//开始线程
            isRunMyRunable = true;
            handler.postDelayed(myRunable_record, 2000); //改为每2s记录一次当前轨迹
            initLocation();

            isNewLakeRecord = true; //新建巡湖记录
            setTitle("新建巡查记录");
            lakeRecord = new LakeRecord();
            lakeRecord.recordDate = DateTime.getNow(); //巡湖时间
            lakeRecord.locLakeName = "选择湖泊";
            viewRender.renderView(findViewById(R.id.sv_main), lakeRecord);

            //巡湖开始时间
            startTime = DateTime.getNow().getYMDHMS(this);
            startTimeHour = DateTime.getNow().hours;
            startTimeMin = DateTime.getNow().minutes;

            //退出巡湖时的提醒
            findViewById(R.id.iv_head_left).setOnClickListener(exitTrackRiver);

            if (!getUser().isNpc())
                refreshToView();
            else
                refreshToNpcView();

            //看是否有未完成的轨迹信息
//			getHostRiverRecordTemporary();

            //检查是否是否有未完成的轨迹
            if (latList_host != null && !latList_host.equals("")) {
                AlertDialog.Builder ab = new AlertDialog.Builder(LakeChiefEditRecordActivity.this);
                ab.setTitle("有未完成的巡湖轨迹");
                ab.setMessage("系统检测到您上次未正常提交巡湖单，继续采用之前的巡湖轨迹？");
                ab.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        latlist_temp = latList_host;
                        lnglist_temp = lngList_host;
                        lakeRecord.lakeId = lakeRecordTempLakeId;

                        for (Lake l : getUser().lakeSum) {
                            if (lakeRecord.lakeId == l.lakeId) {
                                lakeRecord.locLake = l;
                                lakeRecord.locLakeName = l.lakeName;
                                refreshSelectLakeBtn();
                            }
                        }

                        startTimer();//开启定时器
                        arg0.dismiss();
                    }
                });
                ab.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //删除舍弃的轨迹信息
                        getRequestContext().add("Delete_LakeRecordTemporary", new Callback<BaseRes>() {
                            @Override
                            public void callback(BaseRes o) {
                                if (o != null && o.isSuccess()) {

                                }
                            }
                        }, BaseRes.class, submitUuidParam);
                        lakeRecordTempPassTime = 0;
                        startTimer();//开启定时器

                        arg0.dismiss();
                    }
                });
                ab.setCancelable(false);
                ab.create().show();
            }else {
                //开启定时器
                startTimer();
            }
        } else {//从具体巡湖条目中进入
            //确定是新加巡湖单还是编辑巡湖单（现在已经无法编辑）
            isAddNewLakeRecord = false;

            setTitle("编辑巡查记录");
            isNewLakeRecord = false;//不是新建（编辑巡湖记录）
            //取消结束巡湖以及拍照按钮。
            findViewById(R.id.multiple_actions).setVisibility(View.GONE);
            findViewById(R.id.linear_river_record).setVisibility(View.GONE);

            findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            ll_cboxes.removeAllViews();

            showOperating();
            getRequestContext().add("Get_LakeRecord", new Callback<LakeRecordRes>() {
                @Override
                public void callback(LakeRecordRes o) {
                    hideOperating();
                    if (o != null && o.isSuccess()) {

                        o.data.recordId = lakeRecord.recordId;
                        o.data.recordSerNum = lakeRecord.recordSerNum;
                        o.data.recordDate = lakeRecord.recordDate;
                        lakeRecord = o.data;

                        lakeRecord.locLake = null;
                        for (Lake l : getUser().lakeSum) {
                            if (lakeRecord.lakeId == l.lakeId) {
                                lakeRecord.locLake = l;
                                lakeRecord.locLakeName = l.lakeName;
                            }
                        }
                        initPhotoView(o.data.picPath);
                        //切换按钮

                        //获得经纬度信息
                        latList = o.data.latlist;
                        lngList = o.data.lnglist;
//						latitude=o.data.latitude;
//						longitude=o.data.longitude;

                        imgLatlist=o.data.imgLatlist;
                        imgLnglist=o.data.imgLnglist;
                        //图片信息
                        picPath=o.data.picPath;
                        //巡河时长和巡河轨迹长度
                        patrolLength = o.data.patrolLength;
                        patrolTime = o.data.patrolTime;
                        //如果无轨迹则不显示“查看轨迹”按钮
                        if (latList == "" && lngList == "") {
                            btn_track.setVisibility(View.GONE);
                            btn_trackView.setVisibility(View.GONE);
                        } else {
                            btn_track.setVisibility(View.GONE);
                            btn_trackView.setVisibility(View.VISIBLE);
                        }

                        viewRender.renderView(findViewById(R.id.sv_main), lakeRecord);
//						if (!getUser().isNpc())
                        //点击具体条目之后，若巡湖人的级别为人大，则用人大的视图；是湖长，则用湖长的视图
                        if (lakeRecord.recordPersonAuthority > 20)
                            refreshToNpcView();
                        else
                            refreshToView();

                    }
                }
            }, LakeRecordRes.class, ParamUtils.freeParam(null, "recordId", lakeRecord.recordId,
                    "recordPersonName", lakeRecord.recordPersonName,
                    "recordPersonId", lakeRecord.recordPersonId,
                    "authority", getUser().getAuthority()));
        }

		/*findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				findViewById(R.id.tv_btn_explain).setVisibility(View.GONE);
				startAddPhoto();
			}
		});*/

        //巡湖过程中事件上报
        findViewById(R.id.action_event_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LakeChiefEditRecordActivity.this, LakeProblemReportActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("eventFlag", eventFlag);//上报人员身份标志位
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        //相机拍摄照片
        findViewById(R.id.action_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //若照片超过5张，则无法继续添加
                if (((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() > 2) {
                    showToast("照片过多，只限3张，可长按图片进行删除");
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                } else {
                    //关闭FloatingActionMenu
                    getImgLocation();
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                    startAddPhoto();
                }
            }
        });

        //相册挑选照片
        findViewById(R.id.action_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() > 2) {
                    showToast("照片过多，只限3张，可长按图片进行删除");
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                } else {
                    //关闭floatingActionMenu
                    getImgLocation();
                    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
                    startAlbum();
                }
            }
        });

        if (isReadOnly) {
            setTitle("记录详情");
            et_deal.setEnabled(false);
            et_otherquestion.setEnabled(false);
            findViewById(R.id.hsv_photos).setVisibility(View.GONE);
            findViewById(R.id.btn_selriver).setVisibility(View.GONE);
            findViewById(R.id.btn_submit).setVisibility(View.GONE);
            findViewById(R.id.multiple_actions).setVisibility(View.GONE);
            ((Button) findViewById(R.id.btn_cancel)).setText("关闭");
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void showToastCenter(Context context, String toastStr) {
        Toast toast = Toast.makeText(context.getApplicationContext(), toastStr, Toast.LENGTH_SHORT);
        int tvToastId = Resources.getSystem().getIdentifier("message", "id", "android");
        TextView tvToast = ((TextView) toast.getView().findViewById(tvToastId));
        if(tvToast != null){
            tvToast.setGravity(Gravity.CENTER);
        }
        toast.show();
    }
    //请求，判断服务器是否对应湖长有未完成的轨迹
    private void getHostRiverRecordTemporary(){
        getRequestContext().add("Get_LakeRecordTemporary", new Callback<RiverRecordTemporaryJsonRes>() {
            @Override
            public void callback(RiverRecordTemporaryJsonRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {
                    if(o.data!=null){
                        //获得经纬度信息
                        latList_host = o.data.getLatlist();
                        showToast(" o.data.getLatlist()"+"!!!"+ o.data.getLatlist()+"!!!"+latList_host);
                        lngList_host = o.data.getLnglist();
                    }
                }
            }
        }, RiverRecordTemporaryJsonRes.class, submitUuidParam);
    }
    //定时器，定时向服务器发送轨迹经纬度信息
    private int DELAY_TIME = 1000;//延时1s开启定时器
    private int PERIOD_TIME = 90000;//每过90s传一次数据
    Timer mTimer = null;
    TimerTask mTimerTask = null;

    //定时器，用于记录巡河时长并显示
    Timer passTimer = null;
    TimerTask passTimerTask = null;

    private void startTimer(){
        if(lakeRecord.lakeId ==0){
            selectLake();
        }
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try{
                        //失能 提交巡湖单按钮
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Button)findViewById(R.id.btn_submit)).setText("轨迹暂存中...");
                                ((Button)findViewById(R.id.btn_submit)).setBackgroundColor(getResources().getColor(R.color.half_black));
                            }
                        });
                        ((Button)findViewById(R.id.btn_submit)).setClickable(false);

                        submitTemporaryParam.put("latList",latlist_temp);
                        submitTemporaryParam.put("lngList",lnglist_temp);
                        submitTemporaryParam.put("lakeId",lakeRecord.lakeId);
                        submitTemporaryParam.put("passTime",lakeRecordTempPassTime);
                        if(latlist_temp!=null && !latlist_temp.equals("")){
                            getRequestContext().add("AddOrEdit_LakeRecordTemporary", new Callback<BaseRes>() {
                                @Override
                                public void callback(BaseRes o) {
                                    if (o != null && o.isSuccess()) {

                                    }
                                }
                            }, BaseRes.class, submitTemporaryParam);
                        }
                        //使能 提交巡湖单按钮
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((Button)findViewById(R.id.btn_submit)).setText("结束巡湖");
                                ((Button)findViewById(R.id.btn_submit)).setBackgroundColor(getResources().getColor(R.color.blue));
                            }
                        });
                        ((Button)findViewById(R.id.btn_submit)).setClickable(true);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, DELAY_TIME, PERIOD_TIME);

        //巡河时长相关定时器和定时任务
        if (passTimer == null) {
            passTimer = new Timer();
        }

        if (passTimerTask == null) {
            passTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lakeRecordTempPassTime++;
//							showToast(getStringTime(lakeRecordTempPassTime++));
                        }
                    });

                }
            };
        }

        if(passTimer != null && passTimerTask != null )
            passTimer.schedule(passTimerTask, 500, 1000);

    }

    private void stopTimer(){
        //服务器暂存轨迹相关定时器
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        //巡河时长相关定时器
        if (passTimer != null) {
            passTimer.cancel();
            passTimer = null;
        }
        if (passTimerTask != null) {
            passTimerTask.cancel();
            passTimerTask = null;
        }
    }


    private static final String[] CBOX_TITLES = new String[]{//
            //
            "湖面有无成片漂浮废弃物、病死动物等", //
            "湖岸有无垃圾堆放", //
            "湖岸有无新建违法建筑物",//
            "湖底有无明显污泥或垃圾淤积", //
            "湖泊水体有无臭味，颜色有无黑色",//
            "沿线有无晴天排污口", //
//            "湖泊长效管理机制和保洁机制是否到位"//
    };

    private static final String[] CBOX_NPC_TITLES = new String[]{//
            //
            "1.湖泊水质情况评价（颜色、气味、浊度等）", //flotage
            "2.湖泊保洁情况评价（漂浮物、废弃物等）", //rubbish
            "3.湖泊养护情况评价（绿化、游步道等）", // odour
            "4.沿线违法、违章建筑情况", // building
//            "5.沿线排污口情况（以晴天是否排污为准）"// outfall
    };


    private static final String[] CBOX_FIELDS = new String[]{//
            //
            "flotage",//
            "rubbish",//
            "building",//
            "sludge",//
            "odour",//
            "outfall",//
//            "riverinplace",//
    };

    private static final String[] CBOX_NPC_FIELDS = new String[]{//
            //
            "flotage",//
            "rubbish",//
            "odour",//
            "building",//
            "outfall",//
    };

    private View[] CBOXES = new View[CBOX_FIELDS.length];
    private View[] CBOXES_NPC = new View[CBOX_NPC_FIELDS.length];

    private OnCheckedChangeListener cclTogTag = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton cb, boolean c) {
            View v = (View) cb.getTag();
            CompoundButton tcb = (CompoundButton) v.findViewById(cb.getId() == R.id.cb_no ? R.id.cb_yes : R.id.cb_no);
            if (tcb.isChecked() == c) {
                tcb.setChecked(!c);
            }
            v.findViewById(R.id.et_text).setVisibility(((CompoundButton) v.findViewById(R.id.cb_yes)).isChecked() ? View.VISIBLE : View.GONE);
        }
    };

    private View.OnClickListener exitTrackRiver = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            quitLakeRecord();
        }
    };

    //	重写安卓系带返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //TODO something
            if(isNewLakeRecord){
                quitLakeRecord();
            }else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void quitLakeRecord(){
        AlertDialog.Builder ab = new AlertDialog.Builder(LakeChiefEditRecordActivity.this);
        ab.setTitle("是否退出巡湖");
        ab.setMessage("您要放弃这条轨迹吗？");
        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                //删除舍弃的轨迹
                getRequestContext().add("Delete_LakeRecordTemporary", new Callback<BaseRes>() {
                    @Override
                    public void callback(BaseRes o) {
                        if (o != null && o.isSuccess()) {

                        }
                    }
                }, BaseRes.class, submitUuidParam);

                //提交成功之后，将缓存坐标值设为空
                latlist_temp = "";
                lnglist_temp = "";

                imgLatlist="";
                imgLnglist="";

                finish();
            }
        });
        ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }

        });
        ab.setCancelable(false);
        ab.create().show();
    }

    private OnCheckedChangeListener cclTogTagNpc = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton cb, boolean c) {
            View v = (View) cb.getTag();

            if (cb.getId() == R.id.cb_good && c) {
                ((CompoundButton) v.findViewById(R.id.cb_bad)).setChecked(false);
                ((CompoundButton) v.findViewById(R.id.cb_medium)).setChecked(false);
            }

            if (cb.getId() == R.id.cb_medium && c) {
                ((CompoundButton) v.findViewById(R.id.cb_good)).setChecked(false);
                ((CompoundButton) v.findViewById(R.id.cb_bad)).setChecked(false);
            }

            if (cb.getId() == R.id.cb_bad && c) {
                ((CompoundButton) v.findViewById(R.id.cb_medium)).setChecked(false);
                ((CompoundButton) v.findViewById(R.id.cb_good)).setChecked(false);
            }

            v.findViewById(R.id.et_text).setVisibility(((CompoundButton) v.findViewById(R.id.cb_bad)).isChecked() ? View.VISIBLE : View.GONE);
        }
    };

    private void refreshToView() {
        // gen yes or no
        ll_cboxes.removeAllViews();
        Class<?> clz = LakeRecord.class;
        for (int i = 0; i < CBOX_TITLES.length; ++i) {
            if (i > 0) {
                ll_cboxes.addView(LinearLayout.inflate(this, R.layout.inc_vline, null));
            }
            View v = LinearLayout.inflate(this, R.layout.inc_line_yesno, null);
            ((TextView) v.findViewById(R.id.tv_title)).setText(CBOX_TITLES[i]);

            boolean yes = false;
            String text = null;
            try {
                Field f = clz.getField(CBOX_FIELDS[i]);
                yes = f.getInt(lakeRecord) == 1;

                text = (String) clz.getField(CBOX_FIELDS[i] + "s").get(lakeRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (text == null)
                text = "";

            if ("lakeinplace".equals(CBOX_FIELDS[i])) {
                ((CheckBox) v.findViewById(R.id.cb_yes)).setText("不到位");
                ((CheckBox) v.findViewById(R.id.cb_no)).setText("到位");
            }

            v.findViewById(R.id.cb_no).setTag(v);
            v.findViewById(R.id.cb_yes).setTag(v);
            ((EditText) v.findViewById(R.id.et_text)).setText(text);

            if (!isReadOnly) {
                ((CheckBox) v.findViewById(R.id.cb_yes)).setOnCheckedChangeListener(cclTogTag);
                ((CheckBox) v.findViewById(R.id.cb_no)).setOnCheckedChangeListener(cclTogTag);
            } else {
                ((CheckBox) v.findViewById(R.id.cb_yes)).setEnabled(false);
                ((CheckBox) v.findViewById(R.id.cb_no)).setEnabled(false);
                v.findViewById(R.id.et_text).setEnabled(false);
            }
            ((CheckBox) v.findViewById(R.id.cb_yes)).setChecked(yes);
            ((CheckBox) v.findViewById(R.id.cb_no)).setChecked(!yes);
            v.findViewById(R.id.et_text).setVisibility(yes ? View.VISIBLE : View.GONE);

            CBOXES[i] = v;

            if(i<CBOX_TITLES.length-1){
                ll_cboxes.addView(v);
            }

        }
        refreshSelectLakeBtn();
    }

    private void refreshToNpcView() {
        ll_cboxes.removeAllViews();
        Class<?> clz = LakeRecord.class;
        for (int i = 0; i < CBOX_NPC_TITLES.length; ++i) {
            if (i > 0) {
                ll_cboxes.addView(LinearLayout.inflate(this, R.layout.inc_vline, null));
            }
            View v = LinearLayout.inflate(this, R.layout.inc_line_npctrack, null);
            ((TextView) v.findViewById(R.id.tv_title)).setText(CBOX_NPC_TITLES[i]);

            int yes = 1;
            String text = null;
            try {
                Field f = clz.getField(CBOX_NPC_FIELDS[i]);
                yes = f.getInt(lakeRecord); //决定哪一个打勾

                text = (String) clz.getField(CBOX_NPC_FIELDS[i] + "s").get(lakeRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (text == null)
                text = "";

            if ("building".equals(CBOX_NPC_FIELDS[i]) || "outfall".equals(CBOX_NPC_FIELDS[i])) {
                ((CheckBox) v.findViewById(R.id.cb_good)).setText("无");
                ((CheckBox) v.findViewById(R.id.cb_medium)).setText("难以确认");
                ((CheckBox) v.findViewById(R.id.cb_bad)).setText("有");
            }

            v.findViewById(R.id.cb_good).setTag(v);
            v.findViewById(R.id.cb_medium).setTag(v);
            v.findViewById(R.id.cb_bad).setTag(v);
            ((EditText) v.findViewById(R.id.et_text)).setText(text);


            if (!isReadOnly) {
                ((CheckBox) v.findViewById(R.id.cb_good)).setOnCheckedChangeListener(cclTogTagNpc);
                ((CheckBox) v.findViewById(R.id.cb_medium)).setOnCheckedChangeListener(cclTogTagNpc);
                ((CheckBox) v.findViewById(R.id.cb_bad)).setOnCheckedChangeListener(cclTogTagNpc);
            } else {
                ((CheckBox) v.findViewById(R.id.cb_good)).setEnabled(false);
                ((CheckBox) v.findViewById(R.id.cb_bad)).setEnabled(false);
                ((CheckBox) v.findViewById(R.id.cb_medium)).setEnabled(false);
                v.findViewById(R.id.et_text).setEnabled(false);
            }
            if (yes == 0)
                yes = 1;
            ((CheckBox) v.findViewById(R.id.cb_good)).setChecked(yes == 1); //加个小于号是为了默认能够在“好”的地方打勾
            ((CheckBox) v.findViewById(R.id.cb_medium)).setChecked(yes == 2);
            ((CheckBox) v.findViewById(R.id.cb_bad)).setChecked(yes == 3);
            v.findViewById(R.id.et_text).setVisibility(yes == 3 ? View.VISIBLE : View.GONE);

            CBOXES_NPC[i] = v;
            ll_cboxes.addView(v);
        }
        //去掉处理情况
        findViewById(R.id.ll_deal).setVisibility(View.GONE);

        refreshSelectLakeBtn();
    }

    //更新选择湖泊按钮
    private void refreshSelectLakeBtn() {
        if (lakeRecord.locLake != null)
            ((Button) findViewById(R.id.btn_selriver)).setText(lakeRecord.locLake.lakeName);
        else if (getUser().lakeSum.length == 1) {
            //如果湖长或人大只有一个湖泊，则直接显示其所负责的湖泊
            ((Button) findViewById(R.id.btn_selriver)).setText(getUser().lakeSum[0].lakeName);
            lakeRecord.locLake = getUser().lakeSum[0];
            lakeRecord.lakeId = lakeRecord.locLake.lakeId;
            lakeRecord.locLakeName = lakeRecord.locLake.lakeName;
        } else
            ((Button) findViewById(R.id.btn_selriver)).setText("请选择湖泊");
    }

    private void selectLake() {
        Lake[] lakes = getUser().lakeSum;
        String[] names = new String[lakes.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = lakes[i].lakeName;
        }
        Dialog alertDialog = new AlertDialog.Builder(this).setTitle("请选择湖泊").setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lakeRecord.locLake = getUser().lakeSum[which];
                lakeRecord.lakeId = lakeRecord.locLake.lakeId;
                lakeRecord.locLakeName = lakeRecord.locLake.lakeName;
                refreshSelectLakeBtn();
            }
        }).create();
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                else {
                    return false;//默认返回false
                }
            }
        });
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                quitLakeRecord();
//				finish();
                break;
            case R.id.btn_selriver: {
                selectLake();
                break;
            }
	/*	case R.id.btn_track:{
			Intent intent = new Intent(ChiefEditRecordActivity.this,com.zju.hzsz.chief.activity.ChiefInspectActivity.class);
			startActivity(intent);
			break;
		}*/
            case R.id.btn_submit: {

                if (!hasImg) {
                    //如果不是人大，则不用拍照片
                    if (!getUser().isNpc()) {
                        showToast("您还没拍摄照片，请点击右侧“+”按钮添加照片");
                        return;
                    }
                }

                if (lakeRecord.lakeId == 0) {
                    showToast("您还没有选择湖泊，请先选择湖泊");
                    return;
                } else {
                    submitParam = new JSONObject();
                    boolean needdeal = false;
                    try {
                        if (lakeRecord.recordId != 0) {
                            submitParam.put("recordId", lakeRecord.recordId);
                        }
                        if (!getUser().isNpc()) {
                            for (int i = 0; i < CBOX_FIELDS.length; ++i) {
                                boolean b = ((CompoundButton) CBOXES[i].findViewById(R.id.cb_yes)).isChecked();
                                submitParam.put(CBOX_FIELDS[i], b ? 1 : 0);
                                needdeal = b || needdeal;

                                String s = b ? ((EditText) CBOXES[i].findViewById(R.id.et_text)).getText().toString().trim() : "";
                                if (b && s.length() == 0) {
                                    showToast("您还有具体描述没填写，请先填写");
                                    ((EditText) CBOXES[i].findViewById(R.id.et_text)).requestFocus();
                                    ((EditText) CBOXES[i].findViewById(R.id.et_text)).setFocusable(true);
                                    return;
                                }
                                submitParam.put(CBOX_FIELDS[i] + "s", s);
                            }

                            //处理情况
                            if (needdeal && et_deal.getText().toString().length() == 0) {
                                showToast("您还没有填写处理情况，请先填写");
                                et_deal.requestFocus();
                                et_deal.setFocusable(true);
                                return;
                            }
                            submitParam.put("deal", et_deal.getText().toString());

                        } else {
                            for (int i = 0; i < CBOX_NPC_FIELDS.length; ++i) {
                                int b = 0;
                                if (((CompoundButton) CBOXES_NPC[i].findViewById(R.id.cb_good)).isChecked())
                                    b = 1;
                                else if (((CompoundButton) CBOXES_NPC[i].findViewById(R.id.cb_medium)).isChecked())
                                    b = 2;
                                else
                                    b = 3;

                                submitParam.put(CBOX_NPC_FIELDS[i], b);
                                needdeal = b != 1 || needdeal;

                                String s = b == 3 ? ((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).getText().toString().trim() : "";
                                if (b == 3 && s.length() == 0) {
                                    showToast("您还有具体描述没填写，请先填写");
                                    ((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).requestFocus();
                                    ((EditText) CBOXES_NPC[i].findViewById(R.id.et_text)).setFocusable(true);
                                    return;
                                }
                                submitParam.put(CBOX_NPC_FIELDS[i] + "s", s);
                            }

                            submitParam.put("sludge", 0);
                            submitParam.put("sludges", "");
                            submitParam.put("deal", "");

                        }

                        submitParam.put("LakeId", lakeRecord.lakeId);
                        submitParam.put("otherquestion", et_otherquestion.getText().toString());


                        //增加图片链接
                        submitParam.put("picPath", picPath);

                        //添加巡湖轨迹经纬度信息
						submitParam.put("latlist", OptimizePoints(latlist_temp,lnglist_temp)[0]);
						submitParam.put("lnglist", OptimizePoints(latlist_temp,lnglist_temp)[1]);
//                        submitParam.put("latlist", latlist_temp);
//                        submitParam.put("lnglist", lnglist_temp);

                        //添加图片经纬度信息
                        submitParam.put("imgLatlist", imgLatlist);
                        submitParam.put("imgLnglist", imgLnglist);

                        //添加湖长权限和uuid
                        submitParam.put("authority", getUser().getAuthority());
                        submitParam.put("UUID", getUser().getUuid());

                        //巡湖时间
                        submitParam.put("recordStartDate", startTime);

                        submitParam.put("lakeinplace", 0);
                        submitParam.put("lakeinplaces", "");

                        //判断巡湖是否超过了5min
                        //看是否超过5min
                        int endTimeHour = DateTime.getNow().hours;
                        int endTimeMin = DateTime.getNow().minutes;
                        if (lakeRecordTempPassTime <= 300) {
                            //系统参数，值由湖长办定 0：不能结束 1：可以结束
                            if (Values.tourriver == 0) {
                                showToast("您的巡湖时间小于5min, 请继续巡湖");
                                return;
                            }
                        }
                        //巡河时长
                        submitParam.put("passTime", lakeRecordTempPassTime);
                        if (location != null) {
                            submitParam.put("latitude", location.getLatitude());
                            submitParam.put("longtitude", location.getLongitude());
                        }
                        submitParam.put("version",Values.Ver);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
                    if (ll_photos.getChildCount() > 0) {
                        // 有图片
                        final Uri[] bmps = new Uri[ll_photos.getChildCount()];
                        for (int i = 0; i < bmps.length; i++) {
                            bmps[i] = (Uri) ll_photos.getChildAt(i).getTag();
                        }
                        asynCallAndShowProcessDlg("正在处理图片...", new Callable() {
                            @Override
                            public void call(Object... args) {

                                StringBuffer sb = new StringBuffer();
                                for (Uri bmp : bmps) {
                                    if (sb.length() > 0)
                                        sb.append(";");
                                    byte[] bts = bmp2Bytes(bmp);
//									Log.i("NET", bmp.toString() + " bts.len " + bts.length);
                                    Log.i("NET", " bts.len " + bts.length);
                                    sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
                                }
                                String picbase64 = sb.toString();
                                Log.i("NET", "all base64.len " + picbase64.length());
                                try {
                                    submitParam.put("picBase64", picbase64);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                safeCall(new Callable() {
                                    @Override
                                    public void call(Object... args) {
                                        submitData();
                                    }
                                });
                            }
                        });
                    } else {
                        submitData();
                    }
                }
                break;
            }
            default:
                super.onClick(v);
                break;
        }
    }

    JSONObject submitParam = null;
    JSONObject submitTemporaryParam = null;
    JSONObject submitUuidParam = null;
    JSONObject submitSetLakeRecordIsCorrectParam = null;

    private void submitData() {
        showOperating(R.string.doing_submitting);

        //关闭定时器
        stopTimer();
        //判断是否是村级湖长
        boolean isVillageChief = getUser().isLogined() && getUser().isVillageChief();

        getRequestContext().add("AddOrEdit_LakeRecord", new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {
//					hideOperating();
                    showToast("提交成功!");
                    setResult(RESULT_OK);

                    //提交成功之后，将缓存坐标值设为空
                    latlist_temp = "";
                    lnglist_temp = "";

                    imgLnglist="";
                    imgLatlist="";

                    finish();

//					//判断轨迹有效性的请求  （改为在列表中刷新，并且刚退回到列表界面就刷新一次）
//					getRequestContext().add("Set_RiverRecord_IsCorrect", new Callback<BaseRes>() {
//						@Override
//						public void callback(BaseRes o) {
//							hideOperating();
//							if (o != null && o.isSuccess()) {
//
//							}
//						}
//					}, BaseRes.class, submitSetRiverRecordIsCorrectParam);
                }
            }
        }, BaseRes.class, submitParam);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        if (!isReadOnly && isAddNewLakeRecord) {
            getLocalService().getLocation(new LocalService.LocationCallback() {
                @Override
                public void callback(Location location) {
                    LakeChiefEditRecordActivity.this.location = location;
                }
            });
        }
    }

    private Uri imageFilePath = null;

    private void startAddPhoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); //内容提供者，设置地址+存放照片

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //指定开启系统相机的Action
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath); //设置系统相机拍摄完成后照片的存放地址
        startActivityForResult(intent, Tags.CODE_ADDPHOTO);
    }

    //从相册中调取照片
    private void startAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Tags.CODE_ALBUM);
    }

    //拍摄完成后在巡湖表处添加照片
    private void addPhoto(Uri uri) {
        //view的layout属性：48*48+fitXY
        View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
        BitmapFactory.Options op = new BitmapFactory.Options();
        try {
            Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, op);

            int w = Values.UPLOAD_IMG_W;
            int h = Values.UPLOAD_IMG_H;
            if (pic.getHeight() != h)
                pic = ThumbnailUtils.extractThumbnail(pic, w, h);

            view.setTag(pic);
            Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
            ((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    //弹出“是否确定删除”对话框
                    //弹窗
                    AlertDialog.Builder ab = new AlertDialog.Builder(LakeChiefEditRecordActivity.this);
                    ab.setTitle("删除图片");
                    ab.setMessage("是否确定删除该巡湖照片？");
                    ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //移除照片
                            ((LinearLayout) findViewById(R.id.ll_chief_photos)).removeView(view);

                            arg0.dismiss();
                        }
                    });
                    ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    });
                    ab.setCancelable(false);
                    ab.create().show();

                    //在长按操作之后不再加入短按操作
                    return true;
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        view.setTag(uri);
        ((LinearLayout) findViewById(R.id.ll_chief_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount());
        if(((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount()==1){
            if (imgLocation!=null){
//				imgLnglist=imgLnglist+point.longitude;
//				imgLatlist=imgLatlist+point.latitude;
                imgLnglist=imgLnglist+imgLocation.getLongitude();
                imgLatlist=imgLatlist+imgLocation.getLatitude();
            }
        }else {
            if (imgLocation!=null) {
                imgLnglist=imgLnglist+","+imgLocation.getLongitude();
                imgLatlist=imgLatlist+","+imgLocation.getLatitude();
            }
        }
    }

    //	private void getLocation() {
//		if (getLocalService() != null) {
//			getLocalService().getLocation(new LocalService.LocationCallback() {
//				@Override
//				public void callback(Location location) {
//					ChiefEditRecordActivity.this.location = location;
//				}
//			});
//		}
//	}
    private void getImgLocation() {
        if (getLocalService() != null) {
            getLocalService().getLocation(new LocalService.LocationCallback() {
                @Override
                public void callback(Location location) {
                    LakeChiefEditRecordActivity.this.imgLocation = location;
                }
            });
        }
    }
    private byte[] bmp2Bytes(Uri uri) {
        try {
            BitmapFactory.Options op = new BitmapFactory.Options();
            Bitmap photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, op);

            int w = Values.UPLOAD_IMG_W;
            // int h = (int) (photo.getHeight() * (((float) w) /
            // photo.getWidth()));
            int h = Values.UPLOAD_IMG_H;
            if (photo.getHeight() != h)
                photo = ThumbnailUtils.extractThumbnail(photo, w, h);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);
            photo.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            baos.flush();
            baos.close();
            byte[] data = baos.toByteArray();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == Tags.CODE_ADDPHOTO) && resultCode == RESULT_OK) {
            // Log.e("uri", imageFilePath.toString());
            // addPhoto(imageFilePath);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(imageFilePath, "image/*");
            // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 800);
            intent.putExtra("outputY", 800);
            // intent.putExtra("return-data", true);

            intent.putExtra("output", imageFilePath);
            startActivityForResult(intent, Tags.CODE_CUTPHOTO);
        } else if (requestCode == Tags.CODE_CUTPHOTO && resultCode == RESULT_OK) {
            addPhoto(imageFilePath);
            hasImg = true;
        } else if (requestCode == Tags.CODE_ALBUM && resultCode == RESULT_OK) {
            try {

                Uri uri = data.getData();
                final String absolutePath =
                        getAbsolutePath(LakeChiefEditRecordActivity.this, uri);
                addPhoto(uri);
                hasImg = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == Tags.CODE_LATLNG && resultCode == RESULT_OK) {
            String result = data.getExtras().getString("result");
            //由inspect返回并需要上传至服务器的地理位置信息
            latList = data.getExtras().getString("latList");
            lngList = data.getExtras().getString("lngList");
            lakeRecordTempPassTime = data.getExtras().getInt("passTime");
            if(latList == null || lngList == null){
                latList = "";
                lngList = "";
            }
            isAddNewLakeRecord = data.getExtras().getBoolean("isAddNewLakeRecord");

            //由inspect返回的当前位置信息
            latlist_temp = "" + latList;
            lnglist_temp = "" + lngList;
            Log.i("来自record的latList", latList);
            Log.i("来自record的lngList", lngList);
            Log.i("recordinspect", latlist_temp);

            if(!latlist_temp.equals("") && !lnglist_temp.equals("")){
                if (latlist_temp.contains(",")){
                    //如果不止一个数据，变成一个数组
                    lat_temp_array = latlist_temp.split(",");
                    lng_temp_array = lnglist_temp.split(",");
                }else{
                    //如果只有一个数据
                    lat_temp_array = new String[1];
                    lng_temp_array = new String[1];
                    lat_temp_array[0] = latlist_temp;
                    lng_temp_array[0] = lnglist_temp;
                }
                point = new LatLng(Double.parseDouble(lat_temp_array[lat_temp_array.length-1]),
                        Double.parseDouble(lng_temp_array[lat_temp_array.length-1]));
//				showToast("hhhh");
            }

//			showToast(latlist_temp+"++"+lnglist_temp);
//			showToast(point.toString()+"++"+lat_temp_array.length);
        }

    }

    public String getAbsolutePath(final Context context,
                                  final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(
                            MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    String[] imgUrls = null;
    private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(LakeChiefEditRecordActivity.this, PhotoViewActivity.class);
                intent.putExtra("URLS", imgUrls);
                intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                startActivity(intent);
            }
        }
    };

    private void initPhotoView(String urls) {
        final List<String> list = new ArrayList<String>();
        if (urls != null) {
            hasImg = true; //如果之前的巡湖记录存在照片，则无需再进行拍照
            for (String url : urls.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imgUrls = new String[list.size()];
            imgUrls = list.toArray(imgUrls);
            findViewById(R.id.hsv_result_photos).setVisibility(View.VISIBLE);
            final LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_result_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgUrls.length; ++i) {
                final String url = imgUrls[i];
                System.out.println("url:" + url);
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(clkGotoPhotoView);
                //实现长按删除图片
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {

                        //弹出“是否确定删除”对话框
                        //弹窗
                        AlertDialog.Builder ab = new AlertDialog.Builder(LakeChiefEditRecordActivity.this);
                        ab.setTitle("删除图片");
                        ab.setMessage("是否确定删除该巡湖照片？");
                        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                //移除照片
                                ll_photos.removeView(view);

                                //picPath删去相应图片的链接
                                list.remove((int) view.getTag());
                                picPath = "";
                                for (int j = 0; j < list.size(); j++) {
                                    picPath = picPath + list.get(j) + ";";
                                }

                                //为了放大查看图片时而更新数据
                                imgUrls = new String[list.size()];
                                imgUrls = list.toArray(imgUrls);
                                //更新每张图片的tag
                                for (int j = 0; j < ll_photos.getChildCount(); j++) {
                                    ll_photos.getChildAt(j).setTag(j);
                                }

                                arg0.dismiss();
                            }
                        });
                        ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });
                        ab.setCancelable(false);
                        ab.create().show();

                        //在长按操作之后不再加入短按操作
                        return true;

                    }
                });
                ll_photos.addView(view);

            }

        } else {
            findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
        }
    }

    /**
     * 接下来是定位相关的代码
     * String:locate_temp:当前的定位数据
     */


    Handler handler = new Handler();
    private boolean isFirstLoc = true;
    LocationClient mLocClient;
    public MyLocationListener myListener = new MyLocationListener();
    List<LatLng> points = new ArrayList<LatLng>();//全部点
    List<LatLng> threePointsToOnePoint = new ArrayList<LatLng>();//读取三个点，选取历史记录点距离居中的点
    LatLng point;
    //测试巡湖轨迹点数
    int countOfHandler = 0;

    //计算百度地图两点的距离
    public Double getDistance(LatLng point1,LatLng point2) {
        double lat1, lng1,lat2, lng2;
        lat1=point1.latitude;
        lng1=point1.longitude;
        lat2=point2.latitude;
        lng2=point2.longitude;

        Double R=6370996.81;  //地球的半径
    /*
     * 获取两点间x,y轴之间的距离
     */
        Double x = (lng2 - lng1)*Math.PI*R*Math.cos(((lat1+lat2)/2)*Math.PI/180)/180;
        Double y = (lat2 - lat1)*Math.PI*R/180;
        Double distance = Math.hypot(x, y);   //得到两点之间的直线距离
        return   distance;
    }

    /**
     * 地图初始化
     */
    public void initLocation() {

        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);//设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setLocationNotify(true);//设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setPriority(LocationClientOption.GpsFirst);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ChiefEditRecord Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());

        isRunMyRunable = true;

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        points.clear();
        if(isAddNewLakeRecord){
//			showToastCenter(ChiefEditRecordActivity.getCurActivity(),"除拍照等操作外\n请在【查看轨迹】界面方便记录轨迹");
            //线程启动
            isRunMyRunable = true;
            handler.postDelayed(myRunable_record, 2000); //改为每2s记录一次当前轨迹
            initLocation();
            //开启定时器
            startTimer();
        }
    }

    /**
     * 地图sdk监听器
     */
    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation == null)
                return;

            if (!PatrolRecordUtils.isOPen(getApplicationContext())) {
                showMyToast(Toast.makeText(LakeChiefEditRecordActivity.this, "定位未开启，请打开定位。", Toast.LENGTH_LONG),900);
            }
            if (bdLocation.getLocType() == BDLocation.TypeNetWorkException || bdLocation.getLocType() == BDLocation.TypeCriteriaException){
                Toast.makeText(LakeChiefEditRecordActivity.this, "信号弱，请确定网络是否通畅。", Toast.LENGTH_SHORT).show();
            }else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocationFail || bdLocation.getLocType() == BDLocation.TypeOffLineLocationNetworkFail) {
                Toast.makeText(LakeChiefEditRecordActivity.this, "定位失败，请检查。", Toast.LENGTH_SHORT).show();
            }
            LatLng point = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            imgLngTemp=bdLocation.getLongitude();
            imgLatTemp=bdLocation.getLatitude();
            points.add(point);

        }
    }

    /**
     * 每2s记录一次当前坐标值
     */
    private class MyRunable implements Runnable {

        @Override
        public void run() {
//			showToast(String.valueOf(countOfHandler));
//			if(isRunMyRunable){
            if (!mLocClient.isStarted()) {
                mLocClient.start();
                System.out.println("testrc: ER+mLocClient.isStarted()");
            }

            if (points == null) {
                handler.postDelayed(this, 2000);
                System.out.println("testrc: ER+points == null");
                return;
            }

            if (isFirstLoc && points.size() >= 1 && latlist_temp == null) {
                //若从inspect返回（latlist_temp != null)就不执行这里的代码
                if(points.size()>15){
                    isFirstLoc = false;
                    lnglist_temp = "" + points.get(points.size() - 1).longitude;
                    latlist_temp = "" + points.get(points.size() - 1).latitude;
                    point = points.get(points.size() - 1);
                    Log.i("temp:", "first" + latlist_temp);
                    System.out.println("testrc: ER+ points.size()>20");
                }
                System.out.println("testrc: ER+ isFirstLoc && points.size() >= 1 && latlist_temp == null");
            } else if (points.size() > 1) {
                //要解决从inspect返回时point的经纬度
                System.out.println("testrc: ER+points.size() > 1");
                if(point == null){
                    point = points.get(points.size() - 1);
                    if(lnglist_temp.equals("")){
                        lnglist_temp = "" + point.longitude;
                        latlist_temp = "" + point.latitude;
                    }else {
                        lnglist_temp = lnglist_temp + "," + point.longitude;
                        latlist_temp = latlist_temp + "," + point.latitude;
                    }
                }
//					DistanceUtil.getDistance(point,points.get(points.size()-1))
                if (DistanceUtil.getDistance(point,points.get(points.size()-1))<150 &&
                        DistanceUtil.getDistance(point,points.get(points.size()-1))>0.5) { //如果一直处于一个位置则不重复记录
                    System.out.println("testrc: ER+getDistance");
//						showToast("ER"+String.valueOf(countOfHandler++)+String.valueOf(getDistance(point,points.get(points.size()-1))));

                    point = points.get(points.size()-1);
                    lnglist_temp = lnglist_temp + "," + point.longitude;
                    latlist_temp = latlist_temp + "," + point.latitude;
                    System.out.println("testrc: ER"+latlist_temp);
                    Log.i("temp:", latlist_temp);

                }else {
                    if(DistanceUtil.getDistance(point,points.get(points.size()-1))>=150){
                        countNoJoin++;
                        if (countNoJoin >=5){
                            countNoJoin = 0;
                            if (lnglist_temp.contains(",")){
                                //如果不止一个数据，变成一个数组
                                lat_array = latlist_temp.split(",");
                                lng_array = lnglist_temp.split(",");
                            }else{
                                //如果只有一个数据
                                lat_array = new String[1];
                                lng_array = new String[1];
                                lat_array[0] = latlist_temp;
                                lng_array[0] = lnglist_temp;
                            }
                            lat_array[lat_array.length-1] = String.valueOf(points.get(points.size()-1).latitude);
                            lng_array[lat_array.length-1] = String.valueOf(points.get(points.size()-1).longitude);
                            lnglist_temp = lng_array[0];
                            latlist_temp = lat_array[0];
                            for (int k = 1;k<lat_array.length;k++){
                                lnglist_temp = lnglist_temp + "," + lng_array[k];
                                latlist_temp = latlist_temp + "," + lat_array[k];
                            }

                            point = points.get(points.size()-1);
                        }
                    }else {
                        countNoJoin = 0;
                    }
                }
            }
//				showToast(String.valueOf(countOfHandler++));
            handler.postDelayed(this, 2000);
//			}
        }
    }
    //对轨迹的数组，取最新的三个点并按照与point的距离排序，最后取三个数的中间一个
    private LatLng medianFilterOfPoints(List<LatLng> pointsTemp,LatLng pointTemt){
        double one, two, three;
        LatLng decidePoint;
        if (pointsTemp.size()<3){
            return pointsTemp.get(pointsTemp.size() - 1);
        }else {
            one = DistanceUtil.getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 1));//倒数第一个点与pointTemt的距离
            two = DistanceUtil.getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 2));//倒数第2个点与pointTemt的距离
            three = DistanceUtil.getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 3));//倒数第3个点与pointTemt的距离
            if((one>=two && one<three)||(one >=three && one<two)){
                decidePoint = pointsTemp.get(pointsTemp.size() - 1);
            }else if((two>one && two<three)||(two>=three && two<=one)){
                decidePoint = pointsTemp.get(pointsTemp.size() - 2);
            }else {
                decidePoint = pointsTemp.get(pointsTemp.size() - 3);
            }
            return decidePoint;
        }
    }
    //轨迹平滑滤波。SG5点1阶平滑。
    public String[] OptimizePoints(String latListIn,String lngListIn){
        String latListOut = new String("");
        String lngListOut = new String("");//输出经纬度字符串
        String[] latListInArray;
        String[] lngListInArray;//输出经纬度数组
        ArrayList<LatLng> pointsIn = new ArrayList<LatLng>();//输入点字符串转化

        double latitude;
        double longitude;

        if(latListIn!=null && !latListIn.equals("")){
            if (latListIn.contains(",")){
                //如果不止一个数据，变成一个数组
                latListInArray = latListIn.split(",");
                lngListInArray = lngListIn.split(",");
            }else{
                //如果只有一个数据
                latListInArray = new String[1];
                lngListInArray = new String[1];
                latListInArray[0] = latListIn;
                lngListInArray[0] = lngListIn;
            }
            //是否有超过5 个点
            if(latListInArray.length>=5){
                for (int i = 0; i < latListInArray.length; i++){
                    pointsIn.add(new LatLng(Double.parseDouble(latListInArray[i]), Double.parseDouble(lngListInArray[i])));
                }
                int sizeOfPointIn = pointsIn.size();
                for(int i = 0; i < sizeOfPointIn;i++){
                    if(i==0){//第一个
                        latitude =  (3*pointsIn.get(0).latitude+2*pointsIn.get(1).latitude+pointsIn.get(2).latitude-pointsIn.get(4).latitude)/5;
                        longitude = (3*pointsIn.get(0).longitude+2*pointsIn.get(1).longitude+pointsIn.get(2).longitude-pointsIn.get(4).longitude)/5;
                        latListOut = latListOut + latitude;
                        lngListOut = lngListOut + longitude;
                    }else if(i == 1){
                        latitude =  (4*pointsIn.get(0).latitude+3*pointsIn.get(1).latitude+2*pointsIn.get(2).latitude+pointsIn.get(3).latitude)/10;
                        longitude = (4*pointsIn.get(0).longitude+3*pointsIn.get(1).longitude+2*pointsIn.get(2).longitude+pointsIn.get(3).longitude)/10;
                        latListOut = latListOut + "," + latitude;
                        lngListOut = lngListOut + ","+ longitude;
                    }else if(i==sizeOfPointIn-1){//最后1个点
                        latitude =  (3*pointsIn.get(sizeOfPointIn-1).latitude+2*pointsIn.get(sizeOfPointIn-2).latitude+
                                pointsIn.get(sizeOfPointIn-3).latitude-pointsIn.get(sizeOfPointIn-5).latitude)/5;
                        longitude = (3*pointsIn.get(sizeOfPointIn-1).longitude+2*pointsIn.get(sizeOfPointIn-2).longitude+
                                pointsIn.get(sizeOfPointIn-3).longitude-pointsIn.get(sizeOfPointIn-5).longitude)/5;
                        latListOut = latListOut + ","+ latitude;
                        lngListOut = lngListOut +","+ longitude;
                    }else if(i == sizeOfPointIn-2){
                        latitude =  (4*pointsIn.get(sizeOfPointIn-1).latitude+3*pointsIn.get(sizeOfPointIn-2).latitude+
                                2*pointsIn.get(sizeOfPointIn-3).latitude+pointsIn.get(sizeOfPointIn-4).latitude)/10;
                        longitude = (4*pointsIn.get(sizeOfPointIn-1).longitude+3*pointsIn.get(sizeOfPointIn-2).longitude+
                                2*pointsIn.get(sizeOfPointIn-3).longitude+pointsIn.get(sizeOfPointIn-4).longitude)/10;
                        latListOut = latListOut + "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }else{// 中间的点
                        latitude =  (pointsIn.get(i-1).latitude+pointsIn.get(i-2).latitude+pointsIn.get(i).latitude+
                                pointsIn.get(i+1).latitude+pointsIn.get(i+2).latitude)/5;
                        longitude = (pointsIn.get(i-1).longitude+pointsIn.get(i-2).longitude+pointsIn.get(i).longitude+
                                pointsIn.get(i+1).longitude+pointsIn.get(i+2).longitude)/5;
                        latListOut = latListOut+ "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }
                }
            }else {
                latListOut = latListIn;
                lngListOut = lngListIn;
            }

        }
        String[] out = {latListOut,lngListOut};
        return out;
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: ThimOs was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());

//		getUser().setBaiduLatPoints(OptimizePoints(latlist_temp,lnglist_temp)[0]);
//		getUser().setBaiduLngPoints(OptimizePoints(latlist_temp,lnglist_temp)[1]);
        getUser().setBaiduLatPoints(latlist_temp);
        getUser().setBaiduLngPoints(lnglist_temp);

//		points.clear();
        System.out.println("testrc: on stopppppppppppppppppppp");
//		if(mLocClient!=null){
//			mLocClient.unRegisterLocationListener(myListener);
//		}
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
        isRunMyRunable = false;
        //关闭定时器
        stopTimer();

    }

    //防止退出当前activity之后还在继续定位
    @Override
    protected void onDestroy() {
        //关闭定时器
        stopTimer();

        System.out.println("testrc: ER+onDestroyyyyyyyyyyyyyyyyyy");
        points.clear();
        if(mLocClient!=null){
            mLocClient.stop();
            mLocClient.unRegisterLocationListener(myListener);
        }
        client.disconnect();
        isRunMyRunable = false;
        super.onDestroy();
    }
}
