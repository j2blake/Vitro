/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.typeStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.InstanceWrapper.InstanceWrapperException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.WrappedInstance.ValidationFailedException;

/**
 * Test the @Validation annotation.
 */
public class ConfigurationBeanLoader_ValidationTest extends
		ConfigurationBeanLoaderTestBase {
	// --------------------------------------------

	@Test
	public void validationMethodHasParameters_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodWithParameter.class)));

		expectSimpleFailure(
				ValidationMethodWithParameter.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class,
						"should not have parameters"));
	}

	public static class ValidationMethodWithParameter {
		@SuppressWarnings("unused")
		@Validation
		public void validateWithParameter(String s) {
			// Nothing to do
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodDoesNotReturnVoid_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodShouldReturnVoid.class)));

		expectSimpleFailure(
				ValidationMethodShouldReturnVoid.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(InstanceWrapperException.class, "should return void"));
	}

	public static class ValidationMethodShouldReturnVoid {
		@Validation
		public String validateWithReturnType() {
			return "Hi there!";
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodNotAccessible_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationMethodIsPrivate.class)));

		expectSimpleFailure(
				ValidationMethodIsPrivate.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ValidationFailedException.class,
						"Error executing validation method"));
	}

	public static class ValidationMethodIsPrivate {
		@Validation
		private void validateIsPrivate() {
			// private method
		}
	}

	// --------------------------------------------

	@Test
	public void validationMethodThrowsException_throwsException()
			throws ConfigurationBeanLoaderException {
		model.add(typeStatement(GENERIC_INSTANCE_URI,
				toJavaUri(ValidationThrowsException.class)));

		expectSimpleFailure(
				ValidationThrowsException.class,
				throwable(ConfigurationBeanLoaderException.class,
						"Failed to load"),
				throwable(ValidationFailedException.class,
						"Error executing validation method"));
	}

	public static class ValidationThrowsException {
		@Validation
		public void validateFails() {
			throw new RuntimeException("from validation method");
		}
	}


}
