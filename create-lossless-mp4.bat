@echo off
chcp 65001 >nul

echo ==================================================================
echo Converting PNG Frames to High-Quality 60 FPS MP4 Video (Windows Media Player Compatible)...
echo Input:  FastAnimation\docs\render_frames\frame_%%%%05d.png
echo Output: FastAnimation\docs\ParticleTimeline_Lossless_60fps.mp4
echo ==================================================================

if not exist "docs\render_frames\frame_00001.png" (
    echo [ERROR] No frames found in docs\render_frames\! Please run render-lossless-frames.bat first.
    pause
    exit /b 1
)

ffmpeg -y -framerate 60 -i "docs\render_frames\frame_%%05d.png" -vf "pad=ceil(iw/2)*2:ceil(ih/2)*2" -c:v libx264 -crf 10 -preset veryslow -profile:v high -level 4.2 -pix_fmt yuv420p -movflags +faststart "docs\ParticleTimeline_Lossless_60fps.mp4"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] FFmpeg conversion failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ==================================================================
echo MP4 Export Complete:
echo    FastAnimation\docs\ParticleTimeline_Lossless_60fps.mp4
echo ==================================================================
pause
