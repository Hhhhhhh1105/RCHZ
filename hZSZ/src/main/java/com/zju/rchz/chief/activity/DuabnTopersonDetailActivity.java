package com.zju.rchz.chief.activity;

import android.content.ContentValues;
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
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.activity.ComplainMap;
import com.zju.rchz.activity.PhotoViewActivity;
import com.zju.rchz.clz.ViewWarp;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.ChiefComp;
import com.zju.rchz.model.ChiefCompFul;
import com.zju.rchz.model.ChiefCompFulRes;
import com.zju.rchz.model.DubanTopersonData;
import com.zju.rchz.model.DubanTopersonDataRes;
import com.zju.rchz.model.DubanTopersonObject;
import com.zju.rchz.net.Callback;
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
 * Created by ZJTLM4600l on 2017/9/17.
 */

public class DuabnTopersonDetailActivity extends BaseActivity {

//    private ChiefComp comp = null;
    private DubanTopersonObject dubanTopersonObject=null;
//    private ChiefCompFul compFul = null;
    private DubanTopersonData dubanTopersonData=null;
    private boolean isComp = true;
    private boolean isHandled = true;
    private boolean hasImg = false;
    private boolean isNpcComp = false;
    private String imgLnglist="";
    private String imgLatlist="";
    private int isSecondFeedback=0;
    private int isSecondCheck=0;
    private boolean isCoordinator=false;
    private boolean ischief=false;
    private TemplateEngine templateEngine = new TemplateEngine() {

        @Override
        public String evalString(Object model, String tmpl) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            String s = super.evalString(model, tmpl);
            if (s != null) {
                if (isComp) {
                    s = s.replace("建议", "上报");
                } else {
                    s = s.replace("投诉", "上报");
                }
            }
            return s;
        }

    };

    private ViewRender viewRender = new ViewRender(templateEngine);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_compdetail);
        isHandled = getIntent().getBooleanExtra(Tags.TAG_HANDLED, false);
        dubanTopersonObject = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_COMP), DubanTopersonObject.class);
        isNpcComp = getIntent().getBooleanExtra(Tags.TAG_ISNPCCOMP, false);
       //判断是否是协管员 6
        isCoordinator = getUser().isLogined() && getUser().isCoordinator();
        ischief = getUser().isLogined() && getUser().isChief();

        //协管员不需要签收
        if(isCoordinator){
            findViewById(R.id.tv_sgin).setVisibility(View.GONE);
        }else if(ischief){
            findViewById(R.id.tv_sgin).setVisibility(View.VISIBLE);
        }

        //不评价
        findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);

        //是否有回复框
        if (isHandled) {
            findViewById(R.id.ll_handle).setVisibility(View.GONE);
            // findViewById(R.id.ll_status).setVisibility(View.GONE);
        } else {
            if (!dubanTopersonObject.isHandled())
                findViewById(R.id.ll_result).setVisibility(View.GONE);
        }

        if (dubanTopersonObject != null) {
//            isComp = comp.isComp();

            initHead(R.drawable.ic_head_back, 0);
            setTitle(!isHandled ? "处理督办" : "处理单");

//            if (comp.isComp()) {
//                // 投诉

                ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "建议", "上报");
//            } else {
                ViewUtils.replaceInView((ViewGroup) findViewById(R.id.ll_root), "投诉", "上报");
//            }

            findViewById(R.id.btn_submit).setOnClickListener(this);
            findViewById(R.id.btn_store).setOnClickListener(this);
            findViewById(R.id.btn_cancel).setOnClickListener(this);
            findViewById(R.id.tv_complain_map).setOnClickListener(this);

            findViewById(R.id.sv_main).setVisibility(View.INVISIBLE);
            viewRender.renderView(findViewById(R.id.ll_root), dubanTopersonObject);
//            initPhotoView(comp.complaintsPicPath);

            findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    startAddPhoto();
                }
            });

            findViewById(R.id.tv_sgin).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    if(((TextView)findViewById(R.id.tv_sgin)).getText().equals("未签收")){
                        getRequestContext().add("SignUp_Action", new Callback<BaseRes>() {
                            @Override
                            public void callback(BaseRes o) {
                                hideOperating();
                                if (o != null && o.isSuccess()) {
                                    showMessage("签收成功!", null);
                                    ((TextView) findViewById(R.id.tv_sgin)).setText("已签收");
                                }
                            }

                        }, BaseRes.class, ParamUtils.freeParam(null,"dubanTopersonSerNum",dubanTopersonData.serNum,"identityNum",3));
                    }
                }
            });
            loadData();
        } else {
            showToast("没有处理单信息");
            finish();
        }
    }

    private String[] imgUrls = null;

    private View.OnClickListener clkGotoPhotoView = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
                intent.putExtra("URLS", imgUrls);
                intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                startActivity(intent);
            }
        }

    };

    private void loadData() {
        showOperating();

        JSONObject params;
        final String request;

            //获取督办单详情
            request = "Get_DubanToperson_Content";
            params = ParamUtils.freeParam(null, "dubanTopersonId" , dubanTopersonObject.getId());

        getRequestContext().add(request, new Callback<DubanTopersonDataRes>() {
            @Override
            public void callback(DubanTopersonDataRes o) {
                hideOperating();

                if (o != null && o.isSuccess()) {
                    dubanTopersonData = o.data;
//                    compFul.isComp = isComp;
//                    compFul.advTheme = comp.advTheme;
//                    compFul.compTheme = comp.compTheme;
                    viewRender.renderView(findViewById(R.id.ll_root), dubanTopersonData);
                    ((TextView) findViewById(R.id.tv_user_name)).setText("***");
                    ((TextView) findViewById(R.id.tv_user_telno)).setText("***********");

                    ((TextView)findViewById(R.id.tv_handlestatus)).setText(dubanTopersonData.getNewMark(TransformNewstate(dubanTopersonData.getNewState())));
                    findViewById(R.id.sv_main).setVisibility(View.VISIBLE);
                    findViewById(R.id.handle_status).setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.tv_datetime)).setText(dubanTopersonData.getSubmitTime() != null ? dubanTopersonData.getSubmitTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
//                    ((TextView)findViewById(R.id.tv_user_telno)).setText(dubanTopersonData.getTeleNum());

                    if(ischief){
                        if(dubanTopersonData.getSignToRiverChief()==1){
                            ((TextView)findViewById(R.id.tv_sgin)).setText("已签收");
                        }
                    }
                    if(dubanTopersonData.getDealResult()!=null&& dubanTopersonData.getDealTime()!=null){
                        //设置标志位isSecondFeedback
                        isSecondFeedback=1;//下一次是第二次反馈
                        ((TextView)findViewById(R.id.tv_hnd_datetime)).setText(dubanTopersonData.getDealTime() != null ? dubanTopersonData.getDealTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_hnd_telno)).setText(dubanTopersonData.getDealTeleNum());
                        ((TextView)findViewById(R.id.tv_hnd_name)).setText(dubanTopersonData.getDealPersonName());
                        ((TextView)findViewById(R.id.tv_hnd_result)).setText(dubanTopersonData.getDealResult());
                    }else {
                        findViewById(R.id.deal_result1).setVisibility(View.GONE);
                    }

                    //督办主题和内容
                    if(dubanTopersonData.getDubanTheme()!=null&&dubanTopersonData.getDubanTheme()!=""){
                        findViewById(R.id.duban).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_dubanTime)).setText(dubanTopersonData.getDubanTime() != null ? dubanTopersonData.getDubanTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_dubanTheam)).setText(dubanTopersonData.getDubanTheme());
                        ((TextView)findViewById(R.id.tv_dubanContent)).setText(dubanTopersonData.getDubanContent());
                        initAnyPhotoViewDuban(dubanTopersonData.getPicPath());
                    }
                    //处理期限
                    if (dubanTopersonData.getDeadLine()!=null&&dubanTopersonData.getDeadLine()!=""){
                        findViewById(R.id.v_deadline).setVisibility(View.VISIBLE);
                        findViewById(R.id.tr_deadline).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_hnd_deadline)).setText(dubanTopersonData.getDeadLine());
                    }else {
                        findViewById(R.id.v_deadline).setVisibility(View.GONE);
                        findViewById(R.id.tr_deadline).setVisibility(View.GONE);
                    }
                    //是否有第二次处理情况
                    if(dubanTopersonData.getSecondResult()!=null&&dubanTopersonData.getSecondResult()!=""){
                        findViewById(R.id.deal_result2).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_dealResult2_time)).setText(dubanTopersonData.getSecondDealTime() != null ? dubanTopersonData.getSecondDealTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_handleresult2)).setText(dubanTopersonData.getSecondResult());
                        ((TextView)findViewById(R.id.tv_secondDealPerson)).setText(dubanTopersonData.getDealPersonName2());
                        initAnyPhotoViewSeconeDeal(dubanTopersonData.getSecondDealPicPath());
                    }else {
                        findViewById(R.id.deal_result2).setVisibility(View.GONE);
                    }
                    //是否有查验结果
                    if(dubanTopersonData.getFirstCheckContent()!=null&&dubanTopersonData.getFirstCheckContent()!=""){
                        //标志位isSecondCheck设置，设为1表示下一次是第二次查验
                        isSecondCheck=1;
                        findViewById(R.id.check_result1).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_firstCheck_time)).setText(dubanTopersonData.getFirstCheckTime() != null ? dubanTopersonData.getFirstCheckTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_checkResult_1)).setText(dubanTopersonData.getFirstCheckContent());
                        initAnyPhotoViewFirstCheck(dubanTopersonData.getFirstCheckPicPath());
                    }else {
                        findViewById(R.id.check_result1).setVisibility(View.GONE);
                    }
                    //是否有第二次查验结果
                    if(dubanTopersonData.getSecondCheckContent()!=null&&dubanTopersonData.getSecondCheckContent()!=""){
                        findViewById(R.id.check_result2).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_secondCheck_time)).setText(dubanTopersonData.getSecondCheckTime() != null ? dubanTopersonData.getSecondCheckTime().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_checkResult_2)).setText(dubanTopersonData.getSecondCheckContent());
                        initAnyPhotoViewSecondCheck(dubanTopersonData.getSecondCheckPicPath());
                    }else {
                        findViewById(R.id.check_result2).setVisibility(View.GONE);
                    }

                    //是否有批示结果
                    if(dubanTopersonData.getInstructionResult()!=null&&dubanTopersonData.getInstructionResult()!=""){
                        findViewById(R.id.ll_intruction).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.tv_instruction_time)).setText(dubanTopersonData.getInstructionDate() != null ? dubanTopersonData.getInstructionDate().getYMDHM(DuabnTopersonDetailActivity.this) : "");
                        ((TextView)findViewById(R.id.tv_instructionPerson)).setText(dubanTopersonData.getInstructionPerson());
                        ((TextView)findViewById(R.id.tv_intructionResult)).setText(dubanTopersonData.getInstructionResult());
                    }else {
                        findViewById(R.id.check_result2).setVisibility(View.GONE);
                    }
                    initPhotoView(dubanTopersonData.picPath);
                    initResultPhotoView(dubanTopersonData.dealPicPath);

                    findViewById(R.id.ll_evalinfo).setVisibility(View.GONE);

                }
            }
        }, DubanTopersonDataRes.class, params);
    }


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
    @Override
    public void onClick(View v) {
        int ope = 0;
        switch (v.getId()) {
            case R.id.btn_cancel: {
                finish();
                break;
            }
            case R.id.btn_submit:
                ope = 1;
            case R.id.btn_store: {
                String s = ((EditText) findViewById(R.id.et_handlecontent)).getText().toString().trim();
                if (s.length() > 0) {
                    opetype = ope;
                    dealContent = s;
                    LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
                    if (ll_photos.getChildCount() <= 1){
                        showToast("请您拍摄相关图片，在回复详情栏上传之后再提交");
                        return;
                    }
                    if (ll_photos.getChildCount() > 1) {
                        // 有图片
                        hasImg = true;
                        final Uri[] bmps = new Uri[ll_photos.getChildCount() - 1];
                        for (int i = 0; i < bmps.length; ++i) {
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
                                    Log.i("NET", bmp.toString() + " bts.len " + bts.length);
                                    sb.append(Base64.encodeToString(bts, Base64.DEFAULT));
                                }
                                picbase64 = sb.toString();
                                Log.i("NET", "all base64.len " + picbase64.length());
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
                } else {
                    showToast("处理结果不能为空!");
                }
                break;
            }
            case R.id.tv_complain_map:{
                Intent intent = new Intent(DuabnTopersonDetailActivity.this, ComplainMap.class);
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

    private String picbase64 = "";
    private int opetype = 0;
    private String dealContent = null;

    private void submitData() {
        showOperating("正在提交数据...");

        String request,request1;
        JSONObject params,params1;

            request = "Add_ChiefDubanTopersonDeal_Content";
            params = ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "dealContent", dealContent, "isSecondFeedback",isSecondFeedback,"picBase64", picbase64);

        request1="Add_CoordinatorDubanTopersonCheck_Content";
        params1=ParamUtils.freeParam(null, "dubanTopersonId", dubanTopersonObject.getId(), "checkContent", dealContent, "isSecondCheck",isSecondCheck, "picBase64", picbase64);
        getRequestContext().add((isCoordinator? request1:request), new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {
                    showToast(opetype == 0 ? "暂存成功" : "提交成功");
                    if (opetype == 1) {
                        setResult(RESULT_OK);

                        imgLatlist="";
                        imgLnglist="";

                        finish();
                    }
                }
            }
            // picBase64
        }, BaseRes.class, (isCoordinator? params1:params));
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
//		if(((LinearLayout) findViewById(R.id.ll_chief_photos)).getChildCount()==1){
//			imgLnglist=imgLnglist+getLongitude();
//			imgLatlist=imgLatlist+getLatitude();
//		}else {
//			imgLnglist=imgLnglist+","+getLongitude();
//			imgLatlist=imgLatlist+","+getLatitude();
//		}
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
        }
    }

    String[] imgResultUrls = null;
    private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
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
        if (list.size() > 0) {
            imgUrls = new String[list.size()];
            imgUrls = list.toArray(imgUrls);
            findViewById(R.id.hsv_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgUrls.length; ++i) {
                String url = imgUrls[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(DuabnTopersonDetailActivity.this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(clkGotoPhotoView);
                ll_photos.addView(view);
            }

        } else {
            findViewById(R.id.hsv_photos).setVisibility(View.GONE);
        }
    }
    private void initResultPhotoView(String urls) {
        List<String> list = new ArrayList<String>();
        if (urls != null) {
            for (String url : urls.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imgResultUrls = new String[list.size()];
            imgResultUrls = list.toArray(imgResultUrls);
            findViewById(R.id.hsv_result_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_result_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgResultUrls.length; ++i) {
                String url = imgResultUrls[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(clkResultGotoPhotoView);
                ll_photos.addView(view);
            }

        } else {
            findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
        }
    }

    String[] imAnyUrls_SecondDeal = null;
    String[] imAnyUrls_FirstCheck = null;
    String[] imAnyUrls_SecondCheck = null;
    String[] imAnyUrls_Duban=null;

    private void initAnyPhotoViewDuban(String picPath){
        List<String> list = new ArrayList<String>();
        if (picPath != null) {
            for (String url : picPath.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imAnyUrls_Duban = new String[list.size()];
            imAnyUrls_Duban = list.toArray(imAnyUrls_Duban);
//            hsv_id.setVisibility(View.VISIBLE);
            findViewById(R.id.hsv_duban_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_duban_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imAnyUrls_Duban.length; ++i) {
                String url = imAnyUrls_Duban[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof Integer) {
                            Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
                            intent.putExtra("URLS",imAnyUrls_Duban);
                            intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                            startActivity(intent);
                        }
                    }
                });
                ll_photos.addView(view);
            }

        } else {
//            ll_photos.setVisibility(View.GONE);
            findViewById(R.id.hsv_duban_photos).setVisibility(View.GONE);
        }
    }
    private void initAnyPhotoViewSeconeDeal(String picPath){
        List<String> list = new ArrayList<String>();
        if (picPath != null) {
            for (String url : picPath.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imAnyUrls_SecondDeal = new String[list.size()];
            imAnyUrls_SecondDeal = list.toArray(imAnyUrls_SecondDeal);
//            hsv_id.setVisibility(View.VISIBLE);
            findViewById(R.id.hsv_result2_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_handleresult2_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imAnyUrls_SecondDeal.length; ++i) {
                String url = imAnyUrls_SecondDeal[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof Integer) {
                            Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
                            intent.putExtra("URLS",imAnyUrls_SecondDeal);
                            intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                            startActivity(intent);
                        }
                    }
                });
                ll_photos.addView(view);
            }
        } else {
//            ll_photos.setVisibility(View.GONE);
            findViewById(R.id.hsv_result2_photos).setVisibility(View.GONE);
        }
    }

    private void initAnyPhotoViewFirstCheck(String picPath){
        List<String> list = new ArrayList<String>();
        if (picPath != null) {
            for (String url : picPath.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imAnyUrls_FirstCheck = new String[list.size()];
            imAnyUrls_FirstCheck = list.toArray(imAnyUrls_FirstCheck);
//            hsv_id.setVisibility(View.VISIBLE);
            findViewById(R.id.hsv_firstcheck_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_checkResult1_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imAnyUrls_FirstCheck.length; ++i) {
                String url = imAnyUrls_FirstCheck[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof Integer) {
                            Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
                            intent.putExtra("URLS",imAnyUrls_FirstCheck);
                            intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                            startActivity(intent);
                        }
                    }
                });
                ll_photos.addView(view);
            }

        } else {
//            ll_photos.setVisibility(View.GONE);
            findViewById(R.id.hsv_firstcheck_photos).setVisibility(View.GONE);
        }
    }

    private void initAnyPhotoViewSecondCheck(String picPath){
        List<String> list = new ArrayList<String>();
        if (picPath != null) {
            for (String url : picPath.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            imAnyUrls_SecondCheck = new String[list.size()];
            imAnyUrls_SecondCheck = list.toArray(imAnyUrls_SecondCheck);
//            hsv_id.setVisibility(View.VISIBLE);
            findViewById(R.id.hsv_seconecheck_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_checkResult2_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imAnyUrls_SecondCheck.length; ++i) {
                String url = imAnyUrls_SecondCheck[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (v.getTag() instanceof Integer) {
                            Intent intent = new Intent(DuabnTopersonDetailActivity.this, PhotoViewActivity.class);
                            intent.putExtra("URLS",imAnyUrls_SecondCheck);
                            intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                            startActivity(intent);
                        }
                    }
                });
                ll_photos.addView(view);
            }

        } else {
            findViewById(R.id.hsv_seconecheck_photos).setVisibility(View.GONE);
        }
    }
}
