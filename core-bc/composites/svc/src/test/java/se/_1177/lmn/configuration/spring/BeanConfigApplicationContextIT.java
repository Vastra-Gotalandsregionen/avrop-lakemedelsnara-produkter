package se._1177.lmn.configuration.spring;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se._1177.lmn.service.LmnService;
import se._1177.lmn.testconfig.RedisSessionConfig;

import java.net.URL;

import static org.junit.Assert.assertNotNull;

// E.g. start a Docker container via 'docker run --name some-redis -p 6379:6379 -d redis'
// to test the application context.

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BeanConfig.class, CachingConfig.class, RedisSessionConfig.class/*, TestRedisConfiguration.class*/})
public class BeanConfigApplicationContextIT {

    @Autowired
    private ApplicationContext ctx;

    @BeforeClass
    public static void setup() {
        URL truststore = BeanConfigApplicationContextIT.class.getClassLoader().getResource("truststore.jks");
        URL keystore = BeanConfigApplicationContextIT.class.getClassLoader().getResource("keystore.p12");

        System.setProperty("ssl_truststore", truststore.getFile());
        System.setProperty("ssl_keystore", keystore.getFile());
    }

    @Test
    public void contextLoaded() throws Exception {
        assertNotNull(ctx);
    }

    @Test
    public void getBean() throws Exception {
        assertNotNull(ctx.getBeansOfType(LmnService.class));
    }

}
