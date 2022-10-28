package com.narbase.dasma.domain.user.login

import com.narbase.dasma.common.DataResponse
import com.narbase.dasma.common.Handler
import com.narbase.dasma.common.auth.loggedin.AuthorizedClientData
import com.narbase.dasma.data.access.clients.ClientsDao
import com.narbase.dasma.domain.utils.authenticatedClient
import com.narbase.dasma.dto.domain.user.login.LoginDto
import org.jetbrains.exposed.sql.transactions.transaction

class LoginController : Handler<LoginDto.Request, LoginDto.Response>(LoginDto.Request::class) {

    override fun process(
        requestDto: LoginDto.Request,
        clientData: AuthorizedClientData?
    ): DataResponse<LoginDto.Response> {
        val client = clientData.authenticatedClient

        val clientLastLogin = client.lastLogin
        transaction { ClientsDao.updateLastLogin(client.id) }

        return DataResponse(LoginDto.Response(clientLastLogin == null))
    }

}