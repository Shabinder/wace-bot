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

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.lang.Exception

private val serializer =  MapSerializer(String.serializer(),String.serializer())

/*
* Save A Note In DB
* */
fun saveNote(groupId:Long,noteName:String,noteData:String){
    try{//Try and get if already Some Notes Are Saved.
        val groupData = getGroupData(groupId)
        val allNotesData= Json.decodeFromString(serializer,groupData.notes) as MutableMap<String, String>
        allNotesData[noteName] = noteData
        Json.encodeToString(serializer,allNotesData).also {
            groupData.notes = it
            println("Notes Updated for ${groupData.name}: $it")
        }
    }
    catch (e: ExposedSQLException){
        println(e.cause)
    }
}

/*
* Delete a specific Note from all of Group's Notes.
* */
fun deleteNote(groupId: Long,noteName: String):Boolean?{
    try{
        val groupData = getGroupData(groupId)
        val allNotesData = Json.decodeFromString(serializer,groupData.notes) as MutableMap<String, String>
        val index = allNotesData.keys.indexOf(noteName)
        val isNoteRemoved:Boolean? = if (index == -1) {//this Note Doesnt Exist yet
            null //So Do Nothing
        }
        else {//Note exists just remove it from data
            allNotesData.remove(noteName)
            true
        }
        Json.encodeToString(serializer,allNotesData).also {
            groupData.notes = it
            println("Notes Updated for ${groupData.name}: $it")
        }
        return isNoteRemoved
    }
    catch (e: ExposedSQLException){
        println(e.cause)
        return false
    }
}

/*
* Returns Notes List or Null if no notes present
* */
fun getNotesMap(groupId: Long):Map<String,String>{
    return try{
        Json.decodeFromString(serializer, getGroupData(groupId).notes)
    }catch (e: Exception){
        println("getNotesMap() : " + e.message+ "\n"+ e.cause)
        mapOf()
    }
}

/*
* Delete All Notes Saved In DB.
* */
fun clearNotes(groupId: Long){
    val groupData = getGroupData(groupId)
    groupData.notes = "{}" //Assign Notes to empty Map
}
