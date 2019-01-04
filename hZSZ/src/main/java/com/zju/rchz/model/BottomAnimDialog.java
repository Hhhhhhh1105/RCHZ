package com.zju.rchz.model;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zju.rchz.R;
import com.zju.rchz.utils.DipPxUtils;


/**
 * Created by ZJTLM4600l on 2018/7/6.
 */

public class BottomAnimDialog extends Dialog {
    private final Context mContext;

    private final String mItem1Name;
    private final String mItem2Name;
    private final String mItem3Name;


    private BottonAnimDialogListener mListener;
    private TextView mTvItem1;
    private TextView mTvItem2;
    private TextView mTvItem3;

    public BottomAnimDialog(Context context, String item1Name, String item2Name, String item3Name) {

        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;

        this.mItem1Name = item1Name;
        this.mItem2Name = item2Name;
        this.mItem3Name = item3Name;

        initView();

    }


    private void initView() {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.bottom_anim_dialog_layout, null);

        Window window = this.getWindow();
        if (window != null) { //设置dialog的布局样式 让其位于底部
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.width = ViewGroup.LayoutParams.MATCH_PARENT;
                //设置dialog 在布局中的位置
                attr.gravity = Gravity.BOTTOM;

                window.setAttributes(attr);
            }

        }

        mTvItem1 = (TextView) view.findViewById(R.id.tv_item1);
        mTvItem2 = (TextView) view.findViewById(R.id.tv_item2);
        mTvItem3 = (TextView) view.findViewById(R.id.tv_item3);

        mTvItem1.setOnClickListener(new clickListener());
        mTvItem2.setOnClickListener(new clickListener());
        mTvItem3.setOnClickListener(new clickListener());

        setContentView(view);

        setData();
    }


    private void setData() {
        mTvItem1.setText(mItem1Name);
        mTvItem2.setText(mItem2Name);
        mTvItem3.setText(mItem3Name);
    }

    public void setItem1TextColor(int colorId) {//设置item的字体颜色
        if (mTvItem1 != null) {
            mTvItem1.setTextColor(colorId);
        }
    }

    public void setItem2TextColor(int colorId) {
        if (mTvItem2 != null) {
            mTvItem2.setTextColor(colorId);
        }
    }

    public void setItem3TextColor(int colorId) {
        if (mTvItem3 != null) {
            mTvItem3.setTextColor(colorId);
        }
    }

    public void setClickListener(BottonAnimDialogListener listener) {
        this.mListener = listener;
    }

    public interface BottonAnimDialogListener {
        void onItem1Listener();

        void onItem2Listener();

        void onItem3Listener();
    }


    private class clickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_item1:
                    if (mListener != null) {
                        mListener.onItem1Listener();
                    }
                    break;
                case R.id.tv_item2:
                    if (mListener != null) {
                        mListener.onItem2Listener();
                    }
                    break;
                case R.id.tv_item3:
                    if (mListener != null) {
                        mListener.onItem3Listener();
                    }
                    break;
            }
        }
    }
}
