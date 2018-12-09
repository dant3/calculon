package org.apache.logging.log4j.core.appender

import android.util.Log
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.AbstractLifeCycle
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.core.util.Booleans
import java.io.Serializable
import java.nio.charset.Charset

@Plugin(name = "Logcat", category = "Core", elementType = "appender", printObject = true)
class LogcatAppender(name: String, layout: Layout<out Serializable>, filter: Filter? = null, ignoreExceptions: Boolean = true)
    : AbstractAppender(name, filter, layout, ignoreExceptions) {

    override fun append(event: LogEvent) {
        val priority = event.level.toLogcatPriority()
        if (priority > 0) {
            val message = layout.toByteArray(event)
            Log.println(priority, event.loggerName, message.toString(charset = Charset.defaultCharset()))
        }
    }

    private fun Level.toLogcatPriority() = when (this) {
        Level.TRACE -> Log.VERBOSE
        Level.DEBUG -> Log.DEBUG
        Level.INFO -> Log.INFO
        Level.WARN -> Log.WARN
        Level.FATAL -> Log.ERROR
        Level.ERROR -> Log.ERROR
        else -> 0
    }

    /**
     * Create a Console Appender.
     * @param layout The layout to use (required).
     * @param filter The Filter or null.
     * @param name The name of the Appender (required).
     * @param ignore If `"true"` (default) exceptions encountered when appending events are logged; otherwise
     * they are propagated to the caller.
     * @return The ConsoleAppender.
     */
    fun createAppender(
            @PluginElement("Layout") layout: Layout<out Serializable>?,
            @PluginElement("Filter") filter: Filter,
            @PluginAttribute("name") name: String?,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) ignore: String): LogcatAppender? {
        if (name == null) {
            AbstractLifeCycle.LOGGER.error("No name provided for ConsoleAppender")
            return null
        }
        val ignoreExceptions = Booleans.parseBoolean(ignore, true)
        return LogcatAppender(name, layout ?: PatternLayout.createDefaultLayout(), filter, ignoreExceptions)
    }
}