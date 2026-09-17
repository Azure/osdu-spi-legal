package org.opengroup.osdu.legal.tags.validation;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.validation.NameValidator;
import org.opengroup.osdu.core.common.model.legal.validation.OriginatorValidator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OriginatorValidatorTests {

	@Test
	public void should_allowOriginator_That_containLettersAndNumbersWhitespacePeriodHyphen() {

		OriginatorValidator validator = new OriginatorValidator();
		assertTrue(validator.isValid("Usual format", null));
		assertTrue(validator.isValid("UPPERCASE", null));
		assertTrue(validator.isValid("withNumb3r", null));
		assertTrue(validator.isValid("123", null));
		assertTrue(validator.isValid("MyCompany-Tech Corp.", null));
	}

	@Test
	public void should_allowOriginatorThatContain60LettersAndNumbers() {
		OriginatorValidator validator = new OriginatorValidator();
		assertTrue(validator.isValid("thisistest-thisistest-thisistest-thisistest-thisistest-thisi", null));
	}

	@Test
	public void should_not_allowOriginatorThatContain61LettersAndNumbers() {
		OriginatorValidator validator = new OriginatorValidator();
		assertFalse(validator.isValid("thisistest-thisistest-thisistest-thisistest-thisistest-thisis", null));
	}

	@Test
	public void should_notAllowNames_that_containNonAlphaNumericIsLessThan3Characters() {

		OriginatorValidator validator = new OriginatorValidator();
		assertFalse(validator.isValid("Bad!", null));
		assertFalse(validator.isValid("Bad)", null));
		assertFalse(validator.isValid("Bad:", null));
		assertFalse(validator.isValid("Bad>", null));
		assertFalse(validator.isValid("Bad_", null));
		assertFalse(validator.isValid("Bad+", null));
		assertFalse(validator.isValid("ts", null));
		assertFalse(validator.isValid("    ", null));
		assertFalse(validator.isValid(null, null));
	}

	@Test
	public void initialize() {
		// for coverage purposes. Do nothing method!
		NameValidator validator = new NameValidator();
		validator.initialize(null);
		assertFalse(validator.isValid(null, null));
	}
}
