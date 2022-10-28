package com.narbase.dasma.data.enums

import com.narbase.dasma.data.columntypes.EnumPersistenceName
import com.narbase.dasma.dto.common.EnumDtoName

enum class SmsMessageStatus(override val persistenceName: String, override val dtoName: String) : EnumPersistenceName,
    EnumDtoName {
    /**
     * Message not send and its timeToSend has not passed
     */
    Pending("Pending", "Pending"),

    Sent("Sent", "Sent"),

    Failed("Failed", "Failed"),
}