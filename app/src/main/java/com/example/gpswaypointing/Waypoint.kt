package com.example.gpswaypointing


data class Waypoint(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String = "Waypoint $id"
)
