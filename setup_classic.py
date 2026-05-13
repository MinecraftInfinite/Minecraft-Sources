import os
import sys
import json
import requests
import shutil

MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

def get_version_info(version):
    response = requests.get(MANIFEST_URL)
    manifest = response.json()
    
    for v in manifest['versions']:
        if v['id'] == version:
            return requests.get(v['url']).json()
    return None

def download_file(url, path):
    if os.path.exists(path):
        print(f"Skipping {path}, already exists.")
        return
    print(f"Downloading {url} to {path}...")
    response = requests.get(url, stream=True)
    response.raise_for_status()
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        for chunk in response.iter_content(chunk_size=8192):
            f.write(chunk)

def setup_classic(version, mcp_dir):
    print(f"Setting up classic MCP for {version} in {mcp_dir}...")
    info = get_version_info(version)
    if not info:
        print(f"Version {version} not found.")
        return

    jars_dir = os.path.join(mcp_dir, 'jars')
    version_dir = os.path.join(jars_dir, 'versions', version)
    bin_dir = os.path.join(jars_dir, 'bin')
    lib_dir = os.path.join(mcp_dir, 'lib')
    os.makedirs(version_dir, exist_ok=True)
    os.makedirs(bin_dir, exist_ok=True)
    os.makedirs(lib_dir, exist_ok=True)

    # Save version JSON
    with open(os.path.join(version_dir, f"{version}.json"), 'w') as f:
        json.dump(info, f)

    # Download client and server
    downloads = info['downloads']
    if 'client' in downloads:
        download_file(downloads['client']['url'], os.path.join(version_dir, f"{version}.jar"))
        # Also provide a copy in jars/bin/minecraft.jar just in case
        shutil.copy(os.path.join(version_dir, f"{version}.jar"), os.path.join(bin_dir, 'minecraft.jar'))
    if 'server' in downloads:
        download_file(downloads['server']['url'], os.path.join(jars_dir, 'minecraft_server.jar'))
        # And minecraft_server.VERSION.jar
        shutil.copy(os.path.join(jars_dir, 'minecraft_server.jar'), os.path.join(jars_dir, f"minecraft_server.{version}.jar"))

    # Download and extract natives
    natives_dir = os.path.join(version_dir, f"{version}-natives")
    os.makedirs(natives_dir, exist_ok=True)
    
    # Download libraries
    for lib in info['libraries']:
        if 'downloads' in lib:
            if 'artifact' in lib['downloads']:
                artifact = lib['downloads']['artifact']
                lib_path = os.path.join(jars_dir, 'libraries', artifact['path'])
                download_file(artifact['url'], lib_path)
                
                # MCP classic expects some libraries in lib/ or jars/bin/
                name = lib['name']
                if 'lwjgl' in name or 'jinput' in name:
                    # Copy to jars/bin/
                    dest = os.path.join(bin_dir, os.path.basename(artifact['path']))
                    if not os.path.exists(dest):
                        shutil.copy(lib_path, dest)
                else:
                    # Copy to lib/
                    dest = os.path.join(lib_dir, os.path.basename(artifact['path']))
                    if not os.path.exists(dest):
                        shutil.copy(lib_path, dest)
            
            # Extract natives
            if 'classifiers' in lib['downloads'] and 'natives-windows' in lib['downloads']['classifiers']:
                native_info = lib['downloads']['classifiers']['natives-windows']
                native_path = os.path.join(jars_dir, 'libraries', native_info['path'])
                download_file(native_info['url'], native_path)
                # Unzip to natives_dir
                shutil.unpack_archive(native_path, natives_dir, 'zip')

    print(f"Classic setup for {version} complete.")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python setup_classic.py <version> <mcp_dir>")
        sys.exit(1)
    setup_classic(sys.argv[1], sys.argv[2])
