package com.zju.rchz.fragment.river;

import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.utils.AssetsUtils;
import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.model.River;
import com.zju.rchz.model.RiverdecisionRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.net.Constants;
import com.zju.rchz.utils.ParamUtils;

public class RiverPolicyItem extends BaseRiverPagerItem {
	public RiverPolicyItem(River river, BaseActivity context) {
		super(river, context);
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
		context.getRequestContext().add("riverdecision_data_get", new Callback<RiverdecisionRes>() {
			@Override
			public void callback(RiverdecisionRes o) {
				if (o != null && o.isSuccess()) {
					String html = AssetsUtils.readAssetTxt(context, "riverdecision_tpl.html");
					String body = o.data.riverStrategyAbstract != null && o.data.riverStrategyAbstract.length() > 0 ? o.data.riverStrategyAbstract : "暂无内容，待上传。";
					html = html.replace("{{title}}", o.data.riverName).replace("{{body}}", body);
					wv_main.getSettings().setDefaultTextEncodingName("UTF-8");
					wv_main.loadDataWithBaseURL(Constants.SerUrl, html, "text/html", "UTF-8", null);
				}
				setRefreshing(false);
			}
		}, RiverdecisionRes.class, ParamUtils.freeParam(null, "riverId", river.riverId));
	}
}
