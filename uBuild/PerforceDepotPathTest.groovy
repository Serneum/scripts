public class PerforceDepotPathTest {
    
    final def ROOT_TOKEN        = 'Root:'
    final def VIEW_TOKEN        = 'View:'
    
    String clientspec = """Client:    ePBankInfoUBClent
    
    Description:    Created by devOps-uBuild.
    
    Root: D:\\devops\\build\\ePBankInfo___Ear\\Build
    
    Options:    noallwrite clobber nocompress unlocked nomodtime rmdir
    
    SubmitOptions:    submitunchanged
    
    LineEnd:    local
    
    View:
        //ecomm-cav/pbi/pbi-app/pbi-app-2013.02.0/...    //ePBankInfoUBClent/ecomm-cav/pbi/pbi-app/pbi-app-2013.02.0/..."""
    
    static void main(String[] args) {
        PerforceDepotPathTest test = new PerforceDepotPathTest()
        def br = new BufferedReader(new StringReader(test.clientspec))
        def clientInfo = test.readClientInfo(br)
        for (depoPath in clientInfo.depoPaths) {
            println "\t${depoPath}"
        }
    }
    
    def readClientInfo = { def br ->
        def result = [rootPath: null, depoPaths: []]
        String line = null
        while( (line = br.readLine()?.trim()) != null )  {
            println "Parsing line\n\t" + line
            if (line.startsWith(ROOT_TOKEN)) {
                println "Found 'Root' section of Client Spec"
                result.rootPath = line.substring(ROOT_TOKEN.length()).trim()
            }
            else if (line.startsWith(VIEW_TOKEN)) {
                println "Found 'View' section of Client Spec"
                while (line = br.readLine()?.trim()) { // read until we hit a blank line (or null)
                    println "Parsing line\n\t" + line
                    // match first token '//....' where ... has no white-space OR '"//...."' where ... has no quote
                    def path = line.find(~'((?:"//[^"]+)|(?://\\S+))') { match, path -> return path }
                    
                    if (path) {
                        println "Found depot path: ${path}"
                        result.depoPaths << path
                    }
                    else {
                        println "Could not find a depot path in line '${line}'"
                    }
                }
            }
        }
        
        return result
    }
}