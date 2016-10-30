package org.acra;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.IOException;
import org.acra.collector.CrashReportData;
import org.acra.util.ToastSender;

public final class CrashReportDialog extends Activity {
    String mReportFileName;
    private SharedPreferences prefs;
    private EditText userComment;
    private EditText userEmail;

    class 1 implements OnClickListener {
        1() {
        }

        public void onClick(View v) {
            String usrEmail;
            String comment = CrashReportDialog.this.userComment != null ? CrashReportDialog.this.userComment.getText().toString() : ACRAConstants.DEFAULT_STRING_VALUE;
            if (CrashReportDialog.this.prefs == null || CrashReportDialog.this.userEmail == null) {
                usrEmail = ACRAConstants.DEFAULT_STRING_VALUE;
            } else {
                usrEmail = CrashReportDialog.this.userEmail.getText().toString();
                Editor prefEditor = CrashReportDialog.this.prefs.edit();
                prefEditor.putString(ACRA.PREF_USER_EMAIL_ADDRESS, usrEmail);
                prefEditor.commit();
            }
            CrashReportPersister persister = new CrashReportPersister(CrashReportDialog.this.getApplicationContext());
            try {
                Log.d(ACRA.LOG_TAG, "Add user comment to " + CrashReportDialog.this.mReportFileName);
                CrashReportData crashData = persister.load(CrashReportDialog.this.mReportFileName);
                crashData.put(ReportField.USER_COMMENT, comment);
                crashData.put(ReportField.USER_EMAIL, usrEmail);
                persister.store(crashData, CrashReportDialog.this.mReportFileName);
            } catch (IOException e) {
                Log.w(ACRA.LOG_TAG, "User comment not added: ", e);
            }
            Log.v(ACRA.LOG_TAG, "About to start SenderWorker from CrashReportDialog");
            ACRA.getErrorReporter().startSendingReports(false, true);
            int toastId = ACRA.getConfig().resDialogOkToast();
            if (toastId != 0) {
                ToastSender.sendToast(CrashReportDialog.this.getApplicationContext(), toastId, 1);
            }
            CrashReportDialog.this.finish();
        }
    }

    class 2 implements OnClickListener {
        2() {
        }

        public void onClick(View v) {
            ACRA.getErrorReporter().deletePendingNonApprovedReports(false);
            CrashReportDialog.this.finish();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mReportFileName = getIntent().getStringExtra("REPORT_FILE_NAME");
        Log.d(ACRA.LOG_TAG, "Opening CrashReportDialog for " + this.mReportFileName);
        if (this.mReportFileName == null) {
            finish();
        }
        requestWindowFeature(3);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, -1, 1.0f));
        TextView text = new TextView(this);
        text.setText(getText(ACRA.getConfig().resDialogText()));
        scroll.addView(text, -1, -1);
        int commentPromptId = ACRA.getConfig().resDialogCommentPrompt();
        if (commentPromptId != 0) {
            TextView label = new TextView(this);
            label.setText(getText(commentPromptId));
            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            root.addView(label, new LinearLayout.LayoutParams(-1, -2));
            this.userComment = new EditText(this);
            this.userComment.setLines(2);
            root.addView(this.userComment, new LinearLayout.LayoutParams(-1, -2));
        }
        int emailPromptId = ACRA.getConfig().resDialogEmailPrompt();
        if (emailPromptId != 0) {
            label = new TextView(this);
            label.setText(getText(emailPromptId));
            label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
            root.addView(label, new LinearLayout.LayoutParams(-1, -2));
            this.userEmail = new EditText(this);
            this.userEmail.setSingleLine();
            this.userEmail.setInputType(33);
            this.prefs = getSharedPreferences(ACRA.getConfig().sharedPreferencesName(), ACRA.getConfig().sharedPreferencesMode());
            this.userEmail.setText(this.prefs.getString(ACRA.PREF_USER_EMAIL_ADDRESS, ACRAConstants.DEFAULT_STRING_VALUE));
            root.addView(this.userEmail, new LinearLayout.LayoutParams(-1, -2));
        }
        LinearLayout buttons = new LinearLayout(this);
        buttons.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        buttons.setPadding(buttons.getPaddingLeft(), 10, buttons.getPaddingRight(), buttons.getPaddingBottom());
        Button yes = new Button(this);
        yes.setText(17039379);
        yes.setOnClickListener(new 1());
        buttons.addView(yes, new LinearLayout.LayoutParams(-1, -2, 1.0f));
        Button no = new Button(this);
        no.setText(17039369);
        no.setOnClickListener(new 2());
        buttons.addView(no, new LinearLayout.LayoutParams(-1, -2, 1.0f));
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
        int resTitle = ACRA.getConfig().resDialogTitle();
        if (resTitle != 0) {
            setTitle(resTitle);
        }
        getWindow().setFeatureDrawableResource(3, ACRA.getConfig().resDialogIcon());
        cancelNotification();
    }

    protected void cancelNotification() {
        ((NotificationManager) getSystemService("notification")).cancel(666);
    }
}
