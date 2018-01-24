package ch.puzzle.itc.mobiliar.business.auditview;

import ch.puzzle.itc.mobiliar.business.auditview.control.GenericAuditHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AuditviewHandler {
    Class<? extends GenericAuditHandler> handler();
}
