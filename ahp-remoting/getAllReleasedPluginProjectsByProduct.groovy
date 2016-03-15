package scripts.groovy

import com.urbancode.anthill3.dashboard.DashboardFactory
import com.urbancode.anthill3.dashboard.StatusSummary

// usage is like
// ah3groovy uow.groovy --config ahp.properties -n $0.groovy
import com.urbancode.anthill3.domain.workflow.Workflow
import com.urbancode.anthill3.domain.workflow.WorkflowDefinition
import com.urbancode.anthill3.domain.workflow.WorkflowFactory
import com.urbancode.anthill3.domain.property.PropertyValue

if (!this.binding.variables.containsKey('uow')) {
    throw new Exception("Use uow.groovy wrapper script for invoking this script")
}

final CliBuilder cli = new CliBuilder(usage:'$0 [global-opts]')
cli.expandArgumentFiles = false

final OptionAccessor options = cli.parse(args)
if (options == null) {
    System.exit(1)
}
if (options.help) {
    cli.usage()
    System.exit(0)
}

def log = global.log
Long workflowDefintionId = 1597

Map<String, Set<String>> productToProject = new HashMap<String, Set<String>>()

Workflow[] workflows = WorkflowFactory.instance.restoreAllActive()
workflows.each { Workflow workflow ->
    if (workflow.isOriginating()) {
        WorkflowDefinition workflowDef = workflow.workflowDefinition
        if (workflowDef) {
            if (workflowDef.id == workflowDefintionId) {
//                log.info "Looking at ${workflow.project.name} - ${workflow.name}"
                List<StatusSummary> statusSummaries = Arrays.asList(DashboardFactory.instance.getMostRecentSummaryForEachStatus(workflow))
                for (StatusSummary summary : statusSummaries) {
                    if ((summary.statusName == "released" || summary.statusName == "release candidate") && summary.dateAssigned) {
                        String project = workflow.project.name
                        PropertyValue productPropVal = workflow.getProperty("product")?.getPropertyValue()
                        if (productPropVal?.displayedValue == "uDeploy") {
                            productPropVal.value = "ibmucd"
                            workflow.setPropertyValue("product", productPropVal)
                        }
                        else if (product == "AHP") {
                            product = "anthillpro"
                        }

                        if (product == "ibmucd" && !workflow.name.contains("master") && !workflow.name.contains("air") && !workflow.name.contains("ucd")) {
                            product = "udeploy"
                        }

//                        workflow.setPropertyValue("product", product, false)
//                        hasChanges = true
                        Set<String> productProjects = productToProject.get(product)
                        if (!productProjects) {
//                            log.println "Creating '${product}' bucket."
                            productProjects = new HashSet<String>()
                            productToProject.put(product, productProjects)
                        }
                        productProjects.add(project + " - " + workflow.name)

//                        log.println "'${project} - ${workflow.name}' has been released. Product is '${product}'."
                        break
                    }
                }
            }
        }
    }
}

for (Map.Entry<String, Set<String>> entry : productToProject.entrySet()) {
    println entry.key
    List<String> projects = new ArrayList<String>(entry.value)
    Collections.sort(projects, new Comparator<String>() {
        @Override
        int compare(String s, String t1) {
            return s.compareToIgnoreCase(t1)
        }
    })
    projects.each {
        println "    ${it}"
    }
}
