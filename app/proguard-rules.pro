# MyanCast — R8 / ProGuard rules
#
# Phase 5 ဆုံးဖြတ်ချက် — release မှာ `isMinifyEnabled = false` ထားတယ်
# (R8 က Firestore data class ကို ဖြတ်မိရင် demo ပျက်မယ်)။
# Minify ဖွင့်ချင်ရင် အောက်က rule တွေ လိုတယ် —

# Firestore က reflection နဲ့ no-arg constructor + property တွေကို သုံးတယ်
-keep class com.example.myancast.domain.model.** { *; }
-keepclassmembers class com.example.myancast.domain.model.** {
    public <init>();
}

# Room entity (KSP generated code က ဖတ်တယ်)
-keep class com.example.myancast.data.local.** { *; }

# Media3 MediaSessionService — manifest ကနေ နာမည်နဲ့ ခေါ်တယ်
-keep class com.example.myancast.player.PlaybackService { *; }

# Crash log မှာ line number မြင်ရအောင်
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
