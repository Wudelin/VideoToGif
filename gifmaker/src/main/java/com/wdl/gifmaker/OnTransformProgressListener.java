package com.wdl.gifmaker;

/**
 * Create by: wdl at 2019/10/24 14:13
 * 转换进度回调
 */
@SuppressWarnings("unused")
public interface OnTransformProgressListener
{
    /**
     * @param current 当前进度
     * @param total   总需转换
     */
    void onProgress(int current, int total);
}
