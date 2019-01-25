package com.zju.rchz.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.base.Callable;
import com.sin.android.sinlibs.tagtemplate.TemplateEngine;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.DateTime;
import com.zju.rchz.model.DubanTopersonData;
import com.zju.rchz.model.DubanTopersonDataRes;
import com.zju.rchz.model.DubanTopersonObject;
import com.zju.rchz.model.ReplyForInstruction;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.DipPxUtils;
import com.zju.rchz.utils.ImgUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;
import com.zju.rchz.utils.ViewUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * on 2019/1/17.
 * 批示和回复详情显示页面
 * 镇街河长以及 批示过的领导会使用
 */

public class InstructionAndReplyDetailActivity extends BaseActivity {
    private static final String TAG = "DuabnTopersonDetail";
    private DubanTopersonObject dubanTopersonObject=null;
    private DubanTopersonData dubanTopersonData=null;
    private boolean hasImg = false;
    private int ifReply;

    private boolean ischief=false;
    private boolean isSecretary = false;//身份判断
    private boolean isMayor = false;
    private boolean isOtherMayor = false;
    private boolean isBossChief = false;
    private boolean isCityChief = false;


    private TemplateEngine templateEngine = new TemplateEngine() {

        @Override
        public String evalString(Object model, String tmpl) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            String s = super.evalString(model, tmpl);
            if (s != null) {
                s = s.replace("建议", "上报");
                s = s.replace("投诉", "上报");
            }
            return s;
        }
    };

    private ViewRender viewRender = new ViewRender(templateEngine);

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_and_reply_detail);
        dubanTopersonObject = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), DubanTopersonObject.class);
        ifReply = getIntent().getIntExtra(Tags.TAG_HANDLED,0);

        //身份判断
        ischief = getUser().isLogined() && (getUser().isChief()|| getUser().isLakeChief());
        isSecretary = getUser().isLogined() && getUser().isSecretary();
        isMayor = getUser().isLogined() && getUser().isMayor();
        isOtherMayor = getUser().isLogined() && getUser().isOtherMayor();
        isBossChief = getUser().isLogined() && getUser().isBossChief();
        isCityChief = getUser().isLogined() && getUser().isCityChief();
        findViewById(R.id.tv_sgin).setVisibility(View.GONE);

        //是否有回复框（用于再次批示）
        if (ischief) {
            findViewById(R.id.ll_make_intruction).setVisibility(View.GONE);
        }else if(ifReply==1){
            findViewById(R.id.ll_make_intruction).setVisibility(View.GONE);
        }else{
            findViewById(R.id.ll_make_intruction).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tv_return)).setText("再次批示");
            ((EditText)findViewById(R.id.et_make_intruction_handlecontent)).setHint("如有需要，请再次批示");
        }

        if (dubanTopersonObject != null) {

            initHead(R.drawable.ic_head_back, 0);
            setTitle("批示和回复");

            ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "建议", "上报");
            ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "投诉", "上报");
            if (dubanTopersonObject.getRiverOrLake().intValue()==1){
                ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "河道", "湖泊");
            }

            findViewById(R.id.btn_make_intruction_submit).setOnClickListener(this);
            findViewById(R.id.btn_make_intruction_store).setOnClickListener(this);
            findViewById(R.id.btn_make_intruction_cancel).setOnClickListener(this);
            findViewById(R.id.tv_complain_map).setOnClickListener(this);

            findViewById(R.id.sv_main).setVisibility(View.INVISIBLE);
            viewRender.renderView(findViewById(R.id.ll_root), dubanTopersonObject);

            findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    startAddPhoto();
                }
            });

            loadData();
        } else {
            showToast("没有处理单信息");
            finish();
        }
        Log.d(TAG,"InstructionAndReplyDetailActivity");
    }

    private String[] imgUrls = null;

    private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag(R.id.tag_first) instanceof Integer) {
                Intent intent = new Intent(InstructionAndReplyDetailActivity.this, PhotoViewActivity.class);
                intent.putExtra("URLS", (String [])v.getTag(R.id.tag_second));
                intent.putExtra("CUR", ((Integer) v.getTag(R.id.tag_first)).intValue());
                startActivity(intent);
            }
        }

    };

    private void loadData() {
        showOperating();

        JSONObject params;
        final String request;

        //获取督办单详情
        request = "Get_InstructionContent";
        params = ParamUtils.freeParam(null, "dubanId" , dubanTopersonObject.getId());

        getRequestContext().add(request, new Callback<DubanTopersonDataRes>() {
            @Override
            public void callback(DubanTopersonDataRes o) {
                hideOperating();

                if (o != null && o.isSuccess()) {
                    dubanTopersonData = o.data;
                    viewRender.renderView(findViewById(R.id.ll_root), dubanTopersonData);

                    ((TextView) findViewById(R.id.tv_user_name)).setText("***");
                    ((TextView) findViewById(R.id.tv_user_telno)).setText("***********");

                    findViewById(R.id.sv_main).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.tv_datetime)).setText(dubanTopersonData.getSubmitTime() != null ? dubanTopersonData.getSubmitTime().getYMDHM(InstructionAndReplyDetailActivity.this) : "");

                    //回复书记批示
                    final LinearLayout ll_reply_secretary = (LinearLayout) findViewById(R.id.ll_reply_secretary);
                    ll_reply_secretary.removeAllViews();
                    //回复市长批示
                    final LinearLayout ll_reply_mayor = (LinearLayout) findViewById(R.id.ll_reply_mayor);
                    ll_reply_mayor.removeAllViews();
                    //回复分管市长批示
                    final LinearLayout ll_reply_other_mayors = (LinearLayout) findViewById(R.id.ll_reply_other_mayor);
                    ll_reply_other_mayors.removeAllViews();
                    //回复市级河长批示
                    final LinearLayout ll_reply_citychief = (LinearLayout) findViewById(R.id.ll_reply_citychief);
                    ll_reply_citychief.removeAllViews();
                    //回复镇街总河长批示
                    final LinearLayout ll_reply_boss_chief = (LinearLayout) findViewById(R.id.ll_reply_boss_chief);
                    ll_reply_boss_chief.removeAllViews();

                    //是否有市委书记批示结果
                    if(dubanTopersonData.getInstructionFromSecretary()!=null && dubanTopersonData.getInstructionFromSecretary()!=""){
                        findViewById(R.id.ll_party_secretary_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_partySecretaryIntruction_time))
                                .setText(dubanTopersonData.getInstructionFromSecretaryDate() != null ? dubanTopersonData.getInstructionFromSecretaryDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_partySecretaryIntruction)).setText(dubanTopersonData.getInstructionFromSecretary());
                        if(ischief){
                            findViewById(R.id.btn_reply_secretary).setVisibility(View.VISIBLE);
                            findViewById(R.id.btn_reply_secretary).setOnClickListener(btReplyInstruction);
                        }else{
                            findViewById(R.id.btn_reply_secretary).setVisibility(View.GONE);
                        }
                    }else {
                        findViewById(R.id.ll_party_secretary_intruction).setVisibility(View.GONE);
                    }
                    //是否有市长批示结果
                    if(dubanTopersonData.getInstructionFromMayor()!=null && dubanTopersonData.getInstructionFromMayor()!=""){
                        findViewById(R.id.ll_mayor_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_mayorIntruction_time))
                                .setText(dubanTopersonData.getInstructionFromMayorDate() != null ? dubanTopersonData.getInstructionFromMayorDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_mayorIntruction)).setText(dubanTopersonData.getInstructionFromMayor());
                        if(ischief){
                            findViewById(R.id.btn_reply_mayor).setVisibility(View.VISIBLE);
                            findViewById(R.id.btn_reply_mayor).setOnClickListener(btReplyInstruction);
                        }else{
                            findViewById(R.id.btn_reply_mayor).setVisibility(View.GONE);
                        }
                    }else {
                        findViewById(R.id.ll_mayor_intruction).setVisibility(View.GONE);
                    }
                    //是否有分管市长批示结果
                    if(dubanTopersonData.getInstructionFromOtherMayor()!=null && dubanTopersonData.getInstructionFromOtherMayor()!=""){
                        findViewById(R.id.ll_other_mayor_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_otherMayorIntruction_time))
                                .setText(dubanTopersonData.getInstructionFromOtherMayorDate() != null ? dubanTopersonData.getInstructionFromOtherMayorDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_otherMayorIntruction)).setText(dubanTopersonData.getInstructionFromOtherMayor());
                        if(ischief){
                            findViewById(R.id.btn_reply_othormayor).setVisibility(View.VISIBLE);
                            findViewById(R.id.btn_reply_othormayor).setOnClickListener(btReplyInstruction);
                        }else{
                            findViewById(R.id.btn_reply_othormayor).setVisibility(View.GONE);
                        }
                    }else {
                        findViewById(R.id.ll_other_mayor_intruction).setVisibility(View.GONE);
                    }

                    //是否有市级河长批示结果
                    if(dubanTopersonData.getInstructionFromCityChief()!=null && dubanTopersonData.getInstructionFromCityChief()!=""){
                        findViewById(R.id.ll_city_chief_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_cityChiefIntruction_time))
                                .setText(dubanTopersonData.getInstructionFromCityChiefDate() != null ? dubanTopersonData.getInstructionFromCityChiefDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_cityChiefIntruction)).setText(dubanTopersonData.getInstructionFromCityChief());
                        if(ischief){
                            findViewById(R.id.btn_reply_citychief).setVisibility(View.VISIBLE);
                            findViewById(R.id.btn_reply_citychief).setOnClickListener(btReplyInstruction);
                        }else{
                            findViewById(R.id.btn_reply_citychief).setVisibility(View.GONE);
                        }
                    }else {
                        findViewById(R.id.ll_city_chief_intruction).setVisibility(View.GONE);
                    }
                    //是否有镇街总河长批示结果
                    if(dubanTopersonData.getInstructionFromBossChief()!=null && dubanTopersonData.getInstructionFromBossChief()!=""){
                        findViewById(R.id.ll_boss_chief_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_bossChiefIntruction_time))
                                .setText(dubanTopersonData.getInstructionFromBossChiefDate() != null ? dubanTopersonData.getInstructionFromBossChiefDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_bossChiefIntruction)).setText(dubanTopersonData.getInstructionFromBossChief());
                        if(ischief){
                            findViewById(R.id.btn_reply_bosschief).setVisibility(View.VISIBLE);
                            findViewById(R.id.btn_reply_bosschief).setOnClickListener(btReplyInstruction);
                        }else{
                            findViewById(R.id.btn_reply_bosschief).setVisibility(View.GONE);
                        }
                    }else {
                        findViewById(R.id.ll_boss_chief_intruction).setVisibility(View.GONE);
                    }

                    //批示回复列表
                    ShowInstructionReplys(dubanTopersonData.getReplyForSecretaryList(),ll_reply_secretary);
                    ShowInstructionReplys(dubanTopersonData.getReplyForMayorList(),ll_reply_mayor);
                    ShowInstructionReplys(dubanTopersonData.getReplyForOtherMayorList(),ll_reply_other_mayors);
                    ShowInstructionReplys(dubanTopersonData.getReplyForCityChiefList(),ll_reply_citychief);
                    ShowInstructionReplys(dubanTopersonData.getReplyForBossChiefList(),ll_reply_boss_chief);
                    initPhotoView(dubanTopersonData.picPath);
                }
            }
        }, DubanTopersonDataRes.class, params);
    }

    /**
     * 批示回复(一项)
     * @param  replyPersonName;//河长姓名
     * @param  content;//内容
     * @param  picPath;//照片路径
     * @param  replyDate;//回复批示的日期
     * @return
     *
     */
    private View initContItem(String replyPersonName, String content, String picPath, DateTime replyDate) {
        View view = LinearLayout.inflate(this, R.layout.item_reply_instruction, null);
        ((TextView) view.findViewById(R.id.tv_replyIntruction_chief)).setText(replyPersonName);
        ((TextView) view.findViewById(R.id.tv_replyIntruction_content)).setText(content);
        ((TextView) view.findViewById(R.id.tv_replyIntruction_time))
                .setText(replyDate!= null ? replyDate.getYMDHM(InstructionAndReplyDetailActivity.this) : "");
        initReplyPhotoView(view,picPath);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        int dp1px = DipPxUtils.dip2px(getBaseContext(), getBaseContext().getResources().getDimension(R.dimen.linew));
        lp.setMargins(dp1px, dp1px, 0, 0);
        view.setLayoutParams(lp);
        return view;
    }
    //显示批示回复列表
    private void ShowInstructionReplys(ReplyForInstruction[] replyForInstructions,LinearLayout linearLayout){
        if (replyForInstructions.length>0){
            linearLayout.setVisibility(View.VISIBLE);
            for (int i=0;i<replyForInstructions.length;i++){
                LinearLayout replyForOtherMayorList= new LinearLayout(InstructionAndReplyDetailActivity.this);
                replyForOtherMayorList.addView(initContItem(replyForInstructions[i].getReplyPersonName(),
                        replyForInstructions[i].getContent(),
                        replyForInstructions[i].getPicPath(),
                        replyForInstructions[i].getReplyDate()));
                linearLayout.addView(replyForOtherMayorList);
                System.out.println("testrc: pishi : "+i+"..."+replyForInstructions[i].getContent());
                System.out.println("testrc: pishi : ");
            }
        }else {
            linearLayout.setVisibility(View.GONE);
        }
    }
    private View.OnClickListener btReplyInstruction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int flag = 0;//flag;  //回复的是哪个领导的批示 1市委书记 2市长 3分管市长 4镇街总河长 0或null市级河长
            String instructionContent = "";
            String instructionDate = "";
            switch (view.getId()){
                case R.id.btn_reply_secretary:
                    flag =1;
                    instructionContent = dubanTopersonData.getInstructionFromSecretary();
                    instructionDate = dubanTopersonData.getInstructionFromSecretaryDate() != null ? dubanTopersonData.getInstructionFromSecretaryDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "";
                    break;
                case R.id.btn_reply_mayor:
                    flag =2;
                    instructionContent = dubanTopersonData.getInstructionFromMayor();
                    instructionDate = dubanTopersonData.getInstructionFromMayorDate() != null ? dubanTopersonData.getInstructionFromMayorDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "";
                    break;
                case R.id.btn_reply_othormayor:
                    instructionContent = dubanTopersonData.getInstructionFromOtherMayor();
                    instructionDate = dubanTopersonData.getInstructionFromOtherMayorDate() != null ? dubanTopersonData.getInstructionFromOtherMayorDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "";
                    flag =3;
                    break;
                case R.id.btn_reply_citychief:
                    instructionContent = dubanTopersonData.getInstructionFromCityChief();
                    instructionDate = dubanTopersonData.getInstructionFromCityChiefDate() != null ? dubanTopersonData.getInstructionFromCityChiefDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "";
                    flag =0;
                    break;
                case R.id.btn_reply_bosschief:
                    instructionContent = dubanTopersonData.getInstructionFromBossChief();
                    instructionDate = dubanTopersonData.getInstructionFromBossChiefDate() != null ? dubanTopersonData.getInstructionFromBossChiefDate().getYMDHM(InstructionAndReplyDetailActivity.this) : "";
                    flag =4;
                    break;
            }
            Intent intent = new Intent(InstructionAndReplyDetailActivity.this, ReplyInstructionActivity.class);
            intent.putExtra("instructionContent", instructionContent);//将当前的巡河数据传至巡河地图界面
            intent.putExtra("instructionDate", instructionDate);
            intent.putExtra("flag",flag);
            intent.putExtra("dubanId",dubanTopersonObject.getId());
            startActivityForResult(intent, Tags.CODE_REPLY_INSTRUCTION);
        }
    };

    @Override
    public void onClick(View v) {
        int ope = 0;
        switch (v.getId()) {
            case R.id.btn_make_intruction_cancel: {
                finish();
                break;
            }
            case R.id.btn_make_intruction_submit:
                ope = 1;
            case R.id.btn_make_intruction_store: {
                String s = ((EditText) findViewById(R.id.et_make_intruction_handlecontent)).getText().toString().trim();
                if (s.length() > 0) {
                    opetype = ope;
                    dealContent = s;
                    submitData();
//                    LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
//                    if (ll_photos.getChildCount() <= 1){
//                        showToast("请您拍摄相关图片，在回复详情栏上传之后再提交");
//                        return;
//                    }
//                    if (ll_photos.getChildCount() > 1) {
//                        // 有图片
//                        hasImg = true;
//                        final Uri[] bmps = new Uri[ll_photos.getChildCount() - 1];
//                        for (int i = 0; i < bmps.length; ++i) {
//                            bmps[i] = (Uri) ll_photos.getChildAt(i).getTag();
//                        }
//                        asynCallAndShowProcessDlg("正在处理图片...", new Callable() {
//                            @Override
//                            public void call(Object... args) {
//
//                                StringBuffer sb = new StringBuffer();
//                                for (Uri bmp : bmps) {
//                                    if (sb.length() > 0)
//                                        sb.append(";");
//                                    byte[] bts = bmp2Bytes(bmp);
//                                    Log.i("NET", bmp.toString() + " bts.len " + bts.length);
//                                    sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
//                                }
//                                picbase64 = sb.toString();
//                                Log.i("NET", "all base64.len " + picbase64.length());
//                                safeCall(new Callable() {
//                                    @Override
//                                    public void call(Object... args) {
//                                        submitData();
//                                    }
//                                });
//                            }
//                        });
//                    } else {
//                        submitData();
//                    }
                } else {
                    showToast("处理结果不能为空!");
                }
                break;
            }
            case R.id.tv_complain_map:{
                Intent intent = new Intent(InstructionAndReplyDetailActivity.this, ComplainMap.class);
                Bundle bundle=new Bundle();
                bundle.putString("picPath",dubanTopersonData.picPath);
                bundle.putDouble("longitude",dubanTopersonData.longitude);
                bundle.putDouble("latitude",dubanTopersonData.latitude);
                bundle.putString("imgLatlist",dubanTopersonData.imgLatlist);
                bundle.putString("imgLnglist",dubanTopersonData.imgLnglist);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            }
            default:
                super.onClick(v);
        }
    }

//    private String picbase64 = "";
    private int opetype = 0;
    private String dealContent = null;

    private void submitData() {
        String request;
        JSONObject params;

        request = "Add_InstructionFromSomeOne_Content";

        if(isSecretary){
//            showToast("isSecretary");
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "instructionFromSecretary", dealContent);
        }else if(isMayor){
//            showToast("isMayor");
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "instructionFromMayor", dealContent);
        }else if(isOtherMayor){
//            showToast("isOtherMayor");
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "instructionFromOtherMayor", dealContent);
        }else if(isCityChief){
//            showToast("isCityChief");
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "instructionFromCityChief", dealContent);
        }else if (isBossChief){
//            showToast("else");
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "instructionFromBossChief", dealContent);
        }else {
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId());
        }

        getRequestContext().add((request), new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {
                    showToast(opetype == 0 ? "暂存成功" : "提交成功");
                    if (opetype == 1) {
                        setResult(RESULT_OK);

                        finish();
                    }
                }
            }
            // picBase64
        }, BaseRes.class, (params));
    }

    private Uri imageFilePath = null;

    private void startAddPhoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "拍照");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        imageFilePath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath);
        startActivityForResult(intent, Tags.CODE_ADDPHOTO);
    }

    private void addPhoto(Uri uri) {
        View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
        BitmapFactory.Options op = new BitmapFactory.Options();
        try {
            Bitmap pic = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, op);
            view.setTag(pic);
            Log.i("NET", "" + pic.getWidth() + "*" + pic.getHeight());
            ((ImageView) view.findViewById(R.id.iv_photo)).setImageBitmap(pic);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        view.setTag(uri);
        ((LinearLayout) findViewById(R.id.ll_chief_photos)).addView(view, ((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount() - 1);
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
        }else{
            loadData();
        }
    }

    String[] imgResultUrls = null;
    private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(InstructionAndReplyDetailActivity.this, PhotoViewActivity.class);
                intent.putExtra("URLS", imgResultUrls);
                intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                startActivity(intent);
            }
        }
    };

    private void initPhotoView(String urls) {
        List<String> list = new ArrayList<String>();
        if (urls != null) {
            for (String url : urls.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0){
            imgUrls = new String[list.size()];
            imgUrls = list.toArray(imgUrls);
            findViewById(R.id.hsv_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgUrls.length; ++i) {
                String url = imgUrls[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(InstructionAndReplyDetailActivity.this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(R.id.tag_first,i);
                view.setTag(R.id.tag_second,imgUrls);
                view.setOnClickListener(clkGotoPhotoView);
                ll_photos.addView(view);
            }

        } else {
            findViewById(R.id.hsv_photos).setVisibility(View.GONE);
        }
    }

    //批示回复动态加入模块的照片显示
    private void initReplyPhotoView(View view,String urls) {
        List<String> list = new ArrayList<String>();
        if (urls != null) {
            for (String url : urls.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0){
            imgUrls = new String[list.size()];
            imgUrls = list.toArray(imgUrls);
            view.findViewById(R.id.hsv_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) view.findViewById(R.id.ll_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgUrls.length; ++i) {
                String url = imgUrls[i];
                View viewPhoto = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(InstructionAndReplyDetailActivity.this, (ImageView) viewPhoto.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                viewPhoto.setTag(R.id.tag_first,i);
                viewPhoto.setTag(R.id.tag_second,imgUrls);
                viewPhoto.setOnClickListener(clkGotoPhotoView);
                ll_photos.addView(viewPhoto);
            }

        } else {
            findViewById(R.id.hsv_photos).setVisibility(View.GONE);
        }
    }
}
