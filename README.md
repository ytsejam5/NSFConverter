# NSFConverter
A sample tool to convert Lotus Domino/Notes NSF files to MarkLogic documents

## Overview
NSFConverter attempts to convert to HTMLs from documents extracted from Lotus Domino/Notes NSF files input from HTTP body stream instead of "File type identification only"-ed ISYS Document Filters(*), and post HTMLs to MarkLogic XDBC Server.
(*) http://www.marklogic.com/resources/marklogic-document-format-support/resource_download/datasheets/


## Requirements
- Lotus Notes JVM and jar files (tested with Notes 9)
- MarkLogic and XCC/J library jar files (testd with MarkLogic 7.0.5)
- Java Appicatin Server (tested with Tomcat 7.0.59)

## Instration
1. Compile sources included in com.github.ytsejam5.nsfconverter package for compatibility with Java 1.5.
1. Place class files to web application library directory, webapp/WEB-INF/classes(/com/github/ytsejam5/nsfconverter).
1. Open webapp/WEB-INF/web.xml and edit cofigration to connect your MarkLogic XDBC server.
1. Deploy webapp/ to you web application server. (If using Tomcat, refer files in tomcat-bin-sample/ to set Domino/Notes related parameters in your environment.)

## How to use
- CASE 1: calling from MarkLogic CPF modules:
``` example.xqr
let $converter-url := "http://${CONVERTER_HOST}:${CONVERTER_PORT}/nsfconveter"
let $document := fn:doc($cpf:document-uri)
let $filtered-data := xdmp:document-filter($document)
let $content-type := $filtered-data/*:head/*:meta[@name eq "content-type"]/@content
return
	if ($content-type eq "application/vnd.lotus-wordpro") then (
		xdmp:http-post($converter-url, (()), ($document/node()))
	) else ()
```

- CASE 2: calling from curl.
```
curl -X GET --data-binary @test.nsf http://${CONVERTER_HOST}:${CONVERTER_PORT}/nsfconveter
```
