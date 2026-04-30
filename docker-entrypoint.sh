#!/bin/bash
set -e

# Start virtual framebuffer display
Xvfb :99 -screen 0 1280x800x24 &
export DISPLAY=:99

# Give Xvfb a moment to start
sleep 1

# Start VNC server (view-only mirror of :99, no password)
x11vnc -display :99 -nopw -forever -quiet &

# Run the JavaFX application
exec java \
  -Djava.awt.headless=false \
  -jar /app/app.jar
