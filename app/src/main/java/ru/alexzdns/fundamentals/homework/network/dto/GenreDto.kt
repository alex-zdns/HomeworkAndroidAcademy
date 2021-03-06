package ru.alexzdns.fundamentals.homework.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GenreDto (
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String
)