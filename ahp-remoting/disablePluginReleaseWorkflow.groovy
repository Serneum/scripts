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
Long targetId = 1534

WorkflowQueryFilter filter = new WorkflowQueryFilter()
filter.typeFilter = WorkflowQueryFilter.TypeFilter.SECONDARY
filter.activeFilter = WorkflowQueryFilter.ActiveFilter.ONLY_ACTIVE

Workflow[] releaseWorkflows = WorkflowFactory.instance.restoreWithFilter(filter).results
releaseWorkflows.each { workflow ->
    if (workflow.workflowDefinition && workflow.workflowDefinition.id == targetId) {
        log.println "Found ${workflow.project.name} - ${workflow.name}."
        workflow.setActive(false)
        hasChanges = true
    }
}

