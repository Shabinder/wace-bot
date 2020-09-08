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

import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.network.fold
import com.shabinder.wace.database.dbmethods.*
import com.shabinder.wace.models.SubGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

private val serializer = ListSerializer(SubGroup.serializer())


fun communityManagerFun(dispatcher: Dispatcher) = dispatcher.apply {

    text{ bot, update -> //Tag Various Sub-Groups Members.
        GlobalScope.launch {
            newSuspendedTransaction {
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
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = "All <b> ${allTags.joinToString(",",transform = {s -> s.capitalize()})} </b> Devs Are Notified!" +
                                    "Tagged Users : "+
                                    usersToTag.joinToString { "@$it " },
                            parseMode = ParseMode.HTML
                    )
                    response.fold({responseSuccess ->//Users tagged ,Now Time for CleanUp!
                        bot.editMessageText(
                                chatId = update.message!!.chat.id,
                                messageId = responseSuccess?.result?.messageId,
                                text = responseSuccess?.result?.text.toString().substringBefore("Tagged Users")
                        )
                    })
                }
            }
        }
    }

    command("createsubgroup"){ bot,update ->
        if(restrictAdminOnly(bot,update)) {
            GlobalScope.launch {
                newSuspendedTransaction {
                    val subgroupName = update.message?.text.toString().substringAfter("/createsubgroup").trim()
                    val isCreated = createSubGroup(update.message!!.chat.id, subgroupName)
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
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
    }

    command("removesubgroup"){bot, update ->
        if(restrictAdminOnly(bot,update)) {
            GlobalScope.launch {
                newSuspendedTransaction {
                    val subGroupName = update.message?.text.toString().substringAfter("/removesubgroup").trim()
                    if (subGroupName.isBlank()) {
                        bot.sendMessage(
                                chatId = update.message!!.chat.id,
                                replyToMessageId = update.message!!.messageId,
                                text = "Enter Sub-Group Name To Delete!,\n <i>syntax = / removesubgroup {Sub-Group's Name} </i>",
                                parseMode = ParseMode.HTML
                        )
                        return@newSuspendedTransaction //Syntax Error
                    }

                    val isRemoved = removeSubGroup(update.message!!.chat.id, subGroupName)
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
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
    }

    command("listsubgroups"){ bot, update ->
        GlobalScope.launch {
            newSuspendedTransaction {
                val subGroupMap = listSubGroups(update.message!!.chat.id)
                bot.sendMessage(
                        chatId = update.message!!.chat.id,
                        replyToMessageId = update.message!!.messageId,
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
    }

    command("addme"){bot, update ->
        GlobalScope.launch {
            newSuspendedTransaction {
                val username = update.message!!.from?.username
                val subGroupName = update.message!!.text?.substringAfter("/addme")?.trim()
                if(subGroupName.isNullOrBlank() || username.isNullOrBlank()){
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = "Follow <b> Proper Syntax! </b>,\n / <i> addme <SubGroupName> </i> \n And you must have set a UserName in Telegram",
                            parseMode = ParseMode.HTML
                    )
                    return@newSuspendedTransaction
                }
                val isAdded = addMemberToSubGroup(update.message!!.chat.id,subGroupName,username)
                bot.sendMessage(
                        chatId = update.message!!.chat.id,
                        replyToMessageId = update.message!!.messageId,
                        text = if(isAdded){
                            "U have joined <b> $subGroupName </b>!"
                        }else{"It Seems $subGroupName doesnt Exist!"},
                        parseMode = ParseMode.HTML
                )
            }
        }
    }
    command("removeme"){bot, update ->
        GlobalScope.launch {
            newSuspendedTransaction {
                val username = update.message!!.from?.username
                val subGroupName = update.message!!.text?.substringAfter("/removeme")?.trim()
                if(subGroupName.isNullOrBlank() || username.isNullOrBlank()){
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = "Follow <b> Proper Syntax! </b>,\n / <i> removeme <SubGroupName> </i>",
                            parseMode = ParseMode.HTML
                    )
                    return@newSuspendedTransaction
                }
                val isRemoved = removeMembersFromSubGroup(update.message!!.chat.id,subGroupName,username)
                bot.sendMessage(
                        chatId = update.message!!.chat.id,
                        replyToMessageId = update.message!!.messageId,
                        text = if(isRemoved){
                            "U have Left <b> $subGroupName </b>!"
                        }else{"It Seems $subGroupName doesnt Exist!"},
                        parseMode = ParseMode.HTML
                )
            }
        }
    }
}