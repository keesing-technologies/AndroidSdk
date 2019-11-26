package com.keesing.kvsclient;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.keesing.kvsclient.types.LoginCredentials;
import com.keesing.kvsclient.utils.Store;

public class SimpleNavigation extends NavigationView {

    private final Context context;
    private final LayoutInflater inflater;

    private TextView txtTitle;
    private TextView txtSubTitle;

    public SimpleNavigation(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initLayout();
    }

    public SimpleNavigation(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initLayout();
    }

    public SimpleNavigation(Context context) {
        super(context);

        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initLayout();

    }

    private void initLayout() {

        inflater.inflate(R.layout.navigation_drawer_layout, this);
        txtTitle = findViewById(R.id.navdrawer_title);
        txtSubTitle = findViewById(R.id.navdrawer_subtitle);

        LoginCredentials lc = Store.Retreive(this.context, LoginCredentials.STORE_KEY, LoginCredentials.class);

        if(lc != null){
            txtTitle.setText(lc.getAccount());
            txtSubTitle.setText(lc.getUsername());
        } else {
            txtTitle.setText("Need to login first");
            txtSubTitle.setText("");
        }
    }


}
