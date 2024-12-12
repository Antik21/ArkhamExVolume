package application.console

import com.antik.utils.logger.Logger


class ConsoleLogger(private val id: String) : Logger {

    private companion object {
        const val LOG_TEMPLATE = "[%s:%s] %s"
    }


    override fun debug(tag: String, msg: String) {
        println(LOG_TEMPLATE.format(tag, id, msg))
    }

    override fun message(msg: String) {
        println(msg)
    }
}