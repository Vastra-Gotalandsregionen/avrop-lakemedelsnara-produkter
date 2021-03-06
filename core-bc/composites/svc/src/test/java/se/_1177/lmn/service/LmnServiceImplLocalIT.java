package se._1177.lmn.service;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import riv.crm.selfservice.medicalsupply._1.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._1.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._1.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._1.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._1.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._1.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._1.RegisterMedicalSupplyOrderResponseType;
import se._1177.lmn.service.mock.MockWebServiceServer;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;

/**
 * @author Patrik Björk
 */
public class LmnServiceImplLocalIT {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoints;
    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;
    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;
    private LmnServiceImpl lmnService;

    @BeforeClass
    public static void setupMockServer() {
        MockWebServiceServer.publishEndpoints(18081);
    }

    @AfterClass
    public static void stopMockServer() {
        MockWebServiceServer.shutdown();
    }

    @Before
    public void setup() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("webservice-context-test.xml");

        medicalSupplyDeliveryPoints = ctx.getBean(GetMedicalSupplyDeliveryPointsResponderInterface.class);
        medicalSupplyPrescriptions = ctx.getBean(GetMedicalSupplyPrescriptionsResponderInterface.class);
        registerMedicalSupplyOrder = ctx.getBean(RegisterMedicalSupplyOrderResponderInterface.class);

        lmnService = new LmnServiceImpl(medicalSupplyDeliveryPoints, medicalSupplyPrescriptions,
                registerMedicalSupplyOrder, "asdfasdf", "SE000111-234", true);
    }

    @Test
    public void context() {
        assertNotNull(medicalSupplyDeliveryPoints);
    }

    @Test
    @Ignore // FIXME
    public void smokeTest() throws Exception {

        GetMedicalSupplyDeliveryPointsResponseType deliveryPoints =
                lmnService.getMedicalSupplyDeliveryPoints(ServicePointProviderEnum.POSTNORD, "12345");

        GetMedicalSupplyPrescriptionsResponseType prescriptions =
                lmnService.getMedicalSupplyPrescriptions("19121212-1212");

        RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder =
                lmnService.registerMedicalSupplyOrder("19121212-1212", false, "name", new ArrayList<>(),
                        new HashMap<>());
    }

}
