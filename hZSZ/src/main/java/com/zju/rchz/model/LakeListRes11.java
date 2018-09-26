package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2018/6/12.
 */

public class LakeListRes11 extends BaseRes {
    public Lake[] data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null;
    }
}
