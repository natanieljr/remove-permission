package org.droidmate.analyzer.api

import net.dongliu.apk.parser.bean.ApkMeta

/**
 * Dummy APK metadata, used when no metadata is available on the APK
 */
class DummyApkMeta : ApkMeta() {
    init {

        this.packageName = "DUMMY"
    }
}
