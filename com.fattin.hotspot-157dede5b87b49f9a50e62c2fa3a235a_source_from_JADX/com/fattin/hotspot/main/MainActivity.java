package com.fattin.hotspot.main;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ToggleButton;
import com.fattin.hotspot.C0000R;
import com.fattin.hotspot.activity.AbstractActivity;
import com.fattin.hotspot.activity.AccessListActivity.AccessListAdapter;
import com.fattin.hotspot.app.DataProvider;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.connectivity.TetherManager;
import com.fattin.hotspot.helpers.Log;

public class MainActivity extends AbstractActivity {
    public static final String ACTION_ACCESS_LIST_CHANGED = "com.fattin.hotspot.ACTION_ACCESS_LIST_CHANGED";
    private AccessListAdapter adapter;
    private IntentFilter filter;
    private ToggleButton on_off_ToggleButton;
    private BroadcastReceiver receiver;

    /* renamed from: com.fattin.hotspot.main.MainActivity.1 */
    class C00261 extends BroadcastReceiver {
        C00261() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTION_ACCESS_LIST_CHANGED)) {
                MainActivity.this.adapter.refreshData();
            }
        }
    }

    /* renamed from: com.fattin.hotspot.main.MainActivity.2 */
    class C00272 implements OnCheckedChangeListener {
        C00272() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Log.m0d(MainActivity.this.LOG_TAG, "On pressed ...");
                if (DataProvider.isUserLoggedIn()) {
                    Log.m0d(MainActivity.this.LOG_TAG, "On pressed 2...");
                    if (DataProvider.getHideDataPlanNotice() || GlobalStates.isServiceStartingOrStarted()) {
                        MainActivity.this.on_off_ToggleButton.setEnabled(false);
                        MainActivity.this.startBackgroundServices();
                        return;
                    }
                    MainActivity.this.showDialog(393229);
                    return;
                }
                MainActivity.this.showLoginActivity();
                buttonView.setChecked(false);
                buttonView.setEnabled(true);
                return;
            }
            Log.m0d(MainActivity.this.LOG_TAG, "Off pressed ...");
            buttonView.setEnabled(false);
            MainActivity.this.stopBackgroundServices();
        }
    }

    /* renamed from: com.fattin.hotspot.main.MainActivity.3 */
    class C00283 implements OnCheckedChangeListener {
        C00283() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                DataProvider.setHideDataPlanNotice(true);
            }
        }
    }

    /* renamed from: com.fattin.hotspot.main.MainActivity.4 */
    class C00294 implements OnClickListener {
        C00294() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.on_off_ToggleButton.setEnabled(false);
            MainActivity.this.startBackgroundServices();
        }
    }

    /* renamed from: com.fattin.hotspot.main.MainActivity.5 */
    class C00305 implements OnCancelListener {
        C00305() {
        }

        public void onCancel(DialogInterface dialog) {
            MainActivity.this.on_off_ToggleButton.setChecked(false);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.LOG_TAG = "MainActivity";
        setContentView(C0000R.layout.main);
        Log.m0d(this.LOG_TAG, "onCreate -> begin");
        this.adapter = new AccessListAdapter(this);
        this.filter = new IntentFilter(ACTION_ACCESS_LIST_CHANGED);
        this.receiver = new C00261();
        ((ListView) findViewById(C0000R.id.main_access_list_ListView)).setAdapter(this.adapter);
        this.on_off_ToggleButton = (ToggleButton) findViewById(C0000R.id.main_on_off_ToggleButton);
        this.on_off_ToggleButton.setOnCheckedChangeListener(new C00272());
        Log.m2i(this.LOG_TAG, "isTetheringSupported=" + TetherManager.getSingleton().isTetheringSupported());
        Log.m2i(this.LOG_TAG, "isMobileDataEnabled=" + TetherManager.getSingleton().isMobileDataEnabled());
        if (startupCheck()) {
            if (DataProvider.getEulaAgreed()) {
                showLoginActivity();
            } else {
                showEula();
            }
        }
        Log.m0d(this.LOG_TAG, "onCreate -> end");
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(this.receiver, this.filter);
        this.adapter.refreshData();
        updateUI();
    }

    protected void onPause() {
        unregisterReceiver(this.receiver);
        super.onPause();
    }

    protected void updateUI() {
        super.updateUI();
        this.on_off_ToggleButton.setEnabled(!GlobalStates.isServiceStartingOrStopping());
        if (GlobalStates.isServiceStartingOrStarted()) {
            this.on_off_ToggleButton.setChecked(true);
        }
        if (GlobalStates.isServiceStoppingOrStopped()) {
            this.on_off_ToggleButton.setChecked(false);
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        View checkBoxView = View.inflate(this, C0000R.layout.checkbox, null);
        CheckBox checkBox = (CheckBox) checkBoxView.findViewById(C0000R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new C00283());
        checkBox.setText(C0000R.string.dialog_dataplan_notice_checkbox);
        switch (id) {
            case 393229:
                return new Builder(this).setTitle(C0000R.string.dialog_dataplan_notice_title).setMessage(C0000R.string.dialog_dataplan_notice).setView(checkBoxView).setPositiveButton(C0000R.string.button_ok, new C00294()).setOnCancelListener(new C00305()).create();
            default:
                return super.onCreateDialog(id, args);
        }
    }
}
