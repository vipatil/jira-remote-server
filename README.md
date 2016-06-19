##  Create 'Remote Servers'  Setting Definition

...
POST /api/rx/application/admin-settings/component HTTP/1.1
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
...

##  Register remote server

...
POST /api/rx/application/remoteserver HTTP/1.1
Content-Type: application/json

{
  "username": "Allen",
  "password": "password",
  "baseUrl": "http://localhost:8000/",
  "path": "remoteserver/remoteserverdescriptor"
}
...

##  Test Remote server descriptor

...
GET /remoteserver/remoteserverdescriptor HTTP/1.1
Host: 127.0.0.1:8000
Content-Type: application/json

...