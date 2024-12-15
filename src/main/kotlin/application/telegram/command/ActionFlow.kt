package application.telegram.command

interface ActionFlow {
    fun getStartMessage() : String
    fun getCompleteMessage() : String
}