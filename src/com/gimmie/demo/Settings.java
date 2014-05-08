package com.gimmie.demo;

import java.math.BigInteger;
import java.security.SecureRandom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import com.gimmie.Configuration;
import com.gimmie.Gimmie;

public class Settings {

  private static final String CHECKIN_SETTING_KEY = "checkin";
  private static final String USERNAME_KEY = "username";
  private static final String UNLOCK_KEY = "unlock";

  private String mCheckinID;
  private String mDefaultUsername;

  private String mGimmieKey;
  private String mGimmieSecret;
  private String mGimmieURL;
  private String mGimmieCountry;

  private boolean mIsUnlocked;

  private SharedPreferences mSharedPreference;
  private Activity mActivity;
  private Configuration mConfiguration;
  private Gimmie mGimmie;

  public Settings(Activity activity) {
    mActivity = activity;
    mSharedPreference = activity.getPreferences(Context.MODE_PRIVATE);
    mGimmie = Gimmie.getInstance(activity);
    mConfiguration = mGimmie.getConfiguration();

    load();
  }

  public void setGimmie(Gimmie gimmie) {
    mGimmie = gimmie;
    mConfiguration = gimmie.getConfiguration();
  }

  public String getCheckinID() {
    return mCheckinID;
  }

  public void setCheckinID(String checkinID) {
    mCheckinID = checkinID;
  }

  public String getDefaultUsername() {
    return mDefaultUsername;
  }

  public void setDefaultUsername(String username) {
    mDefaultUsername = username;
  }

  public String getGimmieKey() {
    return mGimmieKey;
  }

  public void setGimmieKey(String gimmieKey) {
    mGimmieKey = gimmieKey;
  }

  public String getGimmieSecret() {
    return mGimmieSecret;
  }

  public void setGimmieSecret(String gimmieSecret) {
    mGimmieSecret = gimmieSecret;
  }

  public String getGimmieURL() {
    return mGimmieURL;
  }

  public void setGimmieURL(String gimmieURL) {
    mGimmieURL = gimmieURL;
  }

  public String getGimmieCountry() {
    return mGimmieCountry;
  }

  public void setGimmieCountry(String country) {
    mGimmieCountry = country;
  }

  public String getVersionNumber() {
    try {
      return mActivity.getPackageManager().getPackageInfo(
          mActivity.getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {
      return "Unknown";
    }
  }

  public boolean isUnlocked() {
    return mIsUnlocked;
  }

  public void unlock() {
    mIsUnlocked = true;
  }

  public void save() {
    SharedPreferences sharedPref = mActivity
        .getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPref.edit();
    editor.putString(CHECKIN_SETTING_KEY, mCheckinID);
    editor.putString(USERNAME_KEY, mDefaultUsername);
    editor.putBoolean(UNLOCK_KEY, mIsUnlocked);

    editor.putString(Configuration.API_KEY, mGimmieKey);
    editor.putString(Configuration.API_SECRET, mGimmieSecret);
    editor.putString(Configuration.API_URL, mGimmieURL);
    editor.putString(Configuration.DATA_COUNTRY, mGimmieCountry);

    editor.commit();
  }

  public void load() {
    mCheckinID = mSharedPreference.getString(CHECKIN_SETTING_KEY,
        "4-mayor-of-venue");
    mDefaultUsername = mSharedPreference.getString(USERNAME_KEY, "guest:"
        + new BigInteger(80, new SecureRandom()).toString());
    mIsUnlocked = mSharedPreference.getBoolean(UNLOCK_KEY, false);

    mGimmieKey = mSharedPreference.getString(Configuration.API_KEY,
        mConfiguration.getKey());
    mGimmieSecret = mSharedPreference.getString(Configuration.API_SECRET,
        mConfiguration.getSecret());
    mGimmieURL = mSharedPreference.getString(Configuration.API_URL,
        mConfiguration.getURL());
    mGimmieCountry = mSharedPreference.getString(Configuration.DATA_COUNTRY,
        mConfiguration.getDefaultCountry());

    mConfiguration.setKey(mGimmieKey);
    mConfiguration.setSecret(mGimmieSecret);
    mConfiguration.setURL(mGimmieURL);
    mGimmie.setCountry(mGimmieCountry);
    mGimmie.login(mDefaultUsername);

  }

}
