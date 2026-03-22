#!/bin/zsh
set -euo pipefail
setopt null_glob

zmodload zsh/datetime

cd "${0:A:h}"

start=$EPOCHREALTIME
needs_build=0
main_class=target/classes/org/example/Main.class
if [[ ! -f $main_class ]]; then
  needs_build=1
else
  for source in pom.xml src/main/java/**/*.java; do
    if [[ "$source" -nt $main_class ]]; then
      needs_build=1
      break
    fi
  done
fi

if (( needs_build )); then
  mvn -q -DskipTests compile
fi
A24_SUPPRESS_INTERNAL_TIMING=1 java -cp target/classes org.example.Main
typeset -F 6 elapsed=$(( EPOCHREALTIME - start ))
printf '总耗时: %.6f 秒\n' "$elapsed"
