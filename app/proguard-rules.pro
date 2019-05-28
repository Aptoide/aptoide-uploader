# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/neuro/programming/Android/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:


# For RoboSpice
-dontwarn android.support.**
-dontwarn com.sun.xml.internal.**
-dontwarn com.sun.istack.internal.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.springframework.**
-dontwarn java.awt.**
-dontwarn javax.security.**
-dontwarn java.beans.**
-dontwarn javax.xml.**
-dontwarn java.util.**
-dontwarn org.w3c.dom.**
-dontwarn com.google.common.**
-dontwarn com.octo.android.robospice.persistence.**

### Retrofit
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
   @retrofit.http.* <methods>;
}
-keepattributes Signature

### Jackson SERIALIZER SETTINGS
-keepclassmembers,allowobfuscation class * {
    @org.codehaus.jackson.annotate.* <fields>;
    @org.codehaus.jackson.annotate.* <init>(...);
}


### Uploader
-keep class pt.caixamagica.aptoide.uploader.** { *; }
-keep class com.google.android.vending.licensing.** { *; }

-ignorewarnings

-keep class android.support.v7.appcompat.** { *; }

# Fabric
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

-keepclassmembers class * implements java.io.Serializable {
     private static final java.io.ObjectStreamField[] serialPersistentFields;
     private void writeObject(java.io.ObjectOutputStream);
     private void readObject(java.io.ObjectInputStream);
     java.lang.Object writeReplace();
     java.lang.Object readResolve();
}

-keep class * {
    public private *;
}

