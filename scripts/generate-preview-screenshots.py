#!/usr/bin/env python3
"""Generate UI preview screenshots for KidSpace Launcher (no emulator/KVM required)."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).resolve().parents[1] / "docs" / "screenshots"
OUT.mkdir(parents=True, exist_ok=True)

W, H = 1080, 2400

PRIMARY = (79, 154, 216)
SECONDARY = (126, 200, 227)
ACCENT = (255, 171, 118)


def gradient(draw, y0, y1, c1, c2):
    for y in range(y0, y1):
        t = (y - y0) / max(y1 - y0 - 1, 1)
        r = int(c1[0] * (1 - t) + c2[0] * t)
        g = int(c1[1] * (1 - t) + c2[1] * t)
        b = int(c1[2] * (1 - t) + c2[2] * t)
        draw.line([(0, y), (W, y)], fill=(r, g, b))


def font(size: int, bold: bool = False):
    candidates = [
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def rounded_rect(draw, xy, radius, fill, outline=None, width=0):
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def draw_sky_meadow_scene(draw):
    gradient(draw, 0, H, (184, 228, 255), (200, 240, 212))
    draw.ellipse((W - 220, 100, W - 60, 260), fill=ACCENT)
    draw.ellipse((120, 180, 280, 260), fill=(255, 255, 255))
    draw.ellipse((170, 160, 330, 250), fill=(255, 255, 255))
    draw.ellipse((230, 175, 380, 255), fill=(255, 255, 255))
    draw.pieslice((-80, H - 520, W + 80, H + 120), 180, 360, fill=(129, 199, 132))
    draw.pieslice((120, H - 460, W + 120, H + 180), 180, 360, fill=(102, 187, 106))


def draw_tile(draw, x, y, size, label, icon_color, glyph, use_well=True):
    rounded_rect(draw, (x, y, x + size, y + size), 28, (255, 255, 255, 242))
    pad = 14
    label_h = 38
    icon_area = size - pad * 2 - label_h
    icon_y = y + pad
    icon_x = x + (size - icon_area) // 2
    if use_well:
        rounded_rect(draw, (icon_x, icon_y, icon_x + icon_area, icon_y + icon_area), 22, (242, 245, 249))
        inner = int(icon_area * 0.72)
        inner_x = icon_x + (icon_area - inner) // 2
        inner_y = icon_y + (icon_area - inner) // 2
        rounded_rect(draw, (inner_x, inner_y, inner_x + inner, inner_y + inner), 18, icon_color)
        draw.text((inner_x + inner // 2, inner_y + inner // 2), glyph, fill="white", font=font(int(inner * 0.34), True), anchor="mm")
    else:
        rounded_rect(draw, (x + pad, y + pad, x + size - pad, y + size - pad - label_h), 18, icon_color)
        draw.text((x + size // 2, y + (size - label_h) // 2 + pad), glyph, fill="white", font=font(int(size * 0.22), True), anchor="mm")
    draw.text((x + size // 2, y + size - 20), label, fill=PRIMARY, font=font(22, True), anchor="mm")


def child_home():
    img = Image.new("RGB", (W, H), "#C8F0D4")
    draw = ImageDraw.Draw(img)
    draw_sky_meadow_scene(draw)

    rounded_rect(draw, (50, 110, W - 50, 390), 48, PRIMARY)
    draw.text((W // 2, 175), "✨ KidSpace ✨", fill="white", font=font(52, True), anchor="mm")
    draw.text((W // 2, 245), "Tap your favorites below!", fill="white", font=font(28), anchor="mm")
    draw.text((W // 2, 310), "Parents: long-press here for parent mode", fill=SECONDARY, font=font(24), anchor="mm")

    tiles = [
        ("YouTube", ACCENT, "▶"),
        ("PBS Kids", PRIMARY, "P"),
        ("Khan", SECONDARY, "K"),
        ("Stories", ACCENT, "📖"),
        ("Duolingo", PRIMARY, "D"),
        ("My Look", ACCENT, "🎨"),
    ]

    cols, gap, margin_x, start_y = 3, 24, 56, 430
    tile_size = (W - margin_x * 2 - gap * (cols - 1)) // cols
    for i, (label, color, glyph) in enumerate(tiles):
        col = i % cols
        row = i // cols
        x = margin_x + col * (tile_size + gap)
        y = start_y + row * (tile_size + gap)
        draw_tile(draw, x, y, tile_size, label, color, glyph)

    img.save(OUT / "01-child-home.png")


def parent_gate():
    img = Image.new("RGB", (W, H), "#3949AB")
    draw = ImageDraw.Draw(img)
    gradient(draw, 0, H, (92, 107, 192), (57, 73, 171))

    draw.text((W // 2, 220), "🔒", fill="white", font=font(72), anchor="mm")
    draw.text((W // 2, 320), "Grown-Up Area", fill="white", font=font(52, True), anchor="mm")
    draw.text((W // 2, 400), "Read the numbers and enter them below:", fill=(220, 220, 255), font=font(28), anchor="mm")

    rounded_rect(draw, (80, 500, W - 80, 760), 48, "white")
    draw.text((W // 2, 580), "three, seven, one, nine", fill=(57, 73, 171), font=font(40, True), anchor="mm")
    draw.text((W // 2, 680), "3 7 1 9", fill=(26, 35, 126), font=font(56, True), anchor="mm")

    pad_y = 860
    keys = [["1", "2", "3"], ["4", "5", "6"], ["7", "8", "9"], ["", "0", "⌫"]]
    for row_i, row in enumerate(keys):
        for col_i, key in enumerate(row):
            if not key:
                continue
            x = 120 + col_i * 280
            y = pad_y + row_i * 120
            rounded_rect(draw, (x, y, x + 220, y + 90), 32, (255, 255, 255, 235))
            draw.text((x + 110, y + 45), key, fill=(57, 73, 171), font=font(40, True), anchor="mm")

    rounded_rect(draw, (120, 1380, 500, 1480), 24, (255, 255, 255, 40), outline="white", width=3)
    draw.text((310, 1430), "Cancel", fill="white", font=font(32), anchor="mm")
    rounded_rect(draw, (580, 1380, 960, 1480), 24, (255, 213, 79))
    draw.text((770, 1430), "Unlock", fill=(26, 35, 126), font=font(32, True), anchor="mm")

    img.save(OUT / "02-parent-gate.png")


def parent_mode():
    img = Image.new("RGB", (W, H), "#F5F5F5")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, W, 180), fill=(57, 73, 171))
    draw.text((60, 90), "Parent Mode", fill="white", font=font(44, True), anchor="lm")

    items = ["YouTube Kids", "PBS Kids", "Khan Academy", "National Geographic Kids"]
    y = 220
    for label in items:
        rounded_rect(draw, (40, y, W - 40, y + 120), 32, "white")
        rounded_rect(draw, (60, y + 20, 140, y + 100), 20, PRIMARY)
        draw.text((100, y + 60), label[0], fill="white", font=font(36, True), anchor="mm")
        draw.text((170, y + 60), label, fill="#333333", font=font(32), anchor="lm")
        y += 140

    draw.rectangle((0, H - 160, W, H), fill="white")
    tabs = [("Tiles", True), ("Apps", False), ("Look", False)]
    for i, (name, active) in enumerate(tabs):
        x = 120 + i * 300
        color = (57, 73, 171) if active else (150, 150, 150)
        draw.text((x, H - 90), name, fill=color, font=font(30, active), anchor="mm")

    img.save(OUT / "03-parent-mode.png")


def appearance_tab():
    img = Image.new("RGB", (W, H), "#F5F5F5")
    draw = ImageDraw.Draw(img)
    draw.rectangle((0, 0, W, 180), fill=(57, 73, 171))
    draw.text((60, 90), "Parent Mode", fill="white", font=font(44, True), anchor="lm")

    draw.text((50, 220), "Your Photo", fill="#222222", font=font(34, True), anchor="lm")
    draw.text((50, 270), "Upload a JPEG or PNG from your device", fill="#777777", font=font(24), anchor="lm")
    rounded_rect(draw, (50, 320, 500, 400), 20, "white", outline=PRIMARY, width=3)
    draw.text((275, 360), "📷  Choose photo", fill=PRIMARY, font=font(28), anchor="mm")

    draw.text((50, 450), "Illustrated Backgrounds", fill="#222222", font=font(34, True), anchor="lm")
    presets = [
        ("Sky Meadow", (184, 228, 255), (200, 240, 212)),
        ("Ocean Bubbles", (129, 212, 250), (77, 182, 172)),
        ("Candy Clouds", (248, 187, 217), (209, 196, 233)),
    ]
    y = 510
    for i, (name, c1, c2) in enumerate(presets):
        rounded_rect(draw, (50, y, W - 50, y + 100), 24, "white", outline=PRIMARY if i == 0 else None, width=4 if i == 0 else 0)
        rounded_rect(draw, (70, y + 20, 150, y + 80), 16, c2)
        draw.rectangle((75, y + 25, 145, y + 75), fill=c1)
        draw.text((180, y + 50), name, fill="#333333", font=font(30), anchor="lm")
        draw.text((180, y + 78), "Recolorable vector scene", fill="#999999", font=font(20), anchor="lm")
        y += 120

    draw.rectangle((0, H - 160, W, H), fill="white")
    tabs = [("Tiles", False), ("Apps", False), ("Look", True)]
    for i, (name, active) in enumerate(tabs):
        x = 120 + i * 300
        color = (57, 73, 171) if active else (150, 150, 150)
        draw.text((x, H - 90), name, fill=color, font=font(30, active), anchor="mm")

    img.save(OUT / "04-appearance.png")


if __name__ == "__main__":
    child_home()
    parent_gate()
    parent_mode()
    appearance_tab()
    print(f"Wrote previews to {OUT}")
