package com.narbase.dasma.domain.user.profile

import com.narbase.dasma.common.DataResponse
import com.narbase.dasma.common.Handler
import com.narbase.dasma.common.auth.loggedin.AuthorizedClientData
import com.narbase.dasma.common.exceptions.UnauthenticatedException
import com.narbase.dasma.data.access.users.UsersRepository
import com.narbase.dasma.data.conversions.users.toProfileDto
import com.narbase.dasma.dto.domain.user.profile.GetProfileDto
import java.util.*

class GetProfileController : Handler<GetProfileDto.Request, GetProfileDto.Response>(GetProfileDto.Request::class) {

    override fun process(requestDto: GetProfileDto.Request, clientData: AuthorizedClientData?)
            : DataResponse<GetProfileDto.Response> {

        val clientId = UUID.fromString(clientData?.id ?: throw UnauthenticatedException())
        val userRm = UsersRepository.get(clientId)
        return DataResponse(GetProfileDto.Response(userRm.toProfileDto()))
    }
}