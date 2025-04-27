#!/bin/bash

LANG_DIR="src/main/resources/assets/dmr/lang"

[ -d "$LANG_DIR" ] || exit 1

for file in "$LANG_DIR"/*; do
  [ -f "$file" ] || continue

  filename=$(basename "$file")
  lowercase_filename=$(echo "$filename" | tr '[:upper:]' '[:lower:]')

  [ "$filename" = "$lowercase_filename" ] || mv "$file" "$LANG_DIR/$lowercase_filename"
done
