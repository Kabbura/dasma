package com.narbase.dasma.domain.user.profile

import com.narbase.dasma.common.DataResponse
import com.narbase.dasma.common.Handler
import com.narbase.dasma.common.auth.basic.PasswordEncoder
import com.narbase.dasma.common.auth.loggedin.AuthorizedClientData
import com.narbase.dasma.common.exceptions.UnauthenticatedException
import com.narbase.dasma.data.tables.ClientsTable
import com.narbase.dasma.dto.domain.user.profile.UpdatePasswordDto
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

class UserPasswordController :
    Handler<UpdatePasswordDto.Request, UpdatePasswordDto.Response>(UpdatePasswordDto.Request::class) {


    override fun process(
        requestDto: UpdatePasswordDto.Request, clientData: AuthorizedClientData?
    ): DataResponse<UpdatePasswordDto.Response> {
        val clientId = UUID.fromString(clientData?.id ?: throw UnauthenticatedException(""))

        val didUpdate = updateUserPassword(clientId, requestDto.oldPassword, requestDto.newPassword)
        return DataResponse(UpdatePasswordDto.Response(didUpdate))
    }

    private fun updateUserPassword(clientId: UUID, oldPassword: String, newPassword: String): Boolean {

        val passwordEncoder = PasswordEncoder()
        val existingPasswordHash = transaction {
            ClientsTable.slice(ClientsTable.passwordHash).select { ClientsTable.id eq clientId }.limit(1)
                .firstOrNull()?.get(ClientsTable.passwordHash)
        } ?: ""
        if (passwordEncoder.checkPassword(oldPassword, existingPasswordHash).not()) {
            return false
        }
        transaction {
            ClientsTable.update({ ClientsTable.id eq clientId }) {
                it[passwordHash] = passwordEncoder.encode(newPassword)
            }
        }
        return true
    }
}