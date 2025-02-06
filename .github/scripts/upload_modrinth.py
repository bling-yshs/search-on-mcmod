import os
import json
from pathlib import Path

VERSION_MAP = {
    "1.21.x": ["1.21.4", "1.21.3", "1.21.2", "1.21.1", "1.21"],
    "1.20.6": ["1.20.6"],
    "1.20.x": ["1.20.4", "1.20.3", "1.20.2", "1.20.1", "1.20"],
    "1.19.x": ["1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.19"],
    "1.18.x": ["1.18.2", "1.18.1", "1.18"],
    "1.16.5": ["1.16.5"],
    "1.12.2": ["1.12.2"]
}

def parse_filename(filename):
    parts = filename.stem.split('-')
    if len(parts) >= 4 and parts[0] == "searchonmcmod":
        loader = parts[1].lower()
        mc_version = parts[2]
        mod_version = parts[3]
        # 将loader转换为首字母大写形式，使显示更美观
        display_loader = loader.capitalize()
        if loader == "neoforge":
            display_loader = "NeoForge"

        # 新的name格式：[加载器类型 游戏版本] v模组版本
        name = f"[{display_loader} {mc_version}] v{mod_version}"

        return {
            "name": name,
            "file": filename.name,
            "loader": loader,
            "mc_version": mc_version,
            "game_versions": ",".join(VERSION_MAP.get(mc_version, [mc_version]))
        }
    return None

def main():
    # 获取选中的版本列表
    selected_versions = os.environ.get('GITHUB_SELECTED_VERSIONS', '').split(',')
    
    matrix = []
    for file in Path("./release_assets").glob("*.jar"):
        if meta := parse_filename(file):
            # 只添加选中版本的文件
            if any(meta['mc_version'] == version for version in selected_versions):
                matrix.append(meta)

    print(json.dumps({"include": matrix}))

if __name__ == "__main__":
    main()
