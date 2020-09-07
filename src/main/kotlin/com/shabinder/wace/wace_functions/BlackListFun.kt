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

package com.shabinder.wace.wace_functions

import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.shabinder.wace.database.dbmethods.blacklistWord
import com.shabinder.wace.database.dbmethods.getAllBlackListedWords
import com.shabinder.wace.database.dbmethods.whitelistWord
import org.jetbrains.exposed.sql.transactions.transaction


fun blacklistFun(dispatcher: Dispatcher) = dispatcher.apply {
    command("blacklist"){bot, update ->
        val word = update.message!!.text.toString().replace("/blacklist","").trim()
        bot.sendMessage(
                chatId = update.message!!.chat.id,
                replyToMessageId = update.message!!.messageId,
                text = "BlackListed \" $word \" ."
        )
        transaction {
            blacklistWord(update.message!!.chat.id, word)
        }
    }
    command("whitelist"){bot, update ->
        val word = update.message!!.text.toString().replace("/whitelist","").trim()
        transaction {
            val whiteListed =  whitelistWord(update.message!!.chat.id, word)
            bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    replyToMessageId = update.message!!.messageId,
                    text = if(whiteListed){"WhiteListed \" $word \" ."}
                    else { " \" $word \" wasn't BlackListed."}
            )
        }
    }
    command("getBlacklist"){bot, update ->
        transaction {
            val blackList =  getAllBlackListedWords(update.message!!.chat.id)
            bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    replyToMessageId = update.message!!.messageId,
                    text = if (blackList.isNotEmpty() || !blackList[0].isBlank()) {
                        "BlackList \n ${blackList.reduce { acc, s -> acc + "\n" + s }}"
                    } else {
                        "BlackList is Empty!"
                    }
            )
        }
    }

}