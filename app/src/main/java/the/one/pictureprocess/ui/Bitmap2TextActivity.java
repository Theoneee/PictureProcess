package the.one.pictureprocess.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import the.one.pictureprocess.R;
import the.one.pictureprocess.utils.BitmapUtil;
import uk.co.senab.photoview.PhotoView;

import static the.one.pictureprocess.utils.SDUtils.assets2SD;


/**
 * @author the one
 * @TODO 识别文字
 * @time 2018-4-6
 * @remark 主要是往文件里写入assets的识别库到手机存储
 */
public class Bitmap2TextActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String PHOTO_PATH = "photo_path";

    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    private static String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    /**
     * 权限请求值
     */
    private static final int PERMISSION_REQUEST_CODE = 0;

    @BindView(R.id.et_result)
    TextView etResult;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.photo_view)
    PhotoView photoView;
    private QMUITipDialog tipDialog;
    private String result;
    private Bitmap btmp;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_bitmap_text);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
        //Android6.0之前安装时就能复制，6.0之后要先请求权限，所以6.0以上的这个方法无用。
        assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);

        String uri = getIntent().getStringExtra(PHOTO_PATH);
        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在识别")
                .create();
        try {
            BitmapUtil.decodeSampledBitmapFromPath(uri, new BitmapUtil.OnBitmapCreateListner() {
                @Override
               public void onCreateBitmap(Bitmap bitmap) {
                    btmp = bitmap;
                    recognition();
                    tipDialog.show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startThisActivity(Activity activity, String path) {
        Intent intent = new Intent(activity, Bitmap2TextActivity.class);
        intent.putExtra(PHOTO_PATH, path);
        activity.startActivity(intent);

    }

    /**
     * 识别图像
     */
    private void recognition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!checkTraineddataExists()) {
                    assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                }
                final long startTime = System.currentTimeMillis();
                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
                tessBaseAPI.setImage(btmp);
                result = tessBaseAPI.getUTF8Text();
                final long finishTime = System.currentTimeMillis();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etResult.setText(result);
                        tvTime.setText((finishTime - startTime) + "ms");
                        tvTime.setVisibility(View.VISIBLE);
                    }
                });
                handler.sendEmptyMessage(1);
                tessBaseAPI.end();
            }
        }).start();
    }

    /**
     * 检查是否存在识别库
     *
     * @return
     */
    public boolean checkTraineddataExists() {
        File file = new File(LANGUAGE_PATH);
        return file.exists();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //执行逻辑
            tipDialog.dismiss();
            photoView.setImageBitmap(btmp);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        photoView.setImageResource(0);
        System.gc();
    }

    /**
     * 请求到权限后在这里复制识别库
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: " + grantResults[0]);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: copy");
                    assets2SD(getApplicationContext(), LANGUAGE_PATH, DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.iv_close)
    public void onViewClicked() {
        finish();
    }

}
