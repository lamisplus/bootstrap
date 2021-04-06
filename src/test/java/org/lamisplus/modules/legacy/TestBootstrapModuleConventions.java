package org.lamisplus.modules.legacy;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.AbstractAcrossModuleConventionsTest;
import org.lamisplus.modules.bootstrap.BootstrapModule;

public class TestBootstrapModuleConventions extends AbstractAcrossModuleConventionsTest
{

	@Override
	protected AcrossModule createModule() {
		return new BootstrapModule();
	}
}
