import os
import sys

def get_mod_version(properties_file):
    try:
        with open(properties_file, 'r', encoding='utf-8') as file:
            for line in file:
                if line.strip().startswith("mod_version="):
                    return line.strip().split("=", 1)[1].strip()
    except FileNotFoundError:
        print(f"Error: {properties_file} not found.")
        sys.exit(1)

    return None

if __name__ == "__main__":
    properties_file = "gradle.properties"
    mod_version = get_mod_version(properties_file)

    if mod_version:
        print(f"{mod_version}")
    else:
        print("Error: mod_version not found in gradle.properties.")
        sys.exit(1)
