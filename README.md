# VideoToGif
 目前效率偏低，在找寻新的方案！
How to use?
--------
* 1.设置输出环境(gif文件保的路径)<br>
* 2.实例化GifTransform<br>
* 3.调用内部方法即可<br>

```
 transform = new GifTransform(file.getAbsolutePath());
        transform.setQuality(80);
        transform.setScaleX(1);
        transform.setScaleY(2);
        transform.setOnTransformProgressListener(new OnTransformProgressListener()
        {
            @Override
            public void onProgress(int current, int total)
            {
                Log.e(TAG, "current : " + current);
            }
        });
  final boolean result = transform.transformFromVideo(afd,0,5 * 10000,2000);

```

参考 ： https://github.com/boybeak/GifMaker

相关方法以及参数:
------
  方法  | 含义 
  ------------- | ------------- 
 transformFromVideo  | 从video转换 
 setQuality  | 设置gif质量 
 setScaleX/setScaleY  | 宽高缩放比例 
 
   参数  | 含义 
  ------------- | ------------- 
 startMillSecond  | 从视频的哪个时间点开始 
 endMillSecond  | 结束时间
 periodMillSecond  | 每隔此周期时间转换一次 


效果
-------
![result](https://github.com/Wudelin/VideoToGif/blob/master/pictrue/1571906621942.gif "效果图")  


Download
--------

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
dependencies {
	implementation 'com.github.Wudelin:VideoToGif:1.0.0'
}
```


