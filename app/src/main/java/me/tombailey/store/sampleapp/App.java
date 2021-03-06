package me.tombailey.store.sampleapp;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import me.tombailey.store.http.Cache;
import me.tombailey.store.http.Proxy;
import me.tombailey.store.http.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;

/**
 * Created by tomba on 04/03/2017.
 */

public class App extends Application {

    private static final String LOG_TAG = App.class.getName();


    private BroadcastReceiver mProxyBroadReceiver;

    private Proxy mLatestProxy;
    private ReplaySubject<Proxy> mProxyReplaySubject;
    private BehaviorSubject<Proxy> mProxyBehaviorSubject;

    @Override
    public void onCreate() {
        super.onCreate();

        Cache cache = new Cache.Builder()
                //10mb
                .context(this)
                .maxSize(1024 * 1024 * 10)
                .cacheDirectory(getCacheDir())
                .build();
        Request.setCache(cache);

        subscribeForProxyUpdates();
        startProxy();
    }

    /**
     * Get a subscription for the latest instance of the proxy. The latest proxy value may be null
     * if the proxy has stopped
     * @return a subscription for the latest instance of the proxy
     */
    public Observable<Proxy> subscribeForProxy() {
        return mProxyBehaviorSubject.take(1);
    }

    protected void subscribeForProxyUpdates() {
        IntentFilter proxyIntentFilter = new IntentFilter();
        proxyIntentFilter.addAction("me.tombailey.store.PROXY_STATUS_UPDATE");

        if (mProxyBroadReceiver == null) {
            mProxyReplaySubject = ReplaySubject.create(1);
            mProxyBehaviorSubject = BehaviorSubject.create();
            mProxyReplaySubject.subscribe(mProxyBehaviorSubject);

            mProxyBroadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent statusUpdate) {
                    if ("me.tombailey.store.PROXY_STATUS_UPDATE".equalsIgnoreCase(statusUpdate.getAction())) {
                        String status = statusUpdate.getStringExtra("status");
                        Log.d(LOG_TAG, "proxy status is now '" + status + "'");

                        if ("running".equalsIgnoreCase(status)) {
                            String host = statusUpdate.getStringExtra("host");
                            int port = statusUpdate.getIntExtra("port", 0);

                            Log.d(LOG_TAG, "proxy is running on " + host + ":" + port);
                            mLatestProxy = new Proxy(host, port);
                        } else {
                            mLatestProxy = null;
                        }
                        mProxyReplaySubject.onNext(mLatestProxy);
                    }
                }
            };
            registerReceiver(mProxyBroadReceiver, proxyIntentFilter);
        }
    }

    /**
     * Request for the proxy to start
     */
    public void startProxy() {
        Intent startTorConnectionService = new Intent();
        startTorConnectionService.setComponent(new ComponentName("me.tombailey.store", "me.tombailey.store.service.TorConnectionService"));
        startTorConnectionService.setAction("start");
        startService(startTorConnectionService);
    }
}
