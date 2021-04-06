package org.lamisplus.modules.bootstrap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;

public class BootstrapModule extends AcrossModule {
    public final static String NAME = "BootstrapModule";

    public BootstrapModule() {
        super();
        addApplicationContextConfigurer(new ComponentScanConfigurer(getClass().getPackage().getName() + ".module"));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
