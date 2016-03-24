package se.vgregion.mvk.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Patrik Björk
 */
@Component
@Scope("request")
public class UtilController {

    public String getLinkSuffix() {
        return "?faces-redirect=true&amp;includeViewParams=true";
    }
}
