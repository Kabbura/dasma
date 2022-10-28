package com.narbase.dasma.domain.admin.staff

import com.narbase.dasma.common.DataResponse
import com.narbase.dasma.common.Handler
import com.narbase.dasma.common.auth.loggedin.AuthorizedClientData
import com.narbase.dasma.data.tables.UsersTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

class EnableStaffController :
    Handler<EnableStaffController.RequestDto, EnableStaffController.ResponseDto>(RequestDto::class) {
    override fun process(requestDto: RequestDto, clientData: AuthorizedClientData?): DataResponse<ResponseDto> {

        transaction {
            UsersTable.update({
                (UsersTable.id eq requestDto.userId)
            }) {
                it[isInactive] = requestDto.isActive.not()
            }
        }
        return DataResponse()
    }

    class RequestDto(
        val userId: UUID,
        val isActive: Boolean

    )

    class ResponseDto()
}