package scripts.groovy

// usage is like
// ah3groovy uow.groovy --config ahp.properties -n $0.groovy
import com.urbancode.anthill3.domain.workflow.Workflow
import com.urbancode.anthill3.domain.workflow.WorkflowFactory
import com.urbancode.anthill3.domain.workflow.WorkflowQueryFilter

if (!this.binding.variables.containsKey('uow')) {
    throw new Exception("Use uow.groovy wrapper script for invoking this script")
}

def log = global.log
def oldToNewNames = [
        "Deploy and Register Plugin": "Mark Plugin as Released",
        "Mark Plugin For Release": "Mark Plugin as Release Candidate"
]

WorkflowQueryFilter filter = new WorkflowQueryFilter()
filter.typeFilter = WorkflowQueryFilter.TypeFilter.SECONDARY
filter.activeFilter = WorkflowQueryFilter.ActiveFilter.ALL
oldToNewNames.each { oldName, newName ->
    filter.setNamePattern(oldName)

    Workflow[] deployRegisterWorkflows = WorkflowFactory.instance.restoreWithFilter(filter).results
    deployRegisterWorkflows.each { workflow ->
        log.println "Found ${workflow.project.name} - ${workflow.name}. Renaming to ${newName}"
        workflow.name = newName
        workflow.propertyNames.each { prop ->
            log.println "Found property '${prop}'. Removing it."
            workflow.removePropertyValue(prop)
        }
        hasChanges = true
        log.println ""
    }
}
