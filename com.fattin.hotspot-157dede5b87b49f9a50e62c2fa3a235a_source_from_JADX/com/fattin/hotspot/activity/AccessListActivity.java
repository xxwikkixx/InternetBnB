package com.fattin.hotspot.activity;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.netfilter.AccessListDataObject;
import com.fattin.hotspot.netfilter.AccessListDatabase;
import com.google.analytics.tracking.android.EasyTracker;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AccessListActivity extends ListActivity {
    public static final String ACTION_ACCESS_LIST_CHANGED = "com.fattin.hotspot.ACTION_ACCESS_LIST_CHANGED";
    private AccessListAdapter adapter;
    private IntentFilter filter;
    private BroadcastReceiver receiver;

    /* renamed from: com.fattin.hotspot.activity.AccessListActivity.1 */
    class C00041 extends BroadcastReceiver {
        C00041() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AccessListActivity.ACTION_ACCESS_LIST_CHANGED)) {
                AccessListActivity.this.adapter.refreshData();
            }
        }
    }

    public static class AccessListAdapter extends BaseAdapter {
        private ArrayList<AccessListDataObject> dataList;
        private LayoutInflater inflater;

        public AccessListAdapter(Context context) {
            this.dataList = new ArrayList(AccessListDatabase.getAllDevicesData());
            this.inflater = LayoutInflater.from(context);
        }

        public void refreshData() {
            this.dataList = new ArrayList(AccessListDatabase.getAllDevicesData());
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.dataList.size();
        }

        public AccessListDataObject getItem(int position) {
            return (AccessListDataObject) this.dataList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AccessListDataObject item = getItem(position);
            convertView = this.inflater.inflate(C0000R.layout.accesslistitem, null);
            TextView session_status_TextView = (TextView) convertView.findViewById(C0000R.id.accessListItem_session_status_TextView);
            TextView hostname_TextView = (TextView) convertView.findViewById(C0000R.id.accessListItem_hostname_TextView);
            TextView earnings_TextView = (TextView) convertView.findViewById(C0000R.id.accessListItem_earnings_TextView);
            TextView duration_TextView = (TextView) convertView.findViewById(C0000R.id.accessListItem_duration_TextView);
            if (item.getDeviceToken().isEmpty()) {
                session_status_TextView.setText(C0000R.string.accessList_session_status_unauthorized);
                earnings_TextView.setText("0.00 USD");
                duration_TextView.setText("00min 00sec");
                hostname_TextView.setText(item.getHostName());
            } else {
                session_status_TextView.setText(C0000R.string.accessList_session_status_authorized);
                earnings_TextView.setText(new StringBuilder(String.valueOf(new DecimalFormat("###,##0.00").format((double) item.getEarnings()))).append(" USD").toString());
                long duration = (long) item.getDuration();
                int minutes = (int) Math.floor((double) (duration / 60000));
                int seconds = (int) ((duration / 1000) % 60);
                DecimalFormat df2 = new DecimalFormat("####00");
                duration_TextView.setText(df2.format((long) minutes) + "min " + df2.format((long) seconds) + "sec");
                hostname_TextView.setText(item.getHostName());
            }
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0000R.layout.accesslist);
        this.adapter = new AccessListAdapter(this);
        setListAdapter(this.adapter);
        this.filter = new IntentFilter(ACTION_ACCESS_LIST_CHANGED);
        this.receiver = new C00041();
    }

    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    protected void onResume() {
        super.onResume();
        getApplicationContext().registerReceiver(this.receiver, this.filter);
        this.adapter.refreshData();
    }

    protected void onPause() {
        getApplicationContext().unregisterReceiver(this.receiver);
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }
}
