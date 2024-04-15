package com.example.jmb_bms.model

import com.example.jmb_bms.model.database.chat.ChatRow
import kotlinx.coroutines.flow.MutableStateFlow

data class ChatInfoAndMsgHolder(
    val info: MutableStateFlow<ChatRow>,
    val messages: MutableStateFlow<List<ChatMessage>>
)
