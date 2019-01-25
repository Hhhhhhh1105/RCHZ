package com.zju.rchz.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.base.Callable;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.DipPxUtils;
import com.zju.rchz.utils.ParamUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

//河长回复领导批示页面
public class ReplyInstructionActivity extends BaseActivity {

    private int flag;//回复的是哪个领导的批示 1市委书记 2市长 3分管市长 4镇街总河长 0市级河长
    private String instructionContent = "";//批示内容
    private String instructionDate = "";//批示时间
    private String dubanId;//duabntoperson的ID

    //提交信息
    private String replyContent = null;
    private String picbase64 = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_instruction);

        setTitle("回复批示");
        initHead(R.drawable.ic_head_back,0);

        flag = getIntent().getIntExtra("flag",0);
        instructionContent = getIntent().getStringExtra("instructionContent");
        instructionDate = getIntent().getStringExtra("instructionDate");
        dubanId = getIntent().getStringExtra("dubanId");

        findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.btn_submit).setOnClickListener(this);
        findViewById(R.id.ib_chief_photo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startAddPhoto();
            }
        });
        LoadDate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_submit:{
                String s = ((EditText) findViewById(R.id.et_handlecontent)).getText().toString().trim();
                if (s.length() > 0) {
                    replyContent = s;
                    LinearLayout ll_photos = (LinearLayout) findViewById(R.id.ll_chief_photos);
                    if (ll_photos.getChildCount() <= 1){
                        showToast("请您拍摄相关图片，在回复详情栏上传之后再提交");
                        return;
                    }
                    if (ll_photos.getChildCount() > 1) {
                        // 有图片
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
                    showToast("回复不能为空!");
                }
                break;
            }
            case R.id.btn_cancel:
                finish();
                break;
            default:
                super.onClick(v);
        }

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

    private Uri imageFilePath = null;
    private void startAddPhoto(){
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

    private void submitData(){
        showOperating("正在提交数据...");

        String request;
        JSONObject params;

        request = "Add_ReplyForInstruction";
        params = ParamUtils.freeParam(null, "dubanId", dubanId, "replyContent", replyContent,
                "picBase64",picbase64,"flag",flag);

        getRequestContext().add((request), new Callback<BaseRes>() {
            @Override
            public void callback(BaseRes o) {
                hideOperating();
                if (o != null && o.isSuccess()) {
                    showToast( "提交成功");

                    setResult(RESULT_OK);
                    finish();
                }
            }
            // picBase64
        }, BaseRes.class, params);
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

    private void LoadDate(){
        //回复书记批示
        final LinearLayout ll_instruction = (LinearLayout) findViewById(R.id.ll_instruction);
        ll_instruction.removeAllViews();
        ll_instruction.addView(initContItem(instructionContent,instructionDate,flag));
    }
    /**
     * 批示详情(一项)
     * @param  instructionContent;//内容
     * @param  flag;//回复的是哪个领导的批示 1市委书记 2市长 3分管市长 4镇街总河长 0市级河长
     * @param  instructionDate;//回复批示的日期
     * @return
     *
     */
    private View initContItem(String instructionContent, String instructionDate, int flag) {
        View view = null;
        switch (flag){
            case 0:
                view = LinearLayout.inflate(this, R.layout.inc_citychief_instruction, null);
                ((TextView) view.findViewById(R.id.tv_cityChiefIntruction)).setText(instructionContent);
                ((TextView) view.findViewById(R.id.tv_cityChiefIntruction_time)).setText(instructionDate);
                break;
            case 1:
                view = LinearLayout.inflate(this, R.layout.inc_party_secretary_intruction, null);
                ((TextView) view.findViewById(R.id.tv_partySecretaryIntruction)).setText(instructionContent);
                ((TextView) view.findViewById(R.id.tv_partySecretaryIntruction_time)).setText(instructionDate);
                break;
            case 2:
                view = LinearLayout.inflate(this, R.layout.inc_mayor_intruction, null);
                ((TextView) view.findViewById(R.id.tv_mayorIntruction)).setText(instructionContent);
                ((TextView) view.findViewById(R.id.tv_mayorIntruction_time)).setText(instructionDate);
                break;
            case 3:
                view = LinearLayout.inflate(this, R.layout.inc_other_mayor_intruction, null);
                ((TextView) view.findViewById(R.id.tv_otherMayorIntruction)).setText(instructionContent);
                ((TextView) view.findViewById(R.id.tv_otherMayorIntruction_time)).setText(instructionDate);
                break;
            case 4:
                view = LinearLayout.inflate(this, R.layout.inc_boss_chief_instruction, null);
                ((TextView) view.findViewById(R.id.tv_bossChiefIntruction)).setText(instructionContent);
                ((TextView) view.findViewById(R.id.tv_bossChiefIntruction_time)).setText(instructionDate);
                break;
        }

//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
//        int dp1px = DipPxUtils.dip2px(getBaseContext(), getBaseContext().getResources().getDimension(R.dimen.linew));
//        lp.setMargins(dp1px, dp1px, 0, 0);
//        view.setLayoutParams(lp);
        return view;
    }
}
