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

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.network.fold
import com.shabinder.wace.database.dbmethods.*
import com.shabinder.wace.extensions.*
import com.shabinder.wace.models.SubGroup
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val serializer = ListSerializer(SubGroup.serializer())

fun communityManagerFun(dispatcher: Dispatcher) = dispatcher.apply {

    text{ bot, update -> //Tag Various Sub-Groups Members
        suspendGlobally {
            val messageBody:String = update.message?.text.toString()
            val messageTags = messageBody.split("#").map{it.substringBefore(" ").trim().toLowerCase()}

            val groupData = getGroupData(update.message!!.chat.id)
            val allSubGroups = Json.decodeFromString(serializer,groupData.subGroups)
            val allTags = allSubGroups.map { it.subGroupName.toLowerCase() }.toMutableList()
            allTags.retainAll(messageTags)
            val usersToTag:MutableList<String> = mutableListOf()
            for(tag in allTags){
                usersToTag.addAll(allSubGroups[allSubGroups.indexOfFirst { it.subGroupName.equals(tag,true) }].members)
                println("Message TAG : $tag")
            }

            if(usersToTag.isNotEmpty()) {
                val response = bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "All <b> ${allTags.joinToString(",",transform = {s -> s.capitalize()})} </b> Devs Are Notified!" +
                                "Tagged Users : "+
                                usersToTag.joinToString { "@$it " },
                        parseMode = ParseMode.HTML
                )
                response.fold({responseSuccess ->//Users tagged ,Now Time for CleanUp!
                    bot.editMessageText(
                            chatId = update.chatId(),
                            messageId = responseSuccess?.result?.messageId,
                            text = responseSuccess?.result?.text.toString().substringBefore("Tagged Users")
                    )
                })
            }
        }
    }

    order("createSubGroup"){ bot, update ->
        if(restrictAdminOnly(bot,update)) {
            suspendGlobally {
                val subgroupName = update.message?.text.toString().replace("/createsubgroup","",true).trim()
                val isCreated = createSubGroup(update.message!!.chat.id, subgroupName)
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = if (isCreated) {
                            "<b> $subgroupName </b> SubGroup is Now Created."
                        } else {
                            "<b> $subgroupName </b> SubGroup Already Exists!"
                        },
                        parseMode = ParseMode.HTML
                )
            }
        }
    }

    order("removeSubGroup"){ bot, update ->
        if(restrictAdminOnly(bot,update)) {
            suspendGlobally {
                val subGroupName = update.message?.text.toString().replace("/removesubgroup","",true).trim()
                if (subGroupName.isBlank()) {
                    bot.sendMessage(
                            chatId = update.chatId(),
                            replyToMessageId = update.messageId(),
                            text = "Enter Sub-Group Name To Delete!,\n <i>syntax = / removesubgroup {Sub-Group's Name} </i>",
                            parseMode = ParseMode.HTML
                    )
                    return@suspendGlobally //Syntax Error in User Command
                }

                val isRemoved = removeSubGroup(update.message!!.chat.id, subGroupName)
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = if (isRemoved) {
                            "<b> $subGroupName </b> Sub Group is Now Removed."
                        } else {
                            "It Seems $subGroupName doesnt Exist!\""
                        },
                        parseMode = ParseMode.HTML
                )
            }
        }
    }
    order("joinSubGroup"){bot, update ->
        joinSubGroup(bot,update)
    }
    order("listSubGroups"){ bot, update ->
        suspendGlobally {
            val subGroupMap = listSubGroups(update.message!!.chat.id)
            bot.sendMessage(
                    chatId = update.chatId(),
                    replyToMessageId = update.messageId(),
                    text = if (subGroupMap.isEmpty()) {
                        "<b>No Sub-Group Exists in this Group</b>"
                    } else {
                        "<i><b>Name   :   Members</b></i>\n"+
                                subGroupMap.map { "${it.key}    :   ${it.value} \n" }.reduce { acc, s -> acc + s }
                    },
                    parseMode = ParseMode.HTML
            )
        }
    }

    order("addMe"){ bot, update ->
        suspendGlobally {
            val username = update.message!!.from?.username
            val subGroupName = update.message!!.text?.substringAfter("/addme")?.trim()
            if(subGroupName.isNullOrBlank() || username.isNullOrBlank()){
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "Follow <b> Proper Syntax! </b>,\n / <i> addme <SubGroupName> </i> \n And you must have set a UserName in Telegram",
                        parseMode = ParseMode.HTML
                )
                return@suspendGlobally
            }
            val isAdded = addMemberToSubGroup(update.message!!.chat.id,subGroupName,username)
            bot.sendMessage(
                    chatId = update.chatId(),
                    replyToMessageId = update.messageId(),
                    text = if(isAdded){
                        "U have joined <b> $subGroupName </b>!"
                    }else{"It Seems $subGroupName doesnt Exist!"},
                    parseMode = ParseMode.HTML
            )

        }
    }
    order("removeMe"){ bot, update ->
        suspendGlobally {
            val username = update.message!!.from?.username
            val subGroupName = update.message!!.text?.substringAfter("/removeme")?.trim()
            if(subGroupName.isNullOrBlank() || username.isNullOrBlank()){
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "Follow <b> Proper Syntax! </b>,\n / <i> removeme <SubGroupName> </i>",
                        parseMode = ParseMode.HTML
                )
                return@suspendGlobally
            }
            val isRemoved = removeMembersFromSubGroup(update.message!!.chat.id,subGroupName,username)
            bot.sendMessage(
                    chatId = update.chatId(),
                    replyToMessageId = update.messageId(),
                    text = if(isRemoved){
                        "U have Left <b> $subGroupName </b>!"
                    }else{"It Seems $subGroupName doesnt Exist!"},
                    parseMode = ParseMode.HTML
            )
        }
    }
}

fun joinSubGroup(bot: Bot,update:Update){
    suspendGlobally {
        val subGroupMap = listSubGroups(update.chatId())

        val inlineKeyboard = subGroupMap
                .map { InlineKeyboardButton(text = it.key.capitalize(),callbackData = "/addMe ${it.key}") }
                .chunked(2)

        bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId(),
                replyMarkup = InlineKeyboardMarkup(inlineKeyboard),
                text = if (subGroupMap.isEmpty()) {
                    "<b>No Sub-Group Exists in this Group</b>"
                } else {
                    "Join A Sub Group :"
                },
                parseMode = ParseMode.HTML
        )
    }
}