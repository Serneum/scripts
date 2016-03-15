File serverBundle = new File("/Users/crr/workspace/uBuild/src/conf/locale/ServerBundle_en.properties")
File dbBundle = new File("/Users/crr/workspace/uBuild/build/ibm-ucb-install/conf/locale/DatabaseBundle_en.properties")

Properties serverProps = new Properties()
Properties dbProps = new Properties()

serverBundle.withInputStream {
    serverProps.load(it)
}

dbBundle.withInputStream {
    dbProps.load(it)
}

dbProps.each {
    serverProps.remove(it.key)
}

def comments = "NLS_ENCODING=UTF-8\nNLS_MESSAGEFORMAT_NONE"
System.setProperty("line.separator", "\r\n");
def writer = new FileWriter(serverBundle)
serverProps.store(writer, comments)
writer.close()