#!/usr/bin/env bash

DEST=$1
RE="\./build/js/packages(_imported)?(/reproto)?/(kotlin|kotlinx.*)/.*\.js"
find -regextype posix-extended -regex "$RE" -exec cp "{}" "$DEST" \;
