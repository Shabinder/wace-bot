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

package com.shabinder.wace.database

import com.shabinder.wace.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseConnection {
    val database: Database by lazy {
        var dbURL:String
        var userName:String
        var password:String
        try{//Deployed on Heroku.
            val dbURI = URI(System.getenv("DATABASE_URL"))
            dbURL =
                    "jdbc:postgresql://" + dbURI.host.toString() + (if (dbURI.port > 0) ":" + dbURI.port else "") + dbURI.path + "?sslmode=require"
            userName = dbURI.userInfo.split(":")[0]
            password = dbURI.userInfo.split(":")[1]
        }catch (e:java.lang.NullPointerException){//Development on local machine.
            // Stored in Config.kt
            dbURL =
                    "jdbc:postgresql://" + dbHost + (if (dbPort > 0) ":$dbPort" else "") + dbPath + "?sslmode=require"
            userName = dbUserName
            password = dbPassword
        }
        Database.connect(
                dbURL, driver = "org.postgresql.Driver",
                user = userName, password = password
        )
    }
}