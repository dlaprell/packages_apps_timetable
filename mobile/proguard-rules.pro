# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/david/Programme/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-printmapping proguard.map

-renamesourcefileattribute ProGuard
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements eu.laprell.animation.ActivityTransitions.HeroTransitionInterface {
   public void setBackgroundColorAlpha(int);
}

-keepclassmembers class eu.laprell.timetable.animation.CircleColorDrawable {
    public float getAlphaF();
    public void setAlphaF(float);
    public float getCircleX();
    public void setCircleX(float);
    public float getCircleY();
    public void setCircleY(float);
    public float getRadius();
    public void setRadius(float);
}

-keepclassmembers class eu.laprell.timetable.animation.RippleDrawable {
    public void setAnimationStep(float);
}

-keepclassmembers class eu.laprell.animation.ActivityTransitions.SimpleWhiteBackgroundInterface {
    public void setBackgroundColorAlpha(int);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
