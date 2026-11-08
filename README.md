# Matule — тренировка по КЗ «Профессионалы-2026», «Разработка мобильных приложений»

Один git-репозиторий, три отдельных Gradle-проекта и сервер PocketBase.

```
MatuleClothes-main/
├── MatuleUIKit/     проект 1 — библиотека компонентов (uikit) + демо со всеми состояниями (showcase)
├── MatuleNetwork/   проект 2 — сетевая библиотека (network): клиент, запросы, модели, ошибки
├── Matule/          проект 3 — приложение «Matule» (Clean Architecture: domain / data / presentation)
├── backend/         PocketBase: миграция со схемой и тестовыми данными, хук выхода
└── docs/            план, разбор макета, выдержка из задания, скриншоты Figma,
                     презентация (presentation.md), материалы для RuStore (rustore.md)
```

---

## Быстрый старт

### 1. Запусти сервер (обязательно ПЕРЕД входом в приложение)

```bash
cd backend
./pocketbase serve --http=0.0.0.0:8090
```

Сервер нужен всегда, пока пользуешься приложением. После перезагрузки ноутбука — запустить заново.
Проверка, что жив: `curl http://127.0.0.1:8090/api/health` → `{"message":"API is healthy.",...}`.

<details>
<summary>Если бинарника <code>backend/pocketbase</code> нет</summary>

Скачай `pocketbase_X.Y.Z_linux_amd64.zip` (нужна v0.23+) с
<https://github.com/pocketbase/pocketbase/releases> и распакуй в `backend/`.

Если GitHub недоступен — сборка из исходников через Go-прокси:

```bash
# Go один раз: https://dl.google.com/go/go1.24.9.linux-amd64.tar.gz → распаковать в ~/sdk
export PATH=$HOME/sdk/go/bin:$PATH GOPROXY=https://proxy.golang.org
go install github.com/pocketbase/pocketbase/examples/base@latest
cp ~/go/bin/base backend/pocketbase
```

При первом запуске миграции сами создадут коллекции и тестовые данные.
Сбросить базу: остановить сервер и удалить папку `backend/pb_data/`.
</details>

### 2. Открой приложение в Android Studio

```bash
~/Загрузки/android-studio/bin/studio.sh "/home/klim/Документы/MatuleClothes-main/Matule"
```

Открывается именно папка **`Matule/`** (не корень репозитория!). Библиотеки UIKit и Network
подтянутся сами через composite build, отдельно их собирать не нужно.
Дальше: дождаться Gradle Sync → выбрать конфигурацию **`app`** → запустить на эмуляторе (Run ▶).

Демо компонентов — открыть папку `MatuleUIKit`, модуль `showcase`.

### 3. Войди

**`demo@matule.ru` / `Matule2026!`** → приложение предложит придумать свой PIN из 4 цифр
(дальше вход по PIN или биометрии).

---

## Логины и пароли

| Куда | Логин | Пароль |
|---|---|---|
| Приложение (тестовый пользователь) | `demo@matule.ru` | `Matule2026!` |
| Админка PocketBase <http://127.0.0.1:8090/_/> | `admin@matule.ru` | `Admin12345!` |

PIN-код задаётся пользователем при первом входе (хранится локально в виде хэша).

## Настройки (`Matule/gradle.properties`)

```properties
# Адрес сервера. По умолчанию эмулятор: http://10.0.2.2:8090/
# Для физического телефона по Wi-Fi — IP ноутбука в локальной сети:
# MATULE_API_URL=http://192.168.1.10:8090/

# Ключ AppMetrica (https://appmetrica.yandex.ru). Без ключа аналитика пишется в Logcat.
# MATULE_APPMETRICA_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

## Полезные команды

```bash
# Сборка и unit-тесты приложения (из папки Matule/)
./gradlew :app:assembleDebug :app:testDebugUnitTest

# Релиз (подпись — docs/rustore.md)
./gradlew :app:assembleRelease :app:bundleRelease

# Тесты сетевой библиотеки (из папки MatuleNetwork/)
./gradlew :network:test

# Лог сервера
tail -f backend/pocketbase.log

# Остановить сервер
pkill -f 'pocketbase serve'
```

## Частые проблемы

| Симптом | Причина и лечение |
|---|---|
| «Нет соединения с сервером» в приложении | Сервер не запущен → см. шаг 1. Проверка: `curl http://127.0.0.1:8090/api/health`. На телефоне — см. `MATULE_API_URL` выше |
| `./gradlew` падает с `What went wrong: 27` | Gradle 8.13 не работает на Java 27. В `~/.gradle/gradle.properties` прописать `org.gradle.java.home=/usr/lib/jvm/java-21-openjdk` (в Android Studio не нужно — у неё свой JDK) |
| Не качается с GitHub | Заблокирован провайдером → бинарник PocketBase собрать через goproxy (см. шаг 1) |

## Ветки

Каждый спринт — отдельная ветка: `Sprint_1` → `Sprint_2` → `Sprint_3` → `Sprint_4` → `Sprint_5` → `Sprint_6`.
Все слиты в `main` (при появлении удалённого репозитория — оформить PR каждой ветки в `main` по порядку).
