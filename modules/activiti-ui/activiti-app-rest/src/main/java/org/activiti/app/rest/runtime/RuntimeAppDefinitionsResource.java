package org.activiti.app.rest.runtime;


import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.AppDefinitionService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RuntimeAppDefinitionsResource
{
    @Autowired
    protected AppDefinitionService appDefinitionService;
    
    @RequestMapping(value = { "/rest/editor/app-definitions" }, method = { RequestMethod.GET }, produces = { "application/json" })
    public ResultListDataRepresentation getAppDefinitions() {
    	User currentUser = SecurityUtils.getCurrentUserObject();
        return new ResultListDataRepresentation(this.appDefinitionService.getDeployableAppDefinitions(currentUser));
    }
}

