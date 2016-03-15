// usage is like
// ah3groovy uow.groovy --config ahp.properties -n $0.groovy
import com.urbancode.anthill3.domain.buildlife.BuildLife
import com.urbancode.anthill3.domain.buildlife.BuildLifeFactory
import com.urbancode.anthill3.domain.buildlife.BuildLifeQueryFilter
import com.urbancode.anthill3.domain.buildrequest.BuildRequest;
import com.urbancode.anthill3.domain.buildrequest.RequestSourceEnum
import com.urbancode.anthill3.domain.lifecycle.LifeCycleModel
import com.urbancode.anthill3.domain.lifecycle.LifeCycleModelFactory
import com.urbancode.anthill3.domain.persistent.Persistent;
import com.urbancode.anthill3.domain.security.User
import com.urbancode.anthill3.domain.servergroup.ServerGroup
import com.urbancode.anthill3.domain.servergroup.ServerGroupFactory
import com.urbancode.anthill3.domain.status.Status
import com.urbancode.anthill3.domain.status.StatusGroup
import com.urbancode.anthill3.domain.workflow.Workflow;
import com.urbancode.anthill3.domain.workflow.WorkflowFactory;
import com.urbancode.anthill3.services.build.BuildService;

if (!this.binding.variables.containsKey('uow')) {
    throw new Exception("Use uow.groovy wrapper script for invoking this script");
}

def log = global.log;

User user = uow.current.user
RequestSourceEnum source = RequestSourceEnum.MANUAL
Persistent requester = user
ServerGroup environment = ServerGroupFactory.instance.restoreForName("build farm")

LifeCycleModel lcm = LifeCycleModelFactory.instance.restoreForName("Java Library")
StatusGroup statusGroup = lcm.getStatusGroup()
Status releaseCandidate = statusGroup.getStatus("release candidate")

def requests = []
def workflows = WorkflowFactory.instance.restoreForWorkflowProperty("product", "ubuild")
workflows.each { buildWorkflow ->
    BuildLifeQueryFilter filter = new BuildLifeQueryFilter()
    filter.setStatus(releaseCandidate)
    filter.setProfile(buildWorkflow.buildProfile)

    Workflow releaseWorkflow = WorkflowFactory.instance.restoreForProjectAndWorkflowName(buildWorkflow.project, "Mark Plugin as Released")
    BuildLife latest = BuildLifeFactory.instance.restoreWithFilter(filter).firstResult
    if (latest) {
        println "${buildWorkflow.project.name} - ${buildWorkflow.name} - ${latest.id}"
        requests << BuildRequest.createNonOriginatingRequest(latest, releaseWorkflow, environment, user, source, requester)
    }
}

log.println "Running $requests.size Requests"
if (global.dryRun) {
    log.println "\tdry run skipping submission"
}
else {
    if (requests) {
        hasChanges = true;
    }

    requests.each {
        BuildService.instance.runWorkflow(it)
    }
    log.println "\trequests submitted"
}
