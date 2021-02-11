package com.aptoide.uploader.apps;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class ObbTypeConverter {

  @TypeConverter public static List<Obb> restoreObbList(String listOfString) {
    return new Gson().fromJson(listOfString, new TypeToken<List<Obb>>() {
    }.getType());
  }

  @TypeConverter public static String saveObbList(List<Obb> listOfString) {
    return new Gson().toJson(listOfString);
  }
}
