package com.ellabrain.fingerstudy;

import android.os.Handler;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/*
 * Copyright 2018
 * http://www.aiella.com
 * Author: Melvin(Hang Cao)
 * Email : hang.yasuo@gmail.com
 * Date  : 2018/10/28
 */
public class MyAuthCallback extends FingerprintManagerCompat.AuthenticationCallback {
    private Handler mHandler = null;

    public MyAuthCallback(Handler handler) {
        super();
        this.mHandler = handler;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        super.onAuthenticationError(errMsgId, errString);
        if (mHandler != null) {
            mHandler.obtainMessage(Main.MSG_AUTH_ERROR, errMsgId, 0).sendToTarget();
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        if (mHandler != null) {
            mHandler.obtainMessage(Main.MSG_AUTH_HELP, helpMsgId, 0).sendToTarget();
        }
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if(mHandler!=null) {
            mHandler.obtainMessage(Main.MSG_AUTH_SUCESS).sendToTarget();
        }
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (mHandler != null) {
            mHandler.obtainMessage(Main.MSG_AUTH_FAILED).sendToTarget();
        }
    }
}
