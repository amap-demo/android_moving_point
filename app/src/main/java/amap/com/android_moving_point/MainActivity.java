package amap.com.android_moving_point;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements AMap.OnMapLoadedListener, View.OnClickListener {
    public int time = 0;
    private MapView mMapView = null;
    private AMap amap = null;
    private Button mStartButton;
    private SmoothMoveMarker moveMarker;
    private List<LatLng> mOriginList = new ArrayList<LatLng>();
    private List<LatLng> mList = new ArrayList<LatLng>();
    private List<LatLng> mPoint = new ArrayList<LatLng>();
    private List<Integer> mPointIndex = new ArrayList<Integer>(){{
        add(1);add(55);add(150);
        add(200);add(300);add(400);add(499); }};
    private Polyline mOriginPolyline, mPolyline;
    private float amapAngle = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        init();

    }
    private void init() {
        if (amap == null){
            amap = mMapView.getMap();
        }
        amap.setOnMapLoadedListener(this);
        amap.setMapType(AMap.MAP_TYPE_SATELLITE);
        amap.showMapText(false);
        mStartButton= (Button) findViewById(R.id.move_start_button);
        mStartButton.setOnClickListener(this);
    }

    @Override
    public void onMapLoaded() {
        addLocpath();
        initMoveMarker();
        mPolyline = amap.addPolyline(new PolylineOptions().color(Color.YELLOW));
    }


    //在地图上添加本地轨迹数据，并处理
    private void addLocpath() {
        mOriginList = TraceAsset.parseLocationsData(this.getAssets(),
                "traceRecord" + File.separator + "356022065185856.csv");

        //初始化时间，每秒钟几个点
        time = mOriginList.size()/5;

        if (mOriginList != null && mOriginList.size()>0) {
//            mOriginPolyline = amap.addPolyline(new PolylineOptions().addAll(mOriginList).color(Color.GRAY));
//            amap.moveCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mOriginList), 200));
            amap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOriginList.get(0),18));
        }
        for(int i = 0; i <mOriginList.size(); i++){
            if (mPointIndex.contains(i)){
                Marker marker = amap.addMarker(new MarkerOptions().position(mOriginList.get(i)));
                marker.setObject(i);
            }
        }
    }




    private LatLngBounds getBounds(List<LatLng> pointlist) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        if (pointlist == null) {
            return b.build();
        }
        for (int i = 0; i < pointlist.size(); i++) {
            b.include(pointlist.get(i));
        }
        return b.build();

    }

    @Override
    public void onClick(View v) {
        mStartButton.setClickable(false);
        mList.clear();

        moveMarker.startSmoothMove();

    }

    private void initMoveMarker(){
        // 获取轨迹坐标点
        List<LatLng> points = mOriginList;


        moveMarker = new SmoothMoveMarker(amap);
        // 设置滑动的图标
        moveMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.marker));

        /*
        //当移动Marker的当前位置不在轨迹起点，先从当前位置移动到轨迹上，再开始平滑移动
        // LatLng drivePoint = points.get(0);//设置小车当前位置，可以是任意点，这里直接设置为轨迹起点
        LatLng drivePoint = new LatLng(39.980521,116.351905);//设置小车当前位置，可以是任意点
        Pair<Integer, LatLng> pair = PointsUtil.calShortestDistancePoint(points, drivePoint);
        points.set(pair.first, drivePoint);
        List<LatLng> subList = points.subList(pair.first, points.size());
        // 设置滑动的轨迹左边点
        smoothMarker.setPoints(subList);*/

        moveMarker.setPoints(points);//设置平滑移动的轨迹list
        moveMarker.setTotalDuration(time);//设置平滑移动的总时间

        moveMarker.setMoveListener(
                new SmoothMoveMarker.MoveListener() {
                    @Override
                    public void move(final double distance) {
                        LatLng center = moveMarker.getPosition();
                        int index = moveMarker.getIndex();
                        float markerAngle = moveMarker.getMarker().getRotateAngle();



                        //todo 判断是否到拍照点

                        List<Marker> markerList = amap.getMapScreenMarkers();
                        if(mPointIndex.contains(index)){
                            moveMarker.stopMove();
                            for(Marker marker :markerList) {
                                if (marker.getObject()!= null){
                                    int markerindex = (int)marker.getObject();
                                    if (markerindex == index ){
                                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                        startJumpAnimation(marker);
                                    }
                                }
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            moveMarker.startSmoothMove();
                        }

                        //todo 地图移动旋转
                        //每10个点移动一次中心点

                        if ((index%20) == 0){
//                            amapAngle += 10;
                            CameraPosition cameraPosition = new CameraPosition(center, 18, 60, amapAngle);
                            amap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);
                        }else if ((index%20)==10 ){
                            CameraPosition cameraPosition = new CameraPosition(center, 18, 60, amapAngle);
                            amap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2000,null);
                        }


                        //todo 画线
                        mList.add(center);
                        mPolyline.setPoints(mList);

                        /**
                         * 判断是否到终点
                         */
                        if(distance == 0){
                            mStartButton.setClickable(true);
                        }
                    }
                });
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation(Marker marker) {

        if (marker != null ) {
            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = marker.getPosition();
            Point point =  amap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this,125);
            LatLng target = amap.getProjection()
                    .fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if(input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f)*(1.5f - input)));
                    }
                }
            });
            //整个移动所需要的时间
            animation.setDuration(600);
            //设置动画
            marker.setAnimation(animation);
            //开始动画
            marker.startAnimation();

        } else {
            Log.e("amap","screenMarker is null");
        }
    }
    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
