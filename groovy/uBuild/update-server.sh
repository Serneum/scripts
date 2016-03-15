installDir=`sed '/^\#/d' .build.properties | grep 'install.server.dir'  | tail -n 1 | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'`
#git pull
ant clean install-web-content install-jars
UBBuildDir=~/workspace/uBuild/build
if [ -d $UBBuildDir/ubuild-install ]; then
    Product=ubuild
elif [ -d $UBBuildDir/ibm-ucb-install ]; then
    Product=ibm-ucb
else
    Product=
fi
cp $UBBuildDir/$Product-install/lib/$Product* $installDir/lib
$installDir/bin/server run -debug
