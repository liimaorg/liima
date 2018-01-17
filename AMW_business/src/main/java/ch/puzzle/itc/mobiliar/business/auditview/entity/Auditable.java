package ch.puzzle.itc.mobiliar.business.auditview.entity;

public interface Auditable {

    String TYPE_PROPERTY = "Property";
    String TYPE_PROPERTY_DESCRIPTOR = "PropertyDescriptor";
    String TYPE_CONSUMED_RESOURCE_RELATION = "Consumed Relation";

    Integer getId();

    String getNewValueForAuditLog();

    String getType();

    String getNameForAuditLog();
}
