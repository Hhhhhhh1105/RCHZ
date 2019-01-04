package com.zju.rchz.model;

/**
 * Created by Wangli on 2017/2/19.
 */

public class OutletListDataRes extends BaseRes{
    public OutletListData data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.industrialPortJsons != null;
    }
}
