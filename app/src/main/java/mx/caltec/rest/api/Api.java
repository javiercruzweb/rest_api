package mx.caltec.rest.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import mx.caltec.rest.R;
import mx.caltec.rest.listeners.TaskListener;
import mx.caltec.rest.utils.RestGsonBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private static final String TAG = "Api";

    private static Api INSTANCE;
    private Retrofit retrofit;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final Executor THREAD_POOL_EXECUTOR  = Executors.newFixedThreadPool(CORE_POOL_SIZE);
    private WebService webService;
    private Context mContext;
    private String token;

    public static Api getInstance() {
        if (INSTANCE==null) {
            INSTANCE = new Api();
        }

        return INSTANCE;
    }

    private Api() {
        //http client
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS);

        //add custom headers
        client.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                if (token != null) {
                    okhttp3.Request.Builder requestBuilder = original.newBuilder()
                            .header("api-token", token)
                            .method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
                else {
                    return chain.proceed(original);
                }
            }
        });

        //retrofit build
        retrofit = new Retrofit.Builder()
                .baseUrl("")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(RestGsonBuilder.getGsonBuilder().create()))
                .callbackExecutor(THREAD_POOL_EXECUTOR)
                .build();
        webService = retrofit.create(WebService.class);
    }

    private Observable<String> login(String usr, String pwd) {
        return webService.login(usr, pwd)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void loginAPI(String usr, String pwd, final TaskListener<String> listener) {
        Observable<String> loginObservable = login(usr, pwd);
        loginObservable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onNext(String s) {
                token = s;
                saveSession();
            }

            @Override
            public void onError(Throwable e) {
                listener.onError(getApiError(e));
            }

            @Override
            public void onComplete() {
                listener.onSuccess("success");
            }
        });
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void loadSession() {
        SharedPreferences prefs = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.token = prefs.getString("token", null);
    }

    public void saveSession() {
        SharedPreferences settings = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.apply();
    }

    //api error handling
    public String getApiError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            Response response = exception.response();
            try {
                return response.errorBody().string();
            }
            catch (Exception e) {
                return mContext.getString(R.string.api_error);
            }
        }
        else {
            return mContext.getString(R.string.api_error);
        }
    }

}
