package com.narbase.dasma.core


data class MultiLingualText(val en: String, val ar: String) {
    fun toDto(): MultiLingualTextDto = MultiLingualTextDto(en, ar)
}

data class MultiLingualTextDto(val en: String, val ar: String) {
    fun toDs(): MultiLingualText = MultiLingualText(en, ar)
}


