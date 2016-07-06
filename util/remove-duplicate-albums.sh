#!/bin/bash
find . -maxdepth 1 -type d | while read dir ; do
  if test -d "$dir"; then
    pushd "$dir" > /dev/null
    find . -maxdepth 1 -type d | grep -o '.*\[[0-9]\+\]' | while read album ; do
      echo "Removing duplicate album $album"
      `rm -rf "$album"`
    done
    popd > /dev/null
  fi
done
