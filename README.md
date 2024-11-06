# CarRecording

### 一个行车记录仪的app


基本功能：

* 自动录制视频，视频流中带水印、天气、车速、时间等信息
* 每个视频固定时间，到时后自动重新录制新视频
* 手机本地空间有限，采用循环保存方式(存储最新的、删除最老的)，对视频进行管理
* 用户可手动锁定视频，对旧视频文件进行保存，再次循环时不被删除，留作证据


---
### 相关技术：

* mvp架构
* opengl绘制
* 相机cameraV2采集 
* surfaceTextTure二次绘制


---
### 说明

实时的添加水印的相关实现，是和美颜功能原理一样的，都涉及二次绘制。

例如，直播软件的视频流，界面上的礼物，进场信息等。都涉及二次绘制。


## 扩展

本项目视频采集进行二次绘制后，保留了到了文件中。如果实现实时推送到服务器，就是一个直播的主播客户端。最简单的采集视频并推送。还可以添加美颜，水印。

## 截图

##### 横屏录制中
![横屏录制中](https://gitee.com/developergu/car-recording/raw/master/screen_shot/Screenshot_2023-03-16-18-13-25-12.png "横屏录制中")

##### 横屏预览
![横屏预览中](https://gitee.com/developergu/car-recording/raw/master/screen_shot/Screenshot_2023-03-16-18-17-57-30.png "横屏预览中")

##### 视频操作界面
![视频操作界面](https://gitee.com/developergu/car-recording/raw/master/screen_shot/Screenshot_2023-03-16-18-13-48-71.png "视频操作界面")

##### 竖屏
![竖屏](https://gitee.com/developergu/car-recording/raw/master/screen_shot/Screenshot_2023-03-16-18-14-39-10.png "竖屏")