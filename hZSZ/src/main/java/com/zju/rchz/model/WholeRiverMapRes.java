package com.zju.rchz.model;

/**
 * Created by ZJTLM4600l on 2018/6/26.
 */

public class WholeRiverMapRes extends BaseRes{
    public WholeRiverMap data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null;
    }
}
