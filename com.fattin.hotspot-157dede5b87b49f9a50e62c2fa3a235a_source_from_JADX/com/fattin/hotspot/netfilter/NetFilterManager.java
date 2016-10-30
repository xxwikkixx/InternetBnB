package com.fattin.hotspot.netfilter;

import android.os.AsyncTask;
import com.fattin.hotspot.apilib.APIs;
import com.fattin.hotspot.apilib.RestResponse;
import com.fattin.hotspot.apilib.RestResponseExceptions.APIUserAuthTokenInvalidException;
import com.fattin.hotspot.app.BusinessLogic;
import com.fattin.hotspot.app.Constants;
import com.fattin.hotspot.app.GlobalStates;
import com.fattin.hotspot.app.MainApplication;
import com.fattin.hotspot.connectivity.TetherManager;
import com.fattin.hotspot.helpers.CoreTask;
import com.fattin.hotspot.helpers.Log;
import com.fattin.hotspot.helpers.Util;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import org.acra.ACRAConstants;
import org.json.JSONArray;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;

public class NetFilterManager {
    protected static String IPTABLES = null;
    protected static String IPTABLES_OUTPUT = null;
    private static final String LOG_TAG = "NetFilterManager";
    private static ExtendedResolver extResolver;
    private static String verified_nat_rules_checksum;

    /* renamed from: com.fattin.hotspot.netfilter.NetFilterManager.1 */
    class C00321 extends AsyncTask<Void, Void, Boolean> {
        C00321() {
        }

        protected Boolean doInBackground(Void... params) {
            return Boolean.valueOf(NetFilterManager.enableNatRules());
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    /* renamed from: com.fattin.hotspot.netfilter.NetFilterManager.2 */
    class C00332 extends AsyncTask<Void, Void, Boolean> {
        C00332() {
        }

        protected Boolean doInBackground(Void... params) {
            return Boolean.valueOf(NetFilterManager.disableNatRules());
        }
    }

    static {
        IPTABLES = null;
        IPTABLES_OUTPUT = null;
        verified_nat_rules_checksum = null;
        extResolver = null;
    }

    public static boolean enableNatRules(boolean new_tread) {
        if (!new_tread) {
            return enableNatRules();
        }
        new C00321().execute(new Void[0]);
        return true;
    }

    public static boolean disableNatRules(boolean new_tread) {
        if (!new_tread) {
            return disableNatRules();
        }
        new C00332().execute(new Void[0]);
        return true;
    }

    public static synchronized boolean updateWhiteList() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "updateWhiteList -> begin");
            result = (true & add_nat_rules_whitelist()) & update_nat_rules_checksum();
            Log.m0d(LOG_TAG, "updateWhiteList -> end");
        }
        return result;
    }

    private static synchronized boolean enableNatRules() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "enableNatRules -> begin");
            if (!GlobalStates.isNetFilterEnabled()) {
                GlobalStates.setNetFilterEnabled(true);
            }
            result = true & rebuildNatRules();
            Log.m0d(LOG_TAG, "enableNatRules -> end");
        }
        return result;
    }

    private static synchronized boolean disableNatRules() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "disableNatRules -> begin");
            if (GlobalStates.isNetFilterEnabled()) {
                GlobalStates.setNetFilterEnabled(false);
            }
            result = true & flushNatRules();
            Log.m0d(LOG_TAG, "disableNatRules -> end");
        }
        return result;
    }

    public static synchronized boolean rebuildNatRules() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "rebuildNatRules -> begin");
            result = true;
            if (GlobalStates.isNetFilterEnabled()) {
                try {
                    result = (((true & flush_nat_rules()) & add_nat_rules()) & update_nat_rules_checksum()) & BusinessLogic.heartbeatAll();
                } catch (APIUserAuthTokenInvalidException e) {
                    Log.m1e(LOG_TAG, e.getMessage(), e);
                    result = false;
                }
            }
            Log.m0d(LOG_TAG, "rebuildNatRules -> end");
        }
        return result;
    }

    public static synchronized boolean isNatTablesValid() {
        boolean is_nat_table_valid;
        synchronized (NetFilterManager.class) {
            is_nat_table_valid = is_nat_table_valid();
        }
        return is_nat_table_valid;
    }

    private static synchronized boolean flushNatRules() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "flushNatRules -> begin");
            try {
                result = (true & flush_nat_rules()) & update_nat_rules_checksum();
                Log.m0d(LOG_TAG, "flushNatRules -> end");
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
                result = false;
            }
        }
        return result;
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                if (intf.getName().contains(TetherManager.getSingleton().getTetheredWifiIface())) {
                    Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                    continue;
                }
            }
        } catch (SocketException e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        return null;
    }

    protected static synchronized boolean update_nat_rules_output_file() {
        boolean runRootCommand;
        synchronized (NetFilterManager.class) {
            _check_common_fields();
            runRootCommand = CoreTask.runRootCommand(IPTABLES + "-t nat -n -L > " + IPTABLES_OUTPUT + "; chown " + MainApplication.UID + ":" + MainApplication.UID + " " + IPTABLES_OUTPUT);
        }
        return runRootCommand;
    }

    protected static synchronized boolean update_nat_rules_checksum() {
        boolean result;
        synchronized (NetFilterManager.class) {
            Log.m0d(LOG_TAG, "update_nat_rules_checksum()");
            result = true & update_nat_rules_output_file();
            Log.m0d(LOG_TAG, "update_nat_rules_checksum -> verified_nat_rules_checksum=");
            try {
                verified_nat_rules_checksum = Util.getFileChecksum(IPTABLES_OUTPUT);
                Log.m0d(LOG_TAG, verified_nat_rules_checksum);
            } catch (Exception e) {
                Log.m1e(LOG_TAG, e.getMessage(), e);
                result = false;
            }
        }
        return result;
    }

    private static boolean is_nat_table_valid() {
        update_nat_rules_output_file();
        String nat_rules_checksum = ACRAConstants.DEFAULT_STRING_VALUE;
        boolean captive_found = false;
        boolean drop_found = false;
        try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(IPTABLES_OUTPUT)));
            String str = ACRAConstants.DEFAULT_STRING_VALUE;
            while (true) {
                str = buffer.readLine();
                if (str == null) {
                    break;
                }
                if (str.contains("CAPTIVE_PORTAL")) {
                    captive_found = true;
                }
                if (str.contains("DROP")) {
                    drop_found = true;
                }
                if (captive_found && drop_found) {
                    break;
                }
            }
            buffer.close();
            if (captive_found && drop_found) {
                nat_rules_checksum = Util.getFileChecksum(IPTABLES_OUTPUT);
                Log.m0d(LOG_TAG, "is_nat_table_valid -> nat_rules_checksum=");
                Log.m0d(LOG_TAG, nat_rules_checksum);
                Log.m0d(LOG_TAG, "is_nat_table_valid -> verified_nat_rules_checksum=");
                Log.m0d(LOG_TAG, verified_nat_rules_checksum);
                return nat_rules_checksum.equals(verified_nat_rules_checksum);
            }
            Log.m0d(LOG_TAG, "is_nat_table_valid -> keyword(s) not found");
            return false;
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    private static void _check_common_fields() {
        if (IPTABLES == null) {
            IPTABLES = CoreTask.DATA_FILE_PATH + "/bin/iptables ";
        }
        if (IPTABLES_OUTPUT == null) {
            IPTABLES_OUTPUT = CoreTask.DATA_FILE_PATH + "/var/captive.log";
        }
    }

    private static boolean add_nat_rules() {
        _check_common_fields();
        CoreTask.runRootCommand(IPTABLES + "-N ACCESS_LIST;" + IPTABLES + "-I FORWARD -j ACCESS_LIST;" + IPTABLES + "-t nat -N PORTAL_REDIRECT;" + IPTABLES + "-t nat -N ACCESS_LIST;" + IPTABLES + "-t nat -N WHITE_LIST;" + IPTABLES + "-t nat -N CAPTIVE_PORTAL;" + IPTABLES + "-t nat -A CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p udp --dport 67 -j ACCEPT;" + IPTABLES + "-t nat -A CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 67 -j ACCEPT;" + IPTABLES + "-t nat -A CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport " + Constants.SERVER_PORT_ACCESS_WEBSERVER + " -j REDIRECT --to-port " + Constants.SERVER_PORT_ACCESS_WEBSERVER + ";" + IPTABLES + "-t nat -A CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 80 -j REDIRECT --to-port " + Constants.SERVER_PORT_CAPTIVE_WEBSERVER + ";" + IPTABLES + "-t nat -A CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 443 -j REDIRECT --to-port " + Constants.SECURE_SERVER_PORT_CAPTIVE_WEBSERVER + ";" + IPTABLES + "-t nat -A PREROUTING -j PORTAL_REDIRECT;" + IPTABLES + "-t nat -A PREROUTING -j ACCESS_LIST;" + IPTABLES + "-t nat -A PREROUTING -j WHITE_LIST;" + IPTABLES + "-t nat -A PREROUTING -j CAPTIVE_PORTAL;" + IPTABLES + "-t nat -A PREROUTING -j DROP;");
        String dns_server = ACRAConstants.DEFAULT_STRING_VALUE;
        try {
            String domain = "www.google.com";
            String[] servers = new String[]{Constants.FATTIN_DNS2_SERVER, Constants.FATTIN_DNS2_SERVER};
            ExtendedResolver resolver = new ExtendedResolver(servers);
            for (String server : servers) {
                resolver = new ExtendedResolver(servers);
                resolver.setPort(Constants.FATTIN_DNS_PORT);
                resolver.setRetries(3);
                Lookup l = new Lookup(domain, 1);
                l.setResolver(resolver);
                Log.m2i(LOG_TAG, "Trying to reach name server: " + server);
                l.run();
                if (l.getResult() == 0) {
                    Log.m2i(LOG_TAG, new StringBuilder(String.valueOf(server)).append(" name server is reachable").toString());
                    dns_server = server;
                } else {
                    Log.m2i(LOG_TAG, new StringBuilder(String.valueOf(server)).append(" name server is NOT reachable").toString());
                }
                if (!dns_server.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
        }
        return (true & add_name_server_rules(dns_server)) & add_nat_rules_whitelist();
    }

    private static boolean add_name_server_rules(String dns_server) {
        Log.m2i(LOG_TAG, "'" + dns_server + "' is the dns server.");
        try {
            if (dns_server.isEmpty()) {
                throw new Exception("dns server variable is empty !");
            } else if (dns_server.matches(AccessListControl.IPADDRESS_PATTERN)) {
                CoreTask.runRootCommand(IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p udp --dport 53 -j DNAT --to-destination " + dns_server + ":" + Constants.FATTIN_DNS_PORT + ";" + IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 53 -j DNAT --to-destination " + dns_server + ":" + Constants.FATTIN_DNS_PORT + ";");
                return true;
            } else {
                Record[] records = dnsLookup(dns_server);
                if (records != null) {
                    String dnsip = records[0].rdataToString();
                    CoreTask.runRootCommand(IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p udp --dport 53 -j DNAT --to-destination " + dnsip + ":" + Constants.FATTIN_DNS_PORT + ";" + IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 53 -j DNAT --to-destination " + dnsip + ":" + Constants.FATTIN_DNS_PORT + ";");
                    return true;
                }
                throw new Exception("dns records variable is null !");
            }
        } catch (Exception e) {
            Log.m1e(LOG_TAG, e.getMessage(), e);
            try {
                CoreTask.runRootCommand(IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p udp --dport 53 -j ACCEPT;" + IPTABLES + "-t nat -I CAPTIVE_PORTAL -m state --state NEW,ESTABLISHED,RELATED,INVALID -p tcp --dport 53 -j ACCEPT;");
                return true;
            } catch (Exception e1) {
                Log.m1e(LOG_TAG, e1.getMessage(), e1);
                return false;
            }
        }
    }

    private static boolean add_nat_rules_whitelist() {
        Log.m0d(LOG_TAG, "add_nat_rules_whitelist -> begin");
        String command = ACRAConstants.DEFAULT_STRING_VALUE;
        try {
            RestResponse response = APIs.system_get_allowed_ips();
            if (response.isResultSuccess()) {
                ArrayList<String> ips = BusinessLogic.convertJSONArrayToArrayList((JSONArray) response.getOutput());
                Log.m0d(LOG_TAG, "JSONArray=" + ((JSONArray) response.getOutput()).toString());
                Collection<String> whitelisted_ips = WhiteListDatabase.getAllIPsData();
                Log.m0d(LOG_TAG, "whitelisted_ips=" + whitelisted_ips);
                Iterator<String> iterator = whitelisted_ips.iterator();
                while (iterator.hasNext()) {
                    String whitelisted_ip = (String) iterator.next();
                    if (!ips.contains(whitelisted_ip)) {
                        command = new StringBuilder(String.valueOf(command)).append(removeIPfromWhiteListCommand(whitelisted_ip)).toString();
                        iterator.remove();
                        Log.m0d(LOG_TAG, "removing ip " + whitelisted_ip);
                    }
                }
                Log.m0d(LOG_TAG, "whitelisted_ips=" + whitelisted_ips);
                update_nat_rules_output_file();
                Iterator it = ips.iterator();
                while (it.hasNext()) {
                    String ip = (String) it.next();
                    if (ip.matches(AccessListControl.IPADDRESS_PATTERN) && !whitelisted_ips.contains(ip)) {
                        WhiteListDatabase.setIPData(ip);
                        command = new StringBuilder(String.valueOf(command)).append(addIPtoWhiteListCommand(ip)).toString();
                    }
                }
            }
            if (command.isEmpty()) {
                return true;
            }
        } catch (APIUserAuthTokenInvalidException e) {
            Log.m1e(LOG_TAG, "APIUserAuthTokenInvalidException", e);
        } catch (Exception e2) {
            Log.m1e(LOG_TAG, e2.getMessage(), e2);
        }
        Log.m0d(LOG_TAG, "add_nat_rules_whitelist -> end");
        return CoreTask.runRootCommand(command);
    }

    private static String addIPtoWhiteListCommand(String ip) {
        Log.m0d(LOG_TAG, "addIPtoWhiteListCommand -> ip " + ip);
        return new StringBuilder(String.valueOf(ACRAConstants.DEFAULT_STRING_VALUE + IPTABLES + "-t nat -A WHITE_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -d " + ip + " -j ACCEPT;")).append(IPTABLES).append("-I ACCESS_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -d ").append(ip).append(" -j RETURN;").toString();
    }

    private static String removeIPfromWhiteListCommand(String ip) {
        Log.m0d(LOG_TAG, "removeIPfromWhiteListCommand -> ip " + ip);
        return new StringBuilder(String.valueOf(ACRAConstants.DEFAULT_STRING_VALUE + IPTABLES + "-t nat -D WHITE_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -d " + ip + " -j ACCEPT;")).append(IPTABLES).append("-D ACCESS_LIST -m state --state NEW,ESTABLISHED,RELATED,INVALID -d ").append(ip).append(" -j RETURN;").toString();
    }

    private static Record[] dnsLookup(String domain) throws Exception {
        Log.m2i(LOG_TAG, "dnsLookup");
        if (extResolver == null) {
            extResolver = new ExtendedResolver();
            extResolver.setRetries(3);
        }
        Lookup l = new Lookup(domain, 1);
        l.setResolver(extResolver);
        Log.m2i(LOG_TAG, new StringBuilder(String.valueOf(domain)).append(" resolving domain:").append(domain).toString());
        Record[] records = l.run();
        if (l.getResult() == 0) {
            Log.m2i(LOG_TAG, new StringBuilder(String.valueOf(domain)).append(" resolved").toString());
            return records;
        }
        MainApplication.getApplication().displayToastMessage("DNS could not resolve " + domain);
        Log.m2i(LOG_TAG, "Could not resolve domain:" + domain + " result:" + l.getResult() + " reason:" + l.getErrorString());
        throw new Exception("Could not resolve domain:" + domain + " result:" + l.getResult() + " reason:" + l.getErrorString());
    }

    private static boolean flush_nat_rules() {
        _check_common_fields();
        WhiteListDatabase.clear();
        return CoreTask.runRootCommand(IPTABLES + "-t nat -F INPUT;" + IPTABLES + "-t nat -F OUTPUT;" + IPTABLES + "-t nat -F PREROUTING;" + IPTABLES + "-t nat -F CAPTIVE_PORTAL;" + IPTABLES + "-t nat -X CAPTIVE_PORTAL;" + IPTABLES + "-t nat -F ACCESS_LIST;" + IPTABLES + "-t nat -X ACCESS_LIST;" + IPTABLES + "-t nat -F WHITE_LIST;" + IPTABLES + "-t nat -X WHITE_LIST;" + IPTABLES + "-t nat -F PORTAL_REDIRECT;" + IPTABLES + "-t nat -X PORTAL_REDIRECT;" + IPTABLES + "-D FORWARD -j ACCESS_LIST;" + IPTABLES + "-F ACCESS_LIST;" + IPTABLES + "-X ACCESS_LIST;");
    }
}
