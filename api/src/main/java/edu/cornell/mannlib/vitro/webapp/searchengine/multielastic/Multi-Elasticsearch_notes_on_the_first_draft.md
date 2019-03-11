# What is this package?
* The first draft of an Elasticsearch driver for VIVO that can populate more than one index.

# What has been done? 
* Implement the `SearchEngine` interface
	* Classes in `edu.cornell.mannlib.vitro.webapp.searchengine.multielastic`
* No attempt to add new functions.

# Is this ready for production?
No. This is based on the first draft Elasticsearch driver, and suffers from the same 
shortcomings (and maybe more). Check [here][notes_on_first_draft] for more details.

# How to experiment with it?
* Again, check out [this document][notes_on_first_draft].
* But, instead of modifying `{vitro_home}/config/applicationSetup.n3` as that document says,
  modify it as shown below

# The details:

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
    :hasSecondaryIndex "graphql" .
```




[notes_on_first_draft]: ../elasticsearch/Elasticsearch_notes_on_the_first_draft.md