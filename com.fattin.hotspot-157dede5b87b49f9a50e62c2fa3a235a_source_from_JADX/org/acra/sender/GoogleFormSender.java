package org.acra.sender;

import android.net.Uri;
import android.util.Log;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.util.HttpRequest;
import org.xbill.DNS.KEYRecord;

public class GoogleFormSender implements ReportSender {
    private final Uri mFormUri;

    static /* synthetic */ class 1 {
        static final /* synthetic */ int[] $SwitchMap$org$acra$ReportField;

        static {
            $SwitchMap$org$acra$ReportField = new int[ReportField.values().length];
            try {
                $SwitchMap$org$acra$ReportField[ReportField.APP_VERSION_NAME.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$acra$ReportField[ReportField.ANDROID_VERSION.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public GoogleFormSender() {
        this.mFormUri = null;
    }

    public GoogleFormSender(String formKey) {
        this.mFormUri = Uri.parse(String.format(ACRA.getConfig().googleFormUrlFormat(), new Object[]{formKey}));
    }

    public void send(CrashReportData report) throws ReportSenderException {
        Uri formUri;
        if (this.mFormUri == null) {
            formUri = Uri.parse(String.format(ACRA.getConfig().googleFormUrlFormat(), new Object[]{ACRA.getConfig().formKey()}));
        } else {
            formUri = this.mFormUri;
        }
        Map<String, String> formParams = remap(report);
        formParams.put("pageNumber", "0");
        formParams.put("backupCache", ACRAConstants.DEFAULT_STRING_VALUE);
        formParams.put("submit", "Envoyer");
        try {
            URL reportUrl = new URL(formUri.toString());
            Log.d(ACRA.LOG_TAG, "Sending report " + ((String) report.get(ReportField.REPORT_ID)));
            Log.d(ACRA.LOG_TAG, "Connect to " + reportUrl);
            HttpRequest request = new HttpRequest();
            request.setConnectionTimeOut(ACRA.getConfig().connectionTimeout());
            request.setSocketTimeOut(ACRA.getConfig().socketTimeout());
            request.setMaxNrRetries(ACRA.getConfig().maxNumberOfRequestRetries());
            request.sendPost(reportUrl, formParams);
        } catch (IOException e) {
            throw new ReportSenderException("Error while sending report to Google Form.", e);
        }
    }

    private Map<String, String> remap(Map<ReportField, String> report) {
        ReportField[] fields = ACRA.getConfig().customReportContent();
        if (fields.length == 0) {
            fields = ACRA.DEFAULT_REPORT_FIELDS;
        }
        int inputId = 0;
        Map<String, String> result = new HashMap();
        for (ReportField originalKey : fields) {
            switch (1.$SwitchMap$org$acra$ReportField[originalKey.ordinal()]) {
                case KEYRecord.PROTOCOL_TLS /*1*/:
                    result.put("entry." + inputId + ".single", "'" + ((String) report.get(originalKey)));
                    break;
                case KEYRecord.PROTOCOL_EMAIL /*2*/:
                    result.put("entry." + inputId + ".single", "'" + ((String) report.get(originalKey)));
                    break;
                default:
                    result.put("entry." + inputId + ".single", report.get(originalKey));
                    break;
            }
            inputId++;
        }
        return result;
    }
}
