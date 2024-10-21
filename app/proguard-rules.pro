# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# getactivity.xxpermissions
-keep class com.hjq.permissions.** {*;}
# getactivity.easywindow
-keep class com.hjq.window.** {*;}
# getactivity.gsonfactory
-keep class com.hjq.gson.factory.** {*;}
# simplecityapps.recyclerview.fastscroll
-keep class com.simplecityapps.** {*;}
# 避免 Gson 反序列化失败
-keep class pansong291.piano.wizard.entity.** {*;}
# com.xw.repo.bubbleseekbar
-keep class com.xw.repo.** {*;}
