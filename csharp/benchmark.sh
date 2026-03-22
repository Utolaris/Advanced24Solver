#!/bin/zsh
set -euo pipefail
setopt null_glob

zmodload zsh/datetime

cd "${0:A:h}"

start=$EPOCHREALTIME
needs_build=0
if [[ ! -x bin/Release/net10.0/Advanced24SolverCSharp ]]; then
  needs_build=1
else
  for source in Advanced24SolverCSharp.csproj Program.cs Solver.cs; do
    if [[ "$source" -nt bin/Release/net10.0/Advanced24SolverCSharp ]]; then
      needs_build=1
      break
    fi
  done
fi

if (( needs_build )); then
  dotnet build Advanced24SolverCSharp.csproj -c Release -nologo >/dev/null
fi
A24_SUPPRESS_INTERNAL_TIMING=1 ./bin/Release/net10.0/Advanced24SolverCSharp
typeset -F 6 elapsed=$(( EPOCHREALTIME - start ))
printf '总耗时: %.6f 秒\n' "$elapsed"
