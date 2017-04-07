/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmateus on 21-05-2014.
 */
public class PaymentServices implements Parcelable {

  public static final Creator<PaymentServices> CREATOR = new Creator<PaymentServices>() {
    public PaymentServices createFromParcel(Parcel in) {
      return new PaymentServices(in);
    }

    public PaymentServices[] newArray(int size) {
      return new PaymentServices[size];
    }
  };
  public int id;
  public String short_name;
  public String name;
  public ArrayList<PaymentType> types;
  public double price;
  public String currency;
  public double taxRate;
  public String sign;

  public PaymentServices() {

  }

  private PaymentServices(Parcel in) {
    id = in.readInt();
    short_name = in.readString();
    name = in.readString();
    if (types == null) types = new ArrayList<PaymentType>();
    in.readList(types, PaymentType.class.getClassLoader());
    price = in.readDouble();
    currency = in.readString();
    taxRate = in.readDouble();
    sign = in.readString();
  }

  public double getPrice() {
    return price;
  }

  public String getCurrency() {
    return currency;
  }

  public double getTaxRate() {
    return taxRate;
  }

  public String getSign() {
    return sign;
  }

  public List<PaymentType> getTypes() {
    return types;
  }

  public int getId() {
    return id;
  }

  public String getShort_name() {
    return short_name;
  }

  public String getName() {
    return name;
  }

  @Override public int describeContents() {
    return 0;
  }

  public static class PaymentType implements Parcelable {

    public static final Creator<PaymentType> CREATOR = new Creator<PaymentType>() {
      public PaymentType createFromParcel(Parcel in) {
        return new PaymentType(in);
      }

      public PaymentType[] newArray(int size) {
        return new PaymentType[size];
      }
    };
    public String reqType;
    public String label;

    public PaymentType() {

    }

    private PaymentType(Parcel in) {
      reqType = in.readString();
      label = in.readString();
    }

    public String getReqType() {
      return reqType;
    }

    public String getLabel() {
      return label;
    }

    public void writeToParcel(Parcel out, int flags) {
      out.writeString(reqType);
      out.writeString(label);
    }

    @Override public int describeContents() {
      return 0;
    }
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(id);
    out.writeString(short_name);
    out.writeString(name);
    out.writeList(types);
    out.writeDouble(price);
    out.writeString(currency);
    out.writeDouble(taxRate);
    out.writeString(sign);
  }
}
