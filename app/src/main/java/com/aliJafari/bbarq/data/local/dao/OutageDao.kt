package com.aliJafari.bbarq.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aliJafari.bbarq.data.model.Outage

@Dao
interface OutageDao {
    @Query("SELECT * FROM outages")
    fun getAll(): List<Outage>

    @Query("DELETE FROM outages")
    fun deleteAll()

    @Query("SELECT * FROM outages WHERE id IN (:outageIds)")
    fun loadAllByIds(outageIds: IntArray): List<Outage>

    @Insert
    fun insertAll(vararg outages: Outage)

}