package com.mryan.mdex.adpater;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mryan.mdex.R;
import com.mryan.mdex.until.AppInfo;

import java.util.List;

/**
 * @description：TODO
 * @Author MRyan
 * @Date 2020/6/7 14:05
 * @Version 1.0
 */
public class ApplicationInfoAdapter extends BaseAdapter {

    public List<AppInfo> mListAppInfo = null;
    private LayoutInflater mInfater = null;

    public ApplicationInfoAdapter( Context context, List<AppInfo> mListAppInfo) {
        this.mInfater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mListAppInfo = mListAppInfo;
    }



    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder holder = null;
        if (convertView == null || convertView.getTag() == null) {
            view = mInfater.inflate(R.layout.appinfo_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = (AppInfo) getItem(position);
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        holder.tvAppLabel.setText(appInfo.getAppName());
        holder.tvPkgName.setText(appInfo.getPkgName());
        return view;

    }

    class ViewHolder {
        ImageView appIcon;
        TextView tvAppLabel;
        TextView tvPkgName;

        public ViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.imgApp);
            this.tvAppLabel = (TextView) view.findViewById(R.id.tvAppLabel);
            this.tvPkgName = (TextView) view.findViewById(R.id.tvPkgName);
        }
    }
}
