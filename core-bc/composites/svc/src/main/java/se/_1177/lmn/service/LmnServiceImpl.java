package se._1177.lmn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.OrderType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
import riv.crm.selfservice.medicalsupply._0.ResultCodeEnum;
import riv.crm.selfservice.medicalsupply._0.ServicePointProviderEnum;
import riv.crm.selfservice.medicalsupply._0.StatusEnum;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypoints._0.rivtabp21.GetMedicalSupplyDeliveryPointsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplydeliverypointsresponder._0.GetMedicalSupplyDeliveryPointsType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptions._0.rivtabp21.GetMedicalSupplyPrescriptionsResponderInterface;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsResponseType;
import riv.crm.selfservice.medicalsupply.getmedicalsupplyprescriptionsresponder._0.GetMedicalSupplyPrescriptionsType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorder._0.rivtabp21.RegisterMedicalSupplyOrderResponderInterface;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderResponseType;
import riv.crm.selfservice.medicalsupply.registermedicalsupplyorderresponder._0.RegisterMedicalSupplyOrderType;
import se._1177.lmn.model.MedicalSupplyPrescriptionsHolder;
import se._1177.lmn.service.util.Util;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrik Björk
 */
public class LmnServiceImpl implements LmnService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LmnServiceImpl.class);

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint;

    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;

    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    private Map<String, DeliveryPointType> deliveryPointIdToDeliveryPoint = new HashMap<>();

    private String logicalAddress;

    public LmnServiceImpl(
            GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint,
            GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions,
            RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder,
            String logicalAddress) {
        this.medicalSupplyDeliveryPoint = medicalSupplyDeliveryPoint;
        this.medicalSupplyPrescriptions = medicalSupplyPrescriptions;
        this.registerMedicalSupplyOrder = registerMedicalSupplyOrder;
        this.logicalAddress = logicalAddress;
    }

    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsResponseType response = getMedicalSupplyPrescriptions(subjectOfCareId);

        MedicalSupplyPrescriptionsHolder holder = new MedicalSupplyPrescriptionsHolder();

        holder.supplyPrescriptionsResponse = response;

        // Separate those which have zero remaining items to order and those which have valid date older than a year

        List<PrescriptionItemType> orderableItems = new ArrayList<>();
        List<PrescriptionItemType> noLongerOrderable = new ArrayList<>();

        if (response.getResultCode().equals(ResultCodeEnum.OK)) {
            for (PrescriptionItemType item : response.getSubjectOfCareType().getPrescriptionItem()) {

                if (item.getNoOfRemainingOrders() <= 0 || !item.getStatus().equals(StatusEnum.AKTIV)) {
                    noLongerOrderable.add(item);
                } else {
                    // Check date
                    XMLGregorianCalendar lastValidDate = item.getLastValidDate();
                    boolean beforeToday = Util.isBeforeToday(lastValidDate);
                    if (beforeToday) {
                        noLongerOrderable.add(item);
                    } else {
                        // Neither old nor "out of stock" or status other than AKTIV.
                        orderableItems.add(item);
                    }
                }
            }

            sortByOrderableToday(orderableItems);

            holder.orderable = orderableItems;
            holder.noLongerOrderable = noLongerOrderable;
        }

        return holder;
    }

    static void sortByOrderableToday(List<PrescriptionItemType> orderableItems) {

        orderableItems.sort((o1, o2) -> {
            Integer sortNumber1 = getSortNumber(o1);
            Integer sortNumber2 = getSortNumber(o2);

            return sortNumber1.compareTo(sortNumber2);
        });
    }

    private static Integer getSortNumber(PrescriptionItemType item) {
        if (!item.getArticle().isIsOrderable()) {
            return 2;
        }

        if (isAfterToday(item.getNextEarliestOrderDate())) {
            return 1;
        }

        return 0;
    }

    @Scheduled(cron = "0 0/15 0-3,4-23 * * ?")
    public void keepWebServiceAwake() {
        // This delays the external web service going into "sleep mode" where it becomes slower than preferred. We allow
        // it to go into sleep mode between 3 and 4 in the night only.
        try {
            LOGGER.info("Scheduled operation...");
            getMedicalSupplyDeliveryPoints(ServicePointProviderEnum.POSTNORD, "41648");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider,
                                                                                     String postalCode) {
        GetMedicalSupplyDeliveryPointsType parameters = new GetMedicalSupplyDeliveryPointsType();

        parameters.setPostalCode(postalCode);
        parameters.setServicePointProvider(provider);

        GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints = medicalSupplyDeliveryPoint
                .getMedicalSupplyDeliveryPoints(logicalAddress, parameters);

        for (DeliveryPointType deliveryPoint : medicalSupplyDeliveryPoints.getDeliveryPoint()) {
            deliveryPointIdToDeliveryPoint.put(deliveryPoint.getDeliveryPointId(), deliveryPoint);
        }

        return medicalSupplyDeliveryPoints;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsType parameters = new GetMedicalSupplyPrescriptionsType();

        parameters.setSubjectOfCareId(subjectOfCareId);

        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = this.medicalSupplyPrescriptions
                .getMedicalSupplyPrescriptions(logicalAddress, parameters);

        return medicalSupplyPrescriptions;
    }

    @Override
    public RegisterMedicalSupplyOrderResponseType registerMedicalSupplyOrder(
            String subjectOfCareId,
            boolean orderByDelegate,
            String orderer, // May be delegate
            List<PrescriptionItemType> prescriptionItems,
            Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {
        RegisterMedicalSupplyOrderType parameters = new RegisterMedicalSupplyOrderType();

        OrderType order = new OrderType();

        order.setSubjectOfCareId(subjectOfCareId);
        order.setOrderByDelegate(orderByDelegate);
        order.setOrderer(orderer);

        addOrderRows(prescriptionItems, order, deliveryChoicePerItem);

        parameters.setOrder(order);

        return registerMedicalSupplyOrder.registerMedicalSupplyOrder(logicalAddress, parameters);
    }

    @Override
    public DeliveryPointType getDeliveryPointById(String deliveryPointId) {
        return deliveryPointIdToDeliveryPoint.get(deliveryPointId);
    }

    void addOrderRows(List<PrescriptionItemType> articleNumbers, OrderType order, Map<PrescriptionItemType, DeliveryChoiceType> deliveryChoicePerItem) {
        for (PrescriptionItemType item : articleNumbers) {
            OrderRowType orderRow = new OrderRowType();

            DeliveryChoiceType deliveryChoice = deliveryChoicePerItem.get(item);

            orderRow.setDeliveryChoice(deliveryChoice);

            orderRow.setArticle(item.getArticle());

            orderRow.setNoOfPackages(item.getNoOfPackagesPerOrder());

            orderRow.setNoOfPcs(item.getNoOfArticlesPerOrder());

            orderRow.setPrescriptionId(item.getPrescriptionId());

            orderRow.setPrescriptionItemId(item.getPrescriptionItemId());

//            orderRow.setSource(); // TODO: 2016-05-24  

            order.getOrderRow().add(orderRow);
        }
    }

    public static boolean isAfterToday(XMLGregorianCalendar date) {
        return Util.isAfterToday(date);
    }
}

