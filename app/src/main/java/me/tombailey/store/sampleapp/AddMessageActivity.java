package me.tombailey.store.sampleapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import me.tombailey.store.http.Proxy;
import me.tombailey.store.sampleapp.service.MessageService;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by tomba on 04/03/2017.
 */

public class AddMessageActivity extends AppCompatActivity {

    private App mApp;

    private ProgressDialog progressDialog;

    private View mProgressBar;
    private View mContentView;
    private View mErrorView;

    private Subscription mAddMessageSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message_activity);

        mApp = (App) getApplication();

        mProgressBar = findViewById(R.id.activity_add_message_progress_bar);
        mContentView = findViewById(R.id.activity_add_message_linear_layout_content);
        mContentView.findViewById(R.id.activity_add_message_button_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMessage(((EditText) findViewById(R.id.activity_add_message_edit_text_message))
                        .getText().toString());
            }
        });
        mErrorView = findViewById(R.id.activity_add_message_linear_layout_error);
        mErrorView.findViewById(R.id.activity_add_message_button_error_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });

        init();
    }

    @Override
    protected void onDestroy() {
        unsubscribeFromAddMessage();
        super.onDestroy();
    }

    private void unsubscribeFromAddMessage() {
        if (mAddMessageSubscription != null && !mAddMessageSubscription.isUnsubscribed()) {
            mAddMessageSubscription.unsubscribe();
        }
    }

    private void init() {
        mErrorView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mContentView.setVisibility(View.VISIBLE);
    }

    private void addMessage(final String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.add_message_activity_adding_message));
        progressDialog.show();

        unsubscribeFromAddMessage();

        mAddMessageSubscription = mApp.subscribeForProxy().flatMap(new Func1<Proxy, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Proxy proxy) {
                return MessageService.createMessage(proxy, message);
            }
        }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean messageCreated) {
                progressDialog.dismiss();
                if (messageCreated) {
                    onBackPressed();
                    Toast.makeText(AddMessageActivity.this, R.string.add_message_activity_message_added, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddMessageActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                progressDialog.dismiss();

                Toast.makeText(AddMessageActivity.this, R.string.generic_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
