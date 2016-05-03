package se._1177.lmn.service;

import riv.crm.selfservice.medicalsupply._0.AdressType;
import riv.crm.selfservice.medicalsupply._0.DeliveryAlternativeType;
import riv.crm.selfservice.medicalsupply._0.DeliveryChoiceType;
import riv.crm.selfservice.medicalsupply._0.DeliveryMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryNotificationMethodEnum;
import riv.crm.selfservice.medicalsupply._0.DeliveryPointType;
import riv.crm.selfservice.medicalsupply._0.OrderRowType;
import riv.crm.selfservice.medicalsupply._0.OrderType;
import riv.crm.selfservice.medicalsupply._0.PrescriptionItemType;
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

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static se._1177.lmn.service.util.Util.isOlderThanAYear;

/**
 * @author Patrik Björk
 */
public class LmnServiceImpl implements LmnService {

    private GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint;

    private GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions;

    private RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder;

    private Map<DeliveryMethodEnum, String> deliveryMethodToDeliveryMethodId = new HashMap<>();
    private Map<String, DeliveryPointType> deliveryPointIdToDeliveryPoint = new HashMap<>();

    public LmnServiceImpl(
            GetMedicalSupplyDeliveryPointsResponderInterface medicalSupplyDeliveryPoint,
            GetMedicalSupplyPrescriptionsResponderInterface medicalSupplyPrescriptions,
            RegisterMedicalSupplyOrderResponderInterface registerMedicalSupplyOrder) {
        this.medicalSupplyDeliveryPoint = medicalSupplyDeliveryPoint;
        this.medicalSupplyPrescriptions = medicalSupplyPrescriptions;
        this.registerMedicalSupplyOrder = registerMedicalSupplyOrder;
    }

    @Override
    public MedicalSupplyPrescriptionsHolder getMedicalSupplyPrescriptionsHolder(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = getMedicalSupplyPrescriptions(subjectOfCareId);

        MedicalSupplyPrescriptionsHolder holder = new MedicalSupplyPrescriptionsHolder();

        holder.supplyPrescriptionsResponse = medicalSupplyPrescriptions;

        // Separate those which have zero remaining items to order and those which have valid date older than a year

        List<PrescriptionItemType> orderableItems = new ArrayList<>();
        List<PrescriptionItemType> noLongerOrderable = new ArrayList<>();

        for (PrescriptionItemType item : medicalSupplyPrescriptions.getSubjectOfCareType().getPrescriptionItem()) {

            if (item.getNoOfRemainingOrders() <= 0 || !item.getStatus().equals(StatusEnum.AKTIV)) {
                noLongerOrderable.add(item);
            } else {
                // Check date
                XMLGregorianCalendar lastValidDate = item.getLastValidDate();
                boolean olderThanAYear = isOlderThanAYear(lastValidDate);
                if (olderThanAYear) {
                    noLongerOrderable.add(item);
                } else {
                    // Neither old nor "out of stock" or status other than AKTIV.
                    orderableItems.add(item);
                }
            }
        }

        holder.orderable = orderableItems;
        holder.noLongerOrderable = noLongerOrderable;

        return holder;
    }

    public GetMedicalSupplyDeliveryPointsResponseType getMedicalSupplyDeliveryPoints(ServicePointProviderEnum provider,
                                                                                     String postalCode) {
        GetMedicalSupplyDeliveryPointsType parameters = new GetMedicalSupplyDeliveryPointsType();

        parameters.setPostalCode(postalCode);
        parameters.setServicePointProvider(provider);

        GetMedicalSupplyDeliveryPointsResponseType medicalSupplyDeliveryPoints = medicalSupplyDeliveryPoint
                .getMedicalSupplyDeliveryPoints("", parameters);


        for (DeliveryPointType deliveryPoint : medicalSupplyDeliveryPoints.getDeliveryPoint()) {
            deliveryPointIdToDeliveryPoint.put(deliveryPoint.getDeliveryPointId(), deliveryPoint);
        }

        return medicalSupplyDeliveryPoints;
    }

    public GetMedicalSupplyPrescriptionsResponseType getMedicalSupplyPrescriptions(String subjectOfCareId) {
        GetMedicalSupplyPrescriptionsType parameters = new GetMedicalSupplyPrescriptionsType();

        parameters.setSubjectOfCareId(subjectOfCareId);

        GetMedicalSupplyPrescriptionsResponseType medicalSupplyPrescriptions = this.medicalSupplyPrescriptions
                .getMedicalSupplyPrescriptions("", parameters);

        for (PrescriptionItemType item : medicalSupplyPrescriptions.getSubjectOfCareType().getPrescriptionItem()) {
            for (DeliveryAlternativeType deliveryAlternative : item.getDeliveryAlternative()) {

                // Assert they never change id.
                DeliveryMethodEnum deliveryMethod = deliveryAlternative.getDeliveryMethod();
                if (deliveryMethodToDeliveryMethodId.containsKey(deliveryMethod) &&
                        deliveryMethodToDeliveryMethodId.get(deliveryMethod)
                                .equals(deliveryAlternative.getDeliveryMethodId())) {
                    throw new RuntimeException("Thd delivery method id of a delivery method is not expected to change.");
                }

                // Save these for later
                deliveryMethodToDeliveryMethodId.put(deliveryMethod,
                                                        deliveryAlternative.getDeliveryMethodId());
            }
        }

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

        parameters.getOrder().add(order);


        return registerMedicalSupplyOrder.registerMedicalSupplyOrder("", parameters);
    }

    @Override
    public String getDeliveryMethodId(DeliveryMethodEnum deliveryMethod) {
        return deliveryMethodToDeliveryMethodId.get(deliveryMethod);
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

            order.getOrderRow().add(orderRow);
        }
    }
}
