package com.example.lap10715.myandroidkeystore;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends ArrayAdapter {
    private Context mContext;
    private int mItemLayout;
    private List<String> mData;

    public MyAdapter(@NonNull Context context, int resource, List<String> data) {
        super(context, resource);
        this.mContext = context;
        this.mItemLayout = resource;
        this.mData = data;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
        View row = inflater.inflate(mItemLayout, null);
        TextView txtKeyAlias = row.findViewById(R.id.txt_key_alias);
        txtKeyAlias.setText(mData.get(position));

        Button btnDelete = row.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v->{
            ((MainActivity)mContext).deleteKey(mData.get(position));
        });

        Button btnEncrypt = row.findViewById(R.id.btn_encrypt);
        btnEncrypt.setOnClickListener(v->{
            ((MainActivity)mContext).encryptString(mData.get(position));
        });

        Button btnDecrypt = row.findViewById(R.id.btn_decrypt);
        btnDecrypt.setOnClickListener(v->{
            ((MainActivity)mContext).decryptString(mData.get(position));
        });
        return row;
    }

    @Override
    public int getCount() {
        return mData.size();
    }
}
