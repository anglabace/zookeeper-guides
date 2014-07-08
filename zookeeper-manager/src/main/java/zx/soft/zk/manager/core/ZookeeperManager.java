package zx.soft.zk.manager.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration.ClassList;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

import zx.soft.zk.manager.dao.Dao;

public class ZookeeperManager {

	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ZookeeperManager.class);

	public static void main(String[] args) throws Exception {

		logger.info("Starting ZKUI!");
		Properties globalProps = new Properties();
		File f = new File("config.cfg");
		if (f.exists()) {
			globalProps.load(new FileInputStream("config.cfg"));
		} else {
			System.out.println("Please create config.cfg properties file and then execute the program!");
			System.exit(1);
		}

		globalProps.setProperty("uptime", new Date().toString());
		new Dao(globalProps).checkNCreate();

		String webFolder = "webapp";
		Server server = new Server(Integer.parseInt(globalProps.getProperty("serverPort")));

		WebAppContext servletContextHandler = new WebAppContext();
		servletContextHandler.setContextPath("/");
		servletContextHandler.setResourceBase("src/main/resources/" + webFolder);
		ClassList clist = ClassList.setServerDefault(server);
		clist.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
		servletContextHandler.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
				".*(/target/classes/|.*.jar)");
		servletContextHandler.setParentLoaderPriority(true);
		servletContextHandler.setInitParameter("useFileMappedBuffer", "false");
		servletContextHandler.setAttribute("globalProps", globalProps);

		ResourceHandler staticResourceHandler = new ResourceHandler();
		staticResourceHandler.setDirectoriesListed(false);
		Resource staticResources = Resource.newClassPathResource(webFolder);
		staticResourceHandler.setBaseResource(staticResources);
		staticResourceHandler.setWelcomeFiles(new String[] { "html/index.html" });

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { staticResourceHandler, servletContextHandler });

		server.setHandler(handlers);

		server.start();
		server.join();
	}

}
