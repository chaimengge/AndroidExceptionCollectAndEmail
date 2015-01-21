package com.example.test;

import android.app.Application;

public class TestApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler handler = CrashHandler.getInstance();
		handler.init(getApplicationContext());
	}
	
}
