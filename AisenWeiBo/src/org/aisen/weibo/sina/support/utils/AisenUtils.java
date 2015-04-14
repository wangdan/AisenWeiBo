package org.aisen.weibo.sina.support.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.m.common.context.GlobalContext;
import com.m.common.setting.SettingUtility;
import com.m.common.utils.FileUtils;
import com.m.common.utils.Logger;
import com.m.common.utils.SystemUtils;
import com.m.component.bitmaploader.core.BitmapDecoder;

import org.aisen.weibo.sina.base.AppSettings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by wangdan on 15/4/12.
 */
public class AisenUtils {

    public static File getUploadFile(File source) {
        if (source.getName().toLowerCase().endsWith(".gif")) {
            Logger.w("上传图片是GIF图片，上传原图");
            return source;
        }

        File file = null;

        String imagePath = GlobalContext.getInstance().getAppPath() + SettingUtility.getStringSetting("draft") + File.separator;

        int sample = 1;
        int maxSize = 0;

        int type = AppSettings.getUploadSetting();
        // 自动，WIFI时原图，移动网络时高
        if (type == 0) {
            if (SystemUtils.getNetworkType() == SystemUtils.NetWorkType.wifi)
                type = 1;
            else
                type = 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), opts);
        switch (type) {
            // 原图
            case 1:
                Logger.w("原图上传");
                file = source;
                break;
            // 高
            case 2:
                sample = BitmapDecoder.calculateInSampleSize(opts, 1920, 1080);
                Logger.w("高质量上传");
                maxSize = 700 * 1024;
                imagePath = imagePath + "高" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            // 中
            case 3:
                Logger.w("中质量上传");
                sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
                maxSize = 300 * 1024;
                imagePath = imagePath + "中" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            // 低
            case 4:
                Logger.w("低质量上传");
                sample = BitmapDecoder.calculateInSampleSize(opts, 1280, 720);
                maxSize = 100 * 1024;
                imagePath = imagePath + "低" + File.separator + source.getName();
                file = new File(imagePath);
                break;
            default:
                break;
        }

        // 压缩图片
        if (type != 1 && !file.exists()) {
            Logger.w(String.format("压缩图片，原图片 path = %s", source.getAbsolutePath()));
            byte[] imageBytes = FileUtils.readFileToBytes(source);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                out.write(imageBytes);
            } catch (Exception e) {
            }

            Logger.w(String.format("原图片大小%sK", String.valueOf(imageBytes.length / 1024)));
            if (imageBytes.length > maxSize) {
                // 尺寸做压缩
                BitmapFactory.Options options = new BitmapFactory.Options();

                if (sample > 1) {
                    options.inSampleSize = sample;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                    Logger.w(String.format("压缩图片至大小：%d*%d", bitmap.getWidth(), bitmap.getHeight()));
                    out.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    imageBytes = out.toByteArray();
                }

                options.inSampleSize = 1;
                if (imageBytes.length > maxSize) {
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);

                    int quality = 90;
                    out.reset();
                    Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                    while (out.toByteArray().length > maxSize) {
                        out.reset();
                        quality -= 10;
                        Logger.w(String.format("压缩图片至原来的百分之%d大小", quality));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                    }
                }

            }

            try {
                if (!file.getParentFile().exists())
                    file.getParentFile().mkdirs();

                Logger.w(String.format("最终图片大小%sK", String.valueOf(out.toByteArray().length / 1024)));
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(out.toByteArray());
                fo.flush();
                fo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return file;
    }


}
