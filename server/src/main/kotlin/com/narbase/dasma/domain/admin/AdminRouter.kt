package com.narbase.dasma.domain.admin

import com.narbase.dasma.common.auth.AuthenticationConstants
import com.narbase.dasma.domain.admin.staff.EnableStaffController
import com.narbase.dasma.domain.admin.staff.UsersCrudController
import com.narbase.dasma.domain.admin.staff.roles.RolesCurdController
import com.narbase.dasma.domain.user.crud.crud
import com.narbase.dasma.domain.utils.addInactiveUserInterceptor
import com.narbase.dasma.dto.models.roles.Privilege
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

fun Routing.setupAdminRoutes() {
    authenticate(AuthenticationConstants.JWT_AUTH) {
        route("/api/admin") {
            addInactiveUserInterceptor()
            route("/v1") {

                route("/settings") {
                    crud(
                        "/users",
                        UsersCrudController(),
                        Privilege.UsersManagement,
                    )
                    crud(
                        "/roles",
                        RolesCurdController(),
                        Privilege.UsersManagement,
                    )
                    post("/enable_user") {
                        EnableStaffController().handle(call)
                    }
                }
            }
        }
    }
}