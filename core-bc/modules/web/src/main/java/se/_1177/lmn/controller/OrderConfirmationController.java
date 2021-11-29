package se._1177.lmn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import riv.crm.selfservice.medicalsupply._2.ArticleType;
import riv.crm.selfservice.medicalsupply._2.PrescriptionItemType;
import se._1177.lmn.controller.model.Cart;
import se._1177.lmn.controller.session.OrderControllerSession;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Patrik Björk
 */
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderConfirmationController {

    public static final String VIEW_NAME = "Bekräftelse";

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConfirmationController.class);

    @Autowired
    private OrderControllerSession orderControllerSession;

    @Autowired
    private Cart cart;

    private List<ArticleType> articlesWithOneOrderRemaining = new ArrayList<>();
    private List<ArticleType> articlesWithZeroOrdersRemaining = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            Map<ArticleType, Integer> collect = orderControllerSession.getPrescriptionItemInfosToPresent().values().stream()
                    .collect(Collectors.toMap(
                            PrescriptionItemType::getArticle,
                            PrescriptionItemType::getNoOfRemainingOrders
                    ));

            cart.getSuccessfullyOrderedRows().stream()
                    .filter(orderRowType -> collect.containsKey(orderRowType.getArticle()))
                    .forEach(orderRowType -> {
                        Integer integer = collect.get(orderRowType.getArticle());
                        collect.put(orderRowType.getArticle(), integer - 1);
                    });

            articlesWithOneOrderRemaining = collect.entrySet().stream()
                    .filter(entry -> entry.getValue() == 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            articlesWithZeroOrdersRemaining = collect.entrySet().stream()
                    .filter(entry -> entry.getValue() == 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Just to be sure we don't screw something up. The feature of showing remaining orders isn't essential.
            LOGGER.error(e.getMessage(), e);
        }
    }

    public List<ArticleType> getArticlesWithOneOrderRemaining() {
        return articlesWithOneOrderRemaining;
    }

    public List<ArticleType> getArticlesWithZeroOrdersRemaining() {
        return articlesWithZeroOrdersRemaining;
    }
}
