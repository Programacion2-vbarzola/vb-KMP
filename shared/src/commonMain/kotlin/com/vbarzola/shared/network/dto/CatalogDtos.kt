package com.vbarzola.shared.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogCategoryDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val enabled: Boolean? = null,
    val updatedAt: String? = null
)

@Serializable
data class CatalogProfessionalDto(
    val id: Long,
    val firstName: String? = null,
    val lastName: String? = null,
    val enabled: Boolean? = null,
    val updatedAt: String? = null,
    val category: CatalogCategoryDto? = null
)

@Serializable
data class CatalogWeeklyScheduleDto(
    val id: Long,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val slotDurationMinutes: Int,
    val enabled: Boolean? = null,
    val professional: CatalogProfessionalDto
)
