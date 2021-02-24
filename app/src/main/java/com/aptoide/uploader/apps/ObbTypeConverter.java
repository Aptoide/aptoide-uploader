package com.aptoide.uploader.apps;

import androidx.room.TypeConverter;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class ObbTypeConverter {

  private static final Moshi moshi = new Moshi.Builder().build();
  private static final JsonAdapter<List<Obb>> jsonAdapter = moshi.adapter((Type) Obb.class);

  @TypeConverter public static List<Obb> restoreObbList(String listOfString) throws IOException {
    return jsonAdapter.fromJson(listOfString);
  }

  @TypeConverter public static String saveObbList(List<Obb> listOfString) {
    return jsonAdapter.toJson(listOfString);
  }
}
