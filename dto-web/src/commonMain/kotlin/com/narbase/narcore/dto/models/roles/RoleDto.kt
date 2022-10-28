package com.narbase.dasma.dto.models.roles

import com.narbase.dasma.dto.common.IdDto

typealias PrivilegeName = String

data class RoleDto(
    val id: IdDto?,
    val name: String,
    val privileges: List<PrivilegeName>,
)