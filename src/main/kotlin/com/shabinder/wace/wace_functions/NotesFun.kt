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
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.shabinder.wace.database.dbmethods.clearNotes
import com.shabinder.wace.database.dbmethods.deleteNote
import com.shabinder.wace.database.dbmethods.getNotesMap
import com.shabinder.wace.database.dbmethods.saveNote
import com.shabinder.wace.extensions.*

fun notesFun(dispatcher: Dispatcher) = dispatcher.apply{

    order("save"){ bot, update -> //Save New Note in DB
        if(restrictAdminOnly(bot,update)) {
            val message = update.text().replace("/save", "",true).trim()
            val noteName = message.substringBefore(" ")
            val noteData = message.substringAfter(noteName).trim()
            println("Command : /save : \n name = $noteName \n data = $noteData")
            if (noteData.isBlank() or noteName.isBlank()) {
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "Note is Empty, \n Try again {/save <noteName> <noteData>}"
                )
            }else{
                suspendGlobally {
                    saveNote(update.message!!.chat.id, noteName, noteData)
                    bot.sendMessage(
                            chatId = update.chatId(),
                            replyToMessageId = update.messageId(),
                            text = "/ $noteName saved."
                    )
                }
            }
        }
    }

    order("deleteNote") { bot, update -> //Delete Note from DB
        if(restrictAdminOnly(bot,update)) {
            val noteName = update.text().replace("/deleteNote", "",true).trim()
            println("Command : /deleteNote : \n name = $noteName ")
            if (noteName.isBlank()) {
                bot.sendMessage(
                        chatId = update.chatId(),
                        replyToMessageId = update.messageId(),
                        text = "Which Note to delete, \n Try again {/deleteNote <noteName>}"
                )
            } else {
                suspendGlobally {
                    val noteDeleted: Boolean? = deleteNote(update.message!!.chat.id, noteName)
                    bot.sendMessage(
                            chatId = update.chatId(),
                            replyToMessageId = update.messageId(),
                            text = when (noteDeleted) {
                                null -> {
                                    "$noteName note doesn't exist"
                                }
                                true -> {
                                    "/$noteName Deleted."
                                }
                                else -> {
                                    "/$noteName couldn't be deleted,an error occurred!"
                                }
                            }
                    )
                }
            }
        }
    }

    order("notes") { bot, update ->
        showNotes(bot,update)
    }

    order("clearNotes") { bot, update ->
        if(restrictAdminOnly(bot,update)) {
           suspendGlobally {
               clearNotes(update.message!!.chat.id)
               bot.sendMessage(
                       chatId = update.chatId(),
                       replyToMessageId = update.messageId(),
                       text = "Notes Cleared."
               )
           }
        }
    }

    text{ bot, update -> //See if Anyone Requested any Note
        if(update.message?.text.toString().contains("/start",true))return@text//Initialising Group
        suspendGlobally {
            val requestedNote = update.message?.text.toString().split("/").map { it.substringBefore(" ").trim().split("@")[0] }.toMutableList()
            val noteDataMap = getNotesMap(update.chatId())
            for (word in requestedNote) {
                if( noteDataMap.containsKey(word)  || /* If Not Found ,Extra Checks = */ noteDataMap.containsKey(word.toLowerCase()) || noteDataMap.containsKey(word.toUpperCase())){
                    println("Requested Note: $word")
                    bot.sendMessage(
                            chatId = update.chatId(),
                            replyToMessageId = update.messageId(),
                            text = noteDataMap.getValue(word)
                    )
                }
            }
        }
    }
}

fun showNotes(bot:Bot,update: Update){
    suspendGlobally {
        val notesMap: Map<String, String> = getNotesMap(update.chatId())
        bot.sendMessage(
                chatId = update.chatId(),
                replyToMessageId = update.messageId() ,
                text = if (notesMap.isNotEmpty()) {
                    "<i><b>All Notes:</b></i>\n" +
                            notesMap.keys.reduce { acc, s -> acc + "\n" + s }
                } else "<i><b>No Notes Saved.</b></i>",
                parseMode = ParseMode.HTML
        )
    }
}