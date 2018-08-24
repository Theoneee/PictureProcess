package the.one.pictureprocess.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by Administrator on 2018/4/3 0003.
 */

public class GlideUtil {

    public static RequestOptions options = new RequestOptions()
//            .placeholder(R.drawable.loading)
            .diskCacheStrategy(DiskCacheStrategy.ALL);
    public static void LoadImage(Context context,
                            Object url,
                            ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

}
