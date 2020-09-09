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

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ParseMode
import com.shabinder.wace.extensions.chatId
import com.shabinder.wace.database.dbmethods.blacklistWord
import com.shabinder.wace.database.dbmethods.clearBlacklist
import com.shabinder.wace.database.dbmethods.getAllBlackListedWords
import com.shabinder.wace.database.dbmethods.whitelistWord
import com.shabinder.wace.extensions.messageId
import com.shabinder.wace.extensions.order
import com.shabinder.wace.extensions.suspendGlobally

fun blacklistFun(dispatcher: Dispatcher) = dispatcher.apply {

    order("blacklist"){ bot, update ->
        if(restrictAdminOnly(bot,update)){
           suspendGlobally {
               val word = update.message!!.text.toString().replace("/blacklist", "",true).trim()
               bot.sendMessage(
                       chatId = update.chatId(),
                       replyToMessageId = update.messageId(),
                       text = "BlackListed \" $word \" ."
               )
               blacklistWord(update.message!!.chat.id, word)
           }
        }
    }

    order("whitelist"){ bot, update ->
        if(restrictAdminOnly(bot,update)){
            suspendGlobally {
                val word = update.message!!.text.toString().replace("/whitelist", "",true).trim()
                val whiteListed = whitelistWord(update.message!!.chat.id, word)
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = if (whiteListed) {
                        "WhiteListed \" $word \" ."
                    } else {
                        " \" $word \" wasn't BlackListed."
                    }
                )

            }
        }
    }

    order("clearBlacklist"){ bot, update ->
        if(restrictAdminOnly(bot,update)){
            suspendGlobally {
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "BlackList Cleared."
                )
                clearBlacklist(update.chatId())
            }
        }
    }

    order("getBlacklist"){ bot, update ->
        if(restrictAdminOnly(bot,update)){
           suspendGlobally {
               val blackList =  getAllBlackListedWords(update.message!!.chat.id)
               bot.sendMessage(
                       chatId = update.chatId(),
                       replyToMessageId = update.messageId(),
                       text = if (blackList.isNotEmpty()) {
                           "<b>BlackList:</b> \n ${blackList.reduce { acc, s -> acc + "\n" + s }}"
                       } else {
                           "BlackList is Empty!"
                       },
                       parseMode = ParseMode.HTML
               )
           }
        }
    }

    text{bot, update -> //Check For BlackListed Words and delete messages with those Words
        if(update.message?.text.toString().contains("/start",true))return@text//Group initialisation
        suspendGlobally {
            val blockedWords = getAllBlackListedWords(update.message!!.chat.id)
            for (word in blockedWords) {
                if (update.message!!.text.toString().contains(word))
                    bot.deleteMessage(update.chatId(),messageId = update.messageId())
                break
            }
        }
    }
}