from pathlib import Path
from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src" / "main" / "resources" / "assets" / "icons" / "app-icon.png"
DESTINATION = ROOT / "src" / "main" / "resources" / "assets" / "icons" / "app-icon.ico"


def main() -> None:
    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    image = Image.open(SOURCE).convert("RGBA")
    sizes = [(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)]
    image.save(DESTINATION, format="ICO", sizes=sizes)


if __name__ == "__main__":
    main()
