package me.tombailey.store.sampleapp.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import me.tombailey.store.http.Proxy;
import me.tombailey.store.http.Request;
import me.tombailey.store.http.Response;
import me.tombailey.store.http.form.body.FormBody;
import me.tombailey.store.http.form.body.UrlEncodedForm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tomba on 04/03/2017.
 */

public class MessageService {

    private static final String HOST = "http://o3b45q2eynqb26qp.onion";

    private static final String MESSAGE = "message";


    public static Observable<List<String>> getMessages(final Proxy proxy, final int page) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                try {
                    List<String> messages = new ArrayList<String>(32);

                    Request request = new Request.Builder()
                            .proxy(proxy)
                            .get()
                            .url(HOST + "/api/messages?page=" + page)
                            .build();
                    Response response = request.execute();

                    JSONArray messagesJson =
                            new JSONObject(response.getMessageBodyString()).getJSONArray("messages");
                    for (int index = 0; index < messagesJson.length(); index++) {
                        messages.add(messagesJson.getJSONObject(index).getString("message"));
                    }

                    subscriber.onNext(messages);
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    subscriber.onError(throwable);
                }
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Boolean> createMessage(final Proxy proxy, final String message) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    FormBody formBody = new UrlEncodedForm.Builder()
                            .add(MESSAGE, message)
                            .build();

                    Request request = new Request.Builder()
                            .proxy(proxy)
                            .post(formBody)
                            .url(HOST + "/api/messages")
                            .build();
                    Response response = request.execute();

                    subscriber.onNext(response.getStatusCode() == HttpURLConnection.HTTP_CREATED);
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    subscriber.onError(throwable);
                }
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

}
