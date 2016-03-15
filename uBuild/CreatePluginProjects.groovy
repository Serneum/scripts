import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpParams;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

public class ProjectCreator {
    def projToBranch = [
        "AccuRev":"air"
    //    "Ant":"air",
    //    "CSAR":"master",
    //    "CVS":"air",
    //    "ClearCaseBaseSnapshot":"air",
    //    "ClearCaseUCM":"master",
    //    "ClearQuest":"air",
    //    "CppUnit":"master",
    //    "FileUtils":"air",
    //    "Findbugs":"air",
    //    "Fortify360":"air",
    //    "Gerrit":"master",
    //    "Git":"air",
    //    "Groovy":"air",
    //    "HPQualityCenter":"uBuild",
    //    "Harvest":"master",
    //    "JIRA":"uBuild",
    //    "JaCoCo":"master",
    //    "MSBuild":"air",
    //    "MSTest":"air",
    //    "Make":"air",
    //    "Maven":"uBuild",
    //    "Mercurial":"air",
    //    "NCover":"AHP",
    //    "NUnit":"uBuild",
    //    "Nant":"air",
    //    "Perforce":"air",
    //    "Pmd":"air",
    //    "QTP":"air",
    //    "RTC-WorkItems":"uBuild",
    //    "RTC-scm":"air",
    //    "Rake":"uBuild",
    //    "Rally":"air",
    //    "Report-Publisher":"uBuild",
    //    "Selenium":"air",
    //    "Shell":"uBuild",
    //    "Sonar":"ubuild",
    //    "Sonargraph":"master",
    //    "Subversion":"air",
    //    "TFS":"air",
    //    "TFS-WorkItems":"master",
    //    "VisualStudio":"air",
    //    "Xcode":"master",
    //    "accuwork":"air",
    //    "checkstyle":"master",
    //    "clover":"master",
    //    "cobertura":"air",
    //    "codestation":"air",
    //    "docker-plugin":"master",
    //    "filesystem":"air",
    //    "junit":"uBuild",
    //    "preflight-plugin":"ubuild",
    //    "uDeploy":"uBuild",
    //    "urbancode-reporting":"master"
    ]
    
    String BASE_URL = "http://hygieia.uclab.ibm.com:7071/rest2/"
    HttpClient client
    
    public void create()
    throws Exception {
        client = clientSetup();
        
        String sourceTemplateId = "11";
        
        projToBranch.each { projectName, branch ->
            String projId = createProject(BASE_URL + "projects", projectName);
            String processId = createProcess(BASE_URL + "projects/" + projId + "/buildProcesses");
            createTrigger(BASE_URL + "projects/" + projId + "/buildProcesses/" + processId + "/triggers");
            createSourceConfig(BASE_URL + "projects/" + projId + "/buildProcesses/" + processId + "/sourceConfigs", sourceTemplateId, projectName, branch);
        }
    }
    
    /**
     * Creates a json object for the project that is passed to the REST service that creates a project.
     * The name, teams, and template are required. The template has a property so the name and value must be set.
     * @param url the url to hit for creating the project
     * @return the id of the created project
     * @throws Exception
     */
    String createProject(String url, String projectName)
    throws Exception {
        JSONObject project = new JSONObject();
        project.put("name", projectName + " Plugin");
        project.put("description", "Build the " + projectName + " plugin");
        project.put("template", "Git Project Template");
    
        JSONArray teams = new JSONArray();
        teams.put("System Team");
        project.put("teams", teams);
    
        JSONArray props = new JSONArray();
        project.put("templatedProperties", props);
    
        HttpPost method = new HttpPost(url);
        method.setHeader("Content-Type", "application/json");
        method.setEntity(new StringEntity(project.toString()));
        HttpResponse response = client.execute(method);
        String jsonString = processResponse(response, method);
    
        JSONObject obj = new JSONObject(jsonString);
        return obj.getString("id");
    }
    
    /**
     * Creates a json object for the build process that is passed to the REST service that creates the process.
     * The name, teams, and template are required. The template has a property so the name and value must be set.
     * @param url the url to hit for creating the build process
     * @return the id of the created build process
     * @throws Exception
     */
    String createProcess(String url)
    throws Exception {
        JSONObject process = new JSONObject();
        process.put("name", "Gerrit Build");
        process.put("description", "Build a Gerrit project");
        process.put("template", "Gerrit Build");
    
        JSONArray teams = new JSONArray();
        teams.put("System Team");
        process.put("teams", teams);
    
        JSONArray props = new JSONArray();
        JSONObject prop = new JSONObject();
        prop.put("name", "ant.targets");
        prop.put("value", "dist");
        props.put(prop);
        process.put("templatedProperties", props);
    
        HttpPost method = new HttpPost(url);
        method.setHeader("Content-Type", "application/json");
        method.setEntity(new StringEntity(process.toString()));
        HttpResponse response = client.execute(method);
        String jsonString = processResponse(response, method);
    
        JSONObject obj = new JSONObject(jsonString);
        return obj.getString("id");
    }
    
    String createTrigger(String url)
    throws Exception {
        JSONObject process = new JSONObject();
        process.put("name", "Every 15 minutes");
        process.put("type", "ScheduledTrigger");
        process.put("schedule", "Every 15 minutes");
    
        HttpPost method = new HttpPost(url);
        method.setHeader("Content-Type", "application/json");
        method.setEntity(new StringEntity(process.toString()));
        HttpResponse response = client.execute(method);
        String jsonString = processResponse(response, method);
    
        JSONObject obj = new JSONObject(jsonString);
        return obj.getString("id");
    }
    
    /**
     * Creates a json object for the source config that is passed to the REST service that creates source configs.
     * The name, template id, and repository are required. The template has a property so the name and value must be set.
     * @param url the url to hit for creating the project
     * @param templateId the templated id for the source config template
     * @return the id of the created project
     * @throws Exception
     */
    String createSourceConfig(String url, String sourceTemplateId, String projectName, branchName)
    throws Exception {
        JSONObject source = new JSONObject();
        source.put("name", "Gerrit");
        source.put("repository", "Gerrit");
        source.put("templateId", sourceTemplateId);
    
        JSONArray props = new JSONArray();
        JSONObject urlProp = new JSONObject();
        urlProp.put("name", "remote.url");
        urlProp.put("value", "plugins/" + projectName + ".git");
        props.put(urlProp);
            
        JSONObject remoteNameProp = new JSONObject();
        remoteNameProp.put("name", "remote.name");
        remoteNameProp.put("value", "origin");
        props.put(remoteNameProp);
       
        JSONObject branchNameProp = new JSONObject();
        branchNameProp.put("name", "branch.name");
        branchNameProp.put("value", branchName);
        props.put(branchNameProp);
        
        source.put("templatedProperties", props);
    
        HttpPost method = new HttpPost(url);
        method.setHeader("Content-Type", "application/json");
        method.setEntity(new StringEntity(source.toString()));
        HttpResponse response = client.execute(method);
        String jsonString = processResponse(response, method);
    
        JSONObject obj = new JSONObject(jsonString);
        return obj.getString("id");
    }
    
    /**
     * Handles checking the response status code to make sure the response is good.
     * It then converts the response content (if any) into a string.
     * @param response the response object returned from the client executing a method
     * @param method the method that was executed
     * @return The stringified response content.
     * @throws Exception
     */
    String processResponse(HttpResponse response, HttpRequestBase method)
    throws Exception {
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new RuntimeException(method.getMethod() + " to " + method.getURI() + " failed with response: " + responseCode);
        }
    
        InputStream content = response.getEntity().getContent();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(content, output);
        return output.toString("UTF-8");
    }
    
    DefaultHttpClient clientSetup()
    throws Exception {
        SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
        PoolingClientConnectionManager ccmPool = new PoolingClientConnectionManager(schemeRegistry);
        ccmPool.setDefaultMaxPerRoute(20);
        ccmPool.setMaxTotal(100);
        ClientConnectionManager ccm = ccmPool;
        DefaultHttpClient client = new DefaultHttpClient(ccm);
    
        HttpParams params = client.getParams();
        params.removeParameter("http.socket.buffer-size");
        CredentialsProvider credentialProvider = client.getCredentialsProvider();
        UsernamePasswordCredentials clientCredentials = new UsernamePasswordCredentials("admin", "admin");
        credentialProvider.setCredentials(AuthScope.ANY, clientCredentials);
        client.setRedirectStrategy(new DefaultRedirectStrategy());
    
        return client;
    }
}

new ProjectCreator().create()