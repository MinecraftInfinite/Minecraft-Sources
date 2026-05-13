import os
import sys
import json
import requests

MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

def get_version_info(version):
    response = requests.get(MANIFEST_URL)
    manifest = response.json()
    
    for v in manifest['versions']:
        if v['id'] == version:
            return requests.get(v['url']).json()
    return None

def download_file(url, path):
    print(f"Downloading {url} to {path}...")
    response = requests.get(url, stream=True)
    response.raise_for_status()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        for chunk in response.iter_content(chunk_size=8192):
            f.write(chunk)

def main():
    if len(sys.argv) < 3:
        print("Usage: python download_mc.py <version> <target_dir>")
        sys.exit(1)
    
    version = sys.argv[1]
    target_dir = sys.argv[2]
    
    info = get_version_info(version)
    if not info:
        print(f"Version {version} not found.")
        sys.exit(1)
    
    downloads = info['downloads']
    
    if 'client' in downloads:
        download_file(downloads['client']['url'], os.path.join(target_dir, 'client.jar'))
    
    if 'server' in downloads:
        download_file(downloads['server']['url'], os.path.join(target_dir, 'server.jar'))

if __name__ == "__main__":
    main()
