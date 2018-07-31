#!/usr/bin/env bash

# printable code
pdflatex -shell-escape printable_code.tex
mv printable_code.pdf ../printable_code.pdf