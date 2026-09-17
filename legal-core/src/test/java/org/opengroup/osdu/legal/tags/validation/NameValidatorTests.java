package org.opengroup.osdu.legal.tags.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.validation.NameValidator;

public class NameValidatorTests {

	@Test
	public void should_allowNamesThatContainHyphenLettersAndNumbers() {

		NameValidator validator = new NameValidator();
		assertTrue(validator.isValid("Usual-expected-format", null));
		assertTrue(validator.isValid("UPPERCASE", null));
		assertTrue(validator.isValid("with-dash", null));
		assertTrue(validator.isValid("123", null));
	}

	@Test
	public void should_allowNamesThatContain100LettersAndNumbers() {
		NameValidator validator = new NameValidator();
		assertTrue(validator.isValid("thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest12", null));
	}

	@Test
	public void should_not_allowNamesThatContain101LettersAndNumbers() {
		NameValidator validator = new NameValidator();
		assertFalse(validator.isValid("thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest-thisistest101", null));
	}

	@Test
	public void should_notAllowNames_that_containNonAlphaNumericOrWhitespaceOrIsLessTHan3Characters() {

		NameValidator validator = new NameValidator();
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
