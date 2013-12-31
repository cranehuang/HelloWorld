package com.crane.smsapp;



import com.crane.data.Contact;
import com.crane.utils.DraftCache;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmsApp extends Application {
	private static SmsApp mApp;
	
	private TelephonyManager mTelephonyManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mApp = this;
//		Log.init("TestConver");
//		Log.d("Application startup");
//		RecipientIdCache.init(this);
		DraftCache.init(this);
		Contact.init(this);
	}
	
	synchronized public static SmsApp getApplication() {
        return mApp;
    }
	
	/**
     * @return Returns the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }
	
	public static Application app() {
		return mApp;
	}
}
