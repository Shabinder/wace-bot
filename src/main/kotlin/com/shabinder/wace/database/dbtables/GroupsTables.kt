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

package com.shabinder.wace.database.dbtables

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.sql.SQLException

//A DB table is represented by an object
object GroupsTables : IntIdTable(name = "groups_table"){
    var groupId =  long("group_id").uniqueIndex()
    var name = varchar("name",50)
    var link =  varchar("link",50).default("")
    var welcomeMessage =  varchar("welcome_message",500).default("")
    var donationLink =  varchar("donation_link",500).default("")
    var rules = varchar("rules",4000 ).default("")
    var warnLimit = integer("warn_limit").default(3)
    var disabledTypes = varchar("disabled_types",4000 ).default("[]") //Syntax Used = [type1,type2......]
    var blockedWords = varchar("blocked_words",4000 ).default("[]")  //Syntax used =[<word1>,<word2>,<word3>....]
    var notes = varchar("notes",4000 ).default("{}") //Syntax used = [Map(<String>:<String>) ,Map(<String>:<String>) ....]
    var pinnedMessage =  varchar("pinned_message",1000).default("") //Syntax used <messageId>:<messageData>
    var sudoUsers = varchar("sudo_users",4000 ).default("[]") //Syntax used = [<userId1>,<userId2>;....]
    var warnPerUser = varchar("warn_per_user",4000 ).default("[]") //Syntax used = [WarnPerUser(<userName>:<warnCount>) , WarnPerUser(<userName>:<warnCount>) ....]
    var subGroups = varchar("sub_groups",4000 ).default("[]") //Syntax used = [SubGroup1(<subGroupName:[<user1>,<user2>.....]>),SubGroup2(<subGroupName:[<user1>,<user2>.....]>),....]
}

//An entity instance or a ROW in the DB table is defined as a class instance:
class GroupsTable(id: EntityID<Int>): IntEntity(id){
    companion object : IntEntityClass<GroupsTable>(GroupsTables)
    var groupId by GroupsTables.groupId
    var name by GroupsTables.name
    var link by GroupsTables.link
    var welcomeMessage by GroupsTables.welcomeMessage
    var donationLink by GroupsTables.donationLink
    var rules by GroupsTables.rules
    var warnLimit by GroupsTables.warnLimit
    var disabledTypes by GroupsTables.disabledTypes
    var blockedWords by GroupsTables.blockedWords
    var notes by GroupsTables.notes
    var pinnedMessage by GroupsTables.pinnedMessage
    var sudoUsers by GroupsTables.sudoUsers
    var warnPerUser by GroupsTables.warnPerUser
    var subGroups by GroupsTables.subGroups
}


fun newGroup(Id:Long,groupName:String,groupLink:String?){
    try{
        GroupsTable.new {
            groupId = Id
            name = groupName
            link = groupLink ?: ""
        }
    }catch (e:SQLException){
        println(e.cause)
    }
}

fun getAllGroupsNamesWithLinks():List<Pair<String,String>> {
    return  GroupsTable.all()
                .map{
                    it.name to it.link
                }
}

