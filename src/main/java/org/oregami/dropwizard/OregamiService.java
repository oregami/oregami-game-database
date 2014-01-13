package org.oregami.dropwizard;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.oregami.resources.AdminResource;
import org.oregami.resources.GamesResource;
import org.oregami.resources.HomeResource;

import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.hubspot.dropwizard.guice.GuiceBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
//import com.yammer.dropwizard.db.DatabaseConfiguration;
//import com.yammer.dropwizard.hibernate.HibernateBundle;


public class OregamiService extends Service<OregamiConfiguration> {

	public static final String JPA_UNIT = 
			"data"; 
			//"dataMysql";
	
	private GuiceBundle<OregamiConfiguration> guiceBundle;
	private static final JpaPersistModule jpaPersistModule = new JpaPersistModule(JPA_UNIT);
	
	public static void main(String[] args) throws Exception {
		new OregamiService().run(args);
	}
	
	@Override
	public void initialize(Bootstrap<OregamiConfiguration> bootstrap) {
		bootstrap.setName("oregami-server");
		
		guiceBundle = GuiceBundle.<OregamiConfiguration>newBuilder()
				.addModule(new OregamiGuiceModule())
				.addModule(jpaPersistModule)
				.enableAutoConfig("org.oregami")
				.setConfigClass(OregamiConfiguration.class)
				.build();
		bootstrap.addBundle(guiceBundle);
				
	}

	
	@Override
	public void run(OregamiConfiguration config, Environment environment)
			throws Exception {
		
		FilterBuilder fconfig = environment.addFilter(CrossOriginFilter.class, "/*");
		fconfig.setInitParam(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");	
		fconfig.setInitParam(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		environment.addFilter(guiceBundle.getInjector().getInstance(PersistFilter.class), "/*");

		environment.addResource(guiceBundle.getInjector().getInstance(GamesResource.class));
		environment.addResource(guiceBundle.getInjector().getInstance(HomeResource.class));
		environment.addResource(guiceBundle.getInjector().getInstance(AdminResource.class));
	}

	public static JpaPersistModule createJpaModule() {
		return jpaPersistModule;
	}



}
