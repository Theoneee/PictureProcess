package the.one.pictureprocess.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import butterknife.ButterKnife;
import butterknife.OnClick;
import the.one.pictureprocess.R;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.FlymeSetStatusBarLightMode(getWindow(), true);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.btn_new, R.id.btn_album})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_new:
                // 拍照
                requestPermission();
                break;
            case R.id.btn_album:
                // 相册
                FactoryActivity.startThisActivity(this,FactoryActivity.ALBUM);
                break;
        }
    }

    /**
     * 检测权限
     */
    private void requestPermission(){
        // 当系统大于等于Android6.0时才需要动态进行请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查相机权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // 没有则进行申请
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        FactoryActivity.REQUEST_CAMERA);
            }else {
                // 有发送相机跳转
                FactoryActivity.startThisActivity(this,FactoryActivity.CAMERA);
            }
        }else{
            // 小于Android6直接发送相机跳转
            FactoryActivity.startThisActivity(this,FactoryActivity.CAMERA);
        }
    }

    /**
     * 权限申请返回结果
     * @param requestCode 请求码
     * @param permissions 权限组
     * @param grantResults 请求结果 0 同意
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FactoryActivity.REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意则发送相机跳转
                FactoryActivity.startThisActivity(this,FactoryActivity.CAMERA);
            } else {
                // 拒绝则提示
                Toast.makeText(this,"权限已拒绝，请去设置界面打开相机权限",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
