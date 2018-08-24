package the.one.pictureprocess.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.xw.repo.BubbleSeekBar;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDissolveBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToonFilter;
import the.one.pictureprocess.R;
import the.one.pictureprocess.utils.BitmapUtil;
import the.one.pictureprocess.utils.FileUtils;
import the.one.pictureprocess.utils.Path2UriUtil;
import uk.co.senab.photoview.PhotoView;

import static the.one.pictureprocess.utils.FileUtils.getSDCardPath;

/**
 * @author the one
 * @TODO 加工厂
 * @time 2018-4-4
 * @remark 包含：滤镜、剪裁、调整、转文字。
 * 滤镜使用GPUImageView开源框架，剪裁使用的是UCrop开源框架，调整同为GPUImageView实现。转文字使用的是tess-two。
 * 显示的图片不能直接使用GPUImageView布局。因为此布局不会按照图片大小显示，大的剪小，小的放大。所以直接使用PhotoView显示，
 * 可以拖动视觉效果更好。
 * 滤镜和调整实现：GPUImage 设置完滤镜后返回过滤之后的Bitmap，然后再进行显示，在SeekBar调整时自行设置过滤器的过滤标准（参照Util里GPUImageFilterTools)
 * 剪裁：直接将当前Bitmap保存成一个temp.jpg后将此图片地址传递即可。采用lib方式，方便修改剪裁界面。
 * 转文字：跳转到Bitmap2TextActivity查看。
 * <p>
 * 由于当前滤镜数量比较少，直接写死的几个ImageView用来显示滤镜组，应该使用水平方向上的ListView方便今后添加滤镜。
 * SeekBar直接设置的中间值50，没有采用-50 - 50 区间的显示。在亮度调节时采用此种方式更好的显示。
 */
public class FactoryActivity extends AppCompatActivity implements BubbleSeekBar.OnProgressChangedListener, RadioGroup.OnCheckedChangeListener {
    /**
     * 选择的类型
     */
    public static final String TYPE = "type";
    /**
     * 相机
     */
    public static final String CAMERA = "camera";
    /**
     * 相册
     */
    public static final String ALBUM = "album";

    /**
     * 拍照请求码
     */
    public static int REQUEST_CAMERA = 2;
    /**
     * 相册请求码
     */
    public static int REQUEST_ALBUM = 1;
    /**
     * 选择或者拍照的照片地址
     */
    private String path;
    /**
     * Android 7 图片地址
     */
    private Uri uri7 = null;
    /**
     * 普通图片地址
     */
    private Uri normalUri;
    /**
     * 图片文件
     */
    private File mFilePath;

    @BindView(R.id.function_layout)
    LinearLayout functionLayout;
    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.top_layout)
    LinearLayout topLayout;
    @BindView(R.id.filter_layout)
    LinearLayout filterLayout;
    @BindView(R.id.adjust_layout)
    LinearLayout adjustLayout;
    @BindView(R.id.iv_normal)
    ImageView ivNormal;
    @BindView(R.id.iv_normal2)
    ImageView ivNormal2;
    @BindView(R.id.iv_normal3)
    ImageView ivNormal3;
    @BindView(R.id.iv_normal4)
    ImageView ivNormal4;
    @BindView(R.id.iv_normal5)
    ImageView ivNormal5;
    @BindView(R.id.iv_normal6)
    ImageView ivNormal6;
    @BindView(R.id.iv_normal7)
    ImageView ivNormal7;
    @BindView(R.id.iv_normal8)
    ImageView ivNormal8;
    @BindView(R.id.iv_normal9)
    ImageView ivNormal9;
    @BindView(R.id.iv_normal10)
    ImageView ivNormal10;
    @BindView(R.id.iv_normal11)
    ImageView ivNormal11;
    @BindView(R.id.mSeekBar)
    BubbleSeekBar mSeekBar;
    @BindView(R.id.photo_view)
    PhotoView mPhotoView;
    @BindView(R.id.adjust_1)
    RadioButton adjust1;
    @BindView(R.id.adjust_radio_group)
    RadioGroup adjustRadioGroup;

    /**
     * 使用GPUImage添加过滤器得到过滤之后的图片
     */
    private GPUImage gpuImage;
    /**
     * 底部功能操作栏
     */
    private List<LinearLayout> linearLayouts;
    /**
     * 选择的图片
     */
    private Bitmap bitmap;
    /**
     * 过滤器组
     */
    private List<GPUImageFilter> filters;
    /**
     * 滤镜组图片
     */
    private List<Bitmap> bitmaps;

    /**
     * 以下为过滤器
     */
    private GPUImageContrastFilter gpuImageContrastFilter;
    private GPUImageSepiaFilter gpuImageSepiaFilter;
    private GPUImageGrayscaleFilter gpuImageGrayscaleFilter;
    private GPUImageToonFilter gpuImageToonFilter; // 卡通效果
    private GPUImageSketchFilter gpuImageSketchFilter;// 素描
    private GPUImagePosterizeFilter gpuImageMultiplyBlendFilter;//通常用于创建阴影和深度效果
    private GPUImageDissolveBlendFilter gpuImageDissolveBlendFilter; //晕影，形成黑色圆形边缘，突出中间图像的效果
    private GPUImageEmbossFilter gpuImageEmbossFilter;//浮雕效果，带有点3d的感觉
    private GPUImageHazeFilter gpuImageHazeFilter; //朦胧加暗
    private GPUImageGaussianBlurFilter gpuImageGaussianBlurFilter;//高斯模糊
    private GPUImageBrightnessFilter gpuImageBrightnessFilter;// 亮度
    private GPUImageExposureFilter gpuImageExposureFilter;// 曝光
    private GPUImageContrastFilter getGpuImageContrastFilter;// 对比度
    private GPUImageSaturationFilter gpuImageSaturationFilter;// 饱和度
    private GPUImageOpacityFilter gpuImageOpacityFilter;// 不透明度
    /**
     * 当前选择的滤镜
     */
    private int CURRENT_FILTER;
    /**
     * 当前调整过滤器
     */
    private int CURRENT_ADJUST_FILTER = 0;
    /**
     * 调试时图片
     */
    private Bitmap ADJUST_BITMAP;
    /**
     * 原始图片
     */
    private Bitmap BASE_BITMAP;
    /**
     * 亮度值
     */
    private float BrightnessFilter;
    /**
     * 对比度值
     */
    private float ContrastFilter;
    /**
     * 饱和度值
     */
    private float SaturationFilter;
    /**
     * 模糊值
     */
    private float GaussianBlurFilter;
    /**
     * 暗角值
     */
    private float HazeFilter;
    private View view1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        view1 = LayoutInflater.from(this).inflate(R.layout.activity_factory,null);
        setContentView(view1);
        ButterKnife.bind(this);
        initFilters();
        initAdjustStartValue();
        initView();
//        initImagePicker();
        getData();
    }

    public static void startThisActivity(Activity activity, String path) {
        Intent intent = new Intent(activity, FactoryActivity.class);
        intent.putExtra(TYPE, path);
        activity.startActivity(intent);
    }

    public void getData() {
        String type = getIntent().getStringExtra(TYPE);
        if (type.equals(CAMERA)) {
            showCameraAction();
        } else if (type.equals(ALBUM)) {
            openAlbum();
        }
    }

    /**
     * 打开相机
     */
    private void showCameraAction() {
        PictureSelector.create(FactoryActivity.this)
                .openCamera(PictureMimeType.ofImage())
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    /**
     * 打开相册
     */
    public void openAlbum() {
        PictureSelector.create(FactoryActivity.this)
                .openGallery(PictureMimeType.ofImage())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
//                .theme(R.style.picture_white_style)//主题样式(不设置为默认样式) 也可参考demo values/styles下 例如：R.style.picture.white.style
                .imageSpanCount(3)// 每行显示个数 int
                .selectionMode(PictureConfig.SINGLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .previewImage(true)// 是否可预览图片 true or false
                .isCamera(true)// 是否显示拍照按钮 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/PhotoProcess")// 自定义拍照保存路径,可不填
                .enableCrop(false)// 是否裁剪 true or false
                .compress(true)// 是否压缩 true or false
                .glideOverride(200, 200)// int glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                .isGif(false)// 是否显示gif图片 true or false
                .openClickSound(true)// 是否开启点击声音 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
    }

    /**
     * 初始化滤镜模式
     */
    private void initFilters() {
        filters = new ArrayList<>();
        gpuImageContrastFilter = new GPUImageContrastFilter(2.0f);
        gpuImageSepiaFilter = new GPUImageSepiaFilter(2.4f);
        gpuImageGrayscaleFilter = new GPUImageGrayscaleFilter();
        gpuImageToonFilter = new GPUImageToonFilter();
        gpuImageSketchFilter = new GPUImageSketchFilter();
        gpuImageMultiplyBlendFilter = new GPUImagePosterizeFilter();
        gpuImageDissolveBlendFilter = new GPUImageDissolveBlendFilter();
        gpuImageEmbossFilter = new GPUImageEmbossFilter();
        gpuImageHazeFilter = new GPUImageHazeFilter();
        gpuImageGaussianBlurFilter = new GPUImageGaussianBlurFilter();

        gpuImageBrightnessFilter = new GPUImageBrightnessFilter();
        gpuImageExposureFilter = new GPUImageExposureFilter();
        gpuImageSaturationFilter = new GPUImageSaturationFilter();


        filters.add(gpuImageContrastFilter);
        filters.add(gpuImageSepiaFilter);
        filters.add(gpuImageGrayscaleFilter);
        filters.add(gpuImageToonFilter);
        filters.add(gpuImageSketchFilter);
        filters.add(gpuImageMultiplyBlendFilter);
        filters.add(gpuImageDissolveBlendFilter);
        filters.add(gpuImageEmbossFilter);
        filters.add(gpuImageHazeFilter);
        filters.add(gpuImageGaussianBlurFilter);
    }

    private void initView() {
        // 初始化GPUImage
        gpuImage = new GPUImage(this);


        linearLayouts = new ArrayList<>();
        linearLayouts.add(filterLayout);
        linearLayouts.add(adjustLayout);

        // 初始化保存弹窗
        saveDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("保存中...")
                .create();
        // 初始化加载弹窗
        loadingDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("加载中...")
                .create();
        // 初始化保存成功弹窗
        successDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                .setTipWord("保存成功")
                .create();
        // seekBar设置监听
        mSeekBar.setOnProgressChangedListener(this);
        adjustRadioGroup.setOnCheckedChangeListener(this);
    }

    /**
     * 初始化显示的bitmap
     *
     * @param bitmap
     * @throws IOException
     */
    private void initBitmap(Bitmap bitmap) {
        // 设置当前bitmap到photoView
        mPhotoView.setImageBitmap(bitmap);
        // 设置当前bitmap到gpuImage
        gpuImage.deleteImage();
        gpuImage.setImage(bitmap);
        // 初始化滤镜组图片
        initBitmaps();
        // 显示滤镜组图片
        updateFilterImage();
    }

    /**
     * 初始化滤镜组
     */
    private void initBitmaps() {
        if (bitmaps == null)
            bitmaps = new ArrayList<>();
        else
            bitmaps.clear();
        // 首先添加初始图片
        bitmaps.add(bitmap);
        // 根据滤镜组得到相对应的滤镜之后的图片
        for (int i = 0; i < filters.size(); i++) {
            bitmaps.add(getFilterBitmap(i));
        }
    }

    /**
     * 获取经过滤镜之后的bitmap
     *
     * @param index 滤镜组下标
     * @return
     */
    private Bitmap getFilterBitmap(int index) {
        gpuImage.setFilter(filters.get(index));
        return gpuImage.getBitmapWithFilterApplied();
    }

    /**
     * 获取经过滤镜之后的bitmap
     *
     * @param filter 滤镜
     * @return
     */
    private Bitmap getFilterBitmap(GPUImageFilter filter) {
        gpuImage.setFilter(filter);
        ADJUST_BITMAP = gpuImage.getBitmapWithFilterApplied();
        return ADJUST_BITMAP;
    }

    /**
     * 显示滤镜组图片
     */
    private void updateFilterImage() {
        ivNormal.setImageBitmap(bitmaps.get(0));
        ivNormal2.setImageBitmap(bitmaps.get(1));
        ivNormal3.setImageBitmap(bitmaps.get(2));
        ivNormal4.setImageBitmap(bitmaps.get(3));
        ivNormal5.setImageBitmap(bitmaps.get(4));
        ivNormal6.setImageBitmap(bitmaps.get(5));
        ivNormal7.setImageBitmap(bitmaps.get(6));
        ivNormal8.setImageBitmap(bitmaps.get(7));
        ivNormal9.setImageBitmap(bitmaps.get(8));
        ivNormal10.setImageBitmap(bitmaps.get(9));
        ivNormal11.setImageBitmap(bitmaps.get(10));

    }

    /**
     * 页面销毁时释放内存
     */
    private void releaseImageCache(){
        mPhotoView.setImageResource(0);
        ivNormal.setImageResource(0);
        ivNormal2.setImageResource(0);
        ivNormal3.setImageResource(0);
        ivNormal4.setImageResource(0);
        ivNormal5.setImageResource(0);
        ivNormal6.setImageResource(0);
        ivNormal8.setImageResource(0);
        ivNormal9.setImageResource(0);
        ivNormal7.setImageResource(0);
        ivNormal10.setImageResource(0);
        ivNormal11.setImageResource(0);
    }

    /**
     * 初始化调整的基础数据
     */
    private void initAdjustStartValue() {
        BrightnessFilter = 50;
        ContrastFilter = 50;
        SaturationFilter = 50;
        GaussianBlurFilter = 0;
        HazeFilter = 0;
        CURRENT_ADJUST_FILTER = 0;
        adjust1.setChecked(true);
    }

    @OnClick({R.id.tv_filter, R.id.tv_rotate, R.id.tv_adjust, R.id.tv_text,
            R.id.tv_cancel, R.id.tv_confirm, R.id.filter_cancel, R.id.filter_confirm,
            R.id.iv_normal, R.id.iv_normal2, R.id.iv_normal3, R.id.iv_normal4,
            R.id.iv_normal5, R.id.iv_normal6, R.id.iv_normal7, R.id.iv_normal8,
            R.id.iv_normal9, R.id.iv_normal10, R.id.iv_normal11,
            R.id.adjust_cancel, R.id.adjust_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_filter:
                // 滤镜
                viewSetVisible(1);
                break;
            case R.id.tv_rotate:
                // 剪裁  剪裁前先将当前图片（无论经没经过处理过的图片）保存成为一个中间文件
                saveTempPhoto();
                break;
            case R.id.tv_adjust:
                // 调整
                viewSetVisible(2);
                break;
            case R.id.tv_text:
                // 文字

                Bitmap2TextActivity.startThisActivity(this, path);
                break;
            case R.id.tv_cancel:
                // 取消 关闭
                finish();
                break;
            case R.id.tv_confirm:
                // 保存图片
                showEditSavePhotoNameDialog();
                break;
            case R.id.filter_cancel:
                viewSetVisible(0);
                mPhotoView.setImageBitmap(bitmap);
                break;
            case R.id.filter_confirm:
                // 由于第一张是原始图片，所以这里需要+1
                bitmap = bitmaps.get(CURRENT_FILTER + 1);
                initBitmap(bitmap);
                viewSetVisible(0);
                break;
            case R.id.iv_normal:
                // 原画
                updateFilter(0);
                break;
            case R.id.iv_normal2:
                // 滤镜1
                updateFilter(1);
                break;
            case R.id.iv_normal3:
                // 滤镜2
                updateFilter(2);
                break;
            case R.id.iv_normal4:
                // 滤镜3
                updateFilter(3);
                break;
            case R.id.iv_normal5:
                // 滤镜4
                updateFilter(4);
                break;
            case R.id.iv_normal6:
                // 滤镜5
                updateFilter(5);
                break;
            case R.id.iv_normal7:
                // 滤镜6
                updateFilter(6);
                break;
            case R.id.iv_normal8:
                // 滤镜7
                updateFilter(7);
                break;
            case R.id.iv_normal9:
                // 滤镜1
                updateFilter(8);
                break;
            case R.id.iv_normal10:
                // 滤镜8
                updateFilter(9);
                break;
            case R.id.iv_normal11:
                // 滤镜9
                updateFilter(10);
                break;
            case R.id.adjust_cancel:
                // 调整取消
                gpuImage.setImage(bitmap);
                mPhotoView.setImageBitmap(bitmap);
                viewSetVisible(0);
                break;
            case R.id.adjust_confirm:
                // 调整确认
                bitmap = ADJUST_BITMAP;
                mPhotoView.setImageBitmap(bitmap);
                initBitmap(bitmap);
                viewSetVisible(0);
                break;
        }
    }

    /**
     * 更新选择滤镜之后的图片显示
     *
     * @param index
     */
    private void updateFilter(int index) {
        if (index != 0)
            CURRENT_FILTER = index - 1;
        mPhotoView.setImageBitmap(bitmaps.get(index));
    }

    private void viewSetVisible(int index) {
        if (index == 0) {
            ViewVisible(functionLayout);
            ViewVisible(topLayout);
            for (int i = 0; i < linearLayouts.size(); i++) {
                if (linearLayouts.get(i).getVisibility() == View.VISIBLE)
                    ViewGone(linearLayouts.get(i));
            }
        } else {
            if (index == 1) {
                ViewVisible(filterLayout);
            } else {
                ViewVisible(adjustLayout);
            }
            ViewGone(functionLayout);
            ViewGone(topLayout);
        }
        initAdjustStartValue();
    }

    private void ViewGone(LinearLayout linearLayout) {
        linearLayout.setVisibility(View.GONE);
    }

    private void ViewVisible(LinearLayout linearLayout) {
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void showEditSavePhotoNameDialog() {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("保存图片")
                .setPlaceholder("在此输入图片名称")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        String text = builder.getEditText().getText().toString();
                        savePhoto(text);
                    }
                }).show();
    }

    private QMUITipDialog saveDialog;
    private QMUITipDialog successDialog;
    private QMUITipDialog loadingDialog;

    /**
     * 保存图片
     */
    private void savePhoto(String name) {
        saveDialog.show();
        FileUtils.getInstance().SaveImage(this, bitmap, name, new FileUtils.OnPictureSavedListener() {
            @Override
            public void onPictureSaved(Uri uri) {
                saveDialog.dismiss();
                successDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            successDialog.show();
                            Thread.sleep(1000);//休眠1秒
                            successDialog.dismiss();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }


                    }
                }.start();
            }
        });
    }

    /**
     * 保存临时图片
     */
    private void saveTempPhoto() {
        FileUtils.getInstance().SaveImage(this, bitmap, null, new FileUtils.OnPictureSavedListener() {
            @Override
            public void onPictureSaved(Uri uri) {
                UCrop.of(uri, Uri.fromFile(new File(getSDCardPath(), "crop.jpg")))
                        .start(FactoryActivity.this);
            }
        });
    }


    @Override
    public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
        switch (CURRENT_ADJUST_FILTER) {
            case 0:
                // 亮度
                gpuImageBrightnessFilter.setBrightness(range(progress, -0.5f, 0.5f));
                mPhotoView.setImageBitmap(getFilterBitmap(gpuImageBrightnessFilter));
                break;
            case 1:
                // 对比度
                gpuImageContrastFilter.setContrast(range(progress, 0.0f, 2.0f));
                mPhotoView.setImageBitmap(getFilterBitmap(gpuImageContrastFilter));
                break;
            case 2:
                // 饱和度
                gpuImageSaturationFilter.setSaturation(range(progress, 0.0f, 2.0f));
                mPhotoView.setImageBitmap(getFilterBitmap(gpuImageSaturationFilter));
                return;
            case 3:
                // 模糊
                gpuImageGaussianBlurFilter.setBlurSize(range(progress, 0.0f, 1.0f));
                mPhotoView.setImageBitmap(getFilterBitmap(gpuImageGaussianBlurFilter));
                break;
            case 4:
                // 暗角
                gpuImageHazeFilter.setDistance(range(progress, 0.0f, 0.7f));
                mPhotoView.setImageBitmap(getFilterBitmap(gpuImageHazeFilter));
                return;

        }
    }

    @Override
    public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
        gpuImage.setImage(ADJUST_BITMAP);
        switch (CURRENT_ADJUST_FILTER) {
            case 0:
                // 亮度
                BrightnessFilter = progressFloat;
                break;
            case 1:
                // 对比度
                ContrastFilter = progressFloat;
                break;
            case 2:
                // 饱和度
                SaturationFilter = progressFloat;
                return;
            case 3:
                // 模糊
                GaussianBlurFilter = progressFloat;
                break;
            case 4:
                // 暗角
                HazeFilter = progressFloat;
                return;

        }
    }

    @Override
    public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

    }

    private float range(final int percentage, final float start, final float end) {
        return (end - start) * percentage / 100.0f + start;
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
        mPhotoView.setImageBitmap(ADJUST_BITMAP);
        switch (i) {
            case R.id.adjust_1:
                // 亮度
                CURRENT_ADJUST_FILTER = 0;
                mSeekBar.setProgress(BrightnessFilter);
                break;
            case R.id.adjust_2:
                // 对比度
                CURRENT_ADJUST_FILTER = 1;
                mSeekBar.setProgress(ContrastFilter);
                break;
            case R.id.adjust_3:
                // 饱和度
                CURRENT_ADJUST_FILTER = 2;
                mSeekBar.setProgress(SaturationFilter);
                break;
            case R.id.adjust_4:
                // 模糊
                CURRENT_ADJUST_FILTER = 3;
                mSeekBar.setProgress(GaussianBlurFilter);
                break;
            case R.id.adjust_5:
                // 暗角
                CURRENT_ADJUST_FILTER = 4;
                mSeekBar.setProgress(HazeFilter);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PictureConfig.CHOOSE_REQUEST) {
                // 图片、视频、音频选择结果回调
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                // 例如 LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                if(selectList!=null && selectList.size()>0){
                    try {
                        loadBitmap(selectList.get(0).getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    finish();
                }
                return;
        }else if (resultCode == RESULT_OK) {
            Uri uri = UCrop.getOutput(data);
            if (uri != null) {
                try {
                    loadBitmap(Path2UriUtil.getRealFilePath(this, uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
//            loadingDialog.show();
//            //添加图片返回
//            if (data != null && requestCode == REQUEST_ALBUM) {
//                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(
//                        ImagePicker.EXTRA_RESULT_ITEMS);
//                if (images != null) {
//                    try {
//                        loadBitmap(images.get(0).getPath());
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        } else if (resultCode == RESULT_OK) {
//            Uri uri = UCrop.getOutput(data);
//            if (uri != null) {
//                try {
//                    loadBitmap(Path2UriUtil.getRealFilePath(this, uri));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            // 拍照和选择相片取消时才关闭当前界面，否则则是剪裁回来无需关闭
//            if (requestCode == REQUEST_ALBUM)
//                finish();
//        }
    }

    private void loadBitmap(String paths) throws IOException {
        path = paths;
        BitmapUtil.decodeSampledBitmapFromPath(path, new BitmapUtil.OnBitmapCreateListner() {
            @Override
            public void onCreateBitmap(Bitmap bitmap) {
                FactoryActivity.this.bitmap = bitmap;
                BASE_BITMAP = bitmap;
                // 初始化显示的图片（ 由于拍照、相册选取、剪裁之后都会用到，所以这里单独出来）
                initBitmap(bitmap);
                loadingDialog.dismiss();
            }
        });
    }

//    private void initImagePicker() {
//        ImagePicker imagePicker = ImagePicker.getInstance();
//        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
//        imagePicker.setShowCamera(true);                      //显示拍照按钮
//        imagePicker.setCrop(false);                           //允许裁剪（单选才有效）
//        imagePicker.setSelectLimit(1);              //选中数量限制
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseImageCache();
        System.gc();
        //销毁时删除剪裁后的图片和临时文件
        FileUtils.DeleteImage(FactoryActivity.this);
        FileUtils.DeleteCropImage(FactoryActivity.this);
    }
}
