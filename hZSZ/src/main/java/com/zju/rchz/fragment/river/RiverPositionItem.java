package com.zju.rchz.fragment.river;

import android.view.View;
import android.widget.TextView;

import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.model.River;

public class RiverPositionItem extends BaseRiverPagerItem {
	public RiverPositionItem(River river, BaseActivity context) {
		super(river, context);
	}

	protected View view;

	@Override
	public View getView() {
		if (view == null) {
			view = new TextView(context);
			((TextView) view).setText("河道方位");
		}
		return view;
	}
}
