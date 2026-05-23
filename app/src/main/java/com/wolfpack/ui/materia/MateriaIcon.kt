package com.wolfpack.ui.materia

object MateriaIcon {
    fun label(icon: String): String = when (icon) {
        "book" -> "📘"
        "calculator" -> "🧮"
        "science" -> "🔬"
        "history" -> "🏛️"
        "art" -> "🎨"
        "language" -> "🌎"
        "code" -> "💻"
        else -> icon.ifBlank { "📘" }
    }
}
