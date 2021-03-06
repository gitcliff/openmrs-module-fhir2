/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.providers.r4;

import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.openmrs.module.fhir2.BaseFhirIntegrationTest;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class BaseFhirR4IntegrationTest<T extends IResourceProvider, U extends DomainResource> extends BaseFhirIntegrationTest<T, U> {
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
	
	@Override
	public String getServletName() {
		return "fhir2Servlet";
	}
	
	@Override
	public FhirContext getFhirContext() {
		return FHIR_CONTEXT;
	}
	
	@Override
	public FhirRestServlet getRestfulServer() {
		return new FhirRestServlet();
	}
	
	@Override
	public void describeOperationOutcome(Description mismatchDescription, IBaseOperationOutcome baseOperationOutcome) {
		if (baseOperationOutcome instanceof OperationOutcome) {
			OperationOutcome operationOutcome = (OperationOutcome) baseOperationOutcome;
			if (operationOutcome.hasIssue()) {
				mismatchDescription.appendText(" with message ");
				mismatchDescription.appendValue(
				    operationOutcome.getIssue().stream().map(OperationOutcome.OperationOutcomeIssueComponent::getDiagnostics)
				            .collect(Collectors.joining(". ")));
			}
		}
	}
	
	@Override
	public Class<? extends IBaseOperationOutcome> getOperationOutcomeClass() {
		return OperationOutcome.class;
	}
	
	@Override
	public U removeNarrative(U item) {
		@SuppressWarnings("unchecked")
		U newItem = (U) item.copy();
		newItem.setText(null);
		return newItem;
	}
	
	@Override
	public Bundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (Bundle) super.readBundleResponse(response);
	}
	
	@Override
	public OperationOutcome readOperationOutcome(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (OperationOutcome) super.readOperationOutcome(response);
	}
	
	public static Matcher<Bundle.BundleEntryComponent> hasResource(Matcher<? extends IDomainResource> matcher) {
		return new HasResourceMatcher(matcher);
	}
	
	private static class HasResourceMatcher extends TypeSafeMatcher<Bundle.BundleEntryComponent> {
		
		private final Matcher<? extends IDomainResource> matcher;
		
		public HasResourceMatcher(Matcher<? extends IDomainResource> matcher) {
			this.matcher = matcher;
		}
		
		@Override
		protected boolean matchesSafely(Bundle.BundleEntryComponent item) {
			return matcher.matches(item.getResource());
		}
		
		@Override
		public void describeTo(Description description) {
			description.appendText("a bundle component with a resource that ").appendDescriptionOf(matcher);
		}
		
		@Override
		protected void describeMismatchSafely(Bundle.BundleEntryComponent item, Description mismatchDescription) {
			matcher.describeMismatch(item.getResource(), mismatchDescription);
		}
	}
}
