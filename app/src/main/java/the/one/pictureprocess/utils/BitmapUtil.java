package the.one.pictureprocess.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapUtil {

    public interface OnBitmapCreateListner {
        void onCreateBitmap(Bitmap bitmap);
    }

    public static void decodeSampledBitmapFromResource(String path, Resources resource, int resId, OnBitmapCreateListner onBitmapCreateListner) throws FileNotFoundException {
        // 第一次加载时 将inJustDecodeBounds设置为true 表示不真正加载图片到内存
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resource, resId, options);
        // 根据目标宽和高 以及当前图片的大小 计算出压缩比率
        options.inSampleSize = calculateInSampleSize(options, 500, 600);
        LogUtil.showLog(" inSampleSize = " + calculateInSampleSize(options, 500, 600));
        // 将inJustDecodeBounds设置为false 真正加载图片 然后根据压缩比率压缩图片 再去解码
        options.inJustDecodeBounds = false;
        InputStream is2 = new FileInputStream(new File(path));
        Bitmap bitmap = BitmapFactory.decodeStream(is2, null, options);
        if (bitmap != null)
            onBitmapCreateListner.onCreateBitmap(bitmap);
    }

    public static void decodeSampledBitmapFromPath(String path, OnBitmapCreateListner onBitmapCreateListner) throws FileNotFoundException {
        InputStream is = new FileInputStream(new File(path));
        // 第一次加载时 将inJustDecodeBounds设置为true 表示不真正加载图片到内存
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        // 根据目标宽和高 以及当前图片的大小 计算出压缩比率
        options.inSampleSize = calculateInSampleSize(options, 500, 600);
        LogUtil.showLog(" inSampleSize = " + calculateInSampleSize(options, 500, 600));
        // 将inJustDecodeBounds设置为false 真正加载图片 然后根据压缩比率压缩图片 再去解码
        options.inJustDecodeBounds = false;
        InputStream is2 = new FileInputStream(new File(path));
        Bitmap bitmap = BitmapFactory.decodeStream(is2, null, options);
        if (bitmap != null)
            onBitmapCreateListner.onCreateBitmap(bitmap);
    }

    // 计算压缩比率 android官方提供的算法
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//         Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 将当前宽和高 分别减小一半
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


}
