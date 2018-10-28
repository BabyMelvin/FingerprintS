package com.ellabrain.fingerstudy;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
 * Copyright 2018
 * http://www.aiella.com
 * Author: Melvin(Hang Cao)
 * Email : hang.yasuo@gmail.com
 * Date  : 2018/10/18
 */
public class Main extends AppCompatActivity {

    private static final String TAG = "Main";
    private FingerprintManagerCompat mFingerprintManagerCompat;
    private FingerprintManager mFingerprintManager;

    public static final int MSG_AUTH_ERROR = 100;
    public static final int MSG_AUTH_HELP = 101;
    public static final int MSG_AUTH_SUCESS = 102;
    public static final int MSG_AUTH_FAILED = 103;


    private TextView mResultInfo;
    private Button mCancelBtn;
    private Button mStartBtn;
    private CancellationSignal mCancellationSignal;
    private MyAuthCallback mMyAuthCallback;
    private Handler mHandler;

    private void handleHelpCode(int code) {
        switch (code){
            case FingerprintManager.FINGERPRINT_ACQUIRED_GOOD:
                setResultInfo(getString(R.string.AcquiredGood_warning));
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                setResultInfo(getString(R.string.AcquiredImageDirty_warning));
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
                setResultInfo(getString(R.string.AcquiredInsufficient_warning));
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
                setResultInfo(getString(R.string.AcquiredPartial_warning));
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
                setResultInfo(getString(R.string.AcquiredTooFast_warning));
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
                setResultInfo(getString(R.string.AcquiredToSlow_warning));
                break;
        }
    }

    private void setResultInfo(String msg) {
        if(mResultInfo!=null){
            if(msg.equals(getString(R.string.fingerprint_success))){
                mResultInfo.setTextColor(getColor(R.color.success_color));
            }else{
                mResultInfo.setTextColor(getColor(R.color.warning_color));
            }
            mResultInfo.setText(msg);
        }
    }

    private void handlerErrorCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                setResultInfo(getString(R.string.ErrorCanceled_warning));
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                setResultInfo(getString(R.string.ErrorHwUnavailable_warning));
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                setResultInfo(getString(R.string.ErrorLockout_warning));
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                setResultInfo(getString(R.string.ErrorNoSpace_warning));
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                setResultInfo(getString(R.string.ErrorUnableToProcess_warning));
                break;
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");
        initView();
        initData();
    }

    private void initView() {
        mResultInfo = findViewById(R.id.fingerprint_status);
        mCancelBtn = findViewById(R.id.cancel_button);
        mStartBtn = findViewById(R.id.start_button);
        mCancelBtn.setEnabled(false);
        mStartBtn.setEnabled(true);

        //set button listeners
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set button state
                mCancelBtn.setEnabled(false);
                mStartBtn.setEnabled(true);

                //cancel fingerprint auth here
                mCancellationSignal.cancel();
                mCancellationSignal=null;
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset result info
                mResultInfo.setText(R.string.fingerprint_hint);
                mResultInfo.setTextColor(getColor(R.color.hint_color));

                //start fingerprint auth here
                try {
                    CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
                    if(mCancellationSignal==null) {
                        mCancellationSignal = new CancellationSignal();
                    }
                    mFingerprintManagerCompat.authenticate(cryptoObjectHelper.buildCryptoObject(),0,
                            mCancellationSignal,mMyAuthCallback,null);

                    //set button state
                    mStartBtn.setEnabled(false);
                    mCancelBtn.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: "+msg.what+" arg1"+msg.arg1);
            switch (msg.what) {
                case MSG_AUTH_ERROR:
                    handlerErrorCode(msg.arg1);
                    break;
                case MSG_AUTH_FAILED:
                    setResultInfo(getString(R.string.fingerprint_not_recongnized));
                    mCancelBtn.setEnabled(false);
                    mStartBtn.setEnabled(true);
                    mCancellationSignal=null;
                    break;
                case MSG_AUTH_SUCESS:
                    setResultInfo(getString(R.string.fingerprint_success));
                    break;
                case MSG_AUTH_HELP:
                    handleHelpCode(msg.arg1);
                    break;
            }
        }
    };
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initData() {
        //1.获取FingerManager TODO 推荐使用，向下兼容
        mFingerprintManagerCompat = FingerprintManagerCompat.from(this);
        //2.利用API 23+
        mFingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
        if (!mFingerprintManagerCompat.isHardwareDetected()) {
            // no finger detected ,tell user
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_sensor_dialog_title);
            builder.setMessage(R.string.no_sensor_dialog_message);
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setNegativeButton(R.string.cancel_btn_dialg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            //show this dialog
            builder.create().show();
        }
        //3.是否处于安全保护中
        //你的设备必须是使用屏幕锁保护的，这个屏幕锁可以是password，PIN或者图案都行。为什么是这样呢？
        // 因为google原生的逻辑就是：想要使用指纹识别的话，必须首先使能屏幕锁才行，这个和android 5.0中的smart lock逻辑是一样的，i
        // 这是因为google认为目前的指纹识别技术还是有不足之处，安全性还是不能和传统的方式比较的
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        assert keyguardManager != null;
        if (keyguardManager.isKeyguardSecure()) {
            //设备是安全的
        }
        //4.设备是否注册有指纹
        //在android 6.0中，普通app要想使用指纹识别功能的话，用户必须首先在setting中注册至少一个指纹才行，否则是不能使用的
        if (!mFingerprintManagerCompat.hasEnrolledFingerprints()) {
            //no fingerprint image has been enrolled
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_fingerprint_enrolled_dialog_title);
            builder.setMessage(R.string.no_fingerprint_enrooled_message);
            builder.setIcon(R.drawable.ic_launcher_background);
            builder.setNegativeButton(R.string.cancel_btn_dialg, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            //show this dialog
            builder.create().show();
        }else{
            mMyAuthCallback = new MyAuthCallback(mHandler);
        }
    }

    /**
     * 扫描用户按下的指纹：authenticate
     * void authenticate(FingerManager.CryObject crypto,CancellationSignal cancel,int flags,FingerManager.AuthenticateCallback callback,Handler handler);
     * 1.crypto这是一个加密类的对象，指纹扫描器会使用这个对象来判断认证结果的合法性。这个对象可以是null，但是这样的话，就意味这app无条件信任认证的结果，虽然从理论上这个过程可能被攻击，数据可以被篡改，
     * 这是app在这种情况下必须承担的风险。因此，建议这个参数不要置为null。这个类的实例化有点麻烦，主要使用javax的security接口实现，后面我的demo程序中会给出一个helper类，这个类封装内部实现的逻辑，
     * 开发者可以直接使用我的类简化实例化的过程。
     * 2.cancel 这个是CancellationSignal类的一个对象，这个对象用来在指纹识别器扫描用户指纹的是时候取消当前的扫描操作，如果不取消的话，
     * 那么指纹扫描器会一直扫描直到超时（一般为30s，取决于具体的厂商实现），这样的话就会比较耗电。建议这个参数不要置为null。
     * 3. flags 标识位，暂时应该为0，这个标志位应该是保留将来使用的。
     * 4. callback 这个是FingerprintManager.AuthenticationCallback类的对象，这个是这个接口中除了第一个参数之外最重要的参数了。当系统完成了指纹认证过程（失败或者成功都会）后，
     * 会回调这个对象中的接口，通知app认证的结果。这个参数不能为NULL。
     * 5. handler 这是Handler类的对象，如果这个参数不为null的话，那么FingerprintManager将会使用这个handler中的looper来处理来自指纹识别硬件的消息。通常来讲，开发这不用提供这个参数，可以直接置为null，
     * 因为FingerprintManager会默认使用app的main looper来处理。
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }
}
