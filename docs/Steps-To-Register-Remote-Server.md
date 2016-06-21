##  Create 'Remote Servers'  Setting Definition

Before building process with remote actions in Beta build we need to create Setting Definition to store information about remote servers.
This definition is created with below POSt request.


```
POST http://95bt-delhi.bmc.com:8008/api/rx/application/admin-settings/component
Content-Type: application/json

{
	"componentName": "Remote Servers",
	"parentComponentName": "",
	"supportsMultiple": true,
	"viewToOpen": "CommonSettings",
	"status": "Active",
	"showInNAVBar": true,
	"localeList": [{
		"locale": "en",
		"componentLabel": "Remote Servers",
		"firstMenu": "Remote Servers",
		"secondMenu": ""
	}],
	"settingMetaData": [{
		"id": "connectionInstanceId",
		"required": true,
		"defaultValue": null,
		"localeList": [{
			"locale": "en",
			"fieldGrouping": "General",
			"settingLabel": "Connection Name"
		}],
		"dataType": "com.bmc.arsys.rx.standardlib.record.CharacterFieldDefinition",
		"keySetting": true,
		"fieldOrder": 1
	}, {
		"id": "userName",
		"defaultValue": null,
		"localeList": [{
			"locale": "en",
			"fieldGrouping": "General",
			"settingLabel": "User Name"
		}],
		"dataType": "com.bmc.arsys.rx.standardlib.record.CharacterFieldDefinition",
		"fieldOrder": 2
	}, {
		"id": "password",
		"defaultValue": null,
		"localeList": [{
			"locale": "en",
			"fieldGrouping": "General",
			"settingLabel": "Password"
		}],
		"dataType": "com.bmc.arsys.rx.standardlib.record.CharacterFieldDefinition",
		"fieldOrder": 3
	}, {
		"id": "url",
		"defaultValue": null,
		"localeList": [{
			"locale": "en",
			"fieldGrouping": "General",
			"settingLabel": "Url"
		}],
		"dataType": "com.bmc.arsys.rx.standardlib.record.CharacterFieldDefinition",
		"fieldOrder": 4
	}, {
		"id": "path",
		"defaultValue": null,
		"localeList": [{
			"locale": "en",
			"fieldGrouping": "General",
			"settingLabel": "Path"
		}],
		"dataType": "com.bmc.arsys.rx.standardlib.record.CharacterFieldDefinition",
		"fieldOrder": 5
	}]
}
```

##  Register remote server
To Register Remote Server with AR Server use below POST API for Beta.

```
POST http://95bt-delhi.bmc.com:8008/api/rx/application/remoteserver
Content-Type: application/json

{
  "username": "Allen",
  "password": "password",
  "baseUrl": "http://95bt-delhi.bmc.com:8000/",
  "path": "remoteserver/remoteserverdescriptor"
}
```

##  Test Remote server descriptor
Below API is invoked to get descriptor from remote server and to check if if it is valid.
```
GET http://95bt-delhi.bmc.com:8000/remoteserver/remoteserverdescriptor
Content-Type: application/json

```

## Create Jira connection instance in Admin Setting 

```
Go to Administrator  -->  Connectors ---> com.bmc.arsys.rx.jira

Click on plus sign

Fill in fields with below values 

	connectionInstanceId: jira,
	url: https://partnerpedia.atlassian.net,
	login: dsmconnect",
	password: RSFRocks!
	
```

## Get Status of Running process 

First get the list of all running instances of given process name 
Here process name is 'com.example.work-order-lib:Basic Work Order Fulfillment'

```
GET http://95bt-delhi.bmc.com:8008/api/rx/application/datapage/?dataPageType=com.bmc.arsys.rx.application.process.datapage.ProcessInstanceDataPageQuery&pageSize=-1&startIndex=0&processDefinitionName=com.example.work-order-lib:Basic%20Work%20Order%20Fulfillment
Content-Type: application/json

``` 

## Get status of one running instance with given instanceId

In below instance of process is identified by GUID, 'IDGAA5V0FO0Y8AOJDU46OJDU468S23'

```
GET http://95bt-delhi.bmc.com:8008/api/rx/application/process/processinstance/com.example.work-order-lib:Basic%20Work%20Order%20Fulfillment/IDGAA5V0FO0Y8AOJDU46OJDU468S23
Content-Type: application/json

```
