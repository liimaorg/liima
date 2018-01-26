package ch.puzzle.itc.mobiliar.business.auditview.control;

import ch.puzzle.itc.mobiliar.business.auditview.entity.Auditable;
import ch.puzzle.itc.mobiliar.business.property.control.PropertyEntityAuditviewHandler;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyDescriptorEntity;
import ch.puzzle.itc.mobiliar.business.property.entity.PropertyEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ProvidedResourceRelationEntity;
import ch.puzzle.itc.mobiliar.business.template.entity.TemplateDescriptorEntity;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Stateless
public class AuditHandlerRegistry {

    @Inject
    @Named("genericAuditHandler")
    GenericAuditHandler genericAuditHandler;

    @Inject
    @Named("propertyEntityAuditviewHandler")
    PropertyEntityAuditviewHandler propertyEntityAuditviewHandler;

    Map<Class<? extends Auditable>, GenericAuditHandler> auditHandlerRegistry;

    @PostConstruct
    public void init() {
        auditHandlerRegistry = new HashMap<>();
        auditHandlerRegistry.put(PropertyEntity.class, propertyEntityAuditviewHandler);
        auditHandlerRegistry.put(PropertyDescriptorEntity.class, genericAuditHandler);
        auditHandlerRegistry.put(ConsumedResourceRelationEntity.class, genericAuditHandler);
        auditHandlerRegistry.put(ProvidedResourceRelationEntity.class, genericAuditHandler);
        auditHandlerRegistry.put(TemplateDescriptorEntity.class, genericAuditHandler);
    }

    public GenericAuditHandler getAuditHandler(Class<?> aClass) throws NoAuditHandlerException {
        GenericAuditHandler handler = this.auditHandlerRegistry.get(aClass);
        if (handler == null) {
            throw new NoAuditHandlerException();
        }
        return handler;
    }
}
