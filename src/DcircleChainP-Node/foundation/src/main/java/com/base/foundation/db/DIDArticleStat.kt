package com.base.foundation.db

import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.blankj.utilcode.util.LogUtils
import com.github.xpwu.ktdbtable.eq
import com.github.xpwu.ktdbtable.invoke
import com.github.xpwu.ktdbtable.where.and
import com.github.xpwu.ktdbtble.annotation.*
import com.github.xpwu.ktdbtble.annotation.Table
import com.tencent.wcdb.database.SQLiteDatabase

private val table = DIDArticleStat.Companion

@Table("DIDArticleStat")
class DIDArticleStat {
    class ChangedEvent(var address: String="") : NcEvent<String>(listOf(address)) {
        override fun getName(): String {
            return super.getName() + address
        }
    }

    @Column("statId", primaryKey = PrimaryKey.ONLY_ONE)
    var statId: String = ""

    @Column("dataUpdateTime")
    var DataUpdateTime: Long = 0


    @Index(true, name = "id_role_ymd", sequence = 0)
    @Column("roleId")
    var RoleId:String = ""

    @Index(true, name = "id_role_ymd", sequence = 1)
    @Column("roleStatType")
    var RoleStatType: String = ""

    @Index(true, name = "id_role_ymd", sequence = 2)
    @Column("ymd")
    var ymd:String = ""

    @Column("contentNums")
    var ContentNums: Int = 0

    @Column("circulationNums")
    var CirculationNums: Int = 0

    @Column("consumerNums")
    var ConsumerNums: Int = 0

    @Column("consumptionTimes")
    var ConsumptionTimes: Int = 0

    @Column("reachNums")
    var ReachNums: Int = 0

    @Column("groupNums")
    var GroupNums: Int = 0

    @Column("potentialCirculationNums")
    var PotentialCirculationNums: Int = 0

    @Column("potentialConsumerNums")
    var PotentialConsumerNums: Int = 0

    @Column("sgNums")
    var sgNums: Int = 0

    @Column("tTimes")
    var tTimes: Int = 0

    @Column("exposureGroupNums")
    var exposureGroupNums: Int = 0

    @Column("exposurePeopleNums")
    var exposurePeopleNums: Int = 0

    @Column("revenueNums")
    var revenueNums: Int = 0

    @Column("joinTimes")
    var JoinTimes: Int = 0

    @Column("joinUserCount")
    var JoinUserCount: Int = 0

    @Column("shareGroupCount")
    var ShareGroupCount: Int = 0
    /**
    Content Nums (内容总数)
CirculationNums (流通量)
ConsumerNums (消费人数)
ConsumptionTimes (消费次数)
ReachNums (传播人数)
    tTimes(传播次数)
    GroupNums (传播群数)
    PotentialCirculationNums (潜力流通量)
    PotentialConsumerNums (潜力消费数)
    sgNums 来源群数
    exposureGroupNums 曝光群数
    exposurePeopleNums 曝光人数
    revenueNums  个人收入

    * */

    companion object;

    enum class StatRoleType(val value: String) {
        ArticleStat("ArticleStat"),
        CreatorStat("CreatorStat"),
        TransferStat("TransferStat"),
        GroupStat("GroupStat"),
        SingleGroupStat("SingleGroupStat"), // 单个群聊，id则为chatId
        ConsumerStat("ConsumerStat"),
        InviteJoinGroupStat("InviteJoinGroupStat");//用户邀请进群信息

        companion object {
            fun fromValue(value: String) = StatRoleType.values().first { it.value == value }
        }
    }
}

suspend fun Array<DIDArticleStat>.update(): Array<String> {
    val failed: MutableList<String> = mutableListOf()
    for (item in this) {
        if (item.update() != null) {
            failed.add(item.statId)
        }
    }
    return failed.toTypedArray()
}

suspend fun DIDArticleStat.update(): Error? {
    return getUs().shareDB.invoke {
        val columns = listOf(
            DIDArticleStat.ContentNums,
            DIDArticleStat.CirculationNums,
            DIDArticleStat.ConsumerNums,
            DIDArticleStat.ConsumptionTimes,
            DIDArticleStat.ReachNums,
            DIDArticleStat.GroupNums,
            DIDArticleStat.PotentialCirculationNums,
            DIDArticleStat.PotentialConsumerNums,
            DIDArticleStat.exposureGroupNums,
            DIDArticleStat.revenueNums,
            DIDArticleStat.sgNums,
            DIDArticleStat.tTimes,
            DIDArticleStat.exposurePeopleNums,
            DIDArticleStat.RoleStatType,
            DIDArticleStat.ymd,
            DIDArticleStat.DataUpdateTime,
            DIDArticleStat.RoleId,
            DIDArticleStat.JoinTimes,
            DIDArticleStat.JoinUserCount,
            DIDArticleStat.ShareGroupCount,

            )

        val where = DIDArticleStat.statId.eq(this.statId)
        val name = table.TableNameIn(it)
        try {
            val colNum = it.UnderlyingDB.updateWithOnConflict(
                name,
                this.ToContentValues(columns),
                where.ArgSQL,
                where.BindArgs,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            if (colNum <= 0) {
                return@invoke Error("not found")
            }
            return@invoke null
        } catch (e: Exception) {
            LogUtils.e("DIDArticle.Update err", e)
            return@invoke Error(e)
        }
    }
}

suspend fun DIDArticleStat.insert(): Error? {
    return getUs().shareDB.invoke {
        val values = this.ToContentValues()
        val name = table.TableNameIn(it)
        try {
            it.UnderlyingDB.insertOrThrow(name, null, values)
            return@invoke null
        } catch (e: Exception) {
            return@invoke Error(e)
        }
    }
}

suspend fun Array<DIDArticleStat>.insert(): Array<String> {
    return getUs().shareDB.invoke {
        val failedList: MutableList<String> = mutableListOf()
        val name = table.TableNameIn(it)
        it.UnderlyingDB.beginTransaction()
        try {
            for (data in this) {
                try {
                    it.UnderlyingDB.insertOrThrow(name, null, data.ToContentValues())
                } catch (e: Exception) {
                    failedList.add(data.statId)
                }
            }
            it.UnderlyingDB.setTransactionSuccessful()
        } finally {
            it.UnderlyingDB.endTransaction()
        }
        return@invoke failedList.toTypedArray()
    }
}

suspend fun DIDArticleStat.Companion.FindLatestByRole(userid:String , userRoles: Array<DIDArticleStat.StatRoleType> ): Array<DIDArticleStat> {
    return getUs().shareDB {
        val result = mutableListOf<DIDArticleStat>()
        for (role in userRoles) {
            val doc = findLatestByRole(userid,role)
            if (doc != null) {
                result.add(doc)
            }
        }
        return@shareDB result.toTypedArray()
    }
}

suspend fun DIDArticleStat.Companion.FindLatestByArticle(didArticleAddress:String): DIDArticleStat? {
    return getUs().shareDB {
        return@shareDB findLatestByRole(didArticleAddress,DIDArticleStat.StatRoleType.ArticleStat)
    }
}


suspend fun DIDArticleStat.Companion.findByRoleWithDate(address :String , role: DIDArticleStat.StatRoleType,date: String): DIDArticleStat ? {
    return getUs().shareDB { it ->
        val where = DIDArticleStat.RoleId.eq(address) and  DIDArticleStat.RoleStatType.eq(role.value) and
                DIDArticleStat.ymd.eq(date)
        val cursor =
            it.UnderlyingDB.query(
                table.TableNameIn(it),
                DIDArticleStat.AllColumns().map { it.name }.toTypedArray(),
                where.ArgSQL,
                where.BindArgs,
                null,
                null,
                 "${DIDArticleStat.ymd.name} DESC"
            )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@shareDB null
        }
        val doc = DIDArticleStat()
        cursor.ToDIDArticleStat(doc)
        cursor.close()
        return@shareDB doc
    }
}

suspend fun DIDArticleStat.Companion.findLatestByRole(address :String , role: DIDArticleStat.StatRoleType): DIDArticleStat ? {
    return getUs().shareDB { it ->
        val where = DIDArticleStat.RoleId.eq(address) and  DIDArticleStat.RoleStatType.eq(role.value)
        val cursor =
            it.UnderlyingDB.query(
                table.TableNameIn(it),
                DIDArticleStat.AllColumns().map { it.name }.toTypedArray(),
                where.ArgSQL,
                where.BindArgs,
                null,
                null,
                "${DIDArticleStat.ymd.name} DESC"
            )
        if (!cursor.moveToFirst()) {
            cursor.close()
            return@shareDB null
        }
        val doc = DIDArticleStat()
        cursor.ToDIDArticleStat(doc)
        cursor.close()
        return@shareDB doc
    }
}