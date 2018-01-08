package ch.puzzle.itc.mobiliar.business.auditview.entity;

public interface Auditable {

    String TYPE_PROPERTY = "Property";
    String TYPE_PROPERTY_DESCRIPTOR = "PropertyDescriptor";

    Integer getId();

    String getNewValueForAuditLog();

    String getType();

    String getNameForAuditLog();
}
