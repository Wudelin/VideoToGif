# VideoToGif

How to use?
--------
1.设置输出环境(gif文件保的路径)
2.实例化GifTransform
3.调用内部方法即可

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


