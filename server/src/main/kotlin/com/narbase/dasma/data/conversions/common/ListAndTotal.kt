package com.narbase.dasma.data.conversions.common

import com.narbase.dasma.data.models.utils.ListAndTotal

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

fun <Model, Dto> ListAndTotal<Model>.toDto(block: (Model) -> Dto) = ListAndTotal(list.map(block), total)