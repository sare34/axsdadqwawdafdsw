#!/bin/bash
set -e

git config --global --add safe.directory "*"
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/sare34/axsdadqwawdafdsw.git
git branch -M main

echo "======================================================"
echo "🚀 جاري الرفع إلى مستودع sare34/axsdadqwawdafdsw..."
echo "======================================================"
echo "عندما يطلب منك:"
echo "Username: اكتب sare34"
echo "Password: الصق رمز الـ Token (ghp_...)"
echo "======================================================"

git push -u origin main
