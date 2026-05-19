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
    @SerializedName("title_japanese")
    val titleJapanese: String? = null,
    @SerializedName("synopsis")
    val synopsis: String?,
    @SerializedName("score")
    val score: Double?,
    @SerializedName("scored_by")
    val scoredBy: Int? = null,
    @SerializedName("episodes")
    val episodes: Int?,
    @SerializedName("images")
    val images: AnimeImagesDto?,
    @SerializedName("trailer")
    val trailer: TrailerDto? = null,
    @SerializedName("genres")
    val genres: List<GenreDto>?,
    @SerializedName("themes")
    val themes: List<GenreDto>? = null,
    @SerializedName("demographics")
    val demographics: List<GenreDto>? = null,
    @SerializedName("rank")
    val rank: Int?,
    @SerializedName("popularity")
    val popularity: Int? = null,
    @SerializedName("members")
    val members: Int?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("studios")
    val studios: List<StudioDto>?,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("rating")
    val rating: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("streaming")
    val streaming: List<StreamingDto>? = null,
    @SerializedName("relations")
    val relations: List<RelationDto>? = null,
    @SerializedName("theme")
    val theme: AnimeThemeDto? = null
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

data class TrailerDto(
    @SerializedName("youtube_id")
    val youtubeId: String? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("embed_url")
    val embedUrl: String? = null,
    @SerializedName("images")
    val images: TrailerImagesDto? = null
)

data class TrailerImagesDto(
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("medium_image_url")
    val mediumImageUrl: String? = null,
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null,
    @SerializedName("maximum_image_url")
    val maximumImageUrl: String? = null
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

data class StreamingDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)

data class RelationDto(
    @SerializedName("relation")
    val relation: String,
    @SerializedName("entry")
    val entry: List<RelationEntryDto>
)

data class RelationEntryDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)

data class AnimeThemeDto(
    @SerializedName("openings")
    val openings: List<String>? = null,
    @SerializedName("endings")
    val endings: List<String>? = null
)
