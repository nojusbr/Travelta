package com.example.travel_organiser

data class Plan(
    val planId: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val createdTime: String,
    val createdDate: String,
    val isReminderChecked: Boolean
)
