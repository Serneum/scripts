package scripts.groovy

import com.urbancode.anthill3.domain.artifacts.ArtifactSet
import com.urbancode.anthill3.domain.artifacts.ArtifactSetFactory
import com.urbancode.anthill3.domain.profile.ArtifactDeliverPatterns
import com.urbancode.anthill3.domain.profile.BuildProfile

// usage is like
// ah3groovy uow.groovy --config ahp.properties -n $0.groovy
import com.urbancode.anthill3.domain.workflow.Workflow
import com.urbancode.anthill3.domain.workflow.WorkflowDefinition
import com.urbancode.anthill3.domain.workflow.WorkflowFactory

if (!this.binding.variables.containsKey('uow')) {
    throw new Exception("Use uow.groovy wrapper script for invoking this script")
}

final CliBuilder cli = new CliBuilder(usage:'$0 [global-opts] <artifact-set-id> <artifact-set-base-dir> <artifact-set-includes> <artifact-set-excludes> <workflow-definition-id>')
cli.expandArgumentFiles = false

cli.header = '''Add the new artifact set to all workflow that are using the specified workflow definition
'''
cli._(longOpt:'help','print this message')

final OptionAccessor options = cli.parse(args)
if (options == null) {
    System.exit(1)
}
if (options.help) {
    cli.usage()
    System.exit(0)
}

def arguments = options.arguments()
if (arguments.size() != 5) {
    System.err.println("Must specify the artifact set information and the workflow definition id")
    System.err.println()
    cli.usage()
    System.exit(1)
}

def log = global.log
Long artifactSetId = Long.valueOf(arguments[0])
def artifactSetBaseDir = arguments[1]
def artifactSetIncludes = arguments[2]
def artifactSetExcludes = arguments[3]
Long workflowDefintionId = Long.valueOf(arguments[4])

ArtifactSet artifactSet = ArtifactSetFactory.instance.restore(artifactSetId)
if (artifactSet) {
    Workflow[] workflows = WorkflowFactory.instance.restoreAll()
    workflows.each { Workflow workflow ->
        boolean hasArtifactSet = false
        WorkflowDefinition workflowDef = workflow.workflowDefinition
        if (workflowDef) {
            if (workflowDef.id == workflowDefintionId) {
                BuildProfile buildProfile = workflow.buildProfile
                ArtifactDeliverPatterns[] artifactConfigs = buildProfile.artifactConfigArray
                artifactConfigs.each {config ->
                    if (config.artifactSet.id == artifactSetId) {
                        hasArtifactSet = true
                    }
                }
                if (!hasArtifactSet) {
                    log.println "Artifact set '${artifactSet.name}' not found on workflow '${workflow.project.name} - ${workflow.name}'. Creating it."
                    ArtifactDeliverPatterns pattern = new ArtifactDeliverPatterns(buildProfile, artifactSet)
                    pattern.setBaseDirectory(artifactSetBaseDir)
                    pattern.setArtifactPatterns(artifactSetIncludes)
                    pattern.setArtifactExcludePatterns(artifactSetExcludes)
                    buildProfile.addArtifactConfig(pattern)
                    hasChanges = true
                }
                else {
                    log.println "Workflow '${workflow.project.name} - ${workflow.name}' already has artifact set '${artifactSet.name}'. Moving to the next workflow."
                }
            }
        }
    }
}
else {
    log.println("Could not find artifact set for id ${artifactSetId}.")
    System.exit(1)
}
