package com.example.travel_organiser

data class Plan(
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val isReminderChecked: Boolean,
    val createdTime: String,
    val createdDate: String
)