package com.vbarzola.shared.catalog

import com.vbarzola.shared.db.AppDatabase
import com.vbarzola.shared.network.CatalogoApiClient
import kotlinx.datetime.Clock

data class ProfessionalItem(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val categoryId: Long,
    val categoryName: String,
    val enabled: Boolean
)

data class CategoryItem(
    val id: Long,
    val name: String
)

class CatalogRepository(
    private val database: AppDatabase,
    private val apiClient: CatalogoApiClient
) {
    fun isSynced(): Boolean =
        database.catalogSyncStatusQueries.get().executeAsOneOrNull() != null

    suspend fun syncSnapshot(): Result<Unit> = runCatching {
        val categories = apiClient.getCategories().getOrThrow()
        val professionals = apiClient.getProfessionals().getOrThrow()
        val schedules = apiClient.getWeeklySchedules().getOrThrow()

        database.transaction {
            database.professionalCategoryQueries.deleteAll()
            database.professionalQueries.deleteAll()
            database.weeklyScheduleQueries.deleteAll()

            categories.forEach { c ->
                database.professionalCategoryQueries.upsert(
                    id = c.id,
                    name = c.name,
                    description = c.description,
                    enabled = if (c.enabled == true) 1L else 0L,
                    updatedAt = c.updatedAt ?: ""
                )
            }

            professionals.forEach { p ->
                val cat = p.category ?: return@forEach
                database.professionalQueries.upsert(
                    id = p.id,
                    categoryId = cat.id,
                    firstName = p.firstName ?: "",
                    lastName = p.lastName ?: "",
                    enabled = if (p.enabled == true) 1L else 0L,
                    updatedAt = p.updatedAt ?: ""
                )
            }

            schedules.forEach { s ->
                database.weeklyScheduleQueries.upsert(
                    id = s.id,
                    professionalId = s.professional.id,
                    dayOfWeek = s.dayOfWeek,
                    startTime = s.startTime,
                    endTime = s.endTime,
                    slotDurationMinutes = s.slotDurationMinutes.toLong(),
                    enabled = if (s.enabled == true) 1L else 0L
                )
            }

            database.catalogSyncStatusQueries.upsert(
                localVersion = categories.size.toLong(),
                appliedAt = Clock.System.now().toString()
            )
        }
    }

    fun getCategories(): List<CategoryItem> =
        database.professionalCategoryQueries.selectEnabled()
            .executeAsList()
            .map { CategoryItem(it.id, it.name) }

    fun searchProfessionals(
        query: String = "",
        categoryId: Long? = null,
        enabledOnly: Boolean = true
    ): List<ProfessionalItem> = when {
        query.isNotBlank() ->
            database.professionalQueries.searchByName(query, ::toProfessionalItem).executeAsList()
        categoryId != null ->
            database.professionalQueries.selectByCategory(categoryId, ::toProfessionalItem).executeAsList()
        enabledOnly ->
            database.professionalQueries.selectEnabled(::toProfessionalItem).executeAsList()
        else ->
            database.professionalQueries.selectAll(::toProfessionalItem).executeAsList()
    }

    private fun toProfessionalItem(
        id: Long,
        category_id: Long,
        first_name: String,
        last_name: String,
        enabled: Long,
        updated_at: String,
        category_name: String
    ) = ProfessionalItem(
        id = id,
        firstName = first_name,
        lastName = last_name,
        categoryId = category_id,
        categoryName = category_name,
        enabled = enabled == 1L
    )
}
