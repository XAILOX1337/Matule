/// <reference path="../pb_data/types.d.ts" />

/**
 * POST /api/matule/logout — «Выход».
 *
 * В PocketBase токены не хранятся на сервере, поэтому выход реализован через
 * смену tokenKey пользователя: все ранее выданные токены перестают действовать.
 * Требует заголовок Authorization с токеном пользователя.
 */
routerAdd("POST", "/api/matule/logout", (e) => {
  const user = e.auth;
  if (user) {
    user.refreshTokenKey();
    e.app.save(user);
  }
  return e.json(200, { success: true });
}, $apis.requireAuth("users"));

/**
 * POST /api/matule/check-email — свободна ли почта (экран «Создание профиля»).
 *
 * Тело запроса: {"email": "name@domain.ru"}. Ответ: {"available": true | false}.
 * Авторизация не нужна: регистрация идёт до входа.
 */
routerAdd("POST", "/api/matule/check-email", (e) => {
  const body = e.requestInfo().body || {};
  const email = String(body.email || "").trim().toLowerCase();
  if (!email) {
    throw new BadRequestError("Укажите email");
  }
  let available = true;
  try {
    e.app.findAuthRecordByEmail("users", email);
    available = false;
  } catch (err) {
    available = true;
  }
  return e.json(200, { available: available });
});
