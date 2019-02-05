/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.transience;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.jena.DatasetWrapper;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange.Operation;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.ChangeSetImpl;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.RDFServiceJena;

/**
 * An RDFService that is based on an in-memory Dataset.
 * 
 * This is very nice for running acceptance tests, because: (1) it runs very
 * quickly, and (2) it is always empty at startup.
 * 
 * The only real challenge is the expectation that VIVO has about the default
 * graph. VIVO expects it to be the union of all of the named graphs. SDB and
 * TDB can be configured to behave this way, but the default in-memory Dataset
 * can not. Or at least, I was not able to find such a configuration.
 * 
 * In order to make this happen, we modify every ChangeSet that is applied,
 * expanding the list of ModelChange objects, so that any additions or deletions
 * that are made to a named graph are also made to the default graph.
 * 
 * NOTE: This could lead to surprising behavior if identical triples were added
 * to two different named graphs, and then one of them was deleted. By the
 * intended scheme (default is union of named graphs), the triple in question
 * would still be found. By the scheme implemented here, it would not be found.
 */
public class RDFServiceTransient extends RDFServiceJena {
	private static final Log log = LogFactory.getLog(RDFServiceTransient.class);

	private final Dataset dataset;

	public RDFServiceTransient() {
		this.dataset = DatasetFactory.create();
		this.dataset.setDefaultModel(ModelFactory.createDefaultModel());
	}

	@Override
	protected DatasetWrapper getDatasetWrapper() {
		return new DatasetWrapper(this.dataset);
	}

	@Override
	public boolean changeSetUpdate(ChangeSet changeSet)
			throws RDFServiceException {

		if (changeSet.getPreconditionQuery() != null
				&& !isPreconditionSatisfied(changeSet.getPreconditionQuery(),
						changeSet.getPreconditionQueryType())) {
			return false;
		}

		try {
			insureThatInputStreamsAreResettable(changeSet);

			if (log.isDebugEnabled()) {
				log.debug("Change Set: " + changeSet);
			}
			notifyListenersOfPreChangeEvents(changeSet);

			dataset.begin(ReadWrite.WRITE);
			try {
				applyChangeSetToModel(
						expandChangeSetToMaintainDefaultModel(changeSet),
						dataset);
				dataset.commit();
			} finally {
				dataset.end();
			}

			notifyListenersOfChanges(changeSet);
			notifyListenersOfPostChangeEvents(changeSet);
			return true;
		} catch (Exception e) {
			log.error(e, e);
			throw new RDFServiceException(e);
		} finally {
			rebuildGraphURICache = true;
		}
	}

	/**
	 * Create a new ChangeSet that expands on the ModelChange list from the
	 * original.
	 * 
	 * All additions and removals that were applied to named graphs are now
	 * applied to the default graph also.
	 */
	private ChangeSet expandChangeSetToMaintainDefaultModel(ChangeSet changeSet)
			throws IOException {
		ChangeSetImpl expanded = new ChangeSetImpl();
		for (ModelChange mc : changeSet.getModelChanges()) {
			if (mc.getOperation() == Operation.ADD) {
				expanded.addAddition(mc.getSerializedModel(),
						mc.getSerializationFormat(), mc.getGraphURI());
				if (mc.getGraphURI() != null) {
					expanded.addAddition(
							duplicateInputStream(mc.getSerializedModel()),
							mc.getSerializationFormat(), null);
				}
			} else { // Operation.REMOVE
				expanded.addRemoval(mc.getSerializedModel(),
						mc.getSerializationFormat(), mc.getGraphURI());
				if (mc.getGraphURI() != null) {
					expanded.addRemoval(
							duplicateInputStream(mc.getSerializedModel()),
							mc.getSerializationFormat(), null);
				}
			}
		}
		return expanded;
	}

	/**
	 * Can't use the same input stream for two ModelChange objects, since
	 * applyChangeSetToModel() doesn't reset the stream.
	 */
	private InputStream duplicateInputStream(InputStream serializedModel)
			throws IOException {
		serializedModel.mark(Integer.MAX_VALUE);
		byte[] bytes = IOUtils.toByteArray(serializedModel);
		serializedModel.reset();
		return new ByteArrayInputStream(bytes);
	}

	public Dataset getDataset() {
		return this.dataset;
	}
}
