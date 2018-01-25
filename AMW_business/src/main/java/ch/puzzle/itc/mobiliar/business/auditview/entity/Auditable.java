package ch.puzzle.itc.mobiliar.business.auditview.entity;

public interface Auditable {

    String TYPE_PROPERTY = "Property";
    String TYPE_PROPERTY_DESCRIPTOR = "PropertyDescriptor";
    String TYPE_CONSUMED_RESOURCE_RELATION = "Consumed Relation";
    String TYPE_PROVIDED_RESOURCE_RELATION = "Provided Relation";
    String TYPE_TEMPLATE_DESCRIPTOR = "Template Descriptor";

    Integer getId();

    String getNewValueForAuditLog();

    String getType();

    String getNameForAuditLog();
}
