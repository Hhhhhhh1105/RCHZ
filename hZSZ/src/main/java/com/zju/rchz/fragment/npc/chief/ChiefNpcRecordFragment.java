package com.zju.rchz.fragment.npc.chief;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.chief.activity.ChiefEditRecordActivity;
import com.zju.rchz.chief.activity.YearMonthSelectDialog;
import com.zju.rchz.fragment.BaseFragment;
import com.zju.rchz.model.RiverRecord;
import com.zju.rchz.model.RiverRecordListRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;
import com.zju.rchz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 河长端 - 查看代表巡河界面
 * Created by Wangli on 2017/6/8.
 */

public class ChiefNpcRecordFragment extends BaseFragment {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Object> list = new ArrayList<Object>();

    public String year = "2017";
    public String month = "6";
    private YearMonthSelectDialog selectDialog = null;
    JSONObject params = null;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        year = "" + Calendar.getInstance().get(Calendar.YEAR);
        month = "" + (Calendar.getInstance().get(Calendar.MONTH) + 1);

        adapter = new SimpleListAdapter(getBaseActivity(), list, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                convertView = LinearLayout.inflate(context, com.zju.rchz.R.layout.item_npc_record, null);

                final RiverRecord record = (RiverRecord) data;
                getBaseActivity().getViewRender().renderView(convertView, record);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getBaseActivity(), ChiefEditRecordActivity.class);
                        intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
                        intent.putExtra(Tags.TAG_ABOOLEAN, true);
                        getBaseActivity().startActivity(intent);
                    }
                });

                return convertView;
            }
        });

        listViewWarp = new ListViewWarp(getBaseActivity(), adapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                return loadData(true);
            }

            @Override
            public boolean onLoadMore() {
                return false;
            }
        });

        listViewWarp.startRefresh();

        if (this.rootView == null) {
            View view = LinearLayout.inflate(getBaseActivity(), com.zju.rchz.R.layout.inc_seldate, null);
            listViewWarp.getListView().addHeaderView(view);
            rootView = listViewWarp.getRootView();
        }

        rootView.findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if (selectDialog == null) {
                     selectDialog = new YearMonthSelectDialog(getBaseActivity(), new YearMonthSelectDialog.Callback() {
                         @Override
                         public void onYMSelected(int y, int m) {
                             year = "" + y;
                             month = "" + m;
                             refreshDateView();
                             loadData(true);
                         }
                     });
                 }
                selectDialog.show();
            }
        });

        refreshDateView();

        return rootView;
    }

    private void refreshDateView() {
        ((TextView) rootView.findViewById(com.zju.rchz.R.id.tv_seldate)).setText(year + "年" + month + "月");
    }

    private boolean loadData(final boolean refresh) {
        if (refresh) {
            listViewWarp.setRefreshing(true);
            list.clear();
            adapter.notifyDataSetInvalidated();
        }

        params = ParamUtils.freeParam(null, "month", month, "year", year);

        getBaseActivity().getRequestContext().add("Get_ChiefDeputyRecord_List", new Callback<RiverRecordListRes>() {
            @Override
            public void callback(RiverRecordListRes o) {
                listViewWarp.setRefreshing(false);
                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        list.clear();
                    for (RiverRecord riverRecord : o.data ) {
                        list.add(riverRecord);
                    }

                    adapter.notifyDataSetInvalidated();
                }

                if (list.size() == 0) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, RiverRecordListRes.class, params);

        return true;
    }
}
