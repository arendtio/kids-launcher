#!/usr/bin/env python3
"""Generate UI preview screenshots for KidSpace Launcher (no emulator/KVM required)."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).resolve().parents[1] / "docs" / "screenshots"
OUT.mkdir(parents=True, exist_ok=True)

W, H = 1080, 2400


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


def child_home():
    img = Image.new("RGB", (W, H), "#AED581")
    draw = ImageDraw.Draw(img)
    gradient(draw, 0, H, (255, 241, 118), (174, 213, 129))

    rounded_rect(draw, (60, 120, W - 60, 340), 56, (107, 157, 255, 230))
    draw.text((W // 2, 190), "✨ KidSpace ✨", fill="white", font=font(56, True), anchor="mm")
    draw.text((W // 2, 270), "Tap your favorites below!", fill=(255, 217, 61), font=font(32), anchor="mm")

    tiles = [
        ("YouTube Kids", (255, 107, 157), "▶"),
        ("PBS Kids", (76, 175, 80), "P"),
        ("Khan Academy", (255, 152, 0), "K"),
        ("Story Time", (156, 39, 176), "📖"),
    ]
    positions = [(60, 400), (W // 2 + 20, 400), (60, 920), (W // 2 + 20, 920)]
    for i, (label, accent, glyph) in enumerate(tiles):
        x, y = positions[i]
        rounded_rect(draw, (x, y, x + 460, y + 460), 56, (255, 255, 255))
        rounded_rect(draw, (x + 70, y + 50, x + 390, y + 290), 40, accent)
        draw.text((x + 230, y + 170), glyph, fill="white", font=font(72, True), anchor="mm")
        draw.text((x + 230, y + 360), label, fill=(107, 157, 255), font=font(34, True), anchor="mm")

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
        rounded_rect(draw, (60, y + 20, 140, y + 100), 20, (107, 157, 255))
        draw.text((90, y + 60), label[0], fill="white", font=font(36, True), anchor="mm")
        draw.text((170, y + 60), label, fill="#333333", font=font(32), anchor="lm")
        y += 140

    draw.rectangle((0, H - 160, W, H), fill="white")
    tabs = [("Tiles", True), ("Apps", False), ("Look", False)]
    for i, (name, active) in enumerate(tabs):
        x = 120 + i * 300
        color = (57, 73, 171) if active else (150, 150, 150)
        draw.text((x, H - 90), name, fill=color, font=font(30, active), anchor="mm")

    img.save(OUT / "03-parent-mode.png")


if __name__ == "__main__":
    child_home()
    parent_gate()
    parent_mode()
    print(f"Wrote previews to {OUT}")
