package com.mhandharbeni.benibeacon.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.estimote.coresdk.recognition.packets.Beacon;
import com.mhandharbeni.benibeacon.BuildConfig;

import org.altbeacon.beacon.BeaconParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Constant {
    public static String BEACON_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    public static String IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static String ALTBEACON_LAYOUT = BeaconParser.ALTBEACON_LAYOUT;
    public static String EDDYSTONE_UID_LAYOUT = BeaconParser.EDDYSTONE_UID_LAYOUT;
    public static String EDDYSTONE_URL_LAYOUT = BeaconParser.EDDYSTONE_URL_LAYOUT;
    public static String EDDYSTONE_TLM_LAYOUT = BeaconParser.EDDYSTONE_TLM_LAYOUT;


    public static String SNBeacon;
    public static List<Beacon> listSNBeacon = new ArrayList<>();
    public static List<Beacon> listNativeBeacon = new ArrayList<>();

    private static int countSetBeacon = 0;
    private static int countSetNativeBeacon = 0;
    private static int countSetQRcode = 0;


    public static int NOTIF_BEACON_FOREGROUNDID = 2019;

    private static Integer MAX_COUNTER_BEACON = 150;



    public static void setCountSetBeacon(int countSetBeacon) {
        Constant.countSetBeacon = countSetBeacon;
    }




    public static void setListSNBeacon(List<com.estimote.coresdk.recognition.packets.Beacon> listSNBeacon) {
        if (listSNBeacon !=null && listSNBeacon.size() > 0){
            setCountSetBeacon(0);
        }
        Constant.listSNBeacon = listSNBeacon;
    }

    public static void addEachNativeBeacon(Beacon beacon){
        if (checkListBeacon(beacon.getMacAddress().toString()).size() < 1){
            listNativeBeacon.add(beacon);
        }
        setListNativeBeacon(listNativeBeacon);
        sortNativeBeacon();
        Constant.setListSNBeacon(Constant.listNativeBeacon);
    }

    private static List<Beacon> checkListBeacon(String dBeacon){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return Constant.listNativeBeacon.stream()
//                    .filter(beacon -> beacon.getMacAddress().toString().equalsIgnoreCase(dBeacon))
//                    .collect(Collectors.toList());
//        }else{
        List<Beacon> results = new ArrayList<>();
        for (Beacon beacon : Constant.listNativeBeacon){
            if (beacon.getMacAddress().toString().equalsIgnoreCase(dBeacon)){
                results.add(beacon);
            }
        }
        return results;
//        }
    }
    public static void setListNativeBeacon(List<Beacon> listNativeBeacon){
        if (listNativeBeacon != null && listNativeBeacon.size() > 0){
            setCountSetNativeBeacon(0);
        }
        Constant.listNativeBeacon = listNativeBeacon;
    }

    public static int getCountSetNativeBeacon(){
        return countSetNativeBeacon;
    }

    public static void setCountSetNativeBeacon(int countSetNativeBeacon){
        Constant.countSetNativeBeacon = countSetNativeBeacon;
    }
    public static void sortNativeBeacon(){
        Collections.sort(Constant.listNativeBeacon, new SortRssi());
    }


    public static List<Beacon> getListSNBeacon() {
        List<Beacon> returns = new ArrayList<>(Constant.listSNBeacon);
        if (Constant.listSNBeacon.size() < 4){
            returns.clear();
            returns.addAll(Constant.listSNBeacon);
        } else {
            returns.clear();
            for (int i=0;i<4;i++){
                returns.add(Constant.listSNBeacon.get(i));
            }
        }
        if (Constant.getCountSetBeacon() <= Constant.MAX_COUNTER_BEACON){
            setCountSetBeacon(getCountSetBeacon()+1);
        }else{
            setCountSetBeacon(0);
            setListSNBeacon(new ArrayList<>());
        }
        return returns;
    }
    public static int getCountSetBeacon() {
        return countSetBeacon;
    }

    static class SortRssi implements Comparator<Beacon>
    {
        @Override
        public int compare(Beacon a, Beacon b) {
            return b.getRssi() - a.getRssi();
        }
    }


    public static List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart")),
            new Intent("com.meizu.safe.security.SHOW_APPSEC").addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID)
    );
    public static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

}
