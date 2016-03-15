import groovy.xml.StreamingMarkupBuilder

File projectListFile = new File(args[0])

def xmlBuilder = new StreamingMarkupBuilder()
xmlBuilder.encoding = "UTF-8"
def xml = xmlBuilder.bind {
    mkp.xmlDeclaration()
    'project'('xmlns': 'http://www.urbancode.com/schema/cs/project-dependencies/1.0',
            'xmlns:ns2': 'http://www.urbancode.com/schema/cs/project-build-lives/1.0',
            'xmlns:ns4': 'http://www.urbancode.com/schema/cs/workflows/1.0',
            'xmlns:ns3': 'http://www.urbancode.com/schema/cs/projects/1.0',
            'xmlns:ns5': 'http://www.urbancode.com/schema/cs/command-error/1.0',
            'xmlns:ns6': 'http://www.urbancode.com/schema/cs/project-details/1.0',
            'xmlns:ns7': 'http://www.urbancode.com/schema/cs/command-message/1.0',
            'xmlns:ns8': 'http://www.urbancode.com/schema/cs/command-output/1.0',
            'name': 'uBuild') {
        'deliverables'
        'dependencies' {
            projectListFile.eachLine { line ->
                println line
                def parts = line.split(", ")
                def name = parts[0]
                def workflow = "build workflow [${parts[1]}]"
                'dependency' {
                    'cs-project'('name': name, 'workflow': workflow)
                    'criteria'('status': 'success')
                    'delivery'('artifact-set': 'Translation') {
                        'directory' "build/translations/plugins/${name}"
                    }
                }
            }
        }
    }
}
println xml.toString()
