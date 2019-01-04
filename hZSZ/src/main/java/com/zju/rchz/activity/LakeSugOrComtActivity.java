package com.zju.rchz.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sin.android.sinlibs.base.Callable;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.model.Lake;
import com.zju.rchz.model.River;
import com.zju.rchz.model.SugOrComRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.service.LocalService;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

/**
 * Created by ZJTLM4600l on 2018/7/6.
 */

public class LakeSugOrComtActivity extends BaseActivity {
//    River river = null;   //投诉河段

    Lake lake = null;
    int segment_ix = -1;   //分段信息
    private boolean isCom = false;   //投诉还是建议
    private Location location = null;
    //照片位置
    private Location imgLocation = null;

    private Lake l;

    private LinearLayout ll_cboxes = null;
    private boolean isReadOnly = false;

    private String imgLnglist="";//图片经纬度
    private String imgLatlist="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		mLocClient=new LocationClient(this);
//		mLocClient.registerLocationListener(new MLocationListener());

        setContentView(R.layout.activity_suggest);
        findViewById(R.id.sv_form).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_submit_result).setVisibility(View.GONE);
        findViewById(R.id.ll_gps).setVisibility(View.GONE);

        initHead(R.drawable.ic_head_close, 0);

        l = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_LAKE), Lake.class);
        int ix = getIntent().getIntExtra(Tags.TAG_INDEX, -1);
        isCom = getIntent().getBooleanExtra(Tags.TAG_ABOOLEAN, false);

        if (isCom) {
            // 投诉
            ((TextView) findViewById(R.id.tv_suggest_river)).setText(R.string.form_complaint_lake);
            ((CheckBox) findViewById(R.id.ck_anonymity)).setText(R.string.form_complaint_anonymity);

            ((EditText) findViewById(R.id.et_suggest_name)).setHint(R.string.hint_com_name);

            ((EditText) findViewById(R.id.et_suggest_subject)).setHint(R.string.hint_com_subject);
        } else {
            // 建议
            ((TextView) findViewById(R.id.tv_suggest_river)).setText(R.string.form_suggest_lake);
            findViewById(R.id.hsv_photos).setVisibility(View.GONE);
        }

        setTitle(isCom ? R.string.icomplaint : R.string.isuggest);


        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                submmit();
            }
        });

        findViewById(R.id.ib_photo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                getImgLocation();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
                // values.put(MediaStore.Images.Media.WIDTH, "800");
                // values.put(MediaStore.Images.Media.HEIGHT, "600");
                // values.put(MediaStore.Images.Media.DESCRIPTION,
                // "this is description");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);

                startActivityForResult(intent, Tags.CODE_ADDPHOTO);
            }
        });

        ((CheckBox) findViewById(R.id.ck_anonymity)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean cked) {
                ((TextView) findViewById(R.id.tv_anonymity_tip)).setText(cked ? R.string.tip_anonymity_checked : R.string.tip_anonymity_uncheck);
            }
        });

        ((CheckBox) findViewById(R.id.ck_gps)).setChecked(!getUser().gpsdisable);

        ((CheckBox) findViewById(R.id.ck_gps)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean cked) {
                ((TextView) findViewById(R.id.tv_gps_tip)).setText(cked ? "(温馨提示:您的GPS定位信息将提交到服务器，用于投诉地图显示)" : "(温馨提示:您取消了定位)");
                if (cked)
                    getLocation();
            }
        });

        if (getUser().isLogined()) {
            ((EditText) findViewById(R.id.et_phonenum)).setText(getUser().userName);
        }

        if (l != null) {
//            if (r.isPiecewise() && ix < 0) {
//                readyToSugOrCom(r);
//            } else {
//                setWithRiver(r, ix);
//            }
            setWithLake(l);
        } else {
//			selectRiver();
        }

        findViewById(R.id.tv_rivername).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                selectRiver();
                selectLake();
            }
        });
    }

    private void selectLake() {
        Intent intent = new Intent(this, LakeListAcitivity.class);
        intent.putExtra(Tags.TAG_CODE, Tags.CODE_SELECTLAKE);
        if (isCom){
            intent.putExtra(Tags.TAG_TITLE, "选择投诉湖泊");
        }
        else{
            intent.putExtra(Tags.TAG_TITLE, "选择建议湖泊");
        }
        startActivityForResult(intent, Tags.CODE_SELECTLAKE);
    }

//    private void readyToSugOrCom(final River river) {
//        if (river.isPiecewise()) {
//
//            //按是否下级河道处理
//            String[] names = new String[river.lowLevelRivers.length];
//            for (int i = 0; i < names.length; ++i) {
//                names[i] = river.lowLevelRivers[i].riverName;
//            }
//            Dialog alertDialog = new AlertDialog.Builder(this).setTitle(R.string.river_select_segment).setItems(names, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    setWithRiver(river, which);
//                }
//            }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                }
//            }).setCancelable(false).create();
//            alertDialog.show();
//        } else {
//            setWithRiver(river, -1);
//        }
//    }

//    //为各个按钮绑定监听器
//    private void btnInit() {
//        int[] btnToInit = {
//                R.id.cb_good_1, R.id.cb_good_2, R.id.cb_good_3,
//                R.id.cb_medium_1, R.id.cb_medium_2, R.id.cb_medium_3,
//                R.id.cb_bad_1, R.id.cb_bad_2, R.id.cb_bad_3
//        };
//
//        for (int id: btnToInit) {
//            View v = findViewById(id);
//            if (v != null)
//                ((CompoundButton) v).setOnCheckedChangeListener(cclTogTagNpc);
//        }
//    }
//
//    //各个按钮的监听器
//    private CompoundButton.OnCheckedChangeListener cclTogTagNpc = new CompoundButton.OnCheckedChangeListener() {
//        @Override
//        public void onCheckedChanged(CompoundButton cb, boolean c) {
//
//            if (c) {
//                switch (cb.getId()) {
//                    case R.id.cb_good_1 : {
//                        chiefPatrol = 1;
//                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
//                        break;
//                    }
//                    case R.id.cb_bad_1 : {
//                        chiefPatrol = 3;
//                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_1)).setChecked(false);
//                        break;
//                    }
//
//                    case R.id.cb_medium_1 : {
//                        chiefPatrol = 2;
//                        ((CompoundButton) findViewById(R.id.cb_bad_1)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_good_1)).setChecked(false);
//                        break;
//                    }
//
//                    case R.id.cb_good_2 : {
//                        chiefFeedBack = 1;
//                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
//                        break;
//                    }
//                    case R.id.cb_bad_2 : {
//                        chiefFeedBack = 3;
//                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_2)).setChecked(false);
//                        break;
//                    }
//
//                    case R.id.cb_medium_2 : {
//                        chiefFeedBack = 2;
//                        ((CompoundButton) findViewById(R.id.cb_bad_2)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_good_2)).setChecked(false);
//                        break;
//                    }
//
//                    case R.id.cb_good_3 : {
//                        chiefWork = 1;
//                        ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
//                        break;
//                    }
//                    case R.id.cb_bad_3 : {
//                        chiefWork = 3;
//                        ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_medium_3)).setChecked(false);
//                        break;
//                    }
//
//                    case R.id.cb_medium_3 : {
//                        chiefWork = 2;
//                        ((CompoundButton) findViewById(R.id.cb_bad_3)).setChecked(false);
//                        ((CompoundButton) findViewById(R.id.cb_good_3)).setChecked(false);
//                        break;
//                    }
//
//                    default:
//                        break;
//                }
//            }
//
//        }
//    };

	/*建议表单相关 end*/

//    private void setWithRiver(River r, int ix) {
//        if (r != null) {
//            river = r;
//            segment_ix = ix;
//
//            if (river != null) {
//                if (segment_ix == -1) {
//                    ((TextView) findViewById(R.id.tv_rivername)).setText(river.riverName);
//                } else {
//                    ((TextView) findViewById(R.id.tv_rivername)).setText(river.lowLevelRivers[segment_ix].riverName);
//                }
//            } else {
//                showToast("没有传入河道参数");
//                finish();
//            }
//        }
//    }
    private void setWithLake(Lake l) {
        if (l != null) {
            lake = l;

            if (lake != null) {
                if(l.isTownLake()){
                    ((TextView) findViewById(R.id.tv_rivername)).setText(lake.lakeName);
                }else {
                    showToast("市级/村级湖泊不能投诉（建议）。");
                }

            } else {
                showToast("没有传入湖泊参数");
                finish();
            }
        }
    }

    private Uri imageFilePath = null;
    private String uname = null;

    private String subject = null;
    private String contentt = null;
    private String telno = null;
    private boolean anonymity = false;
//    private int rid = 0;

    private int lakeId = 0;
    private String picbase64 = null;

    private void submmit() {
        uname = ((EditText) findViewById(R.id.et_suggest_name)).getText().toString().trim();
        //注意了，如果是人大，没有这两个编辑框。
//		subject = subject == null ? ((EditText) findViewById(R.id.et_suggest_subject)).getText().toString().trim(): subject;
        subject = ((EditText) findViewById(R.id.et_suggest_subject)).getText().toString().trim();

        contentt = ((EditText) findViewById(R.id.et_suggest_contentt)).getText().toString().trim();
        telno = ((EditText) findViewById(R.id.et_phonenum)).getText().toString().trim();

        if (uname.length() == 0) {
            showToast("姓名不能为空!");
            ((EditText) findViewById(R.id.et_suggest_name)).requestFocus();
            return;
        }

        if (lake == null) {
            if(isCom){
                showToast("请选择要投诉的湖泊");
            }else {
                showToast("请选择要建议的湖泊");
            }

            return;
        }

        if (subject.length() == 0) {
            showToast("主题不能为空!");
            ((EditText) findViewById(R.id.et_suggest_subject)).requestFocus();
            return;
        }

        if (contentt.length() == 0) {
            if (getUser().isNpc() && !isCom) {
                showToast("其他建议不能为空!");
                ((EditText) findViewById(R.id.et_npc_otherquestion)).requestFocus();
            } else{
                showToast("内容描述不能为空!");
                ((EditText) findViewById(R.id.et_suggest_contentt)).requestFocus();
            }
            return;
        }

        if (uname.length() == 0) {
            showToast("姓名不能为空!");
            ((EditText) findViewById(R.id.et_suggest_name)).requestFocus();
            return;
        }

        anonymity = ((CheckBox) findViewById(R.id.ck_anonymity)).isChecked();

        lakeId = lake.lakeId;
        LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_photos);
        if (ll_photos.getChildCount() > 1) {
            // 有图片
            final Bitmap[] bmps = new Bitmap[ll_photos.getChildCount() - 1];
            for (int i = 0; i < bmps.length; ++i) {
                bmps[i] = (Bitmap) ll_photos.getChildAt(i).getTag();
            }
            asynCallAndShowProcessDlg("正在处理图片...", new Callable() {

                @Override
                public void call(Object... args) {

                    StringBuffer sb = new StringBuffer();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 1024);
                    for (Bitmap bmp : bmps) {
                        if (sb.length() > 0)
                            sb.append(";");
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        byte[] bts = baos.toByteArray();
                        Log.i("NET", "bts.len " + bts.length);
                        sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
                        baos.reset();
                    }
                    picbase64 = sb.toString();
                    // Log.i("PIC", "base64 " + picbase64);
                    Log.i("PIC", "base64.len " + picbase64.length());

                    safeCall(new Callable() {
                        @Override
                        public void call(Object... args) {
                            submitData();
                        }
                    });
                }
            });
        } else{
            if(isCom){
                Toast.makeText(this,"您还没拍摄照片，请上传照片完成投诉。", Toast.LENGTH_LONG).show();
            }else {
                submitData();
            }

        }


        // submitData();

        // startAuthCode();
    }

    private void submitData() {
        showOperating();
        JSONObject p = null;
        if (isCom) {
            p = ParamUtils.freeParam(null, "lakeId", lakeId, "compTheme", subject, "compContent", contentt, "compName", uname, "compTelephone", telno, "ifAnonymous", anonymity ? 1 : 0, "picBase64", picbase64 == null ? "" : picbase64,"imgLatlist",imgLatlist,"imgLnglist",imgLnglist);
        } else {
            p = ParamUtils.freeParam(null, "lakeId", lakeId, "advTheme", subject, "advContent", contentt, "advName", uname, "advTelephone", telno, "ifAnonymous", anonymity ? 1 : 0);
        }

        if (location != null && ((CheckBox) findViewById(R.id.ck_gps)).isChecked()) {
            try {
                p.put("latitude", location.getLatitude());
                p.put("longtitude", location.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isCom) {
            getRequestContext().add("lakecomplaints_action_add", new Callback<SugOrComRes>() {
                @Override
                public void callback(SugOrComRes o) {
                    if (o != null && o.isSuccess()) {
                        imgLatlist="";
                        imgLnglist="";
                        doResult(o);
                    } else {
                        hideOperating();
                    }
                }
            }, SugOrComRes.class, p);
        } else {
            getRequestContext().add("lakeadvise_action_add", new Callback<SugOrComRes>() {
                @Override
                public void callback(SugOrComRes o) {
                    if (o != null && o.isSuccess()) {
                        doResult(o);
                    } else {
                        hideOperating();
                    }
                }
            }, SugOrComRes.class, p);
        }
    }

    private void doResult(SugOrComRes o) {
        findViewById(R.id.sv_form).setVisibility(View.GONE);
        findViewById(R.id.ll_submit_result).setVisibility(View.VISIBLE);

        // String fid = ("T" + System.currentTimeMillis()).substring(0, 6);

        ((TextView) findViewById(R.id.tv_submit_result)).setText(StrUtils.renderText(LakeSugOrComtActivity.this, isCom ? R.string.fmt_com_submit_result : R.string.fmt_sug_submit_result, o.data.comOrAdvSerNum));

        hideOperating();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Tags.CODE_ADDPHOTO && resultCode == RESULT_OK) {

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
            View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
            BitmapFactory.Options op = new BitmapFactory.Options();
            try {
                Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageFilePath), null, op);
                view.setTag(pic);
                Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
                ((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ((LinearLayout) findViewById(R.id.ll_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_photos)).getChildCount() - 1);

            if(((LinearLayout) findViewById(R.id.ll_photos)).getChildCount()==2){
                if(imgLocation!=null){
                    imgLnglist=imgLnglist+imgLocation.getLongitude();
                    imgLatlist=imgLatlist+imgLocation.getLatitude();
                }
            }else {
                if (imgLocation!=null){
                    imgLnglist=imgLnglist+","+imgLocation.getLongitude();
                    imgLatlist=imgLatlist+","+imgLocation.getLatitude();
                }
            }



        } else if (requestCode == Tags.CODE_SELECTLAKE && resultCode == RESULT_OK) {
            Lake l = StrUtils.Str2Obj(data.getStringExtra(Tags.TAG_LAKE), Lake.class);
            if (l != null) {
//                readyToSugOrCom(r);
                setWithLake(l);
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (!getUser().gpsdisable)
            getLocation();
    }

    private void getLocation() {
        if (getLocalService() != null) {
            getLocalService().getLocation(new LocalService.LocationCallback() {
                @Override
                public void callback(Location location) {
                    LakeSugOrComtActivity.this.location = location;
                }
            });
        }
    }
    private void getImgLocation() {
        if (getLocalService() != null) {
            getLocalService().getLocation(new LocalService.LocationCallback() {
                @Override
                public void callback(Location location) {
                    LakeSugOrComtActivity.this.imgLocation = location;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getLocalService() != null) {
            // getLocalService().cl
        }
//		mLocClient.stop();
    }
}
