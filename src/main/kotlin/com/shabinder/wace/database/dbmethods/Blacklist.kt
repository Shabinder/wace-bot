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

package com.shabinder.wace.database.dbmethods

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*
* BlackList A Word
* */
fun blacklistWord(groupId:Long, word:String){
    val groupData = getGroupData(groupId)
    val allBlockedWords:MutableList<String> = Json.decodeFromString(string = groupData.blockedWords)
    allBlockedWords.add(word)
    Json.encodeToString(value = allBlockedWords).also {
        groupData.blockedWords = it
        println("Blacklisted Words in ${groupData.name}: $allBlockedWords")
    }
}
/*
* WhiteList A Word and return boolean true is successfully whitelisted.
* */
fun whitelistWord(groupId:Long, word:String):Boolean{
    val groupData = getGroupData(groupId)
    val allBlockedWords:MutableList<String> = Json.decodeFromString(string = groupData.blockedWords)
    val isWhiteListed = allBlockedWords.remove(word)
    Json.encodeToString(value = allBlockedWords).also {
        groupData.blockedWords = it
        println("Blacklisted Words in ${groupData.name}: $allBlockedWords")
    }
    return isWhiteListed
}

/*
* Get All BlockedWords List
* */
fun getAllBlackListedWords(groupId: Long):List<String> = Json.decodeFromString(string = getGroupData(groupId).blockedWords)
