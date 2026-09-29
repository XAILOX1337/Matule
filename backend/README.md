# Бэкенд Matule на PocketBase

Swagger в тренировке не выдаётся, поэтому сервер поднимаем сами. Схема и тестовые данные создаются миграцией автоматически.

## Запуск (Linux)

1. Скачай последнюю версию PocketBase для Linux (нужна **v0.23 или новее**):
   <https://github.com/pocketbase/pocketbase/releases> → файл `pocketbase_X.Y.Z_linux_amd64.zip`.
2. Распакуй бинарник `pocketbase` прямо в эту папку (`backend/`), рядом с `pb_migrations/` и `pb_hooks/`.
3. Создай администратора и запусти сервер:

   ```bash
   cd backend
   chmod +x pocketbase
   ./pocketbase superuser upsert admin@matule.ru Admin12345!
   ./pocketbase serve --http=0.0.0.0:8090
   ```

4. Админка: <http://127.0.0.1:8090/_/> (логин из шага 3). Там видны все коллекции и записи.

При первом старте миграция `pb_migrations/1759140000_matule_init.js` создаст коллекции
`categories`, `products`, `promotions`, `cart`, `orders`, `projects`, поля профиля в `users`
и тестовые данные. Миграция `1759150000_matule_promo_images.js` добавит картинки к акциям
из папки `pb_seed/` — поэтому сервер запускается именно из папки `backend`.
Если нужно начать с нуля — останови сервер и удали папку `pb_data/`.

**Тестовый пользователь:** `demo@matule.ru` / `Matule2026!`

## Адрес сервера в приложении

- Эмулятор Android: `http://10.0.2.2:8090/` — прописан по умолчанию.
- Телефон по Wi-Fi: IP компьютера в локальной сети, например `http://192.168.1.10:8090/`.
  Укажи его в `Matule/gradle.properties`: `MATULE_API_URL=http://192.168.1.10:8090/`.

## API, которое использует приложение

| Запрос из задания | Метод и путь |
|---|---|
| Авторизация | `POST /api/collections/users/auth-with-password` |
| Проверка почты при регистрации | `POST /api/matule/check-email` (хук `pb_hooks/matule.pb.js`) |
| Создание пользователя | `POST /api/collections/users/records` |
| Получение информации о профиле | `GET /api/collections/users/records/{id}` |
| Изменение профиля | `PATCH /api/collections/users/records/{id}` |
| Выход | `POST /api/matule/logout` (хук `pb_hooks/matule.pb.js`) |
| Получение акций | `GET /api/collections/promotions/records` |
| Получение каталога | `GET /api/collections/products/records?filter=(category="…")` |
| Поиск | `GET /api/collections/products/records?filter=(title~"…")` |
| Получение описания товара | `GET /api/collections/products/records/{id}` |
| Добавление в корзину | `POST /api/collections/cart/records` |
| Изменение корзины | `PATCH` / `DELETE /api/collections/cart/records/{id}` |
| Оформление заказа | `POST /api/collections/orders/records` |
| Список проектов | `GET /api/collections/projects/records` |
| Создание проекта | `POST /api/collections/projects/records` (multipart, поле `image`) |

Токен передаётся в заголовке `Authorization: <token>`.
