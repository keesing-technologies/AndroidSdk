package com.keesing.kvsclient.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

//https://stackoverflow.com/questions/41743401/add-navigation-drawer-to-an-existing-activity

public class MenuItemAdapter extends BaseAdapter {

    public MenuItemAdapter(Context context, List<String> sections){

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
