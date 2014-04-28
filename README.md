Android-KCVideoView
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
<!--sherlock actionbar 才能动态隐藏标题栏-->
<dependency>
    <groupId>com.actionbarsherlock</groupId>
    <artifactId>actionbarsherlock</artifactId>
    <version>4.4.0</version>
    <type>apklib</type>
</dependency>
```

###可选依赖项
```
<!--roboguice 和 sherlock的结合，apklib未使用,sample里面使用了-->
<dependency>
    <groupId>com.github.rtyley</groupId>
    <artifactId>roboguice-sherlock</artifactId>
    <version>1.5</version>
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
  <version>0.1.5</version>
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
全屏时速度有些慢，在想办法解决。

###回到Activity，恢复之前播放状态
需要自行改写onPause（通过getCurrentPosition获取当前播放点） onRestart(通过seekTo 设置当前播放点)来实现

###bug fix

####bug 1修复

####for Bug2
由于去除title需要在setContentView之前执行
如果需要请在onCreate中setContentView之前，加入：
requestWindowFeature(Window.FEATURE_NO_TITLE);
或者直接使用SherlockActivity
