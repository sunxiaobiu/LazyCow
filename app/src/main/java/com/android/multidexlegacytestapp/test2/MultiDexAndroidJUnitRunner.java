package com.android.multidexlegacytestapp.test2;

import android.os.Bundle;

import androidx.multidex.MultiDex;
import androidx.test.runner.AndroidJUnitRunner;

import tinker.sample.android.GlobalRef;

public class MultiDexAndroidJUnitRunner extends AndroidJUnitRunner {

  @Override
  public void onCreate(Bundle arguments) {
      MultiDex.installInstrumentation(GlobalRef.applicationContext, getTargetContext());
      super.onCreate(arguments);
  }

}
