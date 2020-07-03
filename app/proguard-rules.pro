# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/neuro/programming/Android/android-sdk-linux/tools/proguard/proguard-android.txt
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

### Glide ###
-keep class androidx.v7.appcompat.** { *; }

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-keepattributes Exceptions, Signature, LineNumberTable, InnerClasses

-keep class com.aptoide.uploader.** { *; }

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**


-keepclassmembers class * implements java.io.Serializable {
     private static final java.io.ObjectStreamField[] serialPersistentFields;
     private void writeObject(java.io.ObjectOutputStream);
     private void readObject(java.io.ObjectInputStream);
     java.lang.Object writeReplace();
     java.lang.Object readResolve();
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep class * {
    public private *;
}

-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * {
   public <init>(android.content.Context, android.util.AttributeSet, int);
 }

 -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
  public static final *** NULL;
 }

 -keepnames @com.google.android.gms.common.annotation.KeepName class *
 -keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
  }

 -keepnames class * implements android.os.Parcelable {
  public static final ** CREATOR;
 }

 #rakam
 -keep class com.google.android.gms.ads.** { *; }
 -dontwarn okio.**

 #Moshi
 # JSR 305 annotations are for embedding nullability information.
 -dontwarn javax.annotation.**

 -keepclasseswithmembers class * {
     @com.squareup.moshi.* <methods>;
 }

 -keep @com.squareup.moshi.JsonQualifier interface *

 # Enum field names are used by the integrated EnumJsonAdapter.
 # values() is synthesized by the Kotlin compiler and is used by EnumJsonAdapter indirectly
 # Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
 -keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
     <fields>;
     **[] values();
 }

 -keep class kotlin.reflect.jvm.internal.impl.builtins.BuiltInsLoaderImpl
 -keepclassmembers class kotlin.Metadata {
     public <methods>;
 }

 -keep class com.aptoide.** {*;}