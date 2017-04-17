package me.tombailey.store.sampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import me.tombailey.store.http.Proxy;
import me.tombailey.store.sampleapp.service.MessageService;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by tomba on 17/02/2017.
 */

public class MainActivity extends AppCompatActivity {

    private App mApp;

    private View mProgressBar;
    private RecyclerView mRecyclerView;
    private View mErrorView;

    private MessageAdapter mMessageAdapter;

    private int mMessagePage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApp = (App) getApplication();

        mProgressBar = findViewById(R.id.activity_main_progress_bar);
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recycler_view);
        mErrorView = findViewById(R.id.activity_main_linear_layout_error);
        mErrorView.findViewById(R.id.activity_main_button_error_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });
        findViewById(R.id.activity_main_floating_action_button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddMessageActivity.class));
            }
        });

        init();
    }

    private void init() {
        mRecyclerView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);


        mApp.subscribeForProxy().flatMap(new Func1<Proxy, Observable<List<String>>>() {
            @Override
            public Observable<List<String>> call(Proxy proxy) {
                mMessagePage = 1;
                return MessageService.getMessages(proxy, mMessagePage);
            }
        }).subscribe(new Action1<List<String>>() {
            @Override
            public void call(List<String> messages) {
                if (messages.size() == 0) {
                    mRecyclerView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mErrorView.setVisibility(View.VISIBLE);

                    ((TextView) mErrorView.findViewById(R.id.activity_main_text_view_error_description))
                            .setText(R.string.main_activity_no_messages);
                } else {
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    mMessageAdapter = new MessageAdapter(messages);
                    mRecyclerView.setAdapter(mMessageAdapter);

                    //TODO: load more messages when the bottom is reached

                    mProgressBar.setVisibility(View.GONE);
                    mErrorView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                mRecyclerView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);

                ((TextView) mErrorView.findViewById(R.id.activity_main_text_view_error_description))
                        .setText(R.string.generic_error);
            }
        });
    }
}
