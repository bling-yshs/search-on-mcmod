import os
import json
from pathlib import Path

VERSION_MAP = {
    "1.21.x": ["1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21"],
    "1.20.x": ["1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"],
    "1.18.x": ["1.18.2", "1.18.1", "1.18"],
    "1.19.x": ["1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19"],
    "1.20.6": ["1.20.6"]
}

def parse_filename(filename):
    parts = filename.stem.split('-')
    if len(parts) >= 4 and parts[0] == "searchonmcmod":
        loader = parts[1].lower()
        mc_version = parts[2]
        return {
            "name": filename.stem,
            "file": filename.name,
            "loader": "neoforge" if loader == "neoforge" else loader,
            "mc_version": mc_version,
            "game_versions": ",".join(VERSION_MAP.get(mc_version, [mc_version]))
        }
    return None

def main():
    matrix = []
    for file in Path("./release_assets").glob("*.jar"):
        if meta := parse_filename(file):
            matrix.append(meta)

    print(json.dumps({"include": matrix}))

if __name__ == "__main__":
    main()
