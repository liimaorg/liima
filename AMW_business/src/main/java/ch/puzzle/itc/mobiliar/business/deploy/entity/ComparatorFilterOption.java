package ch.puzzle.itc.mobiliar.business.deploy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ComparatorFilterOption {
    lt("<", " < ", null, null),
    lte("<=", " <= ", null, null),
    eq("is", " = ", " like ", " is "),
    gte(">=", " >= ", null, null),
    gt(">", " > ", null, null),
    neq("is not", " != ", null, " is not ");
    private String displayName;
    private String sqlNumComperator;
    private String sqlStringComperator;
    private String sqlBoolComperator;
}