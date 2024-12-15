package application.telegram.text_handler

import application.telegram.command.ActionFlow

interface InputTextHandler<T> : ActionFlow {
    fun handleInput(input: String)
    fun nextStepMessage(): String?
    fun isComplete(): Boolean
    fun getResult(): T?
}