package org.droidmate.analyzer.tools;

import net.dongliu.apk.parser.bean.ApkMeta;

/**
 * Dummy APK metadata, used when no metadata is available on the APK
 */
public class DummyApkMeta extends ApkMeta
{
  public DummyApkMeta()
  {
    super();

    this.setPackageName("DUMMY");
  }
}
