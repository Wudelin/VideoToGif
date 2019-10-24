package com.wdl.videotogif;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wdl.gifmaker.GifTransform;
import com.wdl.gifmaker.OnTransformProgressListener;

import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{

    private Button btnTransform;
    private ImageView ivResult;
    private GifTransform transform;
    private static final String TAG = "MainActivity";
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 输出环境
        final String gifPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "Emoji";
        File dir = new File(gifPath);
        if (!dir.exists())
        {
            dir.mkdir();
        }
        file = new File(dir.getAbsolutePath() + File.separator + new Date().getTime() + ".gif");


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
        initView();
    }

    private void initView()
    {
        ivResult = findViewById(R.id.iv_image);
        btnTransform = findViewById(R.id.btn_transform);
        btnTransform.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
//                        final boolean result = transform.
//                                transformFromVideo("/storage/emulated/0/Recorder/1571903243391.mp4",
//                                        0,
//                                        5 * 1000,
//                                        200);
                        // 转换资源文件
                        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.test);
                        final boolean result = transform.
                                transformFromVideo(afd,
                                        0,
                                        5 * 10000,
                                        2000);
                        if (result)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Glide.with(MainActivity.this).asGif().load(file.getAbsolutePath()).into(ivResult);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

    }
}
