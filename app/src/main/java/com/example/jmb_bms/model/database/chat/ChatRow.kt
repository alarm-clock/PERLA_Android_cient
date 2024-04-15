package com.example.jmb_bms.model.database.chat


data class ChatRow(
    val id: String,
    val name: String,
    val owner: String,
    val members: List<String>
)
