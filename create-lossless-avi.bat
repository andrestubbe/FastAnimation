@echo off
chcp 65001 >nul

echo ==================================================================
echo 🎬 Converting PNG Frames to 100%% Lossless 60 FPS AVI Video...
echo Input:  FastAnimation\docs\render_frames\frame_%%%%05d.png
echo Output: FastAnimation\docs\ParticleTimeline_Lossless_60fps.avi
echo ==================================================================

if not exist "docs\render_frames\frame_00001.png" (
    echo [ERROR] No frames found in docs\render_frames\! Please run render-lossless-frames.bat first.
    pause
    exit /b 1
)

ffmpeg -y -framerate 60 -i "docs\render_frames\frame_%%05d.png" -c:v ffv1 -level 3 -threads 0 "docs\ParticleTimeline_Lossless_60fps.avi"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] FFmpeg conversion failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ==================================================================
echo ✅ Lossless AVI Export Complete:
echo    FastAnimation\docs\ParticleTimeline_Lossless_60fps.avi
echo ==================================================================
pause
