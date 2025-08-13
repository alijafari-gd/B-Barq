package com.aliJafari.bbarq.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Outage::class], version = 1, exportSchema = false)
abstract class ADatabase : RoomDatabase() {
    abstract fun OutageDao(): OutageDao
}