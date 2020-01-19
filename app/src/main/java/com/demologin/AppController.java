package com.demologin;

import androidx.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;

public class AppController extends MultiDexApplication {
    private static AppController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        if (BuildConfig.DEBUG)
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static AppController getInstance() {
        return sInstance;
    }

}
