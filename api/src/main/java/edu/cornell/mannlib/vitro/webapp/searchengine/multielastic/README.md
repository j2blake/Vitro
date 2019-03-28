# What is this package?
* The first draft of an Elasticsearch driver for VIVO that 
	* can populate multiple indexes,
	* can create nested fields.

## The gist

### The VIVO search index
VIVO needs a search index. Besides the searches that a user requests, 
VIVO also uses the search index "behind the scenes". The home page, the index page,
and more, all rely on the search index.

### Additional indexes
Some VIVO installations may find it handy to have another search index 
besides the usual one. A second search index may be used to drive an
alternate front-end, or to provide other functions.

A second search index (or third?) should use the same updating mechanism
that the original index uses, so it remains in sync with the data in the
triple store.

### Configuring the search index
The VIVO search indexer uses three types of configuration objects:

* `SearchIndexExcluder` -- tell the indexer that some types of instance should
  not be indexed.
	* For example, timestamps or vcards or "connecting nodes" like authorships or positions. 
	* These types have no documents of their own in the search index.
	* Data from these types may be included in the search documents for related objects,
	  such as people, organizations, or grants.
* `IndexingUriFinder` -- tell the indexer what URIs might be affected when a triple changes.
	* For example, if the name of an Academic Department changes, then the 
	  members of that department must also be re-indexed.
* `DocumentModifier` -- after the minimal document is created, these add fields and values to the document.
	* For example, if a document represents a `foaf:Person`, a `DocumentModifier` might create a 
	  `PREFERRED_TITLE` field to that document.
	  
These objects are usually specified in files in `rdf/display/everytime`.
	  
### The `MultiElasticSearchEngine` 

This implementation of the `SearchEngine` interface provides two additional functions.

#### Support for additional indexes

* When you configure the `MultiElasticSearchEngine` you must specify a "default index",
  which VIVO will use as it always has.
* You may also configure one or more "secondary indexes". These will be populated by
  specially named fields. The fields are created, as always, by the `DocumentModifier` objects.
	* To specify that a field belongs to a secondary index, include the index name, followed by a colon.
	  For example:
		* A field named `course_info` would be created in the default index
		* A field named `graphQL:course_info` would be created in the secondary index named `graphQL`.
		* Within the secondary index, the field would be named `graphQL`.
* This code doesn't provide any way for VIVO to use the secondary indexes. 
  It only provides the way to populate them. 
	* Presumably, other processes will query the secondary index for their own purposes.

#### Support for nested fields

* You may create `DocumentModifier` objects to populate nested fields, in the default index
  or in the secondary index.
	* To specify a nested field, include the parent(s) in the field name, separated by periods.
	  For example:
		* A field named `person.title` would populate a field in the search index named `title`
		  nested within a field named `person`.
		* A field named `graph:person.title` would populate the same field, but in a 
		  secondary index.
* You may need to declare those nested fields in the mapping of the index (see below),
  in order for them to work properly

#### Backward compatibility

* What happens if you configure a `DocumentModifier` with a field named `graph:person.title`
  but you use the standard `SolrSearchEngine` or the `ElasticSearchEngine` instead of the
  `MultiElasticSearchEngine`?
  * You get an field in the standard VIVO index, named `graph:person.title`.
    Not what you wanted, perhaps, but it should not throw exceptions.
   
# How to experiment with this package?
* Install elasticsearch somewhere.
* Decide on the names of your search indexes (see below).
* Create your search indexes with the appropriate mappings (see below).
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

## Decide on the names of your search indexes

### Your default index
This is the index that VIVO will use for its usual activities. 

* The name you choose will be the name of the index in elasticsearch
* In the examples below we will `vivo` as the default index.

### Any secondary indexes
* The name you choose will be the actual name of the index in elasticsearch
* The name will also be the prefix for field names in the configuration of `DocumentModifier` objects.

## A mapping for the default index

* You must explicitly provide a mapping before populating the index.
* If the index uses the default mapping, it will not work correctly. 
* Some fields must be declared as `keyword`, some as unstemmed, etc.

#### Example mapping script:

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
  Any name may be used, but it must match the `hasDefaultIndex` that is specified 
  in `applicationSetup.n3` (see below).
* __*Note:*__ The same first line specifies the location and port number of the elasticsearch server.
  Again, any location and port may be used, but they must match `hasBaseUrl` in `applicationSetup.n3`.

## A mapping for the secondary index
* What are you trying to use this for?
* Here is an example mapping script. It's a silly example, but it shows some nested fields.

```
curl -X PUT "localhost:9200/secondary?pretty" -H 'Content-Type: application/json' -d'
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

* __*Note:*__ The first line of the script specified the name of the index as `secondary`.
  Any name may be used, but it must match (one of) the `hasSecondaryIndex` fields specified
  in `applicationSetup.n3` (see below).
* __*Note:*__ I don't know whether it is necessary to define nested fields in order 
  for them to populate correctly.
* __*Note:*__ There may be other types of fields that need to be defined in the mapping. 

## Check out VIVO and Vitro
* For now, the Elasticsearch driver only lives in my fork of Vitro
* No changes to VIVO are required (yet).

```
git clone https://github.com/vivo-project/VIVO.git
git clone -b feature/multielasticExperiments https://github.com/j2blake/Vitro.git
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
    :hasTargetField "secondary:label" ;
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
    :hasTargetField "secondary:person.type" ;
    :hasSelectQuery """
        SELECT ?type
        WHERE {
          ?uri a ?type .
        }
        """ .

# Store any telephone numbers in the nested "person ==> phone" field.
:documentModifier_multiNestedPhoneNumber
    a   searchIndex:documentBuilding.SelectQueryDocumentModifier ,
        searchIndex:documentBuilding.DocumentModifier ;
    rdfs:label "All labels are added to name fields." ;
    :hasTargetField "secondary:person.phone" ;
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
    "_index" : "secondary",
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

* __*Note:*__ The `DocId` field is present on every record, even though it is not configured.
	* __*TODO:*__ Probably the `URI` field should behave in the same way.
* __*Note:*__ These configurations all use the `SelectQueryDocumentModifier` to populate 
  the fields. It might make more sense, and perhaps be more efficient, to create your own
  custom implementation of the `DocumentModifier` interface. This would be a small 
  Java class that populates some or all of the fields that you require.
* __*Note:*__ See the comments in `SelectQueryDocumentModifier.java` for more information
  about configuring.

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
    :hasDefaultIndex "vivo" ;
    :hasSecondaryIndex "secondary" .
```

* __*Note:*__ You may use zero or more `hasSecondaryIndex` properties depending on how many
  secondary indexes you need to populate.
