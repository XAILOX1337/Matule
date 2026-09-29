#!/usr/bin/env python3
"""
Генератор иконочного шрифта Matule.

Собирает `uikit/src/main/res/font/matule_icons.ttf` из иконок макета и генерирует
enum `MatuleIcons.kt` с кодами глифов, чтобы код и шрифт не расходились.

Иконки описаны ниже на сетке 24×24 (как в Figma) линиями толщиной 1.7 с круглыми
концами. Если положить в папку `svg/` файл `<name>.svg`, выгруженный из Figma
(перед экспортом: Outline stroke, чтобы получились заливки), он заменит
нарисованный вариант этой иконки.

Запуск (нужен fontTools: `pip install fonttools`):
    python3 build_icon_font.py
    python3 build_icon_font.py --preview preview.png   # PNG со всеми иконками (нужен Pillow)
"""
import argparse
import math
import os

from fontTools.fontBuilder import FontBuilder
from fontTools.pens.cu2quPen import Cu2QuPen
from fontTools.pens.transformPen import TransformPen
from fontTools.pens.ttGlyphPen import TTGlyphPen

UPM = 1024
GRID = 24.0
STROKE = 1.7
HW = STROKE / 2
FIRST_CODEPOINT = 0xE900

HERE = os.path.dirname(os.path.abspath(__file__))
PROJECT = os.path.normpath(os.path.join(HERE, "..", ".."))
FONT_OUT = os.path.join(PROJECT, "uikit", "src", "main", "res", "font", "matule_icons.ttf")
KOTLIN_OUT = os.path.join(PROJECT, "uikit", "src", "main", "java", "ru", "matule", "uikit", "icons", "MatuleIcons.kt")
SVG_DIR = os.path.join(HERE, "svg")


# ---------------------------------------------------------------- геометрия

def circle_points(cx, cy, r, n=48):
    return [(cx + r * math.cos(2 * math.pi * k / n), cy + r * math.sin(2 * math.pi * k / n)) for k in range(n)]


def arc_points(cx, cy, r, a0, a1, n=None):
    """Точки дуги; углы в градусах, ось Y направлена вниз, 90° — вверх."""
    n = n or max(8, int(abs(a1 - a0) / 5))
    return [
        (cx + r * math.cos(math.radians(a0 + (a1 - a0) * k / n)),
         cy - r * math.sin(math.radians(a0 + (a1 - a0) * k / n)))
        for k in range(n + 1)
    ]


def quad_points(p0, p1, p2, n=24):
    pts = []
    for k in range(n + 1):
        t = k / n
        x = (1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0]
        y = (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]
        pts.append((x, y))
    return pts


def rounded_rect_points(x, y, w, h, r, n=8):
    r = max(0.001, min(r, w / 2, h / 2))
    pts = []
    corners = [
        (x + r, y + r, 180, 270),
        (x + w - r, y + r, 270, 360),
        (x + w - r, y + h - r, 0, 90),
        (x + r, y + h - r, 90, 180),
    ]
    for cx, cy, a0, a1 in corners:
        for k in range(n + 1):
            a = math.radians(a0 + (a1 - a0) * k / n)
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return pts


class Icon:
    """Иконка как набор залитых контуров (fills) и отверстий (holes) на сетке 24×24."""

    def __init__(self):
        self.fills = []
        self.holes = []
        self.rotation = 0.0

    def dot(self, cx, cy, r):
        self.fills.append(circle_points(cx, cy, r, 24))

    def line(self, x1, y1, x2, y2):
        dx, dy = x2 - x1, y2 - y1
        length = math.hypot(dx, dy)
        if length == 0:
            self.dot(x1, y1, HW)
            return
        nx, ny = -dy / length * HW, dx / length * HW
        self.fills.append([(x1 + nx, y1 + ny), (x2 + nx, y2 + ny), (x2 - nx, y2 - ny), (x1 - nx, y1 - ny)])
        self.dot(x1, y1, HW)
        self.dot(x2, y2, HW)

    def polyline(self, pts, closed=False):
        seq = list(pts) + ([pts[0]] if closed else [])
        for a, b in zip(seq, seq[1:]):
            self.line(a[0], a[1], b[0], b[1])

    def circle(self, cx, cy, r):
        self.fills.append(circle_points(cx, cy, r + HW))
        self.holes.append(circle_points(cx, cy, r - HW))

    def arc(self, cx, cy, r, a0, a1):
        outer = arc_points(cx, cy, r + HW, a0, a1)
        inner = list(reversed(arc_points(cx, cy, r - HW, a0, a1)))
        self.fills.append(outer + inner)
        start = arc_points(cx, cy, r, a0, a0, 1)[0]
        end = arc_points(cx, cy, r, a1, a1, 1)[0]
        self.dot(start[0], start[1], HW)
        self.dot(end[0], end[1], HW)

    def rrect(self, x, y, w, h, r):
        self.fills.append(rounded_rect_points(x - HW, y - HW, w + STROKE, h + STROKE, r + HW))
        self.holes.append(rounded_rect_points(x + HW, y + HW, w - STROKE, h - STROKE, max(r - HW, 0.001)))

    def rotate(self, degrees):
        self.rotation = degrees

    def contours(self):
        def rot(p):
            if not self.rotation:
                return p
            a = math.radians(self.rotation)
            x, y = p[0] - 12, p[1] - 12
            return (12 + x * math.cos(a) - y * math.sin(a), 12 + x * math.sin(a) + y * math.cos(a))

        for c in self.fills:
            yield [rot(p) for p in c], False
        for c in self.holes:
            yield [rot(p) for p in c], True


# ---------------------------------------------------------------- иконки

def eye_outline(i):
    top = quad_points((2.5, 12), (12, 3.5), (21.5, 12))
    bottom = quad_points((21.5, 12), (12, 20.5), (2.5, 12))
    i.polyline(top + bottom[1:-1], closed=True)


def draw_back(i): i.polyline([(15, 5), (8, 12), (15, 19)])
def draw_chevron_down(i): i.polyline([(6, 9), (12, 15), (18, 9)])
def draw_chevron_right(i): i.polyline([(9, 5), (16, 12), (9, 19)])
def draw_search(i): i.circle(10.5, 10.5, 6.5); i.line(15.3, 15.3, 20, 20)
def draw_plus(i): i.line(12, 5, 12, 19); i.line(5, 12, 19, 12)
def draw_minus(i): i.line(5, 12, 19, 12)
def draw_close(i): i.line(6, 6, 18, 18); i.line(18, 6, 6, 18)


def draw_trash(i):
    i.line(4, 6.5, 20, 6.5)
    i.polyline([(9, 6.5), (9, 4), (15, 4), (15, 6.5)])
    i.polyline([(6, 6.5), (7, 20), (17, 20), (18, 6.5)])
    i.line(10, 10.5, 10, 16.5)
    i.line(14, 10.5, 14, 16.5)


def draw_cart(i):
    i.polyline([(2.5, 4), (5.2, 4), (7.4, 15), (18, 15), (20.2, 7.5), (6, 7.5)])
    i.dot(9, 19, 1.6)
    i.dot(16.5, 19, 1.6)


def draw_check(i): i.polyline([(5, 12.5), (10, 17), (19, 7)])
def draw_eye(i): eye_outline(i); i.circle(12, 12, 3)
def draw_eye_off(i): eye_outline(i); i.circle(12, 12, 3); i.line(4, 4, 20, 20)
def draw_filter(i): i.polyline([(4, 5), (20, 5), (14, 12.5), (14, 19), (10, 17), (10, 12.5)], closed=True)


def draw_home(i):
    i.polyline([(3.5, 11), (12, 4), (20.5, 11)])
    i.polyline([(6, 9.5), (6, 20), (18, 20), (18, 9.5)])
    i.polyline([(10, 20), (10, 14.5), (14, 14.5), (14, 20)])


def draw_catalog(i):
    for x, y in [(4, 4), (13, 4), (4, 13), (13, 13)]:
        i.rrect(x, y, 7, 7, 1.8)


def draw_projects(i):
    i.rrect(5, 3, 14, 18, 2.2)
    i.line(8.5, 8, 15.5, 8)
    i.line(8.5, 12, 15.5, 12)
    i.line(8.5, 16, 12.5, 16)


def draw_profile(i): i.circle(12, 8, 3.8); i.arc(12, 21, 7.5, 20, 160)
def draw_more(i): i.dot(5.5, 12, 1.8); i.dot(12, 12, 1.8); i.dot(18.5, 12, 1.8)


def draw_backspace(i):
    i.polyline([(8.5, 5.5), (20.5, 5.5), (20.5, 18.5), (8.5, 18.5), (2.5, 12)], closed=True)
    i.line(11.5, 9.5, 16.5, 14.5)
    i.line(16.5, 9.5, 11.5, 14.5)


def draw_camera(i):
    i.rrect(3, 7, 18, 13, 2.5)
    i.polyline([(8, 7), (9.5, 4.5), (14.5, 4.5), (16, 7)])
    i.circle(12, 13.5, 3.5)


def draw_gallery(i):
    i.rrect(3, 4, 18, 16, 2.5)
    i.polyline([(3.5, 17), (9, 11.5), (13, 15.5), (15.5, 13), (20.5, 18)])
    i.dot(15.5, 8.5, 1.7)


def draw_chat(i): i.rrect(3, 4, 18, 13, 3); i.polyline([(8, 17), (8, 21), (12.5, 17)])


def draw_download(i):
    i.line(12, 4, 12, 15)
    i.polyline([(7.5, 10.5), (12, 15), (16.5, 10.5)])
    i.polyline([(4, 16), (4, 20), (20, 20), (20, 16)])


def draw_location(i):
    i.polyline(arc_points(12, 10, 7, -35, 215) + [(12, 21.5)], closed=True)
    i.circle(12, 10, 2.6)


def draw_send(i):
    i.polyline([(3, 11.2), (21, 3.5), (14.5, 21), (11.3, 12.7)], closed=True)
    i.line(11.3, 12.7, 21, 3.5)


def draw_mic(i): i.rrect(9, 3, 6, 11, 3); i.arc(12, 11, 6.5, 200, 340); i.line(12, 17.5, 12, 21)
def draw_attach(i): i.rrect(9, 2.5, 6, 19, 3); i.line(12, 7, 12, 16.5); i.rotate(45)


def draw_document(i):
    i.polyline([(6, 3), (14, 3), (19, 8), (19, 21), (6, 21)], closed=True)
    i.polyline([(14, 3), (14, 8), (19, 8)])
    i.line(9, 12.5, 16, 12.5)
    i.line(9, 16.5, 16, 16.5)


# (имя в шрифте, имя в Kotlin, русское название, функция рисования)
ICONS = [
    ("back", "Back", "Назад", draw_back),
    ("chevron_down", "ChevronDown", "Раскрыть", draw_chevron_down),
    ("chevron_right", "ChevronRight", "Вперёд", draw_chevron_right),
    ("search", "Search", "Поиск", draw_search),
    ("plus", "Plus", "Добавить", draw_plus),
    ("minus", "Minus", "Убавить", draw_minus),
    ("close", "Close", "Закрыть", draw_close),
    ("trash", "Trash", "Удалить", draw_trash),
    ("cart", "Cart", "Корзина", draw_cart),
    ("check", "Check", "Готово", draw_check),
    ("eye", "Eye", "Показать", draw_eye),
    ("eye_off", "EyeOff", "Скрыть", draw_eye_off),
    ("filter", "Filter", "Фильтр", draw_filter),
    ("home", "Home", "Главная", draw_home),
    ("catalog", "Catalog", "Каталог", draw_catalog),
    ("projects", "Projects", "Проекты", draw_projects),
    ("profile", "Profile", "Профиль", draw_profile),
    ("more", "More", "Ещё", draw_more),
    ("backspace", "Backspace", "Стереть", draw_backspace),
    ("camera", "Camera", "Камера", draw_camera),
    ("gallery", "Gallery", "Галерея", draw_gallery),
    ("chat", "Chat", "Чат", draw_chat),
    ("download", "Download", "Загрузить", draw_download),
    ("location", "Location", "Карта", draw_location),
    ("send", "Send", "Отправить", draw_send),
    ("mic", "Mic", "Микрофон", draw_mic),
    ("attach", "Attach", "Прикрепить", draw_attach),
    ("document", "Document", "Документ", draw_document),
]


# ---------------------------------------------------------------- шрифт

def to_font(p):
    return (round(p[0] / GRID * UPM), round((GRID - p[1]) / GRID * UPM))


def signed_area(pts):
    return 0.5 * sum(pts[k][0] * pts[(k + 1) % len(pts)][1] - pts[(k + 1) % len(pts)][0] * pts[k][1] for k in range(len(pts)))


def glyph_from_icon(icon):
    pen = TTGlyphPen(None)
    for contour, is_hole in icon.contours():
        pts = [to_font(p) for p in contour]
        cleaned = [pts[0]] + [p for prev, p in zip(pts, pts[1:]) if p != prev]
        if len(cleaned) > 1 and cleaned[0] == cleaned[-1]:
            cleaned.pop()
        if len(cleaned) < 3:
            continue
        area = signed_area(cleaned)
        # TrueType: внешние контуры по часовой стрелке, отверстия — против
        if (not is_hole and area > 0) or (is_hole and area < 0):
            cleaned.reverse()
        pen.moveTo(cleaned[0])
        for p in cleaned[1:]:
            pen.lineTo(p)
        pen.closePath()
    return pen.glyph()


def glyph_from_svg(path):
    from fontTools.svgLib import SVGPath
    import xml.etree.ElementTree as ET

    root = ET.parse(path).getroot()
    view_box = [float(v) for v in root.get("viewBox", "0 0 24 24").replace(",", " ").split()]
    scale = UPM / max(view_box[2], view_box[3])
    ttpen = TTGlyphPen(None)
    pen = TransformPen(Cu2QuPen(ttpen, max_err=1.0, reverse_direction=False),
                       (scale, 0, 0, -scale, -view_box[0] * scale, UPM + view_box[1] * scale))
    SVGPath(path).draw(pen)
    return ttpen.glyph()


def build_font():
    glyph_order = [".notdef", "space"] + [name for name, *_ in ICONS]
    cmap = {0x20: "space"}
    glyphs = {".notdef": TTGlyphPen(None).glyph(), "space": TTGlyphPen(None).glyph()}
    for index, (name, _, _, draw) in enumerate(ICONS):
        cmap[FIRST_CODEPOINT + index] = name
        svg = os.path.join(SVG_DIR, name + ".svg")
        if os.path.exists(svg):
            glyphs[name] = glyph_from_svg(svg)
        else:
            icon = Icon()
            draw(icon)
            glyphs[name] = glyph_from_icon(icon)

    fb = FontBuilder(UPM, isTTF=True)
    fb.setupGlyphOrder(glyph_order)
    fb.setupCharacterMap(cmap)
    fb.setupGlyf(glyphs)
    glyf = fb.font["glyf"]
    metrics = {}
    for name in glyph_order:
        glyph = glyf[name]
        glyph.recalcBounds(glyf)
        metrics[name] = (UPM if name != "space" else UPM // 3, getattr(glyph, "xMin", 0))
    fb.setupHorizontalMetrics(metrics)
    fb.setupHorizontalHeader(ascent=UPM, descent=0)
    fb.setupNameTable({"familyName": "Matule Icons", "styleName": "Regular"})
    fb.setupOS2(sTypoAscender=UPM, sTypoDescender=0, sTypoLineGap=0, usWinAscent=UPM, usWinDescent=0, achVendID="MATU", fsType=0)
    fb.setupPost()
    os.makedirs(os.path.dirname(FONT_OUT), exist_ok=True)
    fb.save(FONT_OUT)


def build_kotlin():
    lines = [
        "package ru.matule.uikit.icons",
        "",
        "/**",
        " * Иконки Matule. Каждая иконка — глиф шрифта `res/font/matule_icons.ttf`, собранного из иконок",
        " * макета скриптом `tools/icons/build_icon_font.py`. Файл сгенерирован этим же скриптом — не редактируйте вручную.",
        " *",
        " * @property glyph символ глифа в шрифте (область Private Use Area).",
        " * @property title русское название: подпись в showcase и описание для TalkBack по умолчанию.",
        " */",
        "enum class MatuleIcons(val glyph: String, val title: String) {",
    ]
    for index, (_, kotlin_name, title, _) in enumerate(ICONS):
        sep = "," if index < len(ICONS) - 1 else ","
        lines.append(f'    {kotlin_name}("\\u{FIRST_CODEPOINT + index:04X}", "{title}"){sep}')
    lines.append("}")
    os.makedirs(os.path.dirname(KOTLIN_OUT), exist_ok=True)
    with open(KOTLIN_OUT, "w", encoding="utf-8") as f:
        f.write("\n".join(lines) + "\n")


def build_preview(path):
    from PIL import Image, ImageDraw, ImageFont

    cols, cell, size = 7, 150, 64
    rows = (len(ICONS) + cols - 1) // cols
    image = Image.new("RGB", (cols * cell, rows * cell), "white")
    draw = ImageDraw.Draw(image)
    icon_font = ImageFont.truetype(FONT_OUT, size)
    label_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
    for index, (name, *_rest) in enumerate(ICONS):
        x, y = (index % cols) * cell, (index // cols) * cell
        draw.rectangle([x + (cell - size) // 2, y + 20, x + (cell + size) // 2, y + 20 + size], outline="#EEEEEE")
        draw.text((x + (cell - size) // 2, y + 20), chr(FIRST_CODEPOINT + index), font=icon_font, fill="black")
        draw.text((x + 10, y + 105), name, font=label_font, fill="#555555")
    image.save(path)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--preview", help="сохранить PNG со всеми иконками")
    args = parser.parse_args()
    build_font()
    build_kotlin()
    print("font:", FONT_OUT)
    print("kotlin:", KOTLIN_OUT)
    if args.preview:
        build_preview(args.preview)
        print("preview:", args.preview)
