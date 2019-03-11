# What is this package?
* The first draft of an Elasticsearch driver for VIVO that 
	* can populate multiple indexes,
	* can create nested fields.

# What has been done? 
* Implement the `SearchEngine` interface
	* Classes in `edu.cornell.mannlib.vitro.webapp.searchengine.multielastic`
* No attempt to add new functions.

# How to experiment with it?
* Install elasticsearch somewhere.
* Create one or more search indexes with the appropriate mappings (see below).
* Start elasticsearch
* Check out VIVO and this branch of Vitro (see below), 
* Create a new configuration file that will populate the secondary index (see below).
* Do the usual installation procedure.
* Modify `{vitro_home}/config/applicationSetup.n3` to use this driver (see below).
* Start VIVO

# Not ready for production
* Documentation
	* Instructions on how to install and configure the driver.
	* Instructions on how to setup elasticsearch?
* Smoke test
	* Display a warning if the elasticsearch server is not responding.
* Functional testing
	* Are we getting the proper search results?
	* Are search results in the order that we would like?
* Improved snippets
	* Copy the technique used for Solr
* Code improvement
	* Rigorous closing of HTTP connections.
	* IOC for HTTP code, to help in unit testing
	* Consistent use of exceptions and logging
* Unit tests
* Automatic initialization of the index
	* If VIVO detects an empty index, apply the mapping.

# The details:

## A mapping for the search index
* If the index uses the default mapping, it will not work correctly.
* Some fields must be declared as `keyword`, some as unstemmed, etc.

* Example mapping script:

```
curl -X PUT "localhost:9200/vivo?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "_doc": { 
      "properties": { 
        "ALLTEXT": { 
          "type": "text",
          "analyzer": "english"
        }, 
        "ALLTEXTUNSTEMMED": { 
          "type": "text",
          "analyzer": "standard"
        }, 
        "DocId": {
          "type": "keyword"  
        }, 
        "classgroup": {
          "type": "keyword"  
        }, 
        "type": {
          "type": "keyword"  
        }, 
        "mostSpecificTypeURIs": {
          "type": "keyword"  
        }, 
        "indexedTime": { 
          "type": "long" 
        },
        "nameRaw": { 
          "type": "keyword" 
        },
        "URI": { 
          "type": "keyword" 
        },
        "THUMBNAIL": { 
          "type": "integer" 
        },
        "THUMBNAIL_URL": { 
          "type": "keyword" 
        },
        "nameLowercaseSingleValued": {
          "type": "text",
          "analyzer": "standard",
          "fielddata": "true"
        },
        "BETA" : {
          "type" : "float"
        }
      }
    }
  },
  "query": {
    "default_field": "ALLTEXT"
  }
}
'
```
* __*Note:*__ The first line of the script specifies the name of the index as `vivo`. 
Any name may be used, but it must match the "base URL" that is specified in `applicationSetup.n3` (see below).
* __*Note:*__ The same first line specifies the location and port number of the elasticsearch server.
Again, any location and port may be used, but they must match the "base URL" in `applicationSetup.n3`.

## A mapping for the secondary index
* What are you trying to use this for?
* Here is an example mapping script. It's a silly example, but it shows some nested fields.

```
curl -X PUT "localhost:9200/multi_secondary?pretty" -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "_doc": { 
      "properties": { 
        "person": {
          "type": "nested" 
        }
      }
    }
  },
  "query": {
    "default_field": "ALLTEXT"
  }
}
'
```

## Check out VIVO and Vitro
* For now, the Elasticsearch driver only lives in my fork of Vitro
* No changes to VIVO are required (yet).

```
git clone https://github.com/vivo-project/VIVO.git
git clone -b feature/elasticsearchExperiments https://github.com/j2blake/Vitro.git
```

## Create the search configuration file
* Again, a silly example, but it serves to illustrate.

```
@prefix : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix searchIndex: <java:edu.cornell.mannlib.vitro.webapp.searchindex#> .


#
# configure these DocumentModifiers for the secondary search indexes.
#

# Store the label of each individual in the "label" field.
:documentModifier_multiLabel
    a   searchIndex:documentBuilding.SelectQueryDocumentModifier ,
        searchIndex:documentBuilding.DocumentModifier ;
    rdfs:label "All labels are added to name fields." ;
    :hasTargetField "multi_secondary:label" ;
    :hasSelectQuery """
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
		SELECT ?label 
		WHERE {
			?uri rdfs:label ?label .
	    }
        """ .

# Store each of the types in the nested "person ==> type" field.
:documentModifier_multiNestedType
    a   searchIndex:documentBuilding.SelectQueryDocumentModifier ,
        searchIndex:documentBuilding.DocumentModifier ;
    rdfs:label "All labels are added to name fields." ;
    :hasTargetField "multi_secondary:person.type" ;
    :hasSelectQuery """
		SELECT ?type
		WHERE {
			?uri a ?type .
	    }
        """ .

# Store any telephone numbers in the nested "person ==> type" field.
:documentModifier_multiNestedPhoneNumber
    a   searchIndex:documentBuilding.SelectQueryDocumentModifier ,
        searchIndex:documentBuilding.DocumentModifier ;
    rdfs:label "All labels are added to name fields." ;
    :hasTargetField "multi_secondary:person.phone" ;
    :hasSelectQuery """
        PREFIX obo: <http://purl.obolibrary.org/obo/>
        PREFIX vcard: <http://www.w3.org/2006/vcard/ns#>
		SELECT ?phone
		WHERE {
			?uri obo:ARG_2000028 ?contact .
			?contact vcard:hasTelephone ?vcardphone .
			?vcardphone vcard:telephone ?phone .
	    }
        """ .
```

This will produce search documents in this form:

```
  {
    "_index" : "multi_secondary",
    "_type" : "_doc",
    "_id" : "vitroIndividual:http://scholars.cornell.edu/individual/n1073",
    "_score" : 1.0,
    "_source" : {
      "DocId" : [
        "vitroIndividual:http://scholars.cornell.edu/individual/n1073"
      ],
      "person" : [
        {
          "phone" : [
            "123-4567-890"
          ],
          "type" : [
            "http://vivoweb.org/ontology/core#FacultyMember",
            "http://purl.obolibrary.org/obo/BFO_0000002",
            "http://purl.obolibrary.org/obo/BFO_0000004",
            "http://www.w3.org/2002/07/owl#Thing",
            "http://xmlns.com/foaf/0.1/Person",
            "http://purl.obolibrary.org/obo/BFO_0000001",
            "http://xmlns.com/foaf/0.1/Agent"
          ]
        }
      ],
      "label" : [
        "Foxtrot, Echo"
      ]
    }
  }
```

## Modify `applicationSetup.n3`
* Change this:

```
# ----------------------------
#
# Search engine module: 
#    The Solr-based implementation is the only standard option, but it can be
#    wrapped in an "instrumented" wrapper, which provides additional logging 
#    and more rigorous life-cycle checking.
#

:instrumentedSearchEngineWrapper 
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper> , 
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :wraps :solrSearchEngine .

```

* To this:

```
# ----------------------------
#
# Search engine module: 
#    The Solr-based implementation is the only standard option, but it can be
#    wrapped in an "instrumented" wrapper, which provides additional logging 
#    and more rigorous life-cycle checking.
#

:instrumentedSearchEngineWrapper 
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper> , 
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :wraps :multiElasticSearchEngine .

:multiElasticSearchEngine
    a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.multielastic.MultiElasticSearchEngine> ,
        <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
    :hasBaseUrl "http://localhost:9200" ;
    :hasDefaultIndex "multi_default" ;
    :hasSecondaryIndex "multi_secondary" .
```

## Enhance the contents of the search index
### An example: Publication URIs in the author's search document
* Add a keyword field to the search index

```
        "publicationURI": { 
          "type": "keyword" 
        },
```

* Add a `DocumentModifier` to VIVO.

```
:documentModifier_publications
    a   <java:edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.SelectQueryDocumentModifier> ,
        <java:edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifier> ;
    rdfs:label "URIs of publications are added to publicationURI field." ;
    :hasTargetField "publicationURI" ;
    :hasSelectQuery """
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
        PREFIX vivo: <http://vivoweb.org/ontology/core#>
        PREFIX bibo: <http://purl.org/ontology/bibo/>
        SELECT ?publication 
		WHERE {
			?uri vivo:relatedBy ?authorship .
			?authorship a vivo:Authorship .
			?authorship vivo:relates ?publication .
			?publication a bibo:Document .
	    }
	    """ .
```

## Use data distributors to query the search index
* Install the Data Distribution API
* Add a distributor:

```
:drill_by_URI
    a   <java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor> ,
        <java:edu.cornell.library.scholars.webapp.controller.api.distribute.search.DrillDownSearchByUriDataDistributor> ;
    :actionName "searchAndDrill" .
```

* Run the query:

```
http://localhost:8080/vivo/api/dataRequest/searchAndDrill?uri=http://scholars.cornell.edu/individual/mj495
```