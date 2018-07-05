package com.zju.rchz.fragment.lake;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.zju.rchz.R;
import com.zju.rchz.activity.BaseActivity;
import com.zju.rchz.adapter.PagerItem;
import com.zju.rchz.model.Lake;
import com.zju.rchz.utils.ViewUtils;

/**
 * Created by ZJTLM4600l on 2018/6/12.
 */

public class BaseLakePagerItem extends PagerItem {
    protected Lake lake;
    protected BaseActivity context;
    protected View view;

    public BaseLakePagerItem(Lake lake, BaseActivity context) {
        super();
        this.lake = lake;
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

    @Override
    public View getView() {
        return null;
    }
}
