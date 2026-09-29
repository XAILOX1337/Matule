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
