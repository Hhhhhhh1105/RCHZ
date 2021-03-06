package com.zju.rchz.chief.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.zju.rchz.R;
import com.zju.rchz.Values;
import com.zju.rchz.model.BaseRes;
import com.zju.rchz.model.WholeRiverMap;
import com.zju.rchz.model.WholeRiverMapRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.PatrolRecordUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.baidu.mapapi.utils.DistanceUtil.getDistance;


/**
 * Created by Wangli on 2016/11/28.
 */
public class ChiefInspectActivity extends BaseActivity{

    MapView mMapView;
    BaiduMap mBaiduMap;

    private LatLng riverStart;
    private LatLng riverEnd;
    private String riverAllLatitude;//河道全部点
    private String riverAllLongitude;
    private String[] riverAllLatitudeArray;
    private String[] riverAllLongitudeArray;
    WholeRiverMap wholeRiverMap;
    ArrayList<LatLng> riverAllPoint;

    Button btn_stop;

    private LocationClient mLocationClient;
    BitmapDescriptor track_start = null;
    BitmapDescriptor track_end = null;
    //    boolean isFirstLoc = true;
    int isFirstLoc = 0;
    List<LatLng> points = new ArrayList<LatLng>();//全部点
    List<LatLng> points_tem = new ArrayList<LatLng>();//临时点
    LatLng point;//记录的最后一个轨迹点
    List<LatLng> threePointsToOnePoint = new ArrayList<LatLng>();//读取三个点，选取历史记录点距离居中的点
    //测试巡河轨迹点数
    int countOfHandler = 0;

    //    List<LatLng> points_to_server = new ArrayList<LatLng>();//提交至服务器后台的点
    String lngList;//上传至服务器的经度列表，为上传方便转化为字符串
    String latList;//上传至服务器的纬度列表，为上传方便转化为字符串
    private String[] lat_array;
    private String[] lng_array;
    int countForPoint = 0;

    //暂时的经纬度数据
    private String latlist_temp = null;
    private String lnglist_temp = null;
    private String[] lat_temp_array;
    private String[] lng_temp_array;
    //图片经纬度
    private String imgLnglist=null;
    private String imgLatlist=null;
    private String[] imgLnglist_array;
    private String[] imgLatlist_array;

    ArrayList<LatLng> pointsToDrawFirst;
    private boolean hasHistroyData = false;

    OverlayOptions options;

    Handler handler = new Handler();

    boolean isStopLocClient = false;

    //测试巡河轨迹点数
    private MyRunable myRunableInspect = new MyRunable();

    //确定是新加巡河单还是编辑巡河单（现在已经无法编辑）
    private boolean isAddNewRiverRecord = false;

    //传入河道编号：
    private int riverId;
    private int passTime;//已巡河时长
    private TextView passTimeTextView = null;

    //计数，如果长时间没有新的点加入要删除的轨迹，认为之前的取点有误
    private int countNoJoin = 0;

    private Button myPosition;
    private Button riverPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(this.getApplication());

        setContentView(R.layout.activity_chief_inspect);
        setTitle("巡河");
        initHead(R.drawable.ic_head_back,0);

        handler.postDelayed(myRunableInspect,3000);
//        SDKInitializer.initialize(this);

        mMapView = (MapView) findViewById(R.id.mv_position);
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);
        passTimeTextView = (TextView)findViewById(R.id.tv_passTime);

        btn_stop = (Button) findViewById(R.id.btn_stop);

        latlist_temp = getIntent().getExtras().getString("latlist_temp");
        lnglist_temp = getIntent().getExtras().getString("lnglist_temp");
        isAddNewRiverRecord = getIntent().getExtras().getBoolean("isAddNewRiverRecord");
        riverId = getIntent().getExtras().getInt("riverId",0);
        passTime = getIntent().getExtras().getInt("passTime",0);

        //获取河道应巡点
        JSONObject params;
        final String request;

        if(getUser().isVillageChief()){//村级河长取河道应巡点
            request = "Get_VillChiefRiverMap";
            params = ParamUtils.freeParam(null, "phoneNumber" , getUser().userName);
        }else{//其他河长取河道应巡点
            request = "Get_WholeRiverMap";
            params = ParamUtils.freeParam(null, "riverId", riverId);
        }
        if(riverId!=0 ||getUser().isVillageChief()){
            getRequestContext().add(request, new Callback<WholeRiverMapRes>() {
                @Override
                public void callback(WholeRiverMapRes o) {
                    hideOperating();
                    if (o != null && o.isSuccess()){
                        if (o.data !=null && o.data.allLongitude!=null){
                            wholeRiverMap = o.data;
                            riverAllLatitude = wholeRiverMap.allLatitude;
                            riverAllLongitude = wholeRiverMap.allLongitude;
                        }
                        if(riverAllLatitude !=null && !riverAllLatitude.equals("")){
                            //得到河道的所有坐标
                            if (riverAllLatitude!=null && riverAllLatitude.contains(",")){
                                //如果不止一个数据，变成一个数组
                                riverAllLatitudeArray = riverAllLatitude.split(",");
                                riverAllLongitudeArray = riverAllLongitude.split(",");
                            }else{
                                //如果只有一个数据
                                riverAllLatitudeArray = new String[1];
                                riverAllLongitudeArray = new String[1];
                                riverAllLatitudeArray[0] = riverAllLatitude;
                                riverAllLongitudeArray[0] = riverAllLongitude;
                            }
                            //起点和终点
                            if(riverAllLongitudeArray!=null && riverAllLongitudeArray.length>0){
                                riverStart =new LatLng(Double.parseDouble(riverAllLatitudeArray[0]), Double.parseDouble(riverAllLongitudeArray[0]));
                                riverEnd =new LatLng(Double.parseDouble(riverAllLatitudeArray[riverAllLatitudeArray.length-1]),
                                        Double.parseDouble(riverAllLongitudeArray[riverAllLatitudeArray.length-1]));
                            }
                            riverAllPoint = new ArrayList<LatLng>();

                            for (int i = 0; i < riverAllLatitudeArray.length; i++){
                                riverAllPoint.add(new LatLng(Double.parseDouble(riverAllLatitudeArray[i]), Double.parseDouble(riverAllLongitudeArray[i])));
                            }
                        }
                    }
                }
            }, WholeRiverMapRes.class, params);
        }


        submitTemporaryParam = new JSONObject();
        try{
            submitTemporaryParam.put("UUID",getUser().getUuid());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        track_start = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        track_end = BitmapDescriptorFactory.fromResource(R.drawable.track_end);

        if (latlist_temp != null && !latlist_temp.equals("")){
            hasHistroyData = true;
//            showToast(hasHistroyData+"");
            Log.i("recordinspect", latlist_temp);

            latList = "" + latlist_temp;
            lngList = "" + lnglist_temp;

            if (latlist_temp.contains(",")){
                //如果不止一个数据，变成一个数组
                lat_temp_array = latlist_temp.split(",");
                lng_temp_array = lnglist_temp.split(",");
            }else{
                //如果只有一个数据
                lat_temp_array = new String[1];
                lng_temp_array = new String[1];
                lat_temp_array[0] = latlist_temp;
                lng_temp_array[0] = lnglist_temp;
            }

            pointsToDrawFirst = new ArrayList<LatLng>();
            pointsToDrawFirst.add(new
                    LatLng(Double.parseDouble(lat_temp_array[0]), Double.parseDouble(lng_temp_array[0])));

            for (int i = 0; i < lat_temp_array.length; i++){
                pointsToDrawFirst.add(new LatLng(Double.parseDouble(lat_temp_array[i]), Double.parseDouble(lng_temp_array[i])));

            }

            //将editRecord界面的最后一个点与inspect界面的第一个点连起来
            point = pointsToDrawFirst.get(pointsToDrawFirst.size() - 1);
            points.add(point);
        }
        initLocation();
        mLocationClient.start();

        findViewById(R.id.iv_head_left).setOnClickListener(backToEditListener);

        btn_stop.setOnClickListener(backToEditListener);

        if(isAddNewRiverRecord) {
            startTimer();//打开定时器，向服务器传轨迹点
//            showToast("kai");
        }else {
//            showToast("meikan");
        }
    /*    btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopLocClient = true;
                if(mLocationClient.isStarted()){
                    drawEnd(points);
                    mLocationClient.stop();

                    Intent intent = new Intent();
                    intent.putExtra("result",points.toString());
                    intent.putExtra("latList",  latList);
                    intent.putExtra("lngList", lngList);

                    ChiefInspectActivity.this.setResult(RESULT_OK, intent);
                    ChiefInspectActivity.this.finish();
                }
            }
        });*/

        myPosition = (Button) findViewById(R.id.btn_my_position);
        myPosition.setClickable(false);
        myPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(points!=null && points.size()>0){
                    mBaiduMap.setMyLocationData(new MyLocationData.Builder()
                            .latitude(points.get(points.size()-1).latitude)
                            .longitude(points.get(points.size()-1).longitude).build());

                    MapStatus status = new MapStatus.Builder().target(points.get(points.size()-1)).zoom(Values.MAP_ZOOM_LEVEL).build();
                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
                }else {
                    mBaiduMap.setMyLocationData(new MyLocationData.Builder()
                            .latitude(getLatitude()).longitude(getLongitude()).build());

                    MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(Values.MAP_ZOOM_LEVEL).build();
                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
                }
            }
        });
        riverPosition = (Button) findViewById(R.id.btn_river_position);
        riverPosition.setClickable(false);
        riverPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (riverStart != null && riverEnd != null) {
                    float lat = (float) ((riverStart.latitude + riverEnd.latitude) / 2);
                    float lng = (float) ((riverStart.longitude + riverEnd.longitude) / 2);
                    mBaiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

                    //target：设置地图中心点；zoom:设置缩放级别
                    MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(14).build();
                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
                }else {
                    showToast("暂时未录入河道信息。");
                }
            }
        });
    }

    /**
     * 地图上显示河道
     * drawRiverStartAndEnd
     */
    private void drawRiver() {

        //在地图上显示起点和终点
        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.river_end);
        BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.river_end);
//        baiduMap.addOverlay(optionMe);

        if (riverStart != null && riverEnd != null) {
//            MarkerOptions optionFrom = new MarkerOptions().position(riverStart).icon(bmp_from);
//            MarkerOptions optionTo = new MarkerOptions().position(riverEnd).icon(bmp_to);
//            mBaiduMap.addOverlay(optionFrom);//不画起止点
//河道轨迹连线版
//            OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.BLUE).points(riverAllPoint);
//            mBaiduMap.addOverlay(ooPolyline);
            //河道轨迹点点版hh
            BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.my_blue5px);

            for(int i=0;i<riverAllPoint.size();i++){
                OverlayOptions option1 =  new MarkerOptions()
                        .position(new LatLng(riverAllPoint.get(i).latitude, riverAllPoint.get(i).longitude))
                        .icon(bdA);
                mBaiduMap.addOverlay(option1);
            }



//            Marker marker_End = (Marker) mBaiduMap.addOverlay(optionTo);
        }else {
            showToast("暂时未录入河道信息。");
        }

    }
    //定时器，定时向服务器发送轨迹经纬度信息
    private int DELAY_TIME = 1000;//延时1s开启定时器
    private int PERIOD_TIME = 90000;//每过90s传一次数据
    Timer mTimer = null;
    TimerTask mTimerTask = null;
    JSONObject submitTemporaryParam = null;

    //定时器，用于记录巡河时长并显示
    Timer passTimer = null;
    TimerTask passTimerTask = null;
    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try{
                        submitTemporaryParam.put("latList",latList);
                        submitTemporaryParam.put("lngList",lngList);
                        submitTemporaryParam.put("riverId",riverId);
                        submitTemporaryParam.put("passTime",passTime);

                        if(latlist_temp!=null && !latlist_temp.equals("")){
                            getRequestContext().add("AddOrEdit_RiverRecordTemporary", new Callback<BaseRes>() {
                                @Override
                                public void callback(BaseRes o) {
                                    if (o != null && o.isSuccess()) {

                                    }
                                }
                            }, BaseRes.class, submitTemporaryParam);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, DELAY_TIME, PERIOD_TIME);

        //巡河时长相关定时器和定时任务
        if (passTimer == null) {
            passTimer = new Timer();
        }

        if (passTimerTask == null) {
            passTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            showToast(getStringTime(passTime++));
                            passTimeTextView.setText(getStringTime(passTime++));
                            System.out.println("testrc: passtime: "+passTime);
                        }
                    });

                }
            };
        }

        if(passTimer != null && passTimerTask != null )
            passTimer.schedule(passTimerTask, 500, 1000);

    }

    private void stopTimer(){
        //服务器暂存轨迹相关定时器
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }

        //巡河时长相关定时器
        if (passTimer != null) {
            passTimer.cancel();
            passTimer = null;
        }
        if (passTimerTask != null) {
            passTimerTask.cancel();
            passTimerTask = null;
        }
    }
    //巡河时长显示
    private String getStringTime(int cnt) {
//		int hour = cnt/3600;
        int min = cnt / 60;
        int second = cnt % 60;
        return String.format(Locale.CHINA,"%02d 分 %02d 秒",min,second);
    }

    View.OnClickListener backToEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToRecordEdit();
        }
    };

    //重写安卓系带返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //TODO something
            backToRecordEdit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backToRecordEdit(){
        isStopLocClient = true;
        if(mLocationClient.isStarted()){
//            drawEnd(points);
            mLocationClient.stop();

            Intent intent = new Intent();
            intent.putExtra("result",points.toString());
//            intent.putExtra("latList",  OptimizePoints(latList,lngList)[0]);
//            intent.putExtra("lngList", OptimizePoints(latList,lngList)[1]); //传优化后的轨迹数据
            intent.putExtra("latList",  latList);
            intent.putExtra("lngList", lngList); //优化前的轨迹数据
            intent.putExtra("isAddNewRiverRecord",isAddNewRiverRecord);
            intent.putExtra("passTime",passTime);
            ChiefInspectActivity.this.setResult(RESULT_OK, intent);

            //若成功提交，则地理坐标缓存值设置设为空
            getUser().setBaiduLatPoints("");
            getUser().setBaiduLngPoints("");

            ChiefInspectActivity.this.finish();
        }
    }
    private void drawBeforeTrack() {

        showOperating();

        mBaiduMap.clear();

        drawRiver();//画出河道的起止点

        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        LatLng from = pointsToDrawFirst.get(0);
        Log.i("recordinspect", "起点坐标" + from.toString());
        MarkerOptions optionFrom = new MarkerOptions().position(from).icon(bmp_from);
        mBaiduMap.addOverlay(optionFrom);

        OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.GREEN).points(pointsToDrawFirst);
        mBaiduMap.addOverlay(ooPolyline);
        Log.i("recordinspect", "画过起点");

        MapStatus status = new MapStatus.Builder().target(from).zoom(Values.MAP_ZOOM_LEVEL).build();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

        hideOperating();

    }

    private void initLocation(){

        ///设置是否允许定位图层
        mBaiduMap.setMyLocationEnabled(true);

        //定位初始化，创建LocationClient对象，创建时传入Context对象
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开GPS
        option.setAddrType("all");//返回的定位结果包含地址信息
        option.setCoorType("bd09ll");//设置坐标类型
        option.setScanSpan(1000);//设置发起定位请求的间隔时间为1000ms
        option.setPriority(LocationClientOption.GpsFirst);
        mLocationClient.setLocOption(option);
    }

    private class MyRunable implements Runnable {

        @Override
        public void run() {
            if(!mLocationClient.isStarted()){
                mLocationClient.start();
            }
            if(!isStopLocClient){

                if (points == null) {
                    handler.postDelayed(this, 2000);
                    System.out.println("testrc: IP+points == null");
                    return;
                }
                if(points.size()>=1){
                    //lngList和latList的处理
                    if(lngList == null || latList == null){
                        lngList = "" + points.get(points.size() - 1).longitude;
                        latList = "" + points.get(points.size() - 1).latitude;
//                        point = points.get(points.size() - 1);
                    }else {
                        if(point == null){
                            point = points.get(points.size() - 1);
                            lngList = lngList + "," + point.longitude;
                            latList = latList + "," + point.latitude;
                        }
//                        threePointsToOnePoint.add(points.get(points.size() - 1));
//                        if(threePointsToOnePoint.size() >= 3) {

                        if(DistanceUtil.getDistance(point,points.get(points.size()-1))<180
                                && DistanceUtil.getDistance(point,points.get(points.size()-1))>0.5){
//                            showToast("distance"+String.valueOf(countOfHandler++)+"...."+String.valueOf(getDistance(point,points.get(points.size()-1))));
//                        showToast("DU-distance(if):"+String.valueOf(DistanceUtil.getDistance(point,points.get(points.size()-1))));
//                            point = medianFilterOfPoints(points, point);
                            point = points.get(points.size()-1);
                            lngList = lngList + "," + point.longitude;
                            latList = latList + "," + point.latitude;
//                            showToast(latList);
                            Log.d("lngList:", lngList);
                            Log.d("latList:", latList);
                        }else {
//                        showToast("DU-distance(else):"+String.valueOf(DistanceUtil.getDistance(point,points.get(points.size()-1))));
                            if(DistanceUtil.getDistance(point,points.get(points.size()-1))>=180){
                                countNoJoin++;
                                if (countNoJoin >=5){
                                    countNoJoin = 0;
                                    if (lngList.contains(",")){
                                        //如果不止一个数据，变成一个数组
                                        lat_array = latList.split(",");
                                        lng_array = lngList.split(",");
                                    }else{
                                        //如果只有一个数据
                                        lat_array = new String[1];
                                        lng_array = new String[1];
                                        lat_array[0] = latList;
                                        lng_array[0] = lngList;
                                    }
                                    lat_array[lat_array.length-1] = String.valueOf(points.get(points.size()-1).latitude);
                                    lng_array[lng_array.length-1] = String.valueOf(points.get(points.size()-1).longitude);
                                    lngList = lng_array[0];
                                    latList = lat_array[0];
                                    for (int k = 1;k<lat_array.length;k++){
                                        lngList = lngList + "," + lng_array[k];
                                        latList = latList + "," + lat_array[k];
                                    }
                                    point = points.get(points.size()-1);
                                }
                            }else {
                                countNoJoin = 0;
                            }
                        }

                    }
                }
//                    Log.d("points_to_server:", points_to_server.toString());
//                    countForPoint = 0;
//                }
//                //巡河轨迹测试
//                i +=1;
//                showToast(latList.substring(latList.length()>20? latList.length()-20 :0,latList.length())+"..."+i);

                handler.postDelayed(this,2000);
            }
        }
    }

    //对轨迹的数组，取最新的三个点并按照与point的距离排序，最后取三个数的中间一个
    private LatLng medianFilterOfPoints(List<LatLng> pointsTemp,LatLng pointTemt){
        double one, two, three;
        LatLng decidePoint;
        if (pointsTemp.size()<3){
            return pointsTemp.get(pointsTemp.size() - 1);
        }else {
            one = getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 1));//倒数第一个点与pointTemt的距离
            two = getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 2));//倒数第2个点与pointTemt的距离
            three = getDistance(pointTemt,pointsTemp.get(pointsTemp.size() - 3));//倒数第3个点与pointTemt的距离
            if((one>=two && one<three)||(one >=three && one<two)){
                decidePoint = pointsTemp.get(pointsTemp.size() - 1);
            }else if((two>one && two<three)||(two>=three && two<=one)){
                decidePoint = pointsTemp.get(pointsTemp.size() - 2);
            }else {
                decidePoint = pointsTemp.get(pointsTemp.size() - 3);
            }
            return decidePoint;
        }
    }

    //轨迹平滑滤波。SG5点1阶平滑。
    public String[] OptimizePoints(String latListIn,String lngListIn){
        String latListOut = new String("");
        String lngListOut = new String("");//输出经纬度字符串
        String[] latListInArray;
        String[] lngListInArray;//输出经纬度数组
        ArrayList<LatLng> pointsIn = new ArrayList<LatLng>();//输入点字符串转化

        double latitude;
        double longitude;

        if(!latListIn.equals("") && latListIn!=null){
            if (latListIn.contains(",")){
                //如果不止一个数据，变成一个数组
                latListInArray = latListIn.split(",");
                lngListInArray = lngListIn.split(",");
            }else{
                //如果只有一个数据
                latListInArray = new String[1];
                lngListInArray = new String[1];
                latListInArray[0] = latListIn;
                lngListInArray[0] = lngListIn;
            }
            //是否有超过5 个点
            if(latListInArray.length>=5){
                for (int i = 0; i < latListInArray.length; i++){
                    pointsIn.add(new LatLng(Double.parseDouble(latListInArray[i]), Double.parseDouble(lngListInArray[i])));
                }
                int sizeOfPointIn = pointsIn.size();
                for(int i = 0; i < sizeOfPointIn;i++){
                    if(i==0){//第一个
                        latitude =  (3*pointsIn.get(0).latitude+2*pointsIn.get(1).latitude+pointsIn.get(2).latitude-pointsIn.get(4).latitude)/5;
                        longitude = (3*pointsIn.get(0).longitude+2*pointsIn.get(1).longitude+pointsIn.get(2).longitude-pointsIn.get(4).longitude)/5;
                        latListOut = latListOut+latitude;
                        lngListOut = lngListOut + longitude;
                    }else if(i == 1){
                        latitude =  (4*pointsIn.get(0).latitude+3*pointsIn.get(1).latitude+2*pointsIn.get(2).latitude+pointsIn.get(3).latitude)/10;
                        longitude = (4*pointsIn.get(0).longitude+3*pointsIn.get(1).longitude+2*pointsIn.get(2).longitude+pointsIn.get(3).longitude)/10;
                        latListOut = latListOut + "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }else if(i==sizeOfPointIn-1){//最后1个点
                        latitude =  (3*pointsIn.get(sizeOfPointIn-1).latitude+2*pointsIn.get(sizeOfPointIn-2).latitude+
                                pointsIn.get(sizeOfPointIn-3).latitude-pointsIn.get(sizeOfPointIn-5).latitude)/5;
                        longitude = (3*pointsIn.get(sizeOfPointIn-1).longitude+2*pointsIn.get(sizeOfPointIn-2).longitude+
                                pointsIn.get(sizeOfPointIn-3).longitude-pointsIn.get(sizeOfPointIn-5).longitude)/5;
                        latListOut = latListOut+ "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }else if(i == sizeOfPointIn-2){
                        latitude =  (4*pointsIn.get(sizeOfPointIn-1).latitude+3*pointsIn.get(sizeOfPointIn-2).latitude+
                                2*pointsIn.get(sizeOfPointIn-3).latitude+pointsIn.get(sizeOfPointIn-4).latitude)/10;
                        longitude = (4*pointsIn.get(sizeOfPointIn-1).longitude+3*pointsIn.get(sizeOfPointIn-2).longitude+
                                2*pointsIn.get(sizeOfPointIn-3).longitude+pointsIn.get(sizeOfPointIn-4).longitude)/10;
                        latListOut = latListOut + "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }else{// 中间的点
                        latitude =  (pointsIn.get(i-1).latitude+pointsIn.get(i-2).latitude+pointsIn.get(i).latitude+
                                pointsIn.get(i+1).latitude+pointsIn.get(i+2).latitude)/5;
                        longitude = (pointsIn.get(i-1).longitude+pointsIn.get(i-2).longitude+pointsIn.get(i).longitude+
                                pointsIn.get(i+1).longitude+pointsIn.get(i+2).longitude)/5;
                        latListOut = latListOut + "," + latitude;
                        lngListOut = lngListOut + "," + longitude;
                    }
                }
            }else {
                latListOut = latListIn;
                lngListOut = lngListIn;
            }

        }
        String[] out = {latListOut,lngListOut};
        return out;
    }

    /**
     * 定位SDK监听函数
     */
    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location == null || mMapView == null)
                return;
            if (!PatrolRecordUtils.isOPen(getApplicationContext())) {
//                Toast.makeText(ChiefInspectActivity.this, "定位未开启，请打开定位。", Toast.LENGTH_SHORT).show();
                showMyToast(Toast.makeText(ChiefInspectActivity.this, "定位未开启，请打开定位。", Toast.LENGTH_LONG),900);
            }
            if (location.getLocType() == BDLocation.TypeNetWorkException || location.getLocType() == BDLocation.TypeCriteriaException){
                Toast.makeText(ChiefInspectActivity.this, "信号弱，请确定网络是否通畅。", Toast.LENGTH_SHORT).show();
            }else if (location.getLocType() == BDLocation.TypeOffLineLocationFail || location.getLocType() == BDLocation.TypeOffLineLocationNetworkFail) {
                Toast.makeText(ChiefInspectActivity.this, "定位失败，请检查。", Toast.LENGTH_SHORT).show();
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(0)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            LatLng newPoint = new LatLng(location.getLatitude(),location.getLongitude());
//            points.add(newPoint);

            if(isFirstLoc==0){
//                showToast("isFirstLoc(if):"+points.size());
                isFirstLoc++;
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());

                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(ll);
                //animateMapStatus()方法把定位到的点移动到地图中心
                mBaiduMap.animateMapStatus(msu);
                MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(Values.MAP_ZOOM_LEVEL).build();
                //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

                System.out.println("testrc: IP:isFirstLoc==0 points.size()= "+points.size());
            }else if(isFirstLoc<=4 && hasHistroyData){
                isFirstLoc++;
                System.out.println("testrc: IP:isFirstLoc<=4 && hasHistroyData points.size()= "+points.size());
            }else if(isFirstLoc<=10 && !hasHistroyData){
                isFirstLoc++;
                System.out.println("testrc: IP:iisFirstLoc<=6 && !hasHistroyData points.size()= "+points.size());
            }else{
//                Toast.makeText(getCurActivity(),"isFirstLoc(else):speed: "+location.getSpeed(),Toast.LENGTH_SHORT).show();
                if(newPoint.latitude>20){
                    points.add(newPoint);
                }
            }

            if(points.size() == 2 && !hasHistroyData ){
                myPosition.setClickable(true);
                riverPosition.setClickable(true);
                drawStart(points);
            }else if (points.size() == 2 && hasHistroyData){
                myPosition.setClickable(true);
                riverPosition.setClickable(true);
                drawBeforeTrack();

//巡河轨迹连线版
                options = new PolylineOptions().color(Color.GREEN).width(10).points(points);
                mBaiduMap.addOverlay(options);
//巡河轨迹点点版hh
//                points.get(points.size()-1).latitude
//                BitmapDescriptor bdA = BitmapDescriptorFactory
//                        .fromResource(R.drawable.my_green);
//
//                for(int i=1;i<points.size();i++){
//                    OverlayOptions option1 =  new MarkerOptions()
//                            .position(new LatLng(points.get(i).latitude, points.get(i).longitude))
//                            .icon(bdA);
//                    mBaiduMap.addOverlay(option1);
//                    }



//                if(pointsToDrawFirst.size()>=1){
//                    MapStatus status = new MapStatus.Builder().target(new LatLng(pointsToDrawFirst.get(0).latitude,pointsToDrawFirst.get(0).longitude)).zoom(19).build();
//                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
//                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
//                }else {
//                    MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(19).build();
//                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
//                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
//                }
            }else if(points.size() > 4){
                points_tem = points.subList(points.size() - 4,points.size());
                options = new PolylineOptions().color(Color.GREEN).width(10).points(points_tem);
                mBaiduMap.addOverlay(options);
            }
        }
    }
    private void drawStart(List<LatLng> points) {
        drawRiver();//画出河道的起止点

        //将起点处的经纬度添加至经纬度数组之中
        if (lngList == null || latList == null){
            lngList = "" + points.get(points.size()-1).longitude;
            latList = "" + points.get(points.size()-1).latitude;
        }
//        LatLng avePoint = new LatLng(myLat / points.size(),myLng / points.size());
        LatLng nowPoint = new LatLng(points.get(points.size()-1).latitude,points.get(points.size()-1).longitude);
        points.add(nowPoint);

        MapStatus status = new MapStatus.Builder().target(nowPoint).zoom(Values.MAP_ZOOM_LEVEL).build();
        //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

        options = new MarkerOptions().position(nowPoint).icon(track_start);
        mBaiduMap.addOverlay(options);

    }

    private void drawEnd(List<LatLng> points) {
        double myLat = 0.0;
        double myLng = 0.0;
        if(points.size() > 5){
            for (int i = points.size() - 5; i < points.size(); i++){
                LatLng ll = points.get(i);
                myLat += ll.latitude;
                myLng += ll.longitude;
            }
            LatLng avePoint = new LatLng(myLat / 5, myLng / 5);

            //将终点处的坐标添加至经纬度坐标数组之中
            lngList = lngList + "," + myLng / 5;
            latList = latList + "," + myLat / 5;

            //MarkerOptions options = new MarkerOptions().position(new LatLng(s.compLat, s.compLong)).icon(s.isHandled() ? bmp_done : bmp_undo).title("" + s.compId);
            //options = new DotOptions().center(avePoint).color(0xAAff00ff).radius(15);
            options = new MarkerOptions().position(avePoint).icon(track_end);
            mBaiduMap.addOverlay(options);

//            System.out.println("巡河轨迹点坐标：" + points.toString());
        }
    }



    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        getUser().setBaiduLatPoints(OptimizePoints(latList,lngList)[0]);
//        getUser().setBaiduLngPoints(OptimizePoints(latList,lngList)[1]);
        getUser().setBaiduLatPoints(latList);
        getUser().setBaiduLngPoints(lngList);
//        stopTimer();//关闭时钟，停止上传轨迹点
    }

    @Override
    protected void onDestroy() {
        //退出时销毁地位
        mLocationClient.stop();
        isStopLocClient = true;

        mBaiduMap.setMyLocationEnabled(false);
        if(mMapView!=null){
//            mMapView.onDestroy();
        }
        mMapView = null;
        mBaiduMap = null;
        stopTimer();//关闭时钟，停止上传轨迹点
        super.onDestroy();
    }
}
