package org.apache.logging.log4j.core

import android.content.Context
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.ConfigurationFactory
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.plugins.Plugin
import java.net.URI

@Plugin(category = ConfigurationFactory.CATEGORY, name = "AndroidConfigurationFactory")
class AndroidConfigurationFactory: ConfigurationFactory() {
    private val context: Context
        get() = AndroidConfigurationFactory.context

    private val underlyingFactory
        get() = ConfigurationFactory.getInstance()


    override fun getInputFromResource(resource: String, loader: ClassLoader?): ConfigurationSource {
        return ConfigurationSource(context.assets.open(resource))
    }

    override fun getInputFromString(config: String, loader: ClassLoader?): ConfigurationSource {
        return ConfigurationSource(context.assets.open(config))
    }

    override fun getInputFromUri(configLocation: URI): ConfigurationSource {
        return ConfigurationSource(context.assets.open(configLocation.toString()))
    }

    override fun getSupportedTypes(): Array<String>? = null
    override fun getConfiguration(source: ConfigurationSource): Configuration =
            underlyingFactory.getConfiguration(source)

    companion object {
        private lateinit var context: Context

        fun initialize(context: Context) {
            this.context = context
        }
    }
}