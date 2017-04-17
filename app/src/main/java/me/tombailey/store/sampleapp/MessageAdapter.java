package me.tombailey.store.sampleapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tomba on 04/03/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<String> mMessages;

    public MessageAdapter(List<String> messages) {
        mMessages = messages;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =
                (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(layoutInflater.inflate(R.layout.message_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setMessage(mMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public void addMore(List<String> messages) {
        mMessages.addAll(messages);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setMessage(String messsage) {
            ((TextView) mItemView.findViewById(R.id.message_view_text_view_message))
                    .setText(messsage);
        }
    }

}
