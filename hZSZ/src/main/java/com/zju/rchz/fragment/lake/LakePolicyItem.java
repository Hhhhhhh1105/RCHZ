package com.zju.rchz.fragment.lake;

import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.utils.AssetsUtils;
import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.model.Lake;
import com.zju.rchz.model.LakedecisionRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.net.Constants;
import com.zju.rchz.utils.ParamUtils;

/**
 * Created by ZJTLM4600l on 2018/6/12.
 */

public class LakePolicyItem extends BaseLakePagerItem {
    public LakePolicyItem(Lake lake, BaseActivity context) {
        super(lake, context);
    }

    protected WebView wv_main = null;

    @Override
    public View getView() {
        if (view == null) {
            view = LinearLayout.inflate(context, R.layout.confs_river_policy, null);
            wv_main = (WebView) view.findViewById(R.id.wv_main);
            initedView();
            loadData();
        }
        return view;
    }

    @Override
    public void loadData() {
        setRefreshing(true);
        context.getRequestContext().add("Get_LakeDecision_Data", new Callback<LakedecisionRes>() {
            @Override
            public void callback(LakedecisionRes o) {
                if (o != null && o.isSuccess()) {
                    String html = AssetsUtils.readAssetTxt(context, "riverdecision_tpl.html");
                    String body = o.data.lakeStrategyAbstract != null && o.data.lakeStrategyAbstract.length() > 0 ? o.data.lakeStrategyAbstract : "暂无内容，待上传。";
                    html = html.replace("{{title}}", o.data.lakeName).replace("{{body}}", body);
                    wv_main.getSettings().setDefaultTextEncodingName("UTF-8");
                    wv_main.loadDataWithBaseURL(Constants.SerUrl, html, "text/html", "UTF-8", null);
                }
                setRefreshing(false);
            }
        }, LakedecisionRes.class, ParamUtils.freeParam(null, "lakeId", lake.lakeId));
    }
}
