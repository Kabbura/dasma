package com.narbase.dasma.main.provisioning

import com.narbase.dasma.data.access.clients.ClientsDao
import com.narbase.dasma.data.access.roles.RolesDao
import com.narbase.dasma.data.access.users.UsersRepository
import com.narbase.dasma.deployment.FirstRunConfig
import com.narbase.dasma.dto.models.roles.Privilege
import org.jetbrains.exposed.sql.transactions.transaction

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

fun registerFirstAdmin() {
    val isClientsTableEmpty = transaction { ClientsDao.getClients(0, 1).isEmpty() }
    if (isClientsTableEmpty.not()) return
    val roleId = transaction { RolesDao.create("Super admin", Privilege.values().toList()) }
    UsersRepository.create(
        FirstRunConfig.adminUsername,
        FirstRunConfig.adminPassword,
        "First run admin",
        "249",
        "9999999999",
        listOf(roleId)
    )
}