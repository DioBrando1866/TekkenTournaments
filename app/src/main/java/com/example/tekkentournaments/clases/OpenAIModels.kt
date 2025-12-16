package com.example.tekkentournaments.clases

import kotlinx.serialization.Serializable

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIResponse(
    val choices: List<Choice>? = null,
    val error: APIError? = null
)

@Serializable
data class Choice(
    val message: Message
)

@Serializable
data class APIError(
    val message: String
)