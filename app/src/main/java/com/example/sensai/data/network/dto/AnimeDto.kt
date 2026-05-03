package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class JikanResponse(
    val data: List<AnimeDto>
)

data class JikanSingleResponse(
    val data: AnimeDto
)

data class AnimeDto(
    val malId: Int,
    val title: String,
    val titleEnglish: String?,
    val synopsis: String?,
    val score: Double?,
    val episodes: Int?,
    val images: AnimeImagesDto?,
    val genres: List<GenreDto>?,
    val rank: Int?,
    val members: Int?,
    val duration: String?,
    val studios: List<StudioDto>?,
    val year: Int?
)

data class AnimeImagesDto(
    val jpg: ImageFormatDto?,
    val webp: ImageFormatDto?
)

data class ImageFormatDto(
    val imageUrl: String?,
    val smallImageUrl: String?,
    val largeImageUrl: String?
)

data class GenreDto(
    val malId: Int,
    val name: String
)

data class StudioDto(
    val malId: Int,
    val name: String
)
