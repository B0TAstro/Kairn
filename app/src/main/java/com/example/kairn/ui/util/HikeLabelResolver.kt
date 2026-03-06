package com.example.kairn.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.kairn.R
import com.example.kairn.domain.model.HikeCategory
import com.example.kairn.domain.model.HikeDifficulty

@Composable
fun HikeDifficulty.localizedLabel(): String {
    return when (this) {
        HikeDifficulty.EASY -> stringResource(R.string.difficulty_easy)
        HikeDifficulty.MODERATE -> stringResource(R.string.difficulty_moderate)
        HikeDifficulty.HARD -> stringResource(R.string.difficulty_hard)
        HikeDifficulty.EXPERT -> stringResource(R.string.difficulty_expert)
    }
}

@Composable
fun HikeCategory.localizedLabel(): String {
    return when (this) {
        HikeCategory.MOUNTAIN -> stringResource(R.string.category_mountain)
        HikeCategory.FOREST -> stringResource(R.string.category_forest)
        HikeCategory.LAKE -> stringResource(R.string.category_lake)
        HikeCategory.CAVE -> stringResource(R.string.category_cave)
    }
}
