package com.zju.rchz.fragment.river;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.model.River;
import com.zju.rchz.utils.ViewUtils;

public abstract class BaseRiverPagerItem extends PagerItem {
	protected River river;
	protected BaseActivity context;
	protected View view;

	public BaseRiverPagerItem(River river, BaseActivity context) {
		super();
		this.river = river;
		this.context = context;
	}

	public void loadData() {

	}
	
	public void readyView(){
		
	}

	protected void initedView() {
		if (view != null) {
			if (view.findViewById(R.id.srl_main) instanceof SwipeRefreshLayout) {
				SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_main);
				ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
				swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

					@Override
					public void onRefresh() {
						loadData();
					}
				});
			}
		}
	}

	protected void setRefreshing(boolean b) {
		if (view != null) {
			if (view.findViewById(R.id.srl_main) instanceof SwipeRefreshLayout) {
				SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_main);
				swipeRefreshLayout.setRefreshing(b);
			}
		}
	}
}
