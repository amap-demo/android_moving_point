# android_moving_point
移动点的轨迹动态展示
## 前述 ##
- [高德官网申请Key](http://lbs.amap.com/dev/#/).
- 阅读[地图参考手册](http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html).
- 工程基于Android 地图 SDK实现

## 功能描述 ##
基于3D地图SDK进行封装，实现了移动点轨迹展示。

## 效果图如下 ##
![picture](https://raw.githubusercontent.com/amap-demo/android_moving_point/master/apk/screen.png)

## 扫一扫安装 ##
![Screenshot]( https://raw.githubusercontent.com/amap-demo/android_moving_point/master/apk/1525782811.png) 

## 使用方法 ##
### 1:配置搭建AndroidSDK工程 ###
- [Android Studio工程搭建方法](http://lbs.amap.com/api/android-sdk/guide/creat-project/android-studio-creat-project/#add-jars).
- [通过maven库引入SDK方法](http://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project#gradle_sdk).

### 2:实现方法 ###
构造SmoothMoveMarker对象
``` 
SmoothMoveMarker smoothMarker = new SmoothMoveMarker(mAMap);
// 设置滑动的图标
smoothMarker.setDescriptor(BitmapDescriptorFactory.fromResource(R.drawable.marker));

// 设置滑动的轨迹左边点
smoothMarker.setPoints(pointList);
// 设置滑动的总时间
smoothMarker.setTotalDuration(time);
// 开始滑动
smoothMarker.startSmoothMove();
```
设置SmoothMoveMarker移动监听，在回调move中绘制轨迹
```
moveMarker.setMoveListener(
                new SmoothMoveMarker.MoveListener() {
                    @Override
                    public void move(final double distance) {
                        LatLng center = moveMarker.getPosition();
                        int index = moveMarker.getIndex();
                        float markerAngle = moveMarker.getMarker().getRotateAngle();

                        //判断是否到拍照点
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
                            //需要旋转地图的话，在此改变地图角度
//                            amapAngle += 10;
                            CameraPosition cameraPosition = new CameraPosition(center, 18, 60, amapAngle);
                            amap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2200,null);
                        }else if ((index%20)==10 ){
                            CameraPosition cameraPosition = new CameraPosition(center, 18, 60, amapAngle);
                            amap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),2200,null);
                        }

                        /**
                         * 画线
                         */
                        mList.add(center);
                        mPolyline.setPoints(mList);

                        /**
                         * 判断是否到终点
                         */
                        if(distance == 0){
                            mStartButton.setClickable(true);
                            amap.animateCamera(CameraUpdateFactory.newLatLngBounds(getBounds(mOriginList), 200));
                        }
                    }
                });
  ```
