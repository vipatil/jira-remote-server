https://docs.google.com/document/d/1SdtarS-IUzV-CyTiyZx9pue4Q-UQZRxx-nctNm1iL9A/edit?pref=2&pli=1#heading=h.1sgtfmcqlyf1



#### Build New Maven project in Eclipse 

```
New -> Project -> Maven project
```

#### Update pom.xml to add dependencies 
```xml
 <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>remote-server</groupId>
	<artifactId>remote-server</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<properties>
		<jetty.version>9.3.9.v20160517</jetty.version>
		<jackson.version>2.7.4</jackson.version>
		<args4j.version>2.33</args4j.version>
		<google.version>19.0</google.version>
		<rs-api.version>2.0.1</rs-api.version>
		<jersey.version>2.23</jersey.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>args4j</groupId>
			<artifactId>args4j</artifactId>
			<version>${args4j.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>com.google.guava</groupId>
			<artifactId>guava</artifactId>
			<version>${google.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>org.eclipse.jetty</groupId>
			<artifactId>jetty-server</artifactId>
			<version>${jetty.version}</version>
			<scope>compile</scope>
		</dependency>


		<dependency>
			<groupId>org.eclipse.jetty</groupId>
			<artifactId>jetty-webapp</artifactId>
			<version>${jetty.version}</version>
			<scope>compile</scope>
		</dependency>


		<dependency>
			<groupId>org.eclipse.jetty</groupId>
			<artifactId>jetty-jndi</artifactId>
			<version>${jetty.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>javax.ws.rs</groupId>
			<artifactId>javax.ws.rs-api</artifactId>
			<version>${rs-api.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>org.glassfish.jersey.containers</groupId>
			<artifactId>jersey-container-servlet</artifactId>
			<version>${jersey.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>org.glassfish.jersey.core</groupId>
			<artifactId>jersey-common</artifactId>
			<version>${jersey.version}</version>
			<scope>compile</scope>
		</dependency>

		<dependency>
			<groupId>com.fasterxml.jackson.jaxrs</groupId>
			<artifactId>jackson-jaxrs-json-provider</artifactId>
			<version>${jackson.version}</version>
			<scope>compile</scope>
		</dependency>
	</dependencies>

</project> 

```
#### Add a package for classes for Main, from parsing command line options, REST server and for top level resource

```java
com.example.remote.remote
Main, CliOptions, ServerRunner
 
com.example.remote.remote.rest
RestApplication, TopResource, ApplicationExceptionHandler

com.example.remote.remote.dto
ErrorResponse

```

#### Run REST Server within Eclipse by right clicking on Main class and test top level rest resource with the POSTMAN

```
GET http://127.0.0.1:8000/
```


#### Implement DTO, These represent Remote connector SPI

```java
com.example.remote.remote.dto.descriptor

ActionDescriptor,
ActionOutput
ConnectionInstancePropertyDescriptor
ConnectionInstancePropertyType
ConnectorDescriptor
InputDescriptor
InputOutputType
RemoteServerDescriptor
```

#### Implement REST API

```java
com.example.remote.remote.rest

RemoteServerResource


@Path("remoteserver")  
public class RemoteServerResource {  
      
    private static final RemoteServerDescriptor DESCRIPTOR = new RemoteServerDescriptor();  
      
    @GET  
    @Path("remoteserverdescriptor")  
    @Produces(MediaType.APPLICATION_JSON)  
    public Object get() {  
        return DESCRIPTOR;  
    }  
  
    @GET  
    @Path("remoteserverhealth")  
    @Produces("text/plain")  
    public String checkHealth() {  
        return "OK";  
    }  
}
```
Then Register above endpoint with the Rest server by adding its entry in RestApplication.getClasses() method as shown below at line# 10

```java
@ApplicationPath("/")  
public class RestApplication extends javax.ws.rs.core.Application {  
  
    @Override  
    public Set<Class<?>> getClasses() {  
        final Set<Class<?>> classes = new HashSet<Class<?>>();  
        classes.add(ApplicationExceptionHandler.class);  
        classes.add(JacksonJaxbJsonProvider.class);  
        classes.add(TopResource.class);  
        classes.add(RemoteServerResource.class);  
        return classes;  
    }  
}  

```

#### Test above code with POSTMAN by using below API

```
GET http://localhost:8000/remoteserver/remoteserverdescriptor
```

#### Implement Jira Specific Stuff

Update pom.xml file to add dependency on JIRA repository


```xml
<repositories>  
   <repository>  
      <id>atlassian.com</id>  
      <name>atlassian.com repository</name>  
      <url>https://m2proxy.atlassian.com/repository/public</url>  
   </repository>  
</repositories>
```

Add dependency on JIRA client api library

```xml
<dependency>  
   <groupId>com.atlassian.jira</groupId>  
   <artifactId>jira-rest-java-client-core</artifactId>  
   <version>3.0.0</version>  
</dependency>
```

Add below  packages and classes 

```java
com.example.remote.jira.remote.dto
 
ActionRequest
CreateIssueInput
CreateIssueRequest
JiraConnectionHealthRequest
JiraConnectionHealthRequestPart
JiraConnectionHealthResponse
 
com.example.remote.jira.remote.rest

JiraResource
 
com.example.remote.jira.remote.services

ConnectionHealth
ConnectionHealthService
ConnectionHealthState
JiraConnection
JiraConnectorRestClientFactory
JiraService
 
 
com.example.remote.jira.remote.atlassian

AsynchronousHttpClientFactory
AsynchronousJiraRestClientFactory
DefaultHttpClient
PromiseHttpAsyncClient
SettableFuturePromiseHttpPromiseAsyncClient
 ```
 
Register JiraResource endpoint with the Rest server by adding its entry in RestApplication.getClasses() method as shown below at line# 11

```java
@ApplicationPath("/")  
  public class RestApplication extends javax.ws.rs.core.Application {  
    
      @Override  
      public Set<Class<?>> getClasses() {  
          final Set<Class<?>> classes = new HashSet<Class<?>>();  
          classes.add(ApplicationExceptionHandler.class);  
          classes.add(JacksonJaxbJsonProvider.class);  
          classes.add(TopResource.class);  
         classes.add(RemoteServerResource.class);  
         classes.add(JiraResource.class);  
         return classes;  
     }  
}
```


Update RemoteServerResource.java class to add JIRA connector action as shown below

```java
private static final RemoteServerDescriptor DESCRIPTOR =  
     new RemoteServerDescriptor() {{  
         connectors.add(  
             new ConnectorDescriptor() {{  
                 name = "JIRA";  
                 type = "com.example.remote.jira";  
                 path = JiraResource.BASE_PATH;  

                 connectionInstanceProperties.add(  
                     new ConnectionInstancePropertyDescriptor() {{  
                         name = "url";  
                         displayName = "URL";  
                         helpText = "e.g. https://jira.example.com";  
                         type = ConnectionInstancePropertyType.STRING;  
                         required = true;  
                     }}  
                 );  
                 connectionInstanceProperties.add(  
                     new ConnectionInstancePropertyDescriptor() {{  
                         name = "login";  
                         displayName = "Login";  
                         type = ConnectionInstancePropertyType.STRING;  
                         required = true;  
                     }}  
                 );  
                 connectionInstanceProperties.add(  
                     new ConnectionInstancePropertyDescriptor() {{  
                         name = "password";  
                         displayName = "Password";  
                         type = ConnectionInstancePropertyType.STRING;  
                         //Encrypted is not yet supported in server, so far only String and Int  
                         //type = ConnectionInstancePropertyType.ENCRYPTED_STRING;  
                         required = false; // allow empty password  
                     }}  
                 );  

                 actions.add(  
                     new ActionDescriptor() {{  
                         name = "createIssue";  
                         displayName = "Create JIRA Issue";  
                         path = JiraResource.CREATE_ISSUE_PATH;  
                         inputs.add(  
                                 new InputDescriptor() {{  
                                     name = "summary";  
                                     type = InputOutputType.STRING;  
                                     required = true;  
                                 }}  
                         );  
                         inputs.add(  
                                 new InputDescriptor() {{  
                                     name = "customerName";  
                                     type = InputOutputType.STRING;  
                                     required = true;  
                                 }}  
                         );  
                         inputs.add(  
                                 new InputDescriptor() {{  
                                     name = "severity";  
                                     type = InputOutputType.STRING;  
                                     required = true;  
                                 }}  
                         );  
                         inputs.add(  
                                 new InputDescriptor() {{  
                                     name = "projectKey";  
                                     type = InputOutputType.STRING;  
                                     required = true;  
                                 }}  
                         );  
                         inputs.add(  
                                 new InputDescriptor() {{  
                                     name = "issueType";  
                                     type = InputOutputType.STRING;  
                                     required = true;  
                                 }}  
                         );  
                         output=new InputDescriptor() {{  
                             name = "result";  
                             type = InputOutputType.STRING;  
                             required = true;  
                         }};  
                     }}  
                 );  
             }}  
         );  
     }};
```

Update imports to reflect above changes

```java
import com.example.remote.jira.remote.rest.JiraResource;  
import com.example.remote.remote.dto.descriptor.ActionDescriptor;  
import com.example.remote.remote.dto.descriptor.ConnectionInstancePropertyDescriptor;  
import com.example.remote.remote.dto.descriptor.ConnectionInstancePropertyType;  
import com.example.remote.remote.dto.descriptor.ConnectorDescriptor;  
import com.example.remote.remote.dto.descriptor.InputDescriptor;  
import com.example.remote.remote.dto.descriptor.InputOutputType;  
import com.example.remote.remote.dto.descriptor.RemoteServerDescriptor;
```

Test above changes with POSTMAN and you should get below response

```
GET http://127.0.0.1:8000/remoteserver/remoteserverdescriptor

```



