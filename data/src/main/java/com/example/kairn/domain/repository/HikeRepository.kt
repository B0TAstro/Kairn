package com.example.kairn.domain.repository

import com.example.kairn.domain.model.Hike

interface HikeRepository {
    suspend fun getHikes(): Result<List<Hike>>
}
