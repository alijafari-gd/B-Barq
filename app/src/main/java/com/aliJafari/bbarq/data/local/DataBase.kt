package com.aliJafari.bbarq.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aliJafari.bbarq.data.model.Outage
import com.aliJafari.bbarq.data.local.dao.OutageDao

@Database(entities = [Outage::class], version = 1, exportSchema = false)
abstract class ADatabase : RoomDatabase() {
    abstract fun OutageDao(): OutageDao
}