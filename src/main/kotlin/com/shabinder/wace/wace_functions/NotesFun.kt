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
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ParseMode
import com.shabinder.wace.database.dbmethods.clearNotes
import com.shabinder.wace.database.dbmethods.deleteNote
import com.shabinder.wace.database.dbmethods.getNotesMap
import com.shabinder.wace.database.dbmethods.saveNote
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun notesFun(dispatcher: Dispatcher) = dispatcher.apply{

    command("save"){bot, update -> //Save New Note in DB
        if(restrictAdminOnly(bot,update)) {
            val message = update.message!!.text.toString().replace("/save", "").trim()
            val noteName = message.substringBefore(" ")
            val noteData = message.substringAfter(noteName).trim()
            println("Command : /save : \n name = $noteName \n data = $noteData")
            if (noteData.isBlank() or noteName.isBlank()) {
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    replyToMessageId = update.message!!.messageId,
                    text = "Note is Empty, \n Try again {/save <noteName> <noteData>}"
                )
            } else {
                GlobalScope.launch {
                    newSuspendedTransaction {
                        saveNote(update.message!!.chat.id, noteName, noteData)
                        bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = "/$noteName saved."
                        )
                    }
                }
            }
        }
    }

    command("deleteNote") { bot, update -> //Save New Note in DB
        if(restrictAdminOnly(bot,update)) {
            val noteName = update.message!!.text.toString().replace("/deleteNote", "").trim()
            println("Command : /deleteNote : \n name = $noteName ")
            if (noteName.isBlank()) {
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    replyToMessageId = update.message!!.messageId,
                    text = "Which Note to delete, \n Try again {/deleteNote <noteName>}"
                )
            } else {
                GlobalScope.launch {
                    newSuspendedTransaction {
                        val noteDeleted: Boolean? = deleteNote(update.message!!.chat.id, noteName)
                        bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
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
    }

    command("notes") { bot, update ->
        GlobalScope.launch {
            newSuspendedTransaction {
                val notesMap: Map<String, String> = getNotesMap(update.message!!.chat.id)
                bot.sendMessage(
                    chatId = update.message!!.chat.id,
                    text = if (notesMap.isNotEmpty()) {
                        "<i><b>All Notes:</b></i>\n" +
                                notesMap.keys.reduce { acc, s -> acc + "\n" + s }
                    } else "<i><b>No Notes Saved.</b></i>",
                    parseMode = ParseMode.HTML,
                    replyToMessageId = update.message!!.messageId
                )
            }
        }
    }

    command("clearNotes") { bot, update ->
        if(restrictAdminOnly(bot,update)) {
            GlobalScope.launch {
                newSuspendedTransaction {
                    clearNotes(update.message!!.chat.id)
                    bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = "Notes Cleared."
                    )
                }
            }
        }
    }
    text{ bot, update -> //See if Anyone Requested any Note
        if(update.message?.text.toString().contains("/start"))return@text//Initialising Group
        GlobalScope.launch {
            newSuspendedTransaction {
                val requestedNote = update.message?.text.toString().split("/").map { it.substringBefore(" ").trim() }.toMutableList()
                requestedNote.removeAt(0)
                val noteDataMap = getNotesMap(update.message!!.chat.id)
                for (word in requestedNote) {
                    if(noteDataMap.containsKey(word)){
                        println("Requested Note: $word")
                        bot.sendMessage(
                            chatId = update.message!!.chat.id,
                            replyToMessageId = update.message!!.messageId,
                            text = noteDataMap.getValue(word)
                        )
                    }
                }
            }
        }
    }
}