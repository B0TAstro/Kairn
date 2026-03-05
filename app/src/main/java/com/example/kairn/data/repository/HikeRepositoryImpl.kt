package com.example.kairn.data.repository

import com.example.kairn.data.remote.HikeDto
import com.example.kairn.data.remote.supabaseClient
import com.example.kairn.domain.model.Hike
import com.example.kairn.domain.repository.HikeRepository
import io.github.jan.supabase.postgrest.from

class HikeRepositoryImpl : HikeRepository {

    override suspend fun getHikes(): Result<List<Hike>> = runCatching {
        supabaseClient
            .from("hikes")
            .select()
            .decodeList<HikeDto>()
            .map { it.toDomain() }
    }
}
