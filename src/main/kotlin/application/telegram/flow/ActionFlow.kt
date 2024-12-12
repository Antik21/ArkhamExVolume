package application.telegram.flow

interface ActionFlow {
    fun getStartMessage() : String
    fun getCompleteMessage() : String
}