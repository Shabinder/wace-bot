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
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.network.fold
import com.shabinder.wace.botUserName
import com.shabinder.wace.database.dbmethods.getGroupData
import com.shabinder.wace.database.dbtables.newGroup
import com.shabinder.wace.extensions.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception

fun groupGenericFun(dispatcher: Dispatcher) = dispatcher.apply {

    order("start") { bot, update -> //Bot Starting Command
        if (restrictAdminOnly(bot, update)) {
            initialiseBot(bot,update)
        }
    }

    order("pin") { bot, update ->
        if(restrictAdminOnly(bot,update)) {
            val notification = update.text().replace("/pin","",true).trim().equals("loud",true)
            val messageId = update.message!!.replyToMessage?.messageId
            messageId?.let {
                bot.pinChatMessage(
                        chatId = update.chatId(),
                        messageId = it,
                        disableNotification = !notification
                )
                return@order
            }
            bot.sendMessage(
                    chatId = update.chatId(),
                    replyToMessageId = update.messageId(),
                    text = "Reply To a Message , which needs to be pinned"
            )
        }
    }

    order("unpin") { bot, update ->
        if(restrictAdminOnly(bot,update)){
            bot.unpinChatMessage(
                    chatId = update.chatId()
            )
        }
    }

    order("setRules"){ bot, update ->
        if (restrictAdminOnly(bot, update)) {
            suspendGlobally {
                val rules = update.text().replace("/setrules", "",true).trim()
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = if(rules.isNotBlank()){
                            "<i>Rules</i> for this chat are now Set!"
                        } else{"Rules For This Chat are <b>Cleared</b>." +
                                "\n<i>syntax : / setrules {rules}.</i>"},
                        parseMode = ParseMode.HTML
                )
                val groupData = getGroupData(update.chatId())
                groupData.rules = rules
            }
        }
    }

    order("rules"){ bot, update ->
        showRules(bot,update)
    }

    order("report"){ bot, update ->
        val allAdmins = bot.getChatAdministrators(update.chatId()).first?.body()?.result?.map { it.user.username }
                ?: listOf()
        val response = bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId(),
                text = "<i> Reported to Admins! </i>" +
                        "Tagged Users : "+
                        allAdmins.joinToString { "@$it " },
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

    order("setWelcome") { bot, update ->
        if (restrictAdminOnly(bot, update)) {
            suspendGlobally {
                /* *******************Template!:*************************
                * use following to have the data in welcome message     *
                * {firstname}                                           *
                * {groupname}                                           *
                * {username}                                            *
                * *******************************************************/
                val welcomeMessage = update.text().replace("/setwelcome", "",true).trim()
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = if (welcomeMessage.isNotBlank()) {
                            "<i> $welcomeMessage </i> is Set " +
                                    "\n \n Use of <b>Templates</b> in Welcome Message is also Supported like:<i> \n" +
                                    "{firstname}\n" +
                                    "{groupname}\n" +
                                    "{username} </i>" +
                                    "\n \n To Disable just type / setwelcome without any message"
                        } else {
                            "Welcome Message is <b>Disabled</b>. \n To set type \n / setwelcome \"WelcomeMessage\" "
                        },
                        parseMode = ParseMode.HTML
                )
                val groupData = getGroupData(update.message!!.chat.id)
                groupData.welcomeMessage = welcomeMessage
            }
        }
    }

    text("Ping") { bot, update ->//message to check if WACE is Alive or Not!
        bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId(),
                text = "<i> Pong </i>",
                parseMode = ParseMode.HTML
        )
    }

    message(Filter.Group) { bot, update -> //Welcome New Members
        update.message!!.newChatMember?.let {
            if(it.username != botUserName){
                suspendGlobally {
                    val inlineKeyboardMarkup = InlineKeyboardMarkup(generateButtons())
                    val welcomeMessage = getGroupData(update.message!!.chat.id).welcomeMessage
                            .replace("{firstname}", it.firstName,true)
                            .replace("{username}", it.username ?: "",true)
                            .replace("{groupname}", update.message!!.chat.title ?: "",true)
                    if (welcomeMessage.isNotBlank()) {
                        bot.sendMessage(
                                chatId = update.chatId(),
                                replyToMessageId = update.messageId(),
                                text = welcomeMessage,
                                replyMarkup = inlineKeyboardMarkup
                        )
                    }
                }
            }else{//Bot Is Added in new Chat!
                initialiseBot(bot,update)
            }
        }
    }
    message(Filter.Group) { _, update -> //Bot Removal
        update.message!!.leftChatMember?.let {
            if(it.username == botUserName && it.isBot){
                suspendGlobally {
                    val groupData = getGroupData(update.chatId())
                    groupData.delete()//Clean Up.
                    //Bye Bye All group Data.
                }
            }
        }
    }

    callbackQuery("/rules"){ bot, update ->
        update.callbackQuery?.let {
            showRules(bot,update)
        }
    }
    callbackQuery("/notes"){ bot, update ->
        update.callbackQuery?.let {
            showNotes(bot,update)
        }
    }
    callbackQuery("/joinsubgroup"){ bot, update ->
        update.callbackQuery?.let {
            joinSubGroup(bot,update)
        }
    }

}

/*
* Returns True if user is Chat admin , Else Send Message Only Admin is authorised
* */
fun restrictAdminOnly(bot: Bot, update: Update):Boolean {
    val chatAdmins = bot.getChatAdministrators(update.chatId()).first?.body()?.result?.map { it.user.username }
            ?: listOf()
    if (!chatAdmins.contains(update.message?.from?.username)) {//User is Not Admin.
        bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId(),
                text = "Only Admin is Authorised to run this Command."
        )
        return false
    }
    return true
}

fun showRules(bot: Bot,update: Update){
    suspendGlobally {
        val rules = getGroupData(update.chatId()).rules
        bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId(),
                text = if(rules.isNotBlank()){
                    rules
                }else{"Rules Not yet Set For This Chat Yet" +
                        "\n<i>syntax : / setrules {rules}.</i>"},
                parseMode = ParseMode.HTML
        )
    }
}

fun initialiseBot(bot:Bot,update: Update){
    bot.sendMessage(
            chatId = update.chatId(),
            replyToMessageId = update.messageId(),
            text = "<b>I am Alive!,</b> ${update.message?.from?.firstName} "
                    + if (update.message!!.chat.type.contains("group")) {
                "\n<i><b>Details:</b></i>\n" +
                        "<b>Title :</b> <i> ${update.message!!.chat.title.toString()}</i> \n" +
                        "<b>Link :</b>  <i> ${update.message!!.chat.username}</i> \n" +
                        "<b>ID :</b>  <i> ${update.message!!.chat.id}</i>"
            } else "",
            parseMode = ParseMode.HTML
    )
    if (update.message!!.chat.type.contains("group")) {//initialise in GroupTable
        try {
            transaction {//Keeping It Synchronously as Its Execution is Important!r
                newGroup(
                        update.chatId(),
                        update.message!!.chat.title.toString(),
                        update.message!!.chat.username
                )
            }
        } catch (e: Exception) {
            println("Error While Initialising Group :  " + e.message + "\n" + e.cause)
        }
    }
}