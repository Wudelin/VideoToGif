package com.wdl.gifmaker;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;

import com.wdl.gifmaker.encoder.AnimatedGifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Create by: wdl at 2019/10/24 14:11
 * video转GIF 图片转gif
 */
@SuppressWarnings("unused")
public class GifTransform
{
    /**
     * 缩放程度
     */
    private static final int DEFAULT_SCALE = 1;
    private int scaleX = DEFAULT_SCALE;
    private int scaleY = DEFAULT_SCALE;
    /**
     * GIF质量
     */
    private static final int DEFAULT_QUALITY = 100;
    private int quality = DEFAULT_QUALITY;
    /**
     * GIF输出路径
     */
    private String outputPath;
    /**
     * 进度监听回调
     */
    private OnTransformProgressListener onTransformProgressListener;

    public GifTransform(String outputPath)
    {
        this(outputPath, DEFAULT_SCALE, DEFAULT_SCALE);
    }

    public GifTransform(String outputPath, int scaleX, int scaleY)
    {
        if (scaleX < 1 || scaleY < 1)
        {
            throw new IllegalArgumentException("The zoom level needs to be greater than 1!");
        }
        if (TextUtils.isEmpty(outputPath))
        {
            throw new IllegalArgumentException("Save path cannot be empty!");
        }
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.outputPath = outputPath;
    }

    public GifTransform(String outputPath, int scaleX, int scaleY, OnTransformProgressListener onTransformProgressListener)
    {
        if (scaleX < 1 || scaleY < 1)
        {
            throw new IllegalArgumentException("The zoom level needs to be greater than 1!");
        }
        if (TextUtils.isEmpty(outputPath))
        {
            throw new IllegalArgumentException("Save path cannot be empty!");
        }
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.outputPath = outputPath;
        this.onTransformProgressListener = onTransformProgressListener;
    }


    /**
     * 从文件转换
     *
     * @param files List<File>
     * @return 是否转换成功
     */
    public boolean transformFromFile(List<File> files)
    {
        if (files == null || files.isEmpty())
        {
            throw new IllegalArgumentException("Source File is empty!");
        }
        final int size = files.size();
        List<String> paths = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            File file = files.get(i);
            if (file.exists() && file.length() > 0)
            {
                paths.add(file.getAbsolutePath());
            }
        }
        return transformFromPath(paths);
    }

    /**
     * 从路径转换
     *
     * @param paths 视频源路径
     * @return 是否转换成功
     */
    public boolean transformFromPath(List<String> paths)
    {
        if (paths == null || paths.isEmpty())
        {
            throw new IllegalArgumentException("Source Path is empty!");
        }
        final int size = paths.size();
        List<Bitmap> bitmaps = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            bitmaps.add(BitmapFactory.decodeFile(paths.get(i)));
        }

        return transform(bitmaps);

    }

    /**
     * 从资源文件转换
     *
     * @param resources   Resources
     * @param drawableIds int
     * @return 是否转换成功
     */
    public boolean transformFromResource(Resources resources, @DrawableRes int[] drawableIds)
    {
        if (drawableIds == null || drawableIds.length == 0)
        {
            throw new IllegalArgumentException("DrawableIds is empty!");
        }

        final int size = drawableIds.length;
        List<Bitmap> bitmaps = new ArrayList<>(size);
        for (int drawableId : drawableIds)
        {
            bitmaps.add(BitmapFactory.decodeResource(resources, drawableId));
        }

        return transform(bitmaps);
    }


    /**
     * 转换过程
     *
     * @param bitmaps List<Bitmap>
     * @return 是否转换成功
     */
    public boolean transform(List<Bitmap> bitmaps)
    {
        if (bitmaps == null || bitmaps.isEmpty())
        {
            throw new IllegalArgumentException("Bitmaps is empty!");
        }

        //-----------------设置参数----------------------
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //----------------- encoder.start(bos) ----------------------
        encoder.start(bos);
        encoder.setRepeat(0);
        encoder.setQuality(quality);

        final int size = bitmaps.size();
        Bitmap sourceBmp;
        Bitmap resultBbm;
        for (int i = 0; i < size; i++)
        {
            sourceBmp = bitmaps.get(i);
            if (sourceBmp == null)
            {
                continue;
            }

            // 最终的bmp
            resultBbm = ThumbnailUtils.extractThumbnail(sourceBmp, sourceBmp.getWidth() / scaleX,
                    sourceBmp.getHeight() / scaleY, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

            try
            {
                encoder.addFrame(resultBbm);
                if (onTransformProgressListener != null)
                {
                    onTransformProgressListener.onProgress(i, size);
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                System.gc();
                break;
            } finally
            {
                if (!sourceBmp.isRecycled())
                {
                    sourceBmp.recycle();
                }
                if (!resultBbm.isRecycled())
                {
                    resultBbm.recycle();
                }
            }
        }
        //----------------- encoder.finish() ----------------------
        encoder.finish();
        bitmaps.clear();

        byte[] data = bos.toByteArray();
        File saveFile = new File(outputPath);
        // 判断父路径是否存在
        if (!saveFile.getParentFile().exists())
        {
            saveFile.getParentFile().mkdirs();
        }

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(saveFile);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return saveFile.exists() && saveFile.length() > 0;
    }

    /**
     * 从Video转换
     *
     * @param videoPath        源视频路径
     * @param startMillSecond  开始转换的起始时间
     * @param endMillSecond    转换的结束时间
     * @param periodMillSecond 转换周期
     * @return 转换是否完成
     */
    public boolean transformFromVideo(String videoPath, long startMillSecond, long endMillSecond, long periodMillSecond)
    {
        if (TextUtils.isEmpty(videoPath))
        {
            throw new IllegalArgumentException("VideoPath is empty!");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try
        {
            retriever.setDataSource(videoPath);
        } catch (Exception e)
        {
            e.printStackTrace();
            FileInputStream inputStream;
            try
            {
                inputStream = new FileInputStream(new File(videoPath).getAbsolutePath());
                retriever.setDataSource(inputStream.getFD());
            } catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        return transformWithMediaMetadataRetriever(retriever, startMillSecond, endMillSecond, periodMillSecond);
    }

    /**
     * @param uri              Uri
     * @param startMillSecond  开始转换的起始时间
     * @param endMillSecond    转换的结束时间
     * @param periodMillSecond 转换周期
     * @return 转换是否完成
     */
    public boolean transformFromVideo(Uri uri, long startMillSecond, long endMillSecond, long periodMillSecond)
    {
        if (uri == null || TextUtils.isEmpty(uri.getPath()))
        {
            throw new IllegalArgumentException("uri is empty!");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try
        {
            retriever.setDataSource(uri.getPath());
        } catch (Exception e)
        {
            e.printStackTrace();
            FileInputStream inputStream;
            try
            {
                inputStream = new FileInputStream(new File(uri.getPath()).getAbsolutePath());
                retriever.setDataSource(inputStream.getFD());
            } catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }

        }
        return transformWithMediaMetadataRetriever(retriever, startMillSecond, endMillSecond, periodMillSecond);
    }

    /**
     * 从资源文件的Video转换
     *
     * @param afd              AssetFileDescriptor
     * @param startMillSecond  开始转换的起始时间
     * @param endMillSecond    转换的结束时间
     * @param periodMillSecond 转换周期
     * @return 转换是否完成
     */
    public boolean transformFromVideo(AssetFileDescriptor afd, long startMillSecond, long endMillSecond, long periodMillSecond)
    {
        if (afd == null)
        {
            throw new IllegalArgumentException("AssetFileDescriptor is empty!");
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try
        {
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return transformWithMediaMetadataRetriever(retriever, startMillSecond, endMillSecond, periodMillSecond);
    }


    /**
     * 从Video转换-真是转换
     *
     * @param retriever        MediaMetadataRetriever
     * @param startMillSecond  开始转换的起始时间
     * @param endMillSecond    转换的结束时间
     * @param periodMillSecond 转换周期
     * @return 转换是否完成
     */
    private boolean transformWithMediaMetadataRetriever(MediaMetadataRetriever retriever, long startMillSecond, long endMillSecond, long periodMillSecond)
    {
        if (startMillSecond < 0 || endMillSecond <= 0 || startMillSecond >= endMillSecond || periodMillSecond <= 0)
        {
            throw new IllegalArgumentException("startMillSecond and endMillSecond must > 0 , startMillSecond >= endMillSecond");
        }
        try
        {
            List<Bitmap> bitmaps = new ArrayList<>();

            // 获取视频时长
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = Long.parseLong(durationStr);
            // 获取最短时间
            long endTime = Math.min(duration, endMillSecond);

            // 开始到结束时间循环遍历 periodMillSecond为一个周期
            for (long mill = startMillSecond; mill < endTime; mill += periodMillSecond)
            {
                bitmaps.add(retriever.getFrameAtTime(mill*1000, MediaMetadataRetriever.OPTION_CLOSEST));
            }
            retriever.release();
            return transform(bitmaps);
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }


    public void setQuality(int quality)
    {
        this.quality = quality;
    }

    public void setScaleX(int scaleX)
    {
        this.scaleX = scaleX;
    }


    public void setScaleY(int scaleY)
    {
        this.scaleY = scaleY;
    }

    public void setOnTransformProgressListener(OnTransformProgressListener onTransformProgressListener)
    {
        this.onTransformProgressListener = onTransformProgressListener;
    }
}
