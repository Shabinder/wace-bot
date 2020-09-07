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

package com.shabinder.wace.models

enum class PlatformModel(
        val Name:String,
        val thumbUrl:String
){
    ANDROID(Name = "Android" ,thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTkuH1UIXi8uG8Ulfrlbo3P6ln5k9J_bY7D4Q&usqp=CAU"),
    IOS(Name = "IOS",thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQL2wkO2aDF1O4pv-oteBHHYJkZ3GvbnvBLvrcpOVTK-UH-ng4_ZrSYuRU1pA&s"),
    JAVA(Name = "Java",thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ66OjT1rwqz6nX2aztdh2m2UekgPDGzaU9F3NpyvbwdmIvpiZPt7Tdc1NdBQ&s"),
    KOTLIN(Name = "kotlin",thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT47M76y3jDkW7-Sg1XsGoSIFWrTgw-Mpe0x64kdyZW7H9KkWNMrWA1EoX4bFM&s"),
    PYTHON(Name = "Python",thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRiQVKaJgxArArsyD7Gu4UDCk0E_W_OhXXUWNmNrtrJe1gHaYwyEML5e0kMXyw&s"),
    GENERAL(Name = "General",thumbUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTRFR3TTMiW5XZE29x4IsGH5oQ6Vts2J4iRhm1x3UVl_X7lSEsgmqklpm_zDhQ&s")
}