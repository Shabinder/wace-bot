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

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.CommandHandleUpdate
import com.github.kotlintelegrambot.HandleUpdate
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update

class CustomCommandHandler(private val command :String, handler: HandleUpdate) : Handler(handler) {

    constructor(command: String, handler: CommandHandleUpdate) : this(command, CommandHandleUpdateProxy(handler))

    override val groupIdentifier: String = "CommandHandler"
    override fun checkUpdate(update: Update): Boolean {
        return (update.message?.text != null && (update.message?.text!!.startsWith("/") || update.message?.text!!.startsWith("@")) &&
                update.message?.text!!.drop(1).split(" ")[0].split("@")[0].equals(command,true))
    }
}

private class CommandHandleUpdateProxy(private val handleUpdate: CommandHandleUpdate) : HandleUpdate {
    override fun invoke(bot: Bot, update: Update) {
        handleUpdate(bot, update, update.message?.text?.split("\\s+".toRegex())?.drop(1) ?: listOf())
    }
}