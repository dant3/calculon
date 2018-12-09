package org.apache.logging.log4j.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri




class AndroidConfigurationContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        AndroidConfigurationFactory.initialize(context.applicationContext)
        return false
    }

    override fun query(uri: Uri, projection: Array<String>,
                       selection: String,
                       selectionArgs: Array<String>,
                       sortOrder: String): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues): Uri? = null
    override fun delete(uri: Uri, selection: String, selectionArgs: Array<String>): Int = 0
    override fun update(uri: Uri, values: ContentValues, selection: String, selectionArgs: Array<String>): Int = 0


    override fun attachInfo(context: Context, providerInfo: ProviderInfo) {
        // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
        if (providerInfo.authority == "com.github.dant3.log4j2android.AndroidConfigurationContentProvider") {
            throw IllegalStateException("Incorrect provider authority in manifest. Most likely due to a "
                    + "missing applicationId variable in application\'s build.gradle.")
        }
        super.attachInfo(context, providerInfo)
    }
}
