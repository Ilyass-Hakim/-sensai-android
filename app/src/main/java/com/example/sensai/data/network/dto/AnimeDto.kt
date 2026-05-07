package com.example.sensai.data.network.dto

import com.google.gson.annotations.SerializedName

data class JikanResponse(
    @SerializedName("data")
    val data: List<AnimeDto>,
    @SerializedName("debug_user_id")
    val debugUserId: String? = null,
    @SerializedName("debug_history_count")
    val debugHistoryCount: Int? = null
)

data class JikanSingleResponse(
    @SerializedName("data")
    val data: AnimeDto
)

data class AnimeDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("title_english")
    val titleEnglish: String?,
    @SerializedName("synopsis")
    val synopsis: String?,
    @SerializedName("score")
    val score: Double?,
    @SerializedName("episodes")
    val episodes: Int?,
    @SerializedName("images")
    val images: AnimeImagesDto?,
    @SerializedName("genres")
    val genres: List<GenreDto>?,
    @SerializedName("rank")
    val rank: Int?,
    @SerializedName("members")
    val members: Int?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("studios")
    val studios: List<StudioDto>?,
    @SerializedName("year")
    val year: Int?
)

data class AnimeImagesDto(
    @SerializedName("jpg")
    val jpg: ImageFormatDto?,
    @SerializedName("webp")
    val webp: ImageFormatDto?
)

data class ImageFormatDto(
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("small_image_url")
    val smallImageUrl: String?,
    @SerializedName("large_image_url")
    val largeImageUrl: String?
)

data class GenreDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("name")
    val name: String
)

data class StudioDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("name")
    val name: String
)
