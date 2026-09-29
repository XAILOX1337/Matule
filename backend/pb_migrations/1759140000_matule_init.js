/// <reference path="../pb_data/types.d.ts" />

/**
 * Схема и тестовые данные для приложения Matule.
 *
 * Применяется автоматически при первом запуске `./pocketbase serve`
 * (файл должен лежать в папке pb_migrations рядом с бинарником).
 * Требуется PocketBase v0.23 или новее.
 *
 * Коллекции:
 *  - users (системная)  + поля профиля: patronymic, surname, birthday, gender, notifications
 *  - categories         — категории описаний (публичное чтение)
 *  - products           — описания для машинного вязания (публичное чтение)
 *  - promotions         — акции и новости (публичное чтение)
 *  - cart               — корзина пользователя (только свои записи)
 *  - orders             — заказы пользователя (только свои записи)
 *  - projects           — проекты пользователя с фото (только свои записи)
 *
 * Тестовый пользователь: demo@matule.ru / Matule2026!
 */
migrate((app) => {
  // ---------- users: дополнительные поля профиля ----------
  const users = app.findCollectionByNameOrId("users");
  users.fields.add(new TextField({ name: "patronymic", max: 100 }));
  users.fields.add(new TextField({ name: "surname", max: 100 }));
  users.fields.add(new TextField({ name: "birthday", max: 10 }));
  users.fields.add(new TextField({ name: "gender", max: 20 }));
  users.fields.add(new BoolField({ name: "notifications" }));
  app.save(users);
  const usersId = app.findCollectionByNameOrId("users").id;

  const ownerRule = "user = @request.auth.id";
  const createOwnRule = "@request.auth.id != '' && @request.body.user = @request.auth.id";
  const autodates = [
    { type: "autodate", name: "created", onCreate: true, onUpdate: false },
    { type: "autodate", name: "updated", onCreate: true, onUpdate: true },
  ];

  // ---------- categories ----------
  app.save(new Collection({
    type: "base",
    name: "categories",
    listRule: "",
    viewRule: "",
    fields: [
      { type: "text", name: "title", required: true, max: 100 },
      { type: "number", name: "sort", onlyInt: true },
    ].concat(autodates),
  }));
  const categoriesCol = app.findCollectionByNameOrId("categories");

  // ---------- products ----------
  app.save(new Collection({
    type: "base",
    name: "products",
    listRule: "",
    viewRule: "",
    fields: [
      { type: "text", name: "title", required: true, max: 200 },
      { type: "text", name: "description", max: 5000 },
      { type: "number", name: "price", required: true, min: 1, onlyInt: true },
      { type: "text", name: "type", max: 100 },
      { type: "text", name: "consumption", max: 100 },
      { type: "relation", name: "category", collectionId: categoriesCol.id, maxSelect: 1, cascadeDelete: false },
      { type: "number", name: "sort", onlyInt: true },
    ].concat(autodates),
  }));
  const productsCol = app.findCollectionByNameOrId("products");

  // ---------- promotions ----------
  app.save(new Collection({
    type: "base",
    name: "promotions",
    listRule: "",
    viewRule: "",
    fields: [
      { type: "text", name: "title", required: true, max: 200 },
      { type: "number", name: "price", onlyInt: true },
      { type: "text", name: "color", max: 9 },
      { type: "file", name: "image", maxSelect: 1, maxSize: 5242880, mimeTypes: ["image/jpeg", "image/png", "image/webp"] },
      { type: "number", name: "sort", onlyInt: true },
    ].concat(autodates),
  }));
  const promotionsCol = app.findCollectionByNameOrId("promotions");

  // ---------- cart ----------
  app.save(new Collection({
    type: "base",
    name: "cart",
    listRule: ownerRule,
    viewRule: ownerRule,
    createRule: createOwnRule,
    updateRule: ownerRule,
    deleteRule: ownerRule,
    fields: [
      { type: "relation", name: "user", required: true, collectionId: usersId, maxSelect: 1, cascadeDelete: true },
      { type: "relation", name: "product", required: true, collectionId: productsCol.id, maxSelect: 1, cascadeDelete: true },
      { type: "number", name: "count", required: true, min: 1, onlyInt: true },
    ].concat(autodates),
  }));

  // ---------- orders ----------
  app.save(new Collection({
    type: "base",
    name: "orders",
    listRule: ownerRule,
    viewRule: ownerRule,
    createRule: createOwnRule,
    fields: [
      { type: "relation", name: "user", required: true, collectionId: usersId, maxSelect: 1, cascadeDelete: true },
      { type: "json", name: "items", maxSize: 2000000 },
      { type: "number", name: "total", onlyInt: true },
    ].concat(autodates),
  }));

  // ---------- projects ----------
  app.save(new Collection({
    type: "base",
    name: "projects",
    listRule: ownerRule,
    viewRule: ownerRule,
    createRule: createOwnRule,
    updateRule: ownerRule,
    deleteRule: ownerRule,
    fields: [
      { type: "relation", name: "user", required: true, collectionId: usersId, maxSelect: 1, cascadeDelete: true },
      { type: "text", name: "title", required: true, max: 200 },
      { type: "text", name: "type", max: 100 },
      { type: "text", name: "start_date", max: 10 },
      { type: "text", name: "end_date", max: 10 },
      { type: "text", name: "recipient", max: 100 },
      { type: "text", name: "source", max: 500 },
      { type: "text", name: "category", max: 100 },
      { type: "file", name: "image", maxSelect: 1, maxSize: 10485760, mimeTypes: ["image/jpeg", "image/png", "image/webp", "image/heic"] },
    ].concat(autodates),
  }));

  // ---------- тестовые данные ----------
  const categoryIds = {};
  [
    { key: "women", title: "Женщинам", sort: 1 },
    { key: "men", title: "Мужчинам", sort: 2 },
    { key: "kids", title: "Детям", sort: 3 },
    { key: "acc", title: "Аксессуары", sort: 4 },
  ].forEach((c) => {
    const record = new Record(categoriesCol);
    record.set("title", c.title);
    record.set("sort", c.sort);
    app.save(record);
    categoryIds[c.key] = record.id;
  });

  [
    {
      title: "Рубашка Воскресенье для машинного вязания", type: "Мужская одежда", price: 690, consumption: "80-90 г", category: "men",
      description: "Мой выбор для этих шапок — кардные составы, которые раскрываются деликатным пушком. Кашемиры, мериносы, смесовки с ними отлично подойдут на шапку. Кардные составы берите в большое количество сложений, вязать будем резинку 1х1, плотненько. Пряжу 1400–1500 м в 100 г в 4 сложения, пряжу 700 м в 2 сложения. Ориентир для конечной толщины — 300–350 м в 100 г. Артикулы, из которых мы вязали эту модель: Zermatt Zegna Baruffa, Cashfive, Baby Cashmere Loro Piana, Soft Donegal и другие. Примерный расход на шапку с подгибом 70-90 г.",
    },
    {
      title: "Шорты Вторник для машинного вязания", type: "Мужская одежда", price: 300, consumption: "150-180 г", category: "men",
      description: "Лёгкие летние шорты из хлопка с вискозой. Вяжутся гладью на 5 классе, пояс — двойная резинка под шнурок. Подойдёт пряжа 400–600 м в 100 г в 2–3 сложения.",
    },
    {
      title: "Свитер Четверг оверсайз", type: "Мужская одежда", price: 850, consumption: "450-500 г", category: "men",
      description: "Свободный свитер со спущенным плечом. Основное полотно — гладь, манжеты и горловина — резинка 2х2. Рекомендуем меринос или смесовку с кашемиром, итоговая толщина 250–300 м в 100 г.",
    },
    {
      title: "Платье Суббота миди", type: "Женская одежда", price: 790, consumption: "350-400 г", category: "women",
      description: "Приталенное платье миди с разрезом. Вяжется снизу вверх, талия формируется убавками. Лучше всего смотрится в вискозе и шёлке с шерстью.",
    },
    {
      title: "Кардиган Пятница", type: "Женская одежда", price: 650, consumption: "400-450 г", category: "women",
      description: "Удлинённый кардиган с карманами и планкой на пуговицах. Детали вяжутся отдельно и сшиваются кеттельным швом.",
    },
    {
      title: "Топ Понедельник в рубчик", type: "Женская одежда", price: 350, consumption: "120-150 г", category: "women",
      description: "Облегающий топ в рубчик 1х1 на тонких бретелях. Хлопок с эластаном держит форму и не растягивается.",
    },
    {
      title: "Комбинезон Малыш", type: "Детская одежда", price: 450, consumption: "150-200 г", category: "kids",
      description: "Мягкий комбинезон для малышей 0–12 месяцев с застёжкой по ножкам. Берите гипоаллергенную пряжу: хлопок, бамбук, мериносовую шерсть.",
    },
    {
      title: "Варежки Снежинка", type: "Детская одежда", price: 180, consumption: "40-50 г", category: "kids",
      description: "Тёплые детские варежки с жаккардовой снежинкой. Двойной манжет не пропускает снег.",
    },
    {
      title: "Шапка Среда с отворотом", type: "Аксессуары", price: 250, consumption: "60-80 г", category: "acc",
      description: "Классическая шапка с отворотом резинкой 1х1. Размер регулируется глубиной отворота.",
    },
    {
      title: "Шарф Выходной", type: "Аксессуары", price: 200, consumption: "150-180 г", category: "acc",
      description: "Длинный шарф английской резинкой — объёмный и тёплый. Отлично подходит для первой модели на машине.",
    },
  ].forEach((p, index) => {
    const record = new Record(productsCol);
    record.set("title", p.title);
    record.set("description", p.description);
    record.set("price", p.price);
    record.set("type", p.type);
    record.set("consumption", p.consumption);
    record.set("category", categoryIds[p.category]);
    record.set("sort", index + 1);
    app.save(record);
  });

  [
    { title: "Шорты Вторник", price: 4000, color: "#6CCFDD", sort: 1 },
    { title: "Рубашка Воскресенье", price: 8000, color: "#4D8DF5", sort: 2 },
    { title: "Свитер Четверг", price: 5500, color: "#9B8CF2", sort: 3 },
  ].forEach((p) => {
    const record = new Record(promotionsCol);
    record.set("title", p.title);
    record.set("price", p.price);
    record.set("color", p.color);
    record.set("sort", p.sort);
    app.save(record);
  });

  const demo = new Record(app.findCollectionByNameOrId("users"));
  demo.set("email", "demo@matule.ru");
  demo.set("emailVisibility", true);
  demo.set("verified", true);
  demo.set("name", "Эдуард");
  demo.set("patronymic", "Олегович");
  demo.set("surname", "Тицкий");
  demo.set("birthday", "28.02.1991");
  demo.set("gender", "Мужской");
  demo.set("notifications", true);
  demo.setPassword("Matule2026!");
  app.save(demo);
}, (app) => {
  ["cart", "orders", "projects", "products", "promotions", "categories"].forEach((name) => {
    try {
      app.delete(app.findCollectionByNameOrId(name));
    } catch (_) {}
  });
  try {
    app.delete(app.findAuthRecordByEmail("users", "demo@matule.ru"));
  } catch (_) {}
  const users = app.findCollectionByNameOrId("users");
  ["patronymic", "surname", "birthday", "gender", "notifications"].forEach((name) => users.fields.removeByName(name));
  app.save(users);
});
