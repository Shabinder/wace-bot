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
import com.github.kotlintelegrambot.dispatcher.inlineQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.InlineQuery
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import com.shabinder.wace.models.PlatformModel

fun communityManagerFun(dispatcher: Dispatcher) = dispatcher.apply {
    text{ bot, update -> //Tag Various Communities Devs.
        val messageBody:String = update.message?.text.toString().toLowerCase()
        val messageTags = mutableListOf<String>()
        val allTags = PlatformModel.values().map{"#"+it.Name.toLowerCase()} //[#android, #ios, #java, #kotlin, #python, #general]

        for(tag in allTags){
            if(messageBody.contains(tag)){
                println("Message TAG : $tag")
                messageTags.add(tag.substring(1).capitalize())
            }
        }

        if(messageTags.isNotEmpty()) {
            bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    replyToMessageId = update.message!!.messageId,
                    text = "All <b> ${messageTags.joinToString(",")} </b> Devs Are Notified!", parseMode = ParseMode.HTML
            )
        }
    }
    inlineQuery { bot, inlineQuery: InlineQuery ->
        val queryText = inlineQuery.query

        if (queryText.isBlank() or queryText.isEmpty()) return@inlineQuery

        val inlineResults = (PlatformModel.values()).map {
            InlineQueryResult.Article(
                    id = it.ordinal.toString(),
                    title = it.Name,
                    inputMessageContent = InputMessageContent.Text(it.Name),
                    description = "Get Notified for ${it.Name} Queries!",
                    thumbHeight = 40,
                    thumbWidth = 50,
                    thumbUrl = it.thumbUrl
            )
        }
        bot.answerInlineQuery(inlineQuery.id, inlineResults)
    }
}