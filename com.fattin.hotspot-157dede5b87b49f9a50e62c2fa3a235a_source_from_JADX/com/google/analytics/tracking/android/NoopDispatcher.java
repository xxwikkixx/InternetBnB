package com.google.analytics.tracking.android;

import android.text.TextUtils;
import java.util.List;
import org.acra.ACRAConstants;
import org.xbill.DNS.KEYRecord.Flags;

class NoopDispatcher implements Dispatcher {
    NoopDispatcher() {
    }

    public boolean okToDispatch() {
        return true;
    }

    public int dispatchHits(List<Hit> hits) {
        if (hits == null) {
            return 0;
        }
        Log.iDebug("Hits not actually being sent as dispatch is false...");
        int maxHits = Math.min(hits.size(), 40);
        for (int i = 0; i < maxHits; i++) {
            if (Log.isDebugEnabled()) {
                String logMessage;
                String modifiedHit = TextUtils.isEmpty(((Hit) hits.get(i)).getHitParams()) ? ACRAConstants.DEFAULT_STRING_VALUE : HitBuilder.postProcessHit((Hit) hits.get(i), System.currentTimeMillis());
                if (TextUtils.isEmpty(modifiedHit)) {
                    logMessage = "Hit couldn't be read, wouldn't be sent:";
                } else if (modifiedHit.length() <= 2036) {
                    logMessage = "GET would be sent:";
                } else if (modifiedHit.length() > Flags.FLAG2) {
                    logMessage = "Would be too big:";
                } else {
                    logMessage = "POST would be sent:";
                }
                Log.iDebug(logMessage + modifiedHit);
            }
        }
        return maxHits;
    }
}
