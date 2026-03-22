#!/bin/zsh
set -euo pipefail

zmodload zsh/datetime

cd "${0:A:h}"

start=$EPOCHREALTIME
A24_SUPPRESS_INTERNAL_TIMING=1 uv run python main.py
typeset -F 6 elapsed=$(( EPOCHREALTIME - start ))
printf '总耗时: %.6f 秒\n' "$elapsed"
