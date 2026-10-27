@echo off
echo Rejoining split_config.arm64_v8a.apk...
copy /b split_config.arm64_v8a.apk.part0 + split_config.arm64_v8a.apk.part1 split_config.arm64_v8a.apk
echo Done!
pause
