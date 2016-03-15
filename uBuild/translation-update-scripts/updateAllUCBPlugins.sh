#!/bin/bash
CURR_DIR=`pwd`
while IFS='' read -r line || [[ -n "$line" ]]; do
    PLUGIN_DIR="/Users/crr/workspace/plugins/$line"

    cd $PLUGIN_DIR
    # Add commit hooks if necessary
    # gitdir=$(git rev-parse --git-dir); scp -p -P 29418 crr@urbancodegit.rtp.raleigh.ibm.com:hooks/commit-msg ${gitdir}/hooks/

    # Stash local changes
    git stash

    # Switch to the most preferred branch
    git co master
    git co air
    git co ubuild
    git co uBuild

    # Pull any changes
    git pull

    # Update version in plugin XML
    PLUGIN_XML="src/main/zip/plugin.xml"
    version=$(xmllint --xpath "number(/*[name()='plugin']/*[name()='header']/*[name()='identifier']/@version)" $PLUGIN_XML)
    ((version++))
    sed -i '' "/<header>/,/<\/header>/s/\(version\)=\"[^\"]*\"/\1=\"$version\"/" $PLUGIN_XML

    cd $CURR_DIR
    #Update dependencies, release notes, upgrades, and .gitignore
    groovy updatePluginFiles.groovy $PLUGIN_DIR

    cd $PLUGIN_DIR
    # Commit and submit to Gerrit
    git add .gitignore dependencies.xml src
    git commit -m 'Added RPX dependency and English translations'
    git submit

    cd $CURR_DIR
done < "$1"
