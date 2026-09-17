package org.opengroup.osdu.legal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

public class ServiceConfigTests {

	@Test
	public void should_getEnvVariables() {
		ServiceConfig.Instance().AUTHORIZE_API = "HHH";
		ServiceConfig.Instance().LEGAL_HOSTNAME = "III";

		assertEquals("HHH", ServiceConfig.Instance().getAuthorizeAPI());
		assertEquals("III", ServiceConfig.Instance().getHostname());
	}

	@Test
	public void should_returnExpectedRegion() {
		ServiceConfig.Instance().REGION = "us-central";
		assertEquals("us", ServiceConfig.Instance().getRegion());

		ServiceConfig.Instance().REGION = "europe-west";
		assertEquals("eu", ServiceConfig.Instance().getRegion());

		ServiceConfig.Instance().REGION = "asia-east";
		assertEquals("asia", ServiceConfig.Instance().getRegion());
	}
}