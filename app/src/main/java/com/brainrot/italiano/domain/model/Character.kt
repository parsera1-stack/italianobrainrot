package com.brainrot.italiano.domain.model

/**
 * Модель персонажа Brain Rot
 */
data class Character(
    val id: Int,
    val name: String,
    val neutralEmoji: String = "😐",
    val happyEmoji: String = "😊",
    val sadEmoji: String = "😢"
)

object Characters {
    val all = listOf(
        Character(1, "Балерино Капучино"),
        Character(2, "Шимпанзини Бананини"),
        Character(3, "Спагеттис"),
        Character(4, "Пицца Бой"),
        Character(5, "Ченчинелла"),
        Character(6, "Ассасина Капучино"),
        Character(7, "Пипи Киви"),
        Character(8, "Кокофанто Элефанто"),
        Character(9, "Тролла Лейла"),
        Character(10, "Страуберри Элефант"),
        Character(11, "Мяул"),
        Character(12, "Лимончелло Лимон"),
        Character(13, "Папа Пицца"),
        Character(14, "Тирамису Тедди"),
        Character(15, "Форнайо"),
        Character(16, "тун тун тун тун тун сакур"),
        Character(17, "та-та-та сакур"),
        Character(18, "бом-бом бини гузини"),
        Character(19, "бомбардира крокодила"),
        Character(20, "три потропа тропа трипа")
    )

    fun getRandom(): Character = all.random()
}
