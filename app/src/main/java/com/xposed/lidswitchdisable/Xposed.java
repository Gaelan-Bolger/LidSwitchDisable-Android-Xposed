package com.xposed.lidswitchdisable;

import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xposed implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static final String PACKAGE_NAME = Xposed.class.getPackage().getName();
    private static final String TAG = "LidSwitchDisable";
    private static final boolean DEBUG = true;
    private static String MODULE_PATH = null;

    public static final String PREFERENCES = "preferences";

    private XSharedPreferences mPrefs;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        String packageName = lpParam.packageName;
        if (packageName.equals("android")) {
            log("Loaded Android");
            mPrefs = new XSharedPreferences(PACKAGE_NAME, PREFERENCES);
            mPrefs.makeWorldReadable();

            Class windowManagerPolicyClass;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                windowManagerPolicyClass = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager", lpParam.classLoader);
            else
                windowManagerPolicyClass = XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpParam.classLoader);

            if (null == windowManagerPolicyClass)
                return;

            try {
                XposedHelpers.findAndHookMethod(windowManagerPolicyClass, "applyLidSwitchState", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        mPrefs.reload();

                        if (mPrefs.getBoolean("disable_lid_switch", true)) {
                            XposedHelpers.setBooleanField(param.thisObject, "mLidControlsSleep", false);
                        } else {
                            XposedHelpers.setBooleanField(param.thisObject, "mLidControlsSleep", true);
                        }
                    }
                });
            } catch (Exception e) {
                log("HOOK FAILED: applyLidSwitchState");
            }

            try {
                XposedHelpers.findAndHookMethod(windowManagerPolicyClass, "notifyLidSwitchChanged", Long.TYPE, Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        mPrefs.reload();

                        if (mPrefs.getBoolean("disable_screen_on", false)) {
                            param.args[1] = Boolean.FALSE;
                        }
                    }
                });
            } catch (Exception e) {
                log("HOOK FAILED: notifyLidSwitchChanged");
            }
        }
    }

    private void log(String message) {
        if (DEBUG) XposedBridge.log(TAG + " : " + message);
    }
}
