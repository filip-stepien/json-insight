#!/usr/bin/env bash
# Składa README.md do doc/README.pdf.
#
# Wymaga: pandoc, xelatex (TeX Live), Node/npx (mermaid-cli ściągany przez npx).
# Diagramy w README są obrazkami (doc/img/diagram-*.png) renderowanymi lokalnie
# z Mermaida (doc/img/diagram-*.mmd). Skrypt odświeża rendery i składa PDF.
#
# Użycie:  cd <katalog projektu> && bash doc/build-pdf.sh

set -euo pipefail
cd "$(dirname "$0")/.."

# 1. Render diagramów Mermaid -> PNG (wysoka rozdzielczość, białe tło)
for f in doc/img/diagram-*.mmd; do
  out="${f%.mmd}.png"
  echo "Render: $f -> $out"
  npx -y @mermaid-js/mermaid-cli -i "$f" -o "$out" -b white -s 3
done

# 2. Wersja README do PDF: znaczniki <img> diagramów -> obrazki LaTeX-a o
#    odpowiednim rozmiarze, zrzuty interfejsu skalowane do szerokości strony.
python3 - <<'PY'
import re
md = open("README.md", encoding="utf-8").read()

# rozmiary diagramów w PDF
sizes = {"diagram-1": "height=18cm", "diagram-2": "width=85%", "diagram-3": "height=8cm"}
def himg(m):
    src = m.group(1)
    key = re.search(r"(diagram-\d)", src).group(1)
    return f"![]({src}){{{sizes.get(key, 'width=70%')}}}"
md = re.sub(r'<p align="center"><img src="(doc/img/diagram-\d\.png)"[^>]*></p>', himg, md)

# zrzuty interfejsu (pliki zaczynające się od cyfry) -> 85% szerokości
md = re.sub(r"(!\[[^\]]*\]\(doc/img/[0-9][^)]*\.png\))", r"\1{width=85%}", md)

open("doc/_readme_pdf.md", "w", encoding="utf-8").write(md)
PY

# 3. Złożenie PDF
pandoc doc/_readme_pdf.md -o doc/README.pdf \
  --pdf-engine=xelatex \
  --toc -V lang=pl -V geometry:margin=2cm -V fontsize=10pt \
  -V monofont="Menlo" -V colorlinks=true -V linkcolor=blue -V urlcolor=blue \
  -H doc/pandoc-header.tex --resource-path="$PWD"

rm -f doc/_readme_pdf.md
echo "Gotowe: doc/README.pdf"
