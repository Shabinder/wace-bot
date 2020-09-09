/****************************************************************************************
 *                                                                                      *
 *  * Copyright (C)  2020  Shabinder Singh                                              *
 *  *                                                                                   *
 *  * This program is free software: you can redistribute it and/or modify              *
 *  * it under the terms of the GNU General Public License as published by              *
 *  * the Free Software Foundation, either version 3 of the License, or                 *
 *  * (at your option) any later version.                                               *
 *  *                                                                                   *
 *  * This program is distributed in the hope that it will be useful,                   *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of                    *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                     *
 *  * GNU General Public License for more details.                                      *
 *  *                                                                                   *
 *  * You should have received a copy of the GNU General Public License                 *
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.            *
 *                                                                                      *
 ****************************************************************************************/

package com.shabinder.wace.extensions

import com.github.kotlintelegrambot.HandleUpdate
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.Update
import com.shabinder.wace.models.SubGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Update.chatId() = this.message?.chat?.id ?: this.callbackQuery!!.message!!.chat.id
fun Update.messageId() = this.message?.messageId ?: this.callbackQuery!!.message!!.messageId

fun Update.text() = this.message!!.text.toString()

//Multiple Command Handlers
fun Dispatcher.order(command: List<String>, body: HandleUpdate) {
    for (s in command) {
        addHandler(CustomCommandHandler(s, body))
    }
}

fun Dispatcher.order(command: String, body: HandleUpdate) {
    addHandler(CustomCommandHandler(command, body))
}


fun generateButtons(): List<List<InlineKeyboardButton>> {
    return listOf(
            listOf(InlineKeyboardButton(text = "Rules", callbackData = "/rules"),InlineKeyboardButton(text = "Notes", callbackData = "/notes")),
            listOf(InlineKeyboardButton(text = "Join a SubGroup", callbackData = "/joinsubgroup"))
    )
}


/*
 Using GlobalScope
 As Bot will continue running ,
 So lifetime handling isn't Necessary as DB fun completes in less than 10 sec
 Using Dispatcher.IO  ,as all fun are not CPU intensive
 So using IO Dispatcher Makes Sense as its Optimised for IO less cpu intensive fun
*/
fun suspendGlobally(func: () -> Unit) {
    GlobalScope.launch {
        newSuspendedTransaction(Dispatchers.IO) {
            //Handles DB Operations Too if present.
            func()
        }
    }
}

//Access Private Fields of a Class [Helpful to use in Libraries]
fun<T: Any> T.accessField(fieldName: String): Any? {
    return javaClass.getDeclaredField(fieldName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

