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
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.shabinder.wace.database.dbmethods.getGroupData
import com.shabinder.wace.database.dbtables.newGroup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception

fun groupGenericFun(dispatcher: Dispatcher) = dispatcher.apply {

    command("start"){ bot, update -> //Bot Starting Command
        if(restrictAdminOnly(bot,update)) {
            bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    text = "<b>I am Alive!,</b> ${update.message?.from?.firstName} "
                            + if (update.message!!.chat.type.contains("group")) {
                        "\n<i><b>Group:</b></i>\n" +
                                "<b>Title :</b> <i> ${update.message!!.chat.title.toString()}</i> \n" +
                                "<b>Link :</b>  <i> ${update.message!!.chat.username}</i> \n" +
                                "<b>ID :</b>  <i> ${update.message!!.chat.id}</i>"
                    } else "",
                    parseMode = ParseMode.HTML,
                    replyToMessageId = update.message!!.messageId
            )
            if (update.message!!.chat.type.contains("group")) {//initialise in GroupTable
                try{
                    transaction {//Keeping It Synchronously as Its Execution is Important!r
                        newGroup(
                                update.message!!.chat.id,
                                update.message!!.chat.title.toString(),
                                update.message!!.chat.username
                        )
                    }
                }catch (e:Exception){
                    println("Error While Initialising Group :  "+e.message +"\n" + e.cause)
                }
            }
        }
    }

    command("setwelcome"){ bot, update ->
        if(restrictAdminOnly(bot,update)){
            GlobalScope.launch {
                newSuspendedTransaction {
                    /*
                * Template!:
                * use following to have the data in welcome message
                * {firstname}
                * {groupname}
                * {username}
                * */
                    val welcomeMessage = update.message!!.text.toString().replace("/setwelcome", "").trim()
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
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
    }

    text("ping"){ bot, update ->//message to check if WACE is Alive or Not!
        bot.sendMessage(
                chatId = update.message!!.chat.id,
                replyToMessageId = update.message!!.messageId,
                text = "<i> Pong </i>",
                parseMode = ParseMode.HTML
        )
    }

    message(Filter.Group){ bot, update -> //Welcome New Members
        update.message!!.newChatMember?.let {
            GlobalScope.launch {
                newSuspendedTransaction {
                    val welcomeMessage = getGroupData(update.message!!.chat.id).welcomeMessage
                            .replace("{firstname}",it.firstName)
                            .replace("{username}",it.username?:"")
                            .replace("{groupname}",update.message!!.chat.title ?: "")
                    if(welcomeMessage.isNotBlank()){
                        bot.sendMessage(
                                chatId = update.message!!.chat.id,
                                text = welcomeMessage,
                                replyToMessageId = update.message!!.messageId,
                        )
                    }
                }
            }
        }
    }
}


/*
* Returns True if user is Chat admin , Else Send Message Only Admin is authorised
* */
fun restrictAdminOnly(bot: Bot, update: Update):Boolean{
    val chatAdmins = bot.getChatAdministrators(update.message!!.chat.id).first?.body()?.result?.map { it.user.username } ?: listOf()
    if(!chatAdmins.contains(update.message?.from?.username)){//User is Not Admin.
        bot.sendMessage(
                chatId = update.message!!.chat.id,
                replyToMessageId = update.message!!.messageId,
                text = "Only Admin is Authorised to run this Command."
        )
        return false
    }
    return true
}