package com.example.jmb_bms.model

data class ChatInfo(
    val id: String,
    val name: String,
    val ownerId: String,
    val members: List<String>
)
