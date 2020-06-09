package com.aptoide.uploader

/**
 * Object to remap from Intent so we can decouple from the Android framework
 */
data class IntentData(val action: String, val data: String, val extras: HashMap<String, Any?>)