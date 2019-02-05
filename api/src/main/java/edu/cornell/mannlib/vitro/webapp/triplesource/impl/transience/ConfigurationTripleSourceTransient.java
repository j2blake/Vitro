/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.transience;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ModelMakerWithPersistentEmptyModels;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.transience.RDFServiceTransient;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A ConfigurationTripleSource that is based on an in-memory RDFService.
 * 
 * This is very nice for running acceptance tests, because: (1) it runs very
 * quickly, and (2) it is always empty at startup.
 */
public class ConfigurationTripleSourceTransient
		extends ConfigurationTripleSource {

	private volatile RDFServiceTransient rdfService;
	private RDFServiceFactory rdfServiceFactory;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		try {
			this.rdfService = new RDFServiceTransient();
			this.rdfServiceFactory = createRDFServiceFactory();
			this.dataset = rdfService.getDataset();
			this.modelMaker = createModelMaker();
			ss.info("Initialized the RDF source for Transient storage");
		} catch (Exception e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for Transient storage", e);
		}
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new LoggingRDFServiceFactory(
				new RDFServiceFactorySingle(this.rdfService));
	}

	private ModelMaker createModelMaker() {
		ModelMaker longTermModelMaker = new ModelMakerWithPersistentEmptyModels(
				new MemoryMappingModelMaker(
						new RDFServiceModelMaker(this.rdfService),
						CONFIGURATION_MODELS));
		return addConfigurationDecorators(longTermModelMaker);
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public RDFService getRDFService() {
		return this.rdfService;
	}

	@Override
	public Dataset getDataset() {
		return this.dataset;
	}

	@Override
	public ModelMaker getModelMaker() {
		return this.modelMaker;
	}

	@Override
	public OntModelCache getShortTermOntModels(RDFService shortTermRdfService,
			OntModelCache longTermOntModelCache) {
		// No need to use short-term models.
		return longTermOntModelCache;
	}

	@Override
	public String toString() {
		return "ConfigurationTripleSourceTransient[" + ToString.hashHex(this)
				+ "]";
	}

	@Override
	public void shutdown(Application application) {
		synchronized (this) {
			if (this.rdfService != null) {
				this.rdfService.close();
				this.rdfService = null;
			}
		}
	}

}
