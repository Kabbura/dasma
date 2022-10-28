package com.narbase.dasma.common.db

import org.jetbrains.exposed.sql.Database

object DatabaseConnector {
    fun connect() {
        println("DatabaseConnector connect")
        val db = Database.connect(ConnectionProvider.dataSource)
        db.url
    }
}

