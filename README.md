android-video-view
==================
本项目主要是建立一个独立视屏播放组件KCVideoView

本项目使用Intellij IDEA 13.02开发， 为maven项目，其他开发软件未导入实测

kcvideoview 为 apklib
kcvideoview-samples 为演示例子

###依赖项以及功能如下：
```
<!--http 请求库-->
<dependency>
  <groupId>com.github.kevinsawicki</groupId>
  <artifactId>http-request</artifactId>
  <version>4.2</version>
</dependency>
<!--roboguice，本项目主要用了他的异步任务，获取json-->
<dependency>
  <groupId>org.roboguice</groupId>
  <artifactId>roboguice</artifactId>
  <version>2.0</version>
</dependency>
<!--gson 不解释-->
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.2.4</version>
</dependency>
```

###组件需要的权限：
```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.WAKE_LOCK"></uses-permission>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-permission android:name="android.permission.READ_PHONE_STATE"></uses-permission>
```

###如何引用此组件：
已经独立成maven项目，mvn install之后可以在项目maven添加以下依赖引用：

```
<dependency>
  <groupId>com.github.destinyd</groupId>
  <artifactId>kcvideoview</artifactId>
  <version>0.1.0</version>
  <type>apklib</type>
</dependency>
```

******
使用到KCVideoView的Activity需要在AndroidManifest.xml里面加入如下设置
```
android:configChanges="orientation|keyboardHidden|screenSize"
```
否则屏幕旋转会导致重绘，且全屏按钮使用会不正常

****
由于去除title需要在setContentView之前执行，所以在全屏功能中不能去除，如有需要请自行设定。

全屏时速度有些慢，在想办法解决。
