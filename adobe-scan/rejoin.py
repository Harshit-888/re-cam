import os, glob

parts = sorted(glob.glob('split_config.arm64_v8a.apk.part*'))
if not parts:
    print('No part files found.')
    exit(1)

out_file = 'split_config.arm64_v8a.apk'
print(f'Rejoining {len(parts)} parts into {out_file}...')
with open(out_file, 'wb') as out_fp:
    for p in parts:
        print(f'  Reading {p}...')
        with open(p, 'rb') as in_fp:
            out_fp.write(in_fp.read())

print(f'Done! Successfully reconstructed {out_file} ({os.path.getsize(out_file)/(1024*1024):.2f} MB).')
