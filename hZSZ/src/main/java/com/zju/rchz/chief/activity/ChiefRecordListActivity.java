package com.zju.rchz.chief.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.sin.android.sinlibs.tagtemplate.ViewRender;
import com.zju.rchz.R;
import com.zju.rchz.Tags;
import com.zju.rchz.Values;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.DateTime;
import com.zju.rchz.model.RiverRecord;
import com.zju.rchz.model.RiverRecordListRes;
import com.zju.rchz.model.RiverRecordTemporaryJsonRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.net.Constants;
import com.zju.rchz.utils.DipPxUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;
import com.zju.rchz.view.ListViewWarp;
import com.zju.rchz.view.ListViewWarp.WarpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChiefRecordListActivity extends BaseActivity implements WarpHandler {

	private ListViewWarp listViewWarp = null;
	private SimpleListAdapter adapter = null;
	private List<RiverRecord> records = new ArrayList<RiverRecord>();

	private ViewRender viewRender = new ViewRender();

	private int timesOfRiverRecord;//今日巡河次数
	private DateTime todayDateTime;//今天的日期
	//测试
	private RiverRecord riverRecordTest = null;

	// 编辑巡河单时的监听器
	protected OnClickListener edtClk = new OnClickListener() {

		@Override
		public void onClick(View btn) {
			RiverRecord record = (RiverRecord) btn.getTag();
			if (record != null) {
				Intent intent = new Intent(ChiefRecordListActivity.this, com.zju.rchz.chief.activity.ChiefEditRecordActivity.class);
				intent.putExtra(Tags.TAG_RECORD, StrUtils.Obj2Str(record));
				startActivityForResult(intent, Tags.CODE_EDIT);
			}
		}
	};

	//删除巡河单，没有用到
	protected OnClickListener delClk = new OnClickListener() {

		@Override
		public void onClick(View btn) {
			final RiverRecord record = (RiverRecord) btn.getTag();
			if (record != null) {
				Dialog d = createMessageDialog("提示", "确定删除该记录吗?", "删除", "取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						showOperating();
						getRequestContext().add("Delete_RiverRecord", new Callback<BaseRes>() {
							@Override
							public void callback(BaseRes o) {
								hideOperating();
								if (o != null && o.isSuccess()) {
									showToast("删除成功!");
									records.remove(record);
									adapter.notifyDataSetChanged();
								}
							}
						}, BaseRes.class, ParamUtils.freeParam(null, "recordId", record.recordId));
					}
				}, null, null);
				d.show();
			}
		}
	};

	private SimpleViewInitor recordInitor = new SimpleViewInitor() {

		@Override
		public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
			if (convertView == null) {
				convertView = LinearLayout.inflate(context, R.layout.item_record, null);
			}

			RiverRecord record = (RiverRecord) data;
			viewRender.renderView(convertView, record);
			convertView.setTag(record);
			convertView.findViewById(R.id.btn_edit).setTag(record);
			convertView.findViewById(R.id.btn_delete).setTag(record);

			// convertView.findViewById(R.id.btn_edit).setOnClickListener(edtClk);
			convertView.setOnClickListener(edtClk);
			convertView.findViewById(R.id.btn_delete).setOnClickListener(delClk);

			if(record.isCorrect.equals("1")){
				((TextView)convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setText("有效");
				((TextView) convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setTextColor(android.graphics.Color.GREEN);
			}else if(record.isCorrect.equals("0")){
				((TextView)convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setText("无效");
				((TextView) convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setTextColor(android.graphics.Color.RED);
			}else {
				((TextView)convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setText("判断中");
				((TextView) convertView.findViewById(R.id.tv_riverrecord_iscorrect)).setTextColor(Color.BLACK);
			}
//			if(record.recordDate.year == todayDateTime.year){
//				if(record.recordDate.month == todayDateTime.month && record.recordDate.day == todayDateTime.day){
//					convertView.findViewById(R.id.ll_deal_edit).setVisibility(View.VISIBLE);
//				}
//			}

			return convertView;
		}
	};

	public String year = "2015";
	public String month = "7";

	//记录当前的年月日
	String yearToday="2015";
	String monthToday="10";
	String dayToday="10";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chief_recordlist);
		setTitle("巡查记录");
		initHead(R.drawable.ic_head_back, 0);

		todayDateTime = DateTime.getNow();//初始化

		Locale.setDefault(Locale.CHINA);
		Calendar calendar = Calendar.getInstance();
		year = "" + calendar.get(Calendar.YEAR);
		month = "" + (1 + calendar.get(Calendar.MONTH));
		//初始化当前的年月日
		yearToday=calendar.get(Calendar.YEAR)+"";
		monthToday=calendar.get(Calendar.MONTH)+1+"";
		dayToday=calendar.get(Calendar.DAY_OF_MONTH)+"";

		findViewById(R.id.btn_new).setOnClickListener(this);
		findViewById(R.id.tv_seldate).setOnClickListener(this);

		adapter = new SimpleListAdapter(this, records, recordInitor);

		listViewWarp = new ListViewWarp(this, adapter, this);
		listViewWarp.getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.bg_gray)));
		listViewWarp.getListView().setDividerHeight(DipPxUtils.dip2px(this, getResources().getDimension(R.dimen.padding_medium)));

		//请求参数
		submitUuidParam = new JSONObject();
		try{
			submitUuidParam.put("UUID",getUser().getUuid());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());
		// listViewWarp.getRootView().setBackgroundColor(getResources().getColor(R.color.white));

		submitSetRiverRecordIsCorrectParam = new JSONObject();
		try{
			submitSetRiverRecordIsCorrectParam.put("UUID",getUser().getUuid());
			submitSetRiverRecordIsCorrectParam.put("year",yearToday);
			submitSetRiverRecordIsCorrectParam.put("month",monthToday);
			submitSetRiverRecordIsCorrectParam.put("day",dayToday);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		loadRecords(true);
		getHostRiverRecordTemporary();//获得服务器对应河长未完成的轨迹

		refreshDateView();
	}

	private void refreshDateView() {
		((TextView) findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
	}

	private YearMonthSelectDialog selectDialog = null;
	private String latList_host;
	private String lngList_host;
	JSONObject submitUuidParam = null;

	//请求，判断服务器是否对应河长有未完成的轨迹
	private void getHostRiverRecordTemporary(){
		getRequestContext().add("Get_RiverRecordTemporary", new Callback<RiverRecordTemporaryJsonRes>() {
			@Override
			public void callback(RiverRecordTemporaryJsonRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {
					if(o.data!=null){
						//获得经纬度信息
						latList_host = o.data.getLatlist();
						lngList_host = o.data.getLnglist();
					}else{
						latList_host = "";
						lngList_host = "";
					}
				}
			}
		}, RiverRecordTemporaryJsonRes.class, submitUuidParam);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_new: {
//			if(timesOfRiverRecord >= Values.timesOfRiverTour){
//				showToast("今日巡河次数已达上限！");
//			}else {
			AlertDialog.Builder ab = new AlertDialog.Builder(ChiefRecordListActivity.this);
			ab.setTitle("温馨提示");
			ab.setMessage("请您在巡河履职过程中保持APP在前台显示，中途跳转到其他应用（如微信等）可能导致轨迹考核无效！感谢您对工作的支持！");
			ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {

					Intent intent = new Intent(getCurActivity(), com.zju.rchz.chief.activity.ChiefEditRecordActivity.class);
					Bundle bundle=new Bundle();
					bundle.putString("latList_host", latList_host);
					bundle.putString("lngList_host", lngList_host);
					intent.putExtras(bundle);
					startActivityForResult(intent, Tags.CODE_NEW);

					arg0.dismiss();
				}
			});

			ab.setCancelable(false);
			ab.create().show();
//			}
			break;
		}
		case R.id.tv_seldate: {
			if (selectDialog == null) {
				selectDialog = new YearMonthSelectDialog(this, new YearMonthSelectDialog.Callback() {
					@Override
					public void onYMSelected(int year, int month) {
						ChiefRecordListActivity.this.year = "" + year;
						ChiefRecordListActivity.this.month = "" + month;
						refreshDateView();
						loadRecords(true);
					}
				});
			}
			selectDialog.show();
			break;
		}
		default:
			super.onClick(v);
			break;
		}
	}

	@Override
	public boolean onRefresh() {
		loadRecords(true);
		getHostRiverRecordTemporary();//获得服务器对应河长未完成的轨迹
		return true;
	}

	@Override
	public boolean onLoadMore() {
		loadRecords(false);
		return true;
	}

	JSONObject submitSetRiverRecordIsCorrectParam = null;

	private void loadRecords(final boolean refresh) {
		if (refresh)
			listViewWarp.setRefreshing(true);
		else
			listViewWarp.setLoadingMore(true);

		//判断轨迹有效性的请求
		getRequestContext().add("Set_RiverRecord_IsCorrect", new Callback<BaseRes>() {
			@Override
			public void callback(BaseRes o) {
				hideOperating();
				if (o != null && o.isSuccess()) {

				}
			}
		}, BaseRes.class, submitSetRiverRecordIsCorrectParam);

		getRequestContext().add("Get_Record_List", new Callback<RiverRecordListRes>() {
			@Override
			public void callback(RiverRecordListRes o) {
				listViewWarp.setRefreshing(false);
				listViewWarp.setLoadingMore(false);

				timesOfRiverRecord = 0; //今日巡河次数初始化
				if (o != null && o.isSuccess()) {
					if (refresh)
						records.clear();
					for (RiverRecord rr : o.data) {
						if(rr.recordDate.year == todayDateTime.year){
							if (rr.recordDate.month == todayDateTime.month && rr.recordDate.day == todayDateTime.day){
								timesOfRiverRecord +=1;
							}
						}
						riverRecordTest = rr;
						records.add(rr);
					}
					adapter.notifyDataSetChanged();

					listViewWarp.setNoMore(true);
				}
			}
		}, RiverRecordListRes.class, getPageParam(refresh));

	}

	private final int DefaultPageSize = Constants.DefaultPageSize;

	protected JSONObject getPageParam(boolean refresh) {
		JSONObject j = refresh ? ParamUtils.pageParam(DefaultPageSize, 1) : ParamUtils.pageParam(DefaultPageSize, (records.size() + DefaultPageSize - 1) / DefaultPageSize + 1);
		try {
			j.put("year", year);
			j.put("month", month);
			j.put("authority", getUser().getAuthority());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		//刷新判断今日的有效性
		loadRecords(true);
		getHostRiverRecordTemporary();//获得服务器对应河长未完成的轨迹

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			loadRecords(true);
			getHostRiverRecordTemporary();//获得服务器对应河长未完成的轨迹
	}
	}
}
