package com.psycoguana.nfcfixer

import android.nfc.Tag
import android.nfc.tech.NfcA
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class Xposed : IXposedHookLoadPackage {
    /*
    This module intends to be a workaround for this bug:
    https://gitlab.com/LineageOS/issues/android/-/issues/2795
    which causes a few apps to throw NPE in Android 10+
     */
    private val packageName = "com.sube.cargasube"
    private val classToHook = "android.nfc.tech.NfcA"

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != packageName) {
            return
        }
        XposedBridge.log("Hooked!" + lpparam.packageName)

        val nfcClass = XposedHelpers.findClass(classToHook, lpparam.classLoader)
        val nfcConstructor = XposedHelpers.findConstructorExact(nfcClass, Tag::class.java)

        XposedHelpers.findAndHookMethod(
            classToHook,
            lpparam.classLoader,
            "get",
            Tag::class.java,
            object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): NfcA {
                    XposedBridge.log("NfcA->get hooked!")

                    val tag = param.args[0] as Tag
                    val newTag = TagUtil().cleanupTag(tag)
                    return nfcConstructor.newInstance(newTag) as NfcA
                }
            }
        )
    }
}