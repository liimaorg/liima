package ch.puzzle.itc.mobiliar.business.deploy.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ComparatorFilterOption {
    smaller("<", " < ", null, null),
    smallerequals("<=", " <= ", null, null),
    equals("is", " = ", " like ", " is "),
    greaterequals(">=", " >= ", null, null),
    greater(">", " > ", null, null), notequal("is not", " != ", null, " is not ");
    private String displayName;
    private String sqlNumComperator;
    private String sqlStringComperator;
    private String sqlBoolComperator;
}