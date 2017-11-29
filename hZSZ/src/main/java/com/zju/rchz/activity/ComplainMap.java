package com.zju.rchz.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zju.rchz.Tags;
import com.zju.rchz.model.Imag;
import com.zju.rchz.model.River;
import com.zju.rchz.model.RiverPosition;
import com.zju.rchz.model.RiverPositionRes;
import com.zju.rchz.net.Callback;
import com.zju.rchz.utils.ImgUtils;
import com.zju.rchz.utils.ParamUtils;
import com.zju.rchz.utils.StrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ZJTLM4600l on 2017/9/5.
 */

public class ComplainMap extends BaseActivity {
    River river = null;
    private BaiduMap baiduMap = null;
    protected Location location;
//    private List<LatLng> points;
    private LatLng complainLocation;
//    private LatLng end;
    private LatLng me;
    private InfoWindow mInfoWindow;
    String complainPicPath;

    private static String imgLnglist;
    private static String imgLatlist;

    private static String[] imgLnglistArray;
    private static String[] imgLatlistArray;
    private static String[] picPathArray;

    private LatLng[] imagesLocation;

    BitmapDescriptor bmp_me = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_me);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riverposition);
        initHead(R.drawable.ic_head_back, 0);
        setTitle("投诉方位");
//        river = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_RIVER), River.class);

        me = new LatLng(getLatitude(), getLongitude());

            MapView mv_section = (MapView) findViewById(R.id.mv_position);
            baiduMap = mv_section.getMap();
            baiduMap.getUiSettings().setOverlookingGesturesEnabled(true);
            baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {

                @Override
                public void onMapLoaded() {
//					adjustViewPort();

                }
            });

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        complainPicPath=bundle.getString("picPath");
        double complainLongitude=bundle.getDouble("longitude");
        double complainLatitude=bundle.getDouble("latitude");
        imgLatlist=bundle.getString("imgLatlist");
        imgLnglist=bundle.getString("imgLnglist");

        complainLocation=new LatLng(complainLatitude,complainLongitude);

        if (imgLatlist!=""&&imgLnglist!=""&&complainPicPath!=""){
            imgLatlistArray = imgLatlist.split(",");
            imgLnglistArray = imgLnglist.split(",");
            picPathArray = complainPicPath.split(";");

            if (imgLnglistArray[0]!=""&&imgLatlistArray[0]!=""){
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
                imagesLocation=new LatLng[0];
//                Toast.makeText(this,imgLatlistArray.length+"IFelse"+imgLnglistArray.length,Toast.LENGTH_LONG).show();
            }
        }else {
            imgLatlistArray=new String[0];
            imgLnglistArray=new String[0];
            picPathArray=new String[0];
            imagesLocation=new LatLng[0];
//            Toast.makeText(this,imgLatlistArray.length+"ELSE"+imgLnglistArray.length,Toast.LENGTH_LONG).show();
        }

        drawLocation();

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getIcon()==bmp_me){
                    showLocation(marker);
                }else {
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
                        case "4":{
                            initResultPhotoView(complainPicPath,marker);
                            break;
                        }
                        default:{
                            break;
                        }
                    }
                }
                return false;
            }
        });
    }



    /**
     * 1.在地图上显示点
     * 2.将各点连起来
     */
    private void drawLocation() {

        baiduMap.clear();

        //在地图上显示起点和终点
        BitmapDescriptor bmp_image = BitmapDescriptorFactory.fromResource(R.drawable.complain_map);

        MarkerOptions optionMe = new MarkerOptions().position(me).icon(bmp_me);
        baiduMap.addOverlay(optionMe);
        if (complainLocation != null||(complainLocation.longitude==0&&complainLocation.latitude==0)) {

//            MarkerOptions optionFrom = new MarkerOptions().position(complainLocation).icon(bmp_image);
////            MarkerOptions optionTo = new MarkerOptions().position(end).icon(bmp_to);
//
//            Marker marker_Start = (Marker)baiduMap.addOverlay(optionFrom);
////            Marker marker_End = (Marker) baiduMap.addOverlay(optionTo);

            MarkerOptions[] optionsesImage=new MarkerOptions[imagesLocation.length];
            Marker[] markers=new Marker[imagesLocation.length];
            for (int i=0;i<imagesLocation.length;i++){
                optionsesImage[i]=new MarkerOptions().position(imagesLocation[i]).icon(bmp_image);
                markers[i]=(Marker) baiduMap.addOverlay(optionsesImage[i]);
                markers[i].setTitle(i+"");
            }

            float lat;
            float lng;
            if (imagesLocation.length==0){
                lat = (float) (complainLocation.latitude );
                lng = (float) (complainLocation.longitude) ;

                MarkerOptions optionFrom = new MarkerOptions().position(complainLocation).icon(bmp_image);
                Marker marker_Start = (Marker)baiduMap.addOverlay(optionFrom);
                marker_Start.setTitle(4+"");
            }else {
                lat = (float) (imagesLocation[0].latitude );
                lng = (float) (imagesLocation[0].longitude) ;
            }

            baiduMap.setMyLocationData(new MyLocationData.Builder().latitude(lat).longitude(lng).build());

            //target：设置地图中心点；zoom:设置缩放级别
            MapStatus status = new MapStatus.Builder().target(new LatLng(lat, lng)).zoom(13).build();
            //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));

        }else {
            baiduMap.setMyLocationData(new MyLocationData.Builder()
                    .latitude(getLatitude()).longitude(getLongitude()).build());
            //target：设置地图中心点；zoom:设置缩放级别
            MapStatus status = new MapStatus.Builder().target(new LatLng(getLatitude(), getLongitude())).zoom(13).build();
            //setMapStatus:改变地图的状态；MapStatusUpdateFactory:生成地图状态将要发生的变化
            baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(status));
        }

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

    String[] imgResultUrls = null;
    private View.OnClickListener clkResultGotoPhotoView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                Intent intent = new Intent(ComplainMap.this, PhotoViewActivity.class);
                intent.putExtra("URLS", imgResultUrls);
                intent.putExtra("CUR", ((Integer) v.getTag()).intValue());
                startActivity(intent);
            }
        }
    };

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

}
