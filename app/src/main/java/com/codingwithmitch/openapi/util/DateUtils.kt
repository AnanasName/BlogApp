package com.codingwithmitch.openapi.util

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object{

        fun convertServerStringDateToLong(sd: String): Long{
            var stringDate = sd.removeRange(sd.indexOf("T") until sd.length)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            try{
                val time = sdf.parse(stringDate).time
                return time
            }catch (e: Exception){
                throw Exception(e)
            }
        }

        fun convertLongToStringDate(longDate: Long): String{
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            try{
                val date = sdf.format(Date(longDate))
                return date
            } catch (e: Exception){
                throw Exception(e)
            }
        }
    }
}