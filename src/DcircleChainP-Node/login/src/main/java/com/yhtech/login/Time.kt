package com.yhtech.login

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

object Time {

    /**
     * @param time 对应时间戳
     * @return 返回带有英文标识的上下午
     */
    fun formatChatTime(time: Long): String {
        if (time <= 0) return ""
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.time = Date(time)
        val hour12 = calendar.get(Calendar.HOUR_OF_DAY)
        val amPm = if (hour12 < 12) Calendar.AM else Calendar.PM
        val isAm = amPm == Calendar.AM
        val min = calendar.get(Calendar.MINUTE)
        val minStr = if (min >= 10) min.toString() else "0$min"
        val hourStr = if (hour12 >= 10) hour12.toString() else "0$hour12"
        return hourStr.plus(":").plus(minStr)
            .plus(if (isAm) " AM" else " PM")
    }

    //昨天的日期
    fun  getYesterdayDateString() :String{
        val dateFormat =  SimpleDateFormat("yyyyMMdd")
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -2)
        return dateFormat.format(cal.time)
    }


    /**
     * 判断两个时间是否在同一时间
     * @param date1 前一段
     * @param data2 稍后的时间
     * @param minute 分钟间隔
     * @return true为在间隔时间里
     */
    fun betweenMinutes(date1: Date, date2: Date,minute:Int=15):Boolean{
        val diff = date2.time - date1.time
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        return diffInMinutes < minute
    }

    /**
     * 判断两个时间是否在同一时间
     * @param date1 前一段
     * @param data2 稍后的时间
     * @param minute 分钟间隔
     * @return true为在间隔时间里
     */
    fun betweenMinutes(date1: Long, date2: Long,minute:Int=15):Boolean{
        val diff = date2 - date1
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        return diffInMinutes < minute
    }

    fun formatDateYear(timestamp: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        return simpleDateFormat.format(timestamp)

    }

    fun formatDateByParams(timestamp: Long,params:String): String {
        val simpleDateFormat = SimpleDateFormat(params, Locale.getDefault())
        return simpleDateFormat.format(timestamp)

    }


    fun formatDateNoYear(timestamp: Long): String {
        val simpleDateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        return simpleDateFormat.format(timestamp)

    }

    fun formatLoginTimeNoYear(timestamp: Long): String {
        val simpleDateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        return simpleDateFormat.format(timestamp)

    }
    fun formatDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val currentYear = LocalDateTime.now().year
        return if (localDateTime.year == currentYear) {
            val simpleDateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            simpleDateFormat.format(timestamp)
        } else {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            simpleDateFormat.format(timestamp)
        }
    }

    fun timestampToTime(timestamp: Long): String {
        val now = Calendar.getInstance()
        val dt = Date(timestamp)
        val dtCalendar = Calendar.getInstance()
        dtCalendar.time = dt

        val delta = now.timeInMillis - timestamp
        return if (delta < 60 * 1000) {
            "刚刚"
        } else if (delta < 60 * 60 * 1000) {
            "${delta / (60 * 1000)} 分钟前"
        } else if (delta < 24 * 60 * 60 * 1000) {
            val hours = dt.hours
            val minutes = dt.minutes
            "${if (hours < 10) "0$hours" else hours}:${if (minutes < 10) "0$minutes" else minutes} ${if (hours > 12) "PM" else "AM"}"
        } else if (delta < 7 * 24 * 60 * 60 * 1000) {
            dateFormat(dt, "EEEE")
        } else if (dtCalendar[Calendar.YEAR] == now[Calendar.YEAR]) {
            "${dt.month}月${dt.date}日"
        } else {
            "${dtCalendar[Calendar.YEAR]}年${dt.month}月${dt.date}日"
        }
    }

    fun Date.get(field: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.time = this
        return calendar[field]
    }

    private fun dateFormat(date: Date, pattern: String): String {
        val simpleDateFormat = SimpleDateFormat(pattern)
        return simpleDateFormat.format(date)
    }

    /**
     * 规则
     * 不在当年，需要显示年份
     * 在当年显示，月日
     *January，February，March，April，May，June，July，August，September，October，November，December
     */
    fun formatChatTopTime(time:Long):String{
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val month = calendar.get(Calendar.MONTH)+1
        val buffer = StringBuffer()
        when(month){
            1-> buffer.append("January")
            2-> buffer.append("February")
            3-> buffer.append("March")
            4-> buffer.append("April")
            5-> buffer.append("May")
            6-> buffer.append("June")
            7-> buffer.append("July")
            8-> buffer.append("August")
            9-> buffer.append("September")
            10-> buffer.append("October")
            11-> buffer.append("November")
            12-> buffer.append("December")
        }
        val year = calendar.get(Calendar.YEAR)

        val currentDate = Date()
        val currentYear = currentDate.year + 1900 // Date类中的年份需要加上1900
        val dayof = calendar.get(Calendar.DAY_OF_MONTH)
        val day = if (dayof<10) "0$dayof" else dayof
        return if (currentYear == year) {
            buffer.append(" ")
            buffer.append(day).toString()
        } else {
            buffer.append(day).append(",$year").toString()
        }

    }


    /**
     * 获取当前时间与之前的时间差多少，文件选择器使用的
     */
    fun getTimeAgo(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val now = Calendar.getInstance().timeInMillis

        val diffInMillis = now - calendar.timeInMillis
        val diffInSeconds = diffInMillis / 1000
        val diffInMinutes = diffInSeconds / 60
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24
        val diffInYears = diffInDays / 365

        return when {
            diffInYears > 0 -> "$diffInYears year${if (diffInYears > 1) "s" else ""}"
            diffInDays > 0 -> "$diffInDays day${if (diffInDays > 1) "s" else ""}"
            diffInHours > 0 -> "$diffInHours hour${if (diffInHours > 1) "s" else ""}"
            else -> ""
        }
    }


}