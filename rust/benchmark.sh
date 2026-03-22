#!/bin/zsh
set -euo pipefail
setopt null_glob

zmodload zsh/datetime

cd "${0:A:h}"

start=$EPOCHREALTIME
needs_build=0
if [[ ! -x target/release/advanced24solver_rust ]]; then
  needs_build=1
else
  for source in Cargo.toml src/*.rs; do
    if [[ "$source" -nt target/release/advanced24solver_rust ]]; then
      needs_build=1
      break
    fi
  done
fi

if (( needs_build )); then
  cargo build --release --quiet
fi
A24_SUPPRESS_INTERNAL_TIMING=1 ./target/release/advanced24solver_rust
typeset -F 6 elapsed=$(( EPOCHREALTIME - start ))
printf '总耗时: %.6f 秒\n' "$elapsed"
