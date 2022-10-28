package com.narbase.dasma.domain.client.token

import com.google.gson.annotations.SerializedName
import com.narbase.dasma.common.DataResponse
import com.narbase.dasma.common.Handler
import com.narbase.dasma.common.auth.loggedin.AuthorizedClientData
import com.narbase.dasma.common.exceptions.UnauthenticatedException
import com.narbase.dasma.data.tables.ClientsTable
import com.narbase.dasma.data.tables.DeviceTokensTable
import com.narbase.dasma.data.tables.utils.toEntityId
import com.narbase.dasma.domain.client.token.AddTokenController.RequestDto
import com.narbase.dasma.domain.client.token.AddTokenController.ResponseDto
import com.narbase.dasma.domain.client.token.RemoveTokenController.Companion.removeClientToken
import com.narbase.dasma.domain.utils.client
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class AddTokenController : Handler<RequestDto, ResponseDto>(RequestDto::class) {

    class RequestDto(
        @SerializedName("token")
        val token: String
    )

    class ResponseDto

    override fun process(requestDto: RequestDto, clientData: AuthorizedClientData?): DataResponse<ResponseDto> {
        val client = clientData?.client ?: throw UnauthenticatedException()
        transaction {
            removeClientToken(client, requestDto.token)
            DeviceTokensTable.insert {
                it[token] = requestDto.token
                it[clientId] = client.id.toEntityId(ClientsTable)
                it[createdOn] = DateTime()
            }
        }
        return DataResponse(ResponseDto())
    }
}