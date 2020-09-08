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

import com.shabinder.wace.models.SubGroup
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val serializer = ListSerializer(SubGroup.serializer())

/*
* Returns True after successfully creating a Subgroup , False if subgroup already present!
* */
fun createSubGroup(groupId:Long,subGroupName:String):Boolean{
    val groupData = getGroupData(groupId)
    val subGroupList = Json.decodeFromString(serializer,groupData.subGroups).toMutableList()

    if(subGroupList.map { it.subGroupName.toLowerCase() }.contains(subGroupName.toLowerCase()))return false //SubGroup Already Exists

    subGroupList.add(SubGroup(subGroupName))
    Json.encodeToString(serializer,subGroupList).also {
        groupData.subGroups = it
        println("SubGroup List for ${groupData.name} : $subGroupList")
    }
    return true
}

/*
* Returns True if subgroup exists and removed, False if Subgroup Doesnt exists
* */
fun removeSubGroup(groupId:Long,subGroupName:String):Boolean{
    val groupData = getGroupData(groupId)
    val subGroupList = Json.decodeFromString(serializer,groupData.subGroups).toMutableList()
    var isRemoved = false
    subGroupList.forEachIndexed { index , subGroup ->
        if(subGroup.subGroupName.equals(subGroupName,true)){
            subGroupList.removeAt(index)
            isRemoved = true
        }
    }
    Json.encodeToString(serializer,subGroupList).also {
        groupData.subGroups = it
        println("SubGroup List for ${groupData.name} : $subGroupList")
    }
    return isRemoved
}

/*
* Returns True if subgroup exists and members are added, False if Subgroup Doesnt exists
* */
fun addMemberToSubGroup(groupId:Long,subGroupName:String,subGroupUser:String):Boolean{
    val groupData = getGroupData(groupId)
    val subGroupList = Json.decodeFromString(serializer,groupData.subGroups).toMutableList()
    var areMembersAdded = false
    subGroupList.forEach {
        if (it.subGroupName == subGroupName){
            areMembersAdded = true
            it.members.add(subGroupUser)
        }
    }
    Json.encodeToString(serializer,subGroupList).also {
        groupData.subGroups = it
        println("SubGroup List for ${groupData.name} : $subGroupList")
    }
    return areMembersAdded
}

/*
* Returns True if subgroup exists and members are Removed, False if Subgroup Doesnt exists
* */
fun removeMembersFromSubGroup(groupId:Long,subGroupName:String,subGroupUserName:String):Boolean{
    val groupData = getGroupData(groupId)
    val subGroupList = Json.decodeFromString(serializer,groupData.subGroups).toMutableList()
    var isMemberRemoved = false
    subGroupList.forEach {
        if (it.subGroupName.equals(subGroupName,true)){
            isMemberRemoved = true
            it.members.remove(subGroupUserName)
        }
    }
    Json.encodeToString(serializer,subGroupList).also {
        groupData.subGroups = it
        println("SubGroup List for ${groupData.name} : $subGroupList")
    }
    return isMemberRemoved
}

/*
* Returns a Map <SubGroupName:No. of Members>
* */
fun listSubGroups(groupId:Long):Map<String,Int> =
        Json.decodeFromString(serializer,getGroupData(groupId).subGroups).map{it.subGroupName to it.members.size}.toMap()
