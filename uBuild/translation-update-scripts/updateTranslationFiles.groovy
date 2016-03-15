import groovy.io.FileType

def dirToLocale = [
    'BPO' : 'pt',    // BPO -- Brazilian
    'CHS' : 'zh_CN', // CHS -- Simplified Chinese
    'CHT' : 'zh_TW', // CHT -- Traditional Chinese
    'FRE' : 'fr',    // FRE -- French
    'GER' : 'de',    // GER -- German
    'ITA' : 'it',    // ITA -- Italian
    'JPN' : 'ja',    // JPN -- Japanese
    'KOR' : 'ko',    // KOR -- Korean
    'RUS' : 'ru',    // RUS  -- Russian
    'SPA' : 'es'     // SPA -- Spanish
]

def bundleNames = [
    "Server",
    "Database",
    "Reporting",
    "Security"
]

private def executeCommands(def commands, File workingDir) {
    commands.each { command ->
        executeOnShell(command, workingDir.absoluteFile)
    }
}

private def executeOnShell(String command, File workingDir) {
    println command
    def process = new ProcessBuilder(addShellPrefix(command))
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
    process.inputStream.eachLine {println it}
    process.waitFor();
    return process.exitValue()
}

private def addShellPrefix(String command) {
    commandArray = new String[3]
    commandArray[0] = "sh"
    commandArray[1] = "-c"
    commandArray[2] = command
    return commandArray
}

def plugins = []
def currentDir = new File(this.args[0])
File workspaceDir = new File('/Users/crr/workspace')
File ubuildWorkspace = new File(workspaceDir, 'uBuild')
def gitCommands = [
        "git pull",
        "git add src",
        "git commit -m 'Updated translation files'",
        "git submit",
]
currentDir.eachFile FileType.DIRECTORIES, { localeDir ->
    String locale = dirToLocale[localeDir.name]
    File serverLocaleDir = new File(ubuildWorkspace, 'src/conf/locale')
    bundleNames.each { bundleName ->
        File bundleFile = new File(localeDir, "${bundleName}Bundle_en.properties")
        File newBundleFile = new File(localeDir, "${bundleName}Bundle_${locale}.properties")
        println "Renamed ${bundleFile.canonicalPath} to ${newBundleFile.canonicalPath}"
        bundleFile.renameTo(newBundleFile)

        File newServerLocale = new File(serverLocaleDir, newBundleFile.name)
        println "Moved ${newBundleFile.name} to ${newServerLocale.absolutePath}"
        newBundleFile.renameTo(newServerLocale)
    }

    File pluginsDir = new File(localeDir, "plugins")
    if (pluginsDir.exists()) {
        pluginsDir.eachFile FileType.DIRECTORIES, { pluginDir ->
            dirName = pluginDir.name
            if (dirName == "Gradle") {
                dirName = "Community-Gradle"
            }
            if (!plugins.contains(pluginDir.name)) {
                plugins << dirName
            }
            File translationFile = new File(pluginDir, 'en.properties')
            File newTranslationFile = new File(pluginDir, "${locale}.properties")
            println "Renamed ${translationFile.canonicalPath} to ${newTranslationFile.canonicalPath}"
            translationFile.renameTo(newTranslationFile)

            File pluginWorkspace = new File(workspaceDir, "plugins/${dirName}")
            File pluginWorkspaceLocaleDir = new File(pluginWorkspace, 'src/main/zip/locale')
            pluginWorkspaceLocaleDir.mkdir()
            File newPluginLocale = new File(pluginWorkspaceLocaleDir, newTranslationFile.name)
            println "Moved ${newTranslationFile.name} to ${newPluginLocale.absolutePath}"
            newTranslationFile.renameTo(newPluginLocale)
        }
    }
}

// Commit UCB translations
executeCommands(gitCommands, ubuildWorkspace)

// Commit all plugin translations
plugins.each { plugin ->
    File pluginWorkspace = new File(workspaceDir, "plugins/${plugin}")
    executeCommands(gitCommands, pluginWorkspace)
}
