package com.zju.rchz.lakechief.activity;

import com.baidu.mapapi.utils.DistanceUtil;
import com.zju.rchz.activity.BaseActivity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zju.rchz.R;
import com.zju.rchz.Values;
import com.zju.rchz.activity.ComplainMap;
import com.zju.rchz.activity.PhotoViewActivity;
import com.zju.rchz.model.Imag;
import com.zju.rchz.model.WholeRiverMap;
import com.zju.rchz.model.WholeRiverMapRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ImgUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.PatrolRecordUtils;
import com.zju.rchz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by ZJTLM4600l on 2018/7/13.
 */

public class LakeChiefTrackViewActivity extends BaseActivity {

    private BaiduMap baiduMap = null;
    private  InfoWindow mInfoWindow;
    protected Location location;
    private  static String latList;
    private  static String lngList;
    private  static String imgLnglist;//图片经纬度
    private static String imgLatlist;
    private static String picPath;
    private static Double longitude;
    private static Double latitude;
    private String patrolLength;//巡河长度
    private String patrolTime;//巡河时长

    private static String[] latArray;
    private static String[] lngArray;
    private static String[] imgLnglistArray;
    private static String[] imgLatlistArray;
    private static String[] picPathArray;
    private Imag[] imags;
    private Imag imagTemp=new Imag();
    private LatLng[] imagesLocation;

    private List<LatLng> pointsToDraw;
    private List<Integer> abnormalPoints;//轨迹分段显示的各组点

    private LatLng lakeStart;
    private LatLng lakeEnd;
    private String lakeAllLatitude;//河道全部点
    private String lakeAllLongitude;
    private String[] lakeAllLatitudeArray;
    private String[] lakeAllLongitudeArray;
    WholeRiverMap wholeLakeMap;
    ArrayList<LatLng> lakeAllPoint;

    //传入河道编号：
    private int lakeId;

    Handler handler = new Handler();
    private Button trackPosition;
    private Button lakePosition;
    private TextView txPatrolTime;
    private TextView txPatrolLength;

    BitmapDescriptor bmp_image = BitmapDescriptorFactory.fromResource(R.drawable.river_record_image_location);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_chief_trackview);
        setTitle("查看巡湖轨迹");
        initHead(R.drawable.ic_head_back,0);

        handler.postDelayed(new MyRunable(),3000);
        //地图初始化
        MapView mv_trackview = (MapView) findViewById(R.id.mv_trackview);
        baiduMap = mv_trackview.getMap();
        baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
        mv_trackview.showZoomControls(false);

        txPatrolTime = (TextView) findViewById(R.id.tv_passTime);
        txPatrolLength = (TextView) findViewById((R.id.tv_dstance));

        //得到轨迹经纬度数组
        Bundle bundle=this.getIntent().getExtras();
        latList = bundle.getString("latList");
        lngList = bundle.getString("lngList");
        //得到图片经纬度
        imgLatlist=bundle.getString("imgLatlist");
        imgLnglist=bundle.getString("imgLnglist");
        picPath=bundle.getString("picPath");
//        longitude=bundle.getDouble("longitude");
//        latitude=bundle.getDouble("latitude");
        lakeId = bundle.getInt("lakeId",0);
        patrolLength = bundle.getString("patrolLength");
        patrolTime = bundle.getString("patrolTime");

        if(patrolLength != null && !patrolLength.equals("")){
            txPatrolLength.setText(patrolLength);
        }
        if(patrolTime != null && !patrolTime.equals("")){
            txPatrolTime.setText(patrolTime);
        }

        if(lakeId!=0){
            getRequestContext().add("Get_WholeLakeMap", new Callback<WholeRiverMapRes>() {
                @Override
                public void callback(WholeRiverMapRes o) {
                    hideOperating();
                    if (o != null && o.isSuccess()) {
                        if (o.data !=null && o.data.allLongitude!=null){
                            wholeLakeMap = o.data;
                            lakeAllLatitude = wholeLakeMap.allLatitude;
                            lakeAllLongitude = wholeLakeMap.allLongitude;
                            System.out.println("testrc: o.data !=null && o.data.allLongitude!=null");
                        }
                        if(lakeAllLatitude !=null && !lakeAllLatitude.equals("")){
                            System.out.println("testrc: //得到湖泊的所有坐标");
                            //得到河道的所有坐标
                            if (lakeAllLatitude!=null && lakeAllLatitude.contains(",")){
                                //如果不止一个数据，变成一个数组
                                lakeAllLatitudeArray = lakeAllLatitude.split(",");
                                lakeAllLongitudeArray = lakeAllLongitude.split(",");
                                System.out.println("testrc: //如果不止一个数据，变成一个数组");
                            }else{
                                //如果只有一个数据
                                lakeAllLatitudeArray = new String[1];
                                lakeAllLongitudeArray = new String[1];
                                lakeAllLatitudeArray[0] = lakeAllLatitude;
                                lakeAllLongitudeArray[0] = lakeAllLongitude;
                                System.out.println("testrc: //如果只有一个数据");
                            }
                            lakeStart =new LatLng(Double.parseDouble(lakeAllLatitudeArray[0]), Double.parseDouble(lakeAllLongitudeArray[0]));
                            lakeEnd =new LatLng(Double.parseDouble(lakeAllLatitudeArray[lakeAllLatitudeArray.length-1]),
                                    Double.parseDouble(lakeAllLongitudeArray[lakeAllLatitudeArray.length-1]));

                            lakeAllPoint = new ArrayList<LatLng>();

                            for (int i = 0; i < lakeAllLatitudeArray.length; i++){
                                lakeAllPoint.add(new LatLng(Double.parseDouble(lakeAllLatitudeArray[i]), Double.parseDouble(lakeAllLongitudeArray[i])));
                            }
                        }
                    }
                }
            }, WholeRiverMapRes.class, ParamUtils.freeParam(null, "lakeId", lakeId));
        }

        //将字符串变成数组形式,如果只含有一个坐标点，强行变成两个
        if (getIntent().getExtras().getString("latList") != null){

            //如果仅有一个坐标，则其不包含逗号
            if (!latList.contains(",")){
                latList = latList + "," + latList;
                lngList = lngList + "," + lngList;
            }
        }
        latArray = latList.split(",");
        lngArray = lngList.split(",");

//        if(imgLatlist==null||imgLnglist==null||picPath==null) {
//            imgLatlist=longitude.toString();
//            imgLnglist=latitude.toString();
//        }
        if (imgLatlist!=""&&imgLnglist!=""&&picPath!=""){
            imgLatlistArray = imgLatlist.split(",");
            imgLnglistArray = imgLnglist.split(",");
            picPathArray = picPath.split(";");

            if (imgLnglistArray[0]!=""&&imgLatlistArray[0]!=""){
                imags = new Imag[imgLnglistArray.length];
                imagesLocation = new LatLng[imgLnglistArray.length];

                for (int i = 0; i < imgLnglistArray.length; i++) {
                    if(imgLatlistArray[i]!=""&&imgLnglistArray[i]!=""){
                        imagesLocation[i] = new LatLng(Double.parseDouble(imgLatlistArray[i]), Double.parseDouble(imgLnglistArray[i]));
                    }
                }
            }else {
                imgLatlistArray=new String[0];
                imgLnglistArray=new String[0];
                picPathArray=new String[0];
                imags=new Imag[0];
                imagesLocation=new LatLng[0];
//                Toast.makeText(this,imgLatlistArray.length+"IFelse"+imgLnglistArray.length,Toast.LENGTH_LONG).show();
            }
        }else {
            imgLatlistArray=new String[0];
            imgLnglistArray=new String[0];
            picPathArray=new String[0];
            imags=new Imag[0];
            imagesLocation=new LatLng[0];
//            Toast.makeText(this,imgLatlistArray.length+"ELSE"+imgLnglistArray.length,Toast.LENGTH_LONG).show();
        }

        pointsToDraw = new ArrayList<LatLng>();
        //开始循环填充数据
        for (int i = 0; i < latArray.length; i ++){
            pointsToDraw.add(new LatLng(Double.parseDouble(latArray[i]),Double.parseDouble(lngArray[i])));
        }
        abnormalPoints = new ArrayList<Integer>();
        if (pointsToDraw.size()>1){
            abnormalPoints = PatrolRecordUtils.segmentTrack(pointsToDraw);
        }
        Log.i("来自trackView的latArray", latArray[0].toString());
        Log.i("来自trackView的lngArray", lngArray[0].toString());
        Log.i("pointsToDraw", pointsToDraw.toString());

        //覆盖物点击响应事件
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getIcon()==bmp_image){
                    switch (marker.getTitle()){
                        case "0":{
                            initResultPhotoView(picPathArray[0],marker);
                            break;
                        }
                        case "1":{
                            initResultPhotoView(picPathArray[1],marker);
                            break;
                        }
                        case "2":{
                            initResultPhotoView(picPathArray[2],marker);
                            break;
                        }
                        default:break;
                    }
                }else {
                    showLocation(marker);
                }
                return false;
            }
        });

        trackPosition = (Button) findViewById(R.id.btn_my_position);
        trackPosition.setClickable(false);
        trackPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pointsToDraw!=null){
                    float lat = (float) ((pointsToDraw.get(0).latitude + pointsToDraw.get(pointsToDraw.size()-1).latitude) / 2);
                    float lng = (float) ((pointsToDraw.get(0).longitude + pointsToDraw.get(pointsToDraw.size()-1).longitude) / 2);
                    baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

                    //target：设置地图中心点；zoom:设置缩放级别
                    MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(17).build();
                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
                }

            }
        });
//        findViewById(R.id.river_legend).setVisibility(View.GONE);
//        findViewById(R.id.btn_my_position).setVisibility(View.GONE);
//        findViewById(R.id.btn_river_position).setVisibility(View.GONE);
        lakePosition = (Button) findViewById(R.id.btn_river_position);
        lakePosition.setClickable(false);
        lakePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lakeStart != null && lakeEnd != null) {
                    float lat = (float) ((lakeStart.latitude + lakeEnd.latitude) / 2);
                    float lng = (float) ((lakeStart.longitude + lakeEnd.longitude) / 2);
                    baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

                    //target：设置地图中心点；zoom:设置缩放级别
                    MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(14).build();
                    //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
                }else {
                    showToast("暂时未录入湖泊信息。");
                }
            }
        });
    }

    private class MyRunable implements Runnable {

        @Override
        public void run() {
            lakePosition.setClickable(true);
            trackPosition.setClickable(true);
            //开始画轨迹
            drawTrack();
        }
    }

    private void showLocation(final Marker marker) {  //显示气泡
        // 创建InfoWindow展示的view

        LatLng pt = null;
        double latitude, longitude;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        View view = LayoutInflater.from(this).inflate(R.layout.map_item, null); //自定义气泡形状
        TextView textView_longitude = (TextView) view.findViewById(R.id.tv_longitude);
        TextView textView_latitude = (TextView) view.findViewById(R.id.tv_latitude);
        pt = new LatLng(latitude + 0.0004, longitude + 0.00005);
        textView_longitude.setText(Double.toString(pt.longitude));
        textView_latitude.setText(Double.toString(pt.latitude));

        // 定义用于显示该InfoWindow的坐标点
        // 创建InfoWindow的点击事件监听者
        InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
            public void onInfoWindowClick() {
                baiduMap.hideInfoWindow();//影藏气泡

            }
        };
        // 创建InfoWindow
        mInfoWindow = new InfoWindow(view, pt, -5);
        baiduMap.showInfoWindow(mInfoWindow); //显示气泡

    }

    private void drawTrack() {
        baiduMap.clear();

        //画湖泊
        drawLake();

        //在地图上显示起点和终点
        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.track_start);
        BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.track_end);

        //起点和终点值
        LatLng from = pointsToDraw.get(0);
        LatLng to = pointsToDraw.get(pointsToDraw.size() - 1);

        //起点和终点标记
        MarkerOptions optionFrom = new MarkerOptions().position(from).icon(bmp_from);
        MarkerOptions optionTo = new MarkerOptions().position(to).icon(bmp_to);
        MarkerOptions[] optionsesImage=new MarkerOptions[imagesLocation.length];
        Marker[] markers=new Marker[imagesLocation.length];
        for (int i=0;i<imagesLocation.length;i++){
            optionsesImage[i]=new MarkerOptions().position(imagesLocation[i]).icon(bmp_image);
            markers[i]=(Marker) baiduMap.addOverlay(optionsesImage[i]);
            markers[i].setTitle(i+"");
        }

        //添加标记
        baiduMap.addOverlay(optionFrom);
        baiduMap.addOverlay(optionTo);

        //设置地图中心位置
        float lat = (float) ((from.latitude + to.latitude)/2);
        float lng = (float) ((from.longitude + to.longitude)/2);
        baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

        OverlayOptions ooPolyline;
        for(int i = 0;i<pointsToDraw.size()-1;i++){
            if(DistanceUtil.getDistance(pointsToDraw.get(i),pointsToDraw.get(i+1))>=180){
                //两点之间距离异常的点
                ooPolyline = new PolylineOptions().width(4).dottedLine(true)
                        .color(Color.LTGRAY).points(pointsToDraw.subList(i,i+2));
                baiduMap.addOverlay(ooPolyline);
            }else{
                //两点之间距离正常的点
                ooPolyline = new PolylineOptions().width(10)
                        .color(Color.GREEN).points(pointsToDraw.subList(i,i+2));
                baiduMap.addOverlay(ooPolyline);
            }
        }
//        if(pointsToDraw.size()>1 && abnormalPoints.size()>0){
//            int i = 0;
//            int last= 0;//起始点
//            for (;i<abnormalPoints.size();i++){
//                if(i>0){
//                    last = abnormalPoints.get(i-1);
//                }
//                //两点之间距离正常的点
//                OverlayOptions ooPolyline = new PolylineOptions().width(10)
//                        .color(Color.GREEN).points(pointsToDraw.subList(last,abnormalPoints.get(i)+1));
//                baiduMap.addOverlay(ooPolyline);
//                //两点之间距离异常的点
//                ooPolyline = new PolylineOptions().width(4).dottedLine(true)
//                        .color(Color.LTGRAY).points(pointsToDraw.subList(abnormalPoints.get(i),abnormalPoints.get(i)+2));
//                baiduMap.addOverlay(ooPolyline);
//            }
//            if (abnormalPoints.get(i-1)<pointsToDraw.size()-2){
//                //两点之间距离正常的点
//                OverlayOptions ooPolyline = new PolylineOptions().width(10)
//                        .color(Color.GREEN).points(pointsToDraw.subList(abnormalPoints.get(i-1)+1,pointsToDraw.size()));
//                baiduMap.addOverlay(ooPolyline);
//            }
//        }else{
//            //可设置循环，添加坐标点
//            OverlayOptions ooPolyline = new PolylineOptions().width(10)
//                    .color(Color.GREEN).points(pointsToDraw);
//            baiduMap.addOverlay(ooPolyline);
//        }

        //设置地图中心点与设置缩放级别
        MapStatus status = new MapStatus.Builder().target(new LatLng(lat,lng)).zoom(Values.MAP_ZOOM_LEVEL).build();
        //改变地图的状态
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

    }
    private void showImageLocation(final Marker marker,View view) {  //显示气泡
        // 创建InfoWindow展示的view

        LatLng pt = null;
        double latitude, longitude;
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

//        View view = LayoutInflater.from(this).inflate(R.layout.com_map_item, null); //自定义气泡形状

        pt = new LatLng(latitude + 0.0004, longitude + 0.00005);


        // 定义用于显示该InfoWindow的坐标点
        // 创建InfoWindow的点击事件监听者
        InfoWindow.OnInfoWindowClickListener listener = new InfoWindow.OnInfoWindowClickListener() {
            public void onInfoWindowClick() {
                baiduMap.hideInfoWindow();//影藏气泡

            }
        };
        // 创建InfoWindow
        mInfoWindow = new InfoWindow(view, pt, -5);
        baiduMap.showInfoWindow(mInfoWindow); //显示气泡

    }

    private void initResultPhotoView(String urls,Marker marker) {

        List<String> list = new ArrayList<String>();
        if (urls != null) {
            for (String url : urls.split(";")) {
                String s = url.trim();
                if (s.length() > 0) {
                    list.add(StrUtils.getImgUrl(s));
                }
            }
        }
        if (list.size() > 0) {
            View view1 = LayoutInflater.from(this).inflate(R.layout.com_map_item, null); //自定义气泡形状
            imgResultUrls = new String[list.size()];
            imgResultUrls = list.toArray(imgResultUrls);
            view1.findViewById(R.id.com_map_result_photos).setVisibility(View.VISIBLE);
            LinearLayout ll_photos = (LinearLayout) view1.findViewById(R.id.ll_com_map_result_photos);
            ll_photos.removeAllViews();

            for (int i = 0; i < imgResultUrls.length; ++i) {
                String url = imgResultUrls[i];
                View view = RelativeLayout.inflate(this, R.layout.item_compphoto, null);
                ImgUtils.loadImage(this, (ImageView) view.findViewById(R.id.iv_photo), url, R.drawable.im_loading, R.drawable.im_failed);
                view.setTag(i);
                view.setOnClickListener(clkResultGotoPhotoView);
                ll_photos.addView(view);
            }

            showImageLocation(marker,view1);
        } else {
//            view1.findViewById(R.id.hsv_result_photos).setVisibility(View.GONE);
        }

    }

    /**
     * 地图上显示河道
     * drawRiverStartAndEnd
     */
//    private void drawRiver() {
//        //在地图上显示起点和终点
//        BitmapDescriptor bmp_from = BitmapDescriptorFactory.fromResource(R.drawable.river_start);
//        BitmapDescriptor bmp_to = BitmapDescriptorFactory.fromResource(R.drawable.river_end);
////        baiduMap.addOverlay(optionMe);
//        if (riverStart != null && riverEnd != null) {
//            System.out.println("testrc: drawRiver():riverStart != null && riverEnd != null");
//            MarkerOptions optionFrom = new MarkerOptions().position(riverStart).icon(bmp_from);
//            MarkerOptions optionTo = new MarkerOptions().position(riverEnd).icon(bmp_to);
//            baiduMap.addOverlay(optionFrom);
//
//            OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.BLUE).points(riverAllPoint);
//            baiduMap.addOverlay(ooPolyline);
//            Marker marker_End = (Marker) baiduMap.addOverlay(optionTo);
//        }else {
//            showToast("暂时未录入河道信息。");
//        }
//    }
    //湖泊应巡点
    private void drawLake() {
        if (lakeStart != null && lakeEnd != null) {
            System.out.println("testrc: drawRiver():riverStart != null && riverEnd != null");

            BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.my_blue5px);

            for(int i=0;i<lakeAllPoint.size();i++){
                OverlayOptions option1 =  new MarkerOptions()
                        .position(new LatLng(lakeAllPoint.get(i).latitude, lakeAllPoint.get(i).longitude))
                        .icon(bdA);
                baiduMap.addOverlay(option1);
            }
        }else {
            showToast("暂时未录入湖泊信息。");
        }
    }

    String[] imgResultUrls = null;
    private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(LakeChiefTrackViewActivity.this, PhotoViewActivity.class);
                intent.putExtra("URLS", imgResultUrls);
                intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                startActivity(intent);
            }
        }
    };
}
