package mx.caltec.rest;

import androidx.multidex.MultiDexApplication;

import mx.caltec.rest.api.Api;

public class RestApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Api.getInstance().setmContext(getApplicationContext());
        Api.getInstance().loadSession();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Api.getInstance().saveSession();
    }

}
