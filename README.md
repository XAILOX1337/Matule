# Matule — тренировка по КЗ «Профессионалы-2026», «Разработка мобильных приложений»

Один git-репозиторий, три отдельных Gradle-проекта и сервер PocketBase.

```
MatuleClothes-main/
├── MatuleUIKit/     проект 1 — библиотека компонентов (uikit) + демо со всеми состояниями (showcase)
├── MatuleNetwork/   проект 2 — сетевая библиотека (network): клиент, запросы, модели, ошибки
├── Matule/          проект 3 — приложение «Matule» (Clean Architecture: domain / data / presentation)
├── backend/         PocketBase: миграция со схемой и тестовыми данными, хук выхода
└── docs/            план, разбор макета, выдержка из задания, скриншоты Figma
```

## Как запустить

1. Подними сервер — инструкция в [`backend/README.md`](backend/README.md).
2. Открой в Android Studio папку **`Matule`**. Библиотеки подключаются через composite build
   (`includeBuild("../MatuleUIKit")`, `includeBuild("../MatuleNetwork")`), отдельно их собирать не нужно.
3. Запусти конфигурацию `app` на эмуляторе. Тестовый вход: `demo@matule.ru` / `Matule2026!`.

Демо компонентов: открой папку **`MatuleUIKit`** и запусти модуль `showcase`.
Тесты сетевой библиотеки: открой **`MatuleNetwork`** и запусти `./gradlew :network:test`.

## Ветки

Каждый спринт — отдельная ветка, созданная от предыдущей: `Sprint_1` → `Sprint_2` → `Sprint_3` → `Sprint_4` → `Sprint_5`.
После push создай Pull Request каждой ветки в `main` по порядку.
