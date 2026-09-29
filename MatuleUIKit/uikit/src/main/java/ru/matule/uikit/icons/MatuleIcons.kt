package ru.matule.uikit.icons

/**
 * Иконки Matule. Каждая иконка — глиф шрифта `res/font/matule_icons.ttf`, собранного из иконок
 * макета скриптом `tools/icons/build_icon_font.py`. Файл сгенерирован этим же скриптом — не редактируйте вручную.
 *
 * @property glyph символ глифа в шрифте (область Private Use Area).
 * @property title русское название: подпись в showcase и описание для TalkBack по умолчанию.
 */
enum class MatuleIcons(val glyph: String, val title: String) {
    Back("\uE900", "Назад"),
    ChevronDown("\uE901", "Раскрыть"),
    ChevronRight("\uE902", "Вперёд"),
    Search("\uE903", "Поиск"),
    Plus("\uE904", "Добавить"),
    Minus("\uE905", "Убавить"),
    Close("\uE906", "Закрыть"),
    Trash("\uE907", "Удалить"),
    Cart("\uE908", "Корзина"),
    Check("\uE909", "Готово"),
    Eye("\uE90A", "Показать"),
    EyeOff("\uE90B", "Скрыть"),
    Filter("\uE90C", "Фильтр"),
    Home("\uE90D", "Главная"),
    Catalog("\uE90E", "Каталог"),
    Projects("\uE90F", "Проекты"),
    Profile("\uE910", "Профиль"),
    More("\uE911", "Ещё"),
    Backspace("\uE912", "Стереть"),
    Camera("\uE913", "Камера"),
    Gallery("\uE914", "Галерея"),
    Chat("\uE915", "Чат"),
    Download("\uE916", "Загрузить"),
    Location("\uE917", "Карта"),
    Send("\uE918", "Отправить"),
    Mic("\uE919", "Микрофон"),
    Attach("\uE91A", "Прикрепить"),
    Document("\uE91B", "Документ"),
}
