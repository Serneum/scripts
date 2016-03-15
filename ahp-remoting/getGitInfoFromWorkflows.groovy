import com.urbancode.anthill3.domain.profile.BuildProfileFactory
import com.urbancode.anthill3.domain.profile.BuildProfile
import com.urbancode.anthill3.domain.source.SourceConfig
import com.urbancode.anthill3.domain.source.plugin.PluginSourceConfig
import com.urbancode.anthill3.domain.property.PropertyValueGroup

// usage is like
// ah3groovy uow.groovy --config ahp.properties -n $0.groovy


if (!this.binding.variables.containsKey('uow')) {
    throw new Exception("Use uow.groovy wrapper script for invoking this script")
}

def log = global.log

final CliBuilder cli = new CliBuilder(usage:'$0 [global-opts] <file-name>')
cli.expandArgumentFiles = false

final OptionAccessor options = cli.parse(args)
if (options == null) {
    System.exit(1)
}
def arguments = options.arguments()

File inputFile = new File(arguments[0])
if (!inputFile.exists()) {
    println "File '${inputFile.absolutePath}' does not exist."
    System.exit(1)
}

inputFile.eachLine { line ->
    def parts = line.split(",")
    def projectName = parts[0].trim()
    def workflowName = parts[1]

    BuildProfile profile = BuildProfileFactory.instance.restoreForProjectAndWorkflow(projectName, workflowName)
    SourceConfig sourceConfig = profile.getSourceConfig()
    PluginSourceConfig pSource = (PluginSourceConfig) sourceConfig
    PropertyValueGroup propValueGroup = pSource.getPropertyValueGroups()[0]

    def remoteUrl = "urbancodegit.rtp.raleigh.ibm.com:29418/" + propValueGroup.getPropertyValue("remoteUrl").displayedValue
    def branch = propValueGroup.getPropertyValue("branch").displayedValue

    println "${remoteUrl} - ${branch}"
}
