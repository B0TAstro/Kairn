package com.example.kairn.data.repository

import com.example.kairn.data.remote.HikeDto
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.repository.HikeRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HikeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : HikeRepository {

    override suspend fun getHikes(): Result<List<Hike>> = runCatching {
        postgrest
            .from("hikes")
            .select()
            .decodeList<HikeDto>()
            .map { it.toDomain() }
    }
}
