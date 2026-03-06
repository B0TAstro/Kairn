package com.example.kairn.domain.repository

import com.example.kairn.domain.model.Hike

interface HikeRepository {
    suspend fun getHikes(): Result<List<Hike>>
    suspend fun getHikeById(hikeId: String): Result<Hike>
    suspend fun getCompletedHikes(userId: String): Result<List<Hike>>
}
