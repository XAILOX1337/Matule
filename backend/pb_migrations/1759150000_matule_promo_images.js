/// <reference path="../pb_data/types.d.ts" />

/**
 * Картинки для баннеров «Акции и новости» (файлы в папке backend/pb_seed).
 *
 * Путь к файлам относительный, поэтому сервер запускается из папки backend (см. README).
 * Если файлов нет, баннеры просто останутся без картинок — остальное работает как обычно.
 */
migrate((app) => {
  const images = {
    "Шорты Вторник": "promo_shorts.png",
    "Рубашка Воскресенье": "promo_shirt.png",
    "Свитер Четверг": "promo_sweater.png",
  };
  app.findAllRecords("promotions").forEach((record) => {
    const fileName = images[record.getString("title")];
    if (!fileName || record.getString("image")) {
      return;
    }
    try {
      record.set("image", $filesystem.fileFromPath("pb_seed/" + fileName));
      app.save(record);
    } catch (err) {
      console.log("Картинка акции не подключена: " + fileName + ": " + err);
    }
  });
}, (app) => {
  // Откат не нужен: картинки удаляются вместе с записями акций.
});
